package org.example.server;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Random;

@Getter
@Setter
public class Maze {
    private int width;
    private int height;
    private Trap trap;
    private Point point;
    private int[][] maze;
    private Image wall;
    private Image way;

    private int exitX = -1, exitY = -1;

    public Maze(int width, int height) {
        this.width = width;
        this.height = height;
        maze = new int[height][width];
        wall = new ImageIcon(getClass().getResource("/стена.jpg")).getImage();
        way = new ImageIcon(getClass().getResource("/проход.jpeg")).getImage();
        this.trap = new Trap(this);
        this.point = new Point(this);
    }

    public void setMaze(int[][] newMaze) {
        this.maze = newMaze;
    }

    public void generate() {
        for (int i = 0; i < height; i++) {
            Arrays.fill(maze[i], 1);
        }
        maze[1][1] = 0;
        dfs(1, 1);
        trap.place();
        point.place();
        createExit();
    }

    private void dfs(int x, int y) {
        int[] directions = {0, 1, 2, 3};
        shuffle(directions);

        for (int dir : directions) {
            int nx = x, ny = y;
            switch (dir) {
                case 0: nx = x + 2; break;
                case 1: ny = y + 2; break;
                case 2: nx = x - 2; break;
                case 3: ny = y - 2; break;
            }

            if (isInBoundsL(nx, ny) && maze[ny][nx] == 1) {
                maze[ny][nx] = 0;
                maze[(y + ny) / 2][(x + nx) / 2] = 0;
                dfs(nx, ny);
            }
        }
    }

    private void shuffle(int[] array) {
        Random random = new Random();
        for (int i = 0; i < array.length; i++) {
            int j = random.nextInt(array.length);
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    private void createExit() {
        Random rand = new Random();

        int side = rand.nextInt(4);
        int x = 0, y = 0;

        switch (side) {
            case 0:
                x = rand.nextInt(width - 2) + 1;
                y = 0;
                break;
            case 1:
                x = width - 1;
                y = rand.nextInt(height - 2) + 1;
                break;
            case 2:
                x = rand.nextInt(width - 2) + 1;
                y = height - 1;
                break;
            case 3:
                x = 0;
                y = rand.nextInt(height - 2) + 1;
                break;
        }

        maze[y][x] = 2;
        for (int i = 0; i < 2; ++i) {
            if (side == 0 || side == 2) {
                if (isInBounds(x, y + 1) && maze[y + 1][x] == 1) {
                    y++;
                } else if (isInBounds(x, y - 1) && maze[y - 1][x] == 1) {
                    y--;
                } else {
                    break;
                }
            } else if (side == 1 || side == 3) {
                if (isInBounds(x + 1, y) && maze[y][x + 1] == 1) {
                    x++;
                } else if (isInBounds(x - 1, y) && maze[y][x - 1] == 1) {
                    x--;
                } else {
                    break;
                }
            }
            maze[y][x] = 0;
            System.out.println(maze[y][x] + " " + y + " " + x);
        }

    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    private boolean isInBoundsL(int x, int y) {
        return x > 0 && x < width - 1 && y > 0 && y < height - 1;
    }

    public boolean hasTrapAt(int x, int y) {
        return trap.getTraps()[y][x] != 0;
    }

    public boolean hasPointAt(int x, int y) {
        return point.getPoints()[y][x] == 1;
    }

    public void removePointAt(int x, int y) {
        point.getPoints()[y][x] = 0;
    }

    public boolean isValidMove(int x, int y) {
        return isInBounds(x, y) && (maze[y][x] == 0 || maze[y][x] == 2);
    }

    public void setTrapAt(int x, int y, int value) {
        trap.getTraps()[y][x] = value;
    }

    public void removeTrapAt(int x, int y) {
        trap.getTraps()[y][x] = 0;
    }

    public void draw(Graphics g, int cellSize) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (maze[i][j] == 1) {
                    g.drawImage(wall, j * cellSize, i * cellSize, cellSize, cellSize, null);
                } else {
                    g.drawImage(way, j * cellSize, i * cellSize, cellSize, cellSize, null);
                }
            }
        }
    }

    public boolean trapIsCorrect(int x, int y, int id) {
        return trap.getTraps()[y][x] != id;
    }
}
