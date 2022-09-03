package com.keimons.dmq.test;

import com.keimons.dmq.core.Dispatcher;
import com.keimons.dmq.core.Dispatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 异常测试
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class ThrowableTest {

	@Test
	public void test() {
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			Assertions.assertInstanceOf(RuntimeException.class, e, "[调度器][异常测试] 异常测试失败");
		});
		Dispatcher<Runnable> dispatcher = Dispatchers.newDispatcher(1);
		dispatcher.dispatch(this::throwException, 0);
		dispatcher.dispatch(this::throwException, 0);
		dispatcher.dispatch(this::throwException, 0);
	}

	private void throwException() {
		throw new RuntimeException();
	}
}
