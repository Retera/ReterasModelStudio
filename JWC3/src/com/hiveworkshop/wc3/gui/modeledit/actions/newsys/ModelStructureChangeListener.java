package com.hiveworkshop.wc3.gui.modeledit.actions.newsys;

import java.util.List;

import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.IdObject;

public interface ModelStructureChangeListener {
	void geosetsAdded(List<Geoset> geosets);

	void geosetsRemoved(List<Geoset> geosets);

	void nodesAdded(List<IdObject> nodes);

	void nodesRemoved(List<IdObject> nodes);
}
