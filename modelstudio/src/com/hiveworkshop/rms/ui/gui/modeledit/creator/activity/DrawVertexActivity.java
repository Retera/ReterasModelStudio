package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.DrawGeometryAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.util.MouseEventHelpers;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Set;

public class DrawVertexActivity extends DrawActivity {

	public DrawVertexActivity(ModelHandler modelHandler,
	                          ModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager);
	}

	public DrawVertexActivity(ModelHandler modelHandler,
	                          ModelEditorManager modelEditorManager,
	                          ModelEditorActionType3 lastEditorType) {
		super(modelHandler, modelEditorManager, lastEditorType);
	}

	@Override
	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (MouseEventHelpers.matches(e, getModify(), getSnap())) {
			Vec2 point = getPoint(e);
			mouseStartPoint.set(point);
			this.inverseViewProjectionMatrix.set(viewProjectionMatrix).invert();
			this.viewProjectionMatrix.set(viewProjectionMatrix);


			GeosetVertex geosetVertex = new GeosetVertex(Vec3.ZERO, Vec3.Z_AXIS);
			geosetVertex.addTVertex(new Vec2(0, 0));
			Set<GeosetVertex> vertices = Collections.singleton(geosetVertex);
			UndoAction setupAction = getSetupAction(vertices, Collections.emptySet());

			Mat4 rotMat = getRotMat();
			startPoint3d.set(get3DPoint(mouseStartPoint));
			transformAction = new DrawGeometryAction("Draw Vertex", startPoint3d, rotMat, vertices, setupAction, null).doSetup();
			transformAction.setScale(Vec3.ZERO);

		}
	}

	@Override
	public void mouseDragged(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (transformAction != null) {
			transformAction.setTranslation(get3DPoint(getPoint(e)));
		}
	}

}
