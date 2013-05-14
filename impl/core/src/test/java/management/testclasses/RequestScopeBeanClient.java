package management.testclasses;

import br.gov.frameworkdemoiselle.util.Beans;


public class RequestScopeBeanClient {
	
	public void operationOne(){
		
		RequestScopedClass bean = Beans.getReference(RequestScopedClass.class);
		bean.setInfo( bean.getInfo() + "-OPERATION ONE CALLED-");
		
	}
	
	public void operationTwo(){
		
		RequestScopedClass bean = Beans.getReference(RequestScopedClass.class);
		bean.setInfo( bean.getInfo() + "-OPERATION TWO CALLED-");
		
	}

}
