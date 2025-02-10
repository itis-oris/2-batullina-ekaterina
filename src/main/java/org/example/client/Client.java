package org.example.client;

import org.example.client.gui.MazePanel;
import org.example.client.gui.PreGameFrame;
import org.example.server.Maze;
import org.example.server.ServerPlayer;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private PreGameFrame preGameFrame;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame;
    private MazePanel mazePanel;
    private ClientPlayer clientPlayer;
    private ServerPlayer serverPlayer;
    private int playerId;
    private String username;

    public Client(String username) {
        this.username = username;
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            preGameFrame = new PreGameFrame(username, out);
            this.frame = new JFrame("Maze Game");
            new Thread(this::listenForGameState).start();
            setupControls();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void listenForGameState() {
        try {
            String message;
            Maze maze = null;
            serverPlayer = new ServerPlayer(socket, playerId, username);
            clientPlayer = null;

            while ((message = in.readLine()) != null) {
                String[] parts = message.split(" ");
                if (message.startsWith("STATE")) {

                    int mazeWidth = Integer.parseInt(parts[1]);
                    int mazeHeight = Integer.parseInt(parts[2]);

                    int[][] newMaze = new int[mazeHeight][mazeWidth];
                    int[][] newTrap = new int[mazeHeight][mazeWidth];
                    int[][] newPoint = new int[mazeHeight][mazeWidth];
                    for (int i = 0; i < mazeHeight; i++) {
                        String[] row = in.readLine().split(" ");
                        for (int j = 0; j < mazeWidth; j++) {
                            newMaze[i][j] = Integer.parseInt(row[j]);
                        }
                    }
                    for (int i = 0; i < mazeHeight; i++) {
                        String[] row = in.readLine().split(" ");
                        for (int j = 0; j < mazeWidth; j++) {
                            newTrap[i][j] = Integer.parseInt(row[j]);
                        }
                    }
                    for (int i = 0; i < mazeHeight; i++) {
                        String[] row = in.readLine().split(" ");
                        for (int j = 0; j < mazeWidth; j++) {
                            newPoint[i][j] = Integer.parseInt(row[j]);
                        }
                    }
                    maze = new Maze(mazeWidth, mazeHeight);
                    maze.setMaze(newMaze);
                    maze.getTrap().setTraps(newTrap);
                    maze.getPoint().setPoints(newPoint);
                    if (mazePanel == null) {
                        mazePanel = new MazePanel(clientPlayer, serverPlayer, maze);
                        frame.getContentPane().removeAll();
                        frame.add(mazePanel);
                        frame.pack();
                        frame.revalidate();
                        frame.repaint();
                    } else {
                        mazePanel.updateMazeState(maze);
                    }
                    if (clientPlayer == null) {
                        clientPlayer = new ClientPlayer(mazePanel, serverPlayer, socket);
                        mazePanel.setClientPlayer(clientPlayer);
                    }
                } else if (message.startsWith("PLAYER")) {
                    if (parts.length == 6) {
                        int playerX = Integer.parseInt(parts[1]);
                        int playerY = Integer.parseInt(parts[2]);
                        int score = Integer.parseInt(parts[3]);
                        boolean slowed = Boolean.parseBoolean(parts[4]);
                        int slowdownTimer = Integer.parseInt(parts[5]);
                        if (clientPlayer != null) {
                            clientPlayer.setX(playerX);
                            clientPlayer.setY(playerY);
                            clientPlayer.setScore(score);
                            clientPlayer.setSlowed(slowed);
                            clientPlayer.setSlowdownTimer(slowdownTimer);
                        }
                        if (mazePanel != null) {
                            mazePanel.updatePlayerState(playerX, playerY, score, slowed, slowdownTimer);
                        }
                    }
                    if (parts.length == 3 ) {
                        String winnerUsername = parts[1];
                        if (mazePanel != null) {
                            mazePanel.displayVictoryMessage(winnerUsername);
                            preGameFrame.startButtonTrue();
                            clientPlayer.toBegin();
                            mazePanel = null;
                        }
                    }
                } else if (message.startsWith("TRAP")) {
                    if (clientPlayer != null) {
                        clientPlayer.setSlowed(true);
                    }
                    if (mazePanel != null) {
                        mazePanel.getServerPlayer().setSlowed(true);
                        mazePanel.getServerPlayer().setSlowdownTimer(5);
                        mazePanel.updateScore();
                        clientPlayer.setSlowed(false);
                    }
                }  else if (message.startsWith("SLOWED")) {
                    handleServerMessage(message);
                } else  if (message.startsWith("ID")) {
                    playerId = Integer.parseInt(parts[1]);
                } else if (message.startsWith("VICTORY")) {
                    out.println(message);
                } else if (message.startsWith("ERROR")) {
                    JOptionPane.showMessageDialog(frame, "Недостаточно очков для установки ловушки!");
                } else if (message.startsWith("START_GAME")) {
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        frame.setVisible(true);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleServerMessage(String message) {
        if (message.startsWith("SLOWED")) {
            String[] parts = message.split(" ");
            if (parts.length == 3) {
                boolean slowed = Boolean.parseBoolean(parts[1]);
                int timer = Integer.parseInt(parts[2]);
                updateSlowdownStateFromServer(slowed, timer);
            }
            else if (parts.length == 2 && parts[1].equals("FALSE")) {
                updateSlowdownStateFromServer(false, 0);
            }
        }
    }


    private void updateSlowdownStateFromServer(boolean slowed, int timer) {
        if (clientPlayer != null) {
            clientPlayer.setSlowed(slowed);
            clientPlayer.setSlowdownTimer(timer);
        }
        if (mazePanel != null) {
            mazePanel.repaint();
        }
    }

    private void setupControls() {
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                int dx = 0;
                int dy = 0;
                switch (keyCode) {
                    case KeyEvent.VK_UP:
                        dy = -1;
                        break;
                    case KeyEvent.VK_DOWN:
                        dy = 1;
                        break;
                    case KeyEvent.VK_LEFT:
                        dx = -1;
                        break;
                    case KeyEvent.VK_RIGHT:
                        dx = 1;
                        break;
                    case KeyEvent.VK_SPACE:
                        handleSetTrap();
                        break;
                }
                if (dx != 0 || dy != 0) {
                    sendMoveCommandToServer(dx, dy);
                }
            }
        });
        if (mazePanel != null) {
            mazePanel.setFocusable(true);
            mazePanel.requestFocusInWindow();
        }
        frame.setFocusable(true);
        frame.requestFocusInWindow();
    }

    private void handleSetTrap() {
        sendSetTrapCommandToServer();
    }

    private void sendSetTrapCommandToServer() {
        try {
            out.println("SET_TRAP " + playerId + " " + serverPlayer.getX() + " " + serverPlayer.getY() + " " + serverPlayer.getScore());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMoveCommandToServer(int dx, int dy) {
        try {
            out.println("MOVE " + dx + " " + dy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
