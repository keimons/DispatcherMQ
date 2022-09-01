package com.keimons.dmq.test;

import com.keimons.dmq.core.CompositeHandler;
import com.keimons.dmq.core.Dispatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 调度器退出测试
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class CompositeHandlerExitTest {

	private static final int N_THREAD = 10;

	@Test
	public void test() {
		int count = Thread.activeCount();
		CompositeHandler<?> dispatch = Dispatchers.newCompositeHandler(N_THREAD, Dispatchers.DEFAULT_DIRECT_HANDLER);
		int newCount = Thread.activeCount();
		Assertions.assertEquals(newCount, count + N_THREAD, "[调度器][退出] 创建线程数量错误");
		dispatch.dispatch(() -> TimeUtils.SECONDS.sleep(2));
		dispatch.shutdown();
		Assertions.assertEquals(count, Thread.activeCount(), "[调度器][退出] 销毁线程数量错误");
	}
}
