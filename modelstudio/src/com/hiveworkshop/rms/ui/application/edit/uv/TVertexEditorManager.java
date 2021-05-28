package com.hiveworkshop.rms.ui.application.edit.uv;

import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.TVertSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.TVertexSelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;

public final class TVertexEditorManager extends AbstractModelEditorManager {
//public final class TVertexEditorManager {
	public static boolean MOVE_LINKED;

	public TVertexEditorManager(ModelHandler modelHandler,
	                            ToolbarButtonGroup2<SelectionMode> modeButtonGroup,
	                            ModelEditorChangeListener modelEditorChangeListener,
	                            SelectionListener selectionListener,
	                            ModelStructureChangeListener structureChangeListener) {
		super(modelHandler, modeButtonGroup, modelEditorChangeListener, selectionListener, structureChangeListener);
		setSelectionItemType(TVertexSelectionItemTypes.VERTEX);
	}

	public void setSelectionItemType(TVertexSelectionItemTypes selectionMode) {
		switch (selectionMode) {
			case FACE, VERTEX -> selectionManager = new TVertSelectionManager(modelHandler.getModelView(), transformSelectionMode(selectionMode));
		}

		modelEditor = new TVertexEditor(selectionManager, modelHandler.getModelView(), structureChangeListener, transformSelectionMode(selectionMode));

//		viewportSelectionHandler.setModelEditor(modelEditor);
		viewportSelectionHandler.setModelEditor(selectionManager);
		modelEditorChangeListener.modelEditorChanged(modelEditor);
		selectionListener.onSelectionChanged(selectionManager);
	}

	private SelectionItemTypes transformSelectionMode(TVertexSelectionItemTypes selectionMode) {
		if (selectionMode == TVertexSelectionItemTypes.FACE) {
			return SelectionItemTypes.FACE;
		}
		return SelectionItemTypes.VERTEX;
	}
}
