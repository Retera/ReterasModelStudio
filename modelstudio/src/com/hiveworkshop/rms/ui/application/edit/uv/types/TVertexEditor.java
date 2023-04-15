package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.actions.editor.StaticMeshShrinkFattenAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.actions.uv.StaticMeshUVMoveAction;
import com.hiveworkshop.rms.editor.actions.uv.StaticMeshUVRotateAction;
import com.hiveworkshop.rms.editor.actions.uv.StaticMeshUVScaleAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

/**
 * So, in some ideal future this would be an implementation of the ModelEditor
 * interface, I believe, and the editor would be operating on an interface who
 * could capture clicks and convert them into 2D operations regardless of
 * whether the underlying thing being editor was UV or Mesh.
 *
 * It isn't like that right now, though, so this is just going to be a 2D copy pasta.
 */
public class TVertexEditor extends ModelEditor {
	protected final ModelView modelView;
	protected final ModelStructureChangeListener structureChangeListener;
	protected int uvLayerIndex = 0;
	protected SelectionItemTypes selectionType;

	public TVertexEditor(AbstractSelectionManager selectionManager, ModelView modelView, SelectionItemTypes selectionTyp) {
		super(selectionManager, modelView);
		this.modelView = modelView;
		this.structureChangeListener = ModelStructureChangeListener.changeListener;
		this.selectionType = selectionTyp;
	}

	public Vec2 getSelectionCenter() {
		Set<Vec2> tvertices = new HashSet<>(getTVertices(modelView.getSelectedVertices(), uvLayerIndex));
		return Vec2.centerOfGroup(tvertices); // TODO is this correct?
	}

	public static Collection<Vec2> getTVertices(Collection<GeosetVertex> vertexSelection, int uvLayerIndex) {
		List<Vec2> tVertices = new ArrayList<>();
		for (GeosetVertex vertex : vertexSelection) {
			if (uvLayerIndex < vertex.getTverts().size()) {
				tVertices.add(vertex.getTVertex(uvLayerIndex));
			}
		}
		return tVertices;
	}

	public AbstractTransformAction beginTranslation(Mat4 rotMat) {
		return new StaticMeshUVMoveAction(modelView.getSelectedVertices(), uvLayerIndex, Vec3.ZERO, rotMat);
	}

	public AbstractTransformAction beginExtrude(Mat4 rotMat) {
		return beginTranslation(new Mat4());
	}

	public AbstractTransformAction beginExtend(Mat4 rotMat) {
		return beginTranslation(new Mat4());
	}

	public AbstractTransformAction beginRotation(Vec3 center, Vec3 axis, Mat4 rotMat) {
		return new StaticMeshUVRotateAction(modelView.getSelectedVertices(), uvLayerIndex, center, axis, 0, new Mat4());
	}

	public AbstractTransformAction beginScaling(Vec3 center, Mat4 rotMat) {
		return new StaticMeshUVScaleAction(modelView.getSelectedVertices(), uvLayerIndex, center, Vec3.ONE, rotMat);
	}

	@Override
	public AbstractTransformAction beginSquatTool(Vec3 center, Vec3 axis, Mat4 rotMat) {
		return this.beginRotation(center, axis, rotMat);
//		throw new WrongModeException("Unable to use squat tool outside animation editor mode");
	}

	@Override
	public UndoAction translate(Vec3 v, Mat4 rotMat) {
		Vec3 delta = new Vec3(v);
		return new StaticMeshUVMoveAction(modelView.getSelectedVertices(), uvLayerIndex, delta, new Mat4());
	}

	@Override
	public UndoAction scale(Vec3 center, Vec3 scale, Mat4 rotMat) {
		return new StaticMeshUVScaleAction(modelView.getSelectedVertices(), uvLayerIndex, center, scale, new Mat4());
	}

	@Override
	public UndoAction rotate(Vec3 center, Vec3 rotate, Mat4 rotMat) {
		return new CompoundAction("rotate", null,
				new StaticMeshUVRotateAction(modelView.getSelectedVertices(), uvLayerIndex, center, Vec3.X_AXIS, Math.toRadians(rotate.x), new Mat4()),
				new StaticMeshUVRotateAction(modelView.getSelectedVertices(), uvLayerIndex, center, Vec3.NEGATIVE_Y_AXIS, Math.toRadians(rotate.y), new Mat4()),
				new StaticMeshUVRotateAction(modelView.getSelectedVertices(), uvLayerIndex, center, Vec3.NEGATIVE_Z_AXIS, Math.toRadians(rotate.z), new Mat4()));
	}

	@Override
	public UndoAction setPosition(Vec3 center, Vec3 v) {
		Vec3 delta = Vec3.getDiff(v, center);
		return new StaticMeshUVMoveAction(modelView.getSelectedVertices(), uvLayerIndex, delta, new Mat4());
	}

	public int getUVLayerIndex() {
		return uvLayerIndex;
	}

	public void setUVLayerIndex(int uvLayerIndex) {
		this.uvLayerIndex = uvLayerIndex;
		selectionManager.setUvLayerIndex(uvLayerIndex);
		// TODO deselect vertices with no such layer
	}

	@Override
	public boolean editorWantsAnimation() {
		return false;
	}

	public UndoAction shrinkFatten(float amount, boolean scaleApart) {
		return null;
	}
	public StaticMeshShrinkFattenAction beginShrinkFatten(float amount, boolean scaleApart) {
		return null;
	}
}
