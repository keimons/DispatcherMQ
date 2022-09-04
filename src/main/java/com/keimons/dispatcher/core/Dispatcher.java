package com.keimons.dispatcher.core;

import org.jetbrains.annotations.NotNull;

/**
 * 任务调度器
 * <p>
 * <p>
 * 调度器会对任务进行重排序，并在合适的时机交由处理器执行。任务的重排序必须满足：
 * <b>{@code as-if-serial语义}</b>和<b>{@code happens-before原则}</b>。
 * 而确定任务能否重排序的依据称之为：<b>执行屏障</b>。执行屏障可以确保：
 * <ul>
 *     <li>带有相同执行屏障的任务，必须串行执行；</li>
 *     <li>带有不同执行屏障的任务，可以并行执行。</li>
 * </ul>
 * 执行屏障可以是任意对象，{@code "userId"} 、{@code "itemId"} 、{@code "session"}
 * 等等都可以作为执行屏障。以{@code userId} 为例：
 * <pre>{@code
 * Dispatcher<Runnable> dispatcher = ...;
 * dispatcher.dispatch(() -> System.out.println(1), "user_10000001");
 * dispatcher.dispatch(() -> System.out.println(2), "user_10000001");
 * dispatcher.dispatch(() -> System.out.println(3), "user_10000002");
 * dispatcher.dispatch(() -> System.out.println(4), "user_10000002");
 * // print 1, 2, 3, 4
 * // print 3, 1, 2, 4
 * // print 3, 1, 4, 2
 * // print 1, 3, 4, 2
 * }</pre>
 * 来自{@code "user_10000001"} 的任务和来自{@code "user_10000002"} 的任务，
 * 尽管是在交叉执行，但对于他们各自而言，执行顺序总是固定的。。
 * <p>
 * 任务还可以带有多个执行屏障。带有多个执行屏障的任务成为：<b>联合任务</b>。
 * 联合任务依然保留了执行屏障的特性。以{@code userId} 和{@code itemId} 为例：
 * <pre>{@code
 * dispatcher.dispatch(() -> System.out.println(1), "user_10000001", "item_1001"); // 任务1
 * dispatcher.dispatch(() -> System.out.println(2), "user_10000001", "item_1002"); // 任务2
 * dispatcher.dispatch(() -> System.out.println(3), "user_10000002", "item_1002"); // 任务3
 * dispatcher.dispatch(() -> System.out.println(4), "user_10000002", "item_1001"); // 任务4
 * dispatcher.dispatch(() -> System.out.println(5), "user_10000003", "item_1003"); // 任务5
 * // print 5, 1, 2, 3, 4
 * // print 1, 2, 5, 3, 4
 * // print 1, 2, 3, 4, 5
 * }</pre>
 * 可以看到以下规律：
 * <ol>
 *     <li><b>任务1</b>和<b>任务2</b>中的{@code "user_10000001"} 相同，任务无法重排序执行；</li>
 *     <li><b>任务2</b>和<b>任务3</b>中的{@code "item_1002"} 相同，任务无法重排序执行；</li>
 *     <li><b>任务3</b>和<b>任务4</b>中的{@code "user_10000002"} 相同，任务无法重排序执行；</li>
 *     <li><b>任务5</b>和<b>其它任务</b>无关，可以并行执行。</li>
 * </ol>
 * {@code 任务1，任务2，任务3，任务4} 总是顺序执行，而{@code 任务5} 可以却可以并行执行，所以，
 * 输出的{@code 5} 是穿插在{@code 1, 2, 3, 4} 之间。
 * <p>
 * 任务调度器不仅仅可以对{@link Runnable 任务}进行调度，同样它也可以用于消息的调度。
 * 任务的执行可能是在调度器中直接完成，也可能调度到复合线线程池中完成。
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
	 * @param task 待调度的任务
	 */
	void dispatch(@NotNull T task);

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
	void dispatch(@NotNull T task, Object... fences);

	default void transfer(Object src, Object dst) {
		// TODO 实现一个节点转移
	}
}
