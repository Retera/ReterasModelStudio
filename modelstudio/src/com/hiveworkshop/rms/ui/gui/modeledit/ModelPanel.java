package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.DoNothingActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorMultiManipulatorActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorViewportActivityManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.DisplayPanel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.application.model.ComponentsPanel;
import com.hiveworkshop.rms.ui.application.viewer.AnimationController;
import com.hiveworkshop.rms.ui.application.viewer.AnimationControllerListener;
import com.hiveworkshop.rms.ui.application.viewer.perspective.PerspDisplayPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree.ModelComponentBrowserTree;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.ModelViewManagingTree;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model.ModelEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeNotifier;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Quat;

import javax.swing.*;

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
	private final ToolbarButtonGroup2<SelectionItemTypes> selectionItemTypeNotifier;
	private final ModelEditorViewportActivityManager viewportActivityManager;
	private final ModelEditorChangeNotifier modelEditorChangeNotifier;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final ModelEditorManager modelEditorManager;
	private UVPanel editUVPanel;
	private final JScrollPane modelEditingTreePane;
	private final JScrollPane componentBrowserTreePane;


	private final ModelViewManagingTree modelViewManagingTree;
	private final ModelComponentBrowserTree modelComponentBrowserTree;
	private final MainPanel parent;
	private final Icon icon;
	private JMenuItem menuItem;
	private final AnimationControllerListener animationViewer;
	private final AnimationController animationController;
	private final ComponentsPanel componentsPanel;

	public ModelPanel(MainPanel parent,
	                  ModelHandler modelHandler,
	                  ProgramPreferences prefs,
	                  ToolbarButtonGroup2<SelectionItemTypes> notifier,
	                  ToolbarButtonGroup2<SelectionMode> modeNotifier,
	                  ModelStructureChangeListener modelStructureChangeListener,
	                  CoordDisplayListener coordDisplayListener,
	                  ViewportTransferHandler viewportTransferHandler,
	                  ViewportListener viewportListener,
	                  Icon icon,
	                  boolean specialBLPModel) {
		this.modelHandler = modelHandler;
		this.parent = parent;
		selectionItemTypeNotifier = notifier;
		this.icon = icon;
		viewportActivityManager = new ModelEditorViewportActivityManager(new DoNothingActivity());
		this.modelStructureChangeListener = modelStructureChangeListener;

		modelEditorChangeNotifier = new ModelEditorChangeNotifier();
		modelEditorChangeNotifier.subscribe(viewportActivityManager);

		modelEditorManager = new ModelEditorManager(modelHandler, modeNotifier, modelEditorChangeNotifier, viewportActivityManager, modelStructureChangeListener);

		modelViewManagingTree = new ModelViewManagingTree(modelHandler, modelEditorManager);
		modelEditingTreePane = new JScrollPane(modelViewManagingTree);

		modelComponentBrowserTree = new ModelComponentBrowserTree(modelHandler, modelEditorManager, modelStructureChangeListener);
		componentBrowserTreePane = new JScrollPane(modelComponentBrowserTree);

		selectionItemTypeNotifier.addToolbarButtonListener(modelEditorManager::setSelectionItemType);

		frontArea = getDisplayPanel(modelStructureChangeListener, coordDisplayListener, viewportTransferHandler, viewportListener, "Front", (byte) 1, (byte) 2);
		botArea = getDisplayPanel(modelStructureChangeListener, coordDisplayListener, viewportTransferHandler, viewportListener, "Bottom", (byte) 1, (byte) 0);
		sideArea = getDisplayPanel(modelStructureChangeListener, coordDisplayListener, viewportTransferHandler, viewportListener, "Side", (byte) 0, (byte) 2);

		animationViewer = new AnimationControllerListener(modelHandler, prefs, !specialBLPModel);

		animationController = new AnimationController(modelHandler, true, animationViewer, animationViewer.getCurrentAnimation());

		frontArea.setControlsVisible(prefs.showVMControls());
		botArea.setControlsVisible(prefs.showVMControls());
		sideArea.setControlsVisible(prefs.showVMControls());

		perspArea = new PerspDisplayPanel("Perspective", modelHandler, prefs);

		componentsPanel = new ComponentsPanel(modelHandler, modelStructureChangeListener);

		modelComponentBrowserTree.addSelectListener(componentsPanel);
	}

	private DisplayPanel getDisplayPanel(ModelStructureChangeListener modelStructureChangeListener, CoordDisplayListener coordDisplayListener, ViewportTransferHandler viewportTransferHandler, ViewportListener viewportListener, String side, byte i, byte i2) {
		return new DisplayPanel(side, i, i2, modelHandler, modelEditorManager, modelStructureChangeListener,
				viewportActivityManager, coordDisplayListener,
				modelEditorChangeNotifier, viewportTransferHandler, viewportListener);
	}

	public RenderModel getEditorRenderModel() {
		return modelHandler.getRenderModel();
	}

	public AnimationControllerListener getAnimationViewer() {
		return animationViewer;
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
		ModelEditorMultiManipulatorActivity manipulatorActivity = new ModelEditorMultiManipulatorActivity(builder, modelHandler.getUndoManager(), modelEditorManager.getSelectionView());
		viewportActivityManager.setCurrentActivity(manipulatorActivity);
	}

	public ModelEditorManager getModelEditorManager() {
		return modelEditorManager;
	}

	public UVPanel getEditUVPanel() {
		return editUVPanel;
	}

	public void setEditUVPanel(final UVPanel editUVPanel) {
		this.editUVPanel = editUVPanel;
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
					if (editUVPanel != null) {
						editUVPanel.getView().setVisible(false);
					}
					break;
				case JOptionPane.NO_OPTION:
					if (editUVPanel != null) {
						editUVPanel.getView().setVisible(false);
					}
					break;
			case JOptionPane.CANCEL_OPTION:
				canceled = true;
				break;
			}
		} else {
			if (editUVPanel != null) {
				editUVPanel.getView().setVisible(false);
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
		animationViewer.repaint();
		animationController.repaint();
		modelViewManagingTree.repaint();
		if (editUVPanel != null) {
			editUVPanel.repaint();
		}
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

	public ModelViewManagingTree getModelViewManagingTree() {
		return modelViewManagingTree;
	}

	public ModelComponentBrowserTree getModelComponentBrowserTree() {
		return modelComponentBrowserTree;
	}

	public ComponentsPanel getComponentsPanel() {
		return componentsPanel;
	}

	public JScrollPane getModelEditingTreePane() {
		return modelEditingTreePane;
	}

	public JScrollPane getComponentBrowserTreePane() {
		return componentBrowserTreePane;
	}

	public ModelStructureChangeListener getModelStructureChangeListener() {
		return modelStructureChangeListener;
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
		parent.getCreatorPanel().reloadAnimationList();

		Quat IDENTITY = new Quat();
		getEditorRenderModel().refreshFromEditor(
				IDENTITY, IDENTITY, IDENTITY,
				getPerspArea().getViewport().getParticleTextureInstance());
	}
}
