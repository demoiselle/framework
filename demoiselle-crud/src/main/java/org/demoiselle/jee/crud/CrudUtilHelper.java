/*
  * Demoiselle Framework
  *
  * License: GNU Lesser General Public License (LGPL), version 3 or later.
  * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.lang.reflect.ParameterizedType;

/**
 * @author SERPRO
 *
 */
public class CrudUtilHelper {

    public static Class<?> getTargetClass(Class<?> targetClass){
        if (targetClass.getSuperclass().equals(AbstractREST.class)) {
            Class<?> type = (Class<?>) ((ParameterizedType) targetClass.getGenericSuperclass()).getActualTypeArguments()[0];
            return type;
        }
        return null;
    }
    
    public static void checkIfExistField(Class<?> targetClass, String field) {
        if(targetClass != null) {
            try{
                targetClass.getDeclaredField(field);
            }
            catch(NoSuchFieldException e){
                throw new IllegalArgumentException(e);
            }
        }
    }

}
