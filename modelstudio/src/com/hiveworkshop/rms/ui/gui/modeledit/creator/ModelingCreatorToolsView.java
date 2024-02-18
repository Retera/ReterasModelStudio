package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.TPoseModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportPopupMenu;
import com.hiveworkshop.rms.ui.application.model.nodepanels.AnimationChooser;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.util.CollapsablePanel;
import com.hiveworkshop.rms.util.ModelDependentView;
import com.hiveworkshop.rms.util.uiFactories.Button;
import com.hiveworkshop.rms.util.uiFactories.CheckBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ModelingCreatorToolsView extends ModelDependentView {
	private final SelectionInfoPanel selectionPanel;
	private final CreatorModelingPanel creatorModelingPanel;
	private final ManualTransformPanel transformPanel;
	private final JPanel animationPanel;
	private final JPanel tPosePanel;
	private final AnimationChooser animationChooser;
	private ModelHandler modelHandler;

	private JPopupMenu contextMenu;

	public ModelingCreatorToolsView() {
		super("Modeling", null, new JPanel());
		selectionPanel = new SelectionInfoPanel();
		creatorModelingPanel = new CreatorModelingPanel();
		transformPanel = new ManualTransformPanel();

		animationChooser = new AnimationChooser(true, true, false);
		animationPanel = new JPanel(new MigLayout("ins 0, gap 0, fill", "[grow]", "[][]"));
		animationPanel.add(new JLabel("Animation"), "wrap");
		animationPanel.add(animationChooser);

		tPosePanel = new JPanel(new MigLayout("ins 0, gap 0, fill", "[grow]", "[][]"));
		JCheckBox preserve_animations = CheckBox.create("Preserve Animations", TPoseModelEditor.isPreserveAnimations(), TPoseModelEditor::setPreserveAnimations);
		tPosePanel.add(CheckBox.setTooltip(preserve_animations, "Apply the inverse transformation to the node's keyframes"), "");


		JPanel panel = new JPanel(new MigLayout("fill, ins 0, gap 0, hidemode 2", "", "[top][top][top][top][top][top][top, grow]"));

		panel.add(animationPanel, "growx, spanx, wrap");
		animationPanel.setVisible(false);

		panel.add(tPosePanel, "growx, spanx, wrap");
		tPosePanel.setVisible(false);

		panel.add(getCP("Selection", selectionPanel), "top, growx, spanx, wrap");
		panel.add(getCP("Transform", transformPanel), "top, growx, spanx, wrap");
		panel.add(Button.create(this::showVPPopup, "show popup menu"), "top, wrap");
		panel.add(getCP("Add", creatorModelingPanel), "top, growx, spanx, wrap");

		panel.add(new JPanel(), "top, growx, growy, spanx, wrap");
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		// cheat to not show scrollbars while keeping the scroll pane scrollable
		JPanel tempPanel = new JPanel();
		tempPanel.add(scrollPane.getHorizontalScrollBar());
		tempPanel.add(scrollPane.getVerticalScrollBar());

		this.setComponent(scrollPane);
	}

	private CollapsablePanel getCP(String title, JPanel panel) {
		return new CollapsablePanel(title, panel);
	}

	public void reloadAnimationList() {
		creatorModelingPanel.updateAnimationList();
		animationChooser.updateAnimationList();
	}

	public void setAnimationModeState(SelectionItemTypes selectionItemTypes) {
		creatorModelingPanel.setAnimationModeState(selectionItemTypes);
		transformPanel.setAnimationState(selectionItemTypes);
		animationPanel.setVisible(selectionItemTypes == SelectionItemTypes.ANIMATE);
		tPosePanel.setVisible(selectionItemTypes == SelectionItemTypes.TPOSE);
		repaint();
	}

	@Override
	public ModelingCreatorToolsView setModelPanel(ModelPanel modelPanel) {
		selectionPanel.setModelPanel(modelPanel);
		creatorModelingPanel.setModelPanel(modelPanel);
		transformPanel.setModelPanel(modelPanel);
		if (modelPanel != null) {
			modelHandler = modelPanel.getModelHandler();
			animationChooser.setModel(modelHandler.getModel(), modelHandler.getRenderModel());
			contextMenu = new ViewportPopupMenu(null, ProgramGlobals.getMainPanel(), modelPanel.getModelHandler(), modelPanel.getModelEditorManager());
		} else {
			animationChooser.setModel(null, null);
			modelHandler = null;
			contextMenu = null;
		}
		return this;
	}

	private void showVPPopup(JButton button) {
		if (contextMenu != null) {
			contextMenu.show(button, 0, 0);
		}
	}

	@Override
	public ModelDependentView reload() {
		selectionPanel.updateSelectionPanel();
		return super.reload();
	}
}
