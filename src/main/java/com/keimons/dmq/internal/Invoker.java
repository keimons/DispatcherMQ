package com.keimons.dmq.internal;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Invoker
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class Invoker implements Runnable {

	BlockingDeque<WrapperTask> queue = new LinkedBlockingDeque<>();

	List<WrapperTask> fences = new LinkedList<>();

	Thread thread = new Thread(this);

	Sync sync = new Sync(thread);

	public void start() {
		thread.start();
	}

	public void offer(WrapperTask wrapperTask) {
		queue.offer(wrapperTask);
		sync.acquireWrite();
	}

	public void weakUp() {
		sync.acquireWrite();
	}

	private boolean skip(WrapperTask wrapperTask) {
		// 判断任务是否可以越过所有屏障执行
		for (int i = 0, length = fences.size(); i < length; i++) {
			if (!wrapperTask.isAdvance(fences.get(i))) {
				return false;
			}
		}
		return true;
	}

	private WrapperTask next() {
		WrapperTask wrapperTask = null;
		for (; ; ) {
			Sync sync = this.sync;
			int stamp = sync.acquireRead();
			fences.removeIf(fence -> !fence.isIntercepted());
			Iterator<WrapperTask> iterator = queue.iterator();
			while (iterator.hasNext()) {
				wrapperTask = iterator.next();
				if (skip(wrapperTask)) {
					// 这个任务已经可以执行了，所以，从缓存的队列中移除任务
					iterator.remove();
					if (wrapperTask.tryIntercept()) {
						fences.add(wrapperTask);
						wrapperTask.weakUp();
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
			WrapperTask wrapperTask = next();
			wrapperTask.load();
		}
	}
}
