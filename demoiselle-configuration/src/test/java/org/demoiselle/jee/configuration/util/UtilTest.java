/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.demoiselle.jee.configuration.model.ConfigClassModel;
import org.demoiselle.jee.configuration.model.ConfigEnum;

/**
 * 
 * @author SERPRO
 *
 */
public class UtilTest {

    private static final String DEMOISELLE_CONFIGURATION_TEMP_DIRECTORY = "demoiselle-configuration-test";
    public static final String CONFIG_INTEGER_FIELD = "configInteger";
    public static final String CONFIG_SHORT_FIELD = "configShort";
    public static final String CONFIG_BOOLEAN_FIELD = "configBoolean";
    public static final String CONFIG_BYTE_FIELD = "configByte";
    public static final String CONFIG_CHARACTER_FIELD = "configCharacter";
    public static final String CONFIG_LONG_FIELD = "configLong";
    public static final String CONFIG_DOUBLE_FIELD = "configDouble";
    public static final String CONFIG_FLOAT_FIELD = "configFloat";
    public static final String CONFIG_STRING_FIELD = "configString";
    public static final String CONFIG_INT_FIELD = "configInt";
    public static final String CONFIG_ENUM_FIELD = "configEnum";
    public static final String CONFIG_CLASS_TYPED_FIELD = "configClassTyped";

    public static final String CONFIG_STRING_FIELD_WITH_NAME_ANNOTATION = "configStringWithName";

    public static final String CONFIG_MAP_FIELD = "configMap";
    public static final String CONFIG_MAP_FIELD_IP = CONFIG_MAP_FIELD + ".ip";
    public static final String CONFIG_MAP_FIELD_PROTOCOL = CONFIG_MAP_FIELD + ".protocol";
    public static final String CONFIG_MAP_FIELD_PORT = CONFIG_MAP_FIELD + ".port";
    
    public static final String CONFIG_MAP_FIELD_WITH_HIFEN = "demoiselle.configuration." + CONFIG_MAP_FIELD + ".key-with-hifen";

    public static final String CONFIG_STRING_FIELD_WITH_IGNORE_ANNOTATION = "configFieldWithIgnore";

    public static final String CONFIG_STRING_SUPPRESS_LOGGER_ANNOTATION_FIELD = "configFieldWithSuppressLogger";

    public static final String CONFIG_ARRAY_FIELD = "configArray";

    public static final Integer CONFIG_INTEGER_VALUE = Integer.MAX_VALUE;
    public static final Short CONFIG_SHORT_VALUE = Short.MAX_VALUE;
    public static final Boolean CONFIG_BOOLEAN_VALUE = Boolean.TRUE;
    public static final Byte CONFIG_BYTE_VALUE = Byte.MAX_VALUE;
    public static final Character CONFIG_CHARACTER_VALUE = new Character('a');
    public static final Long CONFIG_LONG_VALUE = Long.MAX_VALUE;
    public static final Double CONFIG_DOUBLE_VALUE = Double.MAX_VALUE;
    public static final Float CONFIG_FLOAT_VALUE = Float.MAX_VALUE;
    public static final String CONFIG_STRING_VALUE = "Test of String";
    public static final int CONFIG_INT_VALUE = 7182;
    public static final String CONFIG_MAP_VALUE_IP = "10.10.10.10";
    public static final String CONFIG_MAP_VALUE_PROTOCOL = "http";
    public static final String CONFIG_MAP_VALUE_PORT = "578";
    public static final String CONFIG_MAP_VALUE_WITH_HIFEN = "1; othervalue=12, 123";
    public static final Map<String, String> CONFIG_MAP_VALUE = new HashMap<>();
    public static final ConfigEnum CONFIG_ENUM_VALUE = ConfigEnum.ENUM2;
    public static final String CONFIG_STRING_NAME_ANNOTATION_FIELD = "config-name-with-name";
    public static final String CONFIG_STRING_SUPPRESS_LOGGER_ANNOTATION_VALUE = "should not printed";

