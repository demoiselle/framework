/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ${package}.util;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author 70744416353
 */
public class Util {

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

    /**
     *
     * @param texto
     * @return
     */
    public static String MD5(String texto) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
        }
        BigInteger hash = new BigInteger(1, md.digest(texto.getBytes()));
        return hash.toString(16);
    }

    private Util() {
    }
}
