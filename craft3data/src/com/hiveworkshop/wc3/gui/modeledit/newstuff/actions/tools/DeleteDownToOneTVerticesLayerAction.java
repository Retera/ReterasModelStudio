package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.tools;

import java.util.List;

import com.etheller.collections.Map;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.TVertex;

public class DeleteDownToOneTVerticesLayerAction implements UndoAction {
	private final Map<GeosetVertex, List<TVertex>> vertexToTVertexRemoved;

	public DeleteDownToOneTVerticesLayerAction(final Map<GeosetVertex, List<TVertex>> vertexToTVertexRemoved) {
		this.vertexToTVertexRemoved = vertexToTVertexRemoved;
	}

	@Override
	public void undo() {
		for (final Map.Entry<GeosetVertex, List<TVertex>> vertexAndTVertices : vertexToTVertexRemoved.entrySet()) {
			final GeosetVertex vertex = vertexAndTVertices.getKey();
			final List<TVertex> tVertices = vertexAndTVertices.getValue();
			vertex.getTverts().addAll(tVertices);
		}
	}

	@Override
	public void redo() {
		for (final Map.Entry<GeosetVertex, List<TVertex>> vertexAndTVertices : vertexToTVertexRemoved.entrySet()) {
			final GeosetVertex vertex = vertexAndTVertices.getKey();
			final List<TVertex> tverts = vertex.getTverts();
			while (tverts.size() > 1) {
				tverts.remove(tverts.size() - 1);
			}
		}
	}

	@Override
	public String actionName() {
		return "delete down to one TVertices layer";
	}

}
