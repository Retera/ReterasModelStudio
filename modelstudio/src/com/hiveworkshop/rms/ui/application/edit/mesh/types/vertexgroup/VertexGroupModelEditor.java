package com.hiveworkshop.rms.ui.application.edit.mesh.types.vertexgroup;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.CheckableDisplayElement;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class VertexGroupModelEditor extends AbstractModelEditor<VertexGroupBundle> {

	public VertexGroupModelEditor(VertexGroupSelectionManager selectionManager,
	                              ModelStructureChangeListener structureChangeListener,
	                              ModelHandler modelHandler) {
		super(selectionManager, structureChangeListener, modelHandler);
	}

	@Override
	public boolean canSelectAt(Vec2 point, CoordinateSystem axes) {
		boolean canSelect = false;
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (triHitTest(triangle, CoordSysUtils.geomV2(axes, point), axes)) {
					canSelect = true;
				}
			}
		}
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (hitTest(geosetVertex, CoordSysUtils.geomV2(axes, point), axes, ProgramGlobals.getPrefs().getVertexSize())) {
					canSelect = true;
				}
			}
		}
		return canSelect;
	}

	@Override
	protected List<VertexGroupBundle> genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<VertexGroupBundle> newSelection = new ArrayList<>();

		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (triHitTest(triangle, min, coordinateSystem)
						|| triHitTest(triangle, max, coordinateSystem)
						|| triHitTest(triangle, min, max, coordinateSystem)) {
					for (GeosetVertex vertex : triangle.getAll()) {
						newSelection.add(new VertexGroupBundle(geoset, vertex.getVertexGroup()));
					}
				}
			}
		}

		List<GeosetVertex> geosetVerticesSelected = new ArrayList<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (hitTest(min, max, geosetVertex, coordinateSystem, ProgramGlobals.getPrefs().getVertexSize())) {
					geosetVerticesSelected.add(geosetVertex);
				}
			}
		}
		for (GeosetVertex vertex : geosetVerticesSelected) {
			newSelection.add(new VertexGroupBundle(vertex.getGeoset(), vertex.getVertexGroup()));
		}
		return newSelection;
	}

	@Override
	protected UndoAction buildHideComponentAction(List<? extends CheckableDisplayElement<?>> selectableComponents, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable) {
		List<VertexGroupBundle> previousSelection = new ArrayList<>(selectionManager.getSelection());
		List<VertexGroupBundle> vertexBundlesToTruncate = new ArrayList<>(selectionManager.getSelection());
		for (CheckableDisplayElement<?> component : selectableComponents) {
			Object item = component.getItem();
			if (item instanceof Geoset) {
				for (VertexGroupBundle bundle : previousSelection) {
					if (bundle.getGeoset() == item) {
						vertexBundlesToTruncate.add(bundle);
					}
				}
			}
		}
		Runnable truncateSelectionRunnable = () -> selectionManager.removeSelection(vertexBundlesToTruncate);
		Runnable unTruncateSelectionRunnable = () -> selectionManager.setSelection(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	@Override
	public void selectByVertices(Collection<? extends Vec3> newSelection) {
		List<VertexGroupBundle> newSelectionGroups = new ArrayList<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (newSelection.contains(geosetVertex)) {
					newSelectionGroups.add(new VertexGroupBundle(geosetVertex.getGeoset(), geosetVertex.getVertexGroup()));
				}
			}
		}
		selectionManager.setSelection(newSelectionGroups);
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
