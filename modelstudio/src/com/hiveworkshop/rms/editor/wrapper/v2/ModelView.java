package com.hiveworkshop.rms.editor.wrapper.v2;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.visitor.GeosetVisitor;
import com.hiveworkshop.rms.editor.model.visitor.MeshVisitor;
import com.hiveworkshop.rms.editor.model.visitor.ModelVisitor;
import com.hiveworkshop.rms.editor.model.visitor.TriangleVisitor;
import com.hiveworkshop.rms.editor.render3d.RenderModel;

import java.util.HashSet;
import java.util.Set;

public final class ModelView {
	private final EditableModel model;
	private final RenderModel editorRenderModel;
	private final ModelViewStateNotifier modelViewStateNotifier;
	private final Set<Geoset> editableGeosets;
	private final Set<Geoset> visibleGeosets;
	private final Set<IdObject> editableIdObjects;
	private final Set<Camera> editableCameras;
	private Geoset highlightedGeoset;
	private IdObject highlightedNode;
	private boolean vetoParticles = false;

	public ModelView(final EditableModel model) {
		this.model = model;
		editorRenderModel = new RenderModel(this.model, this);

		modelViewStateNotifier = new ModelViewStateNotifier();
		editableGeosets = new HashSet<>();
		for (final Geoset geoset : model.getGeosets()) {
			if (!ModelUtils.isLevelOfDetailSupported(model.getFormatVersion()) || (geoset.getLevelOfDetail() == 0)) {
				editableGeosets.add(geoset);
			}
		}
		visibleGeosets = new HashSet<>();
		editableIdObjects = new HashSet<>();
		editableCameras = new HashSet<>();
	}

	public RenderModel getEditorRenderModel() {
		return editorRenderModel;
	}

	public void visit(final ModelVisitor visitor) {
		int geosetId = 0;
		for (final Geoset geoset : model.getGeosets()) {
			final GeosetVisitor geosetRenderer = visitor.beginGeoset(geosetId++, geoset.getMaterial(), geoset.getGeosetAnim());
			boolean isHD = isHd(model, geoset);
			for (Triangle triangle : geoset.getTriangles()) {
				TriangleVisitor triangleRenderer = geosetRenderer.beginTriangle();
				for (GeosetVertex vertex : triangle.getVerts()) {
					triangleRenderer.vertex(vertex, isHD);
				}
				triangleRenderer.triangleFinished();
			}
		}
		for (IdObject object : model.getAllObjects()) {
			visitor.visitIdObject(object);
		}
		for (final Camera camera : model.getCameras()) {
			visitor.camera(camera);
		}
	}

	public boolean isHd(EditableModel model, Geoset geoset) {
		return (ModelUtils.isTangentAndSkinSupported(model.getFormatVersion()))
				&& (geoset.getVertices().size() > 0)
				&& (geoset.getVertex(0).getSkinBoneBones() != null);
	}

//	public void visit(final ModelVisitor visitor) {
////		model.render(renderByViewModelRenderer.reset(visitor));
//		UvIlandThing.render(renderByViewModelRenderer.reset(visitor), model);
//	}

	public void addStateListener(final ModelViewStateListener listener) {
		modelViewStateNotifier.subscribe(listener);
	}

	public Set<Geoset> getVisibleGeosets() {
		return visibleGeosets;
	}

	public Set<Geoset> getEditableGeosets() {
		return editableGeosets;
	}

	public Set<IdObject> getEditableIdObjects() {
		return editableIdObjects;
	}

	public Set<Camera> getEditableCameras() {
		return editableCameras;
	}

	public EditableModel getModel() {
		return model;
	}

	public Geoset getHighlightedGeoset() {
		return highlightedGeoset;
	}

	public IdObject getHighlightedNode() {
		return highlightedNode;
	}

	public void makeGeosetEditable(final Geoset geoset) {
		editableGeosets.add(geoset);
		modelViewStateNotifier.geosetEditable(geoset);
	}

	public void makeGeosetNotEditable(final Geoset geoset) {
		editableGeosets.remove(geoset);
		modelViewStateNotifier.geosetNotEditable(geoset);
	}

	public void makeGeosetVisible(final Geoset geoset) {
		visibleGeosets.add(geoset);
		modelViewStateNotifier.geosetVisible(geoset);
	}

	public void makeGeosetNotVisible(final Geoset geoset) {
		visibleGeosets.remove(geoset);
		modelViewStateNotifier.geosetNotVisible(geoset);
	}

	public void makeIdObjectVisible(final IdObject bone) {
		editableIdObjects.add(bone);
		modelViewStateNotifier.idObjectVisible(bone);
	}

	public void makeIdObjectNotVisible(final IdObject bone) {
		editableIdObjects.remove(bone);
		modelViewStateNotifier.idObjectNotVisible(bone);
	}

	public void makeCameraVisible(final Camera camera) {
		editableCameras.add(camera);
		modelViewStateNotifier.cameraVisible(camera);
	}

	public void makeCameraNotVisible(final Camera camera) {
		editableCameras.remove(camera);
		modelViewStateNotifier.cameraNotVisible(camera);
	}

	public void highlightGeoset(final Geoset geoset) {
		highlightedGeoset = geoset;
		modelViewStateNotifier.highlightGeoset(geoset);
	}

	public void unhighlightGeoset(final Geoset geoset) {
		if (highlightedGeoset == geoset) {
			highlightedGeoset = null;
		}
		modelViewStateNotifier.unhighlightGeoset(geoset);
	}

	public void highlightNode(final IdObject node) {
		highlightedNode = node;
		modelViewStateNotifier.highlightNode(node);
	}

	public void unhighlightNode(final IdObject node) {
		if (highlightedNode == node) {
			highlightedNode = null;
		}
		modelViewStateNotifier.unhighlightNode(node);

	}

	public boolean isVetoOverrideParticles() {
		return vetoParticles;
	}

	public void setVetoOverrideParticles(boolean override) {
		vetoParticles = override;
	}

	public void visitMesh(MeshVisitor visitor) {
		int geosetId = 0;
		for (Geoset geoset : model.getGeosets()) {
			GeosetVisitor geosetRenderer = visitor.beginGeoset(geosetId++, geoset.getMaterial(), geoset.getGeosetAnim());
			boolean isHD = isHd(model, geoset);
			for (Triangle triangle : geoset.getTriangles()) {
				TriangleVisitor triangleRenderer = geosetRenderer.beginTriangle();
				for (GeosetVertex vertex : triangle.getVerts()) {
					triangleRenderer.vertex(vertex, isHD);
				}
				triangleRenderer.triangleFinished();
			}
		}

	}
//	public void visitMesh(final MeshVisitor visitor) {
//		UvIlandThing.renderGeosets(renderByViewMeshRenderer.reset(visitor), model);
//	}
}
