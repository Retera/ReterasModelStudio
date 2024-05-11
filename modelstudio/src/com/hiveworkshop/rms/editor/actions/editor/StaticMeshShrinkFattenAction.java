package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.util.HashableVector;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class StaticMeshShrinkFattenAction extends AbstractShrinkFattenAction {
	private final ArrayList<GeosetVertex> selectedVertices;

	private final ArrayList<Vec3> opgPosVertices;
	private final ArrayList<Vec3> normals;

	public StaticMeshShrinkFattenAction(ModelView modelView, float amount, boolean scaleApart) {
		this(modelView.getSelectedVertices(), amount, scaleApart);
	}
	public StaticMeshShrinkFattenAction(Collection<GeosetVertex> selectedVertices, float amount, boolean scaleApart) {
		this.amount = amount;
		this.selectedVertices = new ArrayList<>(selectedVertices);

		this.opgPosVertices = new ArrayList<>();
		this.normals = new ArrayList<>();
		this.selectedVertices.forEach(v -> opgPosVertices.add(new Vec3(v)));
		if (scaleApart) {
			this.selectedVertices.forEach(v -> normals.add(new Vec3(v.getNormal())));
		} else {
			Map<HashableVector, List<GeosetVertex>> locationToGVs = new HashMap<>();
			for (GeosetVertex vertex : selectedVertices) {
				locationToGVs.computeIfAbsent(new HashableVector(vertex), k -> new ArrayList<>()).add(vertex);
			}

			Map<HashableVector, Vec3> locationToNormal = new HashMap<>();
			for (HashableVector key : locationToGVs.keySet()) {
				Vec3 normal = new Vec3();
				locationToGVs.get(key).forEach(gv -> normal.add(gv.getNormal()));
				locationToNormal.put(key, normal.normalize());
			}

			for (GeosetVertex vertex : selectedVertices) {
				normals.add(locationToNormal.get(new HashableVector(vertex)));
			}
		}
	}

	@Override
	public StaticMeshShrinkFattenAction undo() {
		for (int i = 0; i < selectedVertices.size(); i++) {
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
		return (amount < 0 ? "Shrink by " : "Fatten by ") + String.format(Locale.US, "%3.1f", amount);
	}


	public StaticMeshShrinkFattenAction updateAmount(float deltaAmount) {
		this.amount += deltaAmount;
		rawScale(deltaAmount);
		return this;
	}

	public StaticMeshShrinkFattenAction setAmount(float amount) {
		this.amount = amount;
		rawSetScale(amount);
		return this;
	}

	protected void rawScale(float amountDelta) {
		for (int i = 0; i < selectedVertices.size(); i++) {
			selectedVertices.get(i).addScaled(normals.get(i), amountDelta);
		}
	}

	protected void rawSetScale(float amount) {
		for (int i = 0; i < selectedVertices.size(); i++) {
			selectedVertices.get(i).set(opgPosVertices.get(i)).addScaled(normals.get(i), amount);
		}
	}
}
