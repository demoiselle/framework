package org.demoiselle.jee.configuration.extractor;

import java.lang.reflect.Field;

import org.apache.commons.configuration2.Configuration;

/**
 * Definição de interface para os Extratores. 
 *
 */
public interface ConfigurationValueExtractor {
	
	/**
	 * Extrai o valor de uma fonte baseado nos parâmetros.
	 * 
	 * @param prefix Prefixo utilizado no arquivo fonte.
	 * @param key Chave utilizado no arquivo fonte.
	 * @param field Field a ser preenchido.
	 * @param configuration Objeto de Configuração responsável por extrair o valor.
	 * @return Objeto com o valor definido na fonte.
	 * @throws Exception Exeção emitida caso ocorra algum erro.
	 */
	Object getValue(String prefix, String key, Field field, Configuration configuration) throws Exception;
	
	/**
	 * Validação do tipo de campo informado é suportado pelo extrator 
	 * 
	 * @param field Campo a ser validado.
	 * @return Verdadeiro se for suportado e Falso se não for suportado
	 */
	boolean isSupported(Field field);
}
