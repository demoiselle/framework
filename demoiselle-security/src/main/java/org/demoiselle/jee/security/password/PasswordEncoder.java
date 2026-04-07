/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.password;

/**
 * Interface para codificação e verificação de senhas.
 * <p>
 * Implementações devem gerar hashes seguros com salt aleatório
 * e suportar verificação em tempo constante para evitar timing attacks.
 * </p>
 *
 * <p>Exemplo de uso:</p>
 * <pre>{@code
 * @Inject
 * private PasswordEncoder passwordEncoder;
 *
 * // Gerar hash
 * String hash = passwordEncoder.encode("minhaSenha123");
 *
 * // Verificar
 * boolean ok = passwordEncoder.matches("minhaSenha123", hash);
 * }</pre>
 *
 * @author Demoiselle Framework
 * @since 4.1.0
 */
public interface PasswordEncoder {

    /**
     * Gera um hash seguro da senha fornecida.
     * Cada chamada produz um hash diferente (salt aleatório).
     *
     * @param rawPassword a senha em texto plano
     * @return o hash da senha no formato {@code algorithm:iterations:salt:hash}
     * @throws IllegalArgumentException se rawPassword for null ou vazio
     */
    String encode(CharSequence rawPassword);

    /**
     * Verifica se a senha em texto plano corresponde ao hash armazenado.
     * Usa comparação em tempo constante para evitar timing attacks.
     *
     * @param rawPassword a senha em texto plano a verificar
     * @param encodedPassword o hash armazenado
     * @return {@code true} se a senha corresponde ao hash
     */
    boolean matches(CharSequence rawPassword, String encodedPassword);
}
