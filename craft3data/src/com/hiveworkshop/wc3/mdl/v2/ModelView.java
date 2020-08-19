package com.hiveworkshop.wc3.mdl.v2;

import java.util.Set;

import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.v2.visitor.MeshVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.ModelVisitor;

public interface ModelView {

	EditableModel getModel();

	Set<Geoset> getVisibleGeosets();

	Set<Geoset> getEditableGeosets();

	Set<IdObject> getEditableIdObjects();

	Set<Camera> getEditableCameras();

	void addStateListener(ModelViewStateListener listener);

	void visit(ModelVisitor visitor);

	void visitMesh(MeshVisitor visitor);

	Geoset getHighlightedGeoset();

	IdObject getHighlightedNode();
}
