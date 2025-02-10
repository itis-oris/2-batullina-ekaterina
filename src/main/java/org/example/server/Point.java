package org.example.server;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

@Getter
@Setter
public class Point {
    private int[][] points;
    private Maze maze;
    private int starGlowSize = 10;
    private Timer starEffectTimer;
    private double glowAlpha = 0.2;
    private int glowSize = 0;
    private boolean glowIncreasing = true;
    private int starGlowDirection = 1;

    public Point(Maze maze) {
        this.maze = maze;
        this.points = new int[maze.getHeight()][maze.getWidth()];
        startStarEffect();
    }

    public void updateGlowState(){
        int maxGlow = 30;
        double alphaSpeed = 0.1;

        if (glowIncreasing) {
            glowSize++;
        } else {
            glowSize--;
        }

        if (glowSize >= maxGlow) {
            glowIncreasing = false;
        } else if (glowSize <= 0) {
            glowIncreasing = true;
        }

        if (glowIncreasing) {
            glowAlpha = Math.min(0.2, glowAlpha + alphaSpeed);
        } else {
            glowAlpha = Math.max(0.1, glowAlpha - alphaSpeed);
        }
    }

    public void place() {
        Random random = new Random();
        for (int i = 0; i < maze.getHeight(); i++) {
            for (int j = 0; j < maze.getWidth(); j++) {
                if (maze.getMaze()[i][j] == 0 && random.nextDouble() < 0.05 && i != 1 && j != 1) {
                    points[i][j] = 1;
                }
            }
        }
    }

    public void drawStar(Graphics g, int x, int y, int size) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int[] xPoints = new int[10];
        int[] yPoints = new int[10];

        double angle = Math.PI / 5;
        for (int i = 0; i < 5; i++) {
            xPoints[i * 2] = (int) (x + size * Math.cos(i * 2 * angle));
            yPoints[i * 2] = (int) (y - size * Math.sin(i * 2 * angle));

            xPoints[i * 2 + 1] = (int) (x + size / 2 * Math.cos(i * 2 * angle + angle));
            yPoints[i * 2 + 1] = (int) (y - size / 2 * Math.sin(i * 2 * angle + angle));
        }

        g2d.setColor(Color.YELLOW);
        g2d.fillPolygon(xPoints, yPoints, 10);

        g2d.setColor(Color.ORANGE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawPolygon(xPoints, yPoints, 10);
    }

    public void drawStarGlow(Graphics g, int x, int y, int size) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < 5; i++) {
            int alpha = (int) (255 * glowAlpha);
            g2d.setColor(new Color(255, 255, 0, alpha));
            int offset = i * 5;
            g2d.fillOval(x - size / 2 - offset, y - size / 2 - offset, size + (offset * 2), size + (offset * 2));
        }
    }

    public void startStarEffect() {
        starEffectTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateGlowState();
                starGlowSize += starGlowDirection;
                if (starGlowSize >= 30 || starGlowSize <= 10) {
                    starGlowDirection = -starGlowDirection;
                }
            }
        });
        starEffectTimer.start();
    }
}