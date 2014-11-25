package edu.colostate.vchill.gui;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * This class is the mouse listener that will be used by
 * the display window to allow mouse interaction.  It will
 * forward all requests to some form of communication interface
 * That will handle all the update requests etc.
 * <p/>
 * NOTE: Before making right clicks the popupInit() method should
 * be called.  This needs to be changed at a later point.
 *
 * @author Justin Carlson
 * @author Jochen Deyke
 * @author jpont
 * @version 2008-05-05
 * @created January 07, 2003
 */
public abstract class ViewWindowMouseListener extends MouseInputAdapter implements MouseWheelListener {
    protected static final edu.colostate.vchill.Config config = edu.colostate.vchill.Config.getInstance();
    protected static final edu.colostate.vchill.ViewControl vc = edu.colostate.vchill.ViewControl.getInstance();
    protected static final WindowManager wm = WindowManager.getInstance();
    private boolean dragging = false;
    private int clickX, clickY;

    // these 4 variables are used for tracking the user's mouse drags for painting on the annotation layer
    protected int oldX;
    protected int oldY;
    protected int currentX;
    protected int currentY;

    @Override
    public void mousePressed(final MouseEvent e) {

        if (ViewPaintPanel.GreasePencilAnnotationEnabled) {
            oldX = e.getX();
            oldY = e.getY();
        } else {
            if (e.isPopupTrigger()) {
                this.showPopup(e);
                return;
            }

            switch (e.getButton()) {
                case MouseEvent.BUTTON1:
                case MouseEvent.BUTTON2:
                    if (!dragging) {
                        this.clickX = e.getX();
                        this.clickY = e.getY();
                    }
                    break;
                //            case MouseEvent.BUTTON3:
                //                if (e.isPopupTrigger()) this.showPopup(e);
                ////              this.clickX = e.getX();
                ////              this.clickY = e.getY();
                //                break;
            }
        }
    }

    @Override
    public void mouseClicked(final MouseEvent e) {

        if (ViewPaintPanel.GreasePencilAnnotationEnabled) {
            currentX = e.getX();
            currentY = e.getY();

            this.textUserAnnotation(e, currentX, currentY);

            oldX = currentX;
            oldY = currentY;

        } else {
            switch (e.getButton()) {
                case MouseEvent.BUTTON1:
                    this.markData(e);
                    break;
                case MouseEvent.BUTTON2:
                    this.recenter(e.getX(), e.getY());
                    break;
                //            case MouseEvent.BUTTON3:
                //                this.showPopup(e);
                //                break;
            }
        }
    }

    /**
     * Handles the event when the mouse is dragged across the
     * object this listener is associated with.
     */
    @Override
    public void mouseDragged(final MouseEvent e) {

        if (ViewPaintPanel.GreasePencilAnnotationEnabled) {
            currentX = e.getX();
            currentY = e.getY();

            this.paintUserAnnotation(e, currentX, currentY, oldX, oldY);

            oldX = currentX;
            oldY = currentY;

        } else {
            int mods = e.getModifiersEx();
            if ((mods & InputEvent.BUTTON3_DOWN_MASK) != 0) return; //don't track right-drag

            int startX = Math.min(clickX, e.getX());
            int startY = Math.min(clickY, e.getY());
            int endX = Math.max(clickX, e.getX());
            int endY = Math.max(clickY, e.getY());
            if (endX - startX < 5 && endY - startY < 5) return; //too small - don't count as drag
            this.dragging = true;

            if ((mods & InputEvent.BUTTON2_DOWN_MASK) > 0) { //middle-drag
                this.setDragOffset(e.getX() - clickX, e.getY() - clickY);
            } else if ((mods & InputEvent.BUTTON1_DOWN_MASK) > 0) { //left-drag
                this.setDragRect(startX, startY, endX, endY);
            }
        }
    }

    /*
    @Override public void mouseMoved (final MouseEvent e)
    {
    }
    */

    /**
     * Used to deal with occurances where the mouse button has
     * just been released.
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        if (e.isPopupTrigger()) {
            this.showPopup(e);
            return;
        }

        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                if (this.dragging) {
                    this.dragging = false;
                    this.resetDragRect();
                }
                break;
            case MouseEvent.BUTTON2:
                if (this.dragging) {
                    this.dragging = false;
                    this.resetDragOffset();
                }
                break;
//            case MouseEvent.BUTTON3:
//                if (e.isPopupTrigger()) this.showPopup(e);
//                //else if (e.getX() != clickX && e.getY() != clickY) this.hidePopup();
//                break;
        }
    }

    public void mouseWheelMoved(final MouseWheelEvent mve) {
        double newRange = config.getPlotRange() + mve.getWheelRotation() * mve.getScrollAmount() * 5;
        if (newRange <= 0) return;
        config.setPlotRange(newRange);
        ViewRemotePanel.getInstance().update();
        wm.setCenterInKm();
        wm.replotOverlay();
        wm.clearScreen(); //get rid of old data
        ((ViewWindow) mve.getSource()).repaint();
        vc.rePlot();
    }

    protected void markData(final MouseEvent e) {
    }

    protected void recenter(final int x, final int y) {
    }

    protected void showPopup(final MouseEvent e) {
    }

    protected void hidePopup() {
    }

    protected void setDragOffset(final int x, final int y) {
    }

    protected void resetDragOffset() {
    }

    protected void setDragRect(final int startX, final int startY, final int endX, final int endY) {
    }

    protected void resetDragRect() {
    }

    protected void paintUserAnnotation(MouseEvent e, int currentX, int currentY, int oldX, int oldY) {
    }

    protected void textUserAnnotation(MouseEvent e, int currentX, int currentY) {
    }
}
