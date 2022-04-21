package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.listener.ModelEditorChangeNotifier;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;

public abstract class AbstractModelEditorManager {
	protected ModelHandler modelHandler;
	protected ModelEditor modelEditor;
	protected final ModelEditorChangeNotifier changeNotifier;
	protected AbstractSelectionManager selectionManager;
	protected SelectionListener selectionListener;

	public AbstractModelEditorManager(ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		this.changeNotifier = new ModelEditorChangeNotifier();
	}

	public AbstractModelEditorManager setViewportActivityManager(ViewportActivityManager viewportActivityManager) {
		changeNotifier.subscribe(viewportActivityManager);
		return this;
	}

	public ModelEditor getModelEditor() {
		return modelEditor;
	}

	public AbstractSelectionManager getSelectionView() {
		return selectionManager;
	}

	public AbstractModelEditorManager setSelectionListener(SelectionListener selectionListener) {
		this.selectionListener = selectionListener;
		return this;
	}

	public abstract void setSelectionItemType(SelectionItemTypes selectionMode);
}
