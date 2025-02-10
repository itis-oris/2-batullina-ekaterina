//package org.example.client;
//
//import org.example.server.Player;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//
//public class ArrowButton extends JButton {
//    private final int dx, dy;
//    private final Player player;
//
//    public ArrowButton(String direction, int dx, int dy, Player player) {
//        this.dx = dx;
//        this.dy = dy;
//        this.player = player;
//        setIcon(new ImageIcon(createArrowIcon(direction))); // Передаем BufferedImage напрямую в ImageIcon
//        addActionListener(e -> player.move(dx, dy)); // Реакция на клик
//    }
//
//    // Метод для создания стрелки
//    private BufferedImage createArrowIcon(String direction) {
//        int width = 50;
//        int height = 50;
//        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g = image.createGraphics();
//        g.setColor(Color.BLACK); // Цвет стрелки
//        g.setStroke(new BasicStroke(3));
//
//        // Координаты для рисования стрелок с углом 60 градусов
//        int[] xPoints = new int[3];
//        int[] yPoints = new int[3];
//
//        // Параметры для создания тупоугольных стрелок
//        int baseOffset = 12;  // Это смещение, которое определяет "тупость" углов
//
//        switch (direction) {
//            case "Up":
//                xPoints[0] = width / 2;
//                yPoints[0] = baseOffset;
//                xPoints[1] = baseOffset;
//                yPoints[1] = height - baseOffset;
//                xPoints[2] = width - baseOffset;
//                yPoints[2] = height - baseOffset;
//                break;
//            case "Down":
//                xPoints[0] = width / 2;
//                yPoints[0] = height - baseOffset;
//                xPoints[1] = baseOffset;
//                yPoints[1] = baseOffset;
//                xPoints[2] = width - baseOffset;
//                yPoints[2] = baseOffset;
//                break;
//            case "Left":
//                xPoints[0] = baseOffset;
//                yPoints[0] = height / 2;
//                xPoints[1] = width - baseOffset;
//                yPoints[1] = baseOffset;
//                xPoints[2] = width - baseOffset;
//                yPoints[2] = height - baseOffset;
//                break;
//            case "Right":
//                xPoints[0] = width - baseOffset;
//                yPoints[0] = height / 2;
//                xPoints[1] = baseOffset;
//                yPoints[1] = baseOffset;
//                xPoints[2] = baseOffset;
//                yPoints[2] = height - baseOffset;
//                break;
//        }
//
//        g.fillPolygon(xPoints, yPoints, 3);
//        g.dispose();
//        return image; // Возвращаем BufferedImage
//    }
//}
