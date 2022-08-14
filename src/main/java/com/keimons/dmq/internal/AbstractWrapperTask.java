package com.keimons.dmq.internal;

import com.keimons.dmq.core.Handler;
import com.keimons.dmq.core.Wrapper;
import com.keimons.dmq.utils.MiscUtils;
import jdk.internal.vm.annotation.Contended;

import java.lang.invoke.VarHandle;

/**
 * AbstractInterceptorTask
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public abstract class AbstractWrapperTask implements WrapperTask {

	protected static final VarHandle VV = MiscUtils.findVarHandle(
			AbstractWrapperTask.class, "forbids", int.class
	);

	protected final Handler<Wrapper<Runnable>> handler;

	/**
	 * 等待执行的任务
	 */
	protected final Runnable task;

	/**
	 * 任务执行屏障数量
	 */
	protected final int size;

	/**
	 * 剩余拦截量
	 */
	@Contended
	protected volatile int forbids;

	/**
	 * 是否拦截中
	 */
	protected volatile boolean intercepted = true;

	protected AbstractWrapperTask(Handler<Wrapper<Runnable>> handler, Runnable task, int size) {
		this.handler = handler;
		this.task = task;
		this.size = size;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean tryIntercept() {
		int v;
		do {
			v = forbids;
		} while (!VV.compareAndSet(this, v, v - 1));
		return v > 0;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return {@inheritDoc}
	 */
	@Override
	public boolean isIntercepted() {
		return intercepted;
	}

	public abstract void weakUp();

	/**
	 * 返回其它可执行的拦截器是否能越过当前的提前执行
	 * <p>
	 * 设计语言：
	 * 如果任务屏障完全不同，则可以重排序执行，这对最终的结果不会产生影响。
	 *
	 * @param other 尝试越过此节点的其它节点
	 * @return {@code true}允许越过当前节点重排序运行，{@code false}禁止越过当前节点重排序运行。
	 */
	public abstract boolean isAdvance(WrapperTask other);

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
			this.weakUp();
		}
	}

	@Override
	public void cancel() {
		// 必须确保：1. 拦截器释放；2. 关联线程唤醒。
		this.release();
		this.weakUp();
	}

	@Override
	public Runnable unwrap() {
		return task;
	}

	@Override
	public void load() {
		handler.handle(this);
	}
}
