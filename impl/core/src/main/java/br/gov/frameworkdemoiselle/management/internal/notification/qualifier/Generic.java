package br.gov.frameworkdemoiselle.management.internal.notification.qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import br.gov.frameworkdemoiselle.management.internal.notification.event.NotificationEvent;
import br.gov.frameworkdemoiselle.management.notification.Notification;

/**
 * 
 * Enables {@link NotificationEvent} observers to trigger only with notifications
 * of the base type {@link Notification}.
 * 
 * @author serpro
 *
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
public @interface Generic {

}
