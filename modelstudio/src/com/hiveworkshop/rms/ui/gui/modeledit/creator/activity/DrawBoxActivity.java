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

public class DrawBoxActivity extends DrawActivity {
	private DrawingState drawingState = DrawingState.NOTHING;

	private int numSegsX;
	private int numSegsY;
	private int numSegsZ;
	protected final Vec3 moveVector = new Vec3();
	protected float zDepth = 0;

	private final Vec3 scale = new Vec3(1,1,1);

	public DrawBoxActivity(ModelHandler modelHandler,
	                       ModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager);
	}
	public DrawBoxActivity(ModelHandler modelHandler,
	                       ModelEditorManager modelEditorManager,
	                       ModelEditorActionType3 lastEditorType) {
		super(modelHandler, modelEditorManager, lastEditorType);
	}

	public DrawBoxActivity setNumSegs(int numSegsX, int numSegsY, int numSegsZ) {
		this.numSegsX = numSegsX;
		this.numSegsY = numSegsY;
		this.numSegsZ = numSegsZ;
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
				Mesh mesh = ModelUtils.getBoxMesh2(numSegsX, numSegsY, numSegsZ);
				UndoAction setupAction = getSetupAction(mesh.getVertices(), mesh.getTriangles());


				startPoint3d.set(get3DPoint(mouseStartPoint));

				Mat4 rotMat = getRotMat();
				transformAction = new DrawGeometryAction("Draw Box", startPoint3d, rotMat, mesh.getVertices(), setupAction, null).doSetup();
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

			if (uniformSizeModifier(e)) {
				scale.set(Math.copySign(avgDist3d, xDist3d), Math.copySign(avgDist3d, yDist3d), avgDist3d);
			} else {
				scale.set(xDist3d, yDist3d, avgDist3d);
			}

			transformAction.setScale(scale);

		}
		lastMousePoint.set(mouseEnd);

	}

	private enum DrawingState {
		NOTHING, WANT_BEGIN_BASE, BASE, HEIGHT
    }
}
