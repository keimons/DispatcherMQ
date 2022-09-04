package com.keimons.dispatcher.core.handler;

import com.keimons.dispatcher.core.Handler;
import com.keimons.dispatcher.core.Wrapper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * ScheduledThreadPoolHandler
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
@ApiStatus.Experimental
public class ScheduledThreadPoolHandler extends ScheduledThreadPoolExecutor implements Handler<Runnable> {

	private static final RejectedExecutionHandler defaultHandler =
			new ThreadPoolHandler.AdaptPolicy(Policies.newBlockingCallerPolicy());

	public ScheduledThreadPoolHandler(int corePoolSize) {
		super(corePoolSize, defaultHandler);
	}

	public ScheduledThreadPoolHandler(int corePoolSize, @NotNull ThreadFactory threadFactory) {
		super(corePoolSize, threadFactory, defaultHandler);
	}

	public ScheduledThreadPoolHandler(int corePoolSize, @NotNull RejectedDeliveryHandler<Runnable> handler) {
		super(corePoolSize, new ThreadPoolHandler.AdaptPolicy(handler));
	}

	public ScheduledThreadPoolHandler(int corePoolSize, @NotNull ThreadFactory threadFactory,
									  @NotNull RejectedExecutionHandler handler) {
		super(corePoolSize, threadFactory, handler);
	}

	@Override
	public void handle0(Wrapper<Runnable> wrapperTask) {
		this.execute(new ThreadPoolHandler.Task(wrapperTask));
	}

	public void setRejectedHandler(@NotNull RejectedDeliveryHandler<Runnable> handler) {
		super.setRejectedExecutionHandler(new ThreadPoolHandler.AdaptPolicy(handler));
	}

	@Override
	public void setRejectedExecutionHandler(@NotNull RejectedExecutionHandler handler) {
		super.setRejectedExecutionHandler(handler);
	}
}
