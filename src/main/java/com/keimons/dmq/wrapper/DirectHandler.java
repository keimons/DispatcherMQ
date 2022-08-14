package com.keimons.dmq.wrapper;

import com.keimons.dmq.core.Handler;
import com.keimons.dmq.core.Wrapper;

/**
 * DirectHandler
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class DirectHandler implements Handler<Wrapper<Runnable>> {

	@Override
	public void handle(Wrapper<Runnable> wrapperTask) {
		wrapperTask.invoke();
	}
}
