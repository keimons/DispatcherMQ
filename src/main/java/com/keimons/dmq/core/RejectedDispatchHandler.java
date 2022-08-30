package com.keimons.dmq.core;

/**
 * 拒绝调度处理
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public interface RejectedDispatchHandler<T> {

	void rejectedDispatch(Wrapper<T> wrapperTask, Dispatcher<T> executor);
}
