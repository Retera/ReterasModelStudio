package com.hiveworkshop.wc3.mdl.v2;

import com.etheller.collections.ListView;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.v2.visitor.ModelVisitor;

public interface ModelView {

	MDL getModel();

	ListView<Geoset> getVisibleGeosets();

	ListView<Geoset> getEditableGeosets();

	ListView<IdObject> getEditableIdObjects();

	ListView<Camera> getEditableCameras();

	void addStateListener(ModelViewStateListener listener);

	void visit(ModelVisitor visitor);

	Geoset getHighlightedGeoset();
}
