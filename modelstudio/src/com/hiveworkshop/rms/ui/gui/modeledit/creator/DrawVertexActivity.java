package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.CursorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.Graphics2DToModelElementRendererAdapter;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class DrawVertexActivity implements ModelEditorViewportActivity {

	private Point lastMousePoint;
	private final ProgramPreferences preferences;
	private ModelEditor modelEditor;
	private final UndoActionListener undoActionListener;
	private final Vec3 locationCalculator = new Vec3(0, 0, 0);
	private final ModelView modelView;
	private SelectionView selectionView;
	private final Graphics2DToModelElementRendererAdapter graphics2dToModelElementRendererAdapter;
	private final ViewportListener viewportListener;

	public DrawVertexActivity(final ProgramPreferences preferences,
	                          final UndoActionListener undoActionListener,
	                          final ModelEditor modelEditor,
	                          final ModelView modelView,
	                          final SelectionView selectionView,
	                          final ViewportListener viewportListener) {
		this.preferences = preferences;
		this.undoActionListener = undoActionListener;
		this.modelEditor = modelEditor;
		this.modelView = modelView;
		this.selectionView = selectionView;
		this.viewportListener = viewportListener;
		graphics2dToModelElementRendererAdapter = new Graphics2DToModelElementRendererAdapter(preferences.getVertexSize(), preferences);
	}

	@Override
	public void onSelectionChanged(final SelectionView newSelection) {
		selectionView = newSelection;
	}

	@Override
	public void modelChanged() { }

	@Override
	public void modelEditorChanged(final ModelEditor newModelEditor) {
		modelEditor = newModelEditor;
	}

	@Override
	public void viewportChanged(final CursorManager cursorManager) { }

	@Override
	public void mousePressed(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		locationCalculator.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(e.getX()));
		locationCalculator.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(e.getY()));
		locationCalculator.setCoord(CoordinateSystem.Util.getUnusedXYZ(coordinateSystem), 0);
		try {
			final Viewport viewport = viewportListener.getViewport();
			final Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
			final UndoAction action = modelEditor.addVertex(locationCalculator.x, locationCalculator.y, locationCalculator.z, facingVector);
			undoActionListener.pushAction(action);
		} catch (final WrongModeException exc) {
			JOptionPane.showMessageDialog(null, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e, final CoordinateSystem coordinateSystem) { }

	@Override
	public void mouseMoved(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		lastMousePoint = e.getPoint();
	}

	@Override
	public void mouseDragged(final MouseEvent e, final CoordinateSystem coordinateSystem) { }

	@Override
	public void render(final Graphics2D g, final CoordinateSystem coordinateSystem, final RenderModel renderModel) { }

	@Override
	public void renderStatic(final Graphics2D g, final CoordinateSystem coordinateSystem) {
		selectionView.renderSelection(graphics2dToModelElementRendererAdapter.reset(g, coordinateSystem), coordinateSystem, modelView, preferences);
		g.setColor(preferences.getVertexColor());
		if (lastMousePoint != null) {
			g.fillRect(lastMousePoint.x, lastMousePoint.y, 3, 3);
		}
	}

	@Override
	public boolean isEditing() {
		return false;
	}

}
