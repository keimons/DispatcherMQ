package com.keimons.dmq.test;

import com.keimons.dmq.core.CompositeHandler;
import com.keimons.dmq.core.Handler;
import com.keimons.dmq.handler.Handlers;
import com.keimons.dmq.internal.DefaultCompositeHandler;
import com.keimons.dmq.internal.SerialMode;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.concurrent.Executors;

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
		EnumMap<Defaults.Type, Handler<Runnable>> map = new EnumMap<>(Defaults.Type.class);
		map.put(Defaults.Type.Default, Handlers.newDirectHandler());
		CompositeHandler<Defaults.Type> dispatcher = new DefaultCompositeHandler<>(4, 0, 4,
				SerialMode.producer(), Executors.defaultThreadFactory(), map);
		for (int i = 0; i < 10000; i++) {
			final int index = i;
			dispatcher.dispatch(Defaults.Type.Default, () -> System.out.println(index), 1);
		}
		Thread.sleep(10000);
	}

	@Test
	public void testOrder() throws InterruptedException {
		EnumMap<Defaults.Type, Handler<Runnable>> map = new EnumMap<>(Defaults.Type.class);
		map.put(Defaults.Type.Default, Handlers.newDirectHandler());
		CompositeHandler<Defaults.Type> dispatcher = new DefaultCompositeHandler<>(4, 0, 4,
				SerialMode.producer(), Executors.defaultThreadFactory(), map);
		dispatcher.dispatch(Defaults.Type.Default, () -> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			System.out.println(0);
		}, 0, 1);
		dispatcher.dispatch(Defaults.Type.Default, () -> System.out.println(1), 0, 1);
		dispatcher.dispatch(Defaults.Type.Default, () -> System.out.println(2), 1, 2);
		dispatcher.dispatch(Defaults.Type.Default, () -> System.out.println(3), 1, 2);
		dispatcher.dispatch(Defaults.Type.Default, () -> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			System.out.println(4);
		}, 2, 3);
		dispatcher.dispatch(Defaults.Type.Default, () -> System.out.println(5), 2, 3);
		dispatcher.dispatch(Defaults.Type.Default, () -> System.out.println(6), 3, 0);
		dispatcher.dispatch(Defaults.Type.Default, () -> System.out.println(7), 3, 0);
		System.out.println("任务发布完成");
		Thread.sleep(3000);
	}
}
