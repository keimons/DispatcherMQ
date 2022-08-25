package com.keimons.dmq.internal;

import com.keimons.dmq.core.Sequencer;
import com.keimons.dmq.core.DispatchTask;

/**
 * Serialization
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public interface Serialization {

	void dispatch(DispatchTask dispatchTask, Sequencer sequencer);

	void dispatch(DispatchTask dispatchTask, Sequencer a0, Sequencer a1);

	void dispatch(DispatchTask dispatchTask, Sequencer a0, Sequencer a1, Sequencer a2);

	void dispatch(DispatchTask dispatchTask, Sequencer a0, Sequencer a1, Sequencer a2, Sequencer a3);

	void dispatch(DispatchTask dispatchTask, Sequencer a0, Sequencer a1, Sequencer a2, Sequencer a3, Sequencer a4);

	void dispatch(DispatchTask dispatchTask, Sequencer... sequencers);
}
