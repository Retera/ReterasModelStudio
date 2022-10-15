package com.hiveworkshop.rms.ui.application.edit.uv;

import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.TVertSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.TVertexSelectionItemTypes;

public final class TVertexEditorManager extends AbstractModelEditorManager {
	public static boolean MOVE_LINKED;
	TVertexEditor tVertexEditor;

	public TVertexEditorManager(ModelHandler modelHandler) {
		super(modelHandler);
		selectionManager = new TVertSelectionManager(modelHandler.getRenderModel(), modelHandler.getModelView(), transformSelectionMode(TVertexSelectionItemTypes.VERTEX));
		tVertexEditor = new TVertexEditor(selectionManager, modelHandler.getModelView(), transformSelectionMode(TVertexSelectionItemTypes.VERTEX));
		setSelectionItemType(TVertexSelectionItemTypes.VERTEX);

	}

	public void setSelectionItemType(TVertexSelectionItemTypes selectionMode) {
		switch (selectionMode) {
			case FACE, VERTEX -> selectionManager.setSelectionMode(transformSelectionMode(selectionMode));
		}

		modelEditor = tVertexEditor;

		changeNotifier.modelEditorChanged(modelEditor);


		if(selectionListener != null){
			selectionListener.onSelectionChanged(selectionManager);
		}
	}

	public void setSelectionItemType(SelectionItemTypes selectionMode) {
		if (selectionMode == SelectionItemTypes.FACE) {
			selectionManager.setSelectionMode(selectionMode);
		} else {
			selectionManager.setSelectionMode(SelectionItemTypes.VERTEX);
		}

		modelEditor = tVertexEditor;

		changeNotifier.modelEditorChanged(modelEditor);

		if(selectionListener != null){
			selectionListener.onSelectionChanged(selectionManager);
		}
	}

	private SelectionItemTypes transformSelectionMode(TVertexSelectionItemTypes selectionMode) {
		if (selectionMode == TVertexSelectionItemTypes.FACE) {
			return SelectionItemTypes.FACE;
		}
		return SelectionItemTypes.VERTEX;
	}
}
