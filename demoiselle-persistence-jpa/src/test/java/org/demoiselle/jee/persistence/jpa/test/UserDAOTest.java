package org.demoiselle.jee.persistence.jpa.test;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.UserTransaction;

import org.demoiselle.jee.core.exception.DemoiselleException;
import org.demoiselle.jee.persistence.jpa.crud.GenericCrudDAO;
import org.demoiselle.jee.persistence.jpa.crud.GenericDataPage;
import org.demoiselle.jee.persistence.jpa.entity.User;
import org.demoiselle.jee.persistence.jpa.exception.DemoisellePersistenceException;
import org.demoiselle.jee.persistence.jpa.test.dao.UserDAO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserDAOTest {

	public static final String[] USER_NAMES = { "Homer Jay Simpson", "Marjorie Jo-Jo Bouvier Simpson",
			"Bartholomew Jay Jo-Jo Simpson", "Elizabeth Marie Jay Jo-Jo Simpson", "Margaret Simpson" };

	public static final String[] NEW_USER_NAMES = { "Homer Simpson", "Marge Simpson", "Bart Simpson", "Lisa Simpson",
			"Maggie Simpson" };

	public static final String[] USER_EMAILS = { "homer@domain.com", "marge@domain.com", "bart@domain.com",
			"lisa@domain.com", "maggie@domain.com" };

	@Inject
	private UserDAO dao;

	@Inject
	UserTransaction userTransaction;

	@Deployment
	public static Archive<?> createDeployment() {
		WebArchive war = ShrinkWrap.create(WebArchive.class, "persistence-test.war");
		war.addPackage(User.class.getPackage());
		war.addPackage(UserDAO.class.getPackage());
		war.addPackage(DemoiselleException.class.getPackage());
		war.addPackage(DemoisellePersistenceException.class.getPackage());
		war.addPackage(GenericCrudDAO.class.getPackage());
		war.addAsResource("test-persistence.xml", "META-INF/persistence.xml");
		war.addAsWebInfResource("jbossas-ds.xml");
		war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

		return war;
	}

	@Test
	public void A_create() throws Exception {
		userTransaction.begin();
		User u = new User(USER_NAMES[0], USER_EMAILS[0]);
		dao.create(u);
		userTransaction.commit();
	}

	@Test
	public void B_update() throws Exception {
		userTransaction.begin();
		User u = new User(USER_NAMES[1], USER_EMAILS[1]);
		dao.create(u);
		u.setName(NEW_USER_NAMES[1]);
		dao.edit(u);
		userTransaction.commit();

		User uFinder = dao.find(u.getId());
		Assert.assertEquals(uFinder.getName(), u.getName());
	}

	@Test
	public void C_delete() throws Exception {
		userTransaction.begin();
		User u = new User(USER_NAMES[2], USER_EMAILS[2]);
		dao.create(u);
		dao.remove(u);
		userTransaction.commit();
	}

	@Test
	public void D_find() throws Exception {
		userTransaction.begin();
		User u = new User(USER_NAMES[3], USER_EMAILS[3]);
		dao.create(u);
		userTransaction.commit();

		User uFinder = dao.find(u.getId());

		Assert.assertEquals(uFinder.getName(), u.getName());
	}

	@Test
	public void E_findAll() throws Exception {
		List<User> uFinder = dao.findAll();
		Assert.assertEquals(uFinder.size(), 3);
	}

	@Test
	public void F_findRange() throws Exception {
		List<User> uFinder = dao.findRange(new int[] { 2, 3 });
		Assert.assertEquals(uFinder.size(), 1);
	}

	@Test
	public void G_findDataPAge() throws Exception {
		GenericDataPage page = dao.list();
		Assert.assertEquals(page.getTotal(), 3);
		Assert.assertEquals(page.getContent().size(), 3);
	}

	@Test
	public void H_findWhere() throws Exception {
		List<User> page = dao.find("name", USER_NAMES[3], "id", "ASC", 0, 5);
		Assert.assertEquals(page.size(), 1);
		Assert.assertEquals(page.get(0).getName(), USER_NAMES[3]);
	}

	@Test
	public void I_count() throws Exception {
		Long total = dao.count();
		Assert.assertEquals(total, new Long(3));
	}

	@Test
	public void J_exceptions() throws Exception {
		try {
			try {
				throw new DemoisellePersistenceException("Erro na persistencia");
			} catch (Exception e) {
				throw new DemoisellePersistenceException(e);
			}
		} catch (Exception e) {

		}
	}

	@Test
	public void K_DataPage() throws Exception {
		List<String> l = new ArrayList<String>();
		for (String s : USER_NAMES) {
			l.add(s);
		}
		GenericDataPage page = new GenericDataPage(l, 0, 10, new Long(USER_NAMES.length), "name", "busca");
		Assert.assertEquals(page.getFrom(), 0);
		Assert.assertEquals(page.getSize(), 10);
		
		page.setTotal(5);
		page.setFrom(5);
		page.setFields("name");
		page.setSearch("busca");
		page.setSize(10);
		page.setContent(l);
		page.setSize(10);
		
		Assert.assertEquals(page.getFrom(), 5);
		Assert.assertEquals(page.getSize(), 10);
		Assert.assertEquals(page.getFields(), "name");
		Assert.assertEquals(page.getSearch(), "busca");
	}

}
