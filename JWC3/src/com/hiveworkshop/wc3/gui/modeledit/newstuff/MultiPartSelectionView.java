package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.etheller.collections.ListView;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class MultiPartSelectionView implements SelectionView {
	private final ListView<SelectionView> selectionViews;

	public MultiPartSelectionView(final ListView<SelectionView> selectionViews) {
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
}
