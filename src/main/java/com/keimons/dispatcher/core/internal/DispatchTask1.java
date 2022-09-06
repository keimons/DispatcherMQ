package com.keimons.dispatcher.core.internal;

import com.keimons.dispatcher.core.DispatchTask;
import com.keimons.dispatcher.core.Handler;
import com.keimons.dispatcher.core.Sequencer;

/**
 * 带有1个执行屏障的调度任务
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class DispatchTask1 extends AbstractDispatchTask {

	final Object fence;

	final Sequencer sequencer;

	public DispatchTask1(Handler<Runnable> handler, Runnable task, Object fence, Sequencer sequencer) {
		super(handler, task, 1);
		this.fence = fence;
		this.sequencer = sequencer;
	}

	@Override
	public Object[] fences() {
		return new Object[]{fence};
	}

	@Override
	public void activateTask() {
		sequencer.activate(this);
	}

	@Override
	public boolean dependsOn(DispatchTask task) {
		return task.dependsOn(fence);
	}

	@Override
	public boolean dependsOn(Object fence) {
		return this.fence.equals(fence);
	}
}
