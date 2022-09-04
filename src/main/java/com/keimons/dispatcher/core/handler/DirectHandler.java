package com.keimons.dispatcher.core.handler;

import com.keimons.dispatcher.core.CompositeHandler;
import com.keimons.dispatcher.core.Handler;
import com.keimons.dispatcher.core.Sequencer;
import com.keimons.dispatcher.core.Wrapper;

/**
 * 直接处理器
 * <p>
 * 直接处理器的是{@link Handler 处理器}最简单实现。{@link Sequencer 定序器}通常拥有单独的线程，
 * 这意味着任务可以直接在定序器中直接执行，而直接处理器时间就是使用定序器线程执行任务。
 * 直接处理器不建议执行耗时任务，这将阻塞定序器，而定序器被阻塞是灾难性的。
 * 调度器更推荐使用多个处理器组成{@link CompositeHandler 复合处理器}，
 * 复合处理器更方便执行不同的业务。
 * <p>
 * 直接处理器没有额外的任务调度，能够获得更高的执行效率。尽管代码只有几行，但它确实是调度器的核心设计，
 * 大量的"微"任务，调度的代价甚至已经高于任务本身的执行成本。
 * <p>
 * 使用推荐：定序器数量 >= CPU核心数量；任务执行速度非常快。
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class DirectHandler implements Handler<Runnable> {

	@Override
	public void handle0(Wrapper<Runnable> wrapperTask) {
		wrapperTask.invoke();
	}
}
