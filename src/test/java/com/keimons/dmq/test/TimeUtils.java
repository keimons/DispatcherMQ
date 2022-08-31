package com.keimons.dmq.test;

import java.util.concurrent.TimeUnit;

/**
 * 时间工具
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public enum TimeUtils {

	SECONDS(TimeUnit.SECONDS);

	final TimeUnit timeUnit;

	TimeUtils(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public void sleep(long timeout) {
		try {
			timeUnit.sleep(timeout);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
