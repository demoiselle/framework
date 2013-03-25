package br.gov.frameworkdemoiselle.transaction;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class TransactionInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private int counter = 0;

	private boolean owner;

	public TransactionInfo() {
		clear();
	}

	public void clear() {
		this.owner = false;
		this.counter = 0;
	}

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
