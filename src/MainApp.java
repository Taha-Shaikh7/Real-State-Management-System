import java.sql.Connection;

public class MainApp {

    public static void main(String[] args) {
        new login().setVisible(true);

        Connection con = DBConnection.connect();

        if (con != null) {
            System.out.println("SUCCESS: Connected to Database");
        } else {
            System.out.println("FAILED: Not Connected");
        }

        // NOTHING ELSE HERE
    }
    
    
}