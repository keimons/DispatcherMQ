package com.keimons.dmq.internal;

import com.keimons.dmq.core.Actuator;
import com.keimons.dmq.core.Handler;
import com.keimons.dmq.core.Interceptor;

/**
 * 带有1个执行屏障的任务
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class WrapperTask1 extends AbstractWrapperTask {

	final Object fence;

	final Actuator actuator;

	public WrapperTask1(Handler<Runnable> handler, Runnable task, Object fence,
						Actuator actuator) {
		super(handler, task, 1);
		this.fence = fence;
		this.actuator = actuator;
	}

	@Override
	public Object[] fences() {
		return new Object[]{fence};
	}

	@Override
	public boolean isAdvance(Interceptor other) {
		switch (other.size()) {
			case 1 -> {
				WrapperTask1 node = (WrapperTask1) other;
				return !node.fence.equals(fence);
			}
			case 2 -> {
				WrapperTask2 node = (WrapperTask2) other;
				return !(node.fence0.equals(fence) || node.fence1.equals(fence));
			}
			case 3 -> {
				WrapperTask3 node = (WrapperTask3) other;
				return !(node.fence0.equals(fence) || node.fence1.equals(fence) || node.fence2.equals(fence));
			}
			default -> {
				WrapperTaskX node = (WrapperTaskX) other;
				for (int i = 0, count = node.size; i < count; i++) {
					Object fence = node.fences[i];
					if (fence.equals(this.fence)) {
						return false;
					}
				}
				return true;
			}
		}
	}

	@Override
	public void wakeup() {
		actuator.release(this);
	}
}
