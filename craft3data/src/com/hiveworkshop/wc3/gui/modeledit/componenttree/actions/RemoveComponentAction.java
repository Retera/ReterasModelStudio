package com.hiveworkshop.wc3.gui.modeledit.componenttree.actions;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.ComponentKind;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;

/**
 * Removes a sequence, material, geoset, geoset anim or camera, restoring it at
 * its old list position on undo. A geoset takes its geoset anim with it.
 * <p>
 * Textures, texture anims, global sequences and nodes have their own actions
 * because removing them must also repair whatever pointed at them.
 */
public final class RemoveComponentAction implements UndoAction {
	private final EditableModel model;
	private final Object component;
	private final ComponentKind kind;
	private final ModelStructureChangeListener listener;
	private int index = -1;
	private GeosetAnim removedGeosetAnim;
	private int geosetAnimIndex = -1;

	public RemoveComponentAction(final EditableModel model, final Object component,
			final ModelStructureChangeListener listener) {
		this.model = model;
		this.component = component;
		this.kind = ComponentKind.of(component);
		this.listener = listener;
		switch (kind) {
		case SEQUENCE:
		case MATERIAL:
		case GEOSET:
		case GEOSET_ANIM:
		case CAMERA:
			break;
		default:
			throw new IllegalArgumentException("Use the dedicated removal action for " + kind);
		}
	}

	@Override
	public void redo() {
		final List<Object> removed = new ArrayList<>();
		removed.add(component);
		switch (kind) {
		case SEQUENCE:
			index = ComponentListUtil.removeIdentity(model.getAnims(), component);
			break;
		case MATERIAL:
			index = ComponentListUtil.removeIdentity(model.getMaterials(), component);
			break;
		case GEOSET: {
			final Geoset geoset = (Geoset) component;
			index = ComponentListUtil.removeIdentity(model.getGeosets(), geoset);
			removedGeosetAnim = geoset.getGeosetAnim();
			if (removedGeosetAnim != null) {
				geosetAnimIndex = ComponentListUtil.removeIdentity(model.getGeosetAnims(), removedGeosetAnim);
				removed.add(removedGeosetAnim);
			}
			break;
		}
		case GEOSET_ANIM: {
			final GeosetAnim geosetAnim = (GeosetAnim) component;
			index = ComponentListUtil.removeIdentity(model.getGeosetAnims(), geosetAnim);
			if ((geosetAnim.getGeoset() != null) && (geosetAnim.getGeoset().getGeosetAnim() == geosetAnim)) {
				geosetAnim.getGeoset().setGeosetAnim(null);
			}
			break;
		}
		case CAMERA:
			index = ComponentListUtil.removeIdentity(model.getCameras(), component);
			break;
		default:
			break;
		}
		StructureNotifier.removed(listener, model, removed);
	}

	@Override
	public void undo() {
		final List<Object> added = new ArrayList<>();
		added.add(component);
		switch (kind) {
		case SEQUENCE:
			ComponentListUtil.insertAt(model.getAnims(), index, (com.hiveworkshop.wc3.mdl.Animation) component);
			break;
		case MATERIAL:
			ComponentListUtil.insertAt(model.getMaterials(), index, (com.hiveworkshop.wc3.mdl.Material) component);
			break;
		case GEOSET: {
			final Geoset geoset = (Geoset) component;
			ComponentListUtil.insertAt(model.getGeosets(), index, geoset);
			geoset.setParentModel(model);
			if (removedGeosetAnim != null) {
				ComponentListUtil.insertAt(model.getGeosetAnims(), geosetAnimIndex, removedGeosetAnim);
				geoset.setGeosetAnim(removedGeosetAnim);
				added.add(removedGeosetAnim);
			}
			break;
		}
		case GEOSET_ANIM: {
			final GeosetAnim geosetAnim = (GeosetAnim) component;
			ComponentListUtil.insertAt(model.getGeosetAnims(), index, geosetAnim);
			if (geosetAnim.getGeoset() != null) {
				geosetAnim.getGeoset().setGeosetAnim(geosetAnim);
			}
			break;
		}
		case CAMERA:
			ComponentListUtil.insertAt(model.getCameras(), index, (com.hiveworkshop.wc3.mdl.Camera) component);
			break;
		default:
			break;
		}
		StructureNotifier.added(listener, model, added);
	}

	@Override
	public String actionName() {
		return "delete " + kind.getDisplayName().toLowerCase();
	}
}
