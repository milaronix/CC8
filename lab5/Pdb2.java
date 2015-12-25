import java.sql.*;
import java.io.*;
import java.sql.Driver;

public class Pdb {

	protected String url = "jdbc:mysql://localhost:3306/cc8";
	protected String username = "root";
	protected String password = "12345";
	protected Connection connection = null;

	public Pdb (){
		//conecta();
	}

	public void consulta (){
		conecta();
	}

	public void conecta (){

		System.out.println("url:"+this.url+ " username:"+this.username+" pass:"+this.password);

		try {
			this.connection = DriverManager.getConnection(this.url, this.username, this.password);
		    System.out.println("Database connected!");
		} catch (SQLException e) {
		    throw new IllegalStateException("Cannot connect the database!", e);
		}

	}
	
}