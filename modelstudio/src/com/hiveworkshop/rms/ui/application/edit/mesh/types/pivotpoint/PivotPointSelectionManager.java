package com.hiveworkshop.rms.ui.application.edit.mesh.types.pivotpoint;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.NodeIconPalette;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class PivotPointSelectionManager extends SelectionManager<Vec3> {

	private final Bone renderBoneDummy = new Bone();

	@Override
	public Vec3 getCenter() {
		return Vec3.centerOfGroup(selection);
	}

	@Override
	public Collection<Vec3> getSelectedVertices() {
		return getSelection();
	}

	@Override
	public Set<Triangle> getSelectedFaces() {
		return new HashSet<>();
	}

	@Override
	public void renderSelection(ModelElementRenderer renderer, CoordinateSystem coordinateSystem,
	                            ModelView model, ProgramPreferences programPreferences) {
		Set<Vec3> drawnSelection = new HashSet<>();
		for (IdObject object : model.getEditableIdObjects()) {
			if (selection.contains(object.getPivotPoint())) {
				renderer.renderIdObject(object, NodeIconPalette.SELECTED, programPreferences.getSelectColor(), programPreferences.getPivotPointsSelectedColor());
				drawnSelection.add(object.getPivotPoint());
			}
		}
		for (Camera camera : model.getEditableCameras()) {
			renderer.renderCamera(camera, selection.contains(camera.getPosition()) ? Color.GREEN.darker() : Color.ORANGE.darker(), camera.getPosition(), selection.contains(camera.getTargetPosition()) ? Color.GREEN.darker() : Color.ORANGE.darker(), camera.getTargetPosition());
			drawnSelection.add(camera.getPosition());
			drawnSelection.add(camera.getTargetPosition());
		}
		for (Vec3 vertex : selection) {
			if (!drawnSelection.contains(vertex)) {
				renderBoneDummy.setPivotPoint(vertex);
				renderer.renderIdObject(renderBoneDummy, NodeIconPalette.SELECTED, programPreferences.getSelectColor(), programPreferences.getPivotPointsSelectedColor());
			}
		}
	}

	@Override
	public double getCircumscribedSphereRadius(Vec3 sphereCenter) {
		double radius = 0;
		for (Vec3 item : selection) {
			double distance = sphereCenter.distance(item);
			if (distance >= radius) {
				radius = distance;
			}
		}
		return radius;
	}

	@Override
	public double getCircumscribedSphereRadius(Vec2 center, int tvertexLayerId) {
		return 0;
	}

	@Override
	public Vec2 getUVCenter(int tvertexLayerId) {
		return Vec2.ORIGIN;
	}

	@Override
	public Collection<? extends Vec2> getSelectedTVertices(int tvertexLayerId) {
		return Collections.emptySet();
	}

	@Override
	public void renderUVSelection(TVertexModelElementRenderer renderer, ModelView modelView, ProgramPreferences programPreferences, int tvertexLayerId) {
	}
}
