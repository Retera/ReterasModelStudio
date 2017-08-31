package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.wc3.gui.modeledit.useractions.UndoManager;
import com.hiveworkshop.wc3.gui.modeledit.useractions.UndoManagerImpl;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public final class BigModelEditorGuy {
	private final ModelView model;
	private final UndoManager undoManager;
	private final ProgramPreferences programPreferences;
	private ModelEditor modelEditor;
	private SelectingEventHandler selectingEventListener;

	public BigModelEditorGuy(final ModelView model, final ProgramPreferences programPreferences) {
		this.model = model;
		this.undoManager = new UndoManagerImpl();
		this.programPreferences = programPreferences;
		setSelectionItemType(SelectionItemTypes.VERTEX);
	}

	public void setSelectionItemType(final SelectionItemTypes selectionMode) {
		switch (selectionMode) {
		case FACE: {
			final FaceSelectionManager selectionManager = new FaceSelectionManager();
			modelEditor = new FaceModelEditor(undoManager, model, programPreferences, selectionManager);
			selectingEventListener = new FaceSelectingEventHandler(undoManager, selectionManager, model);
			break;
		}
		default:
		case VERTEX: {
			final VertexSelectionManager selectionManager = new VertexSelectionManager();
			modelEditor = new VertexModelEditor(undoManager, model, programPreferences, selectionManager);
			selectingEventListener = new VertexSelectingEventHandler(undoManager, selectionManager, model);
			break;
		}
		}
	}
}
