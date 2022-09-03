package com.keimons.dmq.internal;

import com.keimons.dmq.core.DispatchTask;
import com.keimons.dmq.core.Sequencer;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 串行模式
 * <p>
 * 不同的串行模式能提供不同的性能和特性：
 * <ul>
 *     <li>{@link #complete() <b>完整版一致性</b>}，对于所有生产者公平的模式，严格按照生产者的生产顺序进行消费。
 *     但是，这可能是个伪需求，对于不同的生产者而言，可能本身就无所谓顺序。</li>
 *     <li>{@link #producer() <b>生产者一致性</b>}，对于单个生产者，严格按照先生产先消费的顺序。能够提供更好的性能。</li>
 * </ul>
 * <p>
 * <b>注意</b>：这是内部API，随时可能发生变动，恕不另行通知。但是，任何的修改都是基于已提供的特性。
 * 内部API的废弃不会影响它的使用，仅代表它可能在未来的某个版本中被删除。
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class SerialMode {

	/**
	 * 完整版一致性
	 * <p>
	 * 先到先消费原则，能够提供针对于所有生产者而言的最强一致性。
	 */
	public static Serialization complete() {
		return new CompleteSerial();
	}

	/**
	 * 生产者一致性
	 * <p>
	 * 提供针对于单个生产者的先到先消费的强一致性。尽管这个实现并不能保证绝对的串行，
	 * 但生产者一致性能提供更好的性能。
	 */
	public static Serialization producer() {
		return new ProducerSerial(false);
	}

	private static class ProducerSerial implements Serialization {

		protected final Lock main;

		private ProducerSerial(boolean fair) {
			this.main = new ReentrantLock(fair);
		}


		@Override
		public void dispatch(DispatchTask dispatchTask, Sequencer sequencer) {
			sequencer.commit(dispatchTask);
		}

		@Override
		public void dispatch(DispatchTask dispatchTask, Sequencer a0, Sequencer a1) {
			main.lock();
			try {
				a0.commit(dispatchTask);
				a1.commit(dispatchTask);
			} finally {
				main.unlock();
			}
		}

		@Override
		public void dispatch(DispatchTask dispatchTask, Sequencer a0, Sequencer a1, Sequencer a2) {
			main.lock();
			try {
				a0.commit(dispatchTask);
				a1.commit(dispatchTask);
				a2.commit(dispatchTask);
			} finally {
				main.unlock();
			}
		}

		@Override
		public void dispatch(DispatchTask dispatchTask, Sequencer a0, Sequencer a1, Sequencer a2, Sequencer a3) {
			main.lock();
			try {
				a0.commit(dispatchTask);
				a1.commit(dispatchTask);
				a2.commit(dispatchTask);
				a3.commit(dispatchTask);
			} finally {
				main.unlock();
			}
		}

		@Override
		public void dispatch(DispatchTask dispatchTask, Sequencer a0, Sequencer a1, Sequencer a2, Sequencer a3,
							 Sequencer a4) {
			main.lock();
			try {
				a0.commit(dispatchTask);
				a1.commit(dispatchTask);
				a2.commit(dispatchTask);
				a3.commit(dispatchTask);
				a4.commit(dispatchTask);
			} finally {
				main.unlock();
			}
		}

		@Override
		public void dispatch(DispatchTask dispatchTask, Sequencer... sequencers) {
			main.lock();
			try {
				for (int i = 0; i < sequencers.length; i++) {
					sequencers[i].commit(dispatchTask);
				}
			} finally {
				main.unlock();
			}
		}
	}

	private static class CompleteSerial extends ProducerSerial {

		private CompleteSerial() {
			super(true);
		}

		@Override
		public void dispatch(DispatchTask dispatchTask, Sequencer sequencer) {
			main.lock();
			try {
				sequencer.commit(dispatchTask);
			} finally {
				main.unlock();
			}
		}
	}
}
