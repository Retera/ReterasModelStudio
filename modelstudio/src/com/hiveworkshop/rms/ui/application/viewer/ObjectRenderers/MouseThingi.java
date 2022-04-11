package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class MouseThingi extends MouseAdapter {

	private final CameraManager cameraManager;
	private ProgramPreferences programPreferences;
	private ViewportActivityManager activityManager;

	private Vec2 startP = null;
	private Vec2 endP = null;

	private boolean isSelecting = false;
	private boolean isActing = false;


	Vec2 lastClick;
	Vec2 leftClickStart;
	Timer clickTimer;
	boolean mouseInBounds = false;

	public MouseThingi(CameraManager cameraManager){
		this.cameraManager = cameraManager;
		this.programPreferences = ProgramGlobals.getPrefs();
//		clickTimer = new Timer(16, this::handleClick);

	}

	public MouseThingi setActivityManager(ViewportActivityManager activityManager) {
		this.activityManager = activityManager;
		return this;
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
//		clickTimer.setRepeats(true);
//		clickTimer.start();
//		mouseInBounds = true;
	}

	@Override
	public void mouseExited(final MouseEvent e) {
//		if ((leftClickStart == null) && (lastClick == null)) {
//			clickTimer.stop();
//		}
//		mouseInBounds = false;
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		startP = new Vec2(e.getX(), e.getY());
		endP = new Vec2(e.getX(), e.getY());
		int modifiersEx = e.getModifiersEx();
		if ((ProgramGlobals.getPrefs().getSelectMouseButton() & modifiersEx) > 0) {
			isSelecting = true;
			if (activityManager != null) {
//				activityManager.mousePressed(e, cameraHandler);
			}
		} else if ((ProgramGlobals.getPrefs().getModifyMouseButton() & modifiersEx) > 0) {
			System.out.println("pressed! endP: " + endP);
			isActing = true;
			if (activityManager != null) {
//				activityManager.mousePressed(e, cameraHandler);
			}
		} else {
			isActing = true;

			if (activityManager != null) {
//				activityManager.mousePressed(e, cameraHandler);
			}
		}
//		if (programPreferences.getThreeDCameraPanButton().isButton(e)) {
//			lastClick = new Vec2(e.getXOnScreen(), e.getYOnScreen());
//		} else if (programPreferences.getThreeDCameraSpinButton().isButton(e)) {
//			leftClickStart = new Vec2(e.getXOnScreen(), e.getYOnScreen());
//		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (endP != null) {
			endP.set(e.getX(), e.getY());
		}
		System.out.println("released! endP: " + endP);

		if ((isActing || isSelecting) && activityManager != null) {

//			activityManager.mouseReleased(e, cameraHandler);
//			System.out.println("getStartPGeo: " + getStartPGeo() + " (" + startP + "), " + "getEndPGeo2: " + getEndPGeo2() + " (" + endP + "), ");
		}

		startP = null;
		endP = null;
		isActing = false;
		isSelecting = false;

//		if (programPreferences.getThreeDCameraPanButton().isButton(e)) {
//			double dx = e.getXOnScreen() - lastClick.x;
//			double dy = e.getYOnScreen() - lastClick.y;
//			cameraManager.applyPan(dx, dy);
//			lastClick = null;
//		} else if (programPreferences.getThreeDCameraSpinButton().isButton(e) && (leftClickStart != null)) {
//			final Vec2 selectEnd = new Vec2(e.getX(), e.getY());
//			// System.out.println(area);
//			// dispMDL.selectVerteces(area,m_d1,m_d2,MainFrame.panel.currentSelectionType());
//			leftClickStart = null;
//		}
//		if (!mouseInBounds && (leftClickStart == null) && (lastClick == null)) {
//			clickTimer.stop();
//		}
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		cameraManager.doZoom(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (endP != null) {
			int modifiersEx = e.getModifiersEx();
//			System.out.println("prefPan: " + programPreferences.getThreeDCameraPanMouseEx() + ", prefSpin: " + programPreferences.getThreeDCameraSpinMouseEx() + ", mouseEx: " + modifiersEx);
			if (programPreferences.getThreeDCameraPanMouseEx() == modifiersEx) {
//				System.out.println("transl x: " + (e.getX() - endP.y) + " (" + e.getX() + "-" + endP.y + ")" + ", transl y: " + (e.getY() - endP.z) + " (" + e.getY() + "-" + endP.z + ")");
//				cameraHandler.translate(-(e.getX() - endP.x), (e.getY() - endP.y));
				cameraManager.applyPan(-(e.getX() - endP.x), (e.getY() - endP.y));
			} else if (programPreferences.getThreeDCameraSpinMouseEx() == modifiersEx) {
//				cameraHandler.rotate((e.getX() - endP.x), (e.getY() - endP.y));
				cameraManager.rotate((e.getX() - endP.x), (e.getY() - endP.y));
			} else if ((isActing || isSelecting) && activityManager != null) {
//				activityManager.mouseDragged(e, cameraHandler);
			}
			endP.set(e.getX(), e.getY());
		}
	}

	public void handleClick(final ActionEvent e) {
		if (e.getSource() == clickTimer) {
			final int xoff = 0;
			final int yoff = 0;
			// Component temp = this;
			// while (temp != null) {
			// xoff += temp.getX();
			// yoff += temp.getY();
			// if (temp.getClass() == ModelPanel.class) {
			// temp = MainFrame.panel;
			// } else {
			// temp = temp.getParent();
			// }
			// }
			final PointerInfo pointerInfo = MouseInfo.getPointerInfo();
			if (pointerInfo != null) {
				final double mx = pointerInfo.getLocation().x - xoff;// MainFrame.frame.getX()-8);
				final double my = pointerInfo.getLocation().y - yoff;// MainFrame.frame.getY()-30);
				// JOptionPane.showMessageDialog(null,mx+","+my+" as mouse,
				// "+lastClick.x+","+lastClick.y+" as last.");
				// System.out.println(xoff+" and "+mx);

				if (lastClick != null) {
					final double dx = mx - lastClick.x;
					final double dy = my - lastClick.y;

					cameraManager.applyPan(dx, dy);

					lastClick.x = (int) mx;
					lastClick.y = (int) my;
				}
				if (leftClickStart != null) {

					rotate(mx, my);
				}
				// MainFrame.panel.setMouseCoordDisplay(m_d1,m_d2,((mx-getWidth()/2)/m_zoom)-m_a,-(((my-getHeight()/2)/m_zoom)-m_b));

			}
		}
	}

	private void rotate(double mx, double my) {
		final double dx = mx - leftClickStart.x;
		final double dy = my - leftClickStart.y;
		cameraManager.horizontalAngle -= Math.toRadians(dy);
		cameraManager.verticalAngle -= Math.toRadians(dx);
		leftClickStart.x = (int) mx;
		leftClickStart.y = (int) my;
	}

}
