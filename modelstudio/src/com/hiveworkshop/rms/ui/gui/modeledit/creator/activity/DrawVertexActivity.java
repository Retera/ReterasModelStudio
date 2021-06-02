package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.DrawVertexAction;
import com.hiveworkshop.rms.editor.actions.addactions.NewGeosetAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

public class DrawVertexActivity extends ViewportActivity {

	private Point lastMousePoint;
	private final ViewportListener viewportListener;

	public DrawVertexActivity(ModelHandler modelHandler,
	                          ModelEditorManager modelEditorManager,
	                          ViewportListener viewportListener) {
		super(modelHandler, modelEditorManager);
		this.viewportListener = viewportListener;
	}

	@Override
	public void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem) {
		Vec3 locationCalculator = new Vec3(0, 0, 0);
		locationCalculator.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(e.getX()));
		locationCalculator.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(e.getY()));
		locationCalculator.setCoord(coordinateSystem.getUnusedXYZ(), 0);
		try {
			Viewport viewport = viewportListener.getViewport();
			Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();

			Geoset solidWhiteGeoset = getSolidWhiteGeoset();


			GeosetVertex geosetVertex = new GeosetVertex(locationCalculator, new Vec3(facingVector));
			geosetVertex.setGeoset(solidWhiteGeoset);
			geosetVertex.addTVertex(new Vec2(0, 0));
			UndoAction action2;
			DrawVertexAction drawVertexAction = new DrawVertexAction(geosetVertex);
			if (!modelView.getModel().contains(solidWhiteGeoset) || !modelView.isEditable(solidWhiteGeoset)) {
				NewGeosetAction newGeosetAction = new NewGeosetAction(solidWhiteGeoset, modelView, modelEditorManager.getStructureChangeListener());
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

	public Geoset getSolidWhiteGeoset() {
		List<Geoset> geosets = modelView.getModel().getGeosets();
		Geoset solidWhiteGeoset = null;
		for (Geoset geoset : geosets) {
			Layer firstLayer = geoset.getMaterial().firstLayer();
			if (modelView.isEditable(solidWhiteGeoset)
					&& geoset.getMaterial() != null
					&& (firstLayer != null)
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
	public void mouseMoved(MouseEvent e, CoordinateSystem coordinateSystem) {
		lastMousePoint = e.getPoint();
	}

	@Override
	public void render(Graphics2D g, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		if (!isAnimated) {
			g.setColor(preferences.getVertexColor());
			if (lastMousePoint != null) {
				g.fillRect(lastMousePoint.x, lastMousePoint.y, 3, 3);
			}
		}
	}

}
