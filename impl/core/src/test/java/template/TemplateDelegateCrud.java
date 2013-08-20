package template;

import br.gov.frameworkdemoiselle.stereotype.BusinessController;
import br.gov.frameworkdemoiselle.template.DelegateCrud;

@BusinessController
public class TemplateDelegateCrud extends DelegateCrud<DummyEntity, Long, CrudImpl>{

	private static final long serialVersionUID = 1L;

}
