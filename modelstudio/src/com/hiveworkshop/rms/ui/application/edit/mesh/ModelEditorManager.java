package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.NodeAnimationModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;

public final class ModelEditorManager {
	private ModelHandler modelHandler;
	private ModelEditor modelEditor;
	private final ViewportSelectionHandler viewportSelectionHandler;
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
		this.viewportSelectionHandler = new ViewportSelectionHandler(modeButtonGroup, null);
		setSelectionItemType(SelectionItemTypes.VERTEX);
	}

	public void setSelectionItemType(SelectionItemTypes selectionMode) {
		switch (selectionMode) {
			case VERTEX, FACE, GROUP, CLUSTER -> {

				SelectionView selectionManager = new SelectionView(modelHandler.getModelView(), selectionMode);
				this.modelEditor = new AbstractModelEditor(selectionManager, structureChangeListener, modelHandler, selectionMode);

				selectionView = selectionManager;
			}
			case ANIMATE -> {
				SelectionView selectionManager = new SelectionView(modelHandler.getModelView(), selectionMode);
				modelEditor = new NodeAnimationModelEditor(selectionManager, structureChangeListener, modelHandler, selectionMode);

				selectionView = selectionManager;
			}
			case TPOSE -> {
				boolean moveLinked = MOVE_LINKED;// dialog == settings[0];
				SelectionView tposeSelectionManager = new SelectionView(modelHandler.getModelView(), moveLinked, selectionMode);

				modelEditor = new AbstractModelEditor(tposeSelectionManager, structureChangeListener, modelHandler, selectionMode);

				selectionView = tposeSelectionManager;
			}
		}

		selectionListener.onSelectionChanged(selectionView);
		viewportSelectionHandler.setSelectingEventHandler(modelEditor);
		modelEditorChangeListener.modelEditorChanged(modelEditor);
	}


//	private SelectionManager getSelectionManager(ModelEditorNotifier modelEditorNotifier, SelectionItemTypes selectionMode) {
//		SelectionManager selectionManager = new SelectionManager(modelHandler.getModelView(), selectionMode);
//		AbstractModelEditor modelEditor = new AbstractModelEditor(selectionManager, structureChangeListener, modelHandler, selectionMode);
//		modelEditorNotifier.subscribe(modelEditor);
//		return selectionManager;
//	}

	public ModelStructureChangeListener getStructureChangeListener() {
		return structureChangeListener;
	}

	public ModelEditor getModelEditor() {
		return modelEditor;
	}

	public ViewportSelectionHandler getViewportSelectionHandler() {
		return viewportSelectionHandler;
	}

	public SelectionView getSelectionView() {
		return selectionView;
	}
}
