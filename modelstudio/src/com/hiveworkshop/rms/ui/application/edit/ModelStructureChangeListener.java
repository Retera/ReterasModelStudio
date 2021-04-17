package com.hiveworkshop.rms.ui.application.edit;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;

import java.util.List;

public interface ModelStructureChangeListener {
	void geosetsAdded(List<Geoset> geosets);

	void geosetsRemoved(List<Geoset> geosets);

	void nodesAdded(List<IdObject> nodes);

	void nodesRemoved(List<IdObject> nodes);

	void camerasAdded(List<Camera> nodes);

	void camerasRemoved(List<Camera> nodes);

	void timelineAdded(TimelineContainer node, AnimFlag<?> timeline);

	void keyframeAdded(TimelineContainer node, AnimFlag<?> timeline, int trackTime);

	void timelineRemoved(TimelineContainer node, AnimFlag<?> timeline);

	void keyframeRemoved(TimelineContainer node, AnimFlag<?> timeline, int trackTime);

	void animationsAdded(List<Animation> animation);

	void animationsRemoved(List<Animation> animation);

	void texturesChanged();

	void headerChanged();

	void animationParamsChanged(Animation animation);

	void globalSequenceLengthChanged(int index, Integer newLength);

	void materialsListChanged();

	void nodeHierarchyChanged();
}
