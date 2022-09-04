package com.keimons.dispatcher.core.handler;

import com.keimons.dispatcher.core.Handler;
import com.keimons.dispatcher.core.Wrapper;

/**
 * 拒绝交付处理
 * <p>
 * 当处理器队列已满时，包装任务无法交付至{@link Handler 处理器}时调用。
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public interface RejectedDeliveryHandler<T> {

	/**
	 * 拒绝交付
	 * <p>
	 * 处理器无法接受此任务时调用。
	 *
	 * @param wrapperTask 包装任务
	 * @param handler     异常
	 */
	void rejectedDelivery(Wrapper<T> wrapperTask, Handler<T> handler);
}
