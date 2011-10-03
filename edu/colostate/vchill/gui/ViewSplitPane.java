package edu.colostate.vchill.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 * Utility class to allow JSplitPanes to be painted to BufferedImages
 *
 * @author  Jochen Deyke
 * @author  jpont
 * @version 2010-08-30
 */
public class ViewSplitPane extends JSplitPane
{
    public ViewSplitPane (final JPanel left, final JPanel right)
    {
        super(JSplitPane.HORIZONTAL_SPLIT, left, right);
        //setDividerSize(0);
        setResizeWeight(0.8);
    }

    public BufferedImage getBufferedImage ()
    {
        BufferedImage imageBuffer =
            new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics c = imageBuffer.createGraphics();
        c.setColor(Color.BLACK);
        c.fillRect(0, 0, getWidth(), getHeight());
        paintChildren(imageBuffer.createGraphics());
        return imageBuffer;
    }
}
