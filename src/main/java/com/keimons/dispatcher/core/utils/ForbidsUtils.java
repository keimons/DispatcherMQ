package com.keimons.dispatcher.core.utils;

import com.keimons.dispatcher.core.Sequencer;

/**
 * ForbidsUtils
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class ForbidsUtils {

	/**
	 * 计算拦截量
	 * <p>
	 * 不采用数组等算法，不计算空值，计算不重复的定序器数量。时间换空间的算法。
	 *
	 * @param s0 定序器
	 * @param s1 定序器
	 * @param s2 定序器
	 * @return 拦截量
	 */
	public static int calcForbids(Sequencer s0, Sequencer s1, Sequencer s2) {
		if (s0 == s1 && s1 == s2) {
			return 1;
		}
		if (s0 == s1 || s1 == s2 || s0 == s2) {
			return 2;
		}
		return 3;
	}
}
