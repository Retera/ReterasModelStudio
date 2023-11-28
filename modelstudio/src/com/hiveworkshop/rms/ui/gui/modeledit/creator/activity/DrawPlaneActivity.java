package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.DrawGeometryAction;
import com.hiveworkshop.rms.editor.model.util.Mesh;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.util.MouseEventHelpers;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;

public class DrawPlaneActivity extends DrawActivity {
	private DrawingState drawingState = DrawingState.NOTHING;
	private int numSegsX;
	private int numSegsY;
	Vec3 scale = new Vec3(1,1,1);

	public DrawPlaneActivity(ModelHandler modelHandler,
	                         ModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager);
	}
	public DrawPlaneActivity(ModelHandler modelHandler,
	                         ModelEditorManager modelEditorManager,
	                         ModelEditorActionType3 lastEditorType) {
		super(modelHandler, modelEditorManager, lastEditorType);
	}

	public DrawPlaneActivity setNumSegs(int numSegsX, int numSegsY) {
		this.numSegsX = numSegsX;
		this.numSegsY = numSegsY;
		return this;
	}

	@Override
	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (MouseEventHelpers.matches(e, getModify(), getSnap())) {
			Vec2 point = getPoint(e);
			scale.set(Vec3.ZERO);
			if (drawingState == DrawingState.NOTHING) {
				mouseStartPoint.set(point);
				this.inverseViewProjectionMatrix.set(viewProjectionMatrix).invert();
				this.viewProjectionMatrix.set(viewProjectionMatrix);
				setHalfScreenXY();

				drawingState = DrawingState.WANT_BEGIN_BASE;
				Mesh mesh = ModelUtils.getPlane(numSegsX, numSegsY);
				UndoAction setupAction = getSetupAction(mesh.getVertices(), mesh.getTriangles());

				Mat4 rotMat = getRotMat();
				startPoint3d.set(get3DPoint(mouseStartPoint));
				transformAction = new DrawGeometryAction("Draw Plane", startPoint3d, rotMat, mesh.getVertices(), setupAction, null).doSetup();
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
			drawingState = DrawingState.NOTHING;
		}
	}


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

		}
		lastMousePoint.set(mouseEnd);

	}

	private enum DrawingState {
		NOTHING, WANT_BEGIN_BASE, BASE
	}
}
