package org.demoiselle.jee.configuration.extractor;

import java.io.IOException;

import org.demoiselle.jee.configuration.util.UtilTest;
import org.junit.After;

public class AbstractConfigurationTest {
	
	protected String FILE_PREFIX = "app";
	protected String PREFIX = "";
	protected UtilTest utilTest = new UtilTest();
	
	@After
	public void destroy() throws IOException{
		utilTest.deleteFilesAfterTest();
	}

}
