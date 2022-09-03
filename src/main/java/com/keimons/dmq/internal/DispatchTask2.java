package com.keimons.dmq.internal;

import com.keimons.dmq.core.DispatchTask;
import com.keimons.dmq.core.Handler;
import com.keimons.dmq.core.Sequencer;

/**
 * 带有2个执行屏障的调度任务
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class DispatchTask2 extends AbstractDispatchTask {

	final Object fence0;

	final Object fence1;

	final Sequencer sequencer0;

	final Sequencer sequencer1;

	public DispatchTask2(Handler<Runnable> handler, Runnable task, Object fence0, Object fence1,
						 Sequencer sequencer0, Sequencer sequencer1) {
		super(handler, task);
		this.fence0 = fence0;
		this.fence1 = fence1;
		this.forbids = sequencer0 == sequencer1 ? 0 : 1;
		this.sequencer0 = sequencer0;
		this.sequencer1 = sequencer1;
	}

	@Override
	public Object[] fences() {
		return new Object[]{fence0, fence1};
	}

	@Override
	public void activateTask() {
		if (sequencer0 == sequencer1) {
			sequencer0.activate(this);
		} else {
			sequencer0.activate(this);
			sequencer1.activate(this);
		}
	}

	@Override
	public boolean dependsOn(DispatchTask task) {
		return task.dependsOn(fence0, fence1);
	}

	@Override
	public boolean dependsOn(Object fence) {
		return fence0.equals(fence) || fence1.equals(fence);
	}
}
