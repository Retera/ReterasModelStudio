package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.*;

public class MergeMultiGeosetsAction implements UndoAction {
	private final EditableModel model;
	private final Geoset recGeoset;
	private final Map<Geoset, Integer> geoNumberMap = new LinkedHashMap<>();
	private final Map<Geoset, Set<GeosetVertex>> donVertsMap = new LinkedHashMap<>();
	private final Map<Geoset, Set<Triangle>> donTrisMap = new LinkedHashMap<>();
	private final ModelStructureChangeListener changeListener;
	private final String actionName;

	public MergeMultiGeosetsAction(Geoset recGeoset, Collection<Geoset> donGeosets, EditableModel model, ModelStructureChangeListener changeListener) {
		this.model = model;
		this.recGeoset = recGeoset;
		for (Geoset g : donGeosets) {
			geoNumberMap.put(g, model.getGeosetId(g));
			donVertsMap.put(g, new LinkedHashSet<>(g.getVertices()));
			donTrisMap.put(g, new LinkedHashSet<>(g.getTriangles()));
		}
		this.changeListener = changeListener;

		String donGeoName = donGeosets.size() == 1 ?
				("Geoset #" + model.getGeosetId(donGeosets.stream().findFirst().get()))
				: (donGeosets.size() + " Geosets");
		actionName = "Merge " + donGeoName + " into Geoset #" + model.getGeosetId(recGeoset);
	}

	@Override
	public MergeMultiGeosetsAction undo() {
		for (Geoset g : geoNumberMap.keySet()) {

			for (GeosetVertex vertex : donVertsMap.get(g)) {
				vertex.setGeoset(g);
				recGeoset.remove(vertex);
			}
			for (Triangle triangle : donTrisMap.get(g)) {
				triangle.setGeoset(g);
			}
			recGeoset.remove(donVertsMap.get(g));
			recGeoset.removeTriangles(donTrisMap.get(g));

			model.add(g, geoNumberMap.get(g));
		}

		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public MergeMultiGeosetsAction redo() {

		for (Geoset g : geoNumberMap.keySet()) {
			for (GeosetVertex vertex : donVertsMap.get(g)) {
				vertex.setGeoset(recGeoset);
			}
			for (Triangle triangle : donTrisMap.get(g)) {
				triangle.setGeoset(recGeoset);
			}
			recGeoset.addVerticies(donVertsMap.get(g));
			recGeoset.addTriangles(donTrisMap.get(g));

			model.remove(g);
		}

		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}
}
