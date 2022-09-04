package com.keimons.dispatcher.core;

/**
 * 阻塞调用者
 * <p>
 * 标注一个对象可以阻塞调用者
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public interface BlockingCaller {

	/**
	 * 无效的异常
	 */
	RuntimeException INVALID = new RuntimeException();
}
