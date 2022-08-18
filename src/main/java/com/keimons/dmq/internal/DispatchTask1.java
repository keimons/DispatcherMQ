package com.keimons.dmq.internal;

import com.keimons.dmq.core.Actuator;
import com.keimons.dmq.core.DispatchTask;
import com.keimons.dmq.core.Handler;

/**
 * 带有1个执行屏障的调度任务
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class DispatchTask1 extends AbstractDispatchTask {

	final Object fence;

	final Actuator actuator;

	public DispatchTask1(Handler<Runnable> handler, Runnable task, Object fence, Actuator actuator) {
		super(handler, task);
		this.fence = fence;
		this.actuator = actuator;
	}

	@Override
	public Object[] fences() {
		return new Object[]{fence};
	}

	@Override
	public boolean dependsOn(DispatchTask task) {
		return task.dependsOn(fence);
	}

	@Override
	public boolean dependsOn(Object fence) {
		return this.fence.equals(fence);
	}

	@Override
	public void wakeup() {
		actuator.release(this);
	}
}
