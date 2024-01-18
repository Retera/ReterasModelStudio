package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.SetAnimationStartAction;
import com.hiveworkshop.rms.editor.actions.animation.SortAnimationsAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.AnimationListCellRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.ListStatus;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.util.TwiList;
import com.hiveworkshop.rms.util.uiFactories.Button;
import com.hiveworkshop.rms.util.uiFactories.CheckBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class ReorderAnimationsPanel extends JPanel {
	private final Map<Animation, Integer> animToOrgIndex;
	private final TwiList<Animation> animList;
	private final ModelHandler modelHandler;
	private int startAt = 100;
	private int animSpacing = 100;
	private boolean doAlignTo;
	private int alignTo = 100;

	public ReorderAnimationsPanel(ModelHandler modelHandler){
		super(new MigLayout("gap 0, ins 0 n n n", "[grow][][grow]", "[align center][grow][align center]"));
		this.modelHandler = modelHandler;
		animToOrgIndex = getAnimToOrgIndex(modelHandler.getModel().getAnims());
		animList = getAnimList(modelHandler.getModel().getAnims());
		add(getSpacingPanel(), "wrap, spanx, align center");
		add(getAnimListPanel());
		add(getArrowPanel(),"wrap");
		JButton apply = Button.create("Apply", e -> applyOrder());
		add(apply, "align center, gap 0 50, spanx, split 2");
		JButton reset = Button.create("Reset", e -> reMakeAnimList());
		add(reset, "align center, wrap");

	}

	private ListStatus getListStatus(Object animation){
		if (animation instanceof Animation
				&& animList.get(animToOrgIndex.get(animation)) != animation){
			return ListStatus.MODIFIED;
		} else {
			return ListStatus.FREE;
		}
	}

	private JPanel getSpacingPanel() {
		JPanel spacingPanel = new JPanel(new MigLayout("gap rel 0, ins 0 n 0 n"));
		JLabel minSpaceL = new JLabel("Spacing");
		minSpaceL.setToolTipText("Frames Between Animations");
		IntEditorJSpinner minSpaceS = new IntEditorJSpinner(animSpacing, 10, 10000, 100, i -> animSpacing = i);

		JLabel startAtL = new JLabel("Start At");
		startAtL.setToolTipText("Place first animation at");
		IntEditorJSpinner startAtS = new IntEditorJSpinner(startAt, 0, 10000, 100, i -> startAt = i);

		IntEditorJSpinner alignSpaceS = new IntEditorJSpinner(alignTo, 10, 10000, 100, i -> alignTo = i);
		alignSpaceS.setEnabled(false);
		JCheckBox alignSpaceC = CheckBox.create("Align to", b -> {
			doAlignTo = b;
			alignSpaceS.setEnabled(b);
			minSpaceL.setText(b ? "Min Spacing" : "Spacing");
			minSpaceL.setToolTipText(b ? "Min Frames Between Animations" : "Frames Between Animations");
			spacingPanel.repaint();
		});
		CheckBox.setTooltip(alignSpaceC, "Start each animation on frames divisible by");

		spacingPanel.add(startAtL, "");
		spacingPanel.add(minSpaceL, "");
		spacingPanel.add(alignSpaceC, "wrap");
		spacingPanel.add(startAtS, "");
		spacingPanel.add(minSpaceS, "");
		spacingPanel.add(alignSpaceS, "");
		return spacingPanel;
	}

	private void reMakeAnimList(){
		animList.clear();
		remakeIndexMap();
		animList.addAll(modelHandler.getModel().getAnims());
	}

	private void remakeIndexMap() {
		animToOrgIndex.clear();
		for (Animation animation : modelHandler.getModel().getAnims()){
			animToOrgIndex.put(animation, animToOrgIndex.size());
		}
	}

	private TwiList<Animation> getAnimList(ArrayList<Animation> anims){
		AnimationListCellRenderer renderer =
				new AnimationListCellRenderer(true, this::getListStatus)
				.setOldIndexFunc(animToOrgIndex::get);
		return new TwiList<>(new ArrayList<>(anims)).setRenderer(renderer);
	}

	private Map<Animation, Integer> getAnimToOrgIndex(ArrayList<Animation> anims) {
		Map<Animation, Integer> animToOrgIndex = new HashMap<>();
		for (Animation animation : anims){
			animToOrgIndex.put(animation, animToOrgIndex.size());
		}
		return animToOrgIndex;
	}

	private JPanel getArrowPanel() {
		JPanel arrowPanel = new JPanel(new MigLayout("gap 0, ins 0", "[]", "[align center]16[align center]0"));

		JButton moveUp = Button.create(RMSIcons.moveUpIcon, e -> move(-1));
		arrowPanel.add(moveUp, "wrap");

		JButton moveDown = Button.create(RMSIcons.moveDownIcon, e -> move(1));
		arrowPanel.add(moveDown, "wrap");
		return arrowPanel;
	}

	private JPanel getAnimListPanel() {
		JPanel animListPanel = new JPanel(new MigLayout("gap 0, ins 0", "[grow, align center]", "[][grow][]"));
		JScrollPane animPane = new JScrollPane(animList);
		animPane.setPreferredSize(new Dimension(400, 500));
		animListPanel.add(animPane, "wrap");
		return animListPanel;
	}


	private void move(int dir){
		final int[] indices = animList.getSelectedIndices();
		int length = indices == null ? 0 : indices.length;
		if (0 < length
				&& 0 <= indices[0] + dir
				&& indices[length - 1] + dir < animList.listSize()) {
			int start = 0 < dir ? length - 1 : 0;
			for (int i = start; 0 <= i && i <= length - 1; i -= dir) {
				animList.moveElement(indices[i], dir);
				indices[i] += dir;
			}
			animList.setSelectedIndices(indices);
		}
	}

	private void applyOrder() {
		TreeMap<Animation, Integer> newAnimationsStartMap = getNewAnimStartsMap();

		List<UndoAction> undoActions = new ArrayList<>();
		for (Animation animation : newAnimationsStartMap.keySet()) {
			undoActions.add(new SetAnimationStartAction(animation, newAnimationsStartMap.get(animation), null));
		}
		undoActions.add(new SortAnimationsAction(modelHandler.getModel()));

		UndoAction action = new CompoundAction("Reorder Animations", undoActions, () -> {
			ModelStructureChangeListener.changeListener.animationParamsChanged();
			remakeIndexMap();
			animList.repaint();
		});
		modelHandler.getUndoManager().pushAction(action.redo());
	}

	private TreeMap<Animation, Integer> getNewAnimStartsMap() {
		TreeMap<Animation, Integer> animationsToNewStarts = new TreeMap<>(Comparator.comparingInt(Animation::getStart));
		int nextStartAt = startAt;
		for (Animation animation : animList.getListModel()) {
			animationsToNewStarts.put(animation, nextStartAt);
			nextStartAt += (animation.getLength() + animSpacing);
			if (doAlignTo) {
				nextStartAt += alignTo - (nextStartAt % alignTo);
			}
		}

		return animationsToNewStarts;
	}
}
