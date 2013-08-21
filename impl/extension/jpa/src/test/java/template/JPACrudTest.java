package template;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;

@RunWith(Arquillian.class)
public class JPACrudTest {

	private static final String PATH = "src/test/resources/template";

	@Inject
	private MyCrud crud;

	@Deployment(name = "1")
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(JPACrudTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/persistence.xml"), "META-INF/persistence.xml");

		return deployment;
	}

	@Test
	public void x() {

	}

	@Test
	public void successfullyInserted() {
		populate(1, 0);

		MyEntity persisted = crud.load(createId("id-1"));
		assertNotNull(persisted);
	}

	@Test
	public void successfullyDeleted() {
		populate(1, 10);

		crud.delete(createId("id-11"));
		MyEntity persisted = crud.load(createId("id-11"));
		assertNull(persisted);
	}

	@Test
	public void successfullyUpdated() {
		populate(1, 20);

		MyEntity persisted;
		persisted = crud.load(createId("id-21"));
		persisted.setDescription("update example");

		crud.update(persisted);
		persisted = crud.load(createId("id-21"));

		assertEquals("update example", persisted.getDescription());
	}

	private void populate(int size, int offset) {
		MyEntity entity;

		for (int i = 0; i < size; i++) {
			entity = new MyEntity();
			entity.setId(createId("id-" + (i + 1 + offset)));
			entity.setDescription("desc-" + (i + 1 + offset));

			crud.insert(entity);
		}
	}

	private String createId(String id) {
		return this.getClass().getName() + "_" + id;
	}
}
