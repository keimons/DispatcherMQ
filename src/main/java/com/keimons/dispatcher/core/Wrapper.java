package com.keimons.dispatcher.core;

/**
 * 任务/消息的包装
 * <p>
 * <h4>使用须知：</h4>
 * <ol>
 *     <li>请确保{@link #invoke()}和{@link #cancel()}这两个方法，必有且仅有一个被调用。</li>
 *     <li>拒绝继承{@link Runnable}接口。包装的可能是可执行的对象，也可能是消息体。</li>
 * </ol>
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public interface Wrapper<T> {

	/**
	 * 执行包装中的内容
	 * <p>
	 * 如果这个方法不能被调用，请确保{@link #cancel()}方法至少会被调用1次。
	 */
	void invoke();

	/**
	 * 取消执行包装中的内容
	 */
	void cancel();

	/**
	 * 返回包装中的内容
	 *
	 * @return 包装中的内容
	 */
	T unwrap();

	/**
	 * 返回包装中的屏障
	 *
	 * @return 包装中的屏障
	 */
	Object[] fences();
}
