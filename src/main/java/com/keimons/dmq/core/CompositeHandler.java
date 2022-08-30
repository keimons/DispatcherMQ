package com.keimons.dmq.core;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * 带有任务调度功能的复合处理器
 * <p>
 * <h2>调度</h2>
 * 复合处理器中至少有一组线程用于任务的调度，这组线程称之为：<b>调度线程</b>。调度线程可以在本地直接执行任务，
 * 也可以将任务派发给其它处理器执行。调度线程可以同时存在多组，多组调度线程联合工作，构成：<b>多重调度</b>。
 * 多重调度可以缓解某个调度线程的压力。
 * <p>
 * 调度线程中应至少有1个线程用于任务调度，当调度线程偏少时，线程将花费更大的代价对任务进行重排序。调度线程数量推荐：
 * <ul>
 *     <li><b>调度线程仅仅调度任务</b>，推荐使用 <b>{@code CPU核心数量}</b> 个线程用于调度。</li>
 *     <li><b>调度线程参与执行任务</b>，推荐使用 <b>{@code CPU核心数量 * 2}</b> 个线程用于调度和执行。</li>
 * </ul>
 * 调度线程数量并没有一个标准，而是根据业务类型选择的，如果任务多是单屏障任务，那么，即便是只有一个调度线程，
 * 也足以完成所有任务的调度。
 * <p>
 * <h2>复合执行</h2>
 * 复合处理器是为业务分层执行而设计的，例如：密集型计算、远程RPC调用、读写文件等涉及IO的操作。在执行非CPU密集型任务时，
 * 同步等待会占用当前线程，异步回调又使得代码晦涩难懂。当使用复合处理器时，只需要将任务分层，按照执行时长划分，
 * 不同的执行时长采用不同的处理器。
 * <p>
 * 调度线程参与执行任务时，在极限情况下，所有任务可能堆积到某个线程，此时还可以使用复合处理器缓解调度线程压力。
 * 复合处理器在一定意义上支持了任务的窃取，当调度线程过于繁忙或堆积大量任务时，可以将一些任务派遣至其它处理器中执行。
 * 此外，复合处理器同样可以用于限流，将某个资源的访问派遣到某个特定的处理器，在处理器中进行限流。
 * <p>
 * <h2>协程</h2>
 * 复合处理器支持使用协程。复合处理器的设计，在一定意义上也是为了弥补协程缺失所带来的问题（但不仅仅是）。遗憾的是，
 * 受限于无法控制并发协程数量，所以，并没有直接支持协程，仅仅是预留部分接口，开发人员可通过实现{@link Handler}
 * 接口支持协程的使用。
 * <p>
 * <b>注意</b>：可以将非CPU密集型的任务交由协程执行，但需要开发人员自行预估并发协程数量，防止过多的协程耗尽内存。
 * CPU密集型的长耗时任务，同样不建议交由协程执行，而是应该交由真实线程执行，真实线程被操作系统按照时间片调度，
 * 这才是更合理的方案。
 * <p>
 * <h2>使用</h2>
 * 演示将任务按照不同的执行时长划分，使用不同的处理器：
 * <pre>{@code
 * enum HandlerType {
 *     LOCAL, LOW, MID, HIGH
 * }
 * EnumMap<HandlerType, Handler<Wrapper<Runnable>>> map = new EnumMap<>(HandlerType.class);
 * map.put(HandlerType.LOCAL, new DirectHandler());
 * map.put(HandlerType.LOW, Handlers.newFixedThreadHandler(8));
 * map.put(HandlerType.MID, Handlers.newFixedThreadHandler(16));
 * map.put(HandlerType.HIGH, Handlers.newFixedThreadHandler(64));
 *
 * CompositeHandler<HandlerType> handlers = Dispatchers.newCompositeHandler(4, map);
 *
 * // 执行时长 0-5ms，本地执行
 * handlers.dispatch(HandlerType.LOCAL, task, "any"); // handlers.dispatch(task, "any");
 * // 执行时长 5-20ms，低级处理器执行
 * handlers.dispatch(HandlerType.LOW, task, "any");
 * // 执行时长 20-200ms，中级处理器执行
 * handlers.dispatch(HandlerType.MID, task, "any");
 * // 执行时长 200+ms，高级处理器执行
 * handlers.dispatch(HandlerType.HIGH, task, "any");
 * }</pre>
 * 复合处理器的使用是多样化的，它同样适用于集群模式。还有一些其它的使用：
 * <ul>
 *     <li>完整版使用敬请参考{@link Dispatchers}。</li>
 *     <li>协程实现敬请参考{@link Handler}。</li>Ï
 * </ul>
 * <h2>注意事项</h2>
 * <ol>
 *     <li>尽量不要在调度线程中执行长耗时的任务，不论是IO密集型还是CPU密集型任务。</li>
 *     <li>任务调度是有成本的，尽可能在调度线程中执行任务。</li>
 *     <li>不要被束缚，复合处理器可以无限复合，甚至，没有任何规定必须在本进程执行。</li>
 *     <li>尽管支持协程，但是协程的使用请慎重。</li>
 * </ol>
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public interface CompositeHandler<E extends Enum<E>> extends Dispatcher<Runnable> {

	/**
	 * 调度一个可执行任务
	 * <p>
	 * 调度一个任务到复合处理器中，并在将来的某个时间执行给定的任务。根据处理器的实现，
	 * 该任务可能在新线程、池线程或调度线程本地执行。如有必要，任务甚至可能被再次调度。
	 *
	 * @param type  使用的处理器
	 * @param task  待执行的任务
	 * @param fence 任务执行屏障
	 */
	void dispatch(E type, @NotNull Runnable task, Object fence);

	/**
	 * 调度一个可执行任务
	 * <p>
	 * 调度一个任务到复合处理器中，并在将来的某个时间执行给定的任务。根据处理器的实现，
	 * 该任务可能在新线程、池线程或调度线程本地执行。如有必要，任务甚至可能被再次调度。
	 *
	 * @param type   使用的处理器
	 * @param task   待执行的任务
	 * @param fence0 任务执行屏障
	 * @param fence1 任务执行屏障
	 */
	void dispatch(E type, @NotNull Runnable task, Object fence0, Object fence1);

	/**
	 * 调度一个可执行任务
	 * <p>
	 * 调度一个任务到复合处理器中，并在将来的某个时间执行给定的任务。根据处理器的实现，
	 * 该任务可能在新线程、池线程或调度线程本地执行。如有必要，任务甚至可能被再次调度。
	 *
	 * @param type   使用的处理器
	 * @param task   待执行的任务
	 * @param fence0 任务执行屏障
	 * @param fence1 任务执行屏障
	 * @param fence2 任务执行屏障
	 */
	void dispatch(E type, @NotNull Runnable task, Object fence0, Object fence1, Object fence2);

	/**
	 * 调度一个可执行任务
	 * <p>
	 * 调度一个任务到复合处理器中，并在将来的某个时间执行给定的任务。根据处理器的实现，
	 * 该任务可能在新线程、池线程或调度线程本地执行。如有必要，任务甚至可能被再次调度。
	 *
	 * @param type   使用的处理器
	 * @param task   待执行的任务
	 * @param fences 任务执行屏障
	 */
	void dispatch(E type, @NotNull Runnable task, Object... fences);

	/**
	 * 关闭复合处理器
	 */
	void shutdown();

	void shutdown(final long timeout, final TimeUnit timeUnit);
}
