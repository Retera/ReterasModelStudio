package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class DeleteNodesAction implements UndoAction {
	private final List<IdObject> objects;
	private final ModelStructureChangeListener changeListener;
	private final ModelView model;
	private final Collection<Vertex> selection;
	private final VertexSelectionHelper vertexSelectionHelper;

	public DeleteNodesAction(final Collection<? extends Vertex> selection, final List<IdObject> objects,
			final ModelStructureChangeListener changeListener, final ModelView model,
			final VertexSelectionHelper vertexSelectionHelper) {
		this.selection = new ArrayList<>(selection);
		this.objects = objects;
		this.changeListener = changeListener;
		this.model = model;
		this.vertexSelectionHelper = vertexSelectionHelper;
	}

	@Override
	public void undo() {
		for (final IdObject object : objects) {
			model.getModel().add(object);
		}
		changeListener.nodesAdded(objects);
		vertexSelectionHelper.selectVertices(selection);
	}

	@Override
	public void redo() {
		for (final IdObject object : objects) {
			model.getModel().remove(object);
		}
		changeListener.nodesRemoved(objects);
		vertexSelectionHelper.selectVertices(new ArrayList<Vertex>());
	}

	@Override
	public String actionName() {
		return "delete nodes";
	}

}
