package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.actions.uv.UVRemapAction;
import com.hiveworkshop.rms.ui.application.actions.uv.UVSnapAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.SetSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions.*;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public abstract class AbstractTVertexEditor<T> extends AbstractSelectingTVertexEditor<T> {
	protected final ModelView model;
	protected final VertexSelectionHelper vertexSelectionHelper;
	protected final ModelStructureChangeListener structureChangeListener;
	protected int uvLayerIndex;

	public AbstractTVertexEditor(final SelectionManager<T> selectionManager,
	                             final ModelView model,
	                             final ModelStructureChangeListener structureChangeListener) {
		super(selectionManager);
		this.model = model;
		this.structureChangeListener = structureChangeListener;
		vertexSelectionHelper = this::selectByVertices;
	}

	@Override
	public UndoAction mirror(final byte dim, final double centerX, final double centerY) {
		final MirrorTVerticesAction mirror =
				new MirrorTVerticesAction(TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex), dim, centerX, centerY);
		// super weird passing of currently editable id Objects, works because
		// mirror action checks selected vertices against pivot points from this
		// list
		mirror.redo();
		return mirror;
	}

	@Override
	public UndoAction remap(final byte xDim, final byte yDim, final UVPanel.UnwrapDirection unwrapDirection) {
		final List<Vec2> tVertices = new ArrayList<>();
		final List<Vec2> newValueHolders = new ArrayList<>();
		final List<Vec2> oldValueHolders = new ArrayList<>();

		Vec2 min = new Vec2(Float.MAX_VALUE, Float.MAX_VALUE);
		Vec2 max = new Vec2(Float.MIN_VALUE, Float.MIN_VALUE);
//		float minX = Float.MAX_VALUE;
//		float minY = Float.MAX_VALUE;
//		float maxX = -Float.MAX_VALUE;
//		float maxY = -Float.MAX_VALUE;
		for (final Vec3 vertex : selectionManager.getSelectedVertices()) {
			if (vertex instanceof GeosetVertex) {
				final GeosetVertex geosetVertex = (GeosetVertex) vertex;
				if (uvLayerIndex < geosetVertex.getTverts().size()) {
					final Vec2 modelDataTVertex = geosetVertex.getTVertex(uvLayerIndex);
					tVertices.add(modelDataTVertex);
					oldValueHolders.add(new Vec2(modelDataTVertex.x, modelDataTVertex.y));
					final Vec2 newCoordValue = new Vec2(vertex.getCoord(xDim), vertex.getCoord(yDim));

					max.set(Math.max(max.x, newCoordValue.x), Math.max(max.y, newCoordValue.y));
					min.set(Math.min(min.x, newCoordValue.x), Math.min(min.y, newCoordValue.y));
//					maxX = Math.max(maxX, newCoordValue.x);
//					minX = Math.min(minX, newCoordValue.x);
//
//					maxY = Math.max(maxY, newCoordValue.y);
//					minY = Math.min(minY, newCoordValue.y);

					newValueHolders.add(newCoordValue);
				}
			}
		}
		Vec2 width = Vec2.getDif(max, min);

		if (width.x == 0) {
			width.x = 0.01f;
		}
		if (width.y == 0) {
			width.y = 0.01f;
		}

//		float widthX = (maxX - minX);
//		float widthY = (maxY - minY);
//		if (widthX == 0) {
//			widthX = 0.01f;
//		}
//		if (widthY == 0) {
//			widthY = 0.01f;
//		}
		for (final Vec2 tv : newValueHolders) {
			tv.sub(min).div(width);
//			tv.x = (tv.x - minX) / widthX;
//			tv.y = (tv.y - minY) / widthY;
		}
		final UVRemapAction uvRemapAction = new UVRemapAction(tVertices, newValueHolders, oldValueHolders, unwrapDirection);
		uvRemapAction.redo();
		return uvRemapAction;
	}

	@Override
	public UndoAction snapSelectedVertices() {
		final Collection<? extends Vec2> selection = TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex);
		final List<Vec2> oldLocations = new ArrayList<>();
		final Vec2 cog = Vec2.centerOfGroup(selection);
		for (final Vec2 vertex : selection) {
			oldLocations.add(new Vec2(vertex));
		}
		final UVSnapAction temp = new UVSnapAction(selection, oldLocations, cog);
		temp.redo();// a handy way to do the snapping!
		return temp;
	}

	@Override
	public void rawTranslate(final double x, final double y) {
		for (final Vec2 vertex : TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex)) {
			vertex.translate(x, y);
		}
	}

	@Override
	public void rawScale(final double centerX, final double centerY,
	                     final double scaleX, final double scaleY) {
		for (final Vec2 vertex : TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex)) {
			vertex.scale(centerX, centerY, scaleX, scaleY);
		}
	}

	@Override
	public void rawRotate2d(final double centerX, final double centerY,
	                        final double radians,
	                        final byte firstXYZ, final byte secondXYZ) {
		for (final Vec2 vertex : TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex)) {
			vertex.rotate(centerX, centerY, radians, firstXYZ, secondXYZ);
		}
	}

	@Override
	public UndoAction translate(final double x, final double y) {
		final Vec2 delta = new Vec2(x, y);
		final StaticMeshUVMoveAction moveAction = new StaticMeshUVMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public UndoAction setPosition(final Vec2 center, final double x, final double y) {
		final Vec2 delta = new Vec2(x - center.x, y - center.y);
		final StaticMeshUVMoveAction moveAction = new StaticMeshUVMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public UndoAction rotate(final Vec2 center, final double rotateRadians) {
		final SimpleRotateUVAction compoundAction = new SimpleRotateUVAction(this, center, rotateRadians);
		compoundAction.redo();
		return compoundAction;
	}

	@Override
	public Vec2 getSelectionCenter() {
//		return selectionManager.getCenter();
		final Set<Vec2> tvertices = new HashSet<>(TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex));
		return Vec2.centerOfGroup(tvertices); // TODO is this correct?
	}

	@Override
	public UndoAction selectFromViewer(final SelectionView viewerSelectionView) {
		final Set<T> previousSelection = selectionManager.getSelection();
		selectByVertices(viewerSelectionView.getSelectedVertices());
		final SetSelectionAction<T> setSelectionAction =
				new SetSelectionAction<>(selectionManager.getSelection(), previousSelection, selectionManager, "select UV from viewer");
		return setSelectionAction;
	}

	@Override
	public GenericMoveAction beginTranslation() {
		return new StaticMeshUVMoveAction(this, Vec2.ORIGIN);
	}

	@Override
	public GenericRotateAction beginRotation(final double centerX, final double centerY,
	                                         final byte dim1, final byte dim2) {
		return new StaticMeshUVRotateAction(this, new Vec2(centerX, centerY), dim1, dim2);
	}

	@Override
	public GenericScaleAction beginScaling(final double centerX, final double centerY) {
		return new StaticMeshUVScaleAction(this, centerX, centerY);
	}

	@Override
	public void setUVLayerIndex(final int uvLayerIndex) {
		this.uvLayerIndex = uvLayerIndex;
		// TODO deselect vertices with no such layer
	}

	@Override
	public int getUVLayerIndex() {
		return uvLayerIndex;
	}
}
