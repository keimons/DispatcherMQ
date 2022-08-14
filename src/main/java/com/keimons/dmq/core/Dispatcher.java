package com.keimons.dmq.core;

import org.jetbrains.annotations.NotNull;

/**
 * 任务调度器
 * <p>
 * 调度器负责调度任务。任务的执行可能是在调度器中直接完成，也可能调度到复合线线程池中完成。
 * <pre>{@code
 * Dispatcher dispatcher = ...
 * dispatcher.dispatch(() -> {}, "any");
 * }</pre>
 * 调度器如何调度任务，取决于实现具体的实现，可以在调度器中直接执行：
 * <pre>{@code
 * class DirectDispatcher implements Dispatcher<Runnable> {
 *
 *     @Override
 *     public void dispatch(Runnable runnable, Object fence) {
 *         task.run();
 *     }
 * }
 * }</pre>
 * 如果想要实现任务调度，则至少应该有一组线程调度任务，调度任务的线程称为：<b>调度线程</b>。
 * 允许任务在调度线程直接执行，或派遣到其它线程（池）中执行。
 * <p>
 * 需要注意的是，如果在调度线程中执行，调度线程将被占用，暂时无法调度任务。
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public interface Dispatcher<T> {

	/**
	 * 调度一个任务
	 *
	 * @param task  待调度的任务
	 * @param fence 任务执行屏障
	 */
	void dispatch(@NotNull T task, Object fence);

	/**
	 * 调度一个任务
	 *
	 * @param task   待调度的任务
	 * @param fence0 任务执行屏障
	 * @param fence1 任务执行屏障
	 */
	void dispatch(@NotNull T task, Object fence0, Object fence1);

	/**
	 * 调度一个任务
	 *
	 * @param task   待调度的任务
	 * @param fence0 任务执行屏障
	 * @param fence1 任务执行屏障
	 * @param fence2 任务执行屏障
	 */
	void dispatch(@NotNull T task, Object fence0, Object fence1, Object fence2);

	/**
	 * 调度一个任务
	 *
	 * @param task   待调度的任务
	 * @param fences 任务执行屏障
	 */
	void dispatch(@NotNull T task, @NotNull Object... fences);
}
