package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.CursorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.actions.DrawBoxAction;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.actions.NewGeosetAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.CompoundMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.DoNothingMoveActionAdapter;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

public class DrawBoxActivity extends ViewportActivity {

	private final Vec3 locationCalculator = new Vec3(0, 0, 0);

	private final ModelElementRenderer modelElementRenderer;
	private final ViewportListener viewportListener;

	private DrawingState drawingState = DrawingState.NOTHING;
	private Vec2 mouseStart;
	private Vec2 lastMousePoint;
	private GenericMoveAction boxAction;
	private int numSegsX;
	private int numSegsY;
	private int numSegsZ;
	private double lastHeightModeZ = 0;
	private double firstHeightModeZ = 0;
	ModelEditorManager modelEditorManager;

	public DrawBoxActivity(ModelHandler modelHandler,
	                       ModelEditorManager modelEditorManager,
	                       ViewportListener viewportListener,
	                       int numSegsX, int numSegsY, int numSegsZ) {
		this.modelHandler = modelHandler;
		this.undoManager = modelHandler.getUndoManager();
		this.modelEditorManager = modelEditorManager;
		this.modelEditor = modelEditorManager.getModelEditor();
		this.modelView = modelHandler.getModelView();
		this.selectionManager = modelEditorManager.getSelectionView();
		this.viewportListener = viewportListener;
		this.numSegsX = numSegsX;
		this.numSegsY = numSegsY;
		this.numSegsZ = numSegsZ;
		modelElementRenderer = new ModelElementRenderer(ProgramGlobals.getPrefs().getVertexSize());
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
	public void onSelectionChanged(AbstractSelectionManager newSelection) {
		selectionManager = newSelection;
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

			locationCalculator.set(CoordSysUtils.convertToVec3(coordinateSystem, e.getPoint()));
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
				Viewport viewport = viewportListener.getViewport();
				Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
				try {
//					boxAction = modelEditor.addBox(mouseStart, mouseEnd, dim1, dim2, facingVector, numSegsX, numSegsY, numSegsZ);


					Geoset solidWhiteGeoset = getSolidWhiteGeoset();

					DrawBoxAction drawVertexAction = new DrawBoxAction(mouseStart, mouseEnd, dim1, dim2, facingVector, numSegsX, numSegsY, numSegsZ, solidWhiteGeoset);

					if (!modelView.getModel().contains(solidWhiteGeoset)) {
						NewGeosetAction newGeosetAction = new NewGeosetAction(solidWhiteGeoset, modelView.getModel(), modelEditorManager.getStructureChangeListener());
						boxAction = new CompoundMoveAction("Add Box", Arrays.asList(new DoNothingMoveActionAdapter(newGeosetAction), drawVertexAction));
					} else {
						boxAction = drawVertexAction;
					}
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
//			selectionView.renderSelection(modelElementRenderer, coordinateSystem, modelView);
		}
	}

	@Override
	public boolean isEditing() {
		return false;
	}

	private enum DrawingState {
		NOTHING, WANT_BEGIN_BASE, BASE, HEIGHT
    }
}
