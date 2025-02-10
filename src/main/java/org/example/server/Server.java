package org.example.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static final List<ClientHandler> clients = new ArrayList<>();
    private static Maze maze;
    private static int playerIdCounter = 1;
    private static String firstPlayerUsername = null;
    private static String secondPlayerUsername = null;
    private static boolean firstPlayerReady = false;
    private static boolean secondPlayerReady = false;

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен на порту " + PORT);

            new Thread(Server::updateSlowdownStates).start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Клиент подключен: " + clientSocket.getInetAddress());

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String m = in.readLine();
                String username = m.split(" ")[1];
                String difficulty = m.split(" ")[2];
                if (username == null || username.isEmpty()) {
                    System.out.println("Никнейм не был передан. Отключаем клиента.");
                    clientSocket.close();
                    continue;
                }
                ClientHandler clientHandler = new ClientHandler(clientSocket, playerIdCounter, username, difficulty);
                clients.add(clientHandler);
                playerIdCounter++;
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void updateSlowdownStates() {
        while (true) {
            try {
                Thread.sleep(1000);
                synchronized (clients) {
                    for (ClientHandler client : clients) {
                        if (client.getPlayer().isSlowed()) {
                            client.getPlayer().updateSlowdown();
                            client.sendPlayerState();
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendMazeStateToClients() {
        if (firstPlayerReady && secondPlayerReady) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.sendMazeState();
                    client.sendPlayerState();
                }
            }
        }
    }

    static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private ServerPlayer player;
        private final int playerId;
        private boolean isReady;
        private String username;
        private boolean isRunning = true;
        private String difficulty;

        public ClientHandler(Socket socket, int playerId, String username, String difficulty) {
            this.clientSocket = socket;
            this.playerId = playerId;
            this.username = username;
            this.difficulty = difficulty;
            this.player = new ServerPlayer(clientSocket, playerId, username);
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                out.println("ID " + playerId);
                out.println("USER " + username);

                player = new ServerPlayer(clientSocket, playerId, username);
                synchronized (clients) {
                    sendPlayerState();
                }

                if (!isReady) {
                    handlePlayerReady(username, difficulty);
                    isReady = true;
                }

                String message;
                while (isRunning && (message = in.readLine()) != null) {
                    if (message.startsWith("READY")) {
                        String[] parts = message.split(" ");
                        String playerDifficulty = parts[2];
                        this.difficulty = playerDifficulty;
                        handlePlayerReady(username, difficulty);
                    }
                    else {
                        handlePlayerInput(message);
                    }
                    synchronized (clients) {
                        sendPlayerState();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                cleanUp();
            }
        }

        private synchronized void handlePlayerReady(String username, String difficulty) {
            if (firstPlayerUsername == null) {
                firstPlayerUsername = username;
                firstPlayerReady = true;
            }
            else if (secondPlayerUsername == null) {
                secondPlayerUsername = username;
                secondPlayerReady = true;
            }
            if (firstPlayerReady && secondPlayerReady) {
                maze = createMazeBasedOnDifficulty(difficulty);
                maze.generate();
                maze.getTrap().place();
                maze.getPoint().place();
                Server.sendMazeStateToClients();
                startGameForBothPlayers();
            }
        }

        private static Maze createMazeBasedOnDifficulty(String difficulty) {
            int width = 0;
            int height = 0;

            switch (difficulty.toLowerCase()) {
                case "easy":
                    width = 20;
                    height = 20;
                    break;
                case "medium":
                    width = 22;
                    height = 22;
                    break;
                case "hard":
                    width = 25;
                    height = 25;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid difficulty: " + difficulty);
            }
            return new Maze(width, height);
        }

        private synchronized void startGameForBothPlayers() {
            getClientByUsername(firstPlayerUsername).sendMessage("START_GAME");
            getClientByUsername(secondPlayerUsername).sendMessage("START_GAME");
        }

        public void stopClient() {
            isRunning = false;
            interrupt();
            cleanUp();
        }

        private void cleanUp() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendMazeState() {

            out.println("STATE " + maze.getWidth() + " " + maze.getHeight());

            for (int i = 0; i < maze.getHeight(); i++) {
                for (int j = 0; j < maze.getWidth(); j++) {
                    out.print(maze.getMaze()[i][j] + " ");
                }
                out.println();
            }

            for (int i = 0; i < maze.getHeight(); i++) {
                for (int j = 0; j < maze.getWidth(); j++) {
                    out.print(maze.getTrap().getTraps()[i][j] + " ");
                }
                out.println();
            }

            for (int i = 0; i < maze.getHeight(); i++) {
                for (int j = 0; j < maze.getWidth(); j++) {
                    out.print(maze.getPoint().getPoints()[i][j] + " ");
                }
                out.println();
            }
        }

        private void sendPlayerState() {
            out.println("PLAYER " + player.getX() + " " + player.getY() + " " + player.getScore() + " " + player.isSlowed() + " " + player.getSlowdownTimer());
        }

        private synchronized void handlePlayerInput(String message) {
            String[] parts = message.split(" ");
            if (parts[0].equals("MOVE")) {

                int dx = Integer.parseInt(parts[1]);
                int dy = Integer.parseInt(parts[2]);

                player.move(dx, dy, maze);
                sendMazeState();
                sendPlayerState();
            } else if (parts[0].equals("SET_TRAP")) {
                int x = Integer.parseInt(parts[2]);
                int y = Integer.parseInt(parts[3]);
                int score = Integer.parseInt(parts[4]);

                if (score >= 3) {
                    player.placeTrap(x, y, maze);
                    player.setScore(player.getScore() - 3);
                    sendMazeState();
                    sendPlayerState();
                }
                else {
                    out.println("ERROR");
                }
            } else if (parts[0].equals("VICTORY")) {
                String winnerUsername = parts[1];
                String victoryMessage = "PLAYER " + winnerUsername + " WINS!";
                getClientByUsername(firstPlayerUsername).sendMessage(victoryMessage);
                getClientByUsername(secondPlayerUsername).sendMessage(victoryMessage);
                stopGame();
            }
        }

        private ClientHandler getClientByUsername(String username) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    if (client.getPlayer().getUsername().equals(username)) {
                        return client;
                    }
                }
            }
            return null;
        }

        public ServerPlayer getPlayer() {
            return player;
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        private void stopGame() {
            isReady = false;
            getClientByUsername(firstPlayerUsername).player.toBegin();
            getClientByUsername(secondPlayerUsername).player.toBegin();
            firstPlayerUsername = null;
            secondPlayerUsername = null;
            firstPlayerReady = false;
            secondPlayerReady = false;
        }

    }
}
