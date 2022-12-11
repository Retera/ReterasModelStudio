package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;

public class StaticMeshShrinkFattenAction implements UndoAction {
	private float amount;
	private final ArrayList<GeosetVertex> selectedVertices;

	private final ArrayList<Vec3> opgPosVertices;

	public StaticMeshShrinkFattenAction(ModelView modelView) {
		this(modelView, 0);
	}

	public StaticMeshShrinkFattenAction(ModelView modelView, float amount) {
		this(modelView.getSelectedVertices(), amount);
	}

	public StaticMeshShrinkFattenAction(Collection<GeosetVertex> selectedVertices) {
		this(selectedVertices, 0);
	}

	public StaticMeshShrinkFattenAction(Collection<GeosetVertex> selectedVertices, float amount) {
		this.amount = amount;
		this.selectedVertices = new ArrayList<>(selectedVertices);

		this.opgPosVertices = new ArrayList<>();
		this.selectedVertices.forEach(v -> opgPosVertices.add(new Vec3(v)));
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
		for (GeosetVertex vertex : selectedVertices) {
			vertex.addScaled(vertex.getNormal(), amount);
		}
	}

}
