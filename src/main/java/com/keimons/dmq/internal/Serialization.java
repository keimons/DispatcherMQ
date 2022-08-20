package com.keimons.dmq.internal;

import com.keimons.dmq.core.Actuator;
import com.keimons.dmq.core.DispatchTask;

/**
 * Serialization
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public interface Serialization {
	void dispatch(DispatchTask dispatchTask, Actuator actuator);

	void dispatch(DispatchTask dispatchTask, Actuator a0, Actuator a1);

	void dispatch(DispatchTask dispatchTask, Actuator a0, Actuator a1, Actuator a2);

	void dispatch(DispatchTask dispatchTask, Actuator a0, Actuator a1, Actuator a2, Actuator a3);

	void dispatch(DispatchTask dispatchTask, Actuator a0, Actuator a1, Actuator a2, Actuator a3, Actuator a4);

	void dispatch(DispatchTask dispatchTask, Actuator... actuators);
}
