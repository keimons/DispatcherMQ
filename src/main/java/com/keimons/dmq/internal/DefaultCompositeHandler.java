package com.keimons.dmq.internal;

import com.keimons.dmq.core.*;
import com.keimons.dmq.utils.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.OptionalInt;
import java.util.concurrent.ThreadFactory;
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

	private final Serialization serialization;

	private final Handler<Runnable>[] handlers;

	private final Sequencer[] sequencers;

	/**
	 * 构造复合执行调度器
	 * <p>
	 * 带有复合执行器的调度器不仅仅可以用于单机模式，还可以用于集群模式。
	 * 参数满足：{@code 0 <= start < end <= nThread}。
	 *
	 * @param nThreads      总线程数量
	 * @param start         当前调度器开始位置
	 * @param end           当前调度器结束位置
	 * @param serialization 排序类型
	 * @param handlers      复合执行器
	 */
	public DefaultCompositeHandler(int nThreads, int start, int end, Serialization serialization,
								   ThreadFactory threadFactory, SequencerFactory sequencerFactory,
								   EnumMap<E, Handler<Runnable>> handlers) {
		// 0 <= start < end <= nThread
		if (!(0 <= start && start < end && end <= nThreads) || handlers == null || handlers.size() < 1) {
			throw new IllegalArgumentException();
		}
		OptionalInt max = handlers.keySet().stream().mapToInt(Enum::ordinal).max();
		this.nThreads = nThreads;
		this.serialization = serialization;
		this.sequencers = ArrayUtils.newInstance(Sequencer.class, nThreads);
		this.handlers = ArrayUtils.newInstance(Handler.class, max.getAsInt() + 1);
		IntStream.range(start, end).forEach(index -> sequencers[index] = sequencerFactory.newSequencer(threadFactory));
		handlers.forEach((key, value) -> this.handlers[key.ordinal()] = value);
	}

	protected void dispatch(int type, Runnable task, Object fence) {
		Sequencer sequencer = sequencers[fence.hashCode() % nThreads];
		Handler<Runnable> handler = handlers[type];
		var wrapperTask = new DispatchTask1(handler, task, fence, sequencer);
		sequencer.actuate(wrapperTask);
	}

	protected void dispatch(int type, Runnable task, Object fence0, Object fence1) {
		Sequencer sequencer0 = sequencers[fence0.hashCode() % nThreads];
		Sequencer sequencer1 = sequencers[fence1.hashCode() % nThreads];
		Handler<Runnable> handler = handlers[type];
		var wrapperTask = new DispatchTask2(handler, task, fence0, fence1, sequencer0, sequencer1);
		if (sequencer0 == sequencer1) {
			serialization.dispatch(wrapperTask);
		} else {
			serialization.dispatch(wrapperTask, sequencer0, sequencer1);
		}
	}

	protected void dispatch(int type, Runnable task, Object fence0, Object fence1, Object fence2) {
		Sequencer sequencer0 = sequencers[fence0.hashCode() % nThreads];
		Sequencer sequencer1 = sequencers[fence1.hashCode() % nThreads];
		Sequencer sequencer2 = sequencers[fence2.hashCode() % nThreads];
		Handler<Runnable> handler = handlers[type];
		var wrapperTask = new DispatchTask3(handler, task, fence0, sequencer0, fence1, sequencer1, fence2, sequencer2);
		if (sequencer0 == sequencer1) {
			if (sequencer0 == sequencer2) {
				serialization.dispatch(wrapperTask, sequencer0);
			} else {
				serialization.dispatch(wrapperTask, sequencer0, sequencer2);
			}
		} else if (sequencer0 == sequencer2 || sequencer1 == sequencer2) {
			serialization.dispatch(wrapperTask, sequencer0, sequencer1);
		} else {
			serialization.dispatch(wrapperTask, sequencer0, sequencer1, sequencer2);
		}
	}

	protected void dispatch(int type, Runnable task, Object... fences) {
		switch (fences.length) {
			case 1 -> dispatch(type, task, fences[0]);
			case 2 -> dispatch(type, task, fences[0], fences[1]);
			case 3 -> dispatch(type, task, fences[0], fences[1], fences[2]);
			default -> {
				Stream<Sequencer> stream =
						Stream.of(fences).map(o -> this.sequencers[o.hashCode() % nThreads]).distinct();
				Sequencer[] sequencers = stream.toArray(Sequencer[]::new);
				Handler<Runnable> handler = handlers[type];
				var wrapperTask = new DispatchTaskX(handler, task, fences, sequencers);
				serialization.dispatch(wrapperTask, sequencers);
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
