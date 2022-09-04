package com.keimons.dispatcher.core.handler;

import com.keimons.dispatcher.core.Handler;
import com.keimons.dispatcher.core.Wrapper;

/**
 * 调用者执行策略
 * <p>
 * 在调用者的线程中执行任务.
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
class CallerRunsPolicy<T> implements RejectedDeliveryHandler<T> {

	@Override
	public void rejectedDelivery(Wrapper<T> wrapperTask, Handler<T> handler) {
		wrapperTask.invoke();
	}
}
