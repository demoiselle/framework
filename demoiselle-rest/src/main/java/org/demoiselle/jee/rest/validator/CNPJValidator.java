/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.rest.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.demoiselle.jee.rest.annotation.CNPJ;

/**
 *
 * @author 70744416353
 */
public class CNPJValidator implements ConstraintValidator<CNPJ, String> {

    private CNPJ constraint;

    String FORMATED = "(\\d{2})\\.(\\d{3})\\.(\\d{3})/(\\d{4})-(\\d{2})";

    String UNFORMATED = "(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})";

    @Override
    public void initialize(CNPJ constraint) {
        this.constraint = constraint;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isEmpty()) {
            return true;
        }

        if (constraint.formatted()) {
            if (!value.matches(FORMATED)) {
                return false;
            }
            value = onlyDigits(value);

        } else if (!value.matches(UNFORMATED)) {
            return false;
        }

        final boolean result = value.length() == 11 && isValidCnpj(value);

        return result;
    }

    private boolean isValidCnpj(String value) {

        if (allDigitsAreEquals(value)) {
            return false;
        }

        int[] found = {0, 0};
        int d1 = Integer.parseInt(value.substring(value.length() - 2, value.length() - 1));
        int d2 = Integer.parseInt(value.substring(value.length() - 1, value.length()));
        for (int a = 0; a < 2; a++) {
            int d, sum = 0, c = 2;
            for (int b = value.length() - 3 + a; b >= 0; b--) {
                d = Integer.parseInt(value.substring(b, b + 1));
                sum += d * c++;
                if (value.length() == 14 && c > 9) { // cnpj
                    c = 2;
                }
            }
            found[a] = 11 - sum % 11;
            found[a] = (found[a] > 9) ? 0 : found[a];
        }
        return d1 == found[0] && d2 == found[1];
    }

    private boolean allDigitsAreEquals(String value) {
        for (char c : value.toCharArray()) {
            if (c != value.charAt(0)) {
                return false;
            }
        }
        return true;
    }

    private String onlyDigits(String value) {
        final StringBuilder out = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            if (Character.isDigit(c)) {
                out.append(c);
            }
        }
        return out.toString();
    }
}

