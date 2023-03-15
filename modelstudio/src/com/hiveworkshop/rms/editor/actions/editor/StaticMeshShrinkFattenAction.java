package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.util.HashableVector;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class StaticMeshShrinkFattenAction implements UndoAction {
	private float amount;
	private final ArrayList<GeosetVertex> selectedVertices;

	private final ArrayList<Vec3> opgPosVertices;
	Map<HashableVector, List<GeosetVertex>> locationToGVs = new HashMap<>();
	Map<HashableVector, Vec3> locationToNormal = new HashMap<>();
	private boolean scaleApart;

	public StaticMeshShrinkFattenAction(ModelView modelView, float amount, boolean scaleApart) {
		this(modelView.getSelectedVertices(), amount, scaleApart);
	}
	public StaticMeshShrinkFattenAction(Collection<GeosetVertex> selectedVertices, float amount, boolean scaleApart) {
		this.amount = amount;
		this.selectedVertices = new ArrayList<>(selectedVertices);

		this.opgPosVertices = new ArrayList<>();
		this.selectedVertices.forEach(v -> opgPosVertices.add(new Vec3(v)));
		this.scaleApart = scaleApart;
		if(!scaleApart){
			for (GeosetVertex vertex : selectedVertices) {
				locationToGVs.computeIfAbsent(new HashableVector(vertex), k -> new ArrayList<>()).add(vertex);
			}

			for(HashableVector key : locationToGVs.keySet()){
				Vec3 normal = new Vec3();
				locationToGVs.get(key).forEach(gv -> normal.add(gv.getNormal()));
				normal.scale(1f/(float) locationToGVs.get(key).size()).normalize();
				locationToNormal.put(key, normal);
			}
		}
	}

	@Override
	public StaticMeshShrinkFattenAction undo() {
		for (int i = 0; i<selectedVertices.size(); i++) {
			selectedVertices.get(i).set(opgPosVertices.get(i));
		}
		return this;
	}

	@Override
	public StaticMeshShrinkFattenAction redo() {
		rawScale(amount);
		return this;
	}

	@Override
	public String actionName() {
		return amount <0 ? "Shrink" : "Fatten";
	}


	public UndoAction updateAmount(float amount) {
		this.amount += amount;
		rawScale(amount);
		return this;
	}

	public void rawScale(float amount) {
		if (scaleApart){
			for (GeosetVertex vertex : selectedVertices) {
				vertex.addScaled(vertex.getNormal(), amount);
			}
		} else {
			for(HashableVector key : locationToGVs.keySet()){
				Vec3 normal = locationToNormal.get(key);
				for (GeosetVertex vertex : locationToGVs.get(key)) {
					vertex.addScaled(normal, amount);
				}
			}
		}
	}

}
