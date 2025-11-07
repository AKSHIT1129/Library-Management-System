public class Main {
    
}
import java.sql.*;

static class Library {
    private Connection conn;

    Library() {
        try {
            // Load driver explicitly if needed, depends on your setup
            // Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/librarydb?useSSL=false&serverTimezone=UTC",
                    "root", "your_password");

            // Create books table if not exists
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS books (
                    id INT PRIMARY KEY,
                    title VARCHAR(100),
                    author VARCHAR(100),
                    isIssued BOOLEAN DEFAULT FALSE
                )
            """);
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    void addBook(Book b) {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO books (id, title, author, isIssued) VALUES (?, ?, ?, FALSE)")) {
            ps.setInt(1, b.id);
            ps.setString(2, b.title);
            ps.setString(3, b.author);
            ps.executeUpdate();
        } catch (SQLException e) {
            showError("Error adding book: " + e.getMessage());
        }
    }

    String showAllBooks() {
        StringBuilder sb = new StringBuilder("--- Library Books --- \n\n");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {

            if (!rs.isBeforeFirst()) return "No books available.";

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                boolean isIssued = rs.getBoolean("isIssued");

                sb.append(id).append(" | ").append(title).append(" by ").append(author)
                  .append(isIssued ? " (Issued)" : " (Available)").append("\n");
            }
        } catch (SQLException e) {
            return "Error retrieving books: " + e.getMessage();
        }
        return sb.toString();
    }

    String issueBook(int id) {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE books SET isIssued = TRUE WHERE id = ? AND isIssued = FALSE")) {
            ps.setInt(1, id);
            int updated = ps.executeUpdate();
            return updated > 0 ? " Book issued successfully!" : " Book already issued or not found!";
        } catch (SQLException e) {
            return "Error issuing book: " + e.getMessage();
        }
    }

    String returnBook(int id) {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE books SET isIssued = FALSE WHERE id = ? AND isIssued = TRUE")) {
            ps.setInt(1, id);
            int updated = ps.executeUpdate();
            return updated > 0 ? " Book returned successfully!" : " Book not issued or not found!";
        } catch (SQLException e) {
            return "Error returning book: " + e.getMessage();
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
