package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.DoNothingActivity;
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
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeNotifier;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.InfoPopup;

import javax.swing.*;

/**
 * The ModelPanel is a pane holding the display of a given MDL model. I plan to
 * tab between them.
 *
 * Eric Theller 6/7/2012
 */
public class ModelPanel {
	private DisplayPanel frontArea, sideArea, botArea;
	private PerspDisplayPanel perspArea;
	private ModelHandler modelHandler;
	private final ProgramPreferences prefs;
	private final ToolbarButtonGroup<SelectionItemTypes> selectionItemTypeNotifier;
	private final ModelEditorViewportActivityManager viewportActivityManager;
	private final ModelEditorChangeNotifier modelEditorChangeNotifier;
	private final ModelEditorManager modelEditorManager;
	private UVPanel editUVPanel;

	private final ModelViewManagingTree modelViewManagingTree;
	private final ModelComponentBrowserTree modelComponentBrowserTree;
	private final JComponent parent;
	private final Icon icon;
	private JMenuItem menuItem;
	private final AnimationControllerListener animationViewer;
	private final AnimationController animationController;
	private final ComponentsPanel componentsPanel;

	public ModelPanel(JComponent parent,
	                  ModelHandler modelHandler,
	                  ProgramPreferences prefs,
	                  ToolbarButtonGroup<SelectionItemTypes> notifier,
	                  ToolbarButtonGroup<SelectionMode> modeNotifier,
	                  ModelStructureChangeListener modelStructureChangeListener,
	                  CoordDisplayListener coordDisplayListener,
	                  ViewportTransferHandler viewportTransferHandler,
	                  ViewportListener viewportListener,
	                  Icon icon,
	                  boolean specialBLPModel) {
		this.modelHandler = modelHandler;

		this.parent = parent;
		this.prefs = prefs;
		selectionItemTypeNotifier = notifier;
		this.icon = icon;
		viewportActivityManager = new ModelEditorViewportActivityManager(new DoNothingActivity());

		modelEditorChangeNotifier = new ModelEditorChangeNotifier();
		modelEditorChangeNotifier.subscribe(viewportActivityManager);

		modelEditorManager = new ModelEditorManager(modelHandler, prefs, modeNotifier, modelEditorChangeNotifier, viewportActivityManager, modelStructureChangeListener);

		modelViewManagingTree = new ModelViewManagingTree(modelHandler, modelEditorManager);

		modelComponentBrowserTree = new ModelComponentBrowserTree(modelHandler, modelEditorManager, modelStructureChangeListener);

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
				viewportActivityManager, prefs,  coordDisplayListener,
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

	public void changeActivity(final ActivityDescriptor activityDescriptor) {
		viewportActivityManager.setCurrentActivity(activityDescriptor.createActivity(modelEditorManager, modelHandler));
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

	public boolean close(MainPanel mainPanel)// MainPanel parent) TODO fix
	{
		// returns true if closed successfully
		boolean canceled = false;
		// int myIndex = parent.tabbedPane.indexOfComponent(this);
		if (!modelHandler.getUndoManager().isUndoListEmpty()) {
			final Object[] options = {"Yes", "No", "Cancel"};
			final int n = JOptionPane.showOptionDialog(parent,
					"Would you like to save " + modelHandler.getModel().getName()/* parent.tabbedPane.getTitleAt(myIndex) */ + " (\""
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
					// parent.tabbedPane.remove(myIndex);
					if (editUVPanel != null) {
						editUVPanel.getView().setVisible(false);
					}
					break;
			case JOptionPane.CANCEL_OPTION:
				canceled = true;
				break;
			}
		} else {
			// parent.tabbedPane.remove(myIndex);
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

	public void viewMatrices() {
		InfoPopup.show(parent, modelEditorManager.getModelEditor().getSelectedMatricesDescription());
	}

	public EditableModel getModel() {
		return modelHandler.getModel();
	}

	public ModelView getModelViewManager() {
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
}
