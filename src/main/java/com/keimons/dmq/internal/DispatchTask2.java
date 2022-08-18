package com.keimons.dmq.internal;

import com.keimons.dmq.core.Actuator;
import com.keimons.dmq.core.DispatchTask;
import com.keimons.dmq.core.Handler;

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

	final Actuator actuator0;

	final Actuator actuator1;

	public DispatchTask2(Handler<Runnable> handler, Runnable task, Object fence0, Object fence1,
						 Actuator actuator0, Actuator actuator1) {
		super(handler, task);
		this.fence0 = fence0;
		this.fence1 = fence1;
		this.forbids = actuator0 == actuator1 ? 0 : 1;
		this.actuator0 = actuator0;
		this.actuator1 = actuator1;
	}

	@Override
	public Object[] fences() {
		return new Object[]{fence0, fence1};
	}

	@Override
	public boolean dependsOn(DispatchTask task) {
		return task.dependsOn(fence0, fence1);
	}

	@Override
	public boolean dependsOn(Object fence) {
		return fence0.equals(fence) || fence1.equals(fence);
	}

	@Override
	public void wakeup() {
		if (actuator0 == actuator1) {
			actuator0.release(this);
		} else {
			actuator0.release(this);
			actuator1.release(this);
		}
	}
}
