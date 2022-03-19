package com.hiveworkshop.rms.ui.application.edit.uv;

import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.listener.ModelEditorChangeNotifier;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.TVertSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.TVertexSelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;

public final class TVertexEditorManager extends AbstractModelEditorManager {
	public static boolean MOVE_LINKED;
	TVertexEditor tVertexEditor;

	public TVertexEditorManager(ModelHandler modelHandler,
	                            ToolbarButtonGroup2<SelectionMode> modeButtonGroup,
	                            ModelEditorChangeNotifier changeNotifier,
	                            SelectionListener selectionListener) {
		super(modelHandler, changeNotifier, selectionListener);
		selectionManager = new TVertSelectionManager(modelHandler.getRenderModel(), modelHandler.getModelView(), transformSelectionMode(TVertexSelectionItemTypes.VERTEX));
		tVertexEditor = new TVertexEditor(selectionManager, modelHandler.getModelView(), transformSelectionMode(TVertexSelectionItemTypes.VERTEX));
		setSelectionItemType(TVertexSelectionItemTypes.VERTEX);

	}

	public TVertexEditorManager(ModelHandler modelHandler,
	                            ModelEditorChangeNotifier changeNotifier,
	                            SelectionListener selectionListener) {
		super(modelHandler, changeNotifier, selectionListener);
		selectionManager = new TVertSelectionManager(modelHandler.getRenderModel(), modelHandler.getModelView(), transformSelectionMode(TVertexSelectionItemTypes.VERTEX));
		tVertexEditor = new TVertexEditor(selectionManager, modelHandler.getModelView(), transformSelectionMode(TVertexSelectionItemTypes.VERTEX));
		setSelectionItemType(TVertexSelectionItemTypes.VERTEX);

	}

	public void setSelectionItemType(TVertexSelectionItemTypes selectionMode) {
		switch (selectionMode) {
			case FACE, VERTEX -> selectionManager.setSelectionMode(transformSelectionMode(selectionMode));
		}

		modelEditor = tVertexEditor;

		viewportSelectionHandler.setSelectionManager(selectionManager);
		changeNotifier.modelEditorChanged(modelEditor);
		selectionListener.onSelectionChanged(selectionManager);
	}

	public void setSelectionItemType(SelectionItemTypes selectionMode) {
		if (selectionMode == SelectionItemTypes.FACE) {
			selectionManager.setSelectionMode(selectionMode);
		} else {
			selectionManager.setSelectionMode(SelectionItemTypes.VERTEX);
		}

		modelEditor = tVertexEditor;

		viewportSelectionHandler.setSelectionManager(selectionManager);
		changeNotifier.modelEditorChanged(modelEditor);
		selectionListener.onSelectionChanged(selectionManager);
	}

	private SelectionItemTypes transformSelectionMode(TVertexSelectionItemTypes selectionMode) {
		if (selectionMode == TVertexSelectionItemTypes.FACE) {
			return SelectionItemTypes.FACE;
		}
		return SelectionItemTypes.VERTEX;
	}
}
