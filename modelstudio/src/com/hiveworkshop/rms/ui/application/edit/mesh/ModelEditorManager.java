package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.NodeAnimationModelEditor;
import com.hiveworkshop.rms.ui.application.edit.animation.NodeAnimationSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.selection.MultiPartSelectionView;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.faces.FaceModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.faces.FaceSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.geosetvertex.GeosetVertexModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.geosetvertex.GeosetVertexSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.pivotpoint.PivotPointModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.pivotpoint.PivotPointSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.tpose.TPoseModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.tpose.TPoseSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.vertexcluster.VertexClusterDefinitions;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.vertexcluster.VertexClusterModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.vertexcluster.VertexClusterSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.vertexgroup.VertexGroupModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.vertexgroup.VertexGroupSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandlerImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Arrays;
import java.util.Collection;

public final class ModelEditorManager {
	private ModelHandler modelHandler;
	private ModelEditor modelEditor;
	private final ViewportSelectionHandlerImpl viewportSelectionHandler;
	private final ModelEditorChangeListener modelEditorChangeListener;
	private SelectionView selectionView;
	private final SelectionListener selectionListener;
	private NodeAnimationSelectionManager nodeAnimationSelectionManager;
	private final ModelStructureChangeListener structureChangeListener;
	public static boolean MOVE_LINKED;

	public ModelEditorManager(ModelHandler modelHandler,
	                          ToolbarButtonGroup2<SelectionMode> modeButtonGroup,
	                          ModelEditorChangeListener modelEditorChangeListener,
	                          SelectionListener selectionListener,
	                          ModelStructureChangeListener structureChangeListener) {
		this.modelHandler = modelHandler;
		this.modelEditorChangeListener = modelEditorChangeListener;
		this.selectionListener = selectionListener;
		this.structureChangeListener = structureChangeListener;
		this.viewportSelectionHandler = new ViewportSelectionHandlerImpl(modeButtonGroup, null);
		setSelectionItemType(SelectionItemTypes.VERTEX);
	}

