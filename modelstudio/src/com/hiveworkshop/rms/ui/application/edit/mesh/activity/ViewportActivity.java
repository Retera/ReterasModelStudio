package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.AbstractCamera;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.preferences.Nav3DMouseAction;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public abstract class ViewportActivity implements SelectionListener {
	protected ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
	protected final Mat4 viewProjectionMatrix = new Mat4();
	protected final Mat4 inverseViewProjectionMatrix = new Mat4();
	protected final Mat4 rotMat = new Mat4();
	protected ProgramPreferences prefs;
	protected AbstractTransformAction transformAction;

	protected Consumer<Cursor> cursorManager;
	protected ModelEditor modelEditor;
	protected ModelHandler modelHandler;
	protected UndoManager undoManager;
	protected ModelView modelView;
	protected final Vec2 mouseStartPoint = new Vec2();
	protected final Vec2 lastMousePoint = new Vec2();
	protected float sizeAdj;
	protected AbstractSelectionManager selectionManager;
	protected AbstractModelEditorManager modelEditorManager;
	protected final Vec3 tempVec3 = new Vec3();
	protected final Vec2 tempVec2 = new Vec2();
	protected final Vec2 relViewPoint = new Vec2();

	protected final Vec3 tempVecX = new Vec3();
	protected final Vec3 tempVecY = new Vec3();
	protected final Vec3 tempVecZ = new Vec3();
	protected final Vec3 point3d = new Vec3();

	public ViewportActivity(ModelHandler modelHandler, AbstractModelEditorManager modelEditorManager) {
		this.modelHandler = modelHandler;
		this.modelView = modelHandler.getModelView();
		this.undoManager = modelHandler.getUndoManager();
		this.modelEditorManager = modelEditorManager;
		this.modelEditor = modelEditorManager.getModelEditor();
		this.selectionManager = modelEditorManager.getSelectionView();
		prefs = ProgramGlobals.getPrefs();
	}

	@Override
	public void onSelectionChanged(AbstractSelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void onActivityEnded() {
	}

	public void modelEditorChanged(ModelEditor newModelEditor) {
		modelEditor = newModelEditor;
	}

	public void viewportChanged(Consumer<Cursor> cursorManager) {
		this.cursorManager = cursorManager;
	}

	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
	}

	public void mouseReleased(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
	}

	public void mouseMoved(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
	}

	public void mouseDragged(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
	}

	public void render(Graphics2D g, AbstractCamera coordinateSystem, RenderModel renderModel, boolean isAnimated) {
	}

	public boolean isEditing() {
		return false;
	}

	public boolean selectionNeeded() {
		return true;
	}

	protected Mat4 getRotMat() {
		tempVec3.set(Vec3.ZERO  ).transform(inverseViewProjectionMatrix, 1, true);

		tempVecX.set(Vec3.X_AXIS).transform(inverseViewProjectionMatrix, 1, true);
		tempVecY.set(Vec3.Y_AXIS).transform(inverseViewProjectionMatrix, 1, true);
		tempVecZ.set(Vec3.NEGATIVE_Z_AXIS).transform(inverseViewProjectionMatrix, 1, true);

		tempVecX.sub(tempVec3).normalize();
		tempVecY.sub(tempVec3).normalize();
		tempVecZ.sub(tempVec3).normalize();

		rotMat.set(tempVecX, tempVecY, tempVecZ);
		return rotMat;
	}

	protected Vec2 getPoint(MouseEvent e) {
		Component component = e.getComponent();
		float xRatio = (2.0f * (float) e.getX() / (float) component.getWidth()) - 1.0f;
		float yRatio = 1.0f - (2.0f * (float) e.getY() / (float) component.getHeight());
		return relViewPoint.set(xRatio, yRatio);
	}

	protected Integer getSelect() {
		return prefs.getNav3DMousePrefs().getKeyStroke(Nav3DMouseAction.SELECT);
	}
	protected Integer getAddSel() {
		return prefs.getNav3DMousePrefs().getKeyStroke(Nav3DMouseAction.ADD_SELECT_MODIFIER);
	}
	protected Integer getRemSel() {
		return prefs.getNav3DMousePrefs().getKeyStroke(Nav3DMouseAction.REMOVE_SELECT_MODIFIER);
	}
	protected Integer getModify() {
		return prefs.getNav3DMousePrefs().getKeyStroke(Nav3DMouseAction.MODIFY);
	}
	protected Integer getSnap() {
		return prefs.getNav3DMousePrefs().getKeyStroke(Nav3DMouseAction.SNAP_TRANSFORM_MODIFIER);
	}
}
