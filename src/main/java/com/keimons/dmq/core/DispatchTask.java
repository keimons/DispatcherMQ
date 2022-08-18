package com.keimons.dmq.core;

import org.jetbrains.annotations.ApiStatus;

/**
 * 可运行的拦截器
 * <p>
 * 它包含了任务相关的所有信息，根据这些信息，可以对任务的执行做一些小手脚。这些任务信息包含：
 * <ol>
 *     <li>任务；</li>
 *     <li>任务执行屏障（可能有多个）；</li>
 *     <li>任务执行器（可能有多个）；</li>
 *     <li>拦截器信息。</li>
 * </ol>
 * 在多线程环境下，它有可能会被多个工作线程持有，持有的形式包括：
 * <ul>
 *     <li>执行屏障，此时仅作为屏障，当拦截器释放时，屏障移除。</li>
 *     <li>缓存节点，当节点无法重排序到屏障之前时，将节点缓存，等待屏障释放后才能开始处理此节点。</li>
 *     <li>执行任务，此任务由最后一个碰到它的线程执行。</li>
 * </ul>
 * 同一个拦截器可以被多个线程持有，但每个线程所持的形式同时只会存在一种，这三种状态是相互冲突的。
 * <p>
 * 同时，可运行的拦截器可以判断一个任务是否能由此线程处理，只有指定的执行线程才能处理这个任务，否则，忽略这个它。
 * <p>
 * <p>
 * 可运行的拦截器，是整个设计的核心，它体现了最重要的两个概念：
 * <ol>
 *     <li>带有相同执行屏障的任务必须串行执行；</li>
 *     <li>带有不同执行屏障的任务可以重排序执行。</li>
 * </ol>
 * 串行设计，保证执行的稳定性，提升系统的吞吐量。重排序执行
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
@ApiStatus.Experimental
public interface DispatchTask extends Interceptor {

	/**
	 * 判断是否依赖另一个任务
	 *
	 * @param task 要判断是否依赖的任务
	 * @return {@code true}依赖这个任务
	 */
	boolean dependsOn(DispatchTask task);

	/**
	 * 投递任务
	 * <p>
	 * 将任务投递至任务处理器，并在将来的合适的时机执行。
	 */
	void deliver();

	/**
	 * 唤醒任务
	 */
	void wakeup();

	/**
	 * 判断是否依赖另一个屏障
	 *
	 * @param fence 要判断是否依赖的屏障
	 * @return {@code true}依赖这个任务
	 */
	boolean dependsOn(Object fence);

	default boolean dependsOn(Object fence0, Object fence1) {
		return dependsOn(fence0) || dependsOn(fence1);
	}

	default boolean dependsOn(Object fence0, Object fence1, Object fence2) {
		return dependsOn(fence0) || dependsOn(fence1) || dependsOn(fence2);
	}

	default boolean dependsOn(Object fence0, Object fence1, Object fence2, Object fence3) {
		return dependsOn(fence0) || dependsOn(fence1) || dependsOn(fence2) || dependsOn(fence3);
	}

	default boolean dependsOn(Object fence0, Object fence1, Object fence2, Object fence3, Object fence4) {
		return dependsOn(fence0) || dependsOn(fence1) || dependsOn(fence2) || dependsOn(fence3) || dependsOn(fence4);
	}

	default boolean dependsOn(Object... fences) {
		for (Object fence : fences) {
			if (dependsOn(fence)) {
				return true;
			}
		}
		return false;
	}
}
