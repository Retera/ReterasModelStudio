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
	private final Map<Animation, ExtLog> modelSequenceToOldExtents = new HashMap<>();
	private final ExtLog oldModelExtents;
	private final Map<Geoset, Map<Animation, ExtLog>> geosetToAnimationToNewExtents = new HashMap<>();
	private final Map<Animation, ExtLog> modelSequenceToNewExtents = new HashMap<>();
	private final ExtLog newModelExtents;

	public RecalculateExtentsAction(EditableModel model, Collection<Geoset> geosetsIncludedForCalculation) {
//		this.modelView = modelView;
		this.model = model;
		double maximumBoundsRadius = 0;
		Vec3 max = new Vec3(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
		Vec3 min = new Vec3(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		for (Geoset geoset : geosetsIncludedForCalculation) {
//			ExtLog extent = geoset.calculateExtent();
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
		newModelExtents = new ExtLog(new Vec3(min), new Vec3(max), maximumBoundsRadius);


		for (Geoset modelGeoset : model.getGeosets()) {
//		for (Geoset modelGeoset : geosetsIncludedForCalculation) {
			Map<Animation, ExtLog> animationToOldExtents = new HashMap<>();
			Map<Animation, ExtLog> animationToNewExtents = new HashMap<>();
			for (Animation anim : modelGeoset.getAnims()) {
				animationToOldExtents.put(anim, anim.getExtents());
				animationToNewExtents.put(anim, newModelExtents.deepCopy());
			}
			geosetToAnimationToOldExtents.put(modelGeoset, animationToOldExtents);
			geosetToAnimationToNewExtents.put(modelGeoset, animationToNewExtents);
		}
		for (Animation sequence : model.getAnims()) {
			modelSequenceToOldExtents.put(sequence, sequence.getExtents());
			modelSequenceToNewExtents.put(sequence, newModelExtents.deepCopy());
		}
		oldModelExtents = model.getExtents();

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
//		System.out.println("Geoset ExtLog: " + new ExtLog(min, max, maximumDistanceFromCenter));
		return new ExtLog(min, max, maximumDistanceFromCenter);
	}

	@Override
	public UndoAction undo() {
		for (Geoset geoset : geosetToAnimationToOldExtents.keySet()) {
			Map<Animation, ExtLog> animationExtLogMap = geosetToAnimationToOldExtents.get(geoset);
			for (Animation animation : animationExtLogMap.keySet()) {
				animation.setExtents(animationExtLogMap.get(animation));

			}
			geoset.setExtents(oldModelExtents);
		}

		for (Animation animation : modelSequenceToOldExtents.keySet()) {
			animation.setExtents(modelSequenceToOldExtents.get(animation));
		}

		model.setExtents(oldModelExtents);
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Geoset geoset : geosetToAnimationToNewExtents.keySet()) {
			Map<Animation, ExtLog> animationExtLogMap = geosetToAnimationToNewExtents.get(geoset);
			for (Animation animation : animationExtLogMap.keySet()) {
				animation.setExtents(animationExtLogMap.get(animation));

			}
			geoset.setExtents(newModelExtents);
		}

		for (Animation animation : modelSequenceToNewExtents.keySet()) {
			animation.setExtents(modelSequenceToNewExtents.get(animation));
		}

		model.setExtents(newModelExtents);
		return this;
	}

	@Override
	public String actionName() {
		return "recalculate extents";
	}

}
