import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Properties;
import javax.swing.JOptionPane;

public class MainApp {
    private JFrame frame;
    private JPanel loginPanel, registrationPanel, dashboardPanel;
    private JTextField loginUsername, regUsername, productSearch, supplierSearch;
    private JPasswordField loginPassword, regPassword;
    private JTable productTable, supplierTable, auditTable;
    private DefaultTableModel productModel, supplierModel, auditModel;
    private Connection conn;
    private String loggedInUser;

    public MainApp() {
        initializeDatabase();
        createUI();
    }

    private void initializeDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/inventory_simple";
            Properties props = new Properties();
            props.load(new java.io.FileInputStream("config.properties"));
            conn = DriverManager.getConnection(url, props);
            initializeTables();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
        }
    }

    private void initializeTables() {
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS products (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, quantity INT NOT NULL DEFAULT 0, price DECIMAL(10,2) NOT NULL, category VARCHAR(50) NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS suppliers (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, contact VARCHAR(100) NOT NULL, product_id INT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS audit_log (id INT AUTO_INCREMENT PRIMARY KEY, action VARCHAR(255) NOT NULL, timestamp VARCHAR(100) NOT NULL, product_id INT, change_amount INT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Initialization Error: " + e.getMessage());
        }
    }

    private void createUI() {
        frame = new JFrame("Inventory System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(title, gbc);

        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        loginPanel.add(usernameLabel, gbc);

        loginUsername = new JTextField(15);
        gbc.gridx = 1;
        loginPanel.add(loginUsername, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0; gbc.gridy = 2;
        loginPanel.add(passwordLabel, gbc);

        loginPassword = new JPasswordField(15);
        gbc.gridx = 1;
        loginPanel.add(loginPassword, gbc);

        JButton loginButton = new JButton("Sign In");
        loginButton.setBackground(new Color(52, 152, 219));
        loginButton.setForeground(Color.WHITE);
        gbc.gridx = 1; gbc.gridy = 3;
        loginPanel.add(loginButton, gbc);

        JButton registerButton = new JButton("Register");
        registerButton.setBackground(new Color(46, 204, 113));
        registerButton.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 3;
        loginPanel.add(registerButton, gbc);

        loginButton.addActionListener(e -> {
            String username = loginUsername.getText().trim();
            String password = new String(loginPassword.getPassword()).trim();
            System.out.println("Attempting login for: " + username);
            if (validateLogin(username, password)) {
                System.out.println("Login successful for: " + username);
                loggedInUser = username;
                frame.remove(loginPanel);
                frame.add(dashboardPanel);
                frame.revalidate();
                frame.repaint();
                refreshTables();
            } else {
                System.out.println("Login failed for: " + username);
                JOptionPane.showMessageDialog(frame, "Invalid credentials!");
            }
        });

        registerButton.addActionListener(e -> {
            frame.remove(loginPanel);
            frame.add(registrationPanel);
            frame.revalidate();
            frame.repaint();
        });

        registrationPanel = new JPanel(new GridBagLayout());
        registrationPanel.setBackground(new Color(245, 247, 250));
        GridBagConstraints rgc = new GridBagConstraints();
        rgc.insets = new Insets(10, 10, 10, 10);

        JLabel regTitle = new JLabel("Create Account");
        regTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        rgc.gridx = 0; rgc.gridy = 0; rgc.gridwidth = 2;
        registrationPanel.add(regTitle, rgc);

        JLabel regUsernameLabel = new JLabel("Username:");
        rgc.gridx = 0; rgc.gridy = 1; rgc.gridwidth = 1;
        registrationPanel.add(regUsernameLabel, rgc);

        regUsername = new JTextField(15);
        rgc.gridx = 1;
        registrationPanel.add(regUsername, rgc);

        JLabel regPasswordLabel = new JLabel("Password:");
        rgc.gridx = 0; rgc.gridy = 2;
        registrationPanel.add(regPasswordLabel, rgc);

        regPassword = new JPasswordField(15);
        rgc.gridx = 1;
        registrationPanel.add(regPassword, rgc);

        JButton submitButton = new JButton("Submit");
        submitButton.setBackground(new Color(46, 204, 113));
        submitButton.setForeground(Color.WHITE);
        rgc.gridx = 1; rgc.gridy = 3;
        registrationPanel.add(submitButton, rgc);

        JButton backButton = new JButton("Back to Login");
        backButton.setBackground(new Color(231, 76, 60));
        backButton.setForeground(Color.WHITE);
        rgc.gridx = 0; rgc.gridy = 3;
        registrationPanel.add(backButton, rgc);

        submitButton.addActionListener(e -> {
            String username = regUsername.getText().trim();
            String password = new String(regPassword.getPassword()).trim();
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Username and password cannot be empty!");
            } else if (username.length() < 4 || password.length() < 6) {
                JOptionPane.showMessageDialog(frame, "Username must be at least 4 characters, password at least 6!");
            } else {
                try {
                    PreparedStatement pstmt = conn.prepareStatement("SELECT username FROM users WHERE username = ?");
                    pstmt.setString(1, username);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(frame, "Username already exists!");
                    } else {
                        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                        pstmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
                        pstmt.setString(1, username);
                        pstmt.setString(2, hashedPassword);
                        pstmt.executeUpdate();
                        JOptionPane.showMessageDialog(frame, "Registration successful! Please log in.");
                        regUsername.setText("");
                        regPassword.setText("");
                        frame.remove(registrationPanel);
                        frame.add(loginPanel);
                        frame.revalidate();
                        frame.repaint();
                    }
                    rs.close();
                    pstmt.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "Registration Error: " + ex.getMessage());
                }
            }
        });

        backButton.addActionListener(e -> {
            frame.remove(registrationPanel);
            frame.add(loginPanel);
            frame.revalidate();
            frame.repaint();
        });

        dashboardPanel = new JPanel(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(250, 250, 252));

        JPanel productPanel = new JPanel(new BorderLayout());
        productSearch = new JTextField(20);
        productSearch.setBorder(BorderFactory.createTitledBorder("Search Products"));
        productPanel.add(productSearch, BorderLayout.NORTH);
        productModel = new DefaultTableModel(new String[]{"ID", "Name", "Quantity", "Price", "Category"}, 0);
        productTable = new JTable(productModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<DefaultTableModel> productSorter = new TableRowSorter<>(productModel);
        productTable.setRowSorter(productSorter);
        productSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(productSorter, productSearch.getText(), 1); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(productSorter, productSearch.getText(), 1); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(productSorter, productSearch.getText(), 1); }
        });
        productPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);
        JPanel productControls = new JPanel(new FlowLayout());
        productControls.add(createButton("Add Product", e -> showProductDialog(null)));
        productControls.add(createButton("Edit Product", e -> editProduct()));
        productControls.add(createButton("Delete Product", e -> deleteProduct()));
        productControls.add(createButton("Manage Stock", e -> manageStock()));
        productControls.add(createButton("Low Stock Report", e -> generateReport()));
        productPanel.add(productControls, BorderLayout.SOUTH);
        tabbedPane.addTab("Products", productPanel);

        JPanel supplierPanel = new JPanel(new BorderLayout());
        supplierSearch = new JTextField(20);
        supplierSearch.setBorder(BorderFactory.createTitledBorder("Search Suppliers"));
        supplierPanel.add(supplierSearch, BorderLayout.NORTH);
        supplierModel = new DefaultTableModel(new String[]{"ID", "Name", "Contact", "Product"}, 0);
        supplierTable = new JTable(supplierModel);
        supplierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<DefaultTableModel> supplierSorter = new TableRowSorter<>(supplierModel);
        supplierTable.setRowSorter(supplierSorter);
        supplierSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(supplierSorter, supplierSearch.getText(), 1); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(supplierSorter, supplierSearch.getText(), 1); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(supplierSorter, supplierSearch.getText(), 1); }
        });
        supplierPanel.add(new JScrollPane(supplierTable), BorderLayout.CENTER);
        JPanel supplierControls = new JPanel(new FlowLayout());
        supplierControls.add(createButton("Add Supplier", e -> showSupplierDialog(null)));
        supplierControls.add(createButton("Edit Supplier", e -> editSupplier()));
        supplierControls.add(createButton("Delete Supplier", e -> deleteSupplier()));
        supplierPanel.add(supplierControls, BorderLayout.SOUTH);
        tabbedPane.addTab("Suppliers", supplierPanel);

        auditModel = new DefaultTableModel(new String[]{"Action", "Timestamp", "Product ID", "Change"}, 0);
        auditTable = new JTable(auditModel);
        auditTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabbedPane.addTab("Audit Log", new JScrollPane(auditTable));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.addActionListener(e -> {
            loggedInUser = null;
            loginUsername.setText("");
            loginPassword.setText("");
            frame.remove(dashboardPanel);
            frame.add(loginPanel);
            frame.revalidate();
            frame.repaint();
        });
        topPanel.add(new JLabel("Welcome, " + (loggedInUser != null ? loggedInUser : "User")));
        topPanel.add(logoutButton);

        dashboardPanel.add(topPanel, BorderLayout.NORTH);
        dashboardPanel.add(tabbedPane, BorderLayout.CENTER);

        frame.add(loginPanel);
        frame.setVisible(true);
    }

    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setBackground(new Color(48, 104, 222));
        button.setForeground(Color.WHITE);
        button.addActionListener(action);
        return button;
    }

    private void filterTable(TableRowSorter<DefaultTableModel> sorter, String text, int column) {
        if (text.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + text, column));
        }
    }

    private boolean validateLogin(String username, String password) {
        System.out.println("Validating login for: " + username + ", password length: " + password.length());
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT password FROM users WHERE username = ?");
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                System.out.println("Stored password hash: " + storedPassword);
                boolean valid = BCrypt.checkpw(password, storedPassword);
                System.out.println("BCrypt check result: " + valid);
                rs.close();
                pstmt.close();
                return valid;
            }
            rs.close();
            pstmt.close();
            System.out.println("No user found for: " + username);
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            JOptionPane.showMessageDialog(frame, "Login Error: " + e.getMessage());
        }
        return false;
    }

    private void refreshTables() {
        productModel.setRowCount(0);
        supplierModel.setRowCount(0);
        auditModel.setRowCount(0);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM products ORDER BY id");
            while (rs.next()) {
                productModel.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getInt("quantity"), rs.getDouble("price"), rs.getString("category")});
            }
            rs = stmt.executeQuery("SELECT s.id, s.name, s.contact, p.name AS product_name FROM suppliers s LEFT JOIN products p ON s.product_id = p.id ORDER BY s.id");
            while (rs.next()) {
                supplierModel.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getString("contact"), rs.getString("product_name")});
            }
            rs = stmt.executeQuery("SELECT action, timestamp, product_id, change_amount FROM audit_log ORDER BY id DESC LIMIT 100");
            while (rs.next()) {
                auditModel.addRow(new Object[]{rs.getString("action"), rs.getString("timestamp"), rs.getInt("product_id"), rs.getInt("change_amount")});
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Refresh Error: " + e.getMessage());
        }
    }

    private void showProductDialog(Vector<Object> product) {
        JTextField name = new JTextField(product != null ? product.get(1).toString() : "");
        JTextField quantity = new JTextField(product != null ? product.get(2).toString() : "");
        JTextField price = new JTextField(product != null ? product.get(3).toString() : "");
        JTextField category = new JTextField(product != null ? product.get(4).toString() : "");
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("Name:")); panel.add(name);
        panel.add(new JLabel("Quantity:")); panel.add(quantity);
        panel.add(new JLabel("Price:")); panel.add(price);
        panel.add(new JLabel("Category:")); panel.add(category);
        int option = JOptionPane.showConfirmDialog(frame, panel, product == null ? "Add Product" : "Edit Product", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String nameText = name.getText().trim();
                int qty = Integer.parseInt(quantity.getText().trim());
                double prc = Double.parseDouble(price.getText().trim());
                String cat = category.getText().trim();
                if (nameText.isEmpty() || qty < 0 || prc < 0 || cat.isEmpty()) throw new Exception("Invalid input: All fields required, quantity and price must be non-negative");
                if (product == null) {
                    PreparedStatement pstmt = conn.prepareStatement("INSERT INTO products (name, quantity, price, category) VALUES (?, ?, ?, ?)");
                    pstmt.setString(1, nameText); pstmt.setInt(2, qty); pstmt.setDouble(3, prc); pstmt.setString(4, cat);
                    pstmt.executeUpdate();
                    pstmt.close();
                    logAction("Product Added: " + nameText, 0, 0);
                } else {
                    PreparedStatement pstmt = conn.prepareStatement("UPDATE products SET name = ?, quantity = ?, price = ?, category = ? WHERE id = ?");
                    pstmt.setString(1, nameText); pstmt.setInt(2, qty); pstmt.setDouble(3, prc); pstmt.setString(4, cat); pstmt.setInt(5, (Integer)product.get(0));
                    pstmt.executeUpdate();
                    pstmt.close();
                    logAction("Product Updated: " + nameText, (Integer)product.get(0), 0);
                }
                refreshTables();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
            }
        }
    }

    private void editProduct() {
        int row = productTable.getSelectedRow();
        if (row >= 0) {
            Vector<Object> productData = new Vector<>();
            productData.add(productModel.getValueAt(row, 0));
            productData.add(productModel.getValueAt(row, 1));
            productData.add(productModel.getValueAt(row, 2));
            productData.add(productModel.getValueAt(row, 3));
            productData.add(productModel.getValueAt(row, 4));
            showProductDialog(productData);
        } else {
            JOptionPane.showMessageDialog(frame, "Select a product to edit.");
        }
    }

    private void deleteProduct() {
        int row = productTable.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this product?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int id = (Integer)productModel.getValueAt(row, 0);
                    String name = (String)productModel.getValueAt(row, 1);
                    PreparedStatement pstmt = conn.prepareStatement("DELETE FROM products WHERE id = ?");
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                    pstmt.close();
                    logAction("Product Deleted: " + name, id, 0);
                    refreshTables();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Select a product to delete.");
        }
    }

    private void manageStock() {
        int row = productTable.getSelectedRow();
        if (row >= 0) {
            JTextField amount = new JTextField(5);
            JRadioButton add = new JRadioButton("Add", true);
            JRadioButton remove = new JRadioButton("Remove");
            ButtonGroup group = new ButtonGroup(); group.add(add); group.add(remove);
            JPanel panel = new JPanel(new GridLayout(3, 1));
            panel.add(new JLabel("Amount:")); panel.add(amount);
            JPanel radioPanel = new JPanel(new FlowLayout());
            radioPanel.add(add); radioPanel.add(remove);
            panel.add(radioPanel);
            int option = JOptionPane.showConfirmDialog(frame, panel, "Manage Stock", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    int qty = Integer.parseInt(amount.getText().trim());
                    if (qty <= 0) throw new Exception("Amount must be positive");
                    int id = (Integer)productModel.getValueAt(row, 0);
                    int currentStock = (Integer)productModel.getValueAt(row, 2);
                    String productName = (String)productModel.getValueAt(row, 1);
                    if (remove.isSelected() && qty > currentStock) throw new Exception("Cannot remove more stock than available. Current stock: " + currentStock);
                    PreparedStatement pstmt = conn.prepareStatement("UPDATE products SET quantity = quantity + ? WHERE id = ?");
                    pstmt.setInt(1, add.isSelected() ? qty : -qty); pstmt.setInt(2, id);
                    pstmt.executeUpdate();
                    pstmt.close();
                    logAction((add.isSelected() ? "Stock Added: " : "Stock Removed: ") + productName, id, add.isSelected() ? qty : -qty);
                    refreshTables();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Select a product to manage stock.");
        }
    }

    private void showSupplierDialog(Vector<Object> supplier) {
        JTextField name = new JTextField(supplier != null ? supplier.get(1).toString() : "");
        JTextField contact = new JTextField(supplier != null ? supplier.get(2).toString() : "");
        JComboBox<String> products = new JComboBox<>(getProductNames());
        if (supplier != null && supplier.get(3) != null) {
            String productName = supplier.get(3).toString();
            for (int i = 0; i < products.getItemCount(); i++) {
                if (products.getItemAt(i).contains(productName)) {
                    products.setSelectedIndex(i);
                    break;
                }
            }
        }
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Name:")); panel.add(name);
        panel.add(new JLabel("Contact:")); panel.add(contact);
        panel.add(new JLabel("Product:")); panel.add(products);
        int option = JOptionPane.showConfirmDialog(frame, panel, supplier == null ? "Add Supplier" : "Edit Supplier", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String nameText = name.getText().trim();
                String contactText = contact.getText().trim();
                String product = (String)products.getSelectedItem();
                int productId = 0;
                if (product != null && product.contains(":")) productId = Integer.parseInt(product.split(":")[0].trim());
                if (nameText.isEmpty() || contactText.isEmpty()) throw new Exception("Name and contact are required");
                if (supplier == null) {
                    PreparedStatement pstmt = conn.prepareStatement("INSERT INTO suppliers (name, contact, product_id) VALUES (?, ?, ?)");
                    pstmt.setString(1, nameText); pstmt.setString(2, contactText); pstmt.setInt(3, productId);
                    pstmt.executeUpdate();
                    pstmt.close();
                    logAction("Supplier Added: " + nameText, productId, 0);
                } else {
                    PreparedStatement pstmt = conn.prepareStatement("UPDATE suppliers SET name = ?, contact = ?, product_id = ? WHERE id = ?");
                    pstmt.setString(1, nameText); pstmt.setString(2, contactText); pstmt.setInt(3, productId); pstmt.setInt(4, (Integer)supplier.get(0));
                    pstmt.executeUpdate();
                    pstmt.close();
                    logAction("Supplier Updated: " + nameText, productId, 0);
                }
                refreshTables();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
            }
        }
    }

    private void editSupplier() {
        int row = supplierTable.getSelectedRow();
        if (row >= 0) {
            Vector<Object> supplierData = new Vector<>();
            supplierData.add(supplierModel.getValueAt(row, 0));
            supplierData.add(supplierModel.getValueAt(row, 1));
            supplierData.add(supplierModel.getValueAt(row, 2));
            supplierData.add(supplierModel.getValueAt(row, 3));
            showSupplierDialog(supplierData);
        } else {
            JOptionPane.showMessageDialog(frame, "Select a supplier to edit.");
        }
    }

    private void deleteSupplier() {
        int row = supplierTable.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this supplier?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int id = (Integer)supplierModel.getValueAt(row, 0);
                    String name = (String)supplierModel.getValueAt(row, 1);
                    PreparedStatement pstmt = conn.prepareStatement("DELETE FROM suppliers WHERE id = ?");
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                    pstmt.close();
                    logAction("Supplier Deleted: " + name, 0, 0);
                    refreshTables();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Select a supplier to delete.");
        }
    }

    private String[] getProductNames() {
        Vector<String> names = new Vector<>();
        names.add("0: None");
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, name FROM products ORDER BY id");
            while (rs.next()) {
                names.add(rs.getInt("id") + ": " + rs.getString("name"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error fetching product names: " + e.getMessage());
        }
        return names.toArray(new String[0]);
    }

    private void logAction(String action, int productId, int changeAmount) {
        try {
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO audit_log (action, timestamp, product_id, change_amount) VALUES (?, ?, ?, ?)");
            pstmt.setString(1, action);
            pstmt.setString(2, new java.util.Date().toString());
            pstmt.setInt(3, productId);
            pstmt.setInt(4, changeAmount);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Log Error: " + e.getMessage());
        }
    }

    private void generateReport() {
        try {
            String threshold = JOptionPane.showInputDialog(frame, "Enter low stock threshold:", "10");
            int thresh = Integer.parseInt(threshold.trim());
            Vector<Object[]> lowStock = new Vector<>();
            PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM products WHERE quantity < ? ORDER BY id");
            pstmt.setInt(1, thresh);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                lowStock.add(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getInt("quantity"), rs.getDouble("price"), rs.getString("category")});
            }
            rs.close();
            pstmt.close();

            if (lowStock.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No products below threshold.");
                return;
            }

            StringBuilder csv = new StringBuilder("ID,Name,Quantity,Price,Category\n");
            StringBuilder chart = new StringBuilder("{\"type\":\"bar\",\"data\":{\"labels\":[");
            StringBuilder data = new StringBuilder("],\"datasets\":[{\"label\":\"Quantity\",\"data\":[");
            boolean first = true;
            for (Object[] row : lowStock) {
                csv.append(String.format("%d,%s,%d,%.2f,%s\n", row[0], row[1], row[2], row[3], row[4]));
                if (!first) { chart.append(","); data.append(","); }
                chart.append("\"").append(row[1]).append("\"");
                data.append(row[2]);
                first = false;
            }
            chart.append(data).append("],\"backgroundColor\":\"#3068DE\",\"borderColor\":\"#2048AE\",\"borderWidth\":1}],\"options\":{\"scales\":{\"y\":{\"beginAtZero\":true,\"title\":{\"display\":true,\"text\":\"Quantity\"}},\"x\":{\"title\":{\"display\":true,\"text\":\"Product\"}}},\"plugins\":{\"title\":{\"display\":true,\"text\":\"Low Stock Products\"}}}}");
            java.nio.file.Files.writeString(java.nio.file.Paths.get("target/low_stock_report.csv"), csv.toString());
            java.nio.file.Files.writeString(java.nio.file.Paths.get("target/low_stock_chart.json"), chart.toString());
            JOptionPane.showMessageDialog(frame, "Report generated in target folder.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Report Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::new);
    }
}