package com.keimons.dmq.core;

import org.jetbrains.annotations.Nullable;

/**
 * 定序器
 * <p>
 * 定序器通常用于对调度任务进行重排序。在不破坏 <b>{@code as-if-serial语义}</b> 和
 * <b>{@code happens-before原则}</b> 的前提下，打乱调度任务的执行顺序，进而提高执行效率。
 * 通常情况下，定序器在遇到这三种调度任务时会发生重排序：
 * <ul>
 *     <li>联合任务：联合任务被多个定序器共享，调度器对于联合任务的设定是：最后一个处理联合任务的定序器联执行此任务。
 *     其它定序器在等待期间，可以提前执行后续任务。</li>
 *     <li>依赖任务：被依赖的任务完成后，才能执行此任务，等待期间，调度器可以提前执行后续任务。</li>
 *     <li>耗时任务：耗时任务通常被调度到某个处理器中执行，任务执行期间定序器空闲，可以执行其他任务。</li>
 * </ul>
 * 真实任务可以在定序器中直接执行，但在执行期间，此定序器将被阻塞，无法调度任务。
 * <p>
 * 调度任务重排序规则请参考：<b>{@link DispatchTask 调度任务}</b>。
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public interface Sequencer {

	boolean isEmpty();

	/**
	 * 启动一个调度任务
	 * <p>
	 * 调度任务可能在定序器中直接执行，也可能被调度到其它处理器中执行。
	 *
	 * @param dispatchTask 调度任务
	 */
	void actuate(DispatchTask dispatchTask);

	/**
	 * 释放一个调度任务
	 *
	 * @param dispatchTask 调度任务
	 */
	void release(@Nullable DispatchTask dispatchTask);
}
