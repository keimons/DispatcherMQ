package com.keimons.dmq.internal;

import com.keimons.dmq.core.Actuator;
import com.keimons.dmq.core.InterceptorTask;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 执行器
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class DefaultActuator implements Actuator, Runnable {

	BlockingDeque<InterceptorTask> queue = new LinkedBlockingDeque<>();

	List<InterceptorTask> fences = new LinkedList<>();

	Thread thread = new Thread(this);

	Sync sync = new Sync(thread);

	void DefaultActuator() {
		thread.start();
	}

	@Override
	public void actuate(InterceptorTask interceptorTask) {
		queue.offer(interceptorTask);
	}

	@Override
	public void release(InterceptorTask interceptorTask) {
		sync.acquireWrite();
	}

	private boolean skip(InterceptorTask wrapperTask) {
		// 判断任务是否可以越过所有屏障执行
		for (int i = 0, length = fences.size(); i < length; i++) {
			if (!wrapperTask.isAdvance(fences.get(i))) {
				return false;
			}
		}
		return true;
	}

	private InterceptorTask next() {
		InterceptorTask wrapperTask = null;
		for (; ; ) {
			Sync sync = this.sync;
			int stamp = sync.acquireRead();
			fences.removeIf(fence -> !fence.isIntercepted());
			Iterator<InterceptorTask> iterator = queue.iterator();
			while (iterator.hasNext()) {
				wrapperTask = iterator.next();
				if (skip(wrapperTask)) {
					// 这个任务已经可以执行了，所以，从缓存的队列中移除任务
					iterator.remove();
					if (wrapperTask.tryIntercept()) {
						fences.add(wrapperTask);
						wrapperTask.wakeup();
					} else {
						return wrapperTask;
					}
				}
			}
			if (wrapperTask == null) {
				sync.validate(stamp);
			}
		}
	}

	@Override
	public void run() {
		for (; ; ) {
			InterceptorTask wrapperTask = next();
			wrapperTask.load();
		}
	}
}
