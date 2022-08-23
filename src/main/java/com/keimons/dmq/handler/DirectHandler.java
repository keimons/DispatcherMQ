package com.keimons.dmq.handler;

import com.keimons.dmq.core.Handler;
import com.keimons.dmq.core.Wrapper;

/**
 * DirectHandler
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class DirectHandler implements Handler<Runnable> {

	@Override
	public void handle(Wrapper<Runnable> wrapperTask) {
		wrapperTask.invoke();
	}
}
