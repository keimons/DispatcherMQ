package com.keimons.dmq.internal;

import com.keimons.dmq.core.Actuator;
import com.keimons.dmq.core.DispatchTask;

import java.util.ArrayList;
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

	BlockingDeque<DispatchTask> queue = new LinkedBlockingDeque<>();

	List<DispatchTask> fences = new LinkedList<>();

	Thread thread = new Thread(this);

	Sync sync = new Sync(thread);

	public DefaultActuator() {
		thread.start();
	}

	@Override
	public void actuate(DispatchTask task) {
		queue.offer(task);
		sync.acquireWrite();
	}

	@Override
	public void release(DispatchTask task) {
		sync.acquireWrite();
	}

	private boolean isDeliver(DispatchTask task) {
		// 判断任务是否可以越过所有屏障执行
		for (int i = 0, length = fences.size(); i < length; i++) {
			if (task.dependsOn(fences.get(i))) {
				return false;
			}
		}
		return true;
	}

	private DispatchTask next() {
		var array = new ArrayList<>();
		array.remove(100);
		DispatchTask task = null;
		for (; ; ) {
			Sync sync = this.sync;
			int stamp = sync.acquireRead();
			fences.removeIf(fence -> !fence.isIntercepted());
			Iterator<DispatchTask> iterator = queue.iterator();
			while (iterator.hasNext()) {
				task = iterator.next();
				if (isDeliver(task)) {
					// 这个任务已经可以执行了，所以，从缓存的队列中移除任务
					iterator.remove();
					if (task.tryIntercept()) {
						fences.add(task);
						task.wakeup();
					} else {
						return task;
					}
				}
			}
			if (task == null) {
				sync.validate(stamp);
			}
		}
	}

	@Override
	public void run() {
		for (; ; ) {
			try {
				DispatchTask task = next();
				task.deliver();
				if (task.isIntercepted()) {
					fences.add(task);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}
