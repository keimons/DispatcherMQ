package com.keimons.dmq.internal;

import com.keimons.dmq.core.Actuator;
import com.keimons.dmq.core.DispatchTask;
import com.keimons.dmq.core.Handler;

/**
 * 带有3个执行屏障的调度任务
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class DispatchTask3 extends AbstractDispatchTask {

	final Object fence0;

	final Actuator actuator0;

	final Object fence1;

	final Actuator actuator1;

	final Object fence2;

	final Actuator actuator2;

	public DispatchTask3(Handler<Runnable> handler, Runnable task,
						 Object fence0, Actuator actuator0,
						 Object fence1, Actuator actuator1,
						 Object fence2, Actuator actuator2) {
		super(handler, task);
		this.fence0 = fence0;
		this.actuator0 = actuator0;
		this.fence1 = fence1;
		this.actuator1 = actuator1;
		this.fence2 = fence2;
		this.actuator2 = actuator2;
		if (this.actuator0 == this.actuator1 || this.actuator0 == this.actuator2 || this.actuator1 == this.actuator2) {
			this.forbids = 1;
		} else {
			this.forbids = 2;
		}
	}

	@Override
	public Object[] fences() {
		return new Object[]{fence0, fence1, fence2};
	}

	@Override
	public boolean dependsOn(DispatchTask task) {
		return task.dependsOn(fence0, fence1, fence2);
	}

	@Override
	public boolean dependsOn(Object fence) {
		return fence0.equals(fence) || fence1.equals(fence) || fence2.equals(fence);
	}

	@Override
	public void wakeup() {
		if (this.actuator0 == this.actuator1) {
			if (this.actuator0 == this.actuator2) {
				actuator0.release(this);
			} else {
				actuator0.release(this);
				actuator2.release(this);
			}
		}
		if (this.actuator0 == this.actuator2 || this.actuator1 == this.actuator2) {
			actuator0.release(this);
			actuator1.release(this);
		} else {
			actuator0.release(this);
			actuator1.release(this);
			actuator2.release(this);
		}
	}
}
