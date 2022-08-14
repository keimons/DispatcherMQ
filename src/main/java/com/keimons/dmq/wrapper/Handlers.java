package com.keimons.dmq.wrapper;

import com.keimons.dmq.core.Handler;
import com.keimons.dmq.core.Wrapper;
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

	public static Handler<Wrapper<Runnable>> newDirectHandler() {
		return new DirectHandler();
	}

	public static Handler<Wrapper<Runnable>> newFixedThreadHandler(int nThreads) {
		return new ThreadPoolHandler(nThreads, nThreads);
	}

	public static Handler<Wrapper<Runnable>> newFixedThreadHandler(
			int nThreads, @NotNull RejectedHandler<Wrapper<Runnable>> handler) {
		return new ThreadPoolHandler(nThreads, nThreads, 0, TimeUnit.MILLISECONDS, handler);
	}

	public static Handler<Wrapper<Runnable>> newCachedThreadHandler(int corePoolSize, int maximumPoolSize) {
		return new ThreadPoolHandler(corePoolSize, maximumPoolSize, 60, TimeUnit.SECONDS);
	}

	public static Handler<Wrapper<Runnable>> newCachedThreadHandler(
			int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit,
			@NotNull RejectedHandler<Wrapper<Runnable>> handler) {
		return new ThreadPoolHandler(corePoolSize, maximumPoolSize, keepAliveTime, unit, handler);
	}
}
