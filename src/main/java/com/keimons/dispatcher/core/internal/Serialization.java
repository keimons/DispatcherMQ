package com.keimons.dispatcher.core.internal;

import com.keimons.dispatcher.core.DispatchTask;
import com.keimons.dispatcher.core.Sequencer;

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
