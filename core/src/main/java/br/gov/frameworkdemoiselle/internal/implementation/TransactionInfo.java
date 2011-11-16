package br.gov.frameworkdemoiselle.internal.implementation;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;

//TODO Inter [NQ]: considerar a necessidade da criação de um contexto de transação para manter a coerência com as demais funcionalidades que possuem um contexto.
//Resposta: não foi feito porque só foi identificado uma funcionalidade (getCurrentTransaction). Aguardar novas ideias.
@RequestScoped
public class TransactionInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private int counter = 0;

	private boolean owner = false;

	public int getCounter() {
		return counter;
	}

	public void incrementCounter() {
		this.counter++;
	}

	public void decrementCounter() {
		this.counter--;
	}

	public void markAsOwner() {
		this.owner = true;
	}

	public boolean isOwner() {
		return owner;
	}
}
