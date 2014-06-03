package ${package}.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.frameworkdemoiselle.junit.DemoiselleRunner;
import ${package}.entity.Bookmark;

@RunWith(DemoiselleRunner.class)
public class BookmarkBCTest {

	@Inject
	private BookmarkBC bookmarkBC;
	
	@Before
	public void before() {
		for (Bookmark bookmark : bookmarkBC.findAll()) {
			bookmarkBC.delete(bookmark.getId());
		}
	}

	@Test
	public void testLoad() {
		bookmarkBC.load();
		List<Bookmark> listaBookmarks = bookmarkBC.findAll();
		assertNotNull(listaBookmarks);
		assertEquals(10, listaBookmarks.size());
	}
	
	@Test
	public void testInsert() {
		Bookmark bookmark = new Bookmark("Demoiselle Portal", "http://www.frameworkdemoiselle.gov.br");
		bookmarkBC.insert(bookmark);
		List<Bookmark> listaBookmarks = bookmarkBC.findAll();
		assertNotNull(listaBookmarks);
		assertEquals(1, listaBookmarks.size());
	}
	
	@Test
	public void testDelete() {
		Bookmark bookmark = new Bookmark("Demoiselle Portal", "http://www.frameworkdemoiselle.gov.br");
		bookmarkBC.insert(bookmark);
		
		List<Bookmark> listaBookmarks = bookmarkBC.findAll();
		assertNotNull(listaBookmarks);
		assertEquals(1, listaBookmarks.size());
		
		bookmarkBC.delete(bookmark.getId());
		listaBookmarks = bookmarkBC.findAll();
		assertEquals(0, listaBookmarks.size());
	}
	@Test
	public void testUpdate() {
		Bookmark bookmark = new Bookmark("Demoiselle Portal", "http://www.frameworkdemoiselle.gov.br");
		bookmarkBC.insert(bookmark);
		
		List<Bookmark> listaBookmarks = bookmarkBC.findAll();
		Bookmark bookmark2 = (Bookmark)listaBookmarks.get(0);
		assertNotNull(listaBookmarks);
		assertEquals("Demoiselle Portal", bookmark2.getDescription());
		
		bookmark2.setDescription("Demoiselle Portal alterado");
		bookmarkBC.update(bookmark2);
		
		listaBookmarks = bookmarkBC.findAll();
		Bookmark bookmark3 = (Bookmark)listaBookmarks.get(0);
		assertEquals("Demoiselle Portal alterado", bookmark3.getDescription());
	}
}
