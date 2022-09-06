package com.keimons.dispatcher.test;

import com.keimons.dispatcher.core.DispatchTask;
import com.keimons.dispatcher.core.Sequencer;
import com.keimons.dispatcher.core.utils.ForbidsUtils;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * ForbidsTest
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class ForbidsTest {

	@Test
	public void test() {
		Sequencer[] sequencers = new Sequencer[10];
		for (int i = 0; i < 8; i++) {
			sequencers[i] = new LocalSequencer(i);
		}
		Random random = new Random();
		for (int i = 0; i < 1000000; i++) {
			List<Sequencer> result = new ArrayList<>();
			for (int j = 0; j < 5; j++) {
				result.add(sequencers[random.nextInt(10)]);
			}
			int count = (int) result.stream().filter(Objects::nonNull).distinct().count();
			int forbids = ForbidsUtils.calcForbids(
					result.get(0), result.get(1), result.get(2), result.get(3), result.get(4)
			);
			Assertions.assertEquals(
					count, forbids,
					"[调度器][执行屏障] 目标拦截数量：" + count + "，实际拦截数量：" + forbids
			);
		}
	}

	private record LocalSequencer(int sequencerId) implements Sequencer {

		@Override
		public boolean isShutdown() {
			return false;
		}

		@Override
		public void commit(DispatchTask dispatchTask) {

		}

		@Override
		public void activate(@Nullable DispatchTask dispatchTask) {

		}

		@Override
		public String toString() {
			return String.valueOf(sequencerId);
		}
	}
}
