package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.DrawPlaneAction;
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
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class DrawPlaneActivity extends ViewportActivity {
	private DrawingState drawingState = DrawingState.NOTHING;
	private Vec2 mouseStart;
	private Vec3 mouseStartV3;
	private Vec2 lastMousePoint;
	private Vec3 lastMousePointV3;
	private GenericMoveAction planeAction;
	private int numSegsX;
	private int numSegsY;

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
			mouseStart = locationCalculator.getProjected(coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
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
		if (Math.abs(mouseEnd.x - mouseStart.x) >= 0.1 && Math.abs(mouseEnd.y - mouseStart.y) >= 0.1) {
			if (planeAction == null) {
				startThePlane(mouseEnd, dim1, dim2);
			} else {
				planeAction.updateTranslation(mouseEnd.x - lastMousePoint.x, mouseEnd.y - lastMousePoint.y, 0);
			}
			lastMousePoint = mouseEnd;
		}
	}

	private void startThePlane(Vec2 mouseEnd, byte dim1, byte dim2) {
		//				Viewport viewport = viewportListener.getViewport();
//				Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
		try {
			Vec3 facingVector = new Vec3(0, 0, 1); // todo make this work with CameraHandler
			Material solidWhiteMaterial = ModelUtils.getWhiteMaterial(modelView.getModel());
			Geoset solidWhiteGeoset = getSolidWhiteGeoset(solidWhiteMaterial);

			DrawPlaneAction drawPlaneAction = new DrawPlaneAction(mouseStart, mouseEnd, dim1, dim2, facingVector, numSegsX, numSegsY, solidWhiteGeoset);

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
	public void mousePressed(MouseEvent e, CameraHandler cameraHandler) {
		if (drawingState == DrawingState.NOTHING) {
			mouseStart = cameraHandler.getPoint_ifYZplane(e.getX(), e.getY());
			mouseStartV3 = cameraHandler.getGeoPoint(e.getX(), e.getY());
			drawingState = DrawingState.WANT_BEGIN_BASE;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, CameraHandler cameraHandler) {
		if (drawingState == DrawingState.BASE) {
			if (planeAction != null) {
				undoManager.pushAction(planeAction);
				planeAction = null;
			}
			drawingState = DrawingState.NOTHING;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e, CameraHandler cameraHandler) {
		mouseDragged(e, cameraHandler);
	}

	@Override
	public void mouseDragged(MouseEvent e, CameraHandler cameraHandler) {
		if (drawingState == DrawingState.WANT_BEGIN_BASE || drawingState == DrawingState.BASE) {
			drawingState = DrawingState.BASE;

			Vec2 mouseEnd = cameraHandler.getPoint_ifYZplane(e.getX(), e.getY());
			Vec3 mouseEndV3 = cameraHandler.getGeoPoint(e.getX(), e.getY());
//			updateBase(mouseEnd, cameraHandler);
			updateBase2(mouseEnd, mouseEndV3, cameraHandler);
		}
	}


	public void updateBase(Vec2 mouseEnd, CameraHandler cameraHandler) {
		if (Math.abs(mouseEnd.x - mouseStart.x) >= 0.1 && Math.abs(mouseEnd.y - mouseStart.y) >= 0.1) {
			if (planeAction == null) {
				startTheAction(mouseEnd, cameraHandler);
			} else {
				planeAction.updateTranslation(mouseEnd.x - lastMousePoint.x, mouseEnd.y - lastMousePoint.y, 0);
			}
			lastMousePoint = mouseEnd;
		}
	}

	public void updateBase2(Vec2 mouseEnd, Vec3 mouseEndV3, CameraHandler cameraHandler) {
//		if (Math.abs(mouseEndV3.x - mouseStartV3.x) >= 0.1 && Math.abs(mouseEndV3.y - mouseStartV3.y) >= 0.1 && Math.abs(mouseEndV3.z - mouseStartV3.z) >= 0.1) {
		if (Math.abs(mouseEnd.x - mouseStart.x) >= 0.1 && Math.abs(mouseEnd.y - mouseStart.y) >= 0.1) {
			if (planeAction == null) {
				System.out.println("starting plane: " + mouseStartV3);
				startTheAction2(mouseEnd, mouseEndV3, cameraHandler);
			} else {
				Vec3 diff = Vec3.getDiff(mouseEndV3, lastMousePointV3);
				System.out.println("updating plane, \t point: " + mouseEndV3 + "\t diff: " + diff + "\t Vec2 end: " + mouseEnd);
//				planeAction.updateTranslation(mouseEnd.x - lastMousePoint.x, mouseEnd.y - lastMousePoint.y, 0);
//				planeAction.updateTranslation(mouseEndV3.x - lastMousePointV3.x, mouseEndV3.y - lastMousePointV3.y, mouseEndV3.z - lastMousePointV3.z);
				planeAction.updateTranslation(diff);
			}
			lastMousePointV3 = mouseEndV3;
			lastMousePoint = mouseEnd;
		}
	}

	private void startTheAction2(Vec2 mouseEnd, Vec3 mouseEndV3, CameraHandler cameraHandler) {
		//				Viewport viewport = viewportListener.getViewport();
//				Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
//		Vec3 facingVector = new Vec3(1, 0, 0).transform(cameraHandler.getViewPortAntiRotMat2());
		try {
			Vec3 facingVector = new Vec3(1, 0, 0);

			Material solidWhiteMaterial = ModelUtils.getWhiteMaterial(modelView.getModel());
			Geoset solidWhiteGeoset = getSolidWhiteGeoset(solidWhiteMaterial);

			UndoAction addAction = getAddAction(solidWhiteMaterial, solidWhiteGeoset);

			DrawPlaneAction2 drawPlaneAction2 = new DrawPlaneAction2(mouseStart, mouseEnd, mouseStartV3, mouseEndV3, cameraHandler, facingVector, numSegsX, numSegsY, solidWhiteGeoset);

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

	private void startTheAction(Vec2 mouseEnd, CameraHandler cameraHandler) {
		//				Viewport viewport = viewportListener.getViewport();
//				Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
//		Vec3 facingVector = new Vec3(1, 0, 0).transform(cameraHandler.getViewPortAntiRotMat2());
		try {
			Vec3 facingVector = new Vec3(1, 0, 0);

			Material solidWhiteMaterial = ModelUtils.getWhiteMaterial(modelView.getModel());
			Geoset solidWhiteGeoset = getSolidWhiteGeoset(solidWhiteMaterial);

			UndoAction addAction = getAddAction(solidWhiteMaterial, solidWhiteGeoset);

			DrawPlaneAction2 drawPlaneAction2 = new DrawPlaneAction2(mouseStart, mouseEnd, cameraHandler, facingVector, numSegsX, numSegsY, solidWhiteGeoset);

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
