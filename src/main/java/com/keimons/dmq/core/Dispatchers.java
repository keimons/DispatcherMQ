package com.keimons.dmq.core;

import com.keimons.dmq.handler.Handlers;
import com.keimons.dmq.internal.DefaultActuator;
import com.keimons.dmq.internal.DefaultCompositeHandler;
import com.keimons.dmq.internal.SerialMode;

import java.util.EnumMap;
import java.util.concurrent.Executors;

/**
 * 调度器
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class Dispatchers {

	private static final EnumMap<Type, Handler<Runnable>> DEFAULT_HANDLER_DIRECT = new EnumMap<>(Type.class);

	static {
		DEFAULT_HANDLER_DIRECT.put(Type.DEFAULT, Handlers.newDirectHandler());
	}

	/**
	 * 构造调度线程本地执行任务的调度器
	 * <p>
	 * 所有任务都将会在调度线程本地执行。
	 * <p>
	 * <b>注意：</b>如果任务被阻塞，将会影响该线程后续任务的执行。
	 *
	 * @param nThreads 调度线程数量
	 * @return 带有本地执行器的调度器
	 */
	public static Dispatcher<Runnable> newDispatcher(int nThreads) {
		return new DefaultCompositeHandler<>(nThreads, 0, nThreads,
				SerialMode.producer(),
				Executors.defaultThreadFactory(),
				DefaultActuator::new,
				DEFAULT_HANDLER_DIRECT
		);
	}

	/**
	 * 构造带有任务调度功能的复合执行器
	 *
	 * @param nThreads 调度线程数量
	 * @param handlers 复合处理器
	 * @param <E>      复合执行器类型
	 * @return 带有任务调度功能的复合执行器
	 */
	public static <E extends Enum<E>> CompositeHandler<E> newCompositeHandler(
			int nThreads, EnumMap<E, Handler<Runnable>> handlers) {
		return new DefaultCompositeHandler<>(nThreads, 0, nThreads,
				SerialMode.producer(), Executors.defaultThreadFactory(), DefaultActuator::new, handlers
		);
	}

	private enum Type {
		DEFAULT
	}
}
