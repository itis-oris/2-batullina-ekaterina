package org.example.server;

import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Getter
@Setter
public class Trap {
    private int[][] traps;
    private Maze maze;

    public Trap(Maze maze) {
        this.maze = maze;
        this.traps = new int[maze.getHeight()][maze.getWidth()];
    }

    public void place() {
        Random random = new Random();
        for (int i = 0; i < maze.getHeight(); i++) {
            for (int j = 0; j < maze.getWidth(); j++) {
                if (maze.getMaze()[i][j] == 0 && random.nextDouble() < 0.02) {
                    traps[i][j] = -1;
                }
            }
        }
    }
}
