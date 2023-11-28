package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.DrawGeometryAction;
import com.hiveworkshop.rms.editor.actions.mesh.AddTriangleAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.util.MouseEventHelpers;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class DrawFaceActivity extends DrawActivity {
	Vec3 scale = new Vec3(1,1,1);

	ArrayList<GeosetVertex> vertices = new ArrayList<>();
	protected boolean isActing = false;


	public DrawFaceActivity(ModelHandler modelHandler,
	                        ModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager);
	}
	public DrawFaceActivity(ModelHandler modelHandler,
	                        ModelEditorManager modelEditorManager,
	                        ModelEditorActionType3 lastEditorType) {
		super(modelHandler, modelEditorManager, lastEditorType);
	}

	@Override
	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (MouseEventHelpers.matches(e, getModify(), getSnap())) {
			Vec2 point = getPoint(e);
			scale.set(Vec3.ZERO);
			if (transformAction == null) {
				isActing = true;
//			if (modelView.getSelectedVertices().size() <= 2) {
//          //need to check that all verts is in the same geoset, and then use that geoset
//				vertices.addAll(modelView.getSelectedVertices());
//			}
				mouseStartPoint.set(point);
				this.inverseViewProjectionMatrix.set(viewProjectionMatrix).invert();
				this.viewProjectionMatrix.set(viewProjectionMatrix);
				setHalfScreenXY();
				GeosetVertex geosetVertex = new GeosetVertex(Vec3.ZERO, Vec3.Z_AXIS);
				geosetVertex.addTVertex(new Vec2(0, 0));
				vertices.add(geosetVertex);
				Set<GeosetVertex> vertexSet = Collections.singleton(geosetVertex);
				UndoAction setupAction = getSetupAction(vertexSet, Collections.emptySet()).redo();
				if (vertices.size() == 3) {
					Triangle triangle = new Triangle(vertices.get(0), vertices.get(1), vertices.get(2)).addToVerts();
					AddTriangleAction addTriangleAction = new AddTriangleAction(geosetVertex.getGeoset(), Collections.singleton(triangle));
					setupAction = new CompoundAction("Draw Face",  changeListener::geosetsUpdated, setupAction, addTriangleAction.redo());
				}

				Mat4 rotMat = getRotMat();
				startPoint3d.set(get3DPoint(mouseStartPoint));
				transformAction = new DrawGeometryAction("Draw Face", startPoint3d, rotMat, vertexSet, setupAction, null);
				scale.set(0, 0, 0);
				transformAction.setScale(scale);

			}
			lastMousePoint.set(point);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (transformAction != null) {
			undoManager.pushAction(transformAction);
			transformAction = null;
			isActing = false;
			if (vertices.size() == 3) {
				vertices.remove(0);
			}
		}
	}

//	@Override
//	public void mouseMoved(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
//		mouseDragged(e, viewProjectionMatrix, sizeAdj);
//	}

	@Override
	public void mouseDragged(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		Vec2 mouseEnd = getPoint(e);
		if (transformAction != null) {

			float xDist3d = (mouseEnd.x - mouseStartPoint.x)*halfScreenX;
			float yDist3d = (mouseEnd.y - mouseStartPoint.y)*halfScreenY;
			float avgDist3d = (Math.abs(xDist3d)+Math.abs(yDist3d))/2f;

//			if (MouseEventHelpers.hasModifier(modifiersEx, MouseEvent.CTRL_DOWN_MASK)) {
//				scale.set(yDist3d, xDist3d, 0);
//			} else
			if (uniformSizeModifier(e)) {
				scale.set(Math.copySign(avgDist3d, xDist3d), Math.copySign(avgDist3d, yDist3d), 0);
			} else {
				scale.set(xDist3d, yDist3d, 0);
			}

			transformAction.setScale(scale);
			transformAction.setTranslation(get3DPoint(getPoint(e)));

		}
		lastMousePoint.set(mouseEnd);

	}
}
