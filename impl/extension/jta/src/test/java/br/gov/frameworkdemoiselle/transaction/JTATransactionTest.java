package br.gov.frameworkdemoiselle.transaction;
import org.junit.Ignore;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verify;
import static org.powermock.reflect.Whitebox.setInternalState;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.util.Beans;
@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Beans.class })
public class JTATransactionTest {

	private UserTransaction userTransaction;

	private JTATransaction jtaTransaction;

	@Before
	public void setUp() {

		userTransaction = createMock(UserTransaction.class);
		jtaTransaction = new JTATransaction();

		setInternalState(jtaTransaction, UserTransaction.class, userTransaction);
	}

	@Test
	public void testGetDElegateWithUserTransactionNull() throws SystemException {
		mockStatic(Beans.class);
		userTransaction = null;
		setInternalState(jtaTransaction, UserTransaction.class, userTransaction);
		userTransaction = createMock(UserTransaction.class);
		expect(Beans.getReference(UserTransaction.class)).andReturn(userTransaction);
		replayAll();

		Assert.assertEquals(userTransaction, jtaTransaction.getDelegate());
	}

	@Test
	public void testGetDElegateWithUserTransactionIsNotNull() throws SystemException {
		Assert.assertEquals(userTransaction, jtaTransaction.getDelegate());
	}

	@Test
	public void testIsActiveWithStatusEqualsActive() throws SystemException {
		expect(userTransaction.getStatus()).andReturn(Integer.valueOf(0));
		replay(userTransaction);
		Assert.assertTrue(this.jtaTransaction.isActive());
	}

	@Test
	public void testIsActiveWithStatusEqualsMarkedRollback() throws SystemException {
		expect(userTransaction.getStatus()).andReturn(Integer.valueOf(1)).times(2);
		replay(userTransaction);
		Assert.assertTrue(this.jtaTransaction.isActive());
	}

	@Test
	public void testIsMarkedRollback() throws SystemException {
		expect(userTransaction.getStatus()).andReturn(Integer.valueOf(1));
		replay(userTransaction);
		Assert.assertTrue(this.jtaTransaction.isMarkedRollback());
	}

	@Test
	public void testBegin() throws SystemException, NotSupportedException {
		userTransaction.begin();
		replay(userTransaction);
		this.jtaTransaction.begin();
		verify();
	}

	@Test
	public void testCommit() throws SystemException, NotSupportedException, SecurityException, IllegalStateException,
			RollbackException, HeuristicMixedException, HeuristicRollbackException {
		userTransaction.commit();
		replay(userTransaction);
		this.jtaTransaction.commit();
		verify();
	}

	@Test
	public void testRollback() throws SystemException, NotSupportedException {
		userTransaction.rollback();
		replay(userTransaction);
		this.jtaTransaction.rollback();
		verify();
	}

	@Test
	public void testSetRollbackOnly() throws SystemException, NotSupportedException {
		userTransaction.setRollbackOnly();
		replay(userTransaction);
		this.jtaTransaction.setRollbackOnly();
		verify();
	}

	@Test
	public void testIsActiveThrowsSystemException() throws SystemException {
		expect(userTransaction.getStatus()).andThrow(new SystemException());
		replay(userTransaction);
		try {
			this.jtaTransaction.isActive();
			Assert.fail();
		} catch (TransactionException cause) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testIsMarkedRollbackThrowsSystemException() throws SystemException {
		expect(userTransaction.getStatus()).andThrow(new SystemException());
		replay(userTransaction);
		try {
			this.jtaTransaction.isMarkedRollback();
			Assert.fail();
		} catch (TransactionException cause) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testBeginThrowsException() throws SystemException, NotSupportedException {
		userTransaction.begin();
		expectLastCall().andThrow(new SystemException());
		replay(userTransaction);
		try {
			this.jtaTransaction.begin();
			Assert.fail();
		} catch (TransactionException cause) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testCommitThrowsException() throws SystemException, SecurityException, IllegalStateException,
			RollbackException, HeuristicMixedException, HeuristicRollbackException {
		userTransaction.commit();
		expectLastCall().andThrow(new SystemException());
		replay(userTransaction);
		try {
			this.jtaTransaction.commit();
			Assert.fail();
		} catch (TransactionException cause) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testRollbackThrowsSystemException() throws SystemException {
		userTransaction.rollback();
		expectLastCall().andThrow(new SystemException());
		replay(userTransaction);
		try {
			this.jtaTransaction.rollback();
			Assert.fail();
		} catch (TransactionException cause) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testSetRollbackOnlyThrowsSystemException() throws SystemException {
		userTransaction.setRollbackOnly();
		expectLastCall().andThrow(new SystemException());
		replay(userTransaction);
		try {
			this.jtaTransaction.setRollbackOnly();
			Assert.fail();
		} catch (TransactionException cause) {
			Assert.assertTrue(true);
		}
	}

}
