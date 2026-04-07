/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.password;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementação de {@link PasswordEncoder} usando PBKDF2 com HMAC-SHA256.
 * <p>
 * Usa apenas APIs nativas do Java ({@code javax.crypto}), sem dependências externas.
 * O formato do hash é: {@code PBKDF2:iterations:salt:hash} (tudo em Base64).
 * </p>
 *
 * <p>Parâmetros padrão:</p>
 * <ul>
 *   <li>Algoritmo: PBKDF2WithHmacSHA256</li>
 *   <li>Iterações: 210.000 (recomendação OWASP 2024)</li>
 *   <li>Salt: 16 bytes aleatórios (SecureRandom)</li>
 *   <li>Hash: 256 bits (32 bytes)</li>
 * </ul>
 *
 * @author Demoiselle Framework
 * @since 4.1.0
 */
@ApplicationScoped
public class Pbkdf2PasswordEncoder implements PasswordEncoder {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "PBKDF2";
    private static final int DEFAULT_ITERATIONS = 210_000;
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 256;
    private static final String SEPARATOR = ":";

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null || rawPassword.length() == 0) {
            throw new IllegalArgumentException("Password must not be null or empty");
        }

        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);

        byte[] hash = pbkdf2(rawPassword, salt, DEFAULT_ITERATIONS);

        return PREFIX + SEPARATOR
                + DEFAULT_ITERATIONS + SEPARATOR
                + Base64.getEncoder().encodeToString(salt) + SEPARATOR
                + Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        String[] parts = encodedPassword.split(SEPARATOR, 4);
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            byte[] actualHash = pbkdf2(rawPassword, salt, iterations);

            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] pbkdf2(CharSequence password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    toCharArray(password), salt, iterations, HASH_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("PBKDF2 hashing failed", e);
        }
    }

    private char[] toCharArray(CharSequence cs) {
        char[] result = new char[cs.length()];
        for (int i = 0; i < cs.length(); i++) {
            result[i] = cs.charAt(i);
        }
        return result;
    }
}
