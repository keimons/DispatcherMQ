package com.keimons.dmq.internal;

import com.keimons.dmq.core.DispatchTask;
import com.keimons.dmq.core.Handler;
import com.keimons.dmq.core.Wrapper;
import com.keimons.dmq.utils.MiscUtils;
import jdk.internal.vm.annotation.Contended;

import java.lang.invoke.VarHandle;

/**
 * 调度任务抽象类
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public abstract class AbstractDispatchTask implements DispatchTask, Wrapper<Runnable> {

	protected static final VarHandle VV = MiscUtils.findVarHandle(
			AbstractDispatchTask.class, "forbids", int.class
	);

	protected final Handler<Runnable> handler;

	/**
	 * 等待执行的任务
	 */
	protected final Runnable task;

	/**
	 * 剩余拦截量
	 */
	@Contended
	protected volatile int forbids;

	/**
	 * 是否拦截中
	 */
	protected volatile boolean intercepted = true;

	protected AbstractDispatchTask(Handler<Runnable> handler, Runnable task) {
		this.handler = handler;
		this.task = task;
	}

	@Override
	public void deliveryTask() {
		handler.handle(this);
	}

	@Override
	public boolean tryIntercept() {
		int v;
		do {
			v = forbids;
		} while (!VV.compareAndSet(this, v, v - 1));
		return v > 0;
	}

	@Override
	public boolean isIntercepted() {
		return intercepted;
	}

	@Override
	public void release() {
		this.intercepted = false;
	}

	@Override
	public void invoke() {
		try {
			// 执行真正的任务
			this.task.run();
		} finally {
			// 必须确保：1. 拦截器释放；2. 关联线程唤醒。
			this.release();
			this.activateTask();
		}
	}

	@Override
	public void cancel() {
		// 必须确保：1. 拦截器释放；2. 关联线程唤醒。
		this.release();
		this.activateTask();
	}

	@Override
	public Runnable unwrap() {
		return task;
	}
}
