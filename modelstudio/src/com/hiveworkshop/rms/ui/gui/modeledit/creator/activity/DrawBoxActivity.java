package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.CursorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.Graphics2DToModelElementRendererAdapter;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ActiveViewportWatcher;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

public class DrawBoxActivity implements ModelEditorViewportActivity {

	private final ProgramPreferences preferences;
	private ModelEditor modelEditor;
	private final UndoActionListener undoActionListener;
	private final Vec3 locationCalculator = new Vec3(0, 0, 0);
	private final ModelView modelView;
	private SelectionView selectionView;
	private final Graphics2DToModelElementRendererAdapter graphics2dToModelElementRendererAdapter;
	private final ActiveViewportWatcher activeViewportWatcher;

	private DrawingState drawingState = DrawingState.NOTHING;
	private Point2D.Double mouseStart;
	private Point2D.Double lastMousePoint;
	private GenericMoveAction boxAction;
	private int numSegsX;
	private int numSegsY;
	private int numSegsZ;
	private double lastHeightModeZ = 0;
	private double firstHeightModeZ = 0;

	public DrawBoxActivity(final ProgramPreferences preferences, final UndoActionListener undoActionListener,
			final ModelEditor modelEditor, final ModelView modelView, final SelectionView selectionView,
			final ActiveViewportWatcher activeViewportWatcher, final int numSegsX, final int numSegsY,
			final int numSegsZ) {
		this.preferences = preferences;
		this.undoActionListener = undoActionListener;
		this.modelEditor = modelEditor;
		this.modelView = modelView;
		this.selectionView = selectionView;
		this.activeViewportWatcher = activeViewportWatcher;
		this.numSegsX = numSegsX;
		this.numSegsY = numSegsY;
		this.numSegsZ = numSegsZ;
		graphics2dToModelElementRendererAdapter = new Graphics2DToModelElementRendererAdapter(
				preferences.getVertexSize(), preferences);
	}

	public void setNumSegsX(final int numSegsX) {
		this.numSegsX = numSegsX;
	}

	public void setNumSegsY(final int numSegsY) {
		this.numSegsY = numSegsY;
	}

	public void setNumSegsZ(final int numSegsZ) {
		this.numSegsZ = numSegsZ;
	}

	@Override
	public void onSelectionChanged(final SelectionView newSelection) {
		selectionView = newSelection;
	}

	@Override
	public void modelChanged() {

	}

	@Override
	public void modelEditorChanged(final ModelEditor newModelEditor) {
		modelEditor = newModelEditor;
	}

	@Override
	public void viewportChanged(final CursorManager cursorManager) {

	}

	@Override
	public void mousePressed(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		if (drawingState == DrawingState.NOTHING) {
			locationCalculator.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(e.getX()));
			locationCalculator.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(e.getY()));
			locationCalculator.setCoord(CoordinateSystem.Util.getUnusedXYZ(coordinateSystem), 0);
			mouseStart = new Point2D.Double(locationCalculator.getCoord(coordinateSystem.getPortFirstXYZ()),
					locationCalculator.getCoord(coordinateSystem.getPortSecondXYZ()));
			drawingState = DrawingState.WANT_BEGIN_BASE;
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		if (drawingState == DrawingState.BASE) {
			if (boxAction == null) {
				drawingState = DrawingState.NOTHING;
			} else {

				lastHeightModeZ = coordinateSystem.geomY(e.getY());
				firstHeightModeZ = lastHeightModeZ;
				drawingState = DrawingState.HEIGHT;
			}
		} else if (drawingState == DrawingState.HEIGHT) {
			undoActionListener.pushAction(boxAction);
			boxAction = null;
			drawingState = DrawingState.NOTHING;
		}
	}

	@Override
	public void mouseMoved(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		mouseDragged(e, coordinateSystem);
	}

	@Override
	public void mouseDragged(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		if (drawingState == DrawingState.WANT_BEGIN_BASE || drawingState == DrawingState.BASE) {
			drawingState = DrawingState.BASE;
			locationCalculator.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(e.getX()));
			locationCalculator.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(e.getY()));
			locationCalculator.setCoord(CoordinateSystem.Util.getUnusedXYZ(coordinateSystem), 0);
			final Point2D.Double mouseEnd = new Point2D.Double(
					locationCalculator.getCoord(coordinateSystem.getPortFirstXYZ()),
					locationCalculator.getCoord(coordinateSystem.getPortSecondXYZ()));
			updateBase(mouseStart, mouseEnd, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		} else if (drawingState == DrawingState.HEIGHT) {
			final double heightModeZ = coordinateSystem.geomY(e.getY());
			if (Math.abs(heightModeZ - firstHeightModeZ - 1) > 0.1) {
				boxAction.updateTranslation(0, 0, heightModeZ - lastHeightModeZ);
			}
			lastHeightModeZ = heightModeZ;
		}
	}

	public void updateBase(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		if (Math.abs(mouseEnd.x - this.mouseStart.x) >= 0.1 && Math.abs(mouseEnd.y - this.mouseStart.y) >= 0.1) {
			if (boxAction == null) {
				final Viewport viewport = activeViewportWatcher.getViewport();
				final Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
				try {
					boxAction = modelEditor.addBox(this.mouseStart.x, this.mouseStart.y, mouseEnd.x, mouseEnd.y, dim1,
							dim2, facingVector, numSegsX, numSegsY, numSegsZ);
				} catch (final WrongModeException exc) {
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
	public void render(final Graphics2D g, final CoordinateSystem coordinateSystem, final RenderModel renderModel) {
	}

	@Override
	public void renderStatic(final Graphics2D g, final CoordinateSystem coordinateSystem) {
		selectionView.renderSelection(graphics2dToModelElementRendererAdapter.reset(g, coordinateSystem),
				coordinateSystem, modelView, preferences);

	}

	@Override
	public boolean isEditing() {
		return false;
	}

	private enum DrawingState {
		NOTHING, WANT_BEGIN_BASE, BASE, HEIGHT
    }
}
