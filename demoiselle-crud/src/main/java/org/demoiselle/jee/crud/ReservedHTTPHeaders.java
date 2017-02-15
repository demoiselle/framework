/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

/**
 * Hold the reserved HTTP Headers.
 * 
 * @author SERPRO
 *
 */
public enum ReservedHTTPHeaders {
    
    // Pagination
    HTTP_HEADER_CONTENT_RANGE("Content-Range"),
    HTTP_HEADER_ACCEPT_RANGE("Accept-Range"),
    HTTP_HEADER_ACCESS_CONTROL_EXPOSE_HEADERS("Access-Control-Expose-Headers");
    
    private final String key;

    ReservedHTTPHeaders(String key){
        this.key = key;
    }
    
    public String getKey(){
        return this.key;
    }
}
