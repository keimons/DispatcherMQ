package com.keimons.dmq.internal;

import com.keimons.dmq.core.Actuator;
import com.keimons.dmq.core.Handler;
import com.keimons.dmq.core.Interceptor;

/**
 * 带有2个执行屏障的任务
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class WrapperTask2 extends AbstractWrapperTask {

	final Object fence0;

	final Object fence1;

	final Actuator actuator0;

	final Actuator actuator1;

	public WrapperTask2(Handler<Runnable> handler, Runnable task, Object fence0, Object fence1,
						Actuator actuator0, Actuator actuator1) {
		super(handler, task, 2);
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
	public boolean isAdvance(Interceptor other) {
		switch (other.size()) {
			case 1 -> {
				WrapperTask1 node = (WrapperTask1) other;
				return !(node.fence.equals(fence0) || node.fence.equals(fence1));
			}
			case 2 -> {
				WrapperTask2 node = (WrapperTask2) other;
				return !(node.fence0.equals(fence0) || node.fence0.equals(fence1) ||
						node.fence1.equals(fence0) || node.fence1.equals(fence1));
			}
			case 3 -> {
				WrapperTask3 node = (WrapperTask3) other;
				return !(node.fence0.equals(fence0) || node.fence0.equals(fence1) ||
						node.fence1.equals(fence0) || node.fence1.equals(fence1) ||
						node.fence2.equals(fence0) || node.fence2.equals(fence1));
			}
			default -> {
				WrapperTaskX node = (WrapperTaskX) other;
				for (int i = 0; i < node.size; i++) {
					Object fence = node.fences[i];
					if (fence.equals(fence0) || fence.equals(fence1)) {
						return false;
					}
				}
				return true;
			}
		}
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
