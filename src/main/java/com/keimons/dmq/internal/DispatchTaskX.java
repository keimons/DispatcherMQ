package com.keimons.dmq.internal;

import com.keimons.dmq.core.Actuator;
import com.keimons.dmq.core.DispatchTask;
import com.keimons.dmq.core.Handler;

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

	final Actuator[] actuators;

	public DispatchTaskX(Handler<Runnable> handler, Runnable task, Object[] fences, Actuator[] actuators) {
		super(handler, task);
		this.fences = fences;
		this.actuators = actuators;
		this.forbids = actuators.length - 1;
	}

	@Override
	public Object[] fences() {
		return fences;
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

	@Override
	public void wakeup() {
		for (Actuator actuator : actuators) {
			actuator.release(this);
		}
	}
}
