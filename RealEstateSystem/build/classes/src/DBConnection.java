/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Naveed Ahmed
 */
import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection connect() {

        Connection con = null;

        try {
            // Step 1: Load driver (modern Java may auto-handle it, but safe to include)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Step 2: Connect to database
            con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/real_estate_db",
                "root",
                ""
            );

            System.out.println("Database Connected!");

        } catch (Exception e) {
            System.out.println("Connection Error: " + e);
        }

        return con;
    }
}
