package com.hiveworkshop.wc3.gui.modeledit.actions;

import java.util.Collection;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;

public class SpecialDeleteAction extends DeleteAction {

	private final List<Geoset> deletedGeosets;
	private final MDL parent;
	private final ModelStructureChangeListener modelStructureChangeListener;

	public SpecialDeleteAction(final Collection<? extends Vertex> selection, final List<Triangle> deletedTris,
			final List<Geoset> deletedGs, final MDL parentModel,
			final ModelStructureChangeListener modelStructureChangeListener) {
		super(selection, deletedTris);
		deletedGeosets = deletedGs;
		parent = parentModel;
		this.modelStructureChangeListener = modelStructureChangeListener;
	}

	@Override
	public void redo() {
		super.redo();
		for (int i = 0; i < deletedGeosets.size(); i++) {
			final Geoset g = deletedGeosets.get(i);
			if (g.getGeosetAnim() != null) {
				parent.remove(g.getGeosetAnim());
			}
			parent.remove(g);
		}
		modelStructureChangeListener.geosetsRemoved(deletedGeosets);
	}

	@Override
	public void undo() {
		super.undo();
		for (int i = 0; i < deletedGeosets.size(); i++) {
			final Geoset g = deletedGeosets.get(i);
			if (g.getGeosetAnim() != null) {
				parent.add(g.getGeosetAnim());
			}
			parent.add(g);
		}
		modelStructureChangeListener.geosetsAdded(deletedGeosets);
	}

	@Override
	public String actionName() {
		return "delete vertices and geoset";
	}
}
