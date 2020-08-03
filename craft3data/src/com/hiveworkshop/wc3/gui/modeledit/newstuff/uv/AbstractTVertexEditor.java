package com.hiveworkshop.wc3.gui.modeledit.newstuff.uv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.UVPanel;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.UVRemapAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.UVSnapAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.selection.SetSelectionAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.actions.MirrorTVerticesAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.actions.SimpleRotateUVAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.actions.StaticMeshUVMoveAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.actions.StaticMeshUVRotateAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.actions.StaticMeshUVScaleAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public abstract class AbstractTVertexEditor<T> extends AbstractSelectingTVertexEditor<T> implements TVertexEditor {
	protected final ModelView model;
	protected final VertexSelectionHelper vertexSelectionHelper;
	protected final ModelStructureChangeListener structureChangeListener;
	protected int uvLayerIndex;

	public AbstractTVertexEditor(final SelectionManager<T> selectionManager, final ModelView model,
			final ModelStructureChangeListener structureChangeListener) {
		super(selectionManager);
		this.model = model;
		this.structureChangeListener = structureChangeListener;
		this.vertexSelectionHelper = new VertexSelectionHelper() {
			@Override
			public void selectVertices(final Collection<Vertex> vertices) {
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
		final ArrayList<TVertex> tVertices = new ArrayList<TVertex>();
		final ArrayList<TVertex> newValueHolders = new ArrayList<TVertex>();
		final ArrayList<TVertex> oldValueHolders = new ArrayList<TVertex>();
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;
		for (final Vertex vertex : selectionManager.getSelectedVertices()) {
			if (vertex instanceof GeosetVertex) {
				final GeosetVertex geosetVertex = (GeosetVertex) vertex;
				if (uvLayerIndex < geosetVertex.getTverts().size()) {
					final TVertex modelDataTVertex = geosetVertex.getTVertex(uvLayerIndex);
					tVertices.add(modelDataTVertex);
					oldValueHolders.add(new TVertex(modelDataTVertex.x, modelDataTVertex.y));
					final TVertex newCoordValue = new TVertex(vertex.getCoord(xDim), vertex.getCoord(yDim));
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
		double widthX = (maxX - minX);
		double widthY = (maxY - minY);
		if (widthX == 0) {
			widthX = 0.01;
		}
		if (widthY == 0) {
			widthY = 0.01;
		}
		for (final TVertex tv : newValueHolders) {
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
		final Collection<? extends TVertex> selection = TVertexUtils
				.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex);
		final ArrayList<TVertex> oldLocations = new ArrayList<>();
		final TVertex cog = TVertex.centerOfGroup(selection);
		for (final TVertex vertex : selection) {
			oldLocations.add(new TVertex(vertex));
		}
		final UVSnapAction temp = new UVSnapAction(selection, oldLocations, cog);
		temp.redo();// a handy way to do the snapping!
		return temp;
	}

	@Override
	public void rawTranslate(final double x, final double y) {
		for (final TVertex vertex : TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex)) {
			vertex.translate(x, y);
		}
	}

	@Override
	public void rawScale(final double centerX, final double centerY, final double scaleX, final double scaleY) {
		for (final TVertex vertex : TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex)) {
			vertex.scale(centerX, centerY, scaleX, scaleY);
		}
	}

	@Override
	public void rawRotate2d(final double centerX, final double centerY, final double radians, final byte firstXYZ,
			final byte secondXYZ) {
		for (final TVertex vertex : TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex)) {
			vertex.rotate(centerX, centerY, radians, firstXYZ, secondXYZ);
		}
	}

	@Override
	public UndoAction translate(final double x, final double y) {
		final TVertex delta = new TVertex(x, y);
		final StaticMeshUVMoveAction moveAction = new StaticMeshUVMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public UndoAction setPosition(final TVertex center, final double x, final double y) {
		final TVertex delta = new TVertex(x - center.x, y - center.y);
		final StaticMeshUVMoveAction moveAction = new StaticMeshUVMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public UndoAction rotate(final TVertex center, final double rotateRadians) {
		final SimpleRotateUVAction compoundAction = new SimpleRotateUVAction(this, center, rotateRadians);
		compoundAction.redo();
		return compoundAction;
	}

	@Override
	public TVertex getSelectionCenter() {
//		return selectionManager.getCenter();
		final Set<TVertex> tvertices = new HashSet<>();
		for (final TVertex vertex : TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex)) {
			tvertices.add(vertex);
		}
		return TVertex.centerOfGroup(tvertices); // TODO is this correct?
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
		return new StaticMeshUVMoveAction(this, TVertex.ORIGIN);
	}

	@Override
	public GenericRotateAction beginRotation(final double centerX, final double centerY, final byte dim1,
			final byte dim2) {
		return new StaticMeshUVRotateAction(this, new TVertex(centerX, centerY), dim1, dim2);
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
