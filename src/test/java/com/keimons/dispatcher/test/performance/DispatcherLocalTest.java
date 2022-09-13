package com.keimons.dispatcher.test.performance;

import com.keimons.dispatcher.core.CompositeHandler;
import com.keimons.dispatcher.core.Dispatcher;
import com.keimons.dispatcher.core.Dispatchers;
import com.keimons.dispatcher.test.EmptyTask;
import org.junit.jupiter.api.Test;

/**
 * 调度器本地测试
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class DispatcherLocalTest {

	private static final int TEST_TIMES = 1000_0000;

	@Test
	public void test() throws InterruptedException {
		Runnable empty = new EmptyTask();

		Dispatcher<Runnable> dispatcher = Dispatchers.newDispatcher(1);
		Runnable runnable = () -> {
			for (int i = 0; i < 500_0000; i++) {
				dispatcher.dispatch(empty, 0);
			}
		};
		Thread thread1 = new Thread(runnable);
		Thread thread2 = new Thread(runnable);
		long startTime = System.currentTimeMillis();
		thread1.start();
		thread2.start();
		thread1.join();
		thread2.join();
		((CompositeHandler<?>) dispatcher).shutdown();
		long useTime = System.currentTimeMillis() - startTime;
		System.out.println(useTime);

//		ExecutorService executor = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
//		startTime = System.currentTimeMillis();
//		for (int i = 0; i < TEST_TIMES; i++) {
//			executor.execute(empty);
//		}
//		executor.shutdown();
//		executor.awaitTermination(10, TimeUnit.SECONDS);
//		useTime = System.currentTimeMillis() - startTime;
//		System.out.printlnln(useTime);
	}
}
