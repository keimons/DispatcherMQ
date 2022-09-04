package com.keimons.dispatcher.core.handler;

import com.keimons.dispatcher.core.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * Handlers
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class Handlers {

	public static Handler<Runnable> newDirectHandler() {
		return new DirectHandler();
	}

	public static Handler<Runnable> newFixedThreadHandler(int nThreads) {
		return new ThreadPoolHandler(nThreads, nThreads);
	}

	public static Handler<Runnable> newFixedThreadHandler(
			int nThreads, @NotNull RejectedDeliveryHandler<Runnable> handler) {
		return new ThreadPoolHandler(nThreads, nThreads, 0, TimeUnit.MILLISECONDS, handler);
	}

	public static Handler<Runnable> newCachedThreadHandler(int corePoolSize, int maximumPoolSize) {
		return new ThreadPoolHandler(corePoolSize, maximumPoolSize, 60, TimeUnit.SECONDS);
	}

	public static Handler<Runnable> newCachedThreadHandler(
			int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit,
			@NotNull RejectedDeliveryHandler<Runnable> handler) {
		return new ThreadPoolHandler(corePoolSize, maximumPoolSize, keepAliveTime, unit, handler);
	}
}
