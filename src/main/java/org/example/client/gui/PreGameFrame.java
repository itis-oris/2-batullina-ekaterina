package org.example.client.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintWriter;

import static java.lang.System.out;

public class PreGameFrame extends JFrame {
    private final JFrame frame;
    private final String username;
    private final JComboBox<String> difficultyComboBox;
    private final JButton startButton;
    private final JButton helpButton;
    private final PrintWriter out;
    private boolean isReady = false;

    public PreGameFrame(String username, PrintWriter out) {
        this.username = username;
        this.out = out;
        frame = new JFrame("Pre-Game Screen");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Панель для центра
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1, 10, 10));
        panel.setBackground(new Color(245, 245, 245));

        // Заголовок с приветствием
        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.BLACK);
        panel.add(welcomeLabel);

        // Дропдаун для выбора сложности
        String[] difficulties = {"Easy", "Medium", "Hard"};
        difficultyComboBox = new JComboBox<>(difficulties);
        difficultyComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        difficultyComboBox.setBackground(Color.WHITE);
        difficultyComboBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        difficultyComboBox.setPreferredSize(new Dimension(150, 40));
        panel.add(difficultyComboBox);

        // Кнопка помощи
        helpButton = new JButton("Help");
        helpButton.setFont(new Font("Arial", Font.PLAIN, 14));
        helpButton.setBackground(new Color(230, 230, 230));
        helpButton.setForeground(Color.BLACK);
        helpButton.setFocusPainted(false);
        helpButton.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        helpButton.addActionListener(e -> showHelp());

        // Эффект при наведении на кнопку Help
        helpButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                helpButton.setBackground(new Color(210, 210, 210));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                helpButton.setBackground(new Color(230, 230, 230));
            }
        });
        panel.add(helpButton);

        // Кнопка начала игры
        startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.BOLD, 16));
        startButton.setBackground(new Color(0, 123, 255));
        startButton.setForeground(Color.BLACK);
        startButton.setFocusPainted(false);
        startButton.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 204)));
        startButton.setPreferredSize(new Dimension(180, 40));
        startButton.addActionListener(e -> startGame());

        startButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                startButton.setBackground(new Color(0, 102, 204));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                startButton.setBackground(new Color(0, 123, 255));
            }
        });
        panel.add(startButton);


        frame.add(panel, BorderLayout.CENTER);

        JTextArea infoArea = new JTextArea("Please choose your difficulty and click 'Start Game'.");
        infoArea.setEditable(false);
        infoArea.setBackground(new Color(245, 245, 245));
        infoArea.setFont(new Font("Arial", Font.PLAIN, 12));
        infoArea.setForeground(Color.DARK_GRAY);
        frame.add(new JScrollPane(infoArea), BorderLayout.SOUTH);

        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    private void showHelp() {
        JOptionPane.showMessageDialog(frame, "Game Rules:\n\n" +
                        "1. The first player to reach the exit wins.\n" +
                        "2. The maze contains points that can be collected.\n" +
                        "3. Collect 3 points to place a trap that can catch your opponent.\n" +
                        "4. A trap restricts the opponent's movement for 5 seconds.\n" +
                        "5. To place a trap, press the spacebar.\n",
                "Game Rules", JOptionPane.INFORMATION_MESSAGE);
    }


    // Запуск игры
    private void startGame() {
        if (!isReady) {
            isReady = true;
            startButton.setText("Waiting for other player...");
            startButton.setEnabled(false);
        }

        String difficulty = (String) difficultyComboBox.getSelectedItem();
        notifyServerGameReady(username, difficulty);
    }

    private void notifyServerGameReady(String username, String difficulty) {
        out.println("READY " + username + " " + difficulty);
    }

    public void startButtonTrue() {
        isReady = false;
        startButton.setText("Start Game");
        startButton.setEnabled(true);
    }
}
