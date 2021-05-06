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
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Arrays;
import java.util.Collection;

public final class ModelEditorManager {
	private ModelHandler modelHandler;
	private final ProgramPreferences programPreferences;
	private ModelEditor modelEditor;
	private final ViewportSelectionHandlerImpl viewportSelectionHandler;
	private final ModelEditorChangeListener modelEditorChangeListener;
	private SelectionView selectionView;
	private final SelectionListener selectionListener;
	private NodeAnimationSelectionManager nodeAnimationSelectionManager;
	private final ModelStructureChangeListener structureChangeListener;
	public static boolean MOVE_LINKED;

	public ModelEditorManager(ModelHandler modelHandler,
	                          ProgramPreferences programPreferences,
	                          ToolbarButtonGroup<SelectionMode> modeButtonGroup,
	                          ModelEditorChangeListener modelEditorChangeListener,
	                          SelectionListener selectionListener,
	                          ModelStructureChangeListener structureChangeListener) {
		this.modelHandler = modelHandler;
		this.modelEditorChangeListener = modelEditorChangeListener;
		this.programPreferences = programPreferences;
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
				FaceSelectionManager selectionManager = new FaceSelectionManager();
				PivotPointSelectionManager pivotSelectionManager = new PivotPointSelectionManager();
				ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();
				FaceModelEditor faceModelEditor = new FaceModelEditor(modelHandler.getModelView(), programPreferences, selectionManager, structureChangeListener, modelHandler);
				modelEditorNotifier.subscribe(faceModelEditor);

				PivotPointModelEditor pivotPointModelEditor = new PivotPointModelEditor(modelHandler.getModelView(), programPreferences, pivotSelectionManager, structureChangeListener, modelHandler);
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
				VertexGroupSelectionManager selectionManager = new VertexGroupSelectionManager();
				PivotPointSelectionManager pivotSelectionManager = new PivotPointSelectionManager();
				ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();

				VertexGroupModelEditor vertexGroupModelEditor = new VertexGroupModelEditor(modelHandler.getModelView(), programPreferences, selectionManager, structureChangeListener, modelHandler);
				modelEditorNotifier.subscribe(vertexGroupModelEditor);

				PivotPointModelEditor pivotPointModelEditor = new PivotPointModelEditor(modelHandler.getModelView(), programPreferences, pivotSelectionManager, structureChangeListener, modelHandler);
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
				nodeAnimationSelectionManager = new NodeAnimationSelectionManager(modelHandler.getRenderModel());
				NodeAnimationModelEditor nodeAnimationModelEditor = new NodeAnimationModelEditor(modelHandler.getModelView(), programPreferences, nodeAnimationSelectionManager, modelHandler.getRenderModel(), structureChangeListener);

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
				TPoseModelEditor tPoseModelEditor = new TPoseModelEditor(modelHandler.getModelView(), programPreferences, tposeSelectionManager, structureChangeListener, modelHandler);

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
				VertexClusterDefinitions vertexClusterDefinitions = new VertexClusterDefinitions(modelHandler.getModel());
				VertexClusterSelectionManager selectionManager = new VertexClusterSelectionManager(vertexClusterDefinitions);
				PivotPointSelectionManager pivotSelectionManager = new PivotPointSelectionManager();
				ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();

				VertexClusterModelEditor vertexGroupModelEditor = new VertexClusterModelEditor(modelHandler.getModelView(), programPreferences, selectionManager, structureChangeListener, vertexClusterDefinitions, modelHandler);
				modelEditorNotifier.subscribe(vertexGroupModelEditor);

				PivotPointModelEditor pivotPointModelEditor = new PivotPointModelEditor(modelHandler.getModelView(), programPreferences, pivotSelectionManager, structureChangeListener, modelHandler);
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
			case VERTEX -> {
				GeosetVertexSelectionManager selectionManager = new GeosetVertexSelectionManager();
				PivotPointSelectionManager pivotSelectionManager = new PivotPointSelectionManager();
				ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();

				GeosetVertexModelEditor geosetVertexModelEditor = new GeosetVertexModelEditor(modelHandler.getModelView(), programPreferences, selectionManager, structureChangeListener, modelHandler);
				modelEditorNotifier.subscribe(geosetVertexModelEditor);

				PivotPointModelEditor pivotPointModelEditor = new PivotPointModelEditor(modelHandler.getModelView(), programPreferences, pivotSelectionManager, structureChangeListener, modelHandler);
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
