/*
  * Demoiselle Framework
  *
  * License: GNU Lesser General Public License (LGPL), version 3 or later.
  * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.sort;

/**
 * @author SERPRO
 *
 */
public class SortModel {
    
    private CrudSort type;
    private String value;
    
    public SortModel(CrudSort type, String value){
        this.type = type;
        this.value = value;
    }

    public CrudSort getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "SortModel [type=" + type + ", value=" + value + "]";
    }

}
