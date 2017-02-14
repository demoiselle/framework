/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.sort;

/**
 * This class helps the Sort feature to hold the type for sort ({@link CrudSort#ASC} or {@link CrudSort#DESC}) 
 * and the field that will be used to sort
 * 
 * @author SERPRO
 */
public class SortModel {
    
    private CrudSort type;
    private String field;
    
    public SortModel(CrudSort type, String field){
        this.type = type;
        this.field = field;
    }

    public CrudSort getType() {
        return type;
    }

    public String getField() {
        return field;
    }

    @Override
    public String toString() {
        return "SortModel [type=" + type + ", field=" + field + "]";
    }

}
