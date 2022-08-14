package com.keimons.dmq.internal;

import com.keimons.dmq.core.CompositeHandler;
import com.keimons.dmq.core.Dispatcher;
import com.keimons.dmq.core.Handler;
import com.keimons.dmq.core.Wrapper;
import com.keimons.dmq.utils.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.OptionalInt;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 复合执行调度器
 *
 * <ul>
 *     <li><b>单机模式</b>：调度范围是当前进程，并且所有任务的执行都在本进程执行。</li>
 *     <li><b>集群模式</b>：调度范围是集群中的一部分，所有任务的执行，可能是在当前进程，也可能是在远程。</li>
 * </ul>
 * 在中
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class DefaultCompositeHandler<E extends Enum<E>> implements CompositeHandler<E>, Dispatcher<Runnable> {

	private final int nThreads;

	private final Handler<Wrapper<Runnable>>[] handlers;

	private final Invoker[] invokers;

	private final Lock main = new ReentrantLock();

	/**
	 * 构造复合执行调度器
	 * <p>
	 * 带有复合执行器的调度器不仅仅可以用于单机模式，还可以用于集群模式。
	 * 参数满足：{@code 0 <= start < end <= nThread}。
	 *
	 * @param nThreads 总线程数量
	 * @param start    当前调度器开始位置
	 * @param end      当前调度器结束位置
	 * @param type     排序类型
	 * @param handlers 复合执行器
	 */
	public DefaultCompositeHandler(int nThreads, int start, int end, SerialMode type,
								   EnumMap<E, Handler<Wrapper<Runnable>>> handlers) {
		// 0 <= start < end <= nThread
		if (!(0 <= start && start < end && end <= nThreads) || handlers == null || handlers.size() < 1) {
			throw new IllegalArgumentException();
		}
		OptionalInt max = handlers.keySet().stream().mapToInt(Enum::ordinal).max();
		this.nThreads = nThreads;
		this.invokers = new Invoker[nThreads];
		this.handlers = ArrayUtils.newInstance(Handler.class, max.getAsInt() + 1);
		IntStream.range(start, end).forEach(index -> invokers[index] = new Invoker());
		IntStream.range(start, end).forEach(index -> invokers[index].start());
		handlers.forEach((key, value) -> this.handlers[key.ordinal()] = value);
	}

	protected void dispatch(int type, Runnable task, Object fence) {
		Invoker invoker = invokers[fence.hashCode() % nThreads];
		Handler<Wrapper<Runnable>> handler = handlers[type];
		var wrapperTask = new WrapperTask1(handler, task, fence, invoker);
		invoker.offer(wrapperTask);
	}

	protected void dispatch(int type, Runnable task, Object fence0, Object fence1) {
		Invoker invoker0 = invokers[fence0.hashCode() % nThreads];
		Invoker invoker1 = invokers[fence1.hashCode() % nThreads];
		Handler<Wrapper<Runnable>> handler = handlers[type];
		var wrapperTask = new WrapperTask2(handler, task, fence0, fence1, invoker0, invoker1);
		if (invoker0 == invoker1) {
			invoker0.offer(wrapperTask);
		} else {
			main.lock();
			try {
				invoker0.offer(wrapperTask);
				invoker1.offer(wrapperTask);
			} finally {
				main.unlock();
			}
		}
	}

	protected void dispatch(int type, Runnable task, Object fence0, Object fence1, Object fence2) {
		Invoker invoker0 = invokers[fence0.hashCode() % nThreads];
		Invoker invoker1 = invokers[fence1.hashCode() % nThreads];
		Invoker invoker2 = invokers[fence2.hashCode() % nThreads];
		Handler<Wrapper<Runnable>> handler = handlers[type];
		var wrapperTask = new WrapperTask3(handler, task, fence0, invoker0, fence1, invoker1, fence2, invoker2);
		if (invoker0 == invoker1) {
			if (invoker0 == invoker2) {
				invoker0.offer(wrapperTask);
			} else {
				main.lock();
				try {
					invoker0.offer(wrapperTask);
					invoker2.offer(wrapperTask);
				} finally {
					main.unlock();
				}
			}
		} else if (invoker0 == invoker2 || invoker1 == invoker2) {
			main.lock();
			try {
				invoker0.offer(wrapperTask);
				invoker1.offer(wrapperTask);
			} finally {
				main.unlock();
			}
		} else {
			main.lock();
			try {
				invoker0.offer(wrapperTask);
				invoker1.offer(wrapperTask);
				invoker2.offer(wrapperTask);
			} finally {
				main.unlock();
			}
		}
	}

	protected void dispatch(int type, Runnable task, Object... fences) {
		switch (fences.length) {
			case 1 -> dispatch(type, task, fences[0]);
			case 2 -> dispatch(type, task, fences[0], fences[1]);
			case 3 -> dispatch(type, task, fences[0], fences[1], fences[2]);
			default -> {
				Stream<Invoker> stream = Stream.of(fences).map(o -> invokers[o.hashCode() % nThreads]).distinct();
				Invoker[] invokers = stream.toArray(Invoker[]::new);
				Handler<Wrapper<Runnable>> handler = handlers[type];
				var wrapperTask = new WrapperTaskX(handler, task, fences, invokers);
				if (invokers.length <= 1) {
					invokers[0].offer(wrapperTask);
				} else {
					main.lock();
					try {
						for (Invoker invoker : invokers) {
							invoker.offer(wrapperTask);
						}
					} finally {
						main.unlock();
					}
				}
			}
		}
	}

	@Override
	public void dispatch(@NotNull Runnable task, @NotNull Object fence) {
		dispatch(0, task, fence);
	}

	@Override
	public void dispatch(@NotNull Runnable task, @NotNull Object fence0, @NotNull Object fence1) {
		dispatch(0, task, fence0, fence1);
	}

	@Override
	public void dispatch(@NotNull Runnable task, @NotNull Object fence0, @NotNull Object fence1,
						 @NotNull Object fence2) {
		dispatch(0, task, fence0, fence1, fence2);
	}

	@Override
	public void dispatch(@NotNull Runnable task, @NotNull Object... fences) {
		dispatch(0, task, fences);
	}

	@Override
	public void dispatch(@NotNull E type, @NotNull Runnable task, @NotNull Object fence) {
		dispatch(type.ordinal(), task, fence);
	}

	@Override
	public void dispatch(@NotNull E type, @NotNull Runnable task, @NotNull Object fence0, @NotNull Object fence1) {
		dispatch(type.ordinal(), task, fence0, fence1);
	}

	@Override
	public void dispatch(@NotNull E type, @NotNull Runnable task, @NotNull Object fence0, @NotNull Object fence1,
						 @NotNull Object fence2) {
		dispatch(type.ordinal(), task, fence0, fence1, fence2);
	}

	@Override
	public void dispatch(@NotNull E type, @NotNull Runnable task, @NotNull Object... fences) {
		dispatch(type.ordinal(), task, fences);
	}
}
