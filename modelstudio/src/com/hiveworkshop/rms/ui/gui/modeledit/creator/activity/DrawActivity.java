package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
import com.hiveworkshop.rms.editor.actions.mesh.AddGeometryAction;
import com.hiveworkshop.rms.editor.actions.model.material.AddMaterialAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.util.MouseEventHelpers;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DrawActivity extends ViewportActivity {
	float halfScreenX;
	float halfScreenY;
	protected final Vec3 startPoint3d = new Vec3();

	protected ModelEditorActionType3 lastEditorType;



	public DrawActivity(ModelHandler modelHandler, ModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager);
	}

	public DrawActivity(ModelHandler modelHandler, ModelEditorManager modelEditorManager,
	                        ModelEditorActionType3 lastEditorType) {
		super(modelHandler, modelEditorManager);
		this.lastEditorType = lastEditorType;
	}


	@Override
	public boolean selectionNeeded() {
		return false;
	}

	@Override
	public boolean isEditing() {
		return transformAction != null;
	}

	@Override
	public void mouseReleased(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (transformAction != null) {
			undoManager.pushAction(transformAction);
			transformAction = null;
		}

		if (lastEditorType != null && !hasContinueActivityModifier(e)) {
			System.out.println("returning to prev action type!");
			ProgramGlobals.getCurrentModelPanel().setEditorActionType(lastEditorType);
		} else {
			System.out.println("keep draw vertices!");
		}
	}

	protected boolean hasContinueActivityModifier(MouseEvent e) {
		return MouseEventHelpers.hasModifier(e.getModifiersEx(), MouseEvent.CTRL_DOWN_MASK);
	}

	protected boolean uniformSizeModifier(MouseEvent e) {
		return MouseEventHelpers.hasModifier(e.getModifiersEx(), MouseEvent.SHIFT_DOWN_MASK);
	}


	@Override
	public void mouseMoved(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		mouseDragged(e, viewProjectionMatrix, sizeAdj);
	}

	protected Vec3 get3DPoint(Vec2 mousePos) {
		tempVec3.set(mousePos.x, mousePos.y, -1).transform(inverseViewProjectionMatrix, 1, true);
		tempVecZ.set(mousePos.x, mousePos.y, 1).transform(inverseViewProjectionMatrix, 1, true).sub(tempVec3).normalize();

		point3d.set(mousePos.x, mousePos.y, 0).transform(inverseViewProjectionMatrix, 1, true);

		float dotZ = point3d.dot(tempVecZ);
		point3d.addScaled(tempVecZ, -dotZ);

		return point3d;
	}

	protected void setHalfScreenXY() {
		tempVec3.set(Vec3.ZERO)  .addScaled(Vec3.Z_AXIS, .99f).transform(inverseViewProjectionMatrix, 1, true);
		tempVecX.set(Vec3.X_AXIS).addScaled(Vec3.Z_AXIS, .99f).transform(inverseViewProjectionMatrix, 1, true).sub(tempVec3);
		tempVecY.set(Vec3.Y_AXIS).addScaled(Vec3.Z_AXIS, .99f).transform(inverseViewProjectionMatrix, 1, true).sub(tempVec3);
		halfScreenX = tempVecX.length();
		halfScreenY = tempVecY.length();
	}

	protected UndoAction getSetupAction(Collection<GeosetVertex> vertices, Collection<Triangle> triangles) {
		EditableModel model = modelHandler.getModel();
		Material material = getWhiteMaterial(model);
		Geoset geoset = getGeosetWithMaterial(material);
		List<UndoAction> undoActions = new ArrayList<>();

		if (geoset == null) {
			Geoset newGeoset = new Geoset();
			newGeoset.setMaterial(material);
			newGeoset.addVerticies(vertices);
			newGeoset.addTriangles(triangles);
			vertices.forEach(vertex -> vertex.setGeoset(newGeoset));
			triangles.forEach(triangle -> triangle.setGeoset(newGeoset));
			undoActions.add(new AddGeosetAction(newGeoset, model, null));
			if (!model.contains(material)) {
				undoActions.add(new AddMaterialAction(material, model, null));
			}
		} else {
			undoActions.add(new AddGeometryAction(geoset, vertices, triangles, true, null));
		}

		undoActions.add(new SetSelectionUggAction(vertices, modelView, "Select Mesh", null));
		return new CompoundAction("Draw Mesh", undoActions,  changeListener::geosetsUpdated);
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


	public static Material getWhiteMaterial(EditableModel model) {
		Material material = new Material(new Layer(new Bitmap("Textures\\BTNtempW.blp")));
		if (model.getMaterials().contains(material)) {
			int i = model.getMaterials().indexOf(material);
			return model.getMaterial(i);
		}
		return material;
	}
}
