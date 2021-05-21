package com.hiveworkshop.rms.ui.application.actions.model;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RecalculateExtentsAction implements UndoAction {
	private final ModelView modelView;
	private final Map<Geoset, Map<Animation, ExtLog>> geosetToAnimationToOldExtents = new HashMap<>();
	private final Map<Animation, ExtLog> modelSequenceToOldExtents = new HashMap<>();
	private final ExtLog oldModelExtents;
	private final Map<Geoset, Map<Animation, ExtLog>> geosetToAnimationToNewExtents = new HashMap<>();
	private final Map<Animation, ExtLog> modelSequenceToNewExtents = new HashMap<>();
	private final ExtLog newModelExtents;

	public RecalculateExtentsAction(ModelView modelView, Collection<Geoset> geosetsIncludedForCalculation) {
//		this.modelView = modelView;
		this.modelView = ProgramGlobals.getCurrentModelPanel().getModelView();
		double maximumBoundsRadius = 0;
		Vec3 max = new Vec3(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
		Vec3 min = new Vec3(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		for (Geoset geoset : geosetsIncludedForCalculation) {
			ExtLog extent = geoset.calculateExtent();
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

		for (Geoset modelGeoset : modelView.getModel().getGeosets()) {
//		for (Geoset modelGeoset : geosetsIncludedForCalculation) {
			Map<Animation, ExtLog> animationToOldExtents = new HashMap<>();
			Map<Animation, ExtLog> animationToNewExtents = new HashMap<>();
			for (Animation anim : modelGeoset.getAnims()) {
				animationToOldExtents.put(anim, anim.getExtents());
				animationToNewExtents.put(anim, new ExtLog(newModelExtents));
			}
			geosetToAnimationToOldExtents.put(modelGeoset, animationToOldExtents);
			geosetToAnimationToNewExtents.put(modelGeoset, animationToNewExtents);
		}
		for (Animation sequence : modelView.getModel().getAnims()) {
			modelSequenceToOldExtents.put(sequence, sequence.getExtents());
			modelSequenceToNewExtents.put(sequence, new ExtLog(newModelExtents));
		}
		oldModelExtents = modelView.getModel().getExtents();

	}

	@Override
	public void undo() {
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

		modelView.getModel().setExtents(oldModelExtents);
	}

	@Override
	public void redo() {
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

		modelView.getModel().setExtents(newModelExtents);
	}

	@Override
	public String actionName() {
		return "recalculate extents";
	}

}
