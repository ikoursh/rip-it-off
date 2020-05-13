package com.koursh.inbar.ripitoff;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Main extends JFrame {
    private final Point[] points = new Point[3];
    AtomicInteger i = new AtomicInteger(0);

    private boolean control = false;

    Main() throws AWTException, IOException, DocumentException, InterruptedException {

        this.setUndecorated(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setOpacity(0f);

        this.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
        this.setVisible(true);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                control = e.getID() == KeyEvent.KEY_PRESSED;
            }
            return false;
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (control && i.get() <= 2) {
                    points[i.get()] = e.getLocationOnScreen();
                    i.addAndGet(1);
                }

            }
        });


        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".pdf");
            }

            @Override
            public String getDescription() {
                return null;
            }
        });
        jFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        jFileChooser.setCurrentDirectory(new File("C:\\Users\\Student\\Desktop\\Books"));
        jFileChooser.showDialog(this, "save");


        Robot robot = new Robot();
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter total amount of pages: ");
        int pages = Integer.parseInt(scanner.nextLine());

        System.out.println("define capture rectangle with control+click");
        this.setOpacity(0.1f);

        while (i.get() != 2) {
            Thread.onSpinWait();
        }
        System.out.print("Position your mouse on the next page arrow and control+click it once to start");
        while (i.get() != 3) {
            Thread.onSpinWait();
        }

        this.setOpacity(0f);


        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter.getInstance(document, new FileOutputStream(jFileChooser.getSelectedFile()));
        document.open();


        Rectangle rectangle = new Rectangle(points[0].x, points[0].y, Math.abs(points[1].x - points[0].x), Math.abs(points[1].y - points[0].y));

        for (int j = 0; j < pages; j++) {

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BufferedImage bufferedImage = robot.createScreenCapture(rectangle);
            int mask4 = InputEvent.BUTTON1_MASK;
            robot.mousePress(mask4);
            robot.delay(75);
            robot.mouseRelease(mask4);
            File file = new File(String.format("%s\\%s.png", "C:\\Users\\Student\\Desktop\\Books\\temp", j));
            ImageIO.write(bufferedImage, "png", file);
            Thread.sleep(200);
            com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(file.getAbsolutePath());
            document.add(image);
            file.delete();

        }
        document.close();
        System.exit(0);
    }

    public static void main(String[] args) throws AWTException, IOException, DocumentException, InterruptedException {
        new Main();
    }


}
