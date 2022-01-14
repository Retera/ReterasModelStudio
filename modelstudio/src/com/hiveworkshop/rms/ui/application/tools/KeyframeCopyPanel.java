package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.AddSingleAnimationActions;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.ActionFunction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.SequenceComboBoxRenderer;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.TwiComboBoxModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class KeyframeCopyPanel extends JPanel {
	private final FileDialog fileDialog;
	ModelHandler modelHandler;
	String RRW = "\u23EE"; // ⏮
	String FFW = "\u23ED"; // ⏭
	JComboBox<Animation> donAnimBox;
	JSpinner donTimeSpinner;
	JSpinner donTimeEndSpinner;
	JComboBox<Animation> recAnimBox;
	JSpinner recTimeSpinner;

	JLabel recTimeLabel = new JLabel("0");
	JLabel donTimeLabel = new JLabel("0");

	Animation donAnim;
	Animation recAnim;

	/**
	 * Create the panel.
	 */
	public KeyframeCopyPanel(final ModelHandler modelHandler) {
		fileDialog = new FileDialog(this);
		this.modelHandler = modelHandler;
		setLayout(new MigLayout("fill", "[grow][grow]"));

//		add(new JLabel("Copies all keyframes from source animation within specified interval to destination. \nWARNING: Make sure that the copied interval fits within the destination animation."), "spanx, wrap");
		JTextArea info = new JTextArea("Copies all keyframes from chosen interval in source animation into destination animation starting at specified frame.");
		info.setEditable(false);
		info.setOpaque(false);
		info.setLineWrap(true);
		info.setWrapStyleWord(true);
		add(info, "spanx, growx, wrap");
		JTextArea warning = new JTextArea("WARNING: Make sure that the copied interval fits within the destination animation.");
		warning.setEditable(false);
		warning.setOpaque(false);
		warning.setLineWrap(true);
		warning.setWrapStyleWord(true);
		add(warning, "spanx, growx, wrap");
		JButton addAnimButton = new JButton("add new animation");
		addAnimButton.addActionListener(e -> AddSingleAnimationActions.addEmptyAnimation(this.modelHandler));
		add(addAnimButton, "skip, right, wrap");
		animChoosingStuff();

		JPanel donAnimPanel = getDonAnimPanel();
		add(donAnimPanel, "growx, aligny top");

//		donAnimPanel.add(new JButton("\u23E9"));

		JPanel recAnimPanel = getRecAnimPanel();
		add(recAnimPanel, "growx, wrap, aligny top");

		JButton copyButton = getButton(e -> doCopy(), "Copy Keyframe");
		add(copyButton, "spanx, align center, wrap");
	}

	public static void showPanel() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			KeyframeCopyPanel copyPanel = new KeyframeCopyPanel(modelPanel.getModelHandler());
			FramePopup.show(copyPanel, ProgramGlobals.getMainPanel(), "Copy Keyframes");
		}
	}

	private JPanel getRecAnimPanel() {
		JPanel recAnimPanel = new JPanel(new MigLayout("fill, gap 0"));
		recAnimPanel.add(new JLabel("To:"), "wrap");
		recAnimPanel.add(recAnimBox, "growx, gapbottom 5, wrap");
//		recAnimPanel.add(recTimeLabel, "wrap");
		recAnimPanel.add(recTimeSpinner, "wrap");
//		getSpinnerPanel(recAnimPanel, "Start", recTimeSpinner, () -> recAnim.getStart(), () -> recAnim.getEnd());
		getSpinnerPanel(recAnimPanel, "Start", recTimeSpinner, () -> 0, () -> recAnim.getLength());
		return recAnimPanel;
	}

	private JPanel getDonAnimPanel() {
		JPanel donAnimPanel = new JPanel(new MigLayout("fill, gap 0"));
		donAnimPanel.add(new JLabel("From:"), "wrap");
		donAnimPanel.add(donAnimBox, "growx, gapbottom 5, wrap");
//		donAnimPanel.add(donTimeLabel, "wrap");
//		getSpinnerPanel(donAnimPanel, "Start", donTimeSpinner, () -> donAnim.getStart(), () -> donAnim.getEnd());
		getSpinnerPanel(donAnimPanel, "Start", donTimeSpinner, () -> 0, () -> donAnim.getLength());

//		getSpinnerPanel(donAnimPanel, "End", donTimeEndSpinner, () -> donAnim.getStart(), () -> donAnim.getEnd());
		getSpinnerPanel(donAnimPanel, "End", donTimeEndSpinner, () -> 0, () -> donAnim.getLength());
		return donAnimPanel;
	}

	private void getSpinnerPanel(JPanel donAnimPanel, String start, JSpinner donTimeSpinner,
	                             Supplier<Integer> startSupplier, Supplier<Integer> endSupplier) {
		JPanel startP = new JPanel(new MigLayout("fill, gap 0, ins 0", "[5%:10%:10%]10[grow][][]"));
		startP.add(new JLabel(start));
		startP.add(donTimeSpinner, "growx");
		startP.add(getButton(e -> donTimeSpinner.setValue(startSupplier.get()), RRW));
		startP.add(getButton(e -> donTimeSpinner.setValue(endSupplier.get()), FFW));
		donAnimPanel.add(startP, "growx, wrap");
	}

	private JButton getButton(ActionListener actionListener, String s) {
		JButton dRwButton1 = new JButton(s);
		dRwButton1.addActionListener(actionListener);
		return dRwButton1;
	}

	private void animChoosingStuff() {
		List<Animation> animations = modelHandler.getModel().getAnims();

		TwiComboBoxModel<Animation> donBoxModel = new TwiComboBoxModel<>(animations);
		donAnimBox = new JComboBox<>(donBoxModel);
		donAnimBox.setRenderer(new SequenceComboBoxRenderer(modelHandler));
		donAnimBox.addItemListener(this::donAnimChoosen);
		donAnim = animations.get(0);
//		donTimeLabel.setText(donAnim.getStart() + "  to  " + donAnim.getEnd() + "  (" + (donAnim.getEnd() - donAnim.getStart()) + ")");
//		donTimeLabel.setText(donAnim.getLength() + " ticks");
		donTimeSpinner = new JSpinner(getAnimModel(donAnim));
		donTimeEndSpinner = new JSpinner(getAnimModel(donAnim));

		TwiComboBoxModel<Animation> recBoxModel = new TwiComboBoxModel<>(animations);
		recAnimBox = new JComboBox<>(recBoxModel);
		recAnimBox.setRenderer(new SequenceComboBoxRenderer(modelHandler));
		recAnimBox.addItemListener(this::recAnimChosen);
		recAnim = animations.get(0);
//		recTimeLabel.setText(recAnim.getStart() + "  to  " + recAnim.getEnd() + "  (" + (recAnim.getEnd() - recAnim.getStart()) + ")");
//		recTimeLabel.setText(recAnim.getLength() + " ticks");
		recTimeSpinner = new JSpinner(getAnimModel(recAnim));
		revalidate();
	}

	private void recAnimChosen(ItemEvent e) {
		if(e.getStateChange() == ItemEvent.SELECTED && e.getItem() != null){
			recAnim = (Animation) e.getItem();
			recTimeSpinner.setModel(getAnimModel(recAnim));
//		recTimeLabel.setText(recAnim.getStart() + "  to  " + recAnim.getEnd() + "  (" + (recAnim.getEnd() - recAnim.getStart()) + ")");
//			recTimeLabel.setText(recAnim.getLength() + " ticks");
		}
	}

	private void donAnimChoosen(ItemEvent e) {
		if(e.getStateChange() == ItemEvent.SELECTED && e.getItem() != null){
			donAnim = (Animation) e.getItem();
			donTimeSpinner.setModel(getAnimModel(donAnim));
			donTimeEndSpinner.setModel(getAnimModel(donAnim));
//		donTimeLabel.setText(donAnim.getStart() + "  to  " + donAnim.getEnd() + "  (" + (donAnim.getEnd() - donAnim.getStart()) + ")");
//			donTimeLabel.setText(donAnim.getLength() + " ticks");
		}
	}

	private SpinnerNumberModel getAnimModel(Animation animation) {
		return new SpinnerNumberModel(0, 0, animation.getLength(), 1);
	}

	private void doCopy() {
		Integer donStart = (Integer) donTimeSpinner.getValue();
		Integer recStart = (Integer) recTimeSpinner.getValue();
		int times = (Integer) donTimeEndSpinner.getValue() - donStart + 1;
		copyKeyframe(donAnim, donStart, recAnim, recStart, times);
	}

	private void copyKeyframe(Animation donAnimation, int donKeyframe, Animation recAnimation, int recKeyframe, int times) {
		EditableModel model = modelHandler.getModel();
		List<UndoAction> undoActions = new ArrayList<>();
		List<Bone> bones = model.getBones();
		List<Helper> helpers = model.getHelpers();

		for (Bone bone : bones) {
			ArrayList<AnimFlag<?>> animFlags = bone.getAnimFlags();
			undoActions.addAll(getSetKeyframesAction(donAnimation, donKeyframe, recAnimation, recKeyframe, times, animFlags));
		}
		for (Helper helper : helpers) {
			ArrayList<AnimFlag<?>> animFlags = helper.getAnimFlags();
			undoActions.addAll(getSetKeyframesAction(donAnimation, donKeyframe, recAnimation, recKeyframe, times, animFlags));
		}
		modelHandler.getUndoManager().pushAction(new CompoundAction("Copy keyframes", undoActions, ModelStructureChangeListener.changeListener::keyframesUpdated).redo());
	}

	private List<UndoAction> getSetKeyframesAction(Animation donAnimation, int donKeyframe, Animation recAnimation, int recKeyframe, int times, ArrayList<AnimFlag<?>> animFlags) {
		List<UndoAction> undoActions = new ArrayList<>();
		for (AnimFlag<?> animFlag : animFlags) {
			undoActions.addAll(getAnimFlagKeyframeActions(donAnimation, donKeyframe, recAnimation, recKeyframe, times, animFlag));
		}
		return undoActions;
	}

	private <T> List<UndoAction> getAnimFlagKeyframeActions(Animation donAnimation, int donKeyframe, Animation recAnimation, int recKeyframe, int times, AnimFlag<T> animFlag) {
		List<UndoAction> undoActions = new ArrayList<>();
		List<Integer> removeTimes = new ArrayList<>();
		List<Entry<T>> addEntries = new ArrayList<>();
		for (int j = 0; j < times; j++) {
			if(animFlag.hasEntryAt(recAnimation, recKeyframe + j)){
				removeTimes.add(recKeyframe + j);
			}
			if(animFlag.hasEntryAt(donAnimation, donKeyframe + j)){
				addEntries.add(animFlag.getEntryAt(donAnim, donKeyframe + j).deepCopy().setTime(recKeyframe + j));
			}
		}
		if(!removeTimes.isEmpty()){
			undoActions.add(new RemoveFlagEntryAction<>(animFlag, removeTimes, recAnimation, null));
		}
		if(!addEntries.isEmpty()){
			undoActions.add(new AddFlagEntryAction<>(animFlag, addEntries, recAnimation, null));
		}
		return undoActions;
	}
	private <T> void setAnimFlagKeyframe2(Animation donAnimation, int donKeyframe, Animation recAnimation, int recKeyframe, int times, AnimFlag<T> animFlag) {
		List<UndoAction> undoActions = new ArrayList<>();
		for (int j = 0; j < times; j++) {
			if(animFlag.hasEntryAt(recAnimation, recKeyframe + j)){
				undoActions.add(new RemoveFlagEntryAction<>(animFlag, recKeyframe + j, recAnimation, null));
			}
			if(animFlag.hasEntryAt(donAnimation, donKeyframe + j)){
				undoActions.add(new AddFlagEntryAction<>(animFlag, animFlag.getEntryAt(donAnim, donKeyframe + j).deepCopy().setTime(recKeyframe + j), recAnimation, null));
			}
		}
	}

	private <T> void setAnimFlagKeyframe1(Animation donAnimation, int donKeyframe, Animation recAnimation, int recKeyframe, int times, AnimFlag<T> animFlag) {
		for (int j = 0; j < times; j++) {
			animFlag.removeKeyframe(recKeyframe + j, recAnimation);
			Entry<T> entryAt = animFlag.getEntryAt(donAnimation, donKeyframe + j);
			if (entryAt != null) {
				Entry<T> entry = entryAt.deepCopy();
				animFlag.setOrAddEntry(recKeyframe + j, entry, recAnimation);
			}
		}
	}

	private void openModel() {
		FileDialog fileDialog = new FileDialog();

		final EditableModel model = fileDialog.chooseModelFile(FileDialog.OPEN_WC_MODEL);
	}



	private static class CopyKeyframes extends ActionFunction {
		CopyKeyframes(){
			super(TextKey.COPY_KFS_BETWEEN_ANIMS, () -> KeyframeCopyPanel.showPanel());
			setMenuItemMnemonic(KeyEvent.VK_K);
		}
	}

	public static JMenuItem getMenuItem(){
		return new CopyKeyframes().getMenuItem();
	}
}
