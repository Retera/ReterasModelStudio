package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
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
	private final ModelView model;
	private final ProgramPreferences programPreferences;
	private ModelEditor modelEditor;
	private final ViewportSelectionHandlerImpl viewportSelectionHandler;
	private final ModelEditorChangeListener modelEditorChangeListener;
	private SelectionView selectionView;
	private final SelectionListener selectionListener;
	private final RenderModel renderModel;
	private NodeAnimationSelectionManager nodeAnimationSelectionManager;
	private final ModelStructureChangeListener structureChangeListener;
	public static boolean MOVE_LINKED;

	public ModelEditorManager(final ModelView model, final ProgramPreferences programPreferences, final ToolbarButtonGroup<SelectionMode> modeButtonGroup, final ModelEditorChangeListener modelEditorChangeListener, final SelectionListener selectionListener, final RenderModel renderModel, final ModelStructureChangeListener structureChangeListener) {
		this.model = model;
		this.modelEditorChangeListener = modelEditorChangeListener;
		this.programPreferences = programPreferences;
		this.selectionListener = selectionListener;
		this.renderModel = renderModel;
		this.structureChangeListener = structureChangeListener;
		viewportSelectionHandler = new ViewportSelectionHandlerImpl(modeButtonGroup, null);
		setSelectionItemType(SelectionItemTypes.VERTEX);
	}

	public void setSelectionItemType(final SelectionItemTypes selectionMode) {
		final Collection<? extends Vec3> lastSelectedVertices;
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
				FaceModelEditor faceModelEditor = new FaceModelEditor(model, programPreferences, selectionManager, structureChangeListener);
				modelEditorNotifier.subscribe(faceModelEditor);

				PivotPointModelEditor pivotPointModelEditor = new PivotPointModelEditor(model, programPreferences, pivotSelectionManager, structureChangeListener);
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
				modelEditorNotifier.setCloneContextHelper(new CloneContextHelper(model, structureChangeListener, faceModelEditor.getVertexSelectionHelper(), pivotPointModelEditor.getVertexSelectionHelper(), pivotPointModelEditor.selectionManager, faceModelEditor.selectionManager));
			}
			case GROUP -> {
				VertexGroupSelectionManager selectionManager = new VertexGroupSelectionManager();
				PivotPointSelectionManager pivotSelectionManager = new PivotPointSelectionManager();
				ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();

				VertexGroupModelEditor vertexGroupModelEditor = new VertexGroupModelEditor(model, programPreferences, selectionManager, structureChangeListener);
				modelEditorNotifier.subscribe(vertexGroupModelEditor);

				PivotPointModelEditor pivotPointModelEditor = new PivotPointModelEditor(model, programPreferences, pivotSelectionManager, structureChangeListener);
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
				modelEditorNotifier.setCloneContextHelper(new CloneContextHelper(model, structureChangeListener, vertexGroupModelEditor.getVertexSelectionHelper(), pivotPointModelEditor.getVertexSelectionHelper(), pivotPointModelEditor.selectionManager, vertexGroupModelEditor.selectionManager));
			}
			case ANIMATE -> {
				nodeAnimationSelectionManager = new NodeAnimationSelectionManager(renderModel);
				NodeAnimationModelEditor nodeAnimationModelEditor = new NodeAnimationModelEditor(model, programPreferences, nodeAnimationSelectionManager, renderModel, structureChangeListener);

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
				TPoseSelectionManager tposeSelectionManager = new TPoseSelectionManager(model, moveLinked);
				TPoseModelEditor tPoseModelEditor = new TPoseModelEditor(model, programPreferences, tposeSelectionManager, structureChangeListener);

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
				VertexClusterDefinitions vertexClusterDefinitions = new VertexClusterDefinitions(model.getModel());
				VertexClusterSelectionManager selectionManager = new VertexClusterSelectionManager(vertexClusterDefinitions);
				PivotPointSelectionManager pivotSelectionManager = new PivotPointSelectionManager();
				ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();

				VertexClusterModelEditor vertexGroupModelEditor = new VertexClusterModelEditor(model, programPreferences, selectionManager, structureChangeListener, vertexClusterDefinitions);
				modelEditorNotifier.subscribe(vertexGroupModelEditor);

				PivotPointModelEditor pivotPointModelEditor = new PivotPointModelEditor(model, programPreferences, pivotSelectionManager, structureChangeListener);
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
				modelEditorNotifier.setCloneContextHelper(new CloneContextHelper(model, structureChangeListener, vertexGroupModelEditor.getVertexSelectionHelper(), pivotPointModelEditor.getVertexSelectionHelper(), pivotPointModelEditor.selectionManager, vertexGroupModelEditor.selectionManager));
			}
			case VERTEX -> {
				GeosetVertexSelectionManager selectionManager = new GeosetVertexSelectionManager();
				PivotPointSelectionManager pivotSelectionManager = new PivotPointSelectionManager();
				ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();

				GeosetVertexModelEditor geosetVertexModelEditor = new GeosetVertexModelEditor(model, programPreferences, selectionManager, structureChangeListener);
				modelEditorNotifier.subscribe(geosetVertexModelEditor);

				PivotPointModelEditor pivotPointModelEditor = new PivotPointModelEditor(model, programPreferences, pivotSelectionManager, structureChangeListener);
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
				modelEditorNotifier.setCloneContextHelper(new CloneContextHelper(model, structureChangeListener, geosetVertexModelEditor.getVertexSelectionHelper(), pivotPointModelEditor.getVertexSelectionHelper(), pivotPointModelEditor.selectionManager, geosetVertexModelEditor.selectionManager));
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
