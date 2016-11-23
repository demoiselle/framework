package org.demoiselle.jee.multitenancy.hibernate.context;

import javax.enterprise.context.RequestScoped;

import org.demoiselle.jee.multitenancy.hibernate.entity.Tenant;

@RequestScoped
public class MultiTenantContext {

	private Tenant tenant;

	public MultiTenantContext() {

	}

	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

}
