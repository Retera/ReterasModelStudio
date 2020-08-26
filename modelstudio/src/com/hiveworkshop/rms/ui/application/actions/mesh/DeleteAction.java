package com.hiveworkshop.rms.ui.application.actions.mesh;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vertex3;

/**
 * Something to undo when you deleted something important.
 *
 * Eric Theller 6/11/2012
 */
public class DeleteAction implements UndoAction {
	private final List<Vertex3> selection;
	private final List<Vertex3> deleted;
	private final List<Triangle> deletedTris;
	private final VertexSelectionHelper vertexSelectionHelper;

	public DeleteAction(final Collection<? extends Vertex3> selection, final List<Triangle> deletedTris,
			final VertexSelectionHelper vertexSelectionHelper) {
		this.vertexSelectionHelper = vertexSelectionHelper;
		this.selection = new ArrayList<>(selection);
		this.deleted = new ArrayList<>(selection);
		this.deletedTris = deletedTris;
	}

	@Override
	public void redo() {
		for (int i = 0; i < deleted.size(); i++) {
			if (deleted.get(i).getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) deleted.get(i);
				gv.getGeoset().remove(gv);
			}
		}
		for (final Triangle t : deletedTris) {
			t.getGeoset().removeTriangle(t);
			for (final GeosetVertex vertex : t.getAll()) {
				vertex.getTriangles().remove(t);
			}
		}
		vertexSelectionHelper.selectVertices(new ArrayList<Vertex3>());
	}

	@Override
	public void undo() {
		for (int i = 0; i < deleted.size(); i++) {
			if (deleted.get(i).getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) deleted.get(i);
				gv.getGeoset().addVertex(gv);
			}
		}
		for (final Triangle t : deletedTris) {
			t.getGeoset().addTriangle(t);
			for (final GeosetVertex vertex : t.getAll()) {
				vertex.getTriangles().add(t);
			}
		}
		vertexSelectionHelper.selectVertices(selection);
	}

	@Override
	public String actionName() {
		return "delete vertices";
	}
}
