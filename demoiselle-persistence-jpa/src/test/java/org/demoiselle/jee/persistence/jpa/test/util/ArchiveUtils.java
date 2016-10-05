package org.demoiselle.jee.persistence.jpa.test.util;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class ArchiveUtils {

	private ArchiveUtils() {

	}

	public static JavaArchive[] getDeltaSpikeCoreAndJpaArchive() {
		return ShrinkWrapArchiveUtil.getArchives(null, "META-INF/beans.xml",
				new String[] { "org.apache.deltaspike.core", "org.apache.deltaspike.jpa" }, null, "ds-core_and_jpa");
	}

	public static Asset getBeansXml() {
		Asset beansXml = new StringAsset("<beans>" + "<interceptors>"
				+ "<class>org.apache.deltaspike.jpa.impl.transaction.TransactionalInterceptor</class>"
				+ "</interceptors>" + "</beans>");

		return beansXml;
	}
}