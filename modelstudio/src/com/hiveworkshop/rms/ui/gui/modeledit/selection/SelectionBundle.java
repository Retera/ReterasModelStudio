package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SelectionBundle {
	private final Set<GeosetVertex> selectedVertices = new HashSet<>();
	private final Set<IdObject> selectedIdObjects = new HashSet<>();
	private final Set<Camera> selectedCameras = new HashSet<>();

	public SelectionBundle(Collection<?>... selected) {
		for (Collection<?> c : selected) {
			addSelection(c);
		}
	}

	private void addSelection(Collection<?> selected) {
		if (!selected.isEmpty()) {
			final Object o = selected.stream().findFirst().get();
			if (o instanceof GeosetVertex) {
				this.selectedVertices.addAll((Collection<GeosetVertex>) selected);
			} else if (o instanceof IdObject) {
				this.selectedIdObjects.addAll((Collection<IdObject>) selected);
			} else if (o instanceof Camera) {
				this.selectedCameras.addAll((Collection<Camera>) selected);
			}
		}
	}


	public Set<GeosetVertex> getSelectedVertices() {
		return selectedVertices;
	}

	public Set<IdObject> getSelectedIdObjects() {
		return selectedIdObjects;
	}

	public Set<Camera> getSelectedCameras() {
		return selectedCameras;
	}
}
