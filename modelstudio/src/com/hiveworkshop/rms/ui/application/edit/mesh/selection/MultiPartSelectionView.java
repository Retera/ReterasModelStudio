package com.hiveworkshop.rms.ui.application.edit.mesh.selection;

import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MultiPartSelectionView implements SelectionView {
	private final List<SelectionView> selectionViews;

	public MultiPartSelectionView(List<SelectionView> selectionViews) {
		this.selectionViews = selectionViews;
	}

	@Override
	public Vec3 getCenter() {
		List<Vec3> vertices = new ArrayList<>();
		for (SelectionView selectionView : selectionViews) {
			vertices.addAll(selectionView.getSelectedVertices());
		}
		return Vec3.centerOfGroup(vertices);
	}

	@Override
	public Collection<? extends Vec3> getSelectedVertices() {
		List<Vec3> vertices = new ArrayList<>();
		for (SelectionView selectionView : selectionViews) {
			vertices.addAll(selectionView.getSelectedVertices());
		}
		return vertices;
	}

	@Override
	public Collection<Triangle> getSelectedFaces() {
		List<Triangle> faces = new ArrayList<>();
		for (SelectionView selectionView : selectionViews) {
			faces.addAll(selectionView.getSelectedFaces());
		}
		return faces;
	}

	@Override
	public void renderSelection(ModelElementRenderer renderer, CoordinateSystem coordinateSystem,
	                            ModelView modelView, ProgramPreferences programPreferences) {
		for (SelectionView selectionView : selectionViews) {
			selectionView.renderSelection(renderer, coordinateSystem, modelView, programPreferences);
		}
	}

	@Override
	public double getCircumscribedSphereRadius(Vec3 center) {
		double radius = 0;
		// TODO WHY DOES THIS DISCARD THE CENTER ARG??
		Collection<? extends Vec3> selectedVertices = getSelectedVertices();
		Vec3 centerOfGroup = Vec3.centerOfGroup(selectedVertices);
		for (Vec3 item : selectedVertices) {
			double distance = centerOfGroup.distance(item);
			if (distance >= radius) {
				radius = distance;
			}
		}
		return radius;
	}

	@Override
	public boolean isEmpty() {
		boolean empty = true;
		for (SelectionView selectionView : selectionViews) {
			if (!selectionView.isEmpty()) {
				empty = false;
			}
		}
		return empty;
	}

	@Override
	public double getCircumscribedSphereRadius(Vec2 center, int tvertexLayerId) {
		double radius = 0;
		// TODO WHY DOES THIS DISCARD THE CENTER ARG??
		Collection<? extends Vec2> selectedVertices = getSelectedTVertices(tvertexLayerId);
		Vec2 centerOfGroup = Vec2.centerOfGroup(selectedVertices);
		for (Vec2 item : selectedVertices) {
			double distance = centerOfGroup.distance(item);
			if (distance >= radius) {
				radius = distance;
			}
		}
		return radius;
	}

	@Override
	public Vec2 getUVCenter(int tvertexLayerId) {
		List<Vec2> vertices = new ArrayList<>();
		for (SelectionView selectionView : selectionViews) {
			vertices.addAll(selectionView.getSelectedTVertices(tvertexLayerId));
		}
		return Vec2.centerOfGroup(vertices);
	}

	@Override
	public Collection<? extends Vec2> getSelectedTVertices(int tvertexLayerId) {
		List<Vec2> vertices = new ArrayList<>();
		for (SelectionView selectionView : selectionViews) {
			vertices.addAll(selectionView.getSelectedTVertices(tvertexLayerId));
		}
		return vertices;
	}

	@Override
	public void renderUVSelection(TVertexModelElementRenderer renderer, ModelView modelView,
	                              ProgramPreferences programPreferences, int tvertexLayerId) {
		for (SelectionView selectionView : selectionViews) {
			selectionView.renderUVSelection(renderer, modelView, programPreferences, tvertexLayerId);
		}
	}
}
