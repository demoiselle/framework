package org.demoiselle.jee.multitenancy.hibernate.dao;

import javax.enterprise.inject.spi.CDI;

import org.demoiselle.jee.multitenancy.hibernate.context.MultiTenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class SchemaResolver implements CurrentTenantIdentifierResolver {

	@Override
	public String resolveCurrentTenantIdentifier() {
		MultiTenantContext o = CDI.current().select(MultiTenantContext.class).get();
		return o.getTenant().getName();
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return false;
	}

}