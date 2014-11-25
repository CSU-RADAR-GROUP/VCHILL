package edu.colostate.vchill.plot;

import edu.colostate.vchill.gui.ViewWindowMouseListener;

import java.awt.event.MouseEvent;

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
 * @version 2006-09-05
 * @created January 07, 2003
 */
class ViewPlotMouseListener extends ViewWindowMouseListener {
    //The popup menu that the window will bring up on a right click.
    private ViewPlotPopup popup;

    /**
     * Call in order to actually use the popup window.  This should
     * be done in a constructer and BEFORE the user can right click.
     */
    public void initPopup(final ViewPlotWindow win) {
        this.popup = new ViewPlotPopup(win);
    }

    @Override
    protected void markData(final MouseEvent e) {
        ((ViewPlotWindow) e.getComponent()).markData(e.getX(), e.getY());
    }

    @Override
    protected void recenter(final int x, final int y) {
        this.popup.recenter(x, y);
    }

    @Override
    protected void showPopup(final MouseEvent e) {
        this.popup.show(e);
    }

    @Override
    protected void hidePopup() {
        this.popup.hide();
    }

    @Override
    protected void setDragOffset(final int x, final int y) {
        ((ViewPlotWindow) this.popup.getWindow()).setDragOffset(x, y);
    }

    @Override
    protected void resetDragOffset() {
        ((ViewPlotWindow) this.popup.getWindow()).resetDragOffset();
    }

    @Override
    protected void setDragRect(final int startX, final int startY, final int endX, final int endY) {
        ((ViewPlotWindow) this.popup.getWindow()).setDragRect(startX, startY, endX, endY);
    }

    @Override
    protected void resetDragRect() {
        ((ViewPlotWindow) this.popup.getWindow()).resetDragRect();
    }

    @Override
    protected void paintUserAnnotation(MouseEvent e, int currentX, int currentY, int oldX, int oldY) {
        ((ViewPlotWindow) e.getComponent()).updateAnnotationLayer(currentX, currentY, oldX, oldY);
    }

    @Override
    protected void textUserAnnotation(MouseEvent e, int currentX, int currentY) {
        ((ViewPlotWindow) e.getComponent()).updateTextAnnotationLayer(currentX, currentY);
    }

}
