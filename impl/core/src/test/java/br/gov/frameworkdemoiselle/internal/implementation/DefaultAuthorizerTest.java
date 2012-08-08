//package br.gov.frameworkdemoiselle.internal.implementation;
//
//import static org.easymock.EasyMock.expect;
//import static org.junit.Assert.assertTrue;
//import static org.powermock.api.easymock.PowerMock.mockStatic;
//import static org.powermock.api.easymock.PowerMock.replay;
//
//import java.util.Locale;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//import br.gov.frameworkdemoiselle.DemoiselleException;
//import br.gov.frameworkdemoiselle.util.ResourceBundle;
//
///**
// * @author SERPRO
// * @see DefaultAuthorizer
// */
//@RunWith(PowerMockRunner.class)
//@PrepareForTest(CoreBundle.class)
//public class DefaultAuthorizerTest {
//
//	private DefaultAuthorizer authorizer;
//
//	@Before
//	public void setUp() throws Exception {
//		authorizer = new DefaultAuthorizer();
//
//		mockStatic(CoreBundle.class);
//
//		ResourceBundle bundle = new ResourceBundle("demoiselle-core-bundle", Locale.getDefault());
//		expect(CoreBundle.get()).andReturn(bundle);
//
//		replay(CoreBundle.class);
//	}
//
//	@After
//	public void tearDown() {
//		authorizer = null;
//	}
//
//	@Test
//	public void testHasRole() {
//		try {
//			authorizer.hasRole(null);
//		} catch (Exception e) {
//			assertTrue(e instanceof DemoiselleException);
//		}
//	}
//
//	@Test
//	public void testHasPermission() {
//		try {
//			authorizer.hasPermission(null, null);
//		} catch (Exception e) {
//			assertTrue(e instanceof DemoiselleException);
//		}
//	}
//
//}
