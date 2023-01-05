package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.*;

public class SimplifyGeometryAction implements UndoAction {
	private final List<UndoAction> actions = new ArrayList<>();
	private final ModelStructureChangeListener changeListener;

	public SimplifyGeometryAction(Collection<GeosetVertex> selection, ModelStructureChangeListener changeListener) {
		this(selection, 100,
				false,
				0.001f, 0.00001f, 0.001f, changeListener);
	}

	public SimplifyGeometryAction(Collection<GeosetVertex> selection,
	                              int precision, boolean ign_skin,
	                              float norm_prec, float uv_prec, float tang_prec,
	                              ModelStructureChangeListener changeListener) {
		Map<Geoset, Set<GeosetVertex>> geosetVertexMap = new HashMap<>();
		this.changeListener = changeListener;

		for (GeosetVertex vertex : selection) {
			geosetVertexMap.computeIfAbsent(vertex.getGeoset(), k -> new HashSet<>()).add(vertex);
		}

		for (Geoset geoset : geosetVertexMap.keySet()) {
			actions.add(new SimplifyGeometryAction2(geoset, geosetVertexMap.get(geoset),
			precision, ign_skin,
			norm_prec, uv_prec, tang_prec, null));
		}
	}


	@Override
	public UndoAction redo() {
		for(UndoAction action : actions){
			action.redo();
		}
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		for(UndoAction action : actions){
			action.undo();
		}
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Simplify Geometry";
	}
}
