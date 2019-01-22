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
}
