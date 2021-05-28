package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.actions.DrawVertexAction;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.actions.NewGeosetAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

public class DrawVertexActivity extends ViewportActivity {

	private Point lastMousePoint;
	private final Vec3 locationCalculator = new Vec3(0, 0, 0);
	private final ModelElementRenderer modelElementRenderer;
	private final ViewportListener viewportListener;
	ModelEditorManager modelEditorManager;

	public DrawVertexActivity(ModelHandler modelHandler,
	                          ModelEditorManager modelEditorManager,
	                          ViewportListener viewportListener) {
		this.modelHandler = modelHandler;
		this.modelEditorManager = modelEditorManager;
		this.preferences = ProgramGlobals.getPrefs();
		this.undoManager = modelHandler.getUndoManager();
		this.modelEditor = modelEditorManager.getModelEditor();
		this.modelView = modelHandler.getModelView();
		this.selectionManager = modelEditorManager.getSelectionView();
		this.viewportListener = viewportListener;
		modelElementRenderer = new ModelElementRenderer(ProgramGlobals.getPrefs().getVertexSize());
	}

	@Override
	public void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem) {
		locationCalculator.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(e.getX()));
		locationCalculator.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(e.getY()));
		locationCalculator.setCoord(coordinateSystem.getUnusedXYZ(), 0);
		try {
			Viewport viewport = viewportListener.getViewport();
			Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();

			List<Geoset> geosets = modelView.getModel().getGeosets();
			Geoset solidWhiteGeoset = null;
			for (Geoset geoset : geosets) {
				Layer firstLayer = geoset.getMaterial().firstLayer();
				if ((geoset.getMaterial() != null)
						&& (firstLayer != null)
						&& (firstLayer.getFilterMode() == MdlxLayer.FilterMode.NONE)
						&& "Textures\\white.blp".equalsIgnoreCase(firstLayer.getTextureBitmap().getPath())) {
					solidWhiteGeoset = geoset;
				}
			}
			boolean needsGeosetAction = false;
			if (solidWhiteGeoset == null) {
				solidWhiteGeoset = new Geoset();
				solidWhiteGeoset.setMaterial(new Material(new Layer("None", new Bitmap("Textures\\white.blp"))));
				needsGeosetAction = true;
			}
			GeosetVertex geosetVertex = new GeosetVertex(locationCalculator, new Vec3(facingVector));
			geosetVertex.setGeoset(solidWhiteGeoset);
			geosetVertex.addTVertex(new Vec2(0, 0));
			UndoAction action2;
			DrawVertexAction drawVertexAction = new DrawVertexAction(geosetVertex);
			if (needsGeosetAction) {
				NewGeosetAction newGeosetAction = new NewGeosetAction(solidWhiteGeoset, modelView.getModel(), modelEditorManager.getStructureChangeListener());
				action2 = new CompoundAction("add vertex", Arrays.asList(newGeosetAction, drawVertexAction));
			} else {
				action2 = drawVertexAction;
			}
			action2.redo();

			undoManager.pushAction(action2);
		} catch (WrongModeException exc) {
			JOptionPane.showMessageDialog(null, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, CoordinateSystem coordinateSystem) { }

	@Override
	public void mouseMoved(MouseEvent e, CoordinateSystem coordinateSystem) {
		lastMousePoint = e.getPoint();
	}

	@Override
	public void mouseDragged(MouseEvent e, CoordinateSystem coordinateSystem) { }

	@Override
	public void render(Graphics2D g, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		if (!isAnimated) {
			modelElementRenderer.reset(g, coordinateSystem, modelHandler.getRenderModel(), false);
//			selectionView.renderSelection(modelElementRenderer, coordinateSystem, modelView);
			g.setColor(preferences.getVertexColor());
			if (lastMousePoint != null) {
				g.fillRect(lastMousePoint.x, lastMousePoint.y, 3, 3);
			}
		}
	}

	@Override
	public boolean isEditing() {
		return false;
	}

}
