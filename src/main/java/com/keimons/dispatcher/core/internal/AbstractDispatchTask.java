package com.keimons.dispatcher.core.internal;

import com.keimons.dispatcher.core.DispatchTask;
import com.keimons.dispatcher.core.Handler;
import com.keimons.dispatcher.core.Wrapper;
import com.keimons.dispatcher.core.utils.MiscUtils;

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
	private volatile int forbids;

	protected AbstractDispatchTask(Handler<Runnable> handler, Runnable task, int forbids) {
		this.handler = handler;
		this.task = task;
		this.forbids = forbids;
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
		// 最后一个触碰任务的线程返回false
		return v > 1;
	}

	@Override
	public boolean isIntercepted() {
		return forbids >= 0;
	}

	@Override
	public void release() {
		this.forbids = -1;
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
