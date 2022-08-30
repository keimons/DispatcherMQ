package com.keimons.dmq.handler;

import com.keimons.dmq.core.Handler;
import com.keimons.dmq.core.Wrapper;

/**
 * 拒绝投递处理
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public interface RejectedDeliveryHandler<T> {

	/**
	 * 处理拒绝投递
	 *
	 * @param wrapperTask 包装任务
	 * @param executor    异常
	 */
	void rejectedDelivery(Wrapper<T> wrapperTask, Handler<T> executor);
}
