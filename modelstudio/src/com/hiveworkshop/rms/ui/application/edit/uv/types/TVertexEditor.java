package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.editor.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.editor.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.editor.actions.uv.StaticMeshUVMoveAction;
import com.hiveworkshop.rms.editor.actions.uv.StaticMeshUVRotateAction;
import com.hiveworkshop.rms.editor.actions.uv.StaticMeshUVScaleAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
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
//		return selectionManager.getCenter();
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

	public GenericMoveAction beginTranslation() {
		return new StaticMeshUVMoveAction(modelView.getSelectedVertices(), uvLayerIndex, Vec2.ORIGIN);
	}

	public GenericRotateAction beginRotation(Vec3 center, byte dim1, byte dim2) {
		return new StaticMeshUVRotateAction(modelView.getSelectedVertices(), uvLayerIndex, center, dim1, dim2);
	}

	public GenericRotateAction beginRotation(Vec3 center, Vec3 axis) {
		return new StaticMeshUVRotateAction(modelView.getSelectedVertices(), uvLayerIndex, center, (byte) 0, (byte) 1);
	}

	public GenericScaleAction beginScaling(Vec3 center) {
		return new StaticMeshUVScaleAction(modelView.getSelectedVertices(), uvLayerIndex, center.getProjected((byte) 0, (byte) 1));
	}

	@Override
	public GenericRotateAction beginSquatTool(Vec3 center, byte firstXYZ, byte secondXYZ) {
		throw new WrongModeException("Unable to use squat tool outside animation editor mode");
	}

	@Override
	public GenericRotateAction beginSquatTool(Vec3 center, Vec3 axis) {
		throw new WrongModeException("Unable to use squat tool outside animation editor mode");
	}

	@Override
	public UndoAction translate(Vec3 v) {
		Vec3 delta = new Vec3(v);
		return new StaticMeshUVMoveAction(modelView.getSelectedVertices(), uvLayerIndex, Vec2.ORIGIN).updateTranslation(delta);
	}

	@Override
	public UndoAction scale(Vec3 center, Vec3 scale) {
		return new StaticMeshUVScaleAction(modelView.getSelectedVertices(), uvLayerIndex, center.getProjected((byte) 0, (byte) 1)).updateScale(scale);
	}

	@Override
	public UndoAction rotate(Vec3 center, Vec3 rotate) {
		return new CompoundAction("rotate", Arrays.asList(
				new StaticMeshUVRotateAction(modelView.getSelectedVertices(), uvLayerIndex, center, (byte) 2, (byte) 1),
				new StaticMeshUVRotateAction(modelView.getSelectedVertices(), uvLayerIndex, center, (byte) 0, (byte) 2)));
				// ToDo fix this? not sure if this is used or what it should rotate...
//		return new CompoundAction("rotate", Arrays.asList(
//				new StaticMeshUVRotateAction(modelView.getSelectedVertices(), uvLayerIndex, center, (byte) 2, (byte) 1).updateRotation(Math.toRadians(rotate.x)),
//				new StaticMeshUVRotateAction(modelView.getSelectedVertices(), uvLayerIndex, center, (byte) 0, (byte) 2).updateRotation(Math.toRadians(rotate.y))))
//				.redo();
	}

	@Override
	public UndoAction setPosition(Vec3 center, Vec3 v) {
		Vec3 delta = Vec3.getDiff(v, center);
		return new StaticMeshUVMoveAction(modelView.getSelectedVertices(), uvLayerIndex, Vec2.ORIGIN).updateTranslation(delta);
	}

	public int getUVLayerIndex() {
		return uvLayerIndex;
	}

	public void setUVLayerIndex(int uvLayerIndex) {
		this.uvLayerIndex = uvLayerIndex;
		// TODO deselect vertices with no such layer
	}

	@Override
	public boolean editorWantsAnimation() {
		return false;
	}
}
