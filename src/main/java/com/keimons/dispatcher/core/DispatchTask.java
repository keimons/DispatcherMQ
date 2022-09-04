package com.keimons.dispatcher.core;

import org.jetbrains.annotations.ApiStatus;

/**
 * 调度任务
 * <p>
 * 调度任务包含了任务相关的所有信息，根据这些信息，可以对任务的执行重排序。这些任务信息包含：
 * <ol>
 *     <li>任务；</li>
 *     <li>任务执行屏障（可能有多个）；</li>
 *     <li>任务处理器；</li>
 *     <li>拦截器信息。</li>
 * </ol>
 * 调度器中，一个调度任务有可能会被多个{@link Sequencer 定序器}持有，持有的形式包括：
 * <ul>
 *     <li>执行屏障，此时仅作为屏障，当拦截器释放时，屏障移除。</li>
 *     <li>缓存节点，当节点无法重排序到屏障之前时，将节点缓存，等待屏障释放后才能开始处理此节点。</li>
 *     <li>执行任务，此任务由最后一个碰到它的线程执行。</li>
 * </ul>
 * 同一个拦截器可以被多个线程持有，但每个线程所持的形式同时只会存在一种，这三种状态是相互冲突的。
 * <p>
 * 同时，可运行的拦截器可以判断一个任务是否能由此线程执行，只有指定的执行线程才能执行这个任务，否则，忽略这个它。
 * <p>
 * <p>
 * 调度任务，是整个设计的核心，它体现了最重要的两个概念：
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
	 * 交付任务
	 * <p>
	 * 将任务交付至任务处理器，并在将来的合适的时机执行。
	 *
	 * @see Handler 任务处理器
	 */
	void deliveryTask();

	/**
	 * 激活任务
	 * <p>
	 * 使任务重新回到活跃的状态。需要在以下时刻激活任务：
	 * <ul>
	 *     <li>任务被{@link Sequencer 定序器}消费后；</li>
	 *     <li>任务被{@link Handler 处理器}成功处理后；</li>
	 *     <li>任务取消执行后。</li>
	 * </ul>
	 * 调度任务内部包含定序器，激活任务的同时，也会激活定序器。定序器重新判定调度任务的状态，
	 * 使得执行屏障失效，从而触发后续调度任务的执行。
	 */
	void activateTask();

	/**
	 * 判断是否依赖另一个任务
	 * <p>
	 * 如果一个任务依赖另一个任务，那么这两个任务不能重排序执行，否则，两个不相关的任务，
	 * 可以重排序执行。
	 *
	 * @param task 要判断是否依赖的任务
	 * @return {@code true}依赖这个任务
	 */
	boolean dependsOn(DispatchTask task);

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
