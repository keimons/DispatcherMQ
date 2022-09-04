package com.keimons.dispatcher.core;

/**
 * 拒绝调度处理
 * <p>
 * 当定序器队列已满时，包装任务无法提交至{@link Sequencer 定序器}时调用。
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public interface RejectedDispatchHandler<T> {

	/**
	 * 拒绝调度
	 *
	 * @param wrapperTask 待调度的任务
	 * @param executor    调度器
	 */
	void rejectedDispatch(Wrapper<T> wrapperTask, Dispatcher<T> executor);
}
