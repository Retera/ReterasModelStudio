package com.hiveworkshop.wc3.gui.modeledit.componenttree.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.ComponentKind;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.TextureAnim;

/**
 * Adds one or more freshly created or copied components to a model. Used by
 * New and Paste in the Model tab.
 * <p>
 * Nodes are detached from their parent on undo and re-attached on redo so the
 * parent's child list never holds a node that is not in the model. A
 * duplicated sequence also copies the source sequence's keyframes onto its new
 * interval, and removes them again on undo.
 */
public final class AddComponentsAction implements UndoAction {
	private final EditableModel model;
	private final List<Object> components;
	private final ModelStructureChangeListener listener;
	private final String actionName;
	private final Map<IdObject, IdObject> nodeParents = new HashMap<>();
	private Animation keyframeSource;
	private Animation keyframeTarget;

	public AddComponentsAction(final EditableModel model, final List<Object> components,
			final ModelStructureChangeListener listener, final String actionName) {
		this.model = model;
		this.components = new ArrayList<>(components);
		this.listener = listener;
		this.actionName = actionName;
		for (final Object component : components) {
			if (component instanceof IdObject) {
				nodeParents.put((IdObject) component, ((IdObject) component).getParent());
			}
		}
	}

	/** Copy keyframes of {@code source} onto {@code target}'s interval on redo. */
	public AddComponentsAction withKeyframesFrom(final Animation source, final Animation target) {
		keyframeSource = source;
		keyframeTarget = target;
		return this;
	}

	@Override
	public void redo() {
		for (final Object component : components) {
			add(component);
		}
		if ((keyframeSource != null) && (keyframeTarget != null)) {
			keyframeSource.copyToInterval(keyframeTarget.getStart(), keyframeTarget.getEnd(),
					model.getAllAnimFlags(), model.sortedIdObjects(EventObject.class));
		}
		StructureNotifier.added(listener, model, components);
	}

	@Override
	public void undo() {
		if ((keyframeSource != null) && (keyframeTarget != null)) {
			// the new interval sits after every other sequence, so removing every key
			// inside it removes exactly what redo copied
			for (final AnimFlag flag : model.getAllAnimFlags()) {
				if (!flag.hasGlobalSeq()) {
					flag.deleteAnim(keyframeTarget);
				}
			}
			for (final EventObject eventObject : model.sortedIdObjects(EventObject.class)) {
				if (!eventObject.isHasGlobalSeq()) {
					eventObject.deleteAnim(keyframeTarget);
				}
			}
		}
		for (int i = components.size() - 1; i >= 0; i--) {
			remove(components.get(i));
		}
		StructureNotifier.removed(listener, model, components);
	}

	private void add(final Object component) {
		final ComponentKind kind = ComponentKind.of(component);
		switch (kind) {
		case SEQUENCE:
			model.add((Animation) component);
			break;
		case GLOBAL_SEQUENCE:
			model.add((Integer) component);
			break;
		case TEXTURE:
			model.add((Bitmap) component);
			break;
		case MATERIAL:
			model.add((Material) component);
			break;
		case TEXTURE_ANIM:
			model.add((TextureAnim) component);
			break;
		case GEOSET:
			model.add((Geoset) component);
			break;
		case GEOSET_ANIM: {
			final GeosetAnim geosetAnim = (GeosetAnim) component;
			model.add(geosetAnim);
			if (geosetAnim.getGeoset() != null) {
				geosetAnim.getGeoset().setGeosetAnim(geosetAnim);
			}
			break;
		}
		case CAMERA:
			model.add((Camera) component);
			break;
		default: {
			final IdObject node = (IdObject) component;
			node.setParent(nodeParents.get(node));
			model.add(node);
			break;
		}
		}
	}

	private void remove(final Object component) {
		final ComponentKind kind = ComponentKind.of(component);
		switch (kind) {
		case SEQUENCE:
			ComponentListUtil.removeIdentity(model.getAnims(), component);
			break;
		case GLOBAL_SEQUENCE:
			ComponentListUtil.removeIdentity(model.getGlobalSeqs(), component);
			break;
		case TEXTURE:
			ComponentListUtil.removeIdentity(model.getTextures(), component);
			break;
		case MATERIAL:
			ComponentListUtil.removeIdentity(model.getMaterials(), component);
			break;
		case TEXTURE_ANIM:
			ComponentListUtil.removeIdentity(model.getTexAnims(), component);
			break;
		case GEOSET:
			ComponentListUtil.removeIdentity(model.getGeosets(), component);
			break;
		case GEOSET_ANIM: {
			final GeosetAnim geosetAnim = (GeosetAnim) component;
			ComponentListUtil.removeIdentity(model.getGeosetAnims(), geosetAnim);
			if ((geosetAnim.getGeoset() != null) && (geosetAnim.getGeoset().getGeosetAnim() == geosetAnim)) {
				geosetAnim.getGeoset().setGeosetAnim(null);
			}
			break;
		}
		case CAMERA:
			ComponentListUtil.removeIdentity(model.getCameras(), component);
			break;
		default: {
			final IdObject node = (IdObject) component;
			ComponentListUtil.removeIdentity(model.getIdObjects(), node);
			node.setParent(null);
			break;
		}
		}
	}

	@Override
	public String actionName() {
		return actionName;
	}
}
