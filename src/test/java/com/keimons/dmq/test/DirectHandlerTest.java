package com.keimons.dmq.test;

import com.keimons.dmq.core.CompositeHandler;
import com.keimons.dmq.core.Dispatchers;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 * 测试用例
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class DirectHandlerTest {

	@Test
	public void test() throws InterruptedException {
		CompositeHandler<?> dispatcher = Dispatchers.newCompositeHandler(4, Dispatchers.DEFAULT_DIRECT_HANDLER);
		for (int i = 0; i < 10000; i++) {
			final int index = i;
			dispatcher.dispatch(() -> System.out.println(index), 1);
		}
		Thread.sleep(10000);
	}

	@Test
	public void testOrder() {
		CompositeHandler<?> dispatcher = Dispatchers.newCompositeHandler(4, Dispatchers.DEFAULT_DIRECT_HANDLER);
		dispatcher.dispatch(() -> {
			TimeUtils.SECONDS.sleep(1);
			System.out.println(0);
		}, 0, 1);
		dispatcher.dispatch(() -> System.out.println(1), 0, 1);
		dispatcher.dispatch(() -> System.out.println(2), 1, 2);
		dispatcher.dispatch(() -> System.out.println(3), 1, 2);
		dispatcher.dispatch(() -> {
			TimeUtils.SECONDS.sleep(1);
			System.out.println(4);
		}, 2, 3);
		dispatcher.dispatch(() -> System.out.println(5), 2, 3);
		dispatcher.dispatch(() -> System.out.println(6), 3, 0);
		dispatcher.dispatch(() -> System.out.println(7), 3, 0);
		System.out.println("任务发布完成");
		dispatcher.shutdown(-1, TimeUnit.SECONDS);
		System.out.println("调度器已退出");
	}
}
