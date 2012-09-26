package br.gov.frameworkdemoiselle.internal.interceptor;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;

import java.util.Locale;

import javax.enterprise.inject.Instance;
import javax.interceptor.InvocationContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.security.NotLoggedInException;
import br.gov.frameworkdemoiselle.security.RequiredPermission;
import br.gov.frameworkdemoiselle.security.SecurityContext;
import br.gov.frameworkdemoiselle.security.SecurityException;
import br.gov.frameworkdemoiselle.security.User;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Beans.class)
public class RequiredPermissionInterceptorTest {

	private RequiredPermissionInterceptor interceptor;

	private InvocationContext ic;

	private SecurityContext securityContext;

	class UnnamedClass {

		@RequiredPermission
		public void requiredPermissionWithoutDeclaredResourceAndOperation() {
		}

		@RequiredPermission(operation = "insert")
		public void requiredPermissionWithDeclaredOperation() {
		}

		@RequiredPermission(resource = "contact")
		public void requiredPermissionWithDeclaredResource() {
		}

		@RequiredPermission(resource = "contact", operation = "insert")
		public void requiredPermissionWithDeclaredResourceAndOperation() {
		}
	}

	@Name("contact2")
	class NamedClass {

		@RequiredPermission
		public void requiredPermissionWithoutDeclaredResourceAndOperation() {
		}

		@RequiredPermission(operation = "insert")
		public void requiredPermissionWithDeclaredOperation() {
		}

		@RequiredPermission(resource = "contact")
		public void requiredPermissionWithDeclaredResource() {
		}

		@RequiredPermission(resource = "contact", operation = "insert")
		public void requiredPermissionWithDeclaredResourceAndOperation() {
		}
	}

	@Name("contact2")
	class NamedClassWithNamedMethods {

		@Name("delete")
		@RequiredPermission
		public void requiredPermissionWithoutDeclaredResourceAndOperation() {
		}

		@Name("delete")
		@RequiredPermission(operation = "insert")
		public void requiredPermissionWithDeclaredOperation() {
		}

		@Name("delete")
		@RequiredPermission(resource = "contact")
		public void requiredPermissionWithDeclaredResource() {
		}

		@Name("delete")
		@RequiredPermission(resource = "contact", operation = "insert")
		public void requiredPermissionWithDeclaredResourceAndOperation() {
		}
	}

	@RequiredPermission
	class ClassAnnotedWithRequiredPermission {

		public void withoutRequiredPermissionAnnotation() {
		}

		@RequiredPermission(operation = "insert")
		public void requiredPermissionWithDeclaredOperation() {
		}
	}

	@Before
	public void setUp() throws Exception {
		@SuppressWarnings("unchecked")
		Instance<SecurityContext> securityContextInstance = createMock(Instance.class);

		User user = createMock(User.class);

		this.securityContext = createMock(SecurityContext.class);
		this.ic = createMock(InvocationContext.class);

		mockStatic(Beans.class);
		expect(Beans.getReference(Locale.class)).andReturn(Locale.getDefault());
		expect(Beans.getReference(SecurityContext.class)).andReturn(this.securityContext);

		expect(user.getId()).andReturn("UserName").anyTimes();
		expect(this.securityContext.getUser()).andReturn(user).anyTimes();
		expect(securityContextInstance.get()).andReturn(securityContext).anyTimes();
		expect(this.ic.proceed()).andReturn(null);
		replay(securityContextInstance, user, Beans.class);

		this.interceptor = new RequiredPermissionInterceptor();
	}

	private void prepareMock(Object target, String methodName, String expectedResource, String expectedOperation,
			boolean hasPermission, boolean isLoggedUser) throws Exception {

		expect(this.securityContext.isLoggedIn()).andReturn(isLoggedUser).anyTimes();

		this.securityContext.hasPermission(expectedResource, expectedOperation);

		if (isLoggedUser) {
			expectLastCall().andReturn(hasPermission);
		} else {
			expectLastCall().andThrow(new NotLoggedInException(""));
		}

		expect(this.ic.getTarget()).andReturn(target).anyTimes();
		expect(this.ic.getMethod()).andReturn(target.getClass().getMethod(methodName)).anyTimes();
		replay(this.ic, this.securityContext);
	}

