package management.testclasses;

import java.util.List;

import javax.inject.Inject;

import management.ManagedClassStore;

import br.gov.frameworkdemoiselle.management.extension.ManagementExtension;
import br.gov.frameworkdemoiselle.management.internal.ManagedType;

public class DummyManagementExtension implements ManagementExtension {

	@Inject
	private ManagedClassStore store;

	@Override
	public void initialize(List<ManagedType> managedTypes) {
		// Armazena os beans managed detectados neste store,
		// para depois serem testados.
		store.addManagedTypes(managedTypes);
	}

	@Override
	public void shutdown(List<ManagedType> managedTypes) {
		// Limpa o store, depois o teste verificará se
		// o processo de shutdown rodou e limpou o store.
		store.getManagedTypes().clear();
	}

}
