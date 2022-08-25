package com.keimons.dmq.core;

import java.util.concurrent.ThreadFactory;

/**
 * 定序器工厂
 * <p>
 * 定序器的标准实现是带有线程的，使用线程工厂创建定序器。
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
@FunctionalInterface
public interface SequencerFactory {

	/**
	 * 构造一个新的调度执行单元
	 *
	 * @param threadFactory 线程工厂
	 * @return 新的调度执行单元
	 */
	Sequencer newSequencer(ThreadFactory threadFactory);
}
