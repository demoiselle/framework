package br.gov.frameworkdemoiselle.util;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Defines the default behavior of the objects responsible for rendering files to the user.
 * 
 * @author SERPRO
 */
public interface FileRenderer extends Serializable {

	/**
	 * Renders a byteArray for display to the user.
	 * 
	 * @param byteArray Byte Array to be rendered.
	 * @param contentType 
	 * @param fileName
	 */
	void render(final byte[] byteArray, final ContentType contentType, final String fileName);

	/**
	 * Renders an inputStream for display to the user.
	 * 
	 * @param stream
	 * @param contentType
	 * @param fileName
	 */
	void render(final InputStream stream, final ContentType contentType, final String fileName);

	/**
	 * Renders a file for display to the user.
	 * 
	 * @param file
	 * @param contentType
	 * @param fileName
	 */
	void render(final File file, final ContentType contentType, final String fileName);

	/**
	 * File content type.
	 * 
	 * @author SERPRO
	 */
	public enum ContentType {
		CSV("text/plain"),
		HTML("text/html"),
		ODT("application/vnd.oasis.opendocument.text"),
		PDF("application/pdf"),
		RTF("application/rtf"),
		TXT("text/plain"),
		XLS("application/vnd.ms-excel");	
		
		private String contentType;	
		
		/**
		 * Constructor receiving the fields alias and content type.
		 * @param alias Alias of content type
		 * @param contentType Value of content type
		 */
		private ContentType(String contentType){
			this.contentType = contentType;
		}
		
		/**
		 * Return the content type of Type
		 * @return Content Type
		 */
		public String getContentType() {
			return contentType;
		}	
		
	}

}
