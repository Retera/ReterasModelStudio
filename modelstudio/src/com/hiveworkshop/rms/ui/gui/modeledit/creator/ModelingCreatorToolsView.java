package com.hiveworkshop.rms.ui.gui.modeledit.creator;


import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;
import com.hiveworkshop.rms.util.TwiComboBoxModel;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

public class ModelingCreatorToolsView extends ModelDependentView {
	private final CreatorModelingPanel creatorModelingPanel;
	private final ManualTransformPanel transformPanel;
	private final TabWindow tabbedPanel;
	private final View transformView;
	private final View addView;
	private final JPanel animationPanel;
	private JComboBox<Sequence> animationChooserBox;
	private ModelHandler modelHandler;
//	public ModelingCreatorToolsView() {
//		super("Modeling", null, new JPanel());
//		creatorModelingPanel = new CreatorModelingPanel(ProgramGlobals.getMainPanel().getViewportListener());
//		this.setComponent(creatorModelingPanel);
//	}
public ModelingCreatorToolsView() {
	super("Modeling", null, new JPanel());
	creatorModelingPanel = new CreatorModelingPanel();
	transformPanel = new ManualTransformPanel();
	animationChooserBox = getAnimationChooserBox();
	animationPanel = new JPanel(new MigLayout("ins 0, fill", "[grow]", "[][grow]"));
	animationPanel.add(animationChooserBox, "wrap");
	transformView = getTitledView("Transform", transformPanel);
	addView = getTitledView("Add", creatorModelingPanel);
	tabbedPanel = new TabWindow(new DockingWindow[] {addView, transformView});
	tabbedPanel.setSelectedTab(0);
//	tabbedPanel.setC
//	this.setComponent(creatorModelingPanel);
	this.setComponent(tabbedPanel);
}

	public static View getTitledView(String title, JPanel panel) {
		return new View(title, null, panel);
	}

	public CreatorModelingPanel getCreatorModelingPanel() {
		return creatorModelingPanel;
	}

	public void reloadAnimationList() {
		creatorModelingPanel.reloadAnimationList();
		reloadAnimationList2();
	}

	public void setAnimationModeState(boolean animationModeState) {
		creatorModelingPanel.setAnimationModeState(animationModeState);
		if (animationModeState){
			transformView.setComponent(null);
			animationPanel.add(transformPanel);
			this.setComponent(animationPanel);
		} else {
			if(animationPanel.getComponentCount()>1){
				animationPanel.remove(1);
			}
			transformView.setComponent(transformPanel);
			this.setComponent(tabbedPanel);
		}
		repaint();
	}

	@Override
	public ModelingCreatorToolsView setModelPanel(ModelPanel modelPanel){
		creatorModelingPanel.setModelPanel(modelPanel);
		transformPanel.setModelPanel(modelPanel);
		if(modelPanel != null){
			modelHandler = modelPanel.getModelHandler();
			reloadAnimationList2();
		} else {
			modelHandler = null;
		}
		return this;
	}

	private JComboBox<Sequence> getAnimationChooserBox() {
		final TwiComboBoxModel<Sequence> animationChooserBoxModel = new TwiComboBoxModel<>();
		JComboBox<Sequence> animationChooserBox = new JComboBox<>(animationChooserBoxModel);
//		animationChooserBox.setPrototypeDisplayValue(new Animation("temporary prototype animation", 0, 1));
		animationChooserBox.addItemListener(this::chooseAnimation);
		return animationChooserBox;
	}

	private void chooseAnimation(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			Sequence selectedItem = (Sequence) animationChooserBox.getSelectedItem();
			if (selectedItem != null && modelHandler != null) {
				modelHandler.getEditTimeEnv().setSequence(selectedItem);
			}
		}
	}

	private void reloadAnimationList2() {
		Sequence selectedItem = (Sequence) animationChooserBox.getSelectedItem();
		List<Sequence> allSequences = new ArrayList<>();
		allSequences.addAll(modelHandler.getModel().getAnims());
		allSequences.addAll(modelHandler.getModel().getGlobalSeqs());
		TwiComboBoxModel<Sequence> animationChooserBoxModel = new TwiComboBoxModel<>(allSequences);
		animationChooserBox.setModel(animationChooserBoxModel);

		if(animationChooserBoxModel.getSize() >= 1){
			if (selectedItem != null && allSequences.contains(selectedItem)) {
				animationChooserBox.setSelectedItem(selectedItem);
			} else {
				animationChooserBox.setSelectedIndex(0);
			}
		}
	}

}
