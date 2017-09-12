package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public final class ModelEditorManager {
	private final ModelView model;
	private final ProgramPreferences programPreferences;
	private ModelEditor modelEditor;
	private SelectingEventHandler selectingEventHandler;
	private final ViewportSelectionHandlerImpl viewportSelectionHandler;
	private final ModelEditorChangeListener modelEditorChangeListener;
	private SelectionView selectionView;
	private final SelectionListener selectionListener;

	public ModelEditorManager(final ModelView model, final ProgramPreferences programPreferences,
			final ToolbarButtonGroup<SelectionMode> modeButtonGroup,
			final ModelEditorChangeListener modelEditorChangeListener, final SelectionListener selectionListener) {
		this.model = model;
		this.modelEditorChangeListener = modelEditorChangeListener;
		this.programPreferences = programPreferences;
		this.selectionListener = selectionListener;
		viewportSelectionHandler = new ViewportSelectionHandlerImpl(modeButtonGroup, null);
		setSelectionItemType(SelectionItemTypes.VERTEX);
	}

	public void setSelectionItemType(final SelectionItemTypes selectionMode) {
		switch (selectionMode) {
		case FACE: {
			final FaceSelectionManager selectionManager = new FaceSelectionManager();
			modelEditor = new FaceModelEditor(model, programPreferences, selectionManager);
			selectingEventHandler = new FaceSelectingEventHandler(selectionManager, model);
			viewportSelectionHandler.setSelectingEventHandler(selectingEventHandler);
			modelEditorChangeListener.modelEditorChanged(modelEditor);
			selectionView = selectionManager;
			selectionListener.onSelectionChanged(selectionView);
			break;
		}
		default:
		case VERTEX: {
			final VertexSelectionManager selectionManager = new VertexSelectionManager();
			modelEditor = new VertexModelEditor(model, programPreferences, selectionManager);
			selectingEventHandler = new VertexSelectingEventHandler(selectionManager, model, programPreferences);
			viewportSelectionHandler.setSelectingEventHandler(selectingEventHandler);
			modelEditorChangeListener.modelEditorChanged(modelEditor);
			selectionView = selectionManager;
			selectionListener.onSelectionChanged(selectionView);
			break;
		}
		}
	}

	public ModelEditor getModelEditor() {
		return modelEditor;
	}

	public ViewportSelectionHandlerImpl getViewportSelectionHandler() {
		return viewportSelectionHandler;
	}

	public SelectingEventHandler getSelectingEventHandler() {
		return selectingEventHandler;
	}

	public SelectionView getSelectionView() {
		return selectionView;
	}
}
