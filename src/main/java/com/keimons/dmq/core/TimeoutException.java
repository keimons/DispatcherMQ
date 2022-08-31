package com.keimons.dmq.core;

/**
 * 运行时的超时异常
 * <p>
 * 运行时版本的{@link java.util.concurrent.TimeoutException}，阻塞操作超时时会抛出的异常。
 * 当操作没能在指定的时间内完成时，可以抛出此异常以指示发生了超时。
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class TimeoutException extends RuntimeException {

	public TimeoutException() {

	}

	public TimeoutException(String message) {
		super(message);
	}
}
