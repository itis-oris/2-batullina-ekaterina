package org.example;

import org.example.client.gui.LoginFrame;
import org.example.server.Server;

import javax.swing.*;

public class DualClientTest {

    public static void main(String[] args) {
        new Thread(() -> {
            Server.main(new String[0]);
        }).start();

        Thread client1 = new Thread(() -> {
            SwingUtilities.invokeLater(() -> new LoginFrame());
        });

        Thread client2 = new Thread(() -> {
            SwingUtilities.invokeLater(() -> new LoginFrame());
        });

        client1.start();
        client2.start();
    }
}
