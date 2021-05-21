package com.hiveworkshop.rms.ui.application.edit.mesh.types.geosetvertex;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
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

public class GeosetVertexModelEditor extends AbstractModelEditor<GeosetVertex> {

	public GeosetVertexModelEditor(GeosetVertexSelectionManager selectionManager,
	                               ModelStructureChangeListener structureChangeListener,
	                               ModelHandler modelHandler) {
		super(selectionManager, structureChangeListener, modelHandler);
	}

	@Override
	public void selectByVertices(Collection<? extends Vec3> newSelection) {
		List<GeosetVertex> newGeosetVertices = new ArrayList<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (GeosetVertex vertex : geoset.getVertices()) {
				if (newSelection.contains(vertex)) {
					newGeosetVertices.add(vertex);
				}
			}
		}
		selectionManager.setSelection(newGeosetVertices);
	}

	protected List<GeosetVertex> genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<GeosetVertex> selectedItems = new ArrayList<>();

		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (hitTest(min, max, geosetVertex, coordinateSystem, ProgramGlobals.getPrefs().getVertexSize()))
					selectedItems.add(geosetVertex);
			}
		}
		return selectedItems;
	}

	@Override
	public boolean canSelectAt(Vec2 point, CoordinateSystem axes) {
		boolean canSelect = false;
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
	protected UndoAction buildHideComponentAction(List<? extends CheckableDisplayElement<?>> selectableComponents, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable) {
		List<GeosetVertex> previousSelection = new ArrayList<>(modelView.getSelectedVertices());
		List<GeosetVertex> possibleVerticesToTruncate = new ArrayList<>();
		for (CheckableDisplayElement<?> component : selectableComponents) {
			Object item = component.getItem();
			if (item instanceof Geoset) {
				possibleVerticesToTruncate.addAll(((Geoset) item).getVertices());
			}
		}
		Runnable truncateSelectionRunnable = () -> modelView.removeSelectedVertices(possibleVerticesToTruncate);
		Runnable unTruncateSelectionRunnable = () -> modelView.setSelectedVertices(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
