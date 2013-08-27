package transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import javax.inject.Inject;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.transaction.Transactional;

public class DDL {

	@Name("conn1")
	@Inject
	private Connection connection;

	@Transactional
	public void dropAndCreate() throws Exception {
		dropTable();
		createTable();
	}

	private void dropTable() throws Exception {

		Statement st = connection.createStatement();
		
		try {
			String sql = "DROP TABLE myentity";
			st.executeUpdate(sql);
			st.close();
		} catch (Exception e) {
			
		}
	}

	private void createTable() throws Exception {
		StringBuffer sql = new StringBuffer();

		sql.append("CREATE TABLE myentity ( ");
		sql.append("	id int NOT NULL, ");
		sql.append("	description varchar(10) NOT NULL, ");
		sql.append("CONSTRAINT myentity_pk PRIMARY KEY (id) ");
		sql.append("); ");

		PreparedStatement pstmt = connection.prepareStatement(sql.toString());
		pstmt.execute();
		pstmt.close();
	}
}
