package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.DrawBoxAction;
import com.hiveworkshop.rms.editor.actions.addactions.DrawBoxAction2;
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
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class DrawBoxActivity extends ViewportActivity {

	private DrawingState drawingState = DrawingState.NOTHING;
	private Vec2 mouseStart;
	private Vec2 lastMousePoint;
	private GenericMoveAction boxAction;
	private int numSegsX;
	private int numSegsY;
	private int numSegsZ;
	private double lastHeightModeZ = 0;
	private double firstHeightModeZ = 0;

	public DrawBoxActivity(ModelHandler modelHandler,
	                       ModelEditorManager modelEditorManager,
	                       int numSegsX, int numSegsY, int numSegsZ) {
		super(modelHandler, modelEditorManager);
		this.numSegsX = numSegsX;
		this.numSegsY = numSegsY;
		this.numSegsZ = numSegsZ;
	}

	public void setNumSegsX(int numSegsX) {
		this.numSegsX = numSegsX;
	}

	public void setNumSegsY(int numSegsY) {
		this.numSegsY = numSegsY;
	}

	public void setNumSegsZ(int numSegsZ) {
		this.numSegsZ = numSegsZ;
	}

	@Override
	public void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (drawingState == DrawingState.NOTHING) {
			Vec3 locationCalculator = convertToVec3(coordinateSystem, e.getPoint());
			mouseStart = locationCalculator.getProjected(coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
			drawingState = DrawingState.WANT_BEGIN_BASE;
		}
	}

	public static Vec3 convertToVec3(CoordinateSystem coordinateSystem, Point point) {
		Vec3 vertex = new Vec3(0, 0, 0);
		vertex.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(point.x));
		vertex.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(point.y));
		return vertex;
	}

	@Override
	public void mouseReleased(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (drawingState == DrawingState.BASE) {
			if (boxAction == null) {
				drawingState = DrawingState.NOTHING;
			} else {

				lastHeightModeZ = coordinateSystem.geomY(e.getY());
				firstHeightModeZ = lastHeightModeZ;
				drawingState = DrawingState.HEIGHT;
			}
		} else if (drawingState == DrawingState.HEIGHT) {
			undoManager.pushAction(boxAction);
			boxAction = null;
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
		} else if (drawingState == DrawingState.HEIGHT) {
			double heightModeZ = coordinateSystem.geomY(e.getY());
			if (Math.abs(heightModeZ - firstHeightModeZ - 1) > 0.1) {
				boxAction.updateTranslation(0, 0, heightModeZ - lastHeightModeZ);
			}
			lastHeightModeZ = heightModeZ;
		}
	}

	public void updateBase(Vec2 mouseEnd, byte dim1, byte dim2) {
		if (Math.abs(mouseEnd.x - mouseStart.x) >= 0.1 && Math.abs(mouseEnd.y - mouseStart.y) >= 0.1) {
			if (boxAction == null) {
				Vec3 facingVector = new Vec3(0, 0, 1); // todo make this work with CameraHandler
				try {

					List<GenericMoveAction> moveActions = new ArrayList<>();

					Material solidWhiteMaterial = ModelUtils.getWhiteMaterial(modelView.getModel());
					Geoset solidWhiteGeoset = getSolidWhiteGeoset(solidWhiteMaterial);

					UndoAction addAction = getAddAction(solidWhiteMaterial, solidWhiteGeoset);
					if (addAction != null) {
						moveActions.add(new DoNothingMoveActionAdapter(addAction));
					}

					moveActions.add(new DrawBoxAction(mouseStart, mouseEnd, dim1, dim2, facingVector, numSegsX, numSegsY, numSegsZ, solidWhiteGeoset));

					boxAction = new CompoundMoveAction("Add Box", moveActions);
					;
					boxAction.redo();

				} catch (WrongModeException exc) {
					drawingState = DrawingState.NOTHING;
					JOptionPane.showMessageDialog(null, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				boxAction.updateTranslation(mouseEnd.x - lastMousePoint.x, mouseEnd.y - lastMousePoint.y, 0);
			}
			lastMousePoint = mouseEnd;
		}
	}

	@Override
	public void mousePressed(MouseEvent e, Mat4 viewPortAntiRotMat, double sizeAdj) {
		if (drawingState == DrawingState.NOTHING) {
			mouseStart = getPoint(e);
			drawingState = DrawingState.WANT_BEGIN_BASE;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, Mat4 viewPortAntiRotMat, double sizeAdj) {
		if (drawingState == DrawingState.BASE) {
			if (boxAction == null) {
				drawingState = DrawingState.NOTHING;
			} else {
				lastHeightModeZ = getPoint(e).y;;
				firstHeightModeZ = lastHeightModeZ;
				drawingState = DrawingState.HEIGHT;
			}
		} else if (drawingState == DrawingState.HEIGHT) {
			undoManager.pushAction(boxAction);
			boxAction = null;
			drawingState = DrawingState.NOTHING;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e, Mat4 viewPortAntiRotMat, double sizeAdj) {
		mouseDragged(e, viewPortAntiRotMat, sizeAdj);
	}

	@Override
	public void mouseDragged(MouseEvent e, Mat4 viewPortAntiRotMat, double sizeAdj) {
		Vec2 mouseEnd = getPoint(e);
		if (drawingState == DrawingState.WANT_BEGIN_BASE || drawingState == DrawingState.BASE) {
			drawingState = DrawingState.BASE;


			updateBase(mouseEnd);
		} else if (drawingState == DrawingState.HEIGHT) {
			double heightModeZ = mouseEnd.y;
			if (Math.abs(heightModeZ - firstHeightModeZ - 1) > 0.1) {
				boxAction.updateTranslation(0, 0, heightModeZ - lastHeightModeZ);
			}
			lastHeightModeZ = heightModeZ;
		}
	}

	public void updateBase(Vec2 mouseEnd) {
		if (Math.abs(mouseEnd.x - mouseStart.x) >= 0.1 && Math.abs(mouseEnd.y - mouseStart.y) >= 0.1) {
			if (boxAction == null) {
				Vec3 facingVector = new Vec3(0, 0, 1); // todo make this work with CameraHandler
				try {

					List<GenericMoveAction> moveActions = new ArrayList<>();

					Material solidWhiteMaterial = ModelUtils.getWhiteMaterial(modelView.getModel());
					Geoset solidWhiteGeoset = getSolidWhiteGeoset(solidWhiteMaterial);

					UndoAction addAction = getAddAction(solidWhiteMaterial, solidWhiteGeoset);
					if (addAction != null) {
						moveActions.add(new DoNothingMoveActionAdapter(addAction));
					}

					moveActions.add(new DrawBoxAction2(mouseStart, mouseEnd, facingVector, numSegsX, numSegsY, numSegsZ, solidWhiteGeoset));

					boxAction = new CompoundMoveAction("Add Box", moveActions);
					;
					boxAction.redo();

				} catch (WrongModeException exc) {
					drawingState = DrawingState.NOTHING;
					JOptionPane.showMessageDialog(null, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				boxAction.updateTranslation(mouseEnd.x - lastMousePoint.x, mouseEnd.y - lastMousePoint.y, 0);
			}
			lastMousePoint = mouseEnd;
		}
	}

	private enum DrawingState {
		NOTHING, WANT_BEGIN_BASE, BASE, HEIGHT
    }
}
