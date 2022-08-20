package com.keimons.dmq.core;

/**
 * 执行单元
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public interface Actuator {

	void actuate(DispatchTask dispatchTask);

	void release(DispatchTask dispatchTask);
}
