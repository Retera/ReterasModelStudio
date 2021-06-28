package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.listener.ModelEditorChangeNotifier;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;

public abstract class AbstractModelEditorManager {
	protected ModelHandler modelHandler;
	protected ModelEditor modelEditor;
	protected final ViewportSelectionHandler viewportSelectionHandler;
	protected final ModelEditorChangeNotifier changeNotifier;
	protected AbstractSelectionManager selectionManager;
	protected final SelectionListener selectionListener;

	public AbstractModelEditorManager(ModelHandler modelHandler,
	                                  ToolbarButtonGroup2<SelectionMode> modeButtonGroup,
	                                  ModelEditorChangeNotifier changeNotifier,
	                                  SelectionListener selectionListener) {
		this.modelHandler = modelHandler;
		this.changeNotifier = changeNotifier;
		this.selectionListener = selectionListener;
		this.viewportSelectionHandler = new ViewportSelectionHandler(modeButtonGroup, null);
	}

	public ModelEditor getModelEditor() {
		return modelEditor;
	}

	public ViewportSelectionHandler getViewportSelectionHandler() {
		return viewportSelectionHandler;
	}

	public AbstractSelectionManager getSelectionView() {
		return selectionManager;
	}
}
