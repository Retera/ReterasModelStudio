package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.*;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * The ModelPanel is a pane holding the display of a given MDL model. I plan to
 * tab between them.
 *
 * Eric Theller 6/7/2012
 */
public class ModelPanel implements MouseListener {
	private DisplayPanel frontArea, sideArea, botArea;
	private PerspDisplayPanel perspArea;
	private EditableModel model;
	private final UndoHandler undoHandler;
	private final ModelView modelView;
	private final UndoManager undoManager;
	private final RenderModel editorRenderModel;
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
	                  EditableModel editableModel,
	                  ProgramPreferences prefs,
	                  UndoHandler undoHandler,
	                  ToolbarButtonGroup<SelectionItemTypes> notifier,
	                  ToolbarButtonGroup<SelectionMode> modeNotifier,
	                  ModelStructureChangeListener modelStructureChangeListener,
	                  CoordDisplayListener coordDisplayListener,
	                  ViewportTransferHandler viewportTransferHandler,
	                  ViewportListener viewportListener,
	                  Icon icon,
	                  boolean specialBLPModel) {
		this.model = editableModel;
		this.undoHandler = undoHandler;
		modelView = new ModelView(editableModel);
		undoManager = new UndoManagerImpl(undoHandler);
		editorRenderModel = modelView.getEditorRenderModel();

		this.parent = parent;
		this.prefs = prefs;
		selectionItemTypeNotifier = notifier;
		this.icon = icon;
		viewportActivityManager = new ModelEditorViewportActivityManager(new DoNothingActivity());

		modelEditorChangeNotifier = new ModelEditorChangeNotifier();
		modelEditorChangeNotifier.subscribe(viewportActivityManager);




		modelEditorManager = new ModelEditorManager(modelView, prefs, modeNotifier, modelEditorChangeNotifier, viewportActivityManager, editorRenderModel, modelStructureChangeListener);

		modelViewManagingTree = new ModelViewManagingTree(modelView, undoManager, modelEditorManager);

		modelComponentBrowserTree = new ModelComponentBrowserTree(modelView, undoManager, modelEditorManager, modelStructureChangeListener);

		selectionItemTypeNotifier.addToolbarButtonListener(modelEditorManager::setSelectionItemType);

		frontArea = getDisplayPanel(modelStructureChangeListener, coordDisplayListener, viewportTransferHandler, viewportListener, "Front", (byte) 1, (byte) 2);
		botArea = getDisplayPanel(modelStructureChangeListener, coordDisplayListener, viewportTransferHandler, viewportListener, "Bottom", (byte) 1, (byte) 0);
		sideArea = getDisplayPanel(modelStructureChangeListener, coordDisplayListener, viewportTransferHandler, viewportListener, "Side", (byte) 0, (byte) 2);

		animationViewer = new AnimationControllerListener(modelView, prefs, !specialBLPModel);

		animationController = new AnimationController(modelView, true, animationViewer, animationViewer.getCurrentAnimation());

		frontArea.setControlsVisible(prefs.showVMControls());
		botArea.setControlsVisible(prefs.showVMControls());
		sideArea.setControlsVisible(prefs.showVMControls());

		perspArea = new PerspDisplayPanel("Perspective", modelView, prefs);

		componentsPanel = new ComponentsPanel(modelView, undoManager, modelStructureChangeListener);

		modelComponentBrowserTree.addSelectListener(componentsPanel);
	}

	private DisplayPanel getDisplayPanel(ModelStructureChangeListener modelStructureChangeListener, CoordDisplayListener coordDisplayListener, ViewportTransferHandler viewportTransferHandler, ViewportListener viewportListener, String side, byte i, byte i2) {
		return new DisplayPanel(side, i, i2, modelView, modelEditorManager, modelStructureChangeListener,
				viewportActivityManager, prefs, undoManager, coordDisplayListener, undoHandler,
				modelEditorChangeNotifier, viewportTransferHandler, editorRenderModel, viewportListener);
	}

	public RenderModel getEditorRenderModel() {
		return editorRenderModel;
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
		viewportActivityManager.setCurrentActivity(activityDescriptor.createActivity(modelEditorManager, modelView, undoManager));
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

	public boolean close(final ModelPanelCloseListener listener)// MainPanel parent) TODO fix
	{
		// returns true if closed successfully
		boolean canceled = false;
		// int myIndex = parent.tabbedPane.indexOfComponent(this);
		if (!undoManager.isUndoListEmpty()) {
			final Object[] options = { "Yes", "No", "Cancel" };
			final int n = JOptionPane.showOptionDialog(parent,
					"Would you like to save " + model.getName()/* parent.tabbedPane.getTitleAt(myIndex) */ + " (\""
							+ model.getHeaderName() + "\") before closing?",
					"Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
					options[2]);
			switch (n) {
			case JOptionPane.YES_OPTION:
				// ((ModelPanel)parent.tabbedPane.getComponentAt(myIndex)).getMDLDisplay().getMDL().saveFile();
				listener.save(model);
				// parent.tabbedPane.remove(myIndex);
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

	@Override
	public void mouseEntered(final MouseEvent e) {
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	}

	@Override
	public void mousePressed(final MouseEvent e) {
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	public DisplayPanel getFrontArea() {
		return frontArea;
	}

	public void setFrontArea(final DisplayPanel frontArea) {
		this.frontArea = frontArea;
	}

	public DisplayPanel getSideArea() {
		return sideArea;
	}

	public void setSideArea(final DisplayPanel sideArea) {
		this.sideArea = sideArea;
	}

	public DisplayPanel getBotArea() {
		return botArea;
	}

	public void setBotArea(final DisplayPanel botArea) {
		this.botArea = botArea;
	}

	public PerspDisplayPanel getPerspArea() {
		return perspArea;
	}

	public void setPerspArea(final PerspDisplayPanel perspArea) {
		this.perspArea = perspArea;
	}

	public UndoManager getUndoManager() {
		return undoManager;
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
		return model;
	}

	public ModelView getModelViewManager() {
		return modelView;
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
