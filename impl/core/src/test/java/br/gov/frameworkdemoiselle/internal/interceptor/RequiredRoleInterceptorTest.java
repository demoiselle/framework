package br.gov.frameworkdemoiselle.internal.interceptor;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;

import java.util.Locale;

import javax.enterprise.inject.Instance;
import javax.interceptor.InvocationContext;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.security.AuthorizationException;
import br.gov.frameworkdemoiselle.security.NotLoggedInException;
import br.gov.frameworkdemoiselle.security.RequiredRole;
import br.gov.frameworkdemoiselle.security.SecurityContext;
import br.gov.frameworkdemoiselle.security.User;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Beans.class)
public class RequiredRoleInterceptorTest {

	private RequiredRoleInterceptor interceptor;

	private InvocationContext ic;

	private SecurityContext securityContext;

	class ClassNotAnnoted {

		@RequiredRole("simpleRoleName")
		public void requiredRoleWithSingleRole() {
		}

		@RequiredRole({ "firstRole", "secondRole", "thirdRole", "fourthRole", "fifthRole" })
		public void requiredRoleWithArrayOfRoles() {
		}

		@RequiredRole({ "firstRole, secondRole" })
		public void requiredRoleWithArrayOfSingleRoleComma() {
		}

		@RequiredRole("firstRole, secondRole")
		public void requiredRoleWithSingleRoleComma() {
		}

		@RequiredRole("")
		public void requiredRoleWithEmptyValue() {
		}

		@RequiredRole({})
		public void requiredRoleWithEmptyArray() {
		}

		@RequiredRole({ "" })
		public void requiredRoleWithArrayOfEmptyString() {
		}

		public void methodNotAnnoted() {
		}
	}

	@RequiredRole("classRole")
	class ClassAnnotedWithRequiredRole {

		public void withoutRole() {
		}

		@RequiredRole("simpleRoleName")
		public void requiredRoleWithSingleRole() {
		}
	}

	@Before
	public void setUp() throws Exception {

		@SuppressWarnings("unchecked")
		Instance<SecurityContext> securityContextInstance = EasyMock.createMock(Instance.class);
		User user = EasyMock.createMock(User.class);

		this.securityContext = EasyMock.createMock(SecurityContext.class);
		this.ic = EasyMock.createMock(InvocationContext.class);

		mockStatic(Beans.class);
		expect(Beans.getReference(Locale.class)).andReturn(Locale.getDefault());
		expect(Beans.getReference(SecurityContext.class)).andReturn(this.securityContext);

		expect(user.getId()).andReturn("UserName").anyTimes();
		expect(this.securityContext.getUser()).andReturn(user).anyTimes();
		expect(securityContextInstance.get()).andReturn(securityContext).anyTimes();
		expect(this.ic.proceed()).andReturn(null);
		replay(securityContextInstance, user, Beans.class);

		this.interceptor = new RequiredRoleInterceptor();
	}

	private void prepareMock(Object target, String methodName, String[] expectedRoles, boolean hasHole,
			boolean isLoggedUser) throws Exception {

		expect(this.securityContext.isLoggedIn()).andReturn(isLoggedUser).anyTimes();

		for (String role : expectedRoles) {
			this.securityContext.hasRole(role);
			if (isLoggedUser) {
				EasyMock.expectLastCall().andReturn(hasHole);
			} else {
				EasyMock.expectLastCall().andThrow(new NotLoggedInException(""));
			}
		}

		expect(this.ic.getTarget()).andReturn(target).anyTimes();
		expect(this.ic.getMethod()).andReturn(target.getClass().getMethod(methodName)).anyTimes();
		replay(this.ic, this.securityContext);
	}

	// @Test
	// public void testManageClassNotAnnotedAtRequiredRoleWithSingleRole() throws Exception {
	// Object target = new ClassNotAnnoted();
	// String methodName = "requiredRoleWithSingleRole";
	// String[] expectedRoles = { "simpleRoleName" };
	// prepareMock(target, methodName, expectedRoles, true, true);
	//
	// interceptor.manage(this.ic);
	// }

	// @Test
	// public void testManageClassNotAnnotedAtRequiredRoleWithArrayOfRoles() throws Exception {
	// Object target = new ClassNotAnnoted();
	// String methodName = "requiredRoleWithArrayOfRoles";
	// String[] expectedRoles = { "firstRole", "secondRole", "thirdRole", "fourthRole", "fifthRole" };
	// prepareMock(target, methodName, expectedRoles, true, true);
	//
	// interceptor.manage(this.ic);
	// }

	// @Test
	// public void testManageClassNotAnnotedAtRequiredRoleWithArrayOfSingleRoleComma() throws Exception {
	// Object target = new ClassNotAnnoted();
	// String methodName = "requiredRoleWithArrayOfSingleRoleComma";
	// String[] expectedRoles = { "firstRole, secondRole" };
	// prepareMock(target, methodName, expectedRoles, true, true);
	//
	// interceptor.manage(this.ic);
	// }

