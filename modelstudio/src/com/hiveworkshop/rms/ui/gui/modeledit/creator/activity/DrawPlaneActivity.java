package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.DrawPlaneAction2;
import com.hiveworkshop.rms.editor.actions.editor.CompoundMoveAction;
import com.hiveworkshop.rms.editor.actions.util.DoNothingMoveActionAdapter;
import com.hiveworkshop.rms.editor.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class DrawPlaneActivity extends ViewportActivity {
	private DrawingState drawingState = DrawingState.NOTHING;
	private final Vec2 mouseStartPoint = new Vec2();
	private final Vec2 lastMousePoint = new Vec2();
	private final Vec3 mouseStartV3 = new Vec3();
	private final Vec3 lastMousePointV3 = new Vec3();
	private GenericMoveAction planeAction;
	private int numSegsX;
	private int numSegsY;
	private final Vec3 vec3Heap = new Vec3();
	private final Vec3 vec3HeapTemp = new Vec3();
	private final Quat quatHeap = new Quat();
	private final Mat4 tempMat = new Mat4();

	public DrawPlaneActivity(ModelHandler modelHandler,
	                         ModelEditorManager modelEditorManager,
	                         int numSegsX, int numSegsY, int numSegsZ) {
		super(modelHandler, modelEditorManager);
		this.numSegsX = numSegsX;
		this.numSegsY = numSegsY;
	}

	public void setNumSegsX(int numSegsX) {
		this.numSegsX = numSegsX;
	}

	public void setNumSegsY(int numSegsY) {
		this.numSegsY = numSegsY;
	}

	@Override
	public void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (drawingState == DrawingState.NOTHING) {
			Vec3 locationCalculator = convertToVec3(coordinateSystem, e.getPoint());
			mouseStartPoint.set(locationCalculator.getProjected(coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ()));
			drawingState = DrawingState.WANT_BEGIN_BASE;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (drawingState == DrawingState.BASE) {
			if (planeAction != null) {
				undoManager.pushAction(planeAction);
				planeAction = null;
			}
			drawingState = DrawingState.NOTHING;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e, CoordinateSystem coordinateSystem) {
		mouseDragged(e, coordinateSystem);
	}

	@Override
	public void mouseDragged(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (drawingState == DrawingState.WANT_BEGIN_BASE || drawingState == DrawingState.BASE) {
			drawingState = DrawingState.BASE;
			Vec3 locationCalculator = convertToVec3(coordinateSystem, e.getPoint());

			Vec2 mouseEnd = locationCalculator.getProjected(coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
			updateBase(mouseEnd, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		}
	}

	public static Vec3 convertToVec3(CoordinateSystem coordinateSystem, Point point) {
		Vec3 vertex = new Vec3(0, 0, 0);
		vertex.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(point.x));
		vertex.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(point.y));
		return vertex;
	}

	public void updateBase(Vec2 mouseEnd, byte dim1, byte dim2) {
		if (Math.abs(mouseEnd.x - mouseStartPoint.x) >= 0.1 && Math.abs(mouseEnd.y - mouseStartPoint.y) >= 0.1) {
			vec3Heap.set(0, mouseEnd.x, mouseEnd.y).transform(tempMat);
			if (planeAction == null) {
				startThePlane(mouseEnd, dim1, dim2);
			} else {
				vec3HeapTemp.set(vec3Heap).sub(lastMousePointV3);
//				planeAction.updateTranslation(mouseEnd.x - lastMousePoint.x, mouseEnd.y - lastMousePoint.y, 0);
				planeAction.updateTranslation(vec3HeapTemp);
			}
			lastMousePointV3.set(vec3Heap);
			lastMousePoint.set(mouseEnd);
		}
	}

	private void startThePlane(Vec2 mouseEnd, byte dim1, byte dim2) {
		//				Viewport viewport = viewportListener.getViewport();
//				Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
		try {
			Vec3 facingVector = new Vec3(0, 0, 1); // todo make this work with CameraHandler
			Material solidWhiteMaterial = ModelUtils.getWhiteMaterial(modelView.getModel());
			Geoset solidWhiteGeoset = getSolidWhiteGeoset(solidWhiteMaterial);
			Mat4 vpMat = getVPMat(dim1, dim2);
//			DrawPlaneAction drawPlaneAction = new DrawPlaneAction(mouseStart, mouseEnd, dim1, dim2, facingVector, numSegsX, numSegsY, solidWhiteGeoset);
			DrawPlaneAction2 drawPlaneAction = new DrawPlaneAction2(mouseStartPoint, mouseEnd, vpMat, facingVector, numSegsX, numSegsY, solidWhiteGeoset);

			UndoAction addAction = getAddAction(solidWhiteMaterial, solidWhiteGeoset);
			if (addAction != null) {
				planeAction = new CompoundMoveAction("Add Plane", new DoNothingMoveActionAdapter(addAction), drawPlaneAction);
			} else {
				planeAction = new CompoundMoveAction("Add Plane", drawPlaneAction);
			}
			planeAction.redo();


		} catch (WrongModeException exc) {
			drawingState = DrawingState.NOTHING;
			JOptionPane.showMessageDialog(null, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (drawingState == DrawingState.NOTHING) {
//			mouseStart = new Vec2(e.getX(), e.getY());
			mouseStartPoint.set(getPoint(e));
//			Mat4 viewPortAntiRotMat2 = cameraHandler.getViewportMat();
			mouseStartV3.set(0, mouseStartPoint.x, mouseStartPoint.y).transform(viewProjectionMatrix);
			drawingState = DrawingState.WANT_BEGIN_BASE;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (drawingState == DrawingState.BASE) {
			if (planeAction != null) {
				undoManager.pushAction(planeAction);
				planeAction = null;
			}
			drawingState = DrawingState.NOTHING;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		mouseDragged(e, viewProjectionMatrix, sizeAdj);
	}

	@Override
	public void mouseDragged(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (drawingState == DrawingState.WANT_BEGIN_BASE || drawingState == DrawingState.BASE) {
			drawingState = DrawingState.BASE;

//			Vec2 mouseEnd = new Vec2(e.getX(), e.getY()).scale((float) cameraHandler.sizeAdj());
			Vec2 mouseEnd = getPoint(e);
//			Vec3 mouseEndV3 = cameraHandler.getGeoPoint(e.getX(), e.getY());
//			Mat4 viewPortAntiRotMat2 = cameraHandler.getViewportMat();
			updateBase(mouseEnd, viewProjectionMatrix);
//			updateBase2(mouseEnd, mouseEndV3, viewPortAntiRotMat2);
		}
	}


	public void updateBase(Vec2 mouseEnd, Mat4 viewPortAntiRotMat2) {
		if (Math.abs(mouseEnd.x - mouseStartPoint.x) >= 0.1 && Math.abs(mouseEnd.y - mouseStartPoint.y) >= 0.1) {
			vec3Heap.set(0, mouseEnd.x, mouseEnd.y).transform(viewPortAntiRotMat2);
			if (planeAction == null) {
				startTheAction(mouseEnd, viewPortAntiRotMat2);
			} else {
				vec3HeapTemp.set(vec3Heap).sub(lastMousePointV3);
//				planeAction.updateTranslation(mouseEnd.x - lastMousePoint.x, mouseEnd.y - lastMousePoint.y, 0);
				planeAction.updateTranslation(vec3HeapTemp);
			}
			lastMousePointV3.set(vec3Heap);
			lastMousePoint.set(mouseEnd);
		}
	}

	public void updateBase2(Vec2 mouseEnd, Vec3 mouseEndV3, Mat4 viewPortAntiRotMat2) {
		if (Math.abs(mouseEnd.x - mouseStartPoint.x) >= 0.1 && Math.abs(mouseEnd.y - mouseStartPoint.y) >= 0.1) {
			if (planeAction == null) {
				System.out.println("starting plane: " + mouseStartV3);
				startTheAction2(mouseEnd, mouseEndV3, viewPortAntiRotMat2);
			} else {
				vec3Heap.set(0, mouseEnd.x, mouseEnd.y).transform(viewPortAntiRotMat2);
				vec3HeapTemp.set(mouseEndV3).sub(lastMousePointV3);
				System.out.println("updating plane, \t point: " + mouseEndV3 + "\t diff: " + vec3HeapTemp + "\t Vec2 end: " + mouseEnd);
				planeAction.updateTranslation(vec3HeapTemp);
			}
			lastMousePointV3.set(mouseEndV3);
			lastMousePoint.set(mouseEnd);
		}
	}

	private void startTheAction2(Vec2 mouseEnd, Vec3 mouseEndV3, Mat4 viewPortAntiRotMat2) {
		//				Viewport viewport = viewportListener.getViewport();
//				Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
//		Vec3 facingVector = new Vec3(1, 0, 0).transform(cameraHandler.getViewPortAntiRotMat2());
		try {
			Vec3 facingVector = new Vec3(1, 0, 0);

			Material solidWhiteMaterial = ModelUtils.getWhiteMaterial(modelView.getModel());
			Geoset solidWhiteGeoset = getSolidWhiteGeoset(solidWhiteMaterial);

			DrawPlaneAction2 drawPlaneAction2 = new DrawPlaneAction2(mouseStartPoint, mouseEnd, mouseStartV3, mouseEndV3, viewPortAntiRotMat2, facingVector, numSegsX, numSegsY, solidWhiteGeoset);
			UndoAction addAction = getAddAction(solidWhiteMaterial, solidWhiteGeoset);


			if (addAction != null) {
				planeAction = new CompoundMoveAction("Add Plane", new DoNothingMoveActionAdapter(addAction), drawPlaneAction2);
			} else {
				planeAction = new CompoundMoveAction("Add Plane", drawPlaneAction2);

			}
			planeAction.redo();


		} catch (WrongModeException exc) {
			drawingState = DrawingState.NOTHING;
			JOptionPane.showMessageDialog(null, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}


	private void startThePlane1(Vec2 mouseEnd, byte dim1, byte dim2) {
		//				Viewport viewport = viewportListener.getViewport();
//				Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
		try {
			Vec3 facingVector = new Vec3(0, 0, 1); // todo make this work with CameraHandler
			Material solidWhiteMaterial = ModelUtils.getWhiteMaterial(modelView.getModel());
			Geoset solidWhiteGeoset = getSolidWhiteGeoset(solidWhiteMaterial);

			Mat4 vpMat = getVPMat(dim1, dim2);
//			DrawPlaneAction drawPlaneAction = new DrawPlaneAction(mouseStart, mouseEnd, dim1, dim2, facingVector, numSegsX, numSegsY, solidWhiteGeoset);
			DrawPlaneAction2 drawPlaneAction = new DrawPlaneAction2(mouseStartPoint, mouseEnd, vpMat, facingVector, numSegsX, numSegsY, solidWhiteGeoset);
			UndoAction addAction = getAddAction(solidWhiteMaterial, solidWhiteGeoset);

			if (addAction != null) {
				planeAction = new CompoundMoveAction("Add Plane", new DoNothingMoveActionAdapter(addAction), drawPlaneAction);
			} else {
				planeAction = new CompoundMoveAction("Add Plane", drawPlaneAction);
			}
			planeAction.redo();


		} catch (WrongModeException exc) {
			drawingState = DrawingState.NOTHING;
			JOptionPane.showMessageDialog(null, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}


	private Quat getQuat(byte portFirstXYZ, byte portSecondXYZ){

		Vec3 vp1 = new Vec3().setCoord(portFirstXYZ, 1);
		Vec3 vp2 = new Vec3().setCoord(portSecondXYZ, 1);
		Vec3 vpPlaneNorm = vp1.crossNorm(vp2);

		return quatHeap.setAsRotBetween(vpPlaneNorm,Vec3.Z_AXIS);
	}
	private Mat4 getVPMat(byte portFirstXYZ, byte portSecondXYZ){

		Vec3 vp1 = new Vec3().setCoord(portFirstXYZ, 1);
		Vec3 vp2 = new Vec3().setCoord(portSecondXYZ, 1);
		Vec3 vpPlaneNorm = vp1.crossNorm(vp2);

		return tempMat.fromQuat(quatHeap.setAsRotBetween(vpPlaneNorm,Vec3.Z_AXIS));
	}


	private void startTheAction(Vec2 mouseEnd, Mat4 viewPortAntiRotMat2) {
		//				Viewport viewport = viewportListener.getViewport();
//				Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
//		Vec3 facingVector = new Vec3(1, 0, 0).transform(cameraHandler.getViewPortAntiRotMat2());
		try {
			Vec3 facingVector = new Vec3(1, 0, 0);

			Material solidWhiteMaterial = ModelUtils.getWhiteMaterial(modelView.getModel());
			Geoset solidWhiteGeoset = getSolidWhiteGeoset(solidWhiteMaterial);

			DrawPlaneAction2 drawPlaneAction2 = new DrawPlaneAction2(mouseStartPoint, mouseEnd, viewPortAntiRotMat2, facingVector, numSegsX, numSegsY, solidWhiteGeoset);
			UndoAction addAction = getAddAction(solidWhiteMaterial, solidWhiteGeoset);

			if (addAction != null) {
				planeAction = new CompoundMoveAction("Add Plane", new DoNothingMoveActionAdapter(addAction), drawPlaneAction2);
			} else {
				planeAction = new CompoundMoveAction("Add Plane", drawPlaneAction2);

			}
			planeAction.redo();


		} catch (WrongModeException exc) {
			drawingState = DrawingState.NOTHING;
			JOptionPane.showMessageDialog(null, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}


	private enum DrawingState {
		NOTHING, WANT_BEGIN_BASE, BASE
	}
}
