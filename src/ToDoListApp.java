import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ToDoListApp {

    private Connection connection;
    private JFrame frame;
    private JTable table;
    private DefaultTableModel model;
    private JTextField titleField, descriptionField, priorityField;

    public ToDoListApp() {
        createConnection();
        initializeUI();
        loadData();
    }

    private void createConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:todolist.db");
            Statement stmt = connection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (id INTEGER PRIMARY KEY, title TEXT, description TEXT, priority INTEGER, status INTEGER)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        frame = new JFrame("To-Do List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{"ID", "Title", "Description", "Priority", "Status"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));

        titleField = new JTextField();
        descriptionField = new JTextField();
        priorityField = new JTextField();

        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Description:"));
        panel.add(descriptionField);
        panel.add(new JLabel("Priority:"));
        panel.add(priorityField);

        JButton addButton = new JButton("Add Task");
        addButton.addActionListener(new AddTaskAction());
        panel.add(addButton);

        frame.add(panel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void loadData() {
        try {
            model.setRowCount(0);
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM tasks");
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getInt("priority"), rs.getInt("status")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private class AddTaskAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String title = titleField.getText();
                String description = descriptionField.getText();
                int priority = Integer.parseInt(priorityField.getText());

                PreparedStatement pstmt = connection.prepareStatement("INSERT INTO tasks (title, description, priority, status) VALUES (?, ?, ?, ?)");
                pstmt.setString(1, title);
                pstmt.setString(2, description);
                pstmt.setInt(3, priority);
                pstmt.setInt(4, 0); // статус по умолчанию 0 (незавершена)
                pstmt.executeUpdate();

                loadData();
            } catch (SQLException ex) {
                ex.printStackTrace();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid number for priority.", "Input Error", JOptionPane.ERROR_MESSAGE);

            } finally {
                titleField.setText("");
                descriptionField.setText("");
                priorityField.setText("");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ToDoListApp::new);
    }
}
