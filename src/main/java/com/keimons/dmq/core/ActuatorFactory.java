package com.keimons.dmq.core;

import java.util.concurrent.ThreadFactory;

/**
 * ActuatorFactory
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
@FunctionalInterface
public interface ActuatorFactory {

	/**
	 * 构造一个新的调度执行单元
	 *
	 * @param threadFactory 线程工厂
	 * @return 新的调度执行单元
	 */
	Actuator newActuator(ThreadFactory threadFactory);
}
