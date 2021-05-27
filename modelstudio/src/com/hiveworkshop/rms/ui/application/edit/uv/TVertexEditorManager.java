package com.hiveworkshop.rms.ui.application.edit.uv;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditorChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.TVertexSelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;

public final class TVertexEditorManager {
	private final ModelView modelView;
	private TVertexEditor modelEditor;
	private final TVertexViewportSelectionHandlerImpl viewportSelectionHandler;
	private final TVertexEditorChangeListener modelEditorChangeListener;
	private SelectionView selectionView;
	private final SelectionListener selectionListener;
	private final RenderModel renderModel;
	private final ModelStructureChangeListener structureChangeListener;
	public static boolean MOVE_LINKED;

	public TVertexEditorManager(ModelView modelView,
	                            ToolbarButtonGroup2<SelectionMode> modeButtonGroup,
	                            TVertexEditorChangeListener modelEditorChangeListener,
	                            SelectionListener selectionListener,
	                            RenderModel renderModel,
	                            ModelStructureChangeListener structureChangeListener) {
		this.modelView = modelView;
		this.modelEditorChangeListener = modelEditorChangeListener;
		this.selectionListener = selectionListener;
		this.renderModel = renderModel;
		this.structureChangeListener = structureChangeListener;
		viewportSelectionHandler = new TVertexViewportSelectionHandlerImpl(modeButtonGroup, null);
		setSelectionItemType(TVertexSelectionItemTypes.VERTEX);
	}

	public void setSelectionItemType(final TVertexSelectionItemTypes selectionMode) {
		switch (selectionMode) {
			case FACE, VERTEX -> selectionView = new SelectionManager(modelView, transformSelectionMode(selectionMode));
		}

		modelEditor = new TVertexEditor(modelView, structureChangeListener, selectionMode);

		viewportSelectionHandler.setSelectingEventHandler(modelEditor);
		modelEditorChangeListener.editorChanged(modelEditor);
		selectionListener.onSelectionChanged(selectionView);
	}

	private SelectionItemTypes transformSelectionMode(TVertexSelectionItemTypes selectionMode){
		if(selectionMode == TVertexSelectionItemTypes.FACE){
			return SelectionItemTypes.FACE;
		}
		return SelectionItemTypes.VERTEX;
	}

	public TVertexEditor getModelEditor() {
		return modelEditor;
	}

	public TVertexViewportSelectionHandlerImpl getViewportSelectionHandler() {
		return viewportSelectionHandler;
	}

	public SelectionView getSelectionView() {
		return selectionView;
	}
}
