package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions.MirrorTVerticesAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions.SimpleRotateUVAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions.StaticMeshUVMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions.StaticMeshUVRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions.StaticMeshUVScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vertex2;
import com.hiveworkshop.rms.util.Vertex3;

public abstract class AbstractTVertexEditor<T> extends AbstractSelectingTVertexEditor<T> {
	protected final ModelView model;
	protected final VertexSelectionHelper vertexSelectionHelper;
	protected final ModelStructureChangeListener structureChangeListener;
	protected int uvLayerIndex;

	public AbstractTVertexEditor(final SelectionManager<T> selectionManager, final ModelView model,
			final ModelStructureChangeListener structureChangeListener) {
		super(selectionManager);
		this.model = model;
		this.structureChangeListener = structureChangeListener;
		vertexSelectionHelper = new VertexSelectionHelper() {
			@Override
			public void selectVertices(final Collection<Vertex3> vertices) {
				selectByVertices(vertices);
			}
		};
	}

	@Override
	public UndoAction mirror(final byte dim, final double centerX, final double centerY) {
		final MirrorTVerticesAction mirror = new MirrorTVerticesAction(
				TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex), dim, centerX, centerY);
		// super weird passing of currently editable id Objects, works because
		// mirror action checks selected vertices against pivot points from this
		// list
		mirror.redo();
		return mirror;
	}

	@Override
	public UndoAction remap(final byte xDim, final byte yDim, final UVPanel.UnwrapDirection unwrapDirection) {
		final List<Vertex2> tVertices = new ArrayList<Vertex2>();
		final List<Vertex2> newValueHolders = new ArrayList<Vertex2>();
		final List<Vertex2> oldValueHolders = new ArrayList<Vertex2>();
		float minX = Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float maxX = -Float.MAX_VALUE;
		float maxY = -Float.MAX_VALUE;
		for (final Vertex3 vertex : selectionManager.getSelectedVertices()) {
			if (vertex instanceof GeosetVertex) {
				final GeosetVertex geosetVertex = (GeosetVertex) vertex;
				if (uvLayerIndex < geosetVertex.getTverts().size()) {
					final Vertex2 modelDataTVertex = geosetVertex.getTVertex(uvLayerIndex);
					tVertices.add(modelDataTVertex);
					oldValueHolders.add(new Vertex2(modelDataTVertex.x, modelDataTVertex.y));
					final Vertex2 newCoordValue = new Vertex2(vertex.getCoord(xDim), vertex.getCoord(yDim));
					if (newCoordValue.x > maxX) {
						maxX = newCoordValue.x;
					}
					if (newCoordValue.x < minX) {
						minX = newCoordValue.x;
					}
					if (newCoordValue.y > maxY) {
						maxY = newCoordValue.y;
					}
					if (newCoordValue.y < minY) {
						minY = newCoordValue.y;
					}
					newValueHolders.add(newCoordValue);
				}
			}
		}
		float widthX = (maxX - minX);
		float widthY = (maxY - minY);
		if (widthX == 0) {
			widthX = 0.01f;
		}
		if (widthY == 0) {
			widthY = 0.01f;
		}
		for (final Vertex2 tv : newValueHolders) {
			tv.x = (tv.x - minX) / widthX;
			tv.y = (tv.y - minY) / widthY;
		}
		final UVRemapAction uvRemapAction = new UVRemapAction(tVertices, newValueHolders, oldValueHolders,
				unwrapDirection);
		uvRemapAction.redo();
		return uvRemapAction;
	}

	@Override
	public UndoAction snapSelectedVertices() {
		final Collection<? extends Vertex2> selection = TVertexUtils
				.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex);
		final List<Vertex2> oldLocations = new ArrayList<>();
		final Vertex2 cog = Vertex2.centerOfGroup(selection);
		for (final Vertex2 vertex : selection) {
			oldLocations.add(new Vertex2(vertex));
		}
		final UVSnapAction temp = new UVSnapAction(selection, oldLocations, cog);
		temp.redo();// a handy way to do the snapping!
		return temp;
	}

	@Override
	public void rawTranslate(final double x, final double y) {
		for (final Vertex2 vertex : TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex)) {
			vertex.translate(x, y);
		}
	}

	@Override
	public void rawScale(final double centerX, final double centerY, final double scaleX, final double scaleY) {
		for (final Vertex2 vertex : TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex)) {
			vertex.scale(centerX, centerY, scaleX, scaleY);
		}
	}

	@Override
	public void rawRotate2d(final double centerX, final double centerY, final double radians, final byte firstXYZ,
			final byte secondXYZ) {
		for (final Vertex2 vertex : TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex)) {
			vertex.rotate(centerX, centerY, radians, firstXYZ, secondXYZ);
		}
	}

	@Override
	public UndoAction translate(final double x, final double y) {
		final Vertex2 delta = new Vertex2(x, y);
		final StaticMeshUVMoveAction moveAction = new StaticMeshUVMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public UndoAction setPosition(final Vertex2 center, final double x, final double y) {
		final Vertex2 delta = new Vertex2(x - center.x, y - center.y);
		final StaticMeshUVMoveAction moveAction = new StaticMeshUVMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public UndoAction rotate(final Vertex2 center, final double rotateRadians) {
		final SimpleRotateUVAction compoundAction = new SimpleRotateUVAction(this, center, rotateRadians);
		compoundAction.redo();
		return compoundAction;
	}

	@Override
	public Vertex2 getSelectionCenter() {
//		return selectionManager.getCenter();
		final Set<Vertex2> tvertices = new HashSet<>();
		for (final Vertex2 vertex : TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex)) {
			tvertices.add(vertex);
		}
		return Vertex2.centerOfGroup(tvertices); // TODO is this correct?
	}

	@Override
	public UndoAction selectFromViewer(final SelectionView viewerSelectionView) {
		final Set<T> previousSelection = selectionManager.getSelection();
		selectByVertices(viewerSelectionView.getSelectedVertices());
		final SetSelectionAction<T> setSelectionAction = new SetSelectionAction<>(selectionManager.getSelection(),
				previousSelection, selectionManager, "select UV from viewer");
		return setSelectionAction;
	}

	@Override
	public GenericMoveAction beginTranslation() {
		return new StaticMeshUVMoveAction(this, Vertex2.ORIGIN);
	}

	@Override
	public GenericRotateAction beginRotation(final double centerX, final double centerY, final byte dim1,
                                             final byte dim2) {
		return new StaticMeshUVRotateAction(this, new Vertex2(centerX, centerY), dim1, dim2);
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
