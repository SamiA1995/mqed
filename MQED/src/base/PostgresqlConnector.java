package base;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

public class PostgresqlConnector {
	public static ArrayList<String> runSelectQueries(ArrayList<String> queries) throws SQLException {
		String url = "jdbc:postgresql://localhost:5432/mqed";
		Properties props = new Properties();
		props.setProperty("user", "postgres");
		props.setProperty("password", "password");
		Connection conn = DriverManager.getConnection(url, props);
		ArrayList<String> result_list = new ArrayList();
		Statement st = conn.createStatement();
		for(int i = 0; i < queries.size(); i++) {
			ResultSet rs = st.executeQuery(queries.get(i));
			String result = "";
			while(rs.next()) {
				result = rs.getString(1);
				result_list.add(result);
			}
			rs.close();
		}
		st.close();
		conn.close();
		return result_list;
	}
	
	public static void runQueries(ArrayList<String> queries) throws SQLException {
		String url = "jdbc:postgresql://localhost:5432/mqed";
		Properties props = new Properties();
		props.setProperty("user", "postgres");
		props.setProperty("password", "password");
		Connection conn = DriverManager.getConnection(url, props);
		
		Statement st = conn.createStatement();
		for(int i = 0; i < queries.size(); i++) {
			st.execute(queries.get(i));
		}
		st.close();
		conn.close();
	}
	
	public static void exampleSelectQueries() throws SQLException {
		ArrayList<String> messages = new ArrayList();
		messages.add("SELECT * FROM object WHERE name LIKE '%1'");
		messages.add("SELECT * FROM object WHERE name LIKE '%3'");
		ArrayList<String> messages_result = runSelectQueries(messages);
		for(int i = 0; i < messages_result.size(); i++) {
			System.out.println(messages_result.get(i));
		}
	}
}