package com.keimons.dispatcher.core.handler;

import com.keimons.dispatcher.core.Handler;
import com.keimons.dispatcher.core.Wrapper;

import java.util.concurrent.RejectedExecutionException;

/**
 * 终止执行策略
 * <p>
 * 取消任务的执行，并抛出一个异常。
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
class AbortPolicy<T> implements RejectedDeliveryHandler<T> {

	@Override
	public void rejectedDelivery(Wrapper<T> wrapperTask, Handler<T> handler) {
		wrapperTask.cancel();
		throw new RejectedExecutionException("Task " + wrapperTask + " rejected from " + handler.toString());
	}
}
