package ${package}.business;

import ${package}.entity.Bookmark;
import ${package}.persistence.BookmarkDAO;
import a8.a8.entity.Bookmark;
import br.gov.frameworkdemoiselle.lifecycle.Startup;
import br.gov.frameworkdemoiselle.stereotype.BusinessController;
import br.gov.frameworkdemoiselle.template.DelegateCrud;
import br.gov.frameworkdemoiselle.transaction.Transactional;

@BusinessController
public class BookmarkBC extends DelegateCrud<Bookmark, Long, BookmarkDAO> {

	private static final long serialVersionUID = 1L;

	@Startup
	@Transactional
	public void load() {
		if (findAll().isEmpty()) {
			insert(new Bookmark("Portal", "http://www.frameworkdemoiselle.gov.br"));
			insert(new Bookmark("Documentação", "http://demoiselle.sourceforge.net/docs/framework/reference"));
			insert(new Bookmark("Fórum", "http://pt.stackoverflow.com/tags/demoiselle"));
			insert(new Bookmark("Lista de usuários", "https://lists.sourceforge.net/lists/listinfo/demoiselle-users"));
			insert(new Bookmark("Blog oficial", "http://frameworkdemoiselle.wordpress.com"));
			insert(new Bookmark("Blog experimental", "http://demoisellelab.wordpress.com"));
			insert(new Bookmark("Repositório", "http://github.com/demoiselle/framework"));
			insert(new Bookmark("Bug Tracker", "https://demoiselle.atlassian.net"));
			insert(new Bookmark("Facebook", "http://facebook.com/FrameworkDemoiselle"));
			insert(new Bookmark("Twitter", "http://twitter.com/fwkdemoiselle"));
			insert(new Bookmark("Distribuição",
					"http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22br.gov.frameworkdemoiselle%22"));
			insert(new Bookmark("Binários", "http://sourceforge.net/projects/demoiselle/files/framework"));
		}
	}
}
