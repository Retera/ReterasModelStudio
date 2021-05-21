package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.CursorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.actions.DrawPlaneAction;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.actions.NewGeosetAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.CompoundMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.DoNothingMoveActionAdapter;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

public class DrawPlaneActivity implements ModelEditorViewportActivity {

	private final ProgramPreferences preferences;
	private final UndoManager undoManager;
	private final Vec3 locationCalculator = new Vec3(0, 0, 0);
	private final ModelView modelView;
	private final ModelElementRenderer modelElementRenderer;
	private final ViewportListener viewportListener;
	private ModelEditor modelEditor;
	private SelectionView selectionView;
	private DrawingState drawingState = DrawingState.NOTHING;
	private Vec2 mouseStart;
	private Vec2 lastMousePoint;
	private GenericMoveAction planeAction;
	private int numSegsX;
	private int numSegsY;
	private final ModelHandler modelHandler;
	ModelEditorManager modelEditorManager;

	public DrawPlaneActivity(ModelHandler modelHandler,
	                         ModelEditorManager modelEditorManager,
	                         ViewportListener viewportListener,
	                         int numSegsX, int numSegsY, int numSegsZ) {
		this.modelHandler = modelHandler;
		this.preferences = ProgramGlobals.getPrefs();
		this.modelEditorManager = modelEditorManager;
		this.undoManager = modelHandler.getUndoManager();
		this.modelEditor = modelEditorManager.getModelEditor();
		this.modelView = modelHandler.getModelView();
		this.selectionView = modelEditorManager.getSelectionView();
		this.viewportListener = viewportListener;
		this.numSegsX = numSegsX;
		this.numSegsY = numSegsY;
		modelElementRenderer = new ModelElementRenderer(ProgramGlobals.getPrefs().getVertexSize());
	}

	public void setNumSegsX(int numSegsX) {
		this.numSegsX = numSegsX;
	}

	public void setNumSegsY(int numSegsY) {
		this.numSegsY = numSegsY;
	}

	@Override
	public void onSelectionChanged(SelectionView newSelection) {
		selectionView = newSelection;
	}

	@Override
	public void modelEditorChanged(ModelEditor newModelEditor) {
		modelEditor = newModelEditor;
	}

	@Override
	public void viewportChanged(CursorManager cursorManager) {
	}

	@Override
	public void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (drawingState == DrawingState.NOTHING) {
			locationCalculator.set(CoordSysUtils.convertToVec3(coordinateSystem, e.getPoint()));
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
			locationCalculator.set(CoordSysUtils.convertToVec3(coordinateSystem, e.getPoint()));

			Vec2 mouseEnd = locationCalculator.getProjected(coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
			updateBase(mouseEnd, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		}
	}

	public void updateBase(Vec2 mouseEnd, byte dim1, byte dim2) {
		if (Math.abs(mouseEnd.x - mouseStart.x) >= 0.1 && Math.abs(mouseEnd.y - mouseStart.y) >= 0.1) {
			if (planeAction == null) {
				Viewport viewport = viewportListener.getViewport();
				Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
				try {
//					planeAction = modelEditor.addPlane(mouseStart, mouseEnd, dim1, dim2, facingVector, numSegsX, numSegsY);

					Geoset solidWhiteGeoset = getSolidWhiteGeoset();

					DrawPlaneAction drawVertexAction = new DrawPlaneAction(mouseStart, mouseEnd, dim1, dim2, facingVector, numSegsX, numSegsY, solidWhiteGeoset);

					if (!modelView.getModel().contains(solidWhiteGeoset)) {
						NewGeosetAction newGeosetAction = new NewGeosetAction(solidWhiteGeoset, modelView.getModel(), modelEditorManager.getStructureChangeListener());
						planeAction = new CompoundMoveAction("Add Plane", Arrays.asList(new DoNothingMoveActionAdapter(newGeosetAction), drawVertexAction));
					} else {
						planeAction = drawVertexAction;
					}
					planeAction.redo();


				} catch (WrongModeException exc) {
					drawingState = DrawingState.NOTHING;
					JOptionPane.showMessageDialog(null, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				planeAction.updateTranslation(mouseEnd.x - lastMousePoint.x, mouseEnd.y - lastMousePoint.y, 0);
			}
			lastMousePoint = mouseEnd;
		}
	}

	public Geoset getSolidWhiteGeoset() {
		List<Geoset> geosets = modelView.getModel().getGeosets();
		Geoset solidWhiteGeoset = null;
		for (Geoset geoset : geosets) {
			Layer firstLayer = geoset.getMaterial().firstLayer();
			if ((geoset.getMaterial() != null) && (firstLayer != null)
					&& (firstLayer.getFilterMode() == MdlxLayer.FilterMode.NONE)
					&& "Textures\\white.blp".equalsIgnoreCase(firstLayer.getTextureBitmap().getPath())) {
				solidWhiteGeoset = geoset;
			}
		}

		if (solidWhiteGeoset == null) {
			solidWhiteGeoset = new Geoset();
			solidWhiteGeoset.setMaterial(new Material(new Layer("None", new Bitmap("Textures\\white.blp"))));
		}
		return solidWhiteGeoset;
	}

	@Override
	public void render(Graphics2D g, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		if (!isAnimated) {
			modelElementRenderer.reset(g, coordinateSystem, modelHandler.getRenderModel(), false);
			selectionView.renderSelection(modelElementRenderer, coordinateSystem, modelView);
		}
	}

	@Override
	public boolean isEditing() {
		return false;
	}

	private enum DrawingState {
		NOTHING, WANT_BEGIN_BASE, BASE
	}
}
