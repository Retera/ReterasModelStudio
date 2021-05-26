package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.NodeAnimationModelEditor;
import com.hiveworkshop.rms.ui.application.edit.animation.NodeAnimationSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.selection.MultiPartSelectionView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandlerImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.VertexClusterDefinitions;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.*;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;

import java.util.Arrays;

public final class ModelEditorManager {
	private ModelHandler modelHandler;
	private ModelEditor modelEditor;
	private final ViewportSelectionHandlerImpl viewportSelectionHandler;
	private final ModelEditorChangeListener modelEditorChangeListener;
	private SelectionView selectionView;
	private final SelectionListener selectionListener;
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
		switch (selectionMode) {
			case VERTEX -> {
				ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();
				modelEditorNotifier.setCloneContextHelper(new CloneContextHelper(modelHandler.getModelView(), structureChangeListener));

				GeosetVertexSelectionManager selectionManager = getGeosetVertexSelectionManager(modelEditorNotifier, selectionMode);
				PivotPointSelectionManager pivotSelectionManager = getPivotPointSelectionManager(modelEditorNotifier, selectionMode);

				modelEditor = modelEditorNotifier;

				selectionView = new MultiPartSelectionView(Arrays.asList(selectionManager, pivotSelectionManager));
			}
			case FACE -> {
				ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();
				modelEditorNotifier.setCloneContextHelper(new CloneContextHelper(modelHandler.getModelView(), structureChangeListener));

				FaceSelectionManager selectionManager = getFaceSelectionManager(modelEditorNotifier, selectionMode);
				PivotPointSelectionManager pivotSelectionManager = getPivotPointSelectionManager(modelEditorNotifier, selectionMode);

				modelEditor = modelEditorNotifier;

				selectionView = new MultiPartSelectionView(Arrays.asList(selectionManager, pivotSelectionManager));
			}
			case GROUP -> {
				ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();
				modelEditorNotifier.setCloneContextHelper(new CloneContextHelper(modelHandler.getModelView(), structureChangeListener));

				VertexGroupSelectionManager selectionManager = getVertexGroupSelectionManager(modelEditorNotifier, selectionMode);
				PivotPointSelectionManager pivotSelectionManager = getPivotPointSelectionManager(modelEditorNotifier, selectionMode);

				modelEditor = modelEditorNotifier;

				selectionView = new MultiPartSelectionView(Arrays.asList(selectionManager, pivotSelectionManager));
			}
			case CLUSTER -> {
				ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();
				modelEditorNotifier.setCloneContextHelper(new CloneContextHelper(modelHandler.getModelView(), structureChangeListener));

				PivotPointSelectionManager pivotSelectionManager = getPivotPointSelectionManager(modelEditorNotifier, selectionMode);
				VertexClusterSelectionManager selectionManager = getVertexClusterSelectionManager(modelEditorNotifier, selectionMode);

				modelEditor = modelEditorNotifier;

				selectionView = new MultiPartSelectionView(Arrays.asList(selectionManager, pivotSelectionManager));
			}
			case ANIMATE -> {
				NodeAnimationSelectionManager nodeAnimationSelectionManager = new NodeAnimationSelectionManager(modelHandler.getRenderModel(), modelHandler.getModelView(), selectionMode);
				modelEditor = new NodeAnimationModelEditor(nodeAnimationSelectionManager, structureChangeListener, modelHandler, selectionMode);

				selectionView = nodeAnimationSelectionManager;
			}
			case TPOSE -> {
				boolean moveLinked = MOVE_LINKED;// dialog == settings[0];
				TPoseSelectionManager tposeSelectionManager = new TPoseSelectionManager(modelHandler.getModelView(), moveLinked, selectionMode);

				modelEditor = new AbstractModelEditor<>(tposeSelectionManager, structureChangeListener, modelHandler, selectionMode);

				selectionView = tposeSelectionManager;
			}
		}

		selectionListener.onSelectionChanged(selectionView);
		viewportSelectionHandler.setSelectingEventHandler(modelEditor);
		modelEditorChangeListener.modelEditorChanged(modelEditor);
	}

	private VertexClusterSelectionManager getVertexClusterSelectionManager(ModelEditorNotifier modelEditorNotifier, SelectionItemTypes selectionMode) {
		VertexClusterDefinitions vertexClusterDefinitions = new VertexClusterDefinitions(modelHandler.getModel(), selectionMode);
		VertexClusterSelectionManager selectionManager = new VertexClusterSelectionManager(vertexClusterDefinitions, modelHandler.getModelView(), selectionMode);
		modelEditorNotifier.subscribe(new AbstractModelEditor<>(selectionManager, structureChangeListener, modelHandler, selectionMode));
		return selectionManager;
	}

	private GeosetVertexSelectionManager getGeosetVertexSelectionManager(ModelEditorNotifier modelEditorNotifier, SelectionItemTypes selectionMode) {
		GeosetVertexSelectionManager selectionManager = new GeosetVertexSelectionManager(modelHandler.getModelView(), selectionMode);
		modelEditorNotifier.subscribe(new AbstractModelEditor<>(selectionManager, structureChangeListener, modelHandler, selectionMode));
		return selectionManager;
	}

	private VertexGroupSelectionManager getVertexGroupSelectionManager(ModelEditorNotifier modelEditorNotifier, SelectionItemTypes selectionMode) {
		VertexGroupSelectionManager selectionManager = new VertexGroupSelectionManager(modelHandler.getModelView(), selectionMode);
		modelEditorNotifier.subscribe(new AbstractModelEditor<>(selectionManager, structureChangeListener, modelHandler, selectionMode));
		return selectionManager;
	}

	private FaceSelectionManager getFaceSelectionManager(ModelEditorNotifier modelEditorNotifier, SelectionItemTypes selectionMode) {
		FaceSelectionManager selectionManager = new FaceSelectionManager(modelHandler.getModelView(), selectionMode);
		modelEditorNotifier.subscribe(new AbstractModelEditor<>(selectionManager, structureChangeListener, modelHandler, selectionMode));
		return selectionManager;
	}

	public PivotPointSelectionManager getPivotPointSelectionManager(ModelEditorNotifier modelEditorNotifier, SelectionItemTypes selectionMode) {
		PivotPointSelectionManager selectionManager = new PivotPointSelectionManager(modelHandler.getModelView(), selectionMode);
		modelEditorNotifier.subscribe(new AbstractModelEditor<>(selectionManager, structureChangeListener, modelHandler, selectionMode));
		return selectionManager;
	}

	public ModelStructureChangeListener getStructureChangeListener() {
		return structureChangeListener;
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

//	public NodeAnimationSelectionManager getNodeAnimationSelectionManager() {
//		return nodeAnimationSelectionManager;
//	}
}
