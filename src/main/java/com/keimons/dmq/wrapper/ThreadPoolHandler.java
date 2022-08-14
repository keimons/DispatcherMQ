package com.keimons.dmq.wrapper;

import com.keimons.dmq.core.Handler;
import com.keimons.dmq.core.Wrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * ThreadPoolHandler
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
class ThreadPoolHandler extends ThreadPoolExecutor implements Handler<Wrapper<Runnable>> {

	private static final RejectedExecutionHandler defaultHandler = new AdaptPolicy(new AbortPolicy());

	public ThreadPoolHandler(int corePoolSize, int maximumPoolSize) {
		super(corePoolSize, maximumPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
				Executors.defaultThreadFactory(), defaultHandler);
	}

	public ThreadPoolHandler(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(),
				Executors.defaultThreadFactory(), defaultHandler);
	}

	public ThreadPoolHandler(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit,
							 @NotNull ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(), threadFactory,
				defaultHandler);
	}

	public ThreadPoolHandler(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit,
							 @NotNull RejectedHandler<Wrapper<Runnable>> handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(),
				new AdaptPolicy(handler));
	}

	public ThreadPoolHandler(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit,
							 @NotNull ThreadFactory threadFactory,
							 @NotNull RejectedHandler<Wrapper<Runnable>> handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(), threadFactory,
				new AdaptPolicy(handler));
	}

	public void setRejectedHandler(@NotNull RejectedHandler<Wrapper<Runnable>> handler) {
		super.setRejectedExecutionHandler(new AdaptPolicy(handler));
	}

	@Override
	public void setRejectedExecutionHandler(@NotNull RejectedExecutionHandler handler) {
		super.setRejectedExecutionHandler(handler);
	}

	@Override
	public void handle(Wrapper<Runnable> wrapperTask) {
		this.execute(new Task(wrapperTask));
	}

	private record Task(Wrapper<Runnable> wrapperTask) implements Runnable {

		@Override
		public void run() {
			wrapperTask.invoke();
		}
	}

	public static class AdaptPolicy implements RejectedExecutionHandler {

		private final RejectedHandler<Wrapper<Runnable>> handler;

		public AdaptPolicy(RejectedHandler<Wrapper<Runnable>> handler) {
			Objects.requireNonNull(handler);
			this.handler = handler;
		}

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			handler.rejectedHandle(((Task) r).wrapperTask, (ThreadPoolHandler) executor);
		}
	}

	public static class AbortPolicy implements RejectedHandler<Wrapper<Runnable>> {

		@Override
		public void rejectedHandle(Wrapper<Runnable> wrapperTask, Handler<Wrapper<Runnable>> executor) {
			wrapperTask.cancel();
			throw new RejectedExecutionException("Task " + wrapperTask + " rejected from " + executor.toString());
		}
	}

	public static class DiscardPolicy implements RejectedHandler<Wrapper<Runnable>> {

		public void rejectedHandle(Wrapper<Runnable> wrapperTask, Handler<Wrapper<Runnable>> executor) {
			wrapperTask.cancel();
		}
	}
}