    public static final String[] CONFIG_ARRAY_VALUE = new String[] {};
    public static final String CONFIG_ARRAY_VALUE_1 = "1";
    public static final String CONFIG_ARRAY_VALUE_2 = "2";
    public static final String CONFIG_ARRAY_VALUE_3 = "3";

    public static final Class<?> CONFIG_CLASS_TYPED_VALUE = ConfigClassModel.class;
    public static final String CONFIG_STRING_NAME_ANNOTATION_VALUE = "String annotated with @Name Annotation";

    public static final String CONFIG_STRING_IGNORE_ANNOTATION_VALUE = "String annotated with @Ignore Annotation";

    private Path directoryTemp;

    public Configuration buildConfiguration(Class<? extends FileBasedConfiguration> configurationType,
            final String file) throws ConfigurationException, MalformedURLException {
        BasicConfigurationBuilder<? extends Configuration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
                configurationType);
        Parameters params = new Parameters();

        ((FileBasedConfigurationBuilder<?>) builder)
                .configure(params.fileBased().setURL(new File(file).toURI().toURL()));

        Configuration configuration = builder.getConfiguration();
        return configuration;
    }

    public String createPropertiesFile(String prefix) throws IOException {
        File file = createTempFile(prefix, ".properties");

        String path = file.getAbsolutePath();

        /*
         * 
         * Properties doesn't accept duplicate key, so I need test Array
         * Extractor.
         * 
         * Properties properties = new Properties();
         * 
         * properties.put(CONFIG_INTEGER_FIELD,
         * String.valueOf(CONFIG_INTEGER_VALUE));
         * properties.put(CONFIG_SHORT_FIELD,
         * String.valueOf(CONFIG_SHORT_VALUE));
         * properties.put(CONFIG_BOOLEAN_FIELD,
         * String.valueOf(CONFIG_BOOLEAN_VALUE));
         * properties.put(CONFIG_BYTE_FIELD, String.valueOf(CONFIG_BYTE_VALUE));
         * properties.put(CONFIG_CHARACTER_FIELD,
         * String.valueOf(CONFIG_CHARACTER_VALUE));
         * properties.put(CONFIG_LONG_FIELD, String.valueOf(CONFIG_LONG_VALUE));
         * properties.put(CONFIG_DOUBLE_FIELD,
         * String.valueOf(CONFIG_DOUBLE_VALUE));
         * properties.put(CONFIG_FLOAT_FIELD,
         * String.valueOf(CONFIG_FLOAT_VALUE));
         * properties.put(CONFIG_STRING_FIELD, CONFIG_STRING_VALUE);
         * properties.put(CONFIG_INT_FIELD, String.valueOf(CONFIG_INT_VALUE));
         * 
         * properties.put(CONFIG_MAP_FIELD_IP, CONFIG_MAP_VALUE_IP);
         * properties.put(CONFIG_MAP_FIELD_PROTOCOL, CONFIG_MAP_VALUE_PROTOCOL);
         * properties.put(CONFIG_MAP_FIELD_PORT, CONFIG_MAP_VALUE_PORT);
         * 
         * properties.put(CONFIG_ENUM_FIELD, CONFIG_ENUM_VALUE.name());
         * 
         * properties.put(CONFIG_ARRAY_FIELD, CONFIG_ARRAY_VALUE_1);
         * properties.put(CONFIG_ARRAY_FIELD, CONFIG_ARRAY_VALUE_2);
         * properties.put(CONFIG_ARRAY_FIELD, CONFIG_ARRAY_VALUE_3);
         * 
         * properties.put(CONFIG_STRING_NAME_ANNOTATION, CONFIG_STRING_VALUE);
         * properties.store(new FileOutputStream(file), "Test");
         */

        StringBuffer sb = new StringBuffer();

        sb.append(CONFIG_INTEGER_FIELD).append("=").append(String.valueOf(CONFIG_INTEGER_VALUE)).append("\n");
        sb.append(CONFIG_SHORT_FIELD).append("=").append(String.valueOf(CONFIG_SHORT_VALUE)).append("\n");
        sb.append(CONFIG_BOOLEAN_FIELD).append("=").append(String.valueOf(CONFIG_BOOLEAN_VALUE)).append("\n");
        sb.append(CONFIG_BYTE_FIELD).append("=").append(String.valueOf(CONFIG_BYTE_VALUE)).append("\n");
        sb.append(CONFIG_CHARACTER_FIELD).append("=").append(String.valueOf(CONFIG_CHARACTER_VALUE)).append("\n");
        sb.append(CONFIG_LONG_FIELD).append("=").append(String.valueOf(CONFIG_LONG_VALUE)).append("\n");
        sb.append(CONFIG_DOUBLE_FIELD).append("=").append(String.valueOf(CONFIG_DOUBLE_VALUE)).append("\n");
        sb.append(CONFIG_FLOAT_FIELD).append("=").append(String.valueOf(CONFIG_FLOAT_VALUE)).append("\n");
        sb.append(CONFIG_STRING_FIELD).append("=").append(CONFIG_STRING_VALUE).append("\n");
        sb.append(CONFIG_INT_FIELD).append("=").append(String.valueOf(CONFIG_INT_VALUE)).append("\n");

        sb.append(CONFIG_MAP_FIELD_IP).append("=").append(CONFIG_MAP_VALUE_IP).append("\n");
        sb.append(CONFIG_MAP_FIELD_PROTOCOL).append("=").append(CONFIG_MAP_VALUE_PROTOCOL).append("\n");
        sb.append(CONFIG_MAP_FIELD_PORT).append("=").append(CONFIG_MAP_VALUE_PORT).append("\n");
        
        sb.append(CONFIG_MAP_FIELD_WITH_HIFEN).append("=").append(CONFIG_MAP_VALUE_WITH_HIFEN).append("\n");

        sb.append(CONFIG_ENUM_FIELD).append("=").append(CONFIG_ENUM_VALUE.name()).append("\n");

        sb.append(CONFIG_ARRAY_FIELD).append("=").append(CONFIG_ARRAY_VALUE_1).append("\n");
        sb.append(CONFIG_ARRAY_FIELD).append("=").append(CONFIG_ARRAY_VALUE_2).append("\n");
        sb.append(CONFIG_ARRAY_FIELD).append("=").append(CONFIG_ARRAY_VALUE_3).append("\n");

        sb.append(CONFIG_STRING_NAME_ANNOTATION_FIELD).append("=").append(CONFIG_STRING_NAME_ANNOTATION_VALUE)
                .append("\n");

        sb.append(CONFIG_CLASS_TYPED_FIELD).append("=").append(CONFIG_CLASS_TYPED_VALUE.getCanonicalName())
                .append("\n");

        sb.append(CONFIG_STRING_FIELD_WITH_IGNORE_ANNOTATION).append("=").append(CONFIG_STRING_IGNORE_ANNOTATION_VALUE)
                .append("\n");

        sb.append(CONFIG_STRING_SUPPRESS_LOGGER_ANNOTATION_FIELD).append("=")
                .append(CONFIG_STRING_SUPPRESS_LOGGER_ANNOTATION_VALUE).append("\n");

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(sb.toString().getBytes());
        fos.close();

        return path;
    }

    private File createTempFile(String prefix, String suffix) throws IOException {
        this.directoryTemp = Files.createTempDirectory(DEMOISELLE_CONFIGURATION_TEMP_DIRECTORY);
        return Files.createTempFile(this.directoryTemp, prefix, suffix).toFile();
    }

    public String createXMLFile(String prefix) throws FileNotFoundException, IOException {
        StringBuilder sb = new StringBuilder();

        String keyMapIp = CONFIG_MAP_FIELD_IP.replaceAll(CONFIG_MAP_FIELD + ".", "");
        String keyMapPort = CONFIG_MAP_FIELD_PORT.replaceAll(CONFIG_MAP_FIELD + ".", "");
        String keyMapProtocol = CONFIG_MAP_FIELD_PROTOCOL.replaceAll(CONFIG_MAP_FIELD + ".", "");
        String keyWithHifen = CONFIG_MAP_FIELD_WITH_HIFEN.substring(CONFIG_MAP_FIELD_WITH_HIFEN.lastIndexOf(".") + 1, CONFIG_MAP_FIELD_WITH_HIFEN.length());

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<configuration>");
        sb.append("	<").append(CONFIG_STRING_FIELD).append(">").append(CONFIG_STRING_VALUE).append("</").append(CONFIG_STRING_FIELD).append(">");
        sb.append("	<").append(CONFIG_INTEGER_FIELD).append(">").append(CONFIG_INTEGER_VALUE).append("</").append(CONFIG_INTEGER_FIELD).append(">");
        sb.append("	<").append(CONFIG_SHORT_FIELD).append(">").append(CONFIG_SHORT_VALUE).append("</").append(CONFIG_SHORT_FIELD).append(">");
        sb.append("	<").append(CONFIG_LONG_FIELD).append(">").append(CONFIG_LONG_VALUE).append("</").append(CONFIG_LONG_FIELD).append(">");
        sb.append("	<").append(CONFIG_FLOAT_FIELD).append(">").append(CONFIG_FLOAT_VALUE).append("</").append(CONFIG_FLOAT_FIELD).append(">");
        sb.append("	<").append(CONFIG_DOUBLE_FIELD).append(">").append(CONFIG_DOUBLE_VALUE).append("</").append(CONFIG_DOUBLE_FIELD).append(">");
        sb.append("	<").append(CONFIG_BOOLEAN_FIELD).append(">").append(CONFIG_BOOLEAN_VALUE).append("</").append(CONFIG_BOOLEAN_FIELD).append(">");
        sb.append("	<").append(CONFIG_BYTE_FIELD).append(">").append(CONFIG_BYTE_VALUE).append("</").append(CONFIG_BYTE_FIELD).append(">");
        sb.append("	<").append(CONFIG_CHARACTER_FIELD).append(">").append(CONFIG_CHARACTER_VALUE).append("</").append(CONFIG_CHARACTER_FIELD).append(">");
        sb.append("	<").append(CONFIG_CLASS_TYPED_FIELD).append(">").append(CONFIG_CLASS_TYPED_VALUE.getCanonicalName()).append("</").append(CONFIG_CLASS_TYPED_FIELD).append(">");
        sb.append("	<").append(CONFIG_ENUM_FIELD).append(">").append(CONFIG_ENUM_VALUE).append("</").append(CONFIG_ENUM_FIELD).append(">");
        sb.append("	");
        sb.append("	<").append(CONFIG_MAP_FIELD).append(">");
        sb.append("	  <").append(keyMapIp).append(">").append(CONFIG_MAP_VALUE_IP).append("</").append(keyMapIp).append(">");
        sb.append("	  <").append(keyMapPort).append(">").append(CONFIG_MAP_VALUE_PORT).append("</").append(keyMapPort).append(">");
        sb.append("	  <").append(keyMapProtocol).append(">").append(CONFIG_MAP_VALUE_PROTOCOL).append("</").append(keyMapProtocol).append(">");        
        sb.append("	</").append(CONFIG_MAP_FIELD).append(">");
        sb.append("	");
        sb.append("	<").append(CONFIG_ARRAY_FIELD).append(">").append(CONFIG_ARRAY_VALUE_1).append("</").append(CONFIG_ARRAY_FIELD).append(">");
        sb.append("	<").append(CONFIG_ARRAY_FIELD).append(">").append(CONFIG_ARRAY_VALUE_2).append("</").append(CONFIG_ARRAY_FIELD).append(">");
        sb.append("	<").append(CONFIG_ARRAY_FIELD).append(">").append(CONFIG_ARRAY_VALUE_3).append("</").append(CONFIG_ARRAY_FIELD).append(">");
        sb.append("	");
        sb.append(" <").append(CONFIG_STRING_NAME_ANNOTATION_FIELD).append(">").append(CONFIG_STRING_NAME_ANNOTATION_VALUE).append("</").append(CONFIG_STRING_NAME_ANNOTATION_FIELD).append(">");
        sb.append("	<").append(CONFIG_STRING_SUPPRESS_LOGGER_ANNOTATION_FIELD).append(">").append(CONFIG_STRING_SUPPRESS_LOGGER_ANNOTATION_VALUE).append("</") .append(CONFIG_STRING_SUPPRESS_LOGGER_ANNOTATION_FIELD).append(">");
        
        sb.append(" <demoiselle>");
        sb.append("   <configuration>");
        sb.append("     <").append(CONFIG_MAP_FIELD).append(">");
        sb.append("       <").append(keyWithHifen).append(">").append(CONFIG_MAP_VALUE_WITH_HIFEN).append("</").append(keyWithHifen).append(">");
        sb.append("     </").append(CONFIG_MAP_FIELD).append(">");
        sb.append("   </configuration>");
        sb.append(" </demoiselle>");
        
        sb.append("</configuration>");

        File file = createTempFile(prefix, ".xml");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(sb.toString().getBytes());
        fos.close();

        return file.getAbsolutePath();
    }

    public void createSystemVariables() {
        System.setProperty(CONFIG_INTEGER_FIELD, String.valueOf(CONFIG_INTEGER_VALUE));
        System.setProperty(CONFIG_SHORT_FIELD, String.valueOf(CONFIG_SHORT_VALUE));
        System.setProperty(CONFIG_BOOLEAN_FIELD, String.valueOf(CONFIG_BOOLEAN_VALUE));
        System.setProperty(CONFIG_BYTE_FIELD, String.valueOf(CONFIG_BYTE_VALUE));
        System.setProperty(CONFIG_CHARACTER_FIELD, String.valueOf(CONFIG_CHARACTER_VALUE));
        System.setProperty(CONFIG_LONG_FIELD, String.valueOf(CONFIG_LONG_VALUE));
        System.setProperty(CONFIG_DOUBLE_FIELD, String.valueOf(CONFIG_DOUBLE_VALUE));
        System.setProperty(CONFIG_FLOAT_FIELD, String.valueOf(CONFIG_FLOAT_VALUE));
        System.setProperty(CONFIG_STRING_FIELD, CONFIG_STRING_VALUE);
        System.setProperty(CONFIG_INT_FIELD, String.valueOf(CONFIG_INT_VALUE));

        System.setProperty(CONFIG_MAP_FIELD_IP, CONFIG_MAP_VALUE_IP);
        System.setProperty(CONFIG_MAP_FIELD_PORT, CONFIG_MAP_VALUE_PORT);
        System.setProperty(CONFIG_MAP_FIELD_PROTOCOL, CONFIG_MAP_VALUE_PROTOCOL);

        System.setProperty(CONFIG_ENUM_FIELD, CONFIG_ENUM_VALUE.name());

        System.setProperty(CONFIG_CLASS_TYPED_FIELD, CONFIG_CLASS_TYPED_VALUE.getCanonicalName());

        System.setProperty(CONFIG_STRING_NAME_ANNOTATION_FIELD, CONFIG_STRING_NAME_ANNOTATION_VALUE);
        System.setProperty(CONFIG_STRING_SUPPRESS_LOGGER_ANNOTATION_FIELD, CONFIG_STRING_SUPPRESS_LOGGER_ANNOTATION_VALUE);
        
        System.setProperty(CONFIG_MAP_FIELD_WITH_HIFEN, CONFIG_MAP_VALUE_WITH_HIFEN);

    }

    public Path getDirectoryTemp() {
        return directoryTemp;
    }

    public void deleteFilesAfterTest() throws IOException {
        if (getDirectoryTemp() != null) {
            Files.walkFileTree(getDirectoryTemp(), new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

            });
        }
    }

    public String createPropertiesFile(String prefix, Properties properties) throws IOException {

        File file = createTempFile(prefix, ".properties");

        String path = file.getAbsolutePath();

        properties.store(new FileOutputStream(file), "Demoiselle Configuration Test");

        return path;
    }

}
