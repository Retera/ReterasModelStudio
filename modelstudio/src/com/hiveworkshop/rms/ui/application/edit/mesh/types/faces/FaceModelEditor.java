package com.hiveworkshop.rms.ui.application.edit.mesh.types.faces;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
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

import java.util.*;

public class FaceModelEditor extends AbstractModelEditor<Triangle> {

	public FaceModelEditor(FaceSelectionManager selectionManager,
	                       ModelStructureChangeListener structureChangeListener,
	                       ModelHandler modelHandler) {
		super(selectionManager, structureChangeListener, modelHandler);
	}

	@Override
	public void selectByVertices(Collection<? extends Vec3> newSelection) {
		Set<Triangle> newlySelectedFaces = new HashSet<>();
		Set<GeosetVertex> newlySelectedVerts = new HashSet<>();

		for (Geoset geoset : modelView.getModel().getGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				boolean allInSelection = true;
				for (GeosetVertex vertex : triangle.getVerts()) {
					if (!newSelection.contains(vertex)) {
						allInSelection = false;
						break;
					}
				}
				if (allInSelection) {
					newlySelectedVerts.addAll(Arrays.asList(triangle.getVerts()));
					newlySelectedFaces.add(triangle);
				}
			}
		}
//		selectionManager.setSelection(newlySelectedFaces);
		modelView.setSelectedVertices(newlySelectedVerts);
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
		return canSelect;
	}

	@Override
	protected List<Triangle> genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<Triangle> newSelection = new ArrayList<>();

		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (triHitTest(triangle, min, coordinateSystem)
						|| triHitTest(triangle, max, coordinateSystem)
						|| triHitTest(triangle, min, max, coordinateSystem)) {
					newSelection.add(triangle);
				}
			}
		}
		return newSelection;
	}

	@Override
	protected UndoAction buildHideComponentAction(List<? extends CheckableDisplayElement<?>> selectableComponents, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable) {
		List<Triangle> previousSelection = new ArrayList<>(modelView.getSelectedTriangles());
		List<Triangle> possibleTrianglesToTruncate = new ArrayList<>();
//		List<Vec3> possibleVerticesToTruncate = new ArrayList<>();
		for (CheckableDisplayElement<?> component : selectableComponents) {
			Object item = component.getItem();
//			if (item instanceof Camera) {
//				possibleVerticesToTruncate.add(((Camera) item).getPosition());
//				possibleVerticesToTruncate.add(((Camera) item).getTargetPosition());
//			} else if (item instanceof IdObject) {
//				possibleVerticesToTruncate.add(((IdObject) item).getPivotPoint());
//			} else
			if (item instanceof Geoset) {
				possibleTrianglesToTruncate.addAll(((Geoset) item).getTriangles());
			}
		}
		Runnable truncateSelectionRunnable = () -> modelView.removeSelectedTris(possibleTrianglesToTruncate);
		Runnable unTruncateSelectionRunnable = () -> modelView.setSelectedTris(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
