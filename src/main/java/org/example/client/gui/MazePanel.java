package org.example.client.gui;

import lombok.Getter;
import lombok.Setter;
import org.example.client.ClientPlayer;
import org.example.server.Maze;
import org.example.server.ServerPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintWriter;

@Setter
@Getter
public class MazePanel extends JPanel implements KeyListener {

    private final int cellSize = 30;
    private int mazeWidth;
    private int mazeHeight;
    private Maze maze;
    private ClientPlayer clientPlayer;
    private ServerPlayer serverPlayer;
    private Timer timer;
    private ImageIcon pointIcon;

    public MazePanel(ClientPlayer clientPlayer, ServerPlayer serverPlayer, Maze maze) {
        if (maze == null) {
            throw new IllegalArgumentException("Maze cannot be null");
        }
        this.clientPlayer = clientPlayer;
        this.serverPlayer = serverPlayer;
        this.maze = maze;
        this.mazeHeight = maze.getHeight();
        this.mazeWidth = maze.getWidth();
        initTimer();

        pointIcon = new ImageIcon("point_icon.png");
        setLayout(null);
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

        int windowWidth = mazeWidth * cellSize;
        int windowHeight = mazeHeight * cellSize;
        setPreferredSize(new Dimension(windowWidth, windowHeight));
    }

    private void initTimer() {
        timer = new Timer(50, e -> {
            maze.getPoint().updateGlowState();
            repaint();
        });
        timer.start();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (clientPlayer != null) {
            switch (keyCode) {
                case KeyEvent.VK_UP:
                    clientPlayer.move(0, -1);
                    repaint();
                    break;
                case KeyEvent.VK_DOWN:
                    clientPlayer.move(0, 1);
                    break;
                case KeyEvent.VK_LEFT:
                    clientPlayer.move(-1, 0);
                    break;
                case KeyEvent.VK_RIGHT:
                    clientPlayer.move(1, 0);
                    break;
                case KeyEvent.VK_SPACE:
                    if (serverPlayer.getScore() >= 3) {
                        serverPlayer.placeTrap(serverPlayer.getX(), serverPlayer.getY(), maze);
                        serverPlayer.setScore(serverPlayer.getScore() - 3);
                    } else {
                        JOptionPane.showMessageDialog(this, "Недостаточно очков для установки ловушки!");
                    }
                    break;
                default:
                    break;
            }
        } else {
            System.out.println("clientPlayer is null!");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawMaze(g);
        drawPlayerView(g);
        for (int i = Math.max(0, serverPlayer.getY() - 1); i <= Math.min(mazeHeight - 1, serverPlayer.getY() + 1); i++) {
            for (int j = Math.max(0, serverPlayer.getX() - 1); j <= Math.min(mazeWidth - 1, serverPlayer.getX() + 1); j++) {
                if (maze.getPoint().getPoints()[i][j] == 1) {
                    int pointX = j * cellSize + cellSize / 2;
                    int pointY = i * cellSize + cellSize / 2;
                    int pointSize = 10;
                    drawStar(g, pointX, pointY, pointSize);
                }
            }
        }

        int starIconX = mazeWidth * cellSize - 30;
        int starIconY = 30;

        drawStar(g, starIconX, starIconY, 20);
        g.setColor(Color.BLACK);
        g.drawString("" + serverPlayer.getScore(), mazeWidth * cellSize - 40, 35);
        if (serverPlayer.isSlowed()) {
            g.setColor(Color.RED);
            g.drawString("Ловушка, осталось: " + serverPlayer.getSlowdownTimer() + " с.", mazeWidth * cellSize - 170, mazeWidth * cellSize - 10);
          }
    }

    public int getCellSize() {
        if (maze == null) return 20;
        int rows = maze.getHeight();
        int cols = maze.getWidth();

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        int cellWidth = panelWidth / cols;
        int cellHeight = panelHeight / rows;
        return Math.min(cellWidth, cellHeight);
    }

    private void drawMaze(Graphics g) {
        maze.draw(g, cellSize);
    }

    public void displayVictoryMessage(String winnerUsername) {
        JOptionPane.showMessageDialog(this, "Player " + winnerUsername + " wins!");

        Window topLevelWindow = SwingUtilities.getWindowAncestor(this);
        if (topLevelWindow != null) {
            topLevelWindow.dispose();
        }
    }

    public void drawPlayerView(Graphics g) {
        if (clientPlayer != null) {
            clientPlayer.draw(g, cellSize, maze.getPoint());
        }
    }

    private void drawStar(Graphics g, int pointX, int pointY, int pointSize) {
        maze.getPoint().drawStarGlow(g, pointX, pointY, pointSize);
        maze.getPoint().drawStar(g, pointX, pointY, pointSize);
    }

    public void updateMazeState(Maze maze) {
        this.maze = maze;
        setPreferredSize(new Dimension(maze.getWidth(), maze.getHeight()));
        repaint();
    }

    public void updatePlayerState(int playerX, int playerY, int score, boolean slowed, int slowdownTimer) {
        this.serverPlayer.setX(playerX);
        this.serverPlayer.setY(playerY);
        this.serverPlayer.setScore(score);
        this.serverPlayer.setSlowed(slowed);
        this.serverPlayer.setSlowdownTimer(slowdownTimer);
        repaint();
    }

    public void updateScore() {
        repaint();
    }

}
