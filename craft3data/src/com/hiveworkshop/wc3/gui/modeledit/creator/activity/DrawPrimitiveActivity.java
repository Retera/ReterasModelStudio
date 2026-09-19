package com.hiveworkshop.wc3.gui.modeledit.creator.activity;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.ActiveViewportWatcher;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.Viewport;
import com.hiveworkshop.wc3.gui.modeledit.activity.ModelEditorViewportActivity;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.CursorManager;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.gui.modeledit.activity.Graphics2DToModelElementRendererAdapter;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.render3d.RenderModel;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.util.PrimitiveMeshes.PrimitiveOptions;
import com.hiveworkshop.wc3.util.PrimitiveMeshes.PrimitiveShape;
import com.hiveworkshop.wc3.gui.animedit.WrongModeException;

/**
 * Drag a rectangle to set the primitive's footprint; for shapes with a height
 * phase, release and move the mouse to set the height, then click to finish.
 * Same two-phase flow as the box tool.
 */
public final class DrawPrimitiveActivity implements ModelEditorViewportActivity {
	private final ProgramPreferences preferences;
	private ModelEditor modelEditor;
	private final UndoActionListener undoActionListener;
	private final Vertex locationCalculator = new Vertex(0, 0, 0);
	private final ModelView modelView;
	private SelectionView selectionView;
	private final Graphics2DToModelElementRendererAdapter rendererAdapter;
	private final ActiveViewportWatcher activeViewportWatcher;
	private final PrimitiveShape shape;
	private final PrimitiveOptions options;
	private DrawingState drawingState = DrawingState.NOTHING;
	private Point2D.Double mouseStart;
	private Point2D.Double lastMousePoint;
	private GenericMoveAction action;
	private double lastHeightModeZ = 0;
	private double firstHeightModeZ = 0;

	public DrawPrimitiveActivity(final ProgramPreferences preferences, final UndoActionListener undoActionListener,
			final ModelEditor modelEditor, final ModelView modelView, final SelectionView selectionView,
			final ActiveViewportWatcher activeViewportWatcher, final PrimitiveShape shape,
			final PrimitiveOptions options) {
		this.preferences = preferences;
		this.undoActionListener = undoActionListener;
		this.modelEditor = modelEditor;
		this.modelView = modelView;
		this.selectionView = selectionView;
		this.activeViewportWatcher = activeViewportWatcher;
		this.shape = shape;
		this.options = options;
		this.rendererAdapter = new Graphics2DToModelElementRendererAdapter(preferences.getVertexSize(), preferences);
	}

	@Override
	public void onSelectionChanged(final SelectionView newSelection) {
		this.selectionView = newSelection;
	}

	@Override
	public void modelChanged() {
	}

	@Override
	public void modelEditorChanged(final ModelEditor newModelEditor) {
		this.modelEditor = newModelEditor;
	}

	@Override
	public void viewportChanged(final CursorManager cursorManager) {
	}

	private Point2D.Double geometryPoint(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		locationCalculator.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(e.getX()));
		locationCalculator.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(e.getY()));
		locationCalculator.setCoord(CoordinateSystem.Util.getUnusedXYZ(coordinateSystem), 0);
		return new Point2D.Double(locationCalculator.getCoord(coordinateSystem.getPortFirstXYZ()),
				locationCalculator.getCoord(coordinateSystem.getPortSecondXYZ()));
	}

	@Override
	public void mousePressed(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		if (drawingState == DrawingState.NOTHING) {
			mouseStart = geometryPoint(e, coordinateSystem);
			drawingState = DrawingState.WANT_BEGIN_BASE;
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		if (drawingState == DrawingState.BASE) {
			if (action == null) {
				drawingState = DrawingState.NOTHING;
			} else if (shape.hasHeightPhase()) {
				lastHeightModeZ = coordinateSystem.geomY(e.getY());
				firstHeightModeZ = lastHeightModeZ;
				drawingState = DrawingState.HEIGHT;
			} else {
				finish();
			}
		} else if (drawingState == DrawingState.HEIGHT) {
			finish();
		} else if (drawingState == DrawingState.WANT_BEGIN_BASE) {
			drawingState = DrawingState.NOTHING;
		}
	}

	private void finish() {
		undoActionListener.pushAction(action);
		action = null;
		drawingState = DrawingState.NOTHING;
	}

	@Override
	public void mouseMoved(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		mouseDragged(e, coordinateSystem);
	}

	@Override
	public void mouseDragged(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		if ((drawingState == DrawingState.WANT_BEGIN_BASE) || (drawingState == DrawingState.BASE)) {
			drawingState = DrawingState.BASE;
			updateBase(geometryPoint(e, coordinateSystem), coordinateSystem.getPortFirstXYZ(),
					coordinateSystem.getPortSecondXYZ());
		} else if (drawingState == DrawingState.HEIGHT) {
			final double heightModeZ = coordinateSystem.geomY(e.getY());
			if (Math.abs(heightModeZ - firstHeightModeZ - 1) > 0.1) {
				action.updateTranslation(0, 0, heightModeZ - lastHeightModeZ);
			}
			lastHeightModeZ = heightModeZ;
		}
	}

	private void updateBase(final Point2D.Double mouseEnd, final byte dim1, final byte dim2) {
		if ((Math.abs(mouseEnd.x - mouseStart.x) < 0.1) || (Math.abs(mouseEnd.y - mouseStart.y) < 0.1)) {
			return;
		}
		if (action == null) {
			final Viewport viewport = activeViewportWatcher.getViewport();
			final Vertex facingVector = viewport == null ? new Vertex(0, 0, 1) : viewport.getFacingVector();
			try {
				action = modelEditor.addPrimitive(shape, options, mouseStart.x, mouseStart.y, mouseEnd.x, mouseEnd.y,
						dim1, dim2, facingVector);
			} catch (final WrongModeException exc) {
				drawingState = DrawingState.NOTHING;
				JOptionPane.showMessageDialog(null, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		} else {
			action.updateTranslation(mouseEnd.x - lastMousePoint.x, mouseEnd.y - lastMousePoint.y, 0);
		}
		lastMousePoint = mouseEnd;
	}

	@Override
	public void render(final Graphics2D g, final CoordinateSystem coordinateSystem, final RenderModel renderModel) {
	}

	@Override
	public void renderStatic(final Graphics2D g, final CoordinateSystem coordinateSystem) {
		selectionView.renderSelection(rendererAdapter.reset(g, coordinateSystem), coordinateSystem, modelView,
				preferences);
	}

	@Override
	public boolean isEditing() {
		return false;
	}

	private enum DrawingState {
		NOTHING, WANT_BEGIN_BASE, BASE, HEIGHT
	}
}
