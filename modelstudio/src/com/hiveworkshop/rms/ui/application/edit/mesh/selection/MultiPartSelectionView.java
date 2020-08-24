package com.hiveworkshop.rms.ui.application.edit.mesh.selection;

import com.hiveworkshop.rms.editor.model.TVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.Vertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MultiPartSelectionView implements SelectionView {
	private final List<SelectionView> selectionViews;

	public MultiPartSelectionView(final List<SelectionView> selectionViews) {
		this.selectionViews = selectionViews;
	}

	@Override
	public Vertex getCenter() {
		final List<Vertex> vertices = new ArrayList<>();
		for (final SelectionView selectionView : selectionViews) {
			for (final Vertex vertex : selectionView.getSelectedVertices()) {
				vertices.add(vertex);
			}
		}
		return Vertex.centerOfGroup(vertices);
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
	public Collection<? extends Vertex> getSelectedVertices() {
		final List<Vertex> vertices = new ArrayList<>();
		for (final SelectionView selectionView : selectionViews) {
			for (final Vertex vertex : selectionView.getSelectedVertices()) {
				vertices.add(vertex);
			}
		}
		return vertices;
	}

	@Override
	public double getCircumscribedSphereRadius(final Vertex center) {
		double radius = 0;
		// TODO WHY DOES THIS DISCARD THE CENTER ARG??
		final Collection<? extends Vertex> selectedVertices = getSelectedVertices();
		final Vertex centerOfGroup = Vertex.centerOfGroup(selectedVertices);
		for (final Vertex item : selectedVertices) {
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
	public TVertex getUVCenter(final int tvertexLayerId) {
		final List<TVertex> vertices = new ArrayList<>();
		for (final SelectionView selectionView : selectionViews) {
			for (final TVertex vertex : selectionView.getSelectedTVertices(tvertexLayerId)) {
				vertices.add(vertex);
			}
		}
		return TVertex.centerOfGroup(vertices);
	}

	@Override
	public Collection<? extends TVertex> getSelectedTVertices(final int tvertexLayerId) {
		final List<TVertex> vertices = new ArrayList<>();
		for (final SelectionView selectionView : selectionViews) {
			for (final TVertex vertex : selectionView.getSelectedTVertices(tvertexLayerId)) {
				vertices.add(vertex);
			}
		}
		return vertices;
	}

	@Override
	public double getCircumscribedSphereRadius(final TVertex center, final int tvertexLayerId) {
		double radius = 0;
		// TODO WHY DOES THIS DISCARD THE CENTER ARG??
		final Collection<? extends TVertex> selectedVertices = getSelectedTVertices(tvertexLayerId);
		final TVertex centerOfGroup = TVertex.centerOfGroup(selectedVertices);
		for (final TVertex item : selectedVertices) {
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