	/* Testing UnnamedClass */

	@Test
	public void testManageUnnamedClassAtRequiredPermissionWithoutDeclaredResourceAndOperationMethod() throws Exception {
		try {
			Object target = new UnnamedClass();
			String methodName = "requiredPermissionWithoutDeclaredResourceAndOperation";
			String expectedResource = target.getClass().getSimpleName();
			String expectedOperation = methodName;
			prepareMock(target, methodName, expectedResource, expectedOperation, true, true);

			interceptor.manage(this.ic);
		} catch (SecurityException cause) {
			fail();
		}
	}

	@Test
	public void testManageUnnamedClassAtRequiredPermissionWithDeclaredOperationMethod() throws Exception {
		try {
			Object target = new UnnamedClass();
			String methodName = "requiredPermissionWithDeclaredOperation";
			String expectedResource = target.getClass().getSimpleName();
			String expectedOperation = "insert";
			prepareMock(target, methodName, expectedResource, expectedOperation, true, true);

			interceptor.manage(this.ic);
		} catch (SecurityException cause) {
			fail();
		}
	}

	@Test
	public void testManageUnnamedClassAtRequiredPermissionWithDeclaredResourceMethod() throws Exception {
		try {
			Object target = new UnnamedClass();
			String methodName = "requiredPermissionWithDeclaredResource";
			String expectedResource = "contact";
			String expectedOperation = methodName;
			prepareMock(target, methodName, expectedResource, expectedOperation, true, true);

			interceptor.manage(this.ic);
		} catch (SecurityException cause) {
			fail();
		}
	}

	@Test
	public void testManageUnnamedClassAtRequiredPermissionWithDeclaredResourceAndOperation() throws Exception {
		try {
			Object target = new UnnamedClass();
			String methodName = "requiredPermissionWithDeclaredResourceAndOperation";
			String expectedResource = "contact";
			String expectedOperation = "insert";
			prepareMock(target, methodName, expectedResource, expectedOperation, true, true);

			interceptor.manage(this.ic);
		} catch (SecurityException cause) {
			fail();
		}
	}

	/* Testing NamedClass */

	@Test
	public void testManageNamedClassAtRequiredPermissionWithoutDeclaredResourceAndOperationMethod() throws Exception {
		try {
			Object target = new NamedClass();
			String methodName = "requiredPermissionWithoutDeclaredResourceAndOperation";
			String expectedResource = "contact2";
			String expectedOperation = methodName;
			prepareMock(target, methodName, expectedResource, expectedOperation, true, true);

			interceptor.manage(this.ic);
		} catch (SecurityException cause) {
			fail();
		}
	}

	@Test
	public void testManageNamedClassAtRequiredPermissionWithDeclaredOperationMethod() throws Exception {
		try {
			Object target = new NamedClass();
			String methodName = "requiredPermissionWithDeclaredOperation";
			String expectedResource = "contact2";
			String expectedOperation = "insert";
			prepareMock(target, methodName, expectedResource, expectedOperation, true, true);

			interceptor.manage(this.ic);
		} catch (SecurityException cause) {
			fail();
		}
	}

	@Test
	public void testManageNamedClassAtRequiredPermissionWithDeclaredResourceMethod() throws Exception {
		try {
			Object target = new NamedClass();
			String methodName = "requiredPermissionWithDeclaredResource";
			String expectedResource = "contact";
			String expectedOperation = methodName;
			prepareMock(target, methodName, expectedResource, expectedOperation, true, true);

			interceptor.manage(this.ic);
		} catch (SecurityException cause) {
			fail();
		}
	}

	@Test
	public void testManageNamedClassAtRequiredPermissionWithDeclaredResourceAndOperation() throws Exception {
		try {
			Object target = new NamedClass();
			String methodName = "requiredPermissionWithDeclaredResourceAndOperation";
			String expectedResource = "contact";
			String expectedOperation = "insert";
			prepareMock(target, methodName, expectedResource, expectedOperation, true, true);

			interceptor.manage(this.ic);
		} catch (SecurityException cause) {
			fail();
		}
	}

	/* Testing NamedClassWithNamedMethods */

