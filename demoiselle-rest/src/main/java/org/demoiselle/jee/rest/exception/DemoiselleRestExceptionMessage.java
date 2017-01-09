/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception;

/**
 * Message Exception class intended to be used by REST DemoiselleFramework
 * exceptions.
 * 
 * @author SERPRO
 */
public class DemoiselleRestExceptionMessage {

	private String error;
	private String error_description;

	private String error_link;

	public DemoiselleRestExceptionMessage(String error, String error_description, String error_link) {
		this.error = error;
		this.error_description = error_description;
		this.error_link = error_link;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getError_description() {
		return error_description;
	}

	public void setError_description(String error_description) {
		this.error_description = error_description;
	}

	public String getError_link() {
		return error_link;
	}

	public void setError_link(String error_link) {
		this.error_link = error_link;
	}

}