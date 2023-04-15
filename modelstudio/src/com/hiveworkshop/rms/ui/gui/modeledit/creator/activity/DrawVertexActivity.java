package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.DrawGeometryAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.AbstractCamera;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.ui.util.MouseEventHelpers;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Set;

public class DrawVertexActivity extends ViewportActivity {
	float[] halfScreenXY;
	float halfScreenX;
	float halfScreenY;
	private final Vec3 startPoint3d = new Vec3();

	private ModelEditorActionType3 lastEditorType;


	public DrawVertexActivity(ModelHandler modelHandler,
	                          ModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager);
	}

	public DrawVertexActivity(ModelHandler modelHandler,
	                          ModelEditorManager modelEditorManager,
	                          ModelEditorActionType3 lastEditorType) {
		super(modelHandler, modelEditorManager);
		this.lastEditorType = lastEditorType;
	}


	@Override
	public boolean selectionNeeded() {
		return false;
	}


	@Override
	public void render(Graphics2D g, AbstractCamera coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		if (!isAnimated) {
			g.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.VERTEX));
			if (lastMousePoint != null) {
				g.fillRect((int) lastMousePoint.x, (int) lastMousePoint.y, 3, 3);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		setPrefs();
		if (MouseEventHelpers.matches(e, prefs.getModifyMouseButton(), prefs.getSnapTransformModifier())) {
			Vec2 point = getPoint(e);
			mouseStartPoint.set(point);
			this.inverseViewProjectionMatrix.set(viewProjectionMatrix).invert();
			this.viewProjectionMatrix.set(viewProjectionMatrix);
			halfScreenXY = halfScreenXY();
			halfScreenX = halfScreenXY[0];
			halfScreenY = halfScreenXY[1];


			GeosetVertex geosetVertex = new GeosetVertex(Vec3.ZERO, Vec3.Z_AXIS);
			geosetVertex.addTVertex(new Vec2(0, 0));
			Set<GeosetVertex> vertices = Collections.singleton(geosetVertex);
			UndoAction setupAction = getSetupAction(vertices, Collections.emptySet());

			Mat4 rotMat = getRotMat();
			startPoint3d.set(get3DPoint(mouseStartPoint));
			DrawGeometryAction geometryAction = new DrawGeometryAction("Draw Vertex", startPoint3d, rotMat, vertices, setupAction, null).doSetup();
			geometryAction.setScale(Vec3.ZERO);

			undoManager.pushAction(geometryAction);
			lastMousePoint.set(point);
			if (lastEditorType != null && !MouseEventHelpers.hasModifier(e.getModifiersEx(), MouseEvent.CTRL_DOWN_MASK)) {
				System.out.println("returning to prev action type!");
				ProgramGlobals.getCurrentModelPanel().setEditorActionType(lastEditorType);
			} else {
				System.out.println("keep draw vertices!");
			}
		}
	}


	@Override
	public void mouseMoved(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		lastMousePoint.set(e.getPoint().x, e.getPoint().y);
	}
}
