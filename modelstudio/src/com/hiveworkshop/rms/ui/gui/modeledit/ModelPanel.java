package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.MultiManipulatorActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.DisplayPanel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.application.model.ComponentsPanel;
import com.hiveworkshop.rms.ui.application.viewer.AnimationController;
import com.hiveworkshop.rms.ui.application.viewer.PreviewPanel;
import com.hiveworkshop.rms.ui.application.viewer.perspective.PerspDisplayPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.listener.ModelEditorChangeNotifier;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.ModelEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree.ModelComponentBrowserTree;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.ModelViewManagingTree;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The ModelPanel is a pane holding the display of a given MDL model. I plan to tab between them.
 *
 * Eric Theller 6/7/2012
 */
public class ModelPanel {
	private final DisplayPanel frontArea;
	private final DisplayPanel sideArea;
	private final DisplayPanel botArea;
	private final PerspDisplayPanel perspArea;
	private final ModelHandler modelHandler;
	private final ViewportActivityManager viewportActivityManager;
	private final ModelEditorChangeNotifier modelEditorChangeNotifier;
	private final ModelEditorManager modelEditorManager;
	private Set<UVPanel> editUVPanels = new HashSet<>();
	private final JScrollPane modelEditingTreePane;
	private final JScrollPane componentBrowserTreePane;

	private SelectionItemTypes selectionType = SelectionItemTypes.VERTEX;
	private ModelEditorActionType3 editorActionType = ModelEditorActionType3.TRANSLATION;


	private final ModelViewManagingTree modelViewManagingTree;
	private final ModelComponentBrowserTree modelComponentBrowserTree;
	private final MainPanel parent;
	private final Icon icon;
	private JMenuItem menuItem;
	private final PreviewPanel previewPanel;
	private final AnimationController animationController;
	private final CoordDisplayListener coordDisplayListener;
	private final ViewportTransferHandler viewportTransferHandler;
	private final ViewportListener viewportListener;
//	private final ComponentsPanel componentsPanel;

	Consumer<SelectionItemTypes> selectionItemTypeListener;

	public ModelPanel(ModelHandler modelHandler,
	                  CoordDisplayListener coordDisplayListener,
	                  ViewportTransferHandler viewportTransferHandler,
	                  ViewportListener viewportListener,
	                  Icon icon,
	                  boolean specialBLPModel) {
		this.modelHandler = modelHandler;
		this.parent = ProgramGlobals.getMainPanel();
		this.icon = icon;
		this.coordDisplayListener = coordDisplayListener;
		this.viewportTransferHandler = viewportTransferHandler;
		this.viewportListener = viewportListener;
		viewportActivityManager = new ViewportActivityManager(null);

		modelEditorChangeNotifier = new ModelEditorChangeNotifier();
		modelEditorChangeNotifier.subscribe(viewportActivityManager);

		modelEditorManager = new ModelEditorManager(modelHandler, modelEditorChangeNotifier, viewportActivityManager);

		modelViewManagingTree = new ModelViewManagingTree(modelHandler, modelEditorManager);
		modelEditingTreePane = new JScrollPane(modelViewManagingTree);

		modelComponentBrowserTree = new ModelComponentBrowserTree(modelHandler);
		componentBrowserTreePane = new JScrollPane(modelComponentBrowserTree);

		frontArea = getDisplayPanel("Front", (byte) 1, (byte) 2);
		botArea = getDisplayPanel("Bottom", (byte) 1, (byte) 0);
		sideArea = getDisplayPanel("Side", (byte) 0, (byte) 2);
		setShowControlls();

//		previewPanel = new PreviewPanel(modelHandler, !specialBLPModel);
		previewPanel = new PreviewPanel(modelHandler, !specialBLPModel, viewportActivityManager);

		animationController = new AnimationController(modelHandler, true, previewPanel, previewPanel.getCurrentAnimation());

		perspArea = new PerspDisplayPanel("Perspective", modelHandler);

//		componentsPanel = new ComponentsPanel(modelHandler);
//
//		modelComponentBrowserTree.addSelectListener(componentsPanel);
	}

	private void setShowControlls() {
		frontArea.setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
		botArea.setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
		sideArea.setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
	}

	public DisplayPanel getDisplayPanel(String side, byte i, byte i2) {
		return new DisplayPanel(side, i, i2, modelHandler, modelEditorManager,
				viewportActivityManager, coordDisplayListener,
				viewportTransferHandler, viewportListener);
	}

	public RenderModel getEditorRenderModel() {
		return modelHandler.getRenderModel();
	}

	public PreviewPanel getAnimationViewer() {
		return previewPanel;
	}

	public AnimationController getAnimationController() {
		return animationController;
	}

	public JComponent getParent() {
		return parent;
	}

	public void setJMenuItem(final JMenuItem item) {
		menuItem = item;
	}

	public JMenuItem getMenuItem() {
		return menuItem;
	}

	public Icon getIcon() {
		return icon;
	}

	public void changeActivity(ModelEditorActionType3 action) {
		ModelEditorManipulatorBuilder builder = new ModelEditorManipulatorBuilder(modelEditorManager, modelHandler, action);
		MultiManipulatorActivity manipulatorActivity = new MultiManipulatorActivity(builder, modelHandler, modelEditorManager);
		viewportActivityManager.setCurrentActivity(manipulatorActivity);
	}

