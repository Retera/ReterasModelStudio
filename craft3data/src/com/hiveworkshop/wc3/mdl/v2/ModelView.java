package com.hiveworkshop.wc3.mdl.v2;

import com.etheller.collections.SetView;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.v2.visitor.MeshVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.ModelVisitor;

public interface ModelView {

	MDL getModel();

	SetView<Geoset> getVisibleGeosets();

	SetView<Geoset> getEditableGeosets();

	SetView<IdObject> getEditableIdObjects();

	SetView<Camera> getEditableCameras();

	void addStateListener(ModelViewStateListener listener);

	void visit(ModelVisitor visitor);

	void visitMesh(MeshVisitor visitor);

	Geoset getHighlightedGeoset();

	IdObject getHighlightedNode();
}
