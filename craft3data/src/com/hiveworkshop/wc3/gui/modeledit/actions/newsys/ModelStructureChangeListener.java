package com.hiveworkshop.wc3.gui.modeledit.actions.newsys;

import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.TimelineContainer;

public interface ModelStructureChangeListener {
	void geosetsAdded(List<Geoset> geosets);

	void geosetsRemoved(List<Geoset> geosets);

	void nodesAdded(List<IdObject> nodes);

	void nodesRemoved(List<IdObject> nodes);

	void camerasAdded(List<Camera> nodes);

	void camerasRemoved(List<Camera> nodes);

	void timelineAdded(TimelineContainer node, AnimFlag timeline);

	void keyframeAdded(TimelineContainer node, AnimFlag timeline, int trackTime);

	void timelineRemoved(TimelineContainer node, AnimFlag timeline);

	void keyframeRemoved(TimelineContainer node, AnimFlag timeline, int trackTime);

	void animationsAdded(List<Animation> animation);

	void animationsRemoved(List<Animation> animation);

	void texturesChanged();

	void headerChanged();

	void animationParamsChanged(Animation animation);

	void globalSequenceLengthChanged(int index, Integer newLength);

	ModelStructureChangeListener DO_NOTHING = new ModelStructureChangeListener() {

		@Override
		public void timelineRemoved(final TimelineContainer node, final AnimFlag timeline) {
		}

		@Override
		public void timelineAdded(final TimelineContainer node, final AnimFlag timeline) {
		}

		@Override
		public void texturesChanged() {
		}

		@Override
		public void nodesRemoved(final List<IdObject> nodes) {
		}

		@Override
		public void nodesAdded(final List<IdObject> nodes) {
		}

		@Override
		public void keyframeRemoved(final TimelineContainer node, final AnimFlag timeline, final int trackTime) {
		}

		@Override
		public void keyframeAdded(final TimelineContainer node, final AnimFlag timeline, final int trackTime) {
		}

		@Override
		public void headerChanged() {
		}

		@Override
		public void globalSequenceLengthChanged(final int index, final Integer newLength) {
		}

		@Override
		public void geosetsRemoved(final List<Geoset> geosets) {
		}

		@Override
		public void geosetsAdded(final List<Geoset> geosets) {
		}

		@Override
		public void camerasRemoved(final List<Camera> nodes) {
		}

		@Override
		public void camerasAdded(final List<Camera> nodes) {
		}

		@Override
		public void animationsRemoved(final List<Animation> animation) {
		}

		@Override
		public void animationsAdded(final List<Animation> animation) {
		}

		@Override
		public void animationParamsChanged(final Animation animation) {
		}
	};
}
