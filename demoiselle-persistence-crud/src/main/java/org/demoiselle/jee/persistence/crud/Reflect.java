/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.persistence.crud;

import java.lang.reflect.Field;

/**
 *
 * @author gladson
 */
public class Reflect {

    /**
     *
     * @param field
     * @param classe
     * @return
     */
    public static boolean fieldInClass(String field, Class<?> classe) {
        Field[] methods = classe.getDeclaredFields();
        for (Field field1 : methods) {
            if (field1.getName().equalsIgnoreCase(field)) {
                return true;
            }
        }
        return false;
    }

    private Reflect() {
    }

}
