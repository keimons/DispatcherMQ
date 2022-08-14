package com.keimons.dmq.core;

/**
 * 任务处理器
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public interface Handler<T> {

	void handle(T wrapperTask);
}
