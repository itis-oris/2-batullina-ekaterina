package org.example.client.gui;

import org.example.client.Client;
import org.example.server.DBHelper;

import javax.swing.*;
import java.awt.*;

public class LoginFrame {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JTextArea infoArea;

    public LoginFrame() {
        frame = new JFrame("Login Screen");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2, 10, 10));
        panel.setBackground(new Color(245, 245, 245));

        JLabel titleLabel = new JLabel("Login", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLACK);
        panel.add(titleLabel);
        panel.add(new JLabel(""));

        JLabel usernameLabel = new JLabel("Username:", JLabel.CENTER);
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameField.setBackground(Color.WHITE);
        usernameField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:", JLabel.CENTER);
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setBackground(Color.WHITE);
        passwordField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.add(passwordField);

        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setBackground(new Color(0, 123, 255));
        loginButton.setForeground(Color.BLACK);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 204)));
        loginButton.setPreferredSize(new Dimension(180, 40));
        loginButton.addActionListener(e -> handleLogin());

        registerButton = new JButton("Register");
        registerButton.setFont(new Font("Arial", Font.BOLD, 16));
        registerButton.setBackground(new Color(40, 167, 69));
        registerButton.setForeground(Color.BLACK);
        registerButton.setFocusPainted(false);
        registerButton.setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34)));
        registerButton.setPreferredSize(new Dimension(180, 40));
        registerButton.addActionListener(e -> handleRegistration());

        panel.add(loginButton);
        panel.add(registerButton);

        frame.add(panel, BorderLayout.CENTER);

        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setBackground(new Color(245, 245, 245));
        infoArea.setFont(new Font("Arial", Font.PLAIN, 14));
        infoArea.setForeground(Color.DARK_GRAY);
        frame.add(new JScrollPane(infoArea), BorderLayout.SOUTH);

        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (DBHelper.checkUserCredentials(username, password)) {
            frame.dispose();
            new Client(username);
        } else {
            infoArea.setForeground(Color.RED);
            infoArea.setText("Invalid username or password.");
        }
    }

    private void handleRegistration() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (DBHelper.registerUser(username, password)) {
            infoArea.setForeground(new Color(34, 139, 34));
            infoArea.setText("Registration successful! You can now log in.");
        } else {
            infoArea.setForeground(Color.RED);
            infoArea.setText("Registration failed. Username may already be taken.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
