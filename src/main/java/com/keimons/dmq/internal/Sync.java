package com.keimons.dmq.internal;

import com.keimons.dmq.utils.MiscUtils;
import jdk.internal.vm.annotation.Contended;

import java.lang.invoke.VarHandle;
import java.util.concurrent.locks.LockSupport;

/**
 * 乐观同步器
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
@Contended
public class Sync implements OptimisticSynchronizer {

	private static final VarHandle VV = MiscUtils.findVarHandle(Sync.class, "stamp", int.class);

	private static final VarHandle BB = MiscUtils.findVarHandle(Sync.class, "blocked", boolean.class);

	/**
	 * 绑定线程
	 * <p>
	 * 同步器与线程是绑定的，{@link #acquireWrite()}，版本变更时，
	 * 有可能需要唤醒等待中的线程。
	 */
	private volatile Thread thread;

	/**
	 * 版本控制
	 * <p>
	 * 读取事件总线前和读取事件总线后，如果发生过版本的变更，则代表读取期间有新事件发布。
	 */
	private volatile int stamp;

	/**
	 * 状态控制
	 * <p>
	 * 如果在读取期间，发生过版本变更，则会触发状态的变更，状态变更
	 */
	private volatile boolean blocked;

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return {@inheritDoc}
	 */
	@Override
	public int acquireRead() {
		return stamp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void acquireWrite() {
		// 版本变更
		VV.getAndAddRelease(this, 1);
		// 验证状态
		if (blocked) {
			// 状态变更
			BB.setRelease(this, false);
			LockSupport.unpark(thread);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param stamp {@inheritDoc}
	 */
	@Override
	public void validate(int stamp) {
		// 悲观地认为队列中已经没有消息了，设置线程状态为休眠
		BB.setRelease(this, true);
		// 判断读取过程中，事件总线是否发生过版本变更
		if (stamp == this.stamp) {
			// 未发生版本变更，线程开始休眠，等待生产者唤醒
			LockSupport.park();
		} else {
			// 已发生版本变更，需要回滚状态，继续消耗消息队列
			BB.setRelease(this, false);
		}
	}
}
