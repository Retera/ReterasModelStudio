package com.hiveworkshop.wc3.gui.modeledit.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.ExtLog;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class RecalculateExtentsAction implements UndoAction {
	private static boolean EACH_GEOSET_GETS_ITS_OWN_BOUNDS = false;
	private final ModelView modelView;
	private final Map<Geoset, ArrayList<Animation>> geosetToOldExtents = new HashMap<>();
	private final Map<Animation, ExtLog> modelSequenceToOldExtents = new HashMap<>();
	private final ExtLog oldModelExtents;
	private final Map<Geoset, ArrayList<Animation>> geosetToNewExtents = new HashMap<>();
	private final Map<Animation, ExtLog> modelSequenceToNewExtents = new HashMap<>();
	private final ExtLog newModelExtents;

	public RecalculateExtentsAction(final ModelView modelView, final List<Geoset> geosetsIncludedForCalculation) {
		this.modelView = modelView;
		double maximumBoundsRadius = 0;
		double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
		for (final Geoset geoset : geosetsIncludedForCalculation) {
			final ExtLog extent = geoset.calculateExtent();
			final Vertex maximumExtent = extent.getMaximumExtent();
			final Vertex minimumExtent = extent.getMinimumExtent();
			final double boundsRadius = extent.getBoundsRadius();
			if (boundsRadius > maximumBoundsRadius) {
				maximumBoundsRadius = boundsRadius;
			}
			if (maximumExtent.x > maxX) {
				maxX = maximumExtent.x;
			}
			if (maximumExtent.y > maxY) {
				maxY = maximumExtent.y;
			}
			if (maximumExtent.z > maxZ) {
				maxZ = maximumExtent.z;
			}
			if (minimumExtent.x < minX) {
				minX = minimumExtent.x;
			}
			if (minimumExtent.y < minY) {
				minY = minimumExtent.y;
			}
			if (minimumExtent.z < minZ) {
				minZ = minimumExtent.z;
			}
		}
		newModelExtents = new ExtLog(new Vertex(minX, minY, minZ), new Vertex(maxX, maxY, maxZ), maximumBoundsRadius);

		for (final Geoset modelGeoset : modelView.getModel().getGeosets()) {
			final ArrayList<Animation> oldExtents = modelGeoset.getAnims();
			geosetToOldExtents.put(modelGeoset, new ArrayList<>(oldExtents));
			final ArrayList<Animation> newExtents = new ArrayList<>();
			for (final Animation anim : modelView.getModel().getAnims()) {
				newExtents.add(new Animation(new ExtLog(newModelExtents)));
			}
			geosetToNewExtents.put(modelGeoset, newExtents);
		}
		for (final Animation sequence : modelView.getModel().getAnims()) {
			modelSequenceToOldExtents.put(sequence, sequence.getExtents());
			modelSequenceToNewExtents.put(sequence, new ExtLog(newModelExtents));
		}
		oldModelExtents = modelView.getModel().getExtents();

	}

	@Override
	public void undo() {
		for (final Map.Entry<Geoset, ArrayList<Animation>> entry : geosetToOldExtents.entrySet()) {
			entry.getKey().setAnims(new ArrayList<>(entry.getValue()));
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
		for (final Map.Entry<Geoset, ArrayList<Animation>> entry : geosetToNewExtents.entrySet()) {
			entry.getKey().setAnims(new ArrayList<>(entry.getValue()));
			if (EACH_GEOSET_GETS_ITS_OWN_BOUNDS) {
				entry.getKey().setExtents(entry.getKey().calculateExtent());
			} else {
				entry.getKey().setExtents(newModelExtents);
			}
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
