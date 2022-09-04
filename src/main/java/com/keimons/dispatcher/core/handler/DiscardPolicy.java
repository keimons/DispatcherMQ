package com.keimons.dispatcher.core.handler;

import com.keimons.dispatcher.core.Handler;
import com.keimons.dispatcher.core.Wrapper;

/**
 * 丢弃任务策略
 * <p>
 * 不处理任务，并直接丢弃。
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
class DiscardPolicy<T> implements RejectedDeliveryHandler<T> {

	public void rejectedDelivery(Wrapper<T> wrapperTask, Handler<T> handler) {
		wrapperTask.cancel();
	}
}
