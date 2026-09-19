package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.IdObject.NodeFlags;
import com.hiveworkshop.wc3.mdl.Vertex;

/**
 * Shared card body for every node type: name, parent, pivot, the generic node
 * flags, the type specific rows contributed by the subclass, and the track
 * summary.
 */
public abstract class ComponentNodeCard<T extends IdObject> extends ComponentCard<T> {
	private final TrackSummaryPanel tracks = new TrackSummaryPanel();

	protected ComponentNodeCard() {
		addTextField("Name", IdObject::getName, IdObject::setName);
		addComboBox("Parent", this::parentOptions, IdObject::getName, IdObject::getParent, IdObject::setParent);
		addVertexRow("Pivot", 1.0, IdObject::getPivotPoint, (node, pivot) -> node.setPivotPoint(pivot));
		addTypeRows();
		beginSection("Node Flags");
		for (final NodeFlags flag : NodeFlags.values()) {
			addCheckBox(flag.getMdlText(), node -> node.hasFlag(flag), (node, value) -> setNodeFlag(node, flag, value));
		}
		endSection();
		addWide(tracks);
	}

	/** Subclasses add their own rows here; called from the constructor. */
	protected abstract void addTypeRows();

	@Override
	protected void onReload() {
		tracks.setTracks(item.getAnimFlags(), navigationListener);
	}

	private List<IdObject> parentOptions() {
		final List<IdObject> options = new ArrayList<>();
		options.add(null);
		for (final IdObject candidate : model().getIdObjects()) {
			if ((candidate != item) && !candidate.childOf(item)) {
				options.add(candidate);
			}
		}
		return options;
	}

	/**
	 * Nodes keep their flags as MDL strings; some (ParticleEmitter2) keep them as
	 * booleans instead, so they override this.
	 */
	protected void setNodeFlag(final T node, final NodeFlags flag, final boolean value) {
		final List<String> flags = node.getFlags();
		flags.removeIf(flag::matches);
		if (value) {
			flags.add(flag.getMdlText());
		}
	}

	protected static Vertex copy(final Vertex vertex) {
		return vertex == null ? null : new Vertex(vertex);
	}
}
