package com.hiveworkshop.rms.editor.actions.model;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RecalculateExtentsAction implements UndoAction {
	private final EditableModel model;
	private final Map<Geoset, Map<Animation, ExtLog>> geosetToAnimationToOldExtents = new HashMap<>();
	private final Map<Geoset, ExtLog> geosetToOldExtents = new HashMap<>();
	private final Map<Animation, ExtLog> modelSequenceToOldExtents = new HashMap<>();
	private final ExtLog oldModelExtents;
	private final Map<Geoset, Map<Animation, ExtLog>> geosetToAnimationToNewExtents = new HashMap<>();
	private final Map<Geoset, ExtLog> geosetToNewExtents = new HashMap<>();
	private final Map<Animation, ExtLog> modelSequenceToNewExtents = new HashMap<>();
	private final ExtLog newModelExtents;

	public RecalculateExtentsAction(EditableModel model, Collection<Geoset> geosetsIncludedForCalculation) {
		this(model, geosetsIncludedForCalculation, true, true);
	}
	public RecalculateExtentsAction(EditableModel model, Collection<Geoset> geosetsIncludedForCalculation, boolean setAll, boolean setModel) {
		this.model = model;
		double maximumBoundsRadius = 0;
		Vec3 max = new Vec3(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
		Vec3 min = new Vec3(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		for (Geoset geoset : geosetsIncludedForCalculation) {
			ExtLog extent = calculateGeosetExtent(geoset);
			Vec3 maximumExtent = extent.getMaximumExtent();
			Vec3 minimumExtent = extent.getMinimumExtent();
			double boundsRadius = extent.getBoundsRadius();
			if (boundsRadius > maximumBoundsRadius) {
				maximumBoundsRadius = boundsRadius;
			}
			max.maximize(maximumExtent);
			min.minimize(minimumExtent);
		}
		newModelExtents = new ExtLog(min, max, maximumBoundsRadius);


		Collection<Geoset> geosToSet = setAll ? model.getGeosets() : geosetsIncludedForCalculation;
		for (Geoset geoset : geosToSet) {
			Map<Animation, ExtLog> animationToOldExtents = new HashMap<>();
			Map<Animation, ExtLog> animationToNewExtents = new HashMap<>();
			for (Animation anim : model.getAnims()) {
				animationToOldExtents.put(anim, geoset.getAnimExtent(anim));
				animationToNewExtents.put(anim, newModelExtents.deepCopy());
			}
			geosetToOldExtents.put(geoset, geoset.getExtents().deepCopy());
			geosetToNewExtents.put(geoset, newModelExtents.deepCopy());
			geosetToAnimationToOldExtents.put(geoset, animationToOldExtents);
			geosetToAnimationToNewExtents.put(geoset, animationToNewExtents);
		}
		for (Animation sequence : model.getAnims()) {
			modelSequenceToOldExtents.put(sequence, sequence.getExtents());
			modelSequenceToNewExtents.put(sequence, newModelExtents.deepCopy());
		}

		oldModelExtents = setModel ? model.getExtents().deepCopy() : null;

	}


	public ExtLog calculateGeosetExtent(Geoset geoset) {
		double maximumDistanceFromCenter = 0;
		Vec3 max = new Vec3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
		Vec3 min = new Vec3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

		for (GeosetVertex geosetVertex : geoset.getVertices()) {
			max.maximize(geosetVertex);
			min.minimize(geosetVertex);

			double distanceFromCenter = geosetVertex.length();
			if (distanceFromCenter > maximumDistanceFromCenter) {
				maximumDistanceFromCenter = distanceFromCenter;
			}
		}
		return new ExtLog(min, max, maximumDistanceFromCenter);
	}

	@Override
	public RecalculateExtentsAction undo() {
		for (Geoset geoset : geosetToAnimationToOldExtents.keySet()) {
			Map<Animation, ExtLog> animationExtLogMap = geosetToAnimationToOldExtents.get(geoset);
			for (Animation animation : animationExtLogMap.keySet()) {
				geoset.add(animation, animationExtLogMap.get(animation));
			}
			geoset.setExtents(geosetToOldExtents.get(geoset));
		}

		for (Animation animation : modelSequenceToOldExtents.keySet()) {
			animation.setExtents(modelSequenceToOldExtents.get(animation));
		}

		if (oldModelExtents != null) {
			model.setExtents(oldModelExtents);
		}
		return this;
	}

	@Override
	public RecalculateExtentsAction redo() {
		for (Geoset geoset : geosetToAnimationToNewExtents.keySet()) {
			Map<Animation, ExtLog> animationExtLogMap = geosetToAnimationToNewExtents.get(geoset);
			for (Animation animation : animationExtLogMap.keySet()) {
				geoset.add(animation, animationExtLogMap.get(animation));
			}
			geoset.setExtents(geosetToNewExtents.get(geoset));
		}

		for (Animation animation : modelSequenceToNewExtents.keySet()) {
			animation.setExtents(modelSequenceToNewExtents.get(animation));
		}

		if (oldModelExtents != null) {
			model.setExtents(newModelExtents);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Recalculate Extents";
	}

}
