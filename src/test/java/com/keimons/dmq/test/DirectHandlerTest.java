package com.keimons.dmq.test;

import com.keimons.dmq.core.CompositeHandler;
import com.keimons.dmq.core.Dispatcher;
import com.keimons.dmq.core.Dispatchers;
import com.keimons.dmq.core.Handler;
import com.keimons.dmq.internal.DefaultCompositeHandler;
import com.keimons.dmq.internal.SerialMode;
import com.keimons.dmq.wrapper.Handlers;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

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
		CompositeHandler<Defaults.Type> dispatcher = new DefaultCompositeHandler<>(4, 0, 4, SerialMode.PRODUCER, map);
		for (int i = 0; i < 10000; i++) {
			final int index = i;
			dispatcher.dispatch(Defaults.Type.Default, () -> System.out.println(index), 1);
		}
		Thread.sleep(10000);
	}

	public void tttt() {
		Map<String, Integer> items = new HashMap<>();
		items.put("book", 1);
		Dispatcher<Runnable> dispatcher = Dispatchers.newDispatcher(4);
		dispatcher.dispatch(() -> System.out.println(1), "user_10000001");
		dispatcher.dispatch(() -> System.out.println(2), "user_10000001");
		dispatcher.dispatch(() -> System.out.println(3), "user_10000001");
		dispatcher.dispatch(() -> System.out.println(4), "user_10000001");
	}
}
