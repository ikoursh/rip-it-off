package com.ripitoff;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main extends JFrame {
    private final Point[] points = new Point[3];
    int i = 0;
    JFrame jFrame = this;

    private boolean control = false;

    Main() {
        System.out.println();

        FlowLayout menu = new FlowLayout();
        this.setLayout(menu);
        Label introText = new Label("Hi, welcome to Rip-It-Off. This tool is used to extract online books to PDF's");
        this.add(introText);

        JPanel fileSelector = new JPanel();
        fileSelector.setLayout(new BoxLayout(fileSelector, BoxLayout.Y_AXIS));

        JLabel selectLabel = new JLabel("NO FILE SELECTED");

        JButton select = new JButton("select output file");
        JFileChooser jFileChooser = new JFileChooser();
        select.addActionListener(e -> {
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
            jFileChooser.showDialog(this, "save");
            if (jFileChooser.getSelectedFile() != null)
                selectLabel.setText(jFileChooser.getSelectedFile().getName());
            else {
                selectLabel.setText("NO FILE SELECTED");
            }
        });
        fileSelector.add(select);
        fileSelector.add(selectLabel);


        JButton next = new JButton("next");
        next.addActionListener(e -> {
            if (jFileChooser.getSelectedFile() != null) {
                getContentPane().removeAll();
                repaint();

                getPageNumber(jFileChooser.getSelectedFile());
            } else {
                JOptionPane.showMessageDialog(this, "A file must be chosen first");
            }
        });
        this.add(fileSelector);
        this.add(next);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);


    }

    private void getPageNumber(File file) {
        JLabel jLabel = new JLabel("Enter the number of pages in the book: ");
        add(jLabel);
        JTextField pntf = new JTextField(5);
        this.add(pntf);
        JButton next = new JButton("next");
        next.addActionListener((ActionEvent event) -> {
            String text = pntf.getText();
            try {
                int pn = Integer.parseInt(text);

                if (pn <= 0) {
                    throw new Exception("number must be positive");
                }
                getContentPane().removeAll();
                repaint();
                showInstructions(pn, file);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error, page number must be a natural number (positive, round)");
            }
        });
        add(next);
        pack();
    }

    private void showCrossHair(Point p, boolean f) {
        CrossHairs c1 = new CrossHairs(f);
        c1.setBounds(p.x, p.y, 224, 224);
        this.add(c1);
        this.repaint();
    }

    private void showInstructions(int pn, File file) {
        JLabel label = new JLabel("<html>This is the last part on which instructions will be available on.<br>" +
                "After you press next you will be presented with a semi-transparent window.<br>" +
                "Navigate to the online version of you book, then alt+tab to bring the window to the foreground<br>" +
                "You will now need to specify some parameters, hold down the cntrl button and select the top left of the book, then do the same for the bottom right.<br>" +
                "Finally, cntrl+click the \"next page\" button.</html>");
        add(label);
        JButton next = new JButton("GO");
        next.addActionListener(e -> go(pn, file));
        add(next);
        pack();
    }

    private void go(int pages, File file) {
        try {
            this.getContentPane().removeAll();
            this.setVisible(false);
            dispose();


            this.setUndecorated(true);
            this.setResizable(false);
            this.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
            this.setVisible(true);
            this.setOpacity(0.3f);
            this.setLayout(null);
            repaint();


            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    control = e.getID() == KeyEvent.KEY_PRESSED;
                }
                return false;
            });

            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    try {
                        if (control && i <= 2) {
                            points[i] = e.getLocationOnScreen();
                            i += 1;

                            if (i == 1 || i == 2) {
                                boolean flip = i == 2;
                                showCrossHair(new Point(points[i - 1].x - 25, points[i - 1].y - 25), flip);
                                CrossHairs.points[flip ? 1 : 0] = new Point(points[i - 1].x - 25, points[i - 1].y - 25);

                            }
                            if (i != 3) {
                                return;
                            }
                            jFrame.setOpacity(0);
                            System.out.println("scanning");
                            Robot robot = new Robot();

                            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
                            PdfWriter.getInstance(document, new FileOutputStream(file));
                            document.open();

                            Point[] points = CrossHairs.points;
                            Rectangle rectangle = new Rectangle(points[0].x + 25, points[0].y + 25, Math.abs(points[1].x - points[0].x), Math.abs(points[1].y - points[0].y));

                            for (int j = 0; j < pages; j++) {
                                System.out.println((j + 1) + "/" + pages);
                                Thread.sleep(500);
                                BufferedImage bufferedImage = robot.createScreenCapture(rectangle);
                                int mask4 = InputEvent.BUTTON1_MASK;
                                robot.mousePress(mask4);
                                robot.delay(75);
                                robot.mouseRelease(mask4);
                                File temp = File.createTempFile("temp" + j, ".png");
                                ImageIO.write(bufferedImage, "png", temp);
                                Thread.sleep(500);
                                com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(temp.getAbsolutePath());
                                document.add(image);
                                temp.delete();
                            }
                            document.close();
                            JOptionPane.showMessageDialog(jFrame, "Done! book saved to: " + file.getAbsolutePath());
                            System.exit(0);

                        }

                    } catch (Exception e2) {
                        e2.printStackTrace();
                        JOptionPane.showMessageDialog(jFrame, "an unexpected error occurred, please send the following error code to support: " + e2.getStackTrace()[0].getLineNumber());
                        System.exit(0);
                    }
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "an unexpected error occurred, please send the following error code to support: " + e.getStackTrace()[0].getLineNumber());
            System.exit(0);
        }
    }

    public static void main(String[] args) throws AWTException, IOException, DocumentException, InterruptedException {
        new Main();
    }


}
