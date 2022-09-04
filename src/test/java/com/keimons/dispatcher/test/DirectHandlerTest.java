package com.keimons.dispatcher.test;

import com.keimons.dispatcher.core.CompositeHandler;
import com.keimons.dispatcher.core.Dispatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 测试用例
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class DirectHandlerTest {

	private static final int TIMES = 10000;

	@Test
	public void test() {
		Node node = new Node();
		CompositeHandler<?> dispatcher = Dispatchers.newCompositeHandler(4, Dispatchers.DEFAULT_DIRECT_HANDLER);
		for (int i = 0; i < TIMES; i++) {
			dispatcher.dispatch(() -> node.number++, 1);
		}
		dispatcher.shutdown();
		Assertions.assertEquals(node.number, TIMES, "[调度器][串行执行] 串行执行任务");
	}

	@Test
	public void testOrder() {
		Node node = new Node();
		CompositeHandler<?> dispatcher = Dispatchers.newCompositeHandler(4, Dispatchers.DEFAULT_DIRECT_HANDLER);
		dispatcher.dispatch(() -> {
			TimeUtils.SECONDS.sleep(1);
			node.number++;
		}, 0, 1);
		dispatcher.dispatch(() -> node.number++, 0, 1);
		dispatcher.dispatch(() -> node.number++, 1, 2);
		dispatcher.dispatch(() -> node.number++, 1, 2);
		dispatcher.dispatch(() -> {
			TimeUtils.SECONDS.sleep(1);
			node.number++;
		}, 2, 3);
		dispatcher.dispatch(() -> node.number++, 2, 3);
		dispatcher.dispatch(() -> node.number++, 3, 0);
		dispatcher.dispatch(() -> node.number++, 0, 1);
		dispatcher.shutdown();
		Assertions.assertEquals(node.number, 8, "[调度器][串行执行] 串行执行任务");
	}

	private static class Node {

		int number;
	}
}
