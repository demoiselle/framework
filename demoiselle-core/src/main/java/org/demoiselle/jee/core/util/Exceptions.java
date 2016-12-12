/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.util;

/**
 *Class that offer tow methods that can help with manipulation of throwable exceptions.
 *
 * @author SERPRO
 */
public final class Exceptions {

	/**
	 * Constructor without parameters.
	 */
	private Exceptions() {
	}

	/**
	 * Receives as parameter any kind of Throwable objects, and throws a RuntimeException instead.
	 *
	 * @param throwable
	 * 			a throwable object.
	 *
	 * @throws RuntimeException throws this kind of exception every time that is called.
	 */
	public static void handleToRuntimeException(final Throwable throwable) throws RuntimeException {
		if (throwable instanceof RuntimeException) {
			throw (RuntimeException) throwable;
		} else {
			throw new RuntimeException(throwable);
		}
	}
}
