package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.DrawGeometryAction;
import com.hiveworkshop.rms.editor.model.util.Mesh;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.util.MouseEventHelpers;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;

public class DrawPlaneActivity extends ViewportActivity {
	private DrawingState drawingState = DrawingState.NOTHING;
	private int numSegsX;
	private int numSegsY;
	Vec3 scale = new Vec3(1,1,1);

	float[] halfScreenXY;
	float halfScreenX;
	float halfScreenY;
	private final Vec3 startPoint3d = new Vec3();

	public DrawPlaneActivity(ModelHandler modelHandler,
	                         ModelEditorManager modelEditorManager,
	                         int numSegsX, int numSegsY, int numSegsZ) {
		super(modelHandler, modelEditorManager);
		this.numSegsX = numSegsX;
		this.numSegsY = numSegsY;
	}

	@Override
	public boolean selectionNeeded() {
		return false;
	}

	@Override
	public boolean isEditing() {
		return drawingState != DrawingState.NOTHING;
	}

	public void setNumSegsX(int numSegsX) {
		this.numSegsX = numSegsX;
	}
	public void setNumSegsY(int numSegsY) {
		this.numSegsY = numSegsY;
	}

	@Override
	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		setPrefs();
		if (MouseEventHelpers.matches(e, prefs.getModifyMouseButton(), prefs.getSnapTransformModifier())) {
			Vec2 point = getPoint(e);
			scale.set(Vec3.ZERO);
			if (drawingState == DrawingState.NOTHING) {
				mouseStartPoint.set(point);
				this.inverseViewProjectionMatrix.set(viewProjectionMatrix).invert();
				this.viewProjectionMatrix.set(viewProjectionMatrix);
				halfScreenXY = halfScreenXY();
				halfScreenX = halfScreenXY[0];
				halfScreenY = halfScreenXY[1];


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
	public void mouseMoved(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		mouseDragged(e, viewProjectionMatrix, sizeAdj);
	}

	@Override
	public void mouseDragged(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		setPrefs();
		Vec2 mouseEnd = getPoint(e);
		if (transformAction != null) {

			int modifiersEx = e.getModifiersEx();

			float xDist3d = (mouseEnd.x - mouseStartPoint.x)*halfScreenX;
			float yDist3d = (mouseEnd.y - mouseStartPoint.y)*halfScreenY;
			float avgDist3d = (Math.abs(xDist3d)+Math.abs(yDist3d))/2f;

//			if(MouseEventHelpers.hasModifier(modifiersEx, MouseEvent.CTRL_DOWN_MASK)){
//				scale.set(yDist3d, xDist3d, 0);
//			} else
			if(MouseEventHelpers.hasModifier(modifiersEx, MouseEvent.SHIFT_DOWN_MASK)){
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
