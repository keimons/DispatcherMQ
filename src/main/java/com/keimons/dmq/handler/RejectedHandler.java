package com.keimons.dmq.handler;

import com.keimons.dmq.core.Handler;
import com.keimons.dmq.core.Wrapper;

/**
 * 处理器拒绝
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public interface RejectedHandler<T> {

	/**
	 * 拒绝处理
	 *
	 * @param wrapperTask 包装任务
	 * @param executor
	 */
	void rejectedHandle(Wrapper<T> wrapperTask, Handler<T> executor);
}
