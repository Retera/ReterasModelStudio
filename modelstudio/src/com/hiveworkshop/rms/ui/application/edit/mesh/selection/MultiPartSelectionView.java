package com.hiveworkshop.rms.ui.application.edit.mesh.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vertex2;
import com.hiveworkshop.rms.util.Vertex3;

public class MultiPartSelectionView implements SelectionView {
	private final List<SelectionView> selectionViews;

	public MultiPartSelectionView(final List<SelectionView> selectionViews) {
		this.selectionViews = selectionViews;
	}

	@Override
	public Vertex3 getCenter() {
		final List<Vertex3> vertices = new ArrayList<>();
		for (final SelectionView selectionView : selectionViews) {
			for (final Vertex3 vertex : selectionView.getSelectedVertices()) {
				vertices.add(vertex);
			}
		}
		return Vertex3.centerOfGroup(vertices);
	}

	@Override
	public Collection<Triangle> getSelectedFaces() {
		final List<Triangle> faces = new ArrayList<>();
		for (final SelectionView selectionView : selectionViews) {
			for (final Triangle face : selectionView.getSelectedFaces()) {
				faces.add(face);
			}
		}
		return faces;
	}

	@Override
	public Collection<? extends Vertex3> getSelectedVertices() {
		final List<Vertex3> vertices = new ArrayList<>();
		for (final SelectionView selectionView : selectionViews) {
			for (final Vertex3 vertex : selectionView.getSelectedVertices()) {
				vertices.add(vertex);
			}
		}
		return vertices;
	}

	@Override
	public double getCircumscribedSphereRadius(final Vertex3 center) {
		double radius = 0;
		// TODO WHY DOES THIS DISCARD THE CENTER ARG??
		final Collection<? extends Vertex3> selectedVertices = getSelectedVertices();
		final Vertex3 centerOfGroup = Vertex3.centerOfGroup(selectedVertices);
		for (final Vertex3 item : selectedVertices) {
			final double distance = centerOfGroup.distance(item);
			if (distance >= radius) {
				radius = distance;
			}
		}
		return radius;
	}

	@Override
	public void renderSelection(final ModelElementRenderer renderer, final CoordinateSystem coordinateSystem,
								final ModelView modelView, final ProgramPreferences programPreferences) {
		for (final SelectionView selectionView : selectionViews) {
			selectionView.renderSelection(renderer, coordinateSystem, modelView, programPreferences);
		}
	}

	@Override
	public boolean isEmpty() {
		boolean empty = true;
		for (final SelectionView selectionView : selectionViews) {
			if (!selectionView.isEmpty()) {
				empty = false;
			}
		}
		return empty;
	}

	@Override
	public Vertex2 getUVCenter(final int tvertexLayerId) {
		final List<Vertex2> vertices = new ArrayList<>();
		for (final SelectionView selectionView : selectionViews) {
			for (final Vertex2 vertex : selectionView.getSelectedTVertices(tvertexLayerId)) {
				vertices.add(vertex);
			}
		}
		return Vertex2.centerOfGroup(vertices);
	}

	@Override
	public Collection<? extends Vertex2> getSelectedTVertices(final int tvertexLayerId) {
		final List<Vertex2> vertices = new ArrayList<>();
		for (final SelectionView selectionView : selectionViews) {
			for (final Vertex2 vertex : selectionView.getSelectedTVertices(tvertexLayerId)) {
				vertices.add(vertex);
			}
		}
		return vertices;
	}

	@Override
	public double getCircumscribedSphereRadius(final Vertex2 center, final int tvertexLayerId) {
		double radius = 0;
		// TODO WHY DOES THIS DISCARD THE CENTER ARG??
		final Collection<? extends Vertex2> selectedVertices = getSelectedTVertices(tvertexLayerId);
		final Vertex2 centerOfGroup = Vertex2.centerOfGroup(selectedVertices);
		for (final Vertex2 item : selectedVertices) {
			final double distance = centerOfGroup.distance(item);
			if (distance >= radius) {
				radius = distance;
			}
		}
		return radius;
	}

	@Override
	public void renderUVSelection(final TVertexModelElementRenderer renderer, final ModelView modelView,
                                  final ProgramPreferences programPreferences, final int tvertexLayerId) {
		for (final SelectionView selectionView : selectionViews) {
			selectionView.renderUVSelection(renderer, modelView, programPreferences, tvertexLayerId);
		}
	}
}
