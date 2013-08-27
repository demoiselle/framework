package transaction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.inject.Inject;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.transaction.TransactionContext;
import br.gov.frameworkdemoiselle.transaction.Transactional;

public class TransactionalBusiness {

	@Inject
	private MyEntity1 m1;
	
	@Inject
	@Name("conn1")
	private Connection conn1;
	
	@Inject
	private TransactionContext transactionContext;
	
	@Transactional
	public boolean isTransactionActiveWithInterceptor(){
		return transactionContext.getCurrentTransaction().isActive();
	}
	
	public boolean isTransactionActiveWithoutInterceptor(){
		return transactionContext.getCurrentTransaction().isActive();
	}
	
	@Transactional
	public void insert() throws Exception {
		String sql = "insert into myentity (id, description) values (1, 'Entidade 1')";
		Statement st = conn1.createStatement();
		st.executeUpdate(sql);
		st.close();
	}
	
	@Transactional
	public void delete() throws Exception {
		String sql = "delete from myentity where id = 1";
		Statement st = conn1.createStatement();
		st.executeUpdate(sql);
		st.close();
	}
	
	@Transactional
	public MyEntity1 find(int id) throws Exception {
		String sql = "select * from myentity where id = " + id; 
		Statement st = conn1.createStatement();
		ResultSet rs = st.executeQuery(sql);
		rs.next();
		m1.setId(rs.getInt(0));
		m1.setDescription(rs.getString(1));
		rs.close();
		st.close();
		return m1;
	}	
	
}