	public void changeActivity(ViewportActivity newActivity) {
		viewportActivityManager.setCurrentActivity(newActivity);
	}

	public ModelEditorManager getModelEditorManager() {
		return modelEditorManager;
	}

	public boolean close() {
		// returns true if closed successfully
		boolean canceled = false;
		if (!modelHandler.getUndoManager().isUndoListEmpty()) {
			final Object[] options = {"Yes", "No", "Cancel"};
			final int n = JOptionPane.showOptionDialog(parent,
					"Would you like to save " + modelHandler.getModel().getName() + " (\""
							+ modelHandler.getModel().getHeaderName() + "\") before closing?",
					"Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
					options[2]);
			switch (n) {
				case JOptionPane.YES_OPTION:
					FileDialog fileDialog = new FileDialog(this);
					fileDialog.onClickSaveAs();
					if (!editUVPanels.isEmpty()) {
						for(UVPanel uVpanel : editUVPanels){
							uVpanel.getView().setVisible(false);
						}
					}
					break;
				case JOptionPane.NO_OPTION:
					if (!editUVPanels.isEmpty()) {
						for(UVPanel uVpanel : editUVPanels){
							uVpanel.getView().setVisible(false);
						}
					}
					break;
				case JOptionPane.CANCEL_OPTION:
					canceled = true;
					break;
			}
		} else {
			if (!editUVPanels.isEmpty()) {
				for(UVPanel uVpanel : editUVPanels){
					uVpanel.getView().setVisible(false);
				}
			}
		}
		return !canceled;
	}

	public DisplayPanel getFrontArea() {
		return frontArea;
	}

	public DisplayPanel getSideArea() {
		return sideArea;
	}

	public DisplayPanel getBotArea() {
		return botArea;
	}

	public PerspDisplayPanel getPerspArea() {
		return perspArea;
	}

	public UndoManager getUndoManager() {
		return modelHandler.getUndoManager();
	}

	public void repaintSelfAndRelatedChildren() {
		botArea.repaint();
		sideArea.repaint();
		frontArea.repaint();
		perspArea.repaint();
		previewPanel.repaint();
		animationController.repaint();
		modelViewManagingTree.repaint();
		if (!editUVPanels.isEmpty()) {
			editUVPanels.removeIf(p -> !p.isVisible());
			editUVPanels.forEach(Component::repaint);
		}
	}

	public ModelPanel addUVPanel(UVPanel uvPanel){
		editUVPanels.add(uvPanel);
		return this;
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
//
//	public ModelViewManagingTree getModelViewManagingTree() {
//		return modelViewManagingTree;
//	}
//
//	public ModelComponentBrowserTree getModelComponentBrowserTree() {
//		return modelComponentBrowserTree;
//	}

	public ComponentsPanel getComponentsPanel() {
		return modelComponentBrowserTree.getComponentsPanel();
	}

	public JScrollPane getModelEditingTreePane() {
		return modelEditingTreePane;
	}

	public JScrollPane getComponentBrowserTreePane() {
		return componentBrowserTreePane;
	}

	public void reloadComponentBrowser() {
		componentBrowserTreePane.setViewportView(modelComponentBrowserTree.reloadFromModelView());
		modelComponentBrowserTree.repaint();
		componentBrowserTreePane.repaint();
	}

	public void reloadModelEditingTree() {
		modelEditingTreePane.setViewportView(modelViewManagingTree.reloadFromModelView());
		modelViewManagingTree.repaint();
		modelEditingTreePane.repaint();
	}

	public void repaintModelTrees() {
		if (modelEditingTreePane != null) {
			modelViewManagingTree.repaint();
			modelEditingTreePane.repaint();
		}
		if (componentBrowserTreePane != null) {
			componentBrowserTreePane.repaint();
		}
	}

	public void reloadGeosetManagers() {
		reloadModelEditingTree();
		reloadComponentBrowser();

		getPerspArea().reloadTextures();
		getAnimationViewer().reload();
		getAnimationController().reload();
		parent.getMainLayoutCreator().getCreatorPanel().reloadAnimationList();

		getEditorRenderModel().refreshFromEditor(getPerspArea().getViewport().getParticleTextureInstance());
	}

	public void setSelectionType(SelectionItemTypes selectionType){
		this.selectionType = selectionType;
		modelEditorManager.setSelectionItemType(selectionType);
		ModelLoader.refreshAnimationModeState();
	}

	public SelectionItemTypes getSelectionType(){
		return selectionType;
	}

	public void setEditorActionType(ModelEditorActionType3 editorActionType){
		this.editorActionType = editorActionType;
		changeActivity(editorActionType);
	}

	public ModelEditorActionType3 getEditorActionType(){
		return editorActionType;
	}

//	public void setSelectionMode(SelectionItemTypes selectionType){
//		this.selectionType = selectionType;
//		modelEditorManager.setSelectionItemType(selectionType);
//		ModelLoader.refreshAnimationModeState();
//	}
//
//	public SelectionItemTypes getSelectionMode(){
//		return selectionType;
//	}
}
