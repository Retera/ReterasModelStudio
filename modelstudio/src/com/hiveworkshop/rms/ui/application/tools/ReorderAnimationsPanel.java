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
	private final List<Animation> backingAnimList;
	private final ModelHandler modelHandler;
	private int startAt = 100;
	private int animSpacing = 100;
	private boolean doAlignTo;
	private int alignTo = 100;

	public ReorderAnimationsPanel(ModelHandler modelHandler){
		super(new MigLayout("gap 0, ins 0 n n n", "[grow][][grow]", "[align center][grow][align center]"));
		this.modelHandler = modelHandler;
		animToOrgIndex = getAnimToOrgIndex(modelHandler.getModel().getAnims());
		backingAnimList = new ArrayList<>(modelHandler.getModel().getAnims());
		animList = getAnimList(backingAnimList);
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
		spacingPanel.add(Button.create("AutoSort", e -> autoSort()), "");
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

	private TwiList<Animation> getAnimList(List<Animation> anims){
		AnimationListCellRenderer renderer =
				new AnimationListCellRenderer(true, this::getListStatus)
				.setOldIndexFunc(animToOrgIndex::get);
		return new TwiList<>(anims).setRenderer(renderer);
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

	private void autoSort(){
//		List<Animation> newOrder = new ArrayList<>(modelHandler.getModel().getAnims());
		List<Animation> selectedValuesList = animList.getSelectedValuesList();
		Map<String, Integer> keyToInd = getKeyToInd();
		backingAnimList.sort((a1, a2) -> compAnim(a1, a2, keyToInd));
		int[] selectedInds = new int[selectedValuesList.size()];
		for (int i = 0; i < selectedValuesList.size(); i++) {
			selectedInds[i] = backingAnimList.indexOf(selectedValuesList.get(i));
		}
		animList.setSelectedIndices(selectedInds);
		repaint();

//		List<String> sortedPrimKeywords = getSortedPrimKeywords();
//		sortedPrimKeywords.addAll(getPropKeys());
//		Map<String, List<Animation>> firstKeyToAnim = new HashMap<>();
//		for (Animation animation : modelHandler.getModel().getAnims()) {
//			firstKeyToAnim.computeIfAbsent(animation.getName().split(" ")[0].toUpperCase(), k -> new ArrayList<>()).add(animation);
//		}
//
//		List<Animation> newOrder2 = new ArrayList<>();
//		for(String key : sortedPrimKeywords) {
//			newOrder2.addAll(firstKeyToAnim.getOrDefault(key, Collections.emptyList()));
//			firstKeyToAnim.remove(key);
//		}
//		firstKeyToAnim.keySet().stream().sorted().forEach(s -> newOrder2.addAll(firstKeyToAnim.get(s)));
//
//		String name1 = "stand walk spell attack birth morph";
//		String name2 = "stand walk attack spell birth morph";
//		String name3 = "walk attack spell birth morph";

	}

	private int compAnim(Animation anim1, Animation anim2, Map<String, Integer> keyToInd) {
		String[] name1 = anim1.getName().toLowerCase().split(" +-* *");
		String[] name2 = anim2.getName().toLowerCase().split(" +-* *");
		for (int i = 0; i < Math.min(name1.length, name2.length); i++) {
			String key1 = name1[i];
			String key2 = name2[i];
			if (!key1.equals(key2)) {
				if (key1.matches("\\d+") && key2.matches("\\d+")){
					return Integer.parseInt(key1) - Integer.parseInt(key2);
				} else if (key1.matches("\\d+")){
					return -1;
				} else if (key2.matches("\\d+")){
					return 1;
				} else if (keyToInd.containsKey(key1) && keyToInd.containsKey(key2)) {
					return keyToInd.get(key1) - keyToInd.get(key2);
				} else if (keyToInd.containsKey(key1)) {
					return -1;
				} else if (keyToInd.containsKey(key2)) {
					return 1;
				} else {
					return key1.compareTo(key2);
				}
			}
		}
		return name1.length - name2.length;
	}

	private Map<String, Integer> getKeyToInd() {
		List<String> sortedKeywords = getPropKeys();
		Map<String, Integer> map = new HashMap<>();
		for (String k : sortedKeywords) {
			map.put(k.toLowerCase(), map.size());
		}
		return map;
	}

	private List<String> getSortedPrimKeywords() {
		return new ArrayList<>(Arrays.asList("stand", "walk", "spell", "attack", "birth", "morph", "sleep", "death", "decay", "dissipate", "portrait", "cinematic"));
	}
	private List<String> getPropKeys(){
		return Arrays.asList(
				"off",
				"first",
				"second",
				"third",
				"fourth",
				"fifth",
				"one",
				"two",
				"three",
				"four",
				"five",
				"stand", "walk", "attack", "spell", "birth", "morph", "sleep", "death", "decay", "dissipate", "portrait", "cinematic",
				"gold",
				"lumber",
				"work",
				"ready",
				"alternate",
				"alternateex",
				"chain",
				"channel",
				"complete",
				"defend",
				"drain",
				"eattree",
				"fast",
				"fill",
				"flail",
				"flesh",
				"fire",
				"hit",
				"left",
				"right",
				"looping",
				"puke",
				"slam",
				"small",
				"medium",
				"large",
				"light",
				"moderate",
				"severe",
				"critical",
				"spiked",
				"spin",
				"turn",
				"swim",
				"talk",
				"throw",
				"victory",
				"wounded",
				"upgrade");
	}
	private List<String> getPropKeys_1(){
		return Arrays.asList(
				"off",
				"first",
				"second",
				"third",
				"fourth",
				"fifth",
				"one",
				"two",
				"three",
				"four",
				"five",
				"gold",
				"lumber",
				"work",
				"ready",
				"alternate",
				"alternateex",
				"chain",
				"channel",
				"complete",
				"defend",
				"drain",
				"eattree",
				"fast",
				"fill",
				"flail",
				"flesh",
				"fire",
				"hit",
				"left",
				"right",
				"looping",
				"puke",
				"severe",
				"slam",
				"small",
				"medium",
				"large",
				"light",
				"moderate",
				"critical",
				"spiked",
				"spin",
				"turn",
				"swim",
				"talk",
				"throw",
				"victory",
				"wounded",
				"upgrade");
	}

	private List<String> getSortedPrimKeywords1() {
		return Arrays.asList("attack", "birth", "cinematic", "death", "decay", "dissipate", "morph", "portrait", "sleep", "spell", "stand", "walk");
	}
	private List<String> getPropKeys1(){
		return Arrays.asList(
				"alternate",
				"alternateex",
				"chain",
				"channel",
				"complete",
				"critical",
				"defend",
				"drain",
				"eattree",
				"fast",
				"fill",
				"flail",
				"flesh",
				"fifth",
				"fire",
				"first",
				"five",
				"four",
				"fourth",
				"gold",
				"hit",
				"large",
				"left",
				"light",
				"looping",
				"lumber",
				"medium",
				"moderate",
				"off",
				"one",
				"puke",
				"ready",
				"right",
				"second",
				"severe",
				"slam",
				"small",
				"spiked",
				"spin",
				"swim",
				"talk",
				"third",
				"three",
				"throw",
				"two",
				"turn",
				"victory",
				"work",
				"wounded",
				"upgrade");
	}
}
