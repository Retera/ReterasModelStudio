package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.NodeAnimationModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;

public class ModelEditorManager extends AbstractModelEditorManager {
	public static boolean MOVE_LINKED;

	public ModelEditorManager(ModelHandler modelHandler,
	                          ToolbarButtonGroup2<SelectionMode> modeButtonGroup,
	                          ModelEditorChangeListener modelEditorChangeListener,
	                          SelectionListener selectionListener,
	                          ModelStructureChangeListener structureChangeListener) {
		super(modelHandler, modeButtonGroup, modelEditorChangeListener, selectionListener, structureChangeListener);
		setSelectionItemType(SelectionItemTypes.VERTEX);
	}

	public void setSelectionItemType(SelectionItemTypes selectionMode) {
		switch (selectionMode) {
			case VERTEX, FACE, GROUP, CLUSTER -> {

				SelectionManager selectionManager = new SelectionManager(modelHandler.getModelView(), selectionMode);
				this.modelEditor = new AbstractModelEditor(selectionManager, structureChangeListener, modelHandler, selectionMode);

				this.selectionManager = selectionManager;
			}
			case ANIMATE -> {
				SelectionManager selectionManager = new SelectionManager(modelHandler.getModelView(), selectionMode);
				modelEditor = new NodeAnimationModelEditor(selectionManager, structureChangeListener, modelHandler, selectionMode);

				this.selectionManager = selectionManager;
			}
			case TPOSE -> {
				boolean moveLinked = MOVE_LINKED;// dialog == settings[0];
				SelectionManager tposeSelectionManager = new SelectionManager(modelHandler.getModelView(), moveLinked, selectionMode);

				modelEditor = new AbstractModelEditor(tposeSelectionManager, structureChangeListener, modelHandler, selectionMode);

				selectionManager = tposeSelectionManager;
			}
		}

		selectionListener.onSelectionChanged(selectionManager);
//		viewportSelectionHandler.setModelEditor(modelEditor);
		viewportSelectionHandler.setModelEditor(selectionManager);
		modelEditorChangeListener.modelEditorChanged(modelEditor);
	}
}
