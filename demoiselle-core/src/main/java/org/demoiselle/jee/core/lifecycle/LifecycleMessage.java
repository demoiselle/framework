package org.demoiselle.jee.core.lifecycle;

import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;

/**
 * 
 * Represents the Messages used by Lifecycle feature
 *
 */
@MessageBundle
public interface LifecycleMessage {
	
	@MessageTemplate("{executing-method}")
	String executingMethod(String method);
	
}
