package com.hiveworkshop.rms.editor.wrapper.v2;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.visitor.MeshVisitor;
import com.hiveworkshop.rms.editor.model.visitor.ModelVisitor;
import com.hiveworkshop.rms.editor.render3d.RenderModel;

import java.util.Set;

public interface ModelView {

	EditableModel getModel();

	RenderModel getEditorRenderModel();

	Set<Geoset> getVisibleGeosets();

	Set<Geoset> getEditableGeosets();

	Set<IdObject> getEditableIdObjects();

	Set<Camera> getEditableCameras();

	void addStateListener(ModelViewStateListener listener);

	void visit(ModelVisitor visitor);

	void visitMesh(MeshVisitor visitor);

	Geoset getHighlightedGeoset();

	IdObject getHighlightedNode();

	boolean isVetoOverrideParticles();

	void setVetoOverrideParticles(boolean override);
}
