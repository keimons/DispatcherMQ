package com.keimons.dispatcher.core.handler;

import com.keimons.dispatcher.core.BlockingCaller;
import com.keimons.dispatcher.core.Handler;
import com.keimons.dispatcher.core.Wrapper;

/**
 * 阻塞调用者策略
 * <p>
 * 该策略会抛出{@link #INVALID 固定}异常。通过检查是否此异常，判断是否继续提交。例如：
 * <pre>{@code
 * Handler<?> handler = null;
 * for (; ; ) {
 *     try {
 *         handler.handle(task);
 *         break;
 *     } catch (Throwable cause) {
 *         if (cause == BlockingCaller.INVALID) {
 *             continue;
 *         }
 *         throw cause;
 *     }
 * }
 * }</pre>
 * 抛出提前构造出的异常，性能相当于加法运算的一百倍，相比较构造并抛出异常的两千倍性能损耗，这是可以接受的。
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
class BlockingCallerPolicy<T> implements RejectedDeliveryHandler<T>, BlockingCaller {

	@Override
	public void rejectedDelivery(Wrapper<T> wrapperTask, Handler<T> handler) {
		// 没有任何操作，消耗相当于加法一百倍的性能，抛出一个占位异常
		throw INVALID;
	}
}
