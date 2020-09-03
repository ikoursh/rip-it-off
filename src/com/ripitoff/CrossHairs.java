package com.ripitoff;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;


public class CrossHairs extends JComponent {

    CrossHairs inst = this;
    private BufferedImage image;
    public static Point[] points = new Point[2];
    boolean flip;

    public CrossHairs(boolean flip) {
        this.flip = flip;
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                inst.setLocation(e.getLocationOnScreen());
                repaint();
                points[flip ? 1 : 0] = e.getLocationOnScreen();


            }
        });

        try {
            image = //(BufferedImage) Toolkit.getDefaultToolkit().createImage(Main.class.getResource("/res/crosshair.png"));
                    ImageIO.read(getClass().getClassLoader().getResource("crosshair.png"));

            if (flip) {
                image = flipV(image);
                image = flipH(image);
            }
        } catch (Exception ignored) {
        }
    }

    public BufferedImage flipH(BufferedImage image) {
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -image.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(tx,
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }


    public BufferedImage flipV(BufferedImage image) {
        AffineTransform ty = AffineTransform.getScaleInstance(-1, 1);
        ty.translate(-image.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(ty,
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this); // see javadoc for more info on the parameters

    }


}