	// @Test
	// public void testManageClassNotAnnotedAtRequiredRoleWithSingleRoleComma() throws Exception {
	// Object target = new ClassNotAnnoted();
	// String methodName = "requiredRoleWithSingleRoleComma";
	// String[] expectedRoles = { "firstRole, secondRole" };
	// prepareMock(target, methodName, expectedRoles, true, true);
	//
	// interceptor.manage(this.ic);
	// }

	// @Test
	// public void testManageClassNotAnnotedAtRequiredRoleWithEmptyValue() throws Exception {
	// try {
	// Object target = new ClassNotAnnoted();
	// String methodName = "requiredRoleWithEmptyValue";
	// String[] expectedRoles = { "" };
	// prepareMock(target, methodName, expectedRoles, false, true);
	//
	// interceptor.manage(this.ic);
	// fail();
	// } catch (AuthorizationException cause) {
	// }
	// }

	// @Test
	// public void testManageClassNotAnnotedAtRequiredRoleWithEmptyArray() throws Exception {
	// try {
	// Object target = new ClassNotAnnoted();
	// String methodName = "requiredRoleWithEmptyArray";
	// String[] expectedRoles = { "" };
	// prepareMock(target, methodName, expectedRoles, false, true);
	//
	// interceptor.manage(this.ic);
	// fail();
	// } catch (AuthorizationException cause) {
	// }
	// }

	// @Test
	// public void testManageClassNotAnnotedAtRequiredRoleWithArrayOfEmptyString() throws Exception {
	// try {
	// Object target = new ClassNotAnnoted();
	// String methodName = "requiredRoleWithArrayOfEmptyString";
	// String[] expectedRoles = { "" };
	// prepareMock(target, methodName, expectedRoles, false, true);
	//
	// interceptor.manage(this.ic);
	// fail();
	// } catch (AuthorizationException cause) {
	// }
	// }

	// @Test
	// public void testManageClassNotAnnotedAtMethodNotAnnoted() throws Exception {
	// try {
	// Object target = new ClassNotAnnoted();
	// String methodName = "methodNotAnnoted";
	// String[] expectedRoles = { "" };
	// prepareMock(target, methodName, expectedRoles, false, true);
	//
	// interceptor.manage(this.ic);
	// fail();
	// } catch (AuthorizationException cause) {
	// }
	// }

	// @Test
	// public void testManageClassAnnotedWithRequiredRoleAtWithoutRole() throws Exception {
	// Object target = new ClassAnnotedWithRequiredRole();
	// String methodName = "withoutRole";
	// String[] expectedRoles = { "classRole" };
	// prepareMock(target, methodName, expectedRoles, true, true);
	//
	// interceptor.manage(this.ic);
	// }

	// @Test
	// public void testManageClassAnnotedWithRequiredRoleAtRequiredRoleWithSingleRole() throws Exception {
	// Object target = new ClassAnnotedWithRequiredRole();
	// String methodName = "requiredRoleWithSingleRole";
	// String[] expectedRoles = { "simpleRoleName" };
	// prepareMock(target, methodName, expectedRoles, true, true);
	//
	// interceptor.manage(this.ic);
	// }

	// @Test
	// public void testDoesNotHaveSingleRole() throws Exception {
	// try {
	// Object target = new ClassNotAnnoted();
	// String methodName = "requiredRoleWithSingleRole";
	// String[] expectedRoles = { "simpleRoleName" };
	// prepareMock(target, methodName, expectedRoles, false, true);
	//
	// interceptor.manage(this.ic);
	// fail();
	// } catch (AuthorizationException cause) {
	// }
	// }

	@Test
	public void testUserNotLoggedIn() throws Exception {
		try {
			Object target = new ClassNotAnnoted();
			String methodName = "requiredRoleWithSingleRole";
			String[] expectedRoles = { "simpleRoleName" };
			prepareMock(target, methodName, expectedRoles, true, false);

			interceptor.manage(this.ic);
			fail();
		} catch (NotLoggedInException cause) {
		}
	}

	// @Test
	// public void testDoesNotHaveOneOrMoreRolesOfArray() throws Exception {
	// Object target = new ClassNotAnnoted();
	// String methodName = "requiredRoleWithArrayOfRoles";
	// String[] expectedRoles = { "thirdRole", "fourthRole", "fifthRole" };
	//
	// expect(this.securityContext.hasRole("firstRole")).andReturn(false);
	// expect(this.securityContext.hasRole("secondRole")).andReturn(false);
	//
	// prepareMock(target, methodName, expectedRoles, true, true);
	//
	// interceptor.manage(this.ic);
	// }

	// @Test
	// public void testHasMoreRolesThenArray() throws Exception {
	// Object target = new ClassNotAnnoted();
	// String methodName = "requiredRoleWithArrayOfRoles";
	// String[] expectedRoles = { "thirdRole", "fourthRole", "fifthRole" };
	//
	// expect(this.securityContext.hasRole("firstRole")).andReturn(false);
	// expect(this.securityContext.hasRole("secondRole")).andReturn(false);
	// expect(this.securityContext.hasRole("sixthRole")).andReturn(true);
	//
	// prepareMock(target, methodName, expectedRoles, true, true);
	//
	// interceptor.manage(this.ic);
	// }
}
