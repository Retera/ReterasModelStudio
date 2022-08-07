package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.edit.uv.TVertexEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * The ModelPanel is a pane holding the display of a given MDL model. I plan to tab between them.
 *
 * Eric Theller 6/7/2012
 */
public class ModelPanel {
	private final ModelHandler modelHandler;
	private final ViewportActivityManager viewportActivityManager;
	private final ViewportActivityManager viewportUVActivityManager;
	private final ModelEditorManager modelEditorManager;
	private final TVertexEditorManager uvModelEditorManager;

	private SelectionItemTypes selectionType = SelectionItemTypes.VERTEX;
	private ModelEditorActionType3 editorActionType = ModelEditorActionType3.TRANSLATION;

	private JMenuItem menuItem;

	Consumer<SelectionItemTypes> selectionItemTypeListener;

	public ModelPanel(ModelHandler modelHandler) {
		ModelTextureThings.setModel(modelHandler.getModel());
		this.modelHandler = modelHandler;

		modelEditorManager = new ModelEditorManager(modelHandler);
		viewportActivityManager = new ViewportActivityManager(modelHandler, modelEditorManager);

		uvModelEditorManager = new TVertexEditorManager(modelHandler);
		viewportUVActivityManager = new ViewportActivityManager(modelHandler, uvModelEditorManager);

		changeActivity(editorActionType);
	}

	public ViewportActivityManager getViewportActivityManager() {
		return viewportActivityManager;
	}

	public ViewportActivityManager getUVViewportActivityManager() {
		return viewportUVActivityManager;
	}

	public RenderModel getEditorRenderModel() {
		return modelHandler.getRenderModel();
	}

	public void setJMenuItem(final JMenuItem item) {
		menuItem = item;
	}

	public JMenuItem getMenuItem() {
		return menuItem;
	}

	public Icon getIcon() {
		return modelHandler.getIcon();
	}

	public void changeActivity(ModelEditorActionType3 action) {
		viewportActivityManager.setCurrentActivity(action);
		viewportUVActivityManager.setCurrentActivity(action);

	}

	public void changeActivity(ViewportActivity newActivity) {
		viewportActivityManager.setCurrentActivity(newActivity);
	}

	public ModelEditorManager getModelEditorManager() {
		return modelEditorManager;
	}

	public boolean close() {
		// returns true if closed successfully
		if (!modelHandler.getUndoManager().isUndoListEmpty()) {
			final Object[] options = {"Yes", "No", "Cancel"};
			EditableModel model = modelHandler.getModel();
			final int n = JOptionPane.showOptionDialog(ProgramGlobals.getMainPanel(),
					"Would you like to save " + model.getName()
							+ " (\"" + model.getHeaderName() + "\") " +
							"before closing?",
					"Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
					options[2]);
			return switch (n) {
				case JOptionPane.YES_OPTION -> {
					FileDialog fileDialog = new FileDialog(this);
					yield fileDialog.onClickSaveAs();
				}
				case JOptionPane.NO_OPTION -> true;
				default -> false;
			};
		}
		return true;
	}

	public UndoManager getUndoManager() {
		return modelHandler.getUndoManager();
	}

	public void repaintSelfAndRelatedChildren() {

//		displayPanels.removeIf(displayPanel -> !displayPanel.isValid());
//		displayPanels.forEach(displayPanel -> displayPanel.repaint());
//		perspArea.repaint();
//		previewPanel.repaint();
//		animationController.repaint();
//		modelViewManagingTree.repaint();
//		if (!editUVPanels.isEmpty()) {
//			editUVPanels.removeIf(p -> !p.isVisible());
//			editUVPanels.forEach(Component::repaint);
//		}
	}

	public EditableModel getModel() {
		return modelHandler.getModel();
	}

	public ModelView getModelView() {
		return modelHandler.getModelView();
	}

	public ModelHandler getModelHandler() {
		return modelHandler;
	}

	public void setSelectionType(SelectionItemTypes selectionType) {
		this.selectionType = selectionType;
		modelEditorManager.setSelectionItemType(selectionType);
		uvModelEditorManager.setSelectionItemType(selectionType);
		ModelLoader.refreshAnimationModeState();
	}

	public SelectionItemTypes getSelectionType() {
		return selectionType;
	}

	public void setEditorActionType(ModelEditorActionType3 editorActionType) {
		this.editorActionType = editorActionType;
		changeActivity(editorActionType);
	}

	public ModelEditorActionType3 getEditorActionType() {
		return editorActionType;
	}

	public void deFocus(){
		modelHandler.getRenderModel().getBufferFiller().clearTextureMap();
		modelHandler.getPreviewRenderModel().getBufferFiller().clearTextureMap();
	}
}
