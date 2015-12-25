package lab5;

import java.sql.*;
import java.io.*;
//import com.mysql.jdbc.*;



class Pdb {
	public static void main(String[] args){
		 String url = "jdbc:mysql://localhost:3306/cc8";
		 String username = "root";
		 String password = "12345";
		 Connection connection = null;

		 System.out.println("url:"+url+ " username:"+username+" pass:"+password);

		try {
			connection = DriverManager.getConnection(url, username, password);
		    System.out.println("Database connected!");
		} catch (SQLException e) {
		    throw new IllegalStateException("Cannot connect the database!", e);
		}
	}
}