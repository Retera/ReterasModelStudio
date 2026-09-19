package com.hiveworkshop.wc3.gui.modeledit.componenttree.actions;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.ComponentKind;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.IdObject;

/**
 * Fires the right {@link ModelStructureChangeListener} callbacks for a set of
 * components that were just added or removed. The listener implementation in
 * the main window is what refreshes trees, viewports and the animation
 * controller, so every action routes through here in both directions.
 */
final class StructureNotifier {
	private StructureNotifier() {
	}

	static void added(final ModelStructureChangeListener listener, final EditableModel model,
			final List<Object> components) {
		fire(listener, model, components, true);
	}

	static void removed(final ModelStructureChangeListener listener, final EditableModel model,
			final List<Object> components) {
		fire(listener, model, components, false);
	}

	private static void fire(final ModelStructureChangeListener listener, final EditableModel model,
			final List<Object> components, final boolean added) {
		final List<Animation> animations = new ArrayList<>();
		final List<Geoset> geosets = new ArrayList<>();
		final List<IdObject> nodes = new ArrayList<>();
		final List<Camera> cameras = new ArrayList<>();
		boolean textures = false;
		boolean globalSeqs = false;
		for (final Object component : components) {
			final ComponentKind kind = ComponentKind.of(component);
			if (kind == null) {
				continue;
			}
			switch (kind) {
			case SEQUENCE:
				animations.add((Animation) component);
				break;
			case GLOBAL_SEQUENCE:
				globalSeqs = true;
				break;
			case TEXTURE:
			case MATERIAL:
			case TEXTURE_ANIM:
			case GEOSET_ANIM:
				textures = true;
				break;
			case GEOSET:
				geosets.add((Geoset) component);
				break;
			case CAMERA:
				cameras.add((Camera) component);
				break;
			default:
				nodes.add((IdObject) component);
				break;
			}
		}
		if (!nodes.isEmpty()) {
			if (added) {
				listener.nodesAdded(nodes);
			} else {
				listener.nodesRemoved(nodes);
			}
		}
		if (!geosets.isEmpty()) {
			if (added) {
				listener.geosetsAdded(geosets);
			} else {
				listener.geosetsRemoved(geosets);
			}
		}
		if (!cameras.isEmpty()) {
			if (added) {
				listener.camerasAdded(cameras);
			} else {
				listener.camerasRemoved(cameras);
			}
		}
		if (!animations.isEmpty()) {
			if (added) {
				listener.animationsAdded(animations);
			} else {
				listener.animationsRemoved(animations);
			}
		}
		if (globalSeqs) {
			listener.globalSequenceLengthChanged(model.getGlobalSeqs().size() - 1, null);
		}
		if (textures) {
			listener.texturesChanged();
		}
	}
}
