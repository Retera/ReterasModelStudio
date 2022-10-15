package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MouseListenerThing2 extends MouseAdapter {
	private final ViewportView viewportView;

	private final double ZOOM_FACTOR = 1.15;
	private final CoordinateSystem coordinateSystem;

	private boolean mouseInBounds = false;
	private JPopupMenu contextMenu;

	private final Consumer<Cursor> cursorManager;

	private UndoHandler undoHandler;
	private ViewportActivityManager viewportActivity;
	private final BiConsumer<Double, Double> coordDisplayListener2;

	private Point lastClick;
	private final Timer clickTimer = new Timer(16, e -> clickTimer());

	public MouseListenerThing2(ViewportView viewportView,
	                           BiConsumer<Double, Double> coordDisplayListener2,
	                           CoordinateSystem coordinateSystem) {
		this.viewportView = viewportView;
		this.coordDisplayListener2 = coordDisplayListener2;
		this.coordinateSystem = coordinateSystem;
		this.cursorManager = viewportView::setCursor;
	}

	public MouseListenerThing2 setUndoHandler(UndoHandler undoHandler) {
		this.undoHandler = undoHandler;
		return this;
	}

	public MouseListenerThing2 setViewportActivity(ViewportActivityManager viewportActivity) {
		this.viewportActivity = viewportActivity;
		return this;
	}

	private boolean clickTimer() {
//		System.out.println("clickTimer! + " + System.currentTimeMillis());
		int[] off = viewportView.getOffsets();
		PointerInfo pointerInfo = MouseInfo.getPointerInfo();
		if ((pointerInfo == null) || (pointerInfo.getLocation() == null)) {
			return true;
		}
		double mx = pointerInfo.getLocation().x - off[0];
		double my = pointerInfo.getLocation().y - off[1];

		if (lastClick != null) {

			int deltaX = (int) mx - lastClick.x;
			int deltaY = (int) my - lastClick.y;
			coordinateSystem.translateZoomed(deltaX, deltaY);

			lastClick.x = (int) mx;
			lastClick.y = (int) my;
		}

		coordDisplayListener2.accept(coordinateSystem.geomX(mx), coordinateSystem.geomY(my));

		viewportView.repaint();
		return false;
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		if (!clickTimer.isRunning()) {
			clickTimer.start();
		}
		if (viewportActivity != null && !viewportActivity.isEditing()) {
			viewportActivity.viewportChanged(cursorManager);
			viewportView.requestFocus();
			mouseInBounds = true;
			viewportView.setBorder(BorderFactory.createBevelBorder(1, Color.YELLOW, Color.YELLOW.darker()));
			clickTimer.setDelay(16);
		}
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		if (viewportActivity != null && !viewportActivity.isEditing()) {
			if (lastClick == null) {
				clickTimer.setDelay(250);
			}
			mouseInBounds = false;
			viewportView.setBorder(BorderFactory.createBevelBorder(1));
		}
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2) {
			lastClick = new Point(e.getX(), e.getY());
		} else if (viewportActivity != null && (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3)) {
			viewportActivity.viewportChanged(cursorManager);
			viewportView.requestFocus();
			viewportActivity.mousePressed(e, coordinateSystem);
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (undoHandler != null && viewportActivity != null) {
			undoHandler.refreshUndo();
			// TODO fix, refresh undo
			if ((e.getButton() == MouseEvent.BUTTON2) && (lastClick != null)) {
				double translateX = (e.getX() - lastClick.x);
				double translateY = (e.getY() - lastClick.y);
				coordinateSystem.translateZoomed(translateX, translateY);
				lastClick = null;
			} else if (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3) {
				viewportActivity.mouseReleased(e, coordinateSystem);
			}

			if (!mouseInBounds && lastClick == null) {
				clickTimer.setDelay(250);
				viewportView.repaint();
			}

			if (mouseInBounds && !viewportView.getBounds().contains(e.getPoint()) && !viewportActivity.isEditing()) {
				mouseExited(e);
			}
		}
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3 && contextMenu != null) {
			contextMenu.show(viewportView, e.getX(), e.getY());
		}
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		coordinateSystem.doZoom(e);
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		if(viewportActivity != null){
			viewportActivity.mouseDragged(e, coordinateSystem);
		}
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		if(viewportActivity != null){
			if (!mouseInBounds && viewportView.getBounds().contains(e.getPoint()) && !viewportActivity.isEditing()) {
				mouseEntered(e);
			}
			viewportActivity.mouseMoved(e, coordinateSystem);
			viewportView.repaint();
		}
	}
}
