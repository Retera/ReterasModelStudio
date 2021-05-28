package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;

public abstract class AbstractModelEditorManager {
	protected ModelHandler modelHandler;
	protected ModelEditor modelEditor;
	protected final ViewportSelectionHandler viewportSelectionHandler;
	protected final ModelEditorChangeListener modelEditorChangeListener;
	protected AbstractSelectionManager selectionManager;
	protected final SelectionListener selectionListener;
	protected final ModelStructureChangeListener structureChangeListener;

	public AbstractModelEditorManager(ModelHandler modelHandler,
	                                  ToolbarButtonGroup2<SelectionMode> modeButtonGroup,
	                                  ModelEditorChangeListener modelEditorChangeListener,
	                                  SelectionListener selectionListener,
	                                  ModelStructureChangeListener structureChangeListener) {
		this.modelHandler = modelHandler;
		this.modelEditorChangeListener = modelEditorChangeListener;
		this.selectionListener = selectionListener;
		this.structureChangeListener = structureChangeListener;
		this.viewportSelectionHandler = new ViewportSelectionHandler(modeButtonGroup, null);
	}

	public ModelStructureChangeListener getStructureChangeListener() {
		return structureChangeListener;
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
