package br.gov.frameworkdemoiselle.management.annotation;

import javax.management.MBeanOperationInfo;


/**
 * Define the operation type for an operation inside a Managed class.
 * 
 * 
 * @author SERPRO
 *
 */
public enum OperationType {
	
	/**
	 * Operation is write-only
	 */
	ACTION(MBeanOperationInfo.ACTION)
	,
	/**
	 * Operation is read-only
	 */
	INFO(MBeanOperationInfo.INFO)
	,
	/**
	 * Operation is read-write
	 */
	ACTION_INFO(MBeanOperationInfo.ACTION_INFO)
	,
	/**
	 * Operation is unkown
	 */
	UNKNOWN(MBeanOperationInfo.UNKNOWN);
	
	private int operationTypeValue;
	
	private OperationType(int type){
		this.operationTypeValue = type;
	}
	
	public int getValue(){
		return operationTypeValue;
	}
	
}
