import java.sql.*;

public class UserDAO {

    public static boolean createAccount(String username, String password) {

        try {

            Connection con = DBConnection.connect();

            if (con == null) {
                System.out.println("No DB Connection!");
                return false;
            }

            String sql = "INSERT INTO users(username, password) VALUES (?, ?)";

            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, username);
            pst.setString(2, password);

            int rows = pst.executeUpdate();

            if (rows > 0) {
                System.out.println("Account Created Successfully!");
                return true;
            }

        } catch (Exception e) {

            System.out.println("Create Account Error: " + e);
        }

        return false;
    }
}