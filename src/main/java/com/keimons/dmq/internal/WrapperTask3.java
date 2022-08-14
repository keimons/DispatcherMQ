package com.keimons.dmq.internal;

import com.keimons.dmq.core.Handler;
import com.keimons.dmq.core.Wrapper;

/**
 * 带有3个执行屏障的任务
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class WrapperTask3 extends AbstractWrapperTask {

	final Object fence0;

	final Invoker invoker0;

	final Object fence1;

	final Invoker invoker1;

	final Object fence2;

	final Invoker invoker2;

	public WrapperTask3(Handler<Wrapper<Runnable>> handler, Runnable task,
						Object fence0, Invoker invoker0, Object fence1, Invoker invoker1,
						Object fence2, Invoker invoker2) {
		super(handler, task, 3);
		this.fence0 = fence0;
		this.invoker0 = invoker0;
		this.fence1 = fence1;
		this.invoker1 = invoker1;
		this.fence2 = fence2;
		this.invoker2 = invoker2;
		if (this.invoker0 == this.invoker1 || this.invoker0 == this.invoker2 || this.invoker1 == this.invoker2) {
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
	public boolean isAdvance(WrapperTask other) {
		switch (other.size()) {
			case 1 -> {
				WrapperTask1 node = (WrapperTask1) other;
				return !(node.fence.equals(fence0) || node.fence.equals(fence1) || node.fence.equals(fence2));
			}
			case 2 -> {
				WrapperTask2 node = (WrapperTask2) other;
				return !(node.fence0.equals(fence0) || node.fence0.equals(fence1) || node.fence0.equals(fence2) ||
						node.fence1.equals(fence0) || node.fence1.equals(fence1) || node.fence1.equals(fence2)
				);
			}
			case 3 -> {
				WrapperTask3 node = (WrapperTask3) other;
				return !(node.fence0.equals(fence0) || node.fence0.equals(fence1) || node.fence0.equals(fence2) ||
						node.fence1.equals(fence0) || node.fence1.equals(fence1) || node.fence1.equals(fence2) ||
						node.fence2.equals(fence0) || node.fence2.equals(fence1) || node.fence2.equals(fence2)
				);
			}
			default -> {
				WrapperTaskX node = (WrapperTaskX) other;
				for (int i = 0; i < node.size; i++) {
					Object fence = node.fences[i];
					if (fence.equals(fence0) || fence.equals(fence1) || fence.equals(fence2)) {
						return false;
					}
				}
				return true;
			}
		}
	}

	@Override
	public void weakUp() {
		if (this.invoker0 == this.invoker1) {
			if (this.invoker0 == this.invoker2) {
				invoker0.weakUp();
			} else {
				invoker0.weakUp();
				invoker2.weakUp();
			}
		} if (this.invoker0 == this.invoker2 || this.invoker1 == this.invoker2) {
			invoker0.weakUp();
			invoker1.weakUp();
		} else {
			invoker0.weakUp();
			invoker1.weakUp();
			invoker2.weakUp();
		}
	}
}
