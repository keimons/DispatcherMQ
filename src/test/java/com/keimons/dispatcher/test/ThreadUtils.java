package com.keimons.dispatcher.test;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工具
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class ThreadUtils {

	public static void printThreads() {
		ThreadGroup group = Thread.currentThread().getThreadGroup();
		int count = group.activeCount();
		Thread[] threads = new Thread[count];
		group.enumerate(threads);
		for (Thread thread : threads) {
			System.out.println(thread);
		}
	}

	public static ThreadFactory createThreadFactory(ThreadGroup group) {
		return new DefaultThreadFactory(group);
	}

	private static class DefaultThreadFactory implements ThreadFactory {
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);

		public DefaultThreadFactory(ThreadGroup group) {
			this.group = group;
		}

		public Thread newThread(@NotNull Runnable r) {
			return new Thread(group, r, "dispatcher-thread-" + threadNumber.getAndIncrement(), 0);
		}
	}
}
