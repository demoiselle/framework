package br.gov.frameworkdemoiselle.internal.context;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.inject.spi.PassivationCapable;


@SuppressWarnings("rawtypes")
public class ContextualStore implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final String PREFIX = ContextualStore.class.getCanonicalName() + "#";
	
	private final AtomicInteger idGenerator = new AtomicInteger();
	
	/*
	 * BASEADO NA IMPLEMENTAÇÃO DE CDI DO WELD
	 * 
	 * Contextuals (instâncias de Bean) implementam a interface PassivationCapable quando são capazes
	 * de participar em um cluster de servidores, dessa forma o mesmo Bean pode ser usado para criar instâncias
	 * em todos os servidores e o ciclo de vida é síncrono
	 * 
	 * Se o contextual não implementar PassivationCapable esse suporte não é possível. Por isso o WELD armazena
	 * separadamente contextuals que implementam PassivationCapable e que não implementam.
	 * 
	 * A implementação abaixo foi baseada nesse comportamento, afim de evitar problemas em aplicações empacotadas em EAR
	 * e servidores de aplicação em cluster.
	 * 
	 */
	private final ConcurrentMap<String, Contextual> idToContextual = new ConcurrentHashMap<String, Contextual>();
	private final ConcurrentMap<Contextual, String> contextualToId = new ConcurrentHashMap<Contextual, String>();
	private final ConcurrentMap<String, Contextual> passivableIdToContextual = new ConcurrentHashMap<String, Contextual>();
	
	/**
	 * Return an unique ID for the given contextual. If it's the first time
	 * this contextual is accessed, registers the contextual for latter retrieval. 
	 * 
	 * @param contextual The contextual to generate an ID
	 * @return The unique ID for the contextual
	 */
	public String putIfAbsentAndGetId(Contextual contextual){
		String returnedId;
		
		if (contextual instanceof PassivationCapable){
			returnedId = ((PassivationCapable)contextual).getId();
			passivableIdToContextual.putIfAbsent(returnedId, contextual);
		}
		else{
			returnedId = contextualToId.get(contextual);
			if (returnedId==null){
				synchronized (contextual) {
					//Esse código é uma implementação de performance. Se já existia
					//ID para um contextual, retornamos. Do contrário precisamos fazer uma operação threadsafe
					//que será cara. Então separamos a possibilide do ID já existir (a mais comum) fora do bloco
					//synchronized, mas se precisarmos entrar no bloco precisamos perguntar pelo ID
					//novamente, caso outra thread tenha criado o ID entre a primeira pergunta e a geração do ID.
					returnedId = contextualToId.get(contextual);
					if (returnedId==null){
						returnedId = new StringBuffer().append(PREFIX).append(idGenerator.incrementAndGet()).toString();
						idToContextual.put(returnedId, contextual);
						contextualToId.put(contextual, returnedId);
					}
				}
			}
		}
		
		return returnedId;
	}
	
	public Contextual getContextual(String id){
		if (id.startsWith(PREFIX)){
			return idToContextual.get(id);
		}
		else{
			return passivableIdToContextual.get(id);
		}
	}
	
	public void clear(){
		idToContextual.clear();
		contextualToId.clear();
		passivableIdToContextual.clear();
	}

}
