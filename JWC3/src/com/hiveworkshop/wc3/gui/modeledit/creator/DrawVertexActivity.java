package com.hiveworkshop.wc3.gui.modeledit.creator;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.animedit.WrongModeException;
import com.hiveworkshop.wc3.gui.modeledit.ActiveViewportWatcher;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.Viewport;
import com.hiveworkshop.wc3.gui.modeledit.activity.CursorManager;
import com.hiveworkshop.wc3.gui.modeledit.activity.Graphics2DToModelElementRendererAdapter;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.ViewportActivity;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.mdl.RenderModel;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class DrawVertexActivity implements ViewportActivity {

	private Point lastMousePoint;
	private final ProgramPreferences preferences;
	private ModelEditor modelEditor;
	private final UndoActionListener undoActionListener;
	private final Vertex locationCalculator = new Vertex(0, 0, 0);
	private final ModelView modelView;
	private SelectionView selectionView;
	private final Graphics2DToModelElementRendererAdapter graphics2dToModelElementRendererAdapter;
	private final ActiveViewportWatcher activeViewportWatcher;

	public DrawVertexActivity(final ProgramPreferences preferences, final UndoActionListener undoActionListener,
			final ModelEditor modelEditor, final ModelView modelView, final SelectionView selectionView,
			final ActiveViewportWatcher activeViewportWatcher) {
		this.preferences = preferences;
		this.undoActionListener = undoActionListener;
		this.modelEditor = modelEditor;
		this.modelView = modelView;
		this.selectionView = selectionView;
		this.activeViewportWatcher = activeViewportWatcher;
		this.graphics2dToModelElementRendererAdapter = new Graphics2DToModelElementRendererAdapter(
				preferences.getVertexSize(), preferences);
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

	@Override
	public void mousePressed(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		locationCalculator.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(e.getX()));
		locationCalculator.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(e.getY()));
		locationCalculator.setCoord(CoordinateSystem.Util.getUnusedXYZ(coordinateSystem), 0);
		try {
			final Viewport viewport = activeViewportWatcher.getViewport();
			final Vertex facingVector = viewport == null ? new Vertex(0, 0, 1) : viewport.getFacingVector();
			final UndoAction action = modelEditor.addVertex(locationCalculator.x, locationCalculator.y,
					locationCalculator.z, facingVector);
			undoActionListener.pushAction(action);
		} catch (final WrongModeException exc) {
			JOptionPane.showMessageDialog(null, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e, final CoordinateSystem coordinateSystem) {

	}

	@Override
	public void mouseMoved(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		lastMousePoint = e.getPoint();
	}

	@Override
	public void mouseDragged(final MouseEvent e, final CoordinateSystem coordinateSystem) {

	}

	@Override
	public void render(final Graphics2D g, final CoordinateSystem coordinateSystem, final RenderModel renderModel) {
	}

	@Override
	public void renderStatic(final Graphics2D g, final CoordinateSystem coordinateSystem) {
		selectionView.renderSelection(graphics2dToModelElementRendererAdapter.reset(g, coordinateSystem),
				coordinateSystem, modelView, preferences);
		g.setColor(preferences.getVertexColor());
		g.fillRect(lastMousePoint.x, lastMousePoint.y, 3, 3);

	}

	@Override
	public boolean isEditing() {
		return false;
	}

}
