package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.SelectionBoxHelper;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.MouseEventHelpers;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MouseListenerThing2 extends MouseAdapter {

	private final CoordinateSystem coordinateSystem;
	private final ProgramPreferences programPreferences;
	private ViewportActivityManager activityManager;

	private Vec2 startP = null;
	private Vec2 endP = null;
	private final Vec2 vec2Temp = new Vec2();
	private final Vec2 endOld = new Vec2();

	private boolean isSelecting = false;
	private boolean isActing = false;

	private final JComponent viewportView;

	private boolean mouseInBounds = false;
	private JPopupMenu contextMenu;
	private final Consumer<Cursor> cursorManager;
	private final BiConsumer<Double, Double> coordDisplayListener2;


	public MouseListenerThing2(JComponent viewportView,
	                           BiConsumer<Double, Double> coordDisplayListener2,
	                           CoordinateSystem coordinateSystem) {
		this.viewportView = viewportView;
		this.coordDisplayListener2 = coordDisplayListener2;
		this.coordinateSystem = coordinateSystem;
		this.cursorManager = viewportView::setCursor;
		programPreferences = ProgramGlobals.getPrefs();
	}


	public MouseListenerThing2 setActivityManager(ViewportActivityManager activityManager) {
		this.activityManager = activityManager;
		return this;
	}


	@Override
	public void mouseEntered(final MouseEvent e) {
		if (activityManager != null && !activityManager.isEditing()) {
			activityManager.viewportChanged(cursorManager);
			viewportView.requestFocus();
			mouseInBounds = true;
			viewportView.setBorder(BorderFactory.createBevelBorder(1, Color.YELLOW, Color.YELLOW.darker()));
		}
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		if (activityManager != null && !activityManager.isEditing()) {
			mouseInBounds = false;
			viewportView.setBorder(BorderFactory.createBevelBorder(1));
		}
	}
	@Override
	public void mousePressed(final MouseEvent e) {
		endP = setPoint(e, endP);
		startP = new Vec2(endP);
		Mat4 viewProjectionMatrix = coordinateSystem.getViewProjectionMatrix();
		double sizeAdj = coordinateSystem.sizeAdj();

		if (activityManager != null) {
			activityManager.viewportChanged(cursorManager);
			viewportView.requestFocus();

			Vec2 topLeft = new Vec2(startP).maximize(endP);
			Vec2 botRight = new Vec2(endP).minimize(startP);
			SelectionBoxHelper viewBox = coordinateSystem.getSelectionBoxHelper(topLeft, botRight);

			activityManager.mousePressed(e, viewProjectionMatrix, viewBox, sizeAdj);
			isSelecting = activityManager.isSelecting();
			isActing = activityManager.isEditing();
		}

	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (endP != null) {
			endP = setPoint(e, endP);
		}
		if (activityManager != null) {
			if (isActing) {

				Mat4 viewProjectionMatrix = coordinateSystem.getViewProjectionMatrix();
				double sizeAdj = coordinateSystem.sizeAdj();
				activityManager.mouseReleased(e, viewProjectionMatrix, sizeAdj);

//			System.out.println("getStartPGeo: " + getStartPGeo() + " (" + startP + "), " + "getEndPGeo2: " + getEndPGeo2() + " (" + endP + "), ");
			} else if (isSelecting && endP != null){
				vec2Temp.set(1, 1);
				startP.mul(vec2Temp);
				endP.mul(vec2Temp);
				Vec2 topLeft = new Vec2(startP).maximize(endP);
				Vec2 botRight = new Vec2(endP).minimize(startP);
				SelectionBoxHelper viewBox = coordinateSystem.getSelectionBoxHelper(topLeft, botRight);

				double sizeAdj = coordinateSystem.sizeAdj();
				activityManager.mouseReleased(e, viewBox, sizeAdj);
			}
		}

		startP = null;
		endP = null;
		isActing = false;
		isSelecting = false;
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

		if (endP != null) {
			int modifiersEx = e.getModifiersEx();
//			System.out.println("prefPan: " + programPreferences.getThreeDCameraPanMouseEx() + ", prefSpin: " + programPreferences.getThreeDCameraSpinMouseEx() + ", mouseEx: " + modifiersEx);

			endOld.set(endP);
			endP = setPoint(e, endP);
			vec2Temp.set(endP).sub(endOld);
			if (MouseEventHelpers.matches(modifiersEx, programPreferences.getThreeDCameraPanMouseEx())
					|| MouseEventHelpers.matches(modifiersEx, programPreferences.getThreeDCameraSpinMouseEx())) {
//				System.out.println("transl x: " + (e.getX() - endP.y) + " (" + e.getX() + "-" + endP.y + ")" + ", transl y: " + (e.getY() - endP.z) + " (" + e.getY() + "-" + endP.z + ")");
				coordinateSystem.translateZoomed(vec2Temp.x, vec2Temp.y);
			} else if ((isActing || isSelecting) && activityManager != null) {
				Mat4 viewProjectionMatrix = coordinateSystem.getViewProjectionMatrix();
				double sizeAdj = coordinateSystem.sizeAdj();
				activityManager.mouseDragged(e, viewProjectionMatrix, sizeAdj);
			}
		}
		updateCoordDisp(e);
		viewportView.repaint();
	}

	private Vec2 setPoint(MouseEvent e, Vec2 point) {
		Component component = e.getComponent();
		float xRatio = 2.0f * ((float) e.getX()) / (float) component.getWidth() - 1.0f;
		float yRatio = 1.0f - 2.0f * ((float) e.getY()) / (float) component.getHeight();
		if(point == null){
			point = new Vec2(xRatio, yRatio);
		} else {
			point.set(xRatio, yRatio);
		}
		return point;
	}

	private void updateCoordDisp(MouseEvent e) {
		setPoint(e, vec2Temp);
		Vec2 vec2 = coordinateSystem.geomVN(vec2Temp);
//		Vec3 vec3 = coordinateSystem.getGeoPoint(vec2Temp);
//		System.out.println("geo v3: " + vec3);
		coordDisplayListener2.accept((double) vec2.x, (double) vec2.y);
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		updateCoordDisp(e);
		if(activityManager != null){
			if (!mouseInBounds && viewportView.getBounds().contains(e.getPoint()) && !activityManager.isEditing()) {
				mouseEntered(e);
			}
			activityManager.mouseMoved(e, coordinateSystem.getViewProjectionMatrix(), coordinateSystem.sizeAdj());
		}
	}

	public boolean isActing() {
		return isActing;
	}

	public boolean isSelecting() {
		return isSelecting;
	}

	public Vec2 getEndP() {
		return endP;
	}

	public Vec2 getStartP() {
		return startP;
	}

	Vec3 vec3Heap = new Vec3();
	public Vec3 getStart(){
		return vec3Heap.set(startP.x, startP.y, 0);
	}
	public Vec3 getEnd(){
		return vec3Heap.set(endP.x, endP.y, 0);
	}

}
