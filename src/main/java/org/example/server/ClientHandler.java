//package org.example.server;
//
//// Вынесите ClientHandler в отдельный файл ClientHandler.java
//
//import java.io.*;
//import java.net.*;
//
//public class ClientHandler extends Thread {
//    private final Socket clientSocket;
//    private PrintWriter out;
//    private BufferedReader in;
//    private ServerPlayer player;
//    private final int playerId;
//    private boolean isReady;
//    private String username;
//    private boolean isRunning = true;
//    private String difficulty;
//
//    public ClientHandler(Socket socket, int playerId, String username, String difficulty) {
//        this.clientSocket = socket;
//        this.playerId = playerId;
//        this.username = username;
//        this.difficulty = difficulty;
//        this.player = new ServerPlayer(clientSocket, playerId, username);
//    }
//
//    @Override
//    public void run() {
//        try {
//            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//            out = new PrintWriter(clientSocket.getOutputStream(), true);
//
//            out.println("ID " + playerId);
//            System.out.println("USER " + username + " FROOOM SERVEEER");
//            out.println("USER " + username);
//
//            player = new ServerPlayer(clientSocket, playerId, username);
//            synchronized (Server.getClients()) {
//                sendPlayerState();
//            }
//
//            if (!isReady) {
//                handlePlayerReady(username, difficulty);
//                isReady = true;
//            }
//
//            String message;
//            while (isRunning && (message = in.readLine()) != null) {
//                System.out.println(message);
//                if (message.startsWith("READY")) {
//                    System.out.println(message + " FRRROOOM SEEERVERRR222222");
//                    String[] parts = message.split(" ");
//                    String playerDifficulty = parts[2];
//                    this.difficulty = playerDifficulty;
//                    handlePlayerReady(username, difficulty);
//                }
//                else {
//                    handlePlayerInput(message);
//                }
//
//                synchronized (Server.clients) {
//                    // Отправляем обновленное состояние только этому клиенту
//                    sendPlayerState();
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            cleanUp();
//        }
//    }
//
//    private synchronized void handlePlayerReady(String username, String difficulty) {
//        System.out.println("handlePlayerReady");
//        System.out.println("First Player: " + Server.firstPlayerUsername);
//        System.out.println("Second Player: " + Server.secondPlayerUsername);
//
//        if (Server.firstPlayerUsername == null) {
//            Server.firstPlayerUsername = username;
//            Server.firstPlayerReady = true;
//        } else if (Server.secondPlayerUsername == null) {
//            Server.secondPlayerUsername = username;
//            Server.secondPlayerReady = true;
//        }
//
//        if (Server.firstPlayerReady && Server.secondPlayerReady) {
//            Maze maze = createMazeBasedOnDifficulty(difficulty);
//            maze.generate();
//            maze.getTrap().place();
//            maze.getPoint().place();
//            Server.sendMazeStateToClients();
//            startGameForBothPlayers();
//        }
//    }
//
//    private static Maze createMazeBasedOnDifficulty(String difficulty) {
//        int width = 0;
//        int height = 0;
//        switch (difficulty.toLowerCase()) {
//            case "easy":
//                width = 20;
//                height = 20;
//                break;
//            case "medium":
//                width = 22;
//                height = 22;
//                break;
//            case "hard":
//                width = 25;
//                height = 25;
//                break;
//            default:
//                throw new IllegalArgumentException("Invalid difficulty: " + difficulty);
//        }
//        return new Maze(width, height);
//    }
//
//    private synchronized void startGameForBothPlayers() {
//        getClientByUsername(Server.firstPlayerUsername).sendMessage("START_GAME");
//        getClientByUsername(Server.secondPlayerUsername).sendMessage("START_GAME");
//    }
//
//    private void cleanUp() {
//        try {
//            if (in != null) in.close();
//            if (out != null) out.close();
//            if (clientSocket != null) clientSocket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void sendMazeState() {
//        out.println("STATE " + Maze.width + " " + Maze.height);
//        // Print maze state...
//    }
//
//    private void sendPlayerState() {
//        out.println("PLAYER " + player.getX() + " " + player.getY() + " " + player.getScore() + " " + player.isSlowed() + " " + player.getSlowdownTimer());
//    }
//
//    private synchronized void handlePlayerInput(String message) {
//        // Handle MOVE, SET_TRAP, VICTORY...
//    }
//
//    private ClientHandler getClientByUsername(String username) {
//        synchronized (Server.clients) {
//            for (ClientHandler client : Server.clients) {
//                if (client.getPlayer().getUsername().equals(username)) {
//                    return client;
//                }
//            }
//        }
//        return null;
//    }
//
//    public ServerPlayer getPlayer() {
//        return player;
//    }
//
//    public void sendMessage(String message) {
//        out.println(message);
//    }
//}
//
