package br.gov.frameworkdemoiselle.management.extension;

import java.util.List;

import br.gov.frameworkdemoiselle.management.internal.ManagedType;

/**
 * 
 * Define an entry point for monitoring extension.
 * 
 * @author serpro
 *
 */
public interface ManagementExtension {

	void initialize(List<ManagedType> managedTypes);

	void shutdown(List<ManagedType> managedTypes);

}
