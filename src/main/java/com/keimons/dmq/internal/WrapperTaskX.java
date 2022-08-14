package com.keimons.dmq.internal;

import com.keimons.dmq.core.Handler;
import com.keimons.dmq.core.Wrapper;

/**
 * 带有n个执行屏障的任务
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class WrapperTaskX extends AbstractWrapperTask {

	/**
	 * 任务执行屏障
	 */
	final Object[] fences;

	final Invoker[] invokers;

	public WrapperTaskX(Handler<Wrapper<Runnable>> handler, Runnable task, Object[] fences, Invoker[] invokers) {
		super(handler, task, fences.length);
		this.fences = fences;
		this.invokers = invokers;
		this.forbids = invokers.length - 1;
	}

	@Override
	public Object[] fences() {
		return fences;
	}

	/**
	 * 判断另一个节点是否能越过当前节点执行
	 *
	 * @param other 后续节点
	 * @return {@code true}可以越过当前栅栏，{@code false}不能越过当前栅栏
	 */
	@Override
	public boolean isAdvance(WrapperTask other) {
		switch (other.size()) {
			case 1 ->{
				WrapperTask1 node = (WrapperTask1) other;
				Object _fence = node.fence;
				for (int i = 0; i < size; i++) {
					Object fence = fences[i];
					if (fence.equals(_fence)) {
						return false;
					}
				}
				return true;
			}
			case 2 -> {
				WrapperTask2 node = (WrapperTask2) other;
				Object _fence0 = node.fence0;
				Object _fence1 = node.fence1;
				for (int i = 0; i < size; i++) {
					Object fence = fences[i];
					if (fence.equals(_fence0) || fence.equals(_fence1)) {
						return false;
					}
				}
				return true;
			}
			case 3 -> {
				WrapperTask3 node = (WrapperTask3) other;
				Object _fence0 = node.fence0;
				Object _fence1 = node.fence1;
				Object _fence2 = node.fence2;
				for (int i = 0; i < size; i++) {
					Object fence = fences[i];
					if (fence.equals(_fence0) || fence.equals(_fence1) || fence.equals(_fence2)) {
						return false;
					}
				}
				return true;
			}
			default -> {
				WrapperTaskX node = (WrapperTaskX) other;
				for (int i = 0; i < node.size; i++) {
					Object fence = node.fences[i];
					for (int j = 0; j < size; j++) {
						if (fence.equals(fences[j])) {
							return false;
						}
					}
				}
				return true;
			}
		}
	}

	@Override
	public void weakUp() {
		for (Invoker invoker : invokers) {
			invoker.weakUp();
		}
	}
}
