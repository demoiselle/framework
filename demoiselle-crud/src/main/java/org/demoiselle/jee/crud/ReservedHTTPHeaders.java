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
    HTTP_HEADER_ACCESS_CONTROL_EXPOSE_HEADERS("Access-Control-Expose-Headers"),

    // PageResult metadata
    HTTP_HEADER_TOTAL_COUNT("X-Total-Count"),
    HTTP_HEADER_TOTAL_PAGES("X-Total-Pages"),
    HTTP_HEADER_CURRENT_PAGE("X-Current-Page"),
    HTTP_HEADER_PAGE_SIZE("X-Page-Size"),
    HTTP_HEADER_HAS_NEXT("X-Has-Next"),
    HTTP_HEADER_HAS_PREVIOUS("X-Has-Previous"),

    // Link header (RFC 8288)
    HTTP_HEADER_LINK("Link");
    
    private final String key;

    ReservedHTTPHeaders(String key){
        this.key = key;
    }
    
    public String getKey(){
        return this.key;
    }
}
