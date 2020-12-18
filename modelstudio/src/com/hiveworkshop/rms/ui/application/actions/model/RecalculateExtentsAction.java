package com.hiveworkshop.rms.ui.application.actions.model;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecalculateExtentsAction implements UndoAction {
	private final ModelView modelView;
	private final Map<Geoset, Map<Animation, ExtLog>> geosetToAnimationToOldExtents = new HashMap<>();
	private final Map<Animation, ExtLog> modelSequenceToOldExtents = new HashMap<>();
	private final ExtLog oldModelExtents;
	private final Map<Geoset, Map<Animation, ExtLog>> geosetToAnimationToNewExtents = new HashMap<>();
	private final Map<Animation, ExtLog> modelSequenceToNewExtents = new HashMap<>();
	private final ExtLog newModelExtents;

	public RecalculateExtentsAction(final ModelView modelView, final List<Geoset> geosetsIncludedForCalculation) {
		this.modelView = modelView;
		double maximumBoundsRadius = 0;
		double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
		for (final Geoset geoset : geosetsIncludedForCalculation) {
			final ExtLog extent = geoset.calculateExtent();
			final Vec3 maximumExtent = extent.getMaximumExtent();
			final Vec3 minimumExtent = extent.getMinimumExtent();
			final double boundsRadius = extent.getBoundsRadius();
			if (boundsRadius > maximumBoundsRadius) {
				maximumBoundsRadius = boundsRadius;
			}
			maxX = Math.max(maximumExtent.x, maxX);
			maxY = Math.max(maximumExtent.y, maxY);
			maxZ = Math.max(maximumExtent.z, maxZ);

			minX = Math.min(minimumExtent.x, minX);
			minY = Math.min(minimumExtent.y, minY);
			minZ = Math.min(minimumExtent.z, minZ);
		}
		newModelExtents = new ExtLog(new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, maxZ), maximumBoundsRadius);

		for (final Geoset modelGeoset : modelView.getModel().getGeosets()) {
			final Map<Animation, ExtLog> animationToOldExtents = new HashMap<>();
			final Map<Animation, ExtLog> animationToNewExtents = new HashMap<>();
			for (final Animation anim : modelGeoset.getAnims()) {
				animationToOldExtents.put(anim, anim.getExtents());
				animationToNewExtents.put(anim, new ExtLog(newModelExtents));
			}
			geosetToAnimationToOldExtents.put(modelGeoset, animationToOldExtents);
			geosetToAnimationToNewExtents.put(modelGeoset, animationToNewExtents);
		}
		for (final Animation sequence : modelView.getModel().getAnims()) {
			modelSequenceToOldExtents.put(sequence, sequence.getExtents());
			modelSequenceToNewExtents.put(sequence, new ExtLog(newModelExtents));
		}
		oldModelExtents = modelView.getModel().getExtents();

	}

	@Override
	public void undo() {
		for (final Map.Entry<Geoset, Map<Animation, ExtLog>> entry : geosetToAnimationToOldExtents.entrySet()) {
			final Map<Animation, ExtLog> animationToOldExtents = entry.getValue();
			for (final Map.Entry<Animation, ExtLog> animationAndOldExtents : animationToOldExtents.entrySet()) {
				final Animation anim = animationAndOldExtents.getKey();
				final ExtLog extents = animationAndOldExtents.getValue();
				anim.setExtents(extents);
			}
			entry.getKey().setExtents(oldModelExtents);
		}
		for (final Map.Entry<Animation, ExtLog> animationAndOldExtents : modelSequenceToOldExtents.entrySet()) {
			final Animation anim = animationAndOldExtents.getKey();
			final ExtLog extents = animationAndOldExtents.getValue();
			anim.setExtents(extents);
		}
		modelView.getModel().setExtents(oldModelExtents);
	}

	@Override
	public void redo() {
		for (final Map.Entry<Geoset, Map<Animation, ExtLog>> entry : geosetToAnimationToNewExtents.entrySet()) {
			final Map<Animation, ExtLog> animationToNewExtents = entry.getValue();
			for (final Map.Entry<Animation, ExtLog> animationAndNewExtents : animationToNewExtents.entrySet()) {
				final Animation anim = animationAndNewExtents.getKey();
				final ExtLog extents = animationAndNewExtents.getValue();
				anim.setExtents(extents);
			}
			entry.getKey().setExtents(newModelExtents);
		}
		for (final Map.Entry<Animation, ExtLog> animationAndNewExtents : modelSequenceToNewExtents.entrySet()) {
			final Animation anim = animationAndNewExtents.getKey();
			final ExtLog extents = animationAndNewExtents.getValue();
			anim.setExtents(extents);
		}
		modelView.getModel().setExtents(newModelExtents);
	}

	@Override
	public String actionName() {
		return "recalculate extents";
	}

}
