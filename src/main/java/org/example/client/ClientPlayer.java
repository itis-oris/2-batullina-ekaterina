package org.example.client;

import lombok.Getter;
import lombok.Setter;
import org.example.client.gui.MazePanel;
import org.example.server.Point;
import org.example.server.ServerPlayer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

@Setter
@Getter
public class ClientPlayer {
    private ServerPlayer serverPlayer;
    private int x, y;
    private int score = 0;
    private boolean isSlowed = false;
    private int slowdownTimer = 0;
    private MazePanel mazePanel;
    private Image way;
    private Socket socket;

    public ClientPlayer(MazePanel mazePanel, ServerPlayer serverPlayer, Socket socket) {
        if (mazePanel == null || serverPlayer == null) {
            throw new IllegalArgumentException("MazePanel and ServerPlayer cannot be null");
        }
        this.serverPlayer = serverPlayer;
        this.x = 1;
        this.y = 1;
        this.mazePanel = mazePanel;
        this.socket = socket;
        initTextures();
    }

    private void initTextures() {
        way = new ImageIcon(getClass().getResource("/проход.jpeg")).getImage();
    }

    public void move(int dx, int dy) {
        if (isSlowed) {
            return;
        }

        sendMoveCommandToServer(dx, dy);

        x = serverPlayer.getX();
        y = serverPlayer.getY();
        score = serverPlayer.getScore();
        isSlowed = serverPlayer.isSlowed();
        slowdownTimer = serverPlayer.getSlowdownTimer();

        if (mazePanel.getMaze().getMaze()[y][x] == 2) {
            sendVictoryCommandToServer();
        }

        int cellSize = mazePanel.getCellSize();
        int startX = Math.max(0, x - 1) * cellSize;
        int startY = Math.max(0, y - 1) * cellSize;
        int endX = Math.min(mazePanel.getMazeWidth(), x + 1) * cellSize;
        int endY = Math.min(mazePanel.getMazeHeight(), y + 1) * cellSize;

        mazePanel.repaint(startX, startY, endX - startX, endY - startY);
    }

    private void sendVictoryCommandToServer() {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("VICTORY " + serverPlayer.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMoveCommandToServer(int dx, int dy) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("MOVE " + dx + " " + dy);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics g, int cellSize, Point point) {
        int startX = x - 1;
        int startY = y - 1;

        startX = Math.max(0, startX);
        startY = Math.max(0, startY);
        int endX = Math.min(point.getMaze().getWidth() - 1, x + 1);
        int endY = Math.min(point.getMaze().getHeight() - 1, y + 1);

        for (int i = 0; i < point.getMaze().getHeight(); i++) {
            for (int j = 0; j < point.getMaze().getWidth(); j++) {
                if (i < startY || i > endY || j < startX || j > endX) {
                    g.setColor(new Color(169, 169, 169));
                    g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                }
            }
        }

        for (int i = startY; i <= endY; i++) {
            for (int j = startX; j <= endX; j++) {
                if (point.getMaze().getMaze()[i][j] == 0 || point.getMaze().getMaze()[i][j] == 2) {
                    g.drawImage(way, j * cellSize, i * cellSize, cellSize, cellSize, null);
                }
            }
        }

        g.setColor(Color.BLACK);
        g.fillOval(x * cellSize + cellSize / 4, y * cellSize + cellSize / 4, cellSize / 2, cellSize / 2);

    }

    public void setScore(int score) {
        this.score = score;
    }

    public void toBegin() {
        x = 1;
        y = 1;
        score = 0;
        serverPlayer.toBegin();
    }
}
