package ${package}.persistence;

import java.util.List;

import javax.persistence.TypedQuery;

import ${package}.entity.Bookmark;
import br.gov.frameworkdemoiselle.stereotype.PersistenceController;
import br.gov.frameworkdemoiselle.template.JPACrud;

@PersistenceController
public class BookmarkDAO extends JPACrud<Bookmark, Long> {

	private static final long serialVersionUID = 1L;

	public List<Bookmark> find(String filter) {
		StringBuffer ql = new StringBuffer();
		ql.append("  from Bookmark b ");
		ql.append(" where lower(b.description) like :description ");
		ql.append("    or lower(b.link) like :link ");

		TypedQuery<Bookmark> query = getEntityManager().createQuery(ql.toString(), Bookmark.class);
		query.setParameter("description", "%" + filter.toLowerCase() + "%");
		query.setParameter("link", "%" + filter.toLowerCase() + "%");

		return query.getResultList();
	}
}
