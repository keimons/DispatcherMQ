package com.keimons.dispatcher.core.handler;

import com.keimons.dispatcher.core.Handler;
import com.keimons.dispatcher.core.Wrapper;
import org.jetbrains.annotations.ApiStatus;
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
@ApiStatus.Experimental
public class ThreadPoolHandler extends ThreadPoolExecutor implements Handler<Runnable> {

	private static final RejectedExecutionHandler defaultHandler = new AdaptPolicy(Policies.newBlockingCallerPolicy());

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
							 @NotNull RejectedDeliveryHandler<Runnable> handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(),
				new AdaptPolicy(handler));
	}

	public ThreadPoolHandler(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit,
							 @NotNull BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, defaultHandler);
	}

	public ThreadPoolHandler(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit,
							 @NotNull ThreadFactory threadFactory,
							 @NotNull RejectedDeliveryHandler<Runnable> handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(), threadFactory,
				new AdaptPolicy(handler));
	}

	public ThreadPoolHandler(int corePoolSize,
							 int maximumPoolSize,
							 long keepAliveTime,
							 @NotNull TimeUnit unit,
							 BlockingQueue<Runnable> workQueue,
							 @NotNull ThreadFactory threadFactory,
							 @NotNull RejectedDeliveryHandler<Runnable> handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, new AdaptPolicy(handler));
	}

	public void setRejectedHandler(@NotNull RejectedDeliveryHandler<Runnable> handler) {
		super.setRejectedExecutionHandler(new AdaptPolicy(handler));
	}

	@Override
	public void setRejectedExecutionHandler(@NotNull RejectedExecutionHandler handler) {
		super.setRejectedExecutionHandler(handler);
	}

	@Override
	public void handle0(Wrapper<Runnable> wrapperTask) {
		this.execute(new Task(wrapperTask));
	}

	record Task(Wrapper<Runnable> wrapperTask) implements Runnable {

		@Override
		public void run() {
			wrapperTask.invoke();
		}
	}

	public static class AdaptPolicy implements RejectedExecutionHandler {

		private final RejectedDeliveryHandler<Runnable> handler;

		public AdaptPolicy(RejectedDeliveryHandler<Runnable> handler) {
			Objects.requireNonNull(handler);
			this.handler = handler;
		}

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			handler.rejectedDelivery(((Task) r).wrapperTask, (ThreadPoolHandler) executor);
		}
	}
}
