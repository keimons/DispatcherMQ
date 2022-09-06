package com.keimons.dispatcher.test;

import com.keimons.dispatcher.core.CompositeHandler;
import com.keimons.dispatcher.core.Dispatchers;
import com.keimons.dispatcher.core.Handler;
import com.keimons.dispatcher.core.handler.ThreadPoolHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 异常抛出测试
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class RejectedDeliveryHandlerTest {

	@Test
	public void testBlockingCaller() {
		EnumMap<HandlerType, Handler<Runnable>> handlers = new EnumMap<>(HandlerType.class);
		handlers.put(HandlerType.POOL, new ThreadPoolHandler(
				1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1))
		);
		CompositeHandler<HandlerType> dispatcher = Dispatchers.newCompositeHandler(1, handlers);
		dispatcher.dispatch(EmptyTask.instance, 1);
		long startTime = System.currentTimeMillis();
		dispatcher.dispatch(() -> TimeUtils.SECONDS.sleep(2), 1);
		dispatcher.dispatch(() -> TimeUtils.SECONDS.sleep(1), 2);
		dispatcher.dispatch(() -> {
			long useTime = System.currentTimeMillis() - startTime;
			Assertions.assertTrue(3000 <= useTime && useTime <= 3050, "[调度器][阻塞调用] 阻塞调用者");
			TimeUtils.SECONDS.sleep(1);
		}, 3);
		dispatcher.shutdown();
		long useTime = System.currentTimeMillis() - startTime;
		Assertions.assertTrue(4000 <= useTime && useTime <= 4050, "[调度器][阻塞调用] 调用耗时：" + useTime);
	}

	enum HandlerType {
		POOL
	}
}
