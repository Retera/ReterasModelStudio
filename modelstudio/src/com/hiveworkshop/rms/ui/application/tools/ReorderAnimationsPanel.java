package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.SetAnimationStartAction;
import com.hiveworkshop.rms.editor.actions.animation.SortAnimationsAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.AnimListCellRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.AnimShell;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanel;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class ReorderAnimationsPanel extends JPanel {

	AnimListCellRenderer renderer = new AnimListCellRenderer(true); //ToDo: make a new renderer and only use Animation
	IterableListModel<AnimShell> animations = new IterableListModel<>();
	JList<AnimShell> animList = new JList<>(animations);
	JScrollPane animPane = new JScrollPane(animList);
	SpinnerNumberModel numberModel = new SpinnerNumberModel(300, 10, 10000, 10);


	ModelHandler modelHandler;

	public ReorderAnimationsPanel(ModelHandler modelHandler){
		super(new MigLayout("gap 0", "[grow][][grow]", "[align center][grow][align center]"));
		this.modelHandler = modelHandler;
		reMakeAnimList();
		add(getSpacingPanel(), "wrap, spanx, align center");
		add(getAnimListPanel());
		add(getArrowPanel(),"wrap");
		JButton apply = new JButton("Apply");
		apply.addActionListener(e -> applyOrder());
		add(apply, "wrap, spanx, align center");

	}

	private JPanel getSpacingPanel() {
		JPanel spacingPanel = new JPanel(new MigLayout(""));
		spacingPanel.add(new JLabel("Frames Between Animations"));
		JSpinner spinner = new JSpinner(numberModel);
		spacingPanel.add(spinner, "");
		return spacingPanel;
	}

	private void reMakeAnimList(){
		animations.clear();
		for(Animation animation : modelHandler.getModel().getAnims()){
			animations.addElement(new AnimShell(animation));
		}
	}

	private JPanel getArrowPanel() {
		JPanel arrowPanel = new JPanel(new MigLayout("gap 0, ins 0", "[]5", "[align center]16[align center]"));

		JButton moveUp = new JButton(ImportPanel.moveUpIcon);
		moveUp.addActionListener(e -> moveUp());
		arrowPanel.add(moveUp, "wrap");

		JButton moveDown = new JButton(ImportPanel.moveDownIcon);
		moveDown.addActionListener(e -> moveDown());
		arrowPanel.add(moveDown, "wrap");
		return arrowPanel;
	}

	private JPanel getAnimListPanel() {
		JPanel animListPanel = new JPanel(new MigLayout("gap 0, ins 0", "[grow, align center]", "[][grow][]"));
//		animListPanel.add(new JLabel("New Refs"), "wrap");

		animList.setCellRenderer(renderer);
		animPane.setPreferredSize(new Dimension(400, 500));
		animListPanel.add(animPane, "wrap");
		return animListPanel;
	}


	private void moveDown() {
		final int[] indices = animList.getSelectedIndices();
		if (indices != null && indices.length > 0) {
			if (indices[indices.length - 1] < animations.size() - 1) {
				for (int i = indices.length - 1; i >= 0; i--) {
					AnimShell animShell = animations.get(indices[i]);
					animations.removeElement(animShell);
					animations.add(indices[i] + 1, animShell);
					indices[i] += 1;
				}
			}
			animList.setSelectedIndices(indices);
		}
	}

	private void moveUp() {
		final int[] indices = animList.getSelectedIndices();
		if (indices != null && indices.length > 0) {
			if (indices[0] > 0) {
				for (int i = 0; i < indices.length; i++) {
					AnimShell animShell = animations.get(indices[i]);
					animations.removeElement(animShell);
					animations.add(indices[i] - 1, animShell);
					indices[i] -= 1;
				}
			}
			animList.setSelectedIndices(indices);
		}
	}

	private void applyOrder() {
		TreeMap<Animation, Integer> newAnimationsStartMap = getNewAnimationsMap();
		List<UndoAction> setFlagActions = new ArrayList<>();

		for (Animation animation : newAnimationsStartMap.keySet()) {
			setFlagActions.add(new SetAnimationStartAction(animation, animation.getStart(), null));
		}
		setFlagActions.add(new SortAnimationsAction(modelHandler.getModel()));
		UndoAction action = new CompoundAction("Reorder Animations", setFlagActions, ModelStructureChangeListener.changeListener::animationParamsChanged);
		modelHandler.getUndoManager().pushAction(action.redo());
	}

	private TreeMap<Animation, Integer> getNewAnimationsMap() {
		TreeMap<Animation, Integer> animationsToNewStarts = new TreeMap<>(Comparator.comparingInt(Animation::getStart));
		int framesBetween = numberModel.getNumber().intValue();
		int lastEnd = 0;
		for (AnimShell animShell : animations) {
			Animation animation = animShell.getAnim();
			animationsToNewStarts.put(animation, lastEnd + framesBetween);
			lastEnd = animation.getEnd();
		}

		return animationsToNewStarts;
	}
}
