package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.edit.uv.TVertexEditorManager;
import com.hiveworkshop.rms.ui.application.model.editors.ThumbnailProvider;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;

import javax.swing.*;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * The ModelPanel is a pane holding the display of a given MDL model. I plan to tab between them.
 *
 * Eric Theller 6/7/2012
 */
public class ModelPanel {
	private final static HashMap<EditableModel, ModelPanel> modelToPanelMap = new HashMap<>();
	private final ModelHandler modelHandler;
	private final ThumbnailProvider thumbnailProvider;
	private final ViewportActivityManager viewportActivityManager;
	private final ViewportActivityManager viewportUVActivityManager;
	private final ModelEditorManager modelEditorManager;

	private final TVertexEditorManager uvModelEditorManager;

	private SelectionItemTypes selectionType = SelectionItemTypes.VERTEX;
	private ModelEditorActionType3 editorActionType = ModelEditorActionType3.TRANSLATION;

	private JMenuItem menuItem;

	Consumer<SelectionItemTypes> selectionItemTypeListener;

	public ModelPanel(ModelHandler modelHandler) {
		this.modelHandler = modelHandler;

		modelEditorManager = new ModelEditorManager(modelHandler);
		viewportActivityManager = new ViewportActivityManager(modelHandler, modelEditorManager);

		uvModelEditorManager = new TVertexEditorManager(modelHandler);
		viewportUVActivityManager = new ViewportActivityManager(modelHandler, uvModelEditorManager);

		changeActivity(editorActionType);

		thumbnailProvider = new ThumbnailProvider(modelHandler.getModel().getWrappedDataSource());

		modelToPanelMap.put(modelHandler.getModel(), this);
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

	public JMenuItem updateMenuItem() {
		menuItem.setText(modelHandler.getModel().getName());
		if(modelHandler.getModel().getFile() != null){
			menuItem.setToolTipText(modelHandler.getModel().getFile().getPath());
		} else {
			menuItem.setToolTipText(null);
		}
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

	public TVertexEditorManager getUvModelEditorManager() {
		return uvModelEditorManager;
	}

	public void close() {
		// removes references
		modelToPanelMap.remove(modelHandler.getModel());

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

	public void refreshFromEditor() {
//		System.out.println("refreshFromEditor");
//		ModelHandler modelHandler = modelPanel.getModelHandler();
		updateRenderModel(modelHandler.getRenderModel());
		updateRenderModel(modelHandler.getPreviewRenderModel());

	}
	public static void updateRenderModel(RenderModel renderModel) {
		TimeEnvironmentImpl timeEnv = renderModel.getTimeEnvironment();
		int animationTime = timeEnv.getAnimationTime();
		Sequence currentSequence = timeEnv.getCurrentSequence();
		timeEnv.setSequence(currentSequence);
		timeEnv.setAnimationTime(animationTime);
		renderModel.refreshFromEditor();
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
		refreshFromEditor();

		ProgramGlobals.getRootWindowUgg().getWindowHandler2().setAnimationMode();
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

	public ThumbnailProvider getThumbnailProvider() {
		return thumbnailProvider;
	}

	public static ModelPanel getModelPanel(EditableModel model){
		return modelToPanelMap.get(model);
	}
}
