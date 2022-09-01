package com.keimons.dmq.internal;

import com.keimons.dmq.core.*;
import com.keimons.dmq.utils.ArrayUtils;
import com.keimons.dmq.utils.MiscUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.VarHandle;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
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

	/**
	 * 未启动
	 * <p>
	 * 尚未启动的调度器
	 */
	protected static final int NEW = Integer.MAX_VALUE;

	/**
	 * 运行中
	 * <p>
	 * 接受新的调度任务并处理排队任务。
	 */
	protected static final int RUNNING = 0;

	/**
	 * 关闭中
	 * <p>
	 * 不接受新任务，但处理排队中的任务。
	 */
	protected static final int SHUTDOWN = 1;

	/**
	 * 已终结
	 * <p>
	 * 生命周期的最后一个状态，不接受新任务，不处理排队任务，中断正在进行的任务，尽可能的快速退出。
	 */
	protected static final int TERMINATED = 2;

	private static final VarHandle VV = MiscUtils.findVarHandle(DefaultCompositeHandler.class, "state", int.class);

	private final int nThreads;

	private final ThreadFactory threadFactory;

	private final Serialization serialization;

	private final Handler<Runnable>[] handlers;

	private final Sequencer[] sequencers;

	/**
	 * 线程池状态
	 * <p>
	 * 用于提供生命周期的所有状态。{@code state}的取值有：
	 * <ul>
	 *     <li>{@link #RUNNING 运行中}：接受新任务，处理排队任务。</li>
	 *     <li>{@link #SHUTDOWN 关闭中}：不接受新任务，但处理排队任务。</li>
	 *     <li>{@link #TERMINATED 已终结}：任务全部处理完成，且定序器已经关闭。</li>
	 * </ul>
	 */
	protected volatile int state = RUNNING;

	/**
	 * 构造复合执行调度器
	 * <p>
	 * 带有复合处理器的调度器不仅仅可以用于单机模式，还可以用于集群模式。
	 * 参数满足：{@code 0 <= start < end <= nThread}。
	 *
	 * @param nThreads      总线程数量
	 * @param start         当前调度器开始位置
	 * @param end           当前调度器结束位置
	 * @param serialization 排序类型
	 * @param handlers      复合处理器
	 */
	public DefaultCompositeHandler(int nThreads, int start, int end, Serialization serialization,
								   ThreadFactory threadFactory, EnumMap<E, Handler<Runnable>> handlers) {
		// 0 <= start < end <= nThread
		if (!(0 <= start && start < end && end <= nThreads) || handlers == null || handlers.size() < 1) {
			throw new IllegalArgumentException();
		}
		OptionalInt max = handlers.keySet().stream().mapToInt(Enum::ordinal).max();
		this.nThreads = nThreads;
		this.threadFactory = threadFactory;
		this.serialization = serialization;
		this.sequencers = ArrayUtils.newInstance(Sequencer.class, nThreads);
		this.handlers = ArrayUtils.newInstance(Handler.class, max.getAsInt() + 1);
		for (int i = start; i < end; i++) {
			ThreadSequencer sequencer = new ThreadSequencer();
			sequencer.start();
			sequencers[i] = sequencer;
		}
		handlers.forEach((key, value) -> this.handlers[key.ordinal()] = value);
	}

	/**
	 * 更新最低运行状态
	 *
	 * @param stateAtLeast 最低运行状态
	 */
	private void casStateAtLeast(int stateAtLeast) {
		int v;
		do {
			v = state;
			if (v >= stateAtLeast) {
				return;
			}
		} while (!VV.compareAndSet(this, v, stateAtLeast));
	}

	private void dispatch(int type, Runnable task, Object fence) {
		Sequencer sequencer = sequencers[fence.hashCode() % nThreads];
		Handler<Runnable> handler = handlers[type];
		var wrapperTask = new DispatchTask1(handler, task, fence, sequencer);
		sequencer.actuate(wrapperTask);
	}

	private void dispatch(int type, Runnable task, Object fence0, Object fence1) {
		Sequencer sequencer0 = sequencers[fence0.hashCode() % nThreads];
		Sequencer sequencer1 = sequencers[fence1.hashCode() % nThreads];
		Handler<Runnable> handler = handlers[type];
		var wrapperTask = new DispatchTask2(handler, task, fence0, fence1, sequencer0, sequencer1);
		if (sequencer0 == sequencer1) {
			serialization.dispatch(wrapperTask, sequencer0);
		} else {
			serialization.dispatch(wrapperTask, sequencer0, sequencer1);
		}
	}

	private void dispatch(int type, Runnable task, Object fence0, Object fence1, Object fence2) {
		if (state > RUNNING) {
			return;
		}
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

	private void dispatch(int type, Runnable task, Object... fences) {
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

	@Override
	public boolean isShutdown() {
		if (state == RUNNING) {
			return false;
		}
		if (state == TERMINATED) {
			return true;
		}
		for (int i = 0; i < sequencers.length; i++) {
			if (!sequencers[i].isShutdown()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void shutdown() {
		this.shutdown(-1, TimeUnit.MILLISECONDS);
	}

	@Override
	public void shutdown(long timeout, TimeUnit timeUnit) {
		casStateAtLeast(SHUTDOWN);
		Stream.of(sequencers).filter(Objects::nonNull).forEach(sequencer -> sequencer.release(null));
		final long timeOutAt = System.currentTimeMillis() + timeUnit.toMillis(timeout);
		while (!isShutdown()) {
			if (timeout >= 0 && System.currentTimeMillis() > timeOutAt) {
				throw new TimeoutException();
			}
			Thread.yield();
		}
		casStateAtLeast(TERMINATED);
	}

	/**
	 * 使用单独线程的定序器
	 * <p>
	 * 启动一个单独的线程，用于对任务进行排队。
	 *
	 * @author houyn[monkey@keimons.com]
	 * @version 1.0
	 * @since 17
	 */
	private class ThreadSequencer implements Sequencer, Runnable {

		BlockingDeque<DispatchTask> queue = new LinkedBlockingDeque<>();

		List<DispatchTask> fences = new LinkedList<>();

		/**
		 * 乐观同步器
		 */
		Sync sync = new Sync();

		/**
		 * 定序器是否已关闭
		 */
		boolean shutdown = false;

		/**
		 * 启动一个线程
		 */
		private void start() {
			Thread thread = threadFactory.newThread(this);
			thread.start();
			sync.setThread(thread);
			sync.acquireWrite();
		}

		/**
		 * 定序器退出
		 * <p>
		 * 当发生异常或关闭调度器时，定序器才会退出。发生退出时：
		 * <ul>
		 *     <li><b>异常退出</b>：删除当前线程，并启动一个新线程。</li>
		 *     <li><b>关闭调度器</b>：退出当前线程。</li>
		 * </ul>
		 *
		 * @param throwable {@code true}异常退出，{@code false}调度器关闭
		 */
		private void exit(boolean throwable) {
			if (throwable && state < SHUTDOWN) {
				start();
			} else {
				shutdown = true;
			}
		}

		@Override
		public boolean isShutdown() {
			return shutdown;
		}

		@Override
		public void actuate(DispatchTask dispatchTask) {
			queue.offer(dispatchTask);
			sync.acquireWrite();
		}

		@Override
		public void release(@Nullable DispatchTask dispatchTask) {
			sync.acquireWrite();
		}

		private boolean isDelivery(DispatchTask task) {
			// 判断任务是否可以越过所有屏障执行
			for (int i = 0, length = fences.size(); i < length; i++) {
				if (task.dependsOn(fences.get(i))) {
					return false;
				}
			}
			return true;
		}

		private @Nullable DispatchTask next() {
			DispatchTask task = null;
			for (; ; ) {
				// 状态检测，如果线程池已停止
				if (state >= SHUTDOWN && queue.isEmpty()) {
					return null;
				}
				Sync sync = this.sync;
				int stamp = sync.acquireRead();
				fences.removeIf(fence -> !fence.isIntercepted());
				Iterator<DispatchTask> iterator = queue.iterator();
				while (iterator.hasNext()) {
					task = iterator.next();
					if (isDelivery(task)) {
						// 这个任务已经可以执行了，所以，从缓存的队列中移除任务
						iterator.remove();
						if (task.tryIntercept()) {
							fences.add(task);
							task.wakeup();
						} else {
							return task;
						}
					}
				}
				if (task == null) {
					sync.validate(stamp);
				}
			}
		}

		@Override
		public void run() {
			boolean throwable = true;
			try {
				DispatchTask task;
				while ((task = next()) != null) {
					try {
						task.deliver();
					} finally {
						if (task.isIntercepted()) {
							fences.add(task);
						}
					}
				}
				throwable = false;
			} finally {
				exit(throwable);
			}
		}
	}
}
