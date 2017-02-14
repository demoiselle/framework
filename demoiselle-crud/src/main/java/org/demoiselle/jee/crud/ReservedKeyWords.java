/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

/**
 * Hold the reserved keywords.
 * 
 * @author SERPRO
 *
 */
public enum ReservedKeyWords {
    
    // Pagination
    DEFAULT_RANGE_KEY("range"),
    
    // Sort
    DEFAULT_SORT_DESC_KEY("desc"),
    DEFAULT_SORT_KEY("sort"),
    
    // Fields
    DEFAULT_FIELD_KEY("fields");
    
    private final String key;

    ReservedKeyWords(String key){
        this.key = key;
    }
    
    public String getKey(){
        return this.key;
    }
}
