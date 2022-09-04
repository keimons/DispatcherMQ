package com.keimons.dispatcher.core.handler;

/**
 * 拒绝交付处理策略
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class Policies {

	/**
	 * 返回阻塞调用者的策略
	 *
	 * @return 阻塞调用者策略
	 */
	public static <T> RejectedDeliveryHandler<T> newBlockingCallerPolicy() {
		return new BlockingCallerPolicy<>();
	}

	/**
	 * 返回终止执行的策略
	 *
	 * @return 终止执行策略
	 */
	public static <T> RejectedDeliveryHandler<T> newAbortPolicy() {
		return new AbortPolicy<>();
	}

	/**
	 * 返回丢弃任务的策略
	 *
	 * @return 丢弃任务策略
	 */
	public static <T> RejectedDeliveryHandler<T> newDiscardPolicy() {
		return new DiscardPolicy<>();
	}

	/**
	 * 返回调用者执行的策略
	 *
	 * @return 调用者执行策略
	 */
	public static <T> CallerRunsPolicy<T> newCallerRunsPolicy() {
		return new CallerRunsPolicy<>();
	}
}
