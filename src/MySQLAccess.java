import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLAccess {
	private String url;
	private String dbName;
	private String driver = "com.mysql.jdbc.Driver";
	private String userName; 
	private String password;
	private Connection conn;
	public void connect(){
		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(url+dbName,userName,password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void setConnectionsDetails(String dbName, String url, String userName, String password){
		this.url = "jdbc:mysql://" + url;
		this.dbName = dbName;
		this.userName = userName;
		this.password = password;
	}
	@SuppressWarnings("finally")
	public boolean queryInsert(String query){
		try{
			Statement st = conn.createStatement();
			int val = st.executeUpdate(query);
			if(val==1){
				System.out.print("Successfully inserted value");
				return true;
			}
		}catch(Exception e){
			return false;
		}finally{
			return false;
		}
		
	}
	@SuppressWarnings("finally")
	public ResultSet querySelect(String query){
		try{
			Statement st = conn.createStatement();
			ResultSet res = st.executeQuery(query);
			return res;
		}catch(Exception e){
			return null;
		}finally{
			return null;
		}
	}
	public void close(){
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
