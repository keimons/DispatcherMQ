package com.keimons.dispatcher.core.internal;

import com.keimons.dispatcher.core.DispatchTask;
import com.keimons.dispatcher.core.Handler;
import com.keimons.dispatcher.core.Sequencer;

/**
 * 带有n个执行屏障的调度任务
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class DispatchTaskX extends AbstractDispatchTask {

	/**
	 * 任务执行屏障
	 */
	final Object[] fences;

	final Sequencer[] sequencers;

	public DispatchTaskX(Handler<Runnable> handler, Runnable task, Object[] fences, Sequencer[] sequencers) {
		super(handler, task, sequencers.length);
		this.fences = fences;
		this.sequencers = sequencers;
	}

	@Override
	public Object[] fences() {
		return fences;
	}

	@Override
	public void activateTask() {
		for (Sequencer sequencer : sequencers) {
			sequencer.activate(this);
		}
	}

	@Override
	public boolean dependsOn(DispatchTask task) {
		return task.dependsOn(fences);
	}

	@Override
	public boolean dependsOn(Object fence) {
		for (Object o : fences) {
			if (o.equals(fence)) {
				return true;
			}
		}
		return false;
	}
}
