package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class MouseThingi extends MouseAdapter {


	private final Vec4 vertexHeap = new Vec4();
	private final Vec4 appliedVertexHeap = new Vec4();
	private final Vec4 vertexSumHeap = new Vec4();
	private final Vec4 normalHeap = new Vec4();
	private final Vec3 normalHeap3 = new Vec3();
	private final Vec4 appliedNormalHeap = new Vec4();
	private final Vec4 normalSumHeap = new Vec4();
	private final Vec3 normalSumHeap3 = new Vec3();
	private final Mat4 skinBonesMatrixHeap = new Mat4();
	private final Mat4 skinBonesMatrixSumHeap = new Mat4();
	private final Mat4 skinBonesMatrixSumHeap3 = new Mat4();
	private final Vec3 screenDimension = new Vec3();
	private final Mat4 screenDimensionMat3Heap = new Mat4();

//	private final ViewerCamera viewerCamera;
	private final PortraitCameraManager cameraManager;

	Vec2 lastClick;
	Vec2 leftClickStart;
	Timer clickTimer;
	Timer paintTimer;
	boolean mouseInBounds = false;
	private ProgramPreferences programPreferences;

	public MouseThingi(PortraitCameraManager cameraManager){
		this.cameraManager = cameraManager;
		this.programPreferences = ProgramGlobals.getPrefs();
		clickTimer = new Timer(16, this::handleClick);

	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		clickTimer.setRepeats(true);
		clickTimer.start();
		mouseInBounds = true;
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		if ((leftClickStart == null) && (lastClick == null)) {
			clickTimer.stop();
		}
		mouseInBounds = false;
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (programPreferences.getThreeDCameraPanButton().isButton(e)) {
			lastClick = new Vec2(e.getXOnScreen(), e.getYOnScreen());
		}
		else if (programPreferences.getThreeDCameraSpinButton().isButton(e)) {
			leftClickStart = new Vec2(e.getXOnScreen(), e.getYOnScreen());
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (programPreferences.getThreeDCameraPanButton().isButton(e)) {
			final double dx = e.getXOnScreen() - lastClick.x;
			final double dy = e.getYOnScreen() - lastClick.y;
			applyPan(dx, dy);
			lastClick = null;
		}
		else if (programPreferences.getThreeDCameraSpinButton().isButton(e) && (leftClickStart != null)) {
			final Vec2 selectEnd = new Vec2(e.getX(), e.getY());
			// System.out.println(area);
			// dispMDL.selectVerteces(area,m_d1,m_d2,MainFrame.panel.currentSelectionType());
			leftClickStart = null;
		}
		if (!mouseInBounds && (leftClickStart == null) && (lastClick == null)) {
			clickTimer.stop();
		}
		/*
		 * if( dispMDL != null ) dispMDL.refreshUndo();
		 */
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		int wr = e.getWheelRotation();
		final boolean neg = wr < 0;

		if (neg) {
			wr = -wr;
		}
		for (int i = 0; i < wr; i++) {
			if (neg) {
				// cameraPos.x -= (mx - getWidth() / 2)
				// * (1 / m_zoom - 1 / (m_zoom * 1.15));
				// cameraPos.y -= (my - getHeight() / 2)
				// * (1 / m_zoom - 1 / (m_zoom * 1.15));
				// cameraPos.z -= (getHeight() / 2)
				// * (1 / m_zoom - 1 / (m_zoom * 1.15));
				cameraManager.distance *= 1.15;
			}
			else {
				cameraManager.distance /= 1.15;
				// cameraPos.x -= (mx - getWidth() / 2)
				// * (1 / (m_zoom * 1.15) - 1 / m_zoom);
				// cameraPos.y -= (my - getHeight() / 2)
				// * (1 / (m_zoom * 1.15) - 1 / m_zoom);
				// cameraPos.z -= (getHeight() / 2)
				// * (1 / (m_zoom * 1.15) - 1 / m_zoom);
			}
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

					applyPan(dx, dy);

					lastClick.x = (int) mx;
					lastClick.y = (int) my;
				}
				if (leftClickStart != null) {

					final double dx = mx - leftClickStart.x;
					final double dy = my - leftClickStart.y;
					cameraManager.horizontalAngle -= Math.toRadians(dy);
					cameraManager.verticalAngle -= Math.toRadians(dx);
					leftClickStart.x = (int) mx;
					leftClickStart.y = (int) my;
				}
				// MainFrame.panel.setMouseCoordDisplay(m_d1,m_d2,((mx-getWidth()/2)/m_zoom)-m_a,-(((my-getHeight()/2)/m_zoom)-m_b));

			}
		}
	}

	private void applyPan(final double dx, final double dy) {
		this.screenDimension.set(-1, 0, 0);
//		MathUtils.copy(cameraManager.camera.viewProjectionMatrix, screenDimensionMat3Heap);
		screenDimensionMat3Heap.set(cameraManager.camera.viewProjectionMatrix);
		screenDimensionMat3Heap.transpose();
//		Matrix3f.transform(screenDimensionMat3Heap, screenDimension, screenDimension);
		screenDimension.transform(screenDimensionMat3Heap);
		screenDimension.normalize();
		screenDimension.scale((float) dx * 0.008f * cameraManager.distance);
		cameraManager.target.add(screenDimension);
		this.screenDimension.set(0, 1, 0);
//		MathUtils.copy(cameraManager.camera.viewProjectionMatrix, screenDimensionMat3Heap);
		screenDimensionMat3Heap.set(cameraManager.camera.viewProjectionMatrix);
		screenDimensionMat3Heap.transpose();
//		Matrix3f.transform(screenDimensionMat3Heap, screenDimension, screenDimension);
		screenDimension.transform(screenDimensionMat3Heap);
		screenDimension.normalize();
		screenDimension.scale((float) dy * 0.008f * cameraManager.distance);
		cameraManager.target.add(screenDimension);
	}
}