	@Test
	public void testManageNamedClassWithNamedMethodsAtRequiredPermissionWithoutDeclaredResourceAndOperationMethod()
			throws Exception {
		try {
			Object target = new NamedClassWithNamedMethods();
			String methodName = "requiredPermissionWithoutDeclaredResourceAndOperation";
			String expectedResource = "contact2";
			String expectedOperation = "delete";
			prepareMock(target, methodName, expectedResource, expectedOperation, true, true);

			interceptor.manage(this.ic);
		} catch (SecurityException cause) {
			fail();
		}
	}

	@Test
	public void testManageNamedClassWithNamedMethodsAtRequiredPermissionWithDeclaredOperationMethod() throws Exception {
		try {
			Object target = new NamedClassWithNamedMethods();
			String methodName = "requiredPermissionWithDeclaredOperation";
			String expectedResource = "contact2";
			String expectedOperation = "insert";
			prepareMock(target, methodName, expectedResource, expectedOperation, true, true);

			interceptor.manage(this.ic);
		} catch (SecurityException cause) {
			fail();
		}
	}

	@Test
	public void testManageNamedClassWithNamedMethodsAtRequiredPermissionWithDeclaredResourceMethod() throws Exception {
		try {
			Object target = new NamedClassWithNamedMethods();
			String methodName = "requiredPermissionWithDeclaredResource";
			String expectedResource = "contact";
			String expectedOperation = "delete";
			prepareMock(target, methodName, expectedResource, expectedOperation, true, true);

			interceptor.manage(this.ic);
		} catch (SecurityException cause) {
			fail();
		}
	}

	@Test
	public void testManageNamedClassWithNamedMethodsAtRequiredPermissionWithDeclaredResourceAndOperation()
			throws Exception {
		try {
			Object target = new NamedClassWithNamedMethods();
			String methodName = "requiredPermissionWithDeclaredResourceAndOperation";
			String expectedResource = "contact";
			String expectedOperation = "insert";
			prepareMock(target, methodName, expectedResource, expectedOperation, true, true);

			interceptor.manage(this.ic);
		} catch (SecurityException cause) {
			fail();
		}
	}

	/* Testing ClassAnnotedWithRequiredPermission */

	@Test
	public void testManageClassAnnotedWithRequiredPermissionAtWithoutRequiredPermissionAnnotation() throws Exception {
		try {
			Object target = new ClassAnnotedWithRequiredPermission();
			String methodName = "withoutRequiredPermissionAnnotation";
			String expectedResource = target.getClass().getSimpleName();
			String expectedOperation = methodName;
			prepareMock(target, methodName, expectedResource, expectedOperation, true, true);

			interceptor.manage(this.ic);
		} catch (SecurityException cause) {
			fail();
		}
	}

	@Test
	public void testManageClassAnnotedWithRequiredPermissionAtRequiredPermissionWithDeclaredOperation()
			throws Exception {
		try {
			Object target = new ClassAnnotedWithRequiredPermission();
			String methodName = "requiredPermissionWithDeclaredOperation";
			String expectedResource = target.getClass().getSimpleName();
			String expectedOperation = "insert";
			prepareMock(target, methodName, expectedResource, expectedOperation, true, true);

			interceptor.manage(this.ic);
		} catch (SecurityException cause) {
			fail();
		}
	}

	/* Other tests */

	@Test
	public void testManagePermissionNotAllowed() throws Exception {
		try {
			Object target = new UnnamedClass();
			String methodName = "requiredPermissionWithDeclaredResourceAndOperation";
			String expectedResource = "contact";
			String expectedOperation = "insert";
			prepareMock(target, methodName, expectedResource, expectedOperation, false, true);

			interceptor.manage(this.ic);
			fail();
		} catch (SecurityException cause) {
		}
	}

	@Test
	public void testUserNotLoggedIn() throws Exception {
		try {
			Object target = new UnnamedClass();
			String methodName = "requiredPermissionWithDeclaredResourceAndOperation";
			String expectedResource = "contact";
			String expectedOperation = "insert";
			prepareMock(target, methodName, expectedResource, expectedOperation, true, false);

			interceptor.manage(this.ic);
			fail();
		} catch (SecurityException cause) {
		}
	}

}
