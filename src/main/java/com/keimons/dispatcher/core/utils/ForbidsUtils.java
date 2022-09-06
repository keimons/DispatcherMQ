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
	 *
	 * @param s0 定序器
	 * @param s1 定序器
	 * @param s2 定序器
	 * @param s3 定序器
	 * @param s4 定序器
	 * @return 拦截量
	 */
	private static int calcMaxIndex(Sequencer s0, Sequencer s1, Sequencer s2, Sequencer s3, Sequencer s4) {
		int index = -1;
		int seq0 = s0 == null ? -1 : s0.sequencerId();
		int seq1 = s1 == null ? -1 : s1.sequencerId();
		int seq2 = s2 == null ? -1 : s2.sequencerId();
		int seq3 = s3 == null ? -1 : s3.sequencerId();
		int seq4 = s4 == null ? -1 : s4.sequencerId();
		// 传导，找到最大值
		if (seq0 >= 0) {
			index = 0;
		}
		if (seq1 > seq0) {
			index = 1;
		} else {
			seq1 = seq0;
		}
		if (seq2 > seq1) {
			index = 2;
		} else {
			seq2 = seq1;
		}
		if (seq3 > seq2) {
			index = 3;
		} else {
			seq3 = seq2;
		}
		if (seq4 > seq3) {
			index = 4;
		}
		return index;
	}

	/**
	 * 计算拦截量
	 * <p>
	 * 不采用数组等算法，不计算空值，计算不重复的定序器数量。时间换空间的算法。
	 *
	 * @param s0 定序器
	 * @param s1 定序器
	 * @param s2 定序器
	 * @param s3 定序器
	 * @param s4 定序器
	 * @return 拦截量
	 */
	public static int calcForbids(Sequencer s0, Sequencer s1, Sequencer s2, Sequencer s3, Sequencer s4) {
		Sequencer o0 = null;
		Sequencer o1 = null;
		Sequencer o2 = null;
		Sequencer o3 = null;
		Sequencer o4 = null;
		for (int i = 0; i < 5; i++) {
			int index = calcMaxIndex(s0, s1, s2, s3, s4);
			Sequencer select = switch (index) {
				case 0 -> {
					Sequencer old = s0;
					s0 = null;
					yield old;
				}
				case 1 -> {
					Sequencer old = s1;
					s1 = null;
					yield old;
				}
				case 2 -> {
					Sequencer old = s2;
					s2 = null;
					yield old;
				}
				case 3 -> {
					Sequencer old = s3;
					s3 = null;
					yield old;
				}
				case 4 -> {
					Sequencer old = s4;
					s4 = null;
					yield old;
				}
				default -> null;
			};
			switch (i) {
				case 0 -> o0 = select;
				case 1 -> o1 = select;
				case 2 -> o2 = select;
				case 3 -> o3 = select;
				case 4 -> o4 = select;
			}
		}
		int count = 0;
		if (o0 != null) {
			count++;
		}
		if (o1 != null && o0 != o1) {
			count++;
		}
		if (o2 != null && o1 != o2) {
			count++;
		}
		if (o3 != null && o2 != o3) {
			count++;
		}
		if (o4 != null && o3 != o4) {
			count++;
		}
		return count;
	}
}
