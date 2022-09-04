package com.keimons.dispatcher.test;

/**
 * 空任务
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class EmptyTask implements Runnable {

	public static final EmptyTask instance = new EmptyTask();

	@Override
	public void run() {

	}
}
