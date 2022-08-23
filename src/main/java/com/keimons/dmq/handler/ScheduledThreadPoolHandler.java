package com.keimons.dmq.handler;

import com.keimons.dmq.core.Handler;
import com.keimons.dmq.core.Wrapper;
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
public class ScheduledThreadPoolHandler extends ScheduledThreadPoolExecutor implements Handler<Runnable> {

	private static final RejectedExecutionHandler defaultHandler =
			new ThreadPoolHandler.AdaptPolicy(new ThreadPoolHandler.AbortPolicy());

	public ScheduledThreadPoolHandler(int corePoolSize) {
		super(corePoolSize, defaultHandler);
	}

	public ScheduledThreadPoolHandler(int corePoolSize, @NotNull ThreadFactory threadFactory) {
		super(corePoolSize, threadFactory, defaultHandler);
	}

	public ScheduledThreadPoolHandler(int corePoolSize, @NotNull RejectedHandler<Runnable> handler) {
		super(corePoolSize, new ThreadPoolHandler.AdaptPolicy(handler));
	}

	public ScheduledThreadPoolHandler(int corePoolSize, @NotNull ThreadFactory threadFactory,
									  @NotNull RejectedExecutionHandler handler) {
		super(corePoolSize, threadFactory, handler);
	}

	@Override
	public void handle(Wrapper<Runnable> wrapperTask) {
		this.execute(new ThreadPoolHandler.Task(wrapperTask));
	}

	public void setRejectedHandler(@NotNull RejectedHandler<Runnable> handler) {
		super.setRejectedExecutionHandler(new ThreadPoolHandler.AdaptPolicy(handler));
	}

	@Override
	public void setRejectedExecutionHandler(@NotNull RejectedExecutionHandler handler) {
		super.setRejectedExecutionHandler(handler);
	}
}
