package org.example.server;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

@Getter
@Setter
public class ServerPlayer {
    private int x, y;
    private int score = 0;
    private int id;
    private String username;
    private boolean isSlowed = false;
    private int slowdownTimer;

    private Socket socket;

    public ServerPlayer(Socket socket, int id, String username) {
        this.x = 1;
        this.y = 1;
        this.id = id;
        this.username = username;
        this.socket = socket;

    }

    public void move(int dx, int dy, Maze maze)  {
        if (isSlowed) {
            return;
        }
        int newX = x + dx;
        int newY = y + dy;


        if (maze.isValidMove(newX, newY)) {
            x = newX;
            y = newY;

            if (maze.hasTrapAt(x, y) && slowdownTimer == 0 && maze.trapIsCorrect(x, y, id)) {
                slowdownTimer = 5;
                isSlowed = true;
                maze.removeTrapAt(x, y);
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("TRAP");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (maze.hasPointAt(x, y)) {
                score++;
                maze.removePointAt(x, y);
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("SCORE " + score);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (maze.getMaze()[y][x] == 2) {
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("VICTORY " + username);
                    System.out.println("Игрок " + id + " победил!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void updateSlowdown() {
        if (slowdownTimer > 0) {
            slowdownTimer--;
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("SLOWED TRUE " + slowdownTimer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (slowdownTimer == 0) {
                isSlowed = false;
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("SLOWED FALSE");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getSlowdownTimer() {
        return slowdownTimer;
    }

    public boolean isSlowed() {
        return isSlowed;
    }

    public void placeTrap(int trapX, int trapY, Maze maze) {
        if (score >= 3) {
            maze.setTrapAt(trapX, trapY, id);
        }
    }

    public void toBegin() {
        x = 1;
        y = 1;
        score = 0;
    }
}