	public void setSelectionItemType(SelectionItemTypes selectionMode) {
		Collection<? extends Vec3> lastSelectedVertices;
		if (selectionView != null) {

			lastSelectedVertices = selectionView.getSelectedVertices();
		} else {
			lastSelectedVertices = null;
		}
		switch (selectionMode) {
			case FACE -> {
				ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();

				FaceSelectionManager selectionManager = new FaceSelectionManager(modelHandler.getModelView());
				FaceModelEditor faceModelEditor = new FaceModelEditor(selectionManager, structureChangeListener, modelHandler);
				modelEditorNotifier.subscribe(faceModelEditor);


				PivotPointSelectionManager pivotSelectionManager = new PivotPointSelectionManager(modelHandler.getModelView());
				PivotPointModelEditor pivotPointModelEditor = new PivotPointModelEditor(pivotSelectionManager, structureChangeListener, modelHandler);

				modelEditorNotifier.subscribe(pivotPointModelEditor);

				modelEditor = modelEditorNotifier;
				if (lastSelectedVertices != null) {
					modelEditor.selectByVertices(lastSelectedVertices);
				}
				viewportSelectionHandler.setSelectingEventHandler(modelEditor);
				modelEditorChangeListener.modelEditorChanged(modelEditor);
				selectionView = new MultiPartSelectionView(Arrays.asList(selectionManager, pivotSelectionManager));
				selectionListener.onSelectionChanged(selectionView);
				nodeAnimationSelectionManager = null;
				modelEditorNotifier.setCloneContextHelper(new CloneContextHelper(modelHandler.getModelView(), structureChangeListener, faceModelEditor.getVertexSelectionHelper(), pivotPointModelEditor.getVertexSelectionHelper(), pivotPointModelEditor.selectionManager, faceModelEditor.selectionManager));
			}
			case GROUP -> {
				ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();

				VertexGroupSelectionManager selectionManager = new VertexGroupSelectionManager(modelHandler.getModelView());
				VertexGroupModelEditor vertexGroupModelEditor = new VertexGroupModelEditor(selectionManager, structureChangeListener, modelHandler);
				modelEditorNotifier.subscribe(vertexGroupModelEditor);

				PivotPointSelectionManager pivotSelectionManager = new PivotPointSelectionManager(modelHandler.getModelView());
				PivotPointModelEditor pivotPointModelEditor = new PivotPointModelEditor(pivotSelectionManager, structureChangeListener, modelHandler);
				modelEditorNotifier.subscribe(pivotPointModelEditor);

				modelEditor = modelEditorNotifier;
				if (lastSelectedVertices != null) {
					modelEditor.selectByVertices(lastSelectedVertices);
				}
				viewportSelectionHandler.setSelectingEventHandler(modelEditor);
				modelEditorChangeListener.modelEditorChanged(modelEditor);
				selectionView = new MultiPartSelectionView(Arrays.asList(selectionManager, pivotSelectionManager));
				selectionListener.onSelectionChanged(selectionView);
				nodeAnimationSelectionManager = null;
				modelEditorNotifier.setCloneContextHelper(new CloneContextHelper(modelHandler.getModelView(), structureChangeListener, vertexGroupModelEditor.getVertexSelectionHelper(), pivotPointModelEditor.getVertexSelectionHelper(), pivotPointModelEditor.selectionManager, vertexGroupModelEditor.selectionManager));
			}
			case ANIMATE -> {
				nodeAnimationSelectionManager = new NodeAnimationSelectionManager(modelHandler.getRenderModel(), modelHandler.getModelView());
				NodeAnimationModelEditor nodeAnimationModelEditor = new NodeAnimationModelEditor(modelHandler.getModelView(), nodeAnimationSelectionManager, modelHandler.getRenderModel(), structureChangeListener);

				modelEditor = nodeAnimationModelEditor;
				if (lastSelectedVertices != null) {
					modelEditor.selectByVertices(lastSelectedVertices);
				}
				viewportSelectionHandler.setSelectingEventHandler(modelEditor);
				modelEditorChangeListener.modelEditorChanged(modelEditor);
				selectionView = nodeAnimationSelectionManager;
				selectionListener.onSelectionChanged(selectionView);
			}
			case TPOSE -> {
				boolean moveLinked = MOVE_LINKED;// dialog == settings[0];
				TPoseSelectionManager tposeSelectionManager = new TPoseSelectionManager(modelHandler.getModelView(), moveLinked);
				TPoseModelEditor tPoseModelEditor = new TPoseModelEditor(tposeSelectionManager, structureChangeListener, modelHandler);

				modelEditor = tPoseModelEditor;
				if (lastSelectedVertices != null) {
					modelEditor.selectByVertices(lastSelectedVertices);
				}
				viewportSelectionHandler.setSelectingEventHandler(modelEditor);
				modelEditorChangeListener.modelEditorChanged(modelEditor);
				selectionView = tposeSelectionManager;
				selectionListener.onSelectionChanged(selectionView);
			}
			case CLUSTER -> {
				ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();


				PivotPointSelectionManager pivotSelectionManager = new PivotPointSelectionManager(modelHandler.getModelView());
				PivotPointModelEditor pivotPointModelEditor = new PivotPointModelEditor(pivotSelectionManager, structureChangeListener, modelHandler);
				modelEditorNotifier.subscribe(pivotPointModelEditor);

				VertexClusterDefinitions vertexClusterDefinitions = new VertexClusterDefinitions(modelHandler.getModel());
				VertexClusterSelectionManager selectionManager = new VertexClusterSelectionManager(vertexClusterDefinitions, modelHandler.getModelView());
				VertexClusterModelEditor vertexGroupModelEditor = new VertexClusterModelEditor(selectionManager, structureChangeListener, vertexClusterDefinitions, modelHandler);
				modelEditorNotifier.subscribe(vertexGroupModelEditor);

				modelEditor = modelEditorNotifier;
				if (lastSelectedVertices != null) {
					modelEditor.selectByVertices(lastSelectedVertices);
				}
				viewportSelectionHandler.setSelectingEventHandler(modelEditor);
				modelEditorChangeListener.modelEditorChanged(modelEditor);
				selectionView = new MultiPartSelectionView(Arrays.asList(selectionManager, pivotSelectionManager));
				selectionListener.onSelectionChanged(selectionView);
				nodeAnimationSelectionManager = null;
				modelEditorNotifier.setCloneContextHelper(new CloneContextHelper(modelHandler.getModelView(), structureChangeListener, vertexGroupModelEditor.getVertexSelectionHelper(), pivotPointModelEditor.getVertexSelectionHelper(), pivotPointModelEditor.selectionManager, vertexGroupModelEditor.selectionManager));
			}
			case VERTEX -> {
				ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();

				GeosetVertexSelectionManager selectionManager = new GeosetVertexSelectionManager(modelHandler.getModelView());
				GeosetVertexModelEditor geosetVertexModelEditor = new GeosetVertexModelEditor(selectionManager, structureChangeListener, modelHandler);
				modelEditorNotifier.subscribe(geosetVertexModelEditor);

				PivotPointSelectionManager pivotSelectionManager = new PivotPointSelectionManager(modelHandler.getModelView());
				PivotPointModelEditor pivotPointModelEditor = new PivotPointModelEditor(pivotSelectionManager, structureChangeListener, modelHandler);
				modelEditorNotifier.subscribe(pivotPointModelEditor);

				modelEditor = modelEditorNotifier;
				if (lastSelectedVertices != null) {
					modelEditor.selectByVertices(lastSelectedVertices);
				}
				viewportSelectionHandler.setSelectingEventHandler(modelEditor);
				modelEditorChangeListener.modelEditorChanged(modelEditor);
				selectionView = new MultiPartSelectionView(Arrays.asList(selectionManager, pivotSelectionManager));
				selectionListener.onSelectionChanged(selectionView);
				nodeAnimationSelectionManager = null;
				modelEditorNotifier.setCloneContextHelper(new CloneContextHelper(modelHandler.getModelView(), structureChangeListener, geosetVertexModelEditor.getVertexSelectionHelper(), pivotPointModelEditor.getVertexSelectionHelper(), pivotPointModelEditor.selectionManager, geosetVertexModelEditor.selectionManager));
			}
		}
	}

	public ModelEditor getModelEditor() {
		return modelEditor;
	}

	public ViewportSelectionHandlerImpl getViewportSelectionHandler() {
		return viewportSelectionHandler;
	}

	public SelectionView getSelectionView() {
		return selectionView;
	}

	public NodeAnimationSelectionManager getNodeAnimationSelectionManager() {
		return nodeAnimationSelectionManager;
	}
}
