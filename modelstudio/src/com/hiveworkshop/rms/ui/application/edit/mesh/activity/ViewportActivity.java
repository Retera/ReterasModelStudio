package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.actions.mesh.AddGeometryAction;
import com.hiveworkshop.rms.editor.actions.model.material.AddMaterialAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
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
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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
	}

	@Override
	public void onSelectionChanged(AbstractSelectionManager selectionManager) {
		this.selectionManager = selectionManager;
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

	public boolean selectionNeeded(){
		return true;
	}

	public Geoset getGeosetWithMaterial(Material material) {
		Set<Geoset> geosets = modelView.getVisEdGeosets();
		for (Geoset geoset : geosets) {
			if (geoset.getMaterial().equals(material)) {
				return geoset;
			}
		}
		return null;
	}

	protected UndoAction getSetupAction(Collection<GeosetVertex> vertices, Collection<Triangle> triangles) {
		EditableModel model = modelHandler.getModel();
		Material material = ModelUtils.getWhiteMaterial(model);
		Geoset geoset = getGeosetWithMaterial(material);
		List<UndoAction> undoActions = new ArrayList<>();

		if(geoset == null){
			Geoset newGeoset = new Geoset();
			newGeoset.setMaterial(material);
			newGeoset.addVerticies(vertices);
			newGeoset.addTriangles(triangles);
			vertices.forEach(vertex -> vertex.setGeoset(newGeoset));
			triangles.forEach(triangle -> triangle.setGeoset(newGeoset));
			undoActions.add(new AddGeosetAction(newGeoset, model, null));
			if(!model.contains(material)){
				undoActions.add(new AddMaterialAction(material, model, null));
			}
		} else {
			undoActions.add(new AddGeometryAction(geoset, vertices, triangles, true, null));
		}

		undoActions.add(new SetSelectionUggAction(vertices, modelView, "Select Mesh", null));
		return new CompoundAction("Draw Mesh", undoActions,  changeListener::geosetsUpdated);
	}


	protected Mat4 getRotMat(){
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


	protected Vec3 get3DPoint(Vec2 mousePos) {
		tempVec3.set(mousePos.x, mousePos.y, -1).transform(inverseViewProjectionMatrix, 1, true);
		tempVecZ.set(mousePos.x, mousePos.y, 1).transform(inverseViewProjectionMatrix, 1, true).sub(tempVec3).normalize();

		point3d.set(mousePos.x, mousePos.y, 0).transform(inverseViewProjectionMatrix, 1, true);

		float dotZ = point3d.dot(tempVecZ);
		point3d.addScaled(tempVecZ, -dotZ);

		return point3d;
	}

	protected float[] halfScreenXY(){
		tempVec3.set(Vec3.ZERO)  .addScaled(Vec3.Z_AXIS, .99f).transform(inverseViewProjectionMatrix, 1, true);
		tempVecX.set(Vec3.X_AXIS).addScaled(Vec3.Z_AXIS, .99f).transform(inverseViewProjectionMatrix, 1, true).sub(tempVec3);
		tempVecY.set(Vec3.Y_AXIS).addScaled(Vec3.Z_AXIS, .99f).transform(inverseViewProjectionMatrix, 1, true).sub(tempVec3);
		return new float[]{tempVecX.length(), tempVecY.length()};
	}

	protected Vec2 getPoint(MouseEvent e) {
		Component component = e.getComponent();
		float xRatio = (2.0f * (float) e.getX() / (float) component.getWidth()) - 1.0f;
		float yRatio = 1.0f - (2.0f * (float) e.getY() / (float) component.getHeight());
		return relViewPoint.set(xRatio, yRatio);
	}

	public void setPrefs() {
		prefs = ProgramGlobals.getPrefs();
	}
}
