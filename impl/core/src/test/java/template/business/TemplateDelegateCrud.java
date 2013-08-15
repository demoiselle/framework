package template.business;

import template.crud.CrudImpl;
import template.model.DummyEntity;
import br.gov.frameworkdemoiselle.stereotype.BusinessController;
import br.gov.frameworkdemoiselle.template.DelegateCrud;

@BusinessController
public class TemplateDelegateCrud extends DelegateCrud<DummyEntity, Long, CrudImpl>{

}
