
import java.sql.*;

public class PropertyDAO {

    // =========================
    // ADD PROPERTY (FINAL)
    // =========================
    public static void addProperty(String title, String location, double price,
                                   String imagePath, String type,
                                   String contact, String status) {

        try {
            Connection con = DBConnection.connect();

            if (con == null) {
                System.out.println("No DB Connection!");
                return;
            }

            String sql = "INSERT INTO properties(title, location, price, image_path, type, contact, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, title);
            pst.setString(2, location);
            pst.setDouble(3, price);
            pst.setString(4, imagePath);
            pst.setString(5, type);
            pst.setString(6, contact);
            pst.setString(7, status);

            pst.executeUpdate();

            System.out.println("Property Added Successfully!");

            con.close();

        } catch (Exception e) {
            System.out.println("Insert Error: " + e);
        }
    }

    // =========================
    // VIEW ALL PROPERTIES
    // =========================
    public static void viewProperties() {

        try {
            Connection con = DBConnection.connect();

            if (con == null) {
                System.out.println("No DB Connection!");
                return;
            }

            String sql = "SELECT * FROM properties";
            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

                String[] images = rs.getString("image_path").split(";");

                System.out.println(
                    rs.getInt("id") + " | " +
                    rs.getString("title") + " | " +
                    rs.getString("location") + " | " +
                    rs.getString("type") + " | " +
                    rs.getString("status") + " | " +
                    rs.getDouble("price") + " | " +
                    rs.getString("contact") + " | " +
                    images[0]
                );
            }

            con.close();

        } catch (Exception e) {
            System.out.println("Fetch Error: " + e);
        }
    }

    // =========================
    // SEARCH PROPERTIES (FINAL)
    // =========================
    public static void searchProperties(String location, String type, String status,
                                        double minPrice, double maxPrice) {

        try {
            Connection con = DBConnection.connect();

            if (con == null) {
                System.out.println("No DB Connection!");
                return;
            }

            String sql = "SELECT * FROM properties WHERE location=? AND type=? AND status=? AND price BETWEEN ? AND ?";

            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, location);
            pst.setString(2, type);
            pst.setString(3, status);
            pst.setDouble(4, minPrice);
            pst.setDouble(5, maxPrice);

            ResultSet rs = pst.executeQuery();

            boolean found = false;

            while (rs.next()) {
                found = true;

                String[] images = rs.getString("image_path").split(";");

                System.out.println(
                    rs.getInt("id") + " | " +
                    rs.getString("title") + " | " +
                    rs.getString("location") + " | " +
                    rs.getString("type") + " | " +
                    rs.getString("status") + " | " +
                    rs.getDouble("price") + " | " +
                    rs.getString("contact") + " | " +
                    images[0]
                );
            }

            if (!found) {
                System.out.println("No property / advertisement available");
            }

            con.close();

        } catch (Exception e) {
            System.out.println("Search Error: " + e);
        }
    }
}