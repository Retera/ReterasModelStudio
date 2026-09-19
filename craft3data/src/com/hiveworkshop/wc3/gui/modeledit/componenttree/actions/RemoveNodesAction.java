package com.hiveworkshop.wc3.gui.modeledit.componenttree.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.GeosetVertexBoneLink;
import com.hiveworkshop.wc3.mdl.IdObject;

/**
 * Removes nodes from the Model tab.
 * <p>
 * By default the children of a removed node are reparented to its parent so
 * the rest of the skeleton keeps its shape; with {@code deleteSubtree} the
 * whole subtree goes. Vertex bone links to removed bones are dropped, as the
 * viewport delete does. Everything is restored in order on undo.
 */
public final class RemoveNodesAction implements UndoAction {
	private final EditableModel model;
	private final List<IdObject> removed = new ArrayList<>();
	private final Map<IdObject, Integer> removedIndices = new LinkedHashMap<>();
	private final Map<IdObject, IdObject> removedParents = new LinkedHashMap<>();
	private final Map<IdObject, IdObject> reparentedChildren = new LinkedHashMap<>();
	private final List<VertexLinkRemoval> linkRemovals = new ArrayList<>();
	private final ModelStructureChangeListener listener;
	private final boolean deleteSubtree;

	public RemoveNodesAction(final EditableModel model, final List<IdObject> nodes, final boolean deleteSubtree,
			final ModelStructureChangeListener listener) {
		this.model = model;
		this.listener = listener;
		this.deleteSubtree = deleteSubtree;
		final Set<IdObject> removalSet = new HashSet<>(nodes);
		if (deleteSubtree) {
			boolean grew = true;
			while (grew) {
				grew = false;
				for (final IdObject candidate : model.getIdObjects()) {
					if (!removalSet.contains(candidate) && (candidate.getParent() != null)
							&& removalSet.contains(candidate.getParent())) {
						removalSet.add(candidate);
						grew = true;
					}
				}
			}
		}
		// keep model order so undo re-inserts at stable indices
		for (final IdObject candidate : model.getIdObjects()) {
			if (removalSet.contains(candidate)) {
				removed.add(candidate);
			}
		}
	}

	public List<IdObject> getRemovedNodes() {
		return removed;
	}

	@Override
	public void redo() {
		removedIndices.clear();
		removedParents.clear();
		reparentedChildren.clear();
		linkRemovals.clear();
		final Set<IdObject> removalSet = new HashSet<>(removed);
		if (!deleteSubtree) {
			for (final IdObject candidate : model.getIdObjects()) {
				if (!removalSet.contains(candidate) && removalSet.contains(candidate.getParent())) {
					IdObject survivor = candidate.getParent();
					while ((survivor != null) && removalSet.contains(survivor)) {
						survivor = survivor.getParent();
					}
					reparentedChildren.put(candidate, candidate.getParent());
					candidate.setParent(survivor);
				}
			}
		}
		for (final Geoset geoset : model.getGeosets()) {
			for (final GeosetVertex vertex : geoset.getVertices()) {
				final List<GeosetVertexBoneLink> links = vertex.getLinks();
				for (int i = links.size() - 1; i >= 0; i--) {
					if (removalSet.contains(links.get(i).bone)) {
						linkRemovals.add(new VertexLinkRemoval(vertex, links.get(i), i));
						links.remove(i);
					}
				}
			}
		}
		for (final IdObject node : removed) {
			removedParents.put(node, node.getParent());
			removedIndices.put(node, ComponentListUtil.removeIdentity(model.getIdObjects(), node));
			node.setParent(null);
		}
		listener.nodesRemoved(new ArrayList<>(removed));
	}

	@Override
	public void undo() {
		for (final IdObject node : removed) {
			ComponentListUtil.insertAt(model.getIdObjects(), removedIndices.get(node), node);
		}
		for (final IdObject node : removed) {
			node.setParent(removedParents.get(node));
		}
		for (final Map.Entry<IdObject, IdObject> entry : reparentedChildren.entrySet()) {
			entry.getKey().setParent(entry.getValue());
		}
		for (int i = linkRemovals.size() - 1; i >= 0; i--) {
			linkRemovals.get(i).undo();
		}
		listener.nodesAdded(new ArrayList<>(removed));
	}

	@Override
	public String actionName() {
		return removed.size() == 1 ? "delete " + removed.get(0).getName() : "delete " + removed.size() + " nodes";
	}

	private static final class VertexLinkRemoval {
		private final GeosetVertex vertex;
		private final GeosetVertexBoneLink link;
		private final int index;

		VertexLinkRemoval(final GeosetVertex vertex, final GeosetVertexBoneLink link, final int index) {
			this.vertex = vertex;
			this.link = link;
			this.index = index;
		}

		void undo() {
			final List<GeosetVertexBoneLink> links = vertex.getLinks();
			if (index <= links.size()) {
				links.add(index, link);
			} else {
				links.add(link);
			}
		}
	}
}
