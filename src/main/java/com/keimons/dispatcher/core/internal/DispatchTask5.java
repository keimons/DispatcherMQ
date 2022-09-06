package com.keimons.dispatcher.core.internal;

import com.keimons.dispatcher.core.DispatchTask;
import com.keimons.dispatcher.core.Handler;
import com.keimons.dispatcher.core.Sequencer;
import com.keimons.dispatcher.core.utils.ForbidsUtils;

/**
 * 带有5个执行屏障的调度任务
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class DispatchTask5 extends AbstractDispatchTask {

	final Object fence0;

	final Sequencer sequencer0;

	final Object fence1;

	final Sequencer sequencer1;

	final Object fence2;

	final Sequencer sequencer2;

	final Object fence3;

	final Sequencer sequencer3;

	final Object fence4;

	final Sequencer sequencer4;

	public DispatchTask5(Handler<Runnable> handler, Runnable task,
						 Object fence0, Sequencer sequencer0,
						 Object fence1, Sequencer sequencer1,
						 Object fence2, Sequencer sequencer2,
						 Object fence3, Sequencer sequencer3,
						 Object fence4, Sequencer sequencer4) {
		super(handler, task, ForbidsUtils.calcForbids(sequencer0, sequencer1, sequencer2, sequencer3, sequencer4));
		this.fence0 = fence0;
		this.sequencer0 = sequencer0;
		this.fence1 = fence1;
		this.sequencer1 = sequencer1;
		this.fence2 = fence2;
		this.sequencer2 = sequencer2;
		this.fence3 = fence3;
		this.sequencer3 = sequencer3;
		this.fence4 = fence4;
		this.sequencer4 = sequencer4;
	}

	@Override
	public Object[] fences() {
		return new Object[]{fence0, fence1, fence2, fence3, fence4};
	}

	@Override
	public void activateTask() {
		sequencer0.activate(this);
		sequencer1.activate(this);
		sequencer2.activate(this);
		sequencer3.activate(this);
		sequencer4.activate(this);
	}

	@Override
	public boolean dependsOn(DispatchTask task) {
		return task.dependsOn(fence0, fence1, fence2, fence3, fence4);
	}

	@Override
	public boolean dependsOn(Object fence) {
		return fence0.equals(fence) || fence1.equals(fence) || fence2.equals(fence) ||
				fence3.equals(fence) || fence4.equals(fence);
	}
}
