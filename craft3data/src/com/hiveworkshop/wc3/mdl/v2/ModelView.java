package com.hiveworkshop.wc3.mdl.v2;

import com.etheller.collections.SetView;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.v2.visitor.MeshVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.ModelVisitor;

public interface ModelView {

	EditableModel getModel();

	SetView<Geoset> getVisibleGeosets();

	SetView<Geoset> getEditableGeosets();

	SetView<IdObject> getEditableIdObjects();

	SetView<Camera> getEditableCameras();

	/** Nodes drawn but locked; editable nodes are not necessarily in this set. */
	SetView<IdObject> getVisibleIdObjects();

	/** Cameras drawn but locked; editable cameras are not necessarily in this set. */
	SetView<Camera> getVisibleCameras();

	ComponentVisibility getGeosetVisibility(Geoset geoset);

	ComponentVisibility getIdObjectVisibility(IdObject node);

	ComponentVisibility getCameraVisibility(Camera camera);

	void addStateListener(ModelViewStateListener listener);

	void visit(ModelVisitor visitor);

	void visitMesh(MeshVisitor visitor);

	Geoset getHighlightedGeoset();

	IdObject getHighlightedNode();
}
