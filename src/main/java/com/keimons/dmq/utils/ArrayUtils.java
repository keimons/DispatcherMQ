package com.keimons.dmq.utils;

import java.lang.reflect.Array;

/**
 * ArrayUtils
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class ArrayUtils {

	@SuppressWarnings("unchecked")
	public static <T> T newInstance(Class<?> clazz, int length) {
		return (T) Array.newInstance(clazz, length);
	}
}
