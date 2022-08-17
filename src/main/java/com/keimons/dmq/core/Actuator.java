package com.keimons.dmq.core;

/**
 * Actuator
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public interface Actuator {

	void actuate(InterceptorTask interceptorTask);

	void release(InterceptorTask interceptorTask);
}
