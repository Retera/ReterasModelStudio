package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
import com.hiveworkshop.rms.editor.actions.addactions.DrawVertexAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DrawFaceActivity extends ViewportActivity {

	private Point lastMousePoint;
	Set<GeosetVertex> createdVerts = new HashSet<>();

	public DrawFaceActivity(ModelHandler modelHandler,
	                        ModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager);
	}

	@Override
	public void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem) {
		Vec3 locationCalculator = new Vec3(0, 0, 0);
		locationCalculator.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(e.getX()));
		locationCalculator.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(e.getY()));
		locationCalculator.setCoord(coordinateSystem.getUnusedXYZ(), 0);
		try {
//			Viewport viewport = viewportListener.getViewport();
//			Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
			Vec3 facingVector = new Vec3(0, 0, 1); // todo make this work with CameraHandler

			Geoset solidWhiteGeoset = getSolidWhiteGeoset();


			GeosetVertex geosetVertex = new GeosetVertex(locationCalculator, new Vec3(facingVector));
			geosetVertex.setGeoset(solidWhiteGeoset);
			geosetVertex.addTVertex(new Vec2(0, 0));
			UndoAction action2;
			DrawVertexAction drawVertexAction = new DrawVertexAction(geosetVertex);
			if (!modelView.getModel().contains(solidWhiteGeoset)) {
				AddGeosetAction addGeosetAction = new AddGeosetAction(solidWhiteGeoset, modelView, ModelStructureChangeListener.changeListener);
				action2 = new CompoundAction("add vertex", Arrays.asList(addGeosetAction, drawVertexAction));
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
			if ((geoset.getMaterial() != null) && (firstLayer != null)
					&& (firstLayer.getFilterMode() == FilterMode.NONE)
					&& "Textures\\white.blp".equalsIgnoreCase(firstLayer.getTextureBitmap().getPath())) {
				solidWhiteGeoset = geoset;
			}
		}

		if (solidWhiteGeoset == null) {
			solidWhiteGeoset = new Geoset();
			solidWhiteGeoset.setMaterial(new Material(new Layer(new Bitmap("Textures\\white.blp"))));
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
//			g.setColor(preferences.getVertexColor());
			g.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.VERTEX));
			if (lastMousePoint != null) {
				g.fillRect(lastMousePoint.x, lastMousePoint.y, 3, 3);
			}
		}
	}

}
