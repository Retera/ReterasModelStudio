package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.AddSequenceAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryMapAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveFlagEntryMapAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class GlobalSeqWizard<T> extends JPanel{
	private final ModelHandler modelHandler;
	private final AnimFlag<T> animFlag;

	private Sequence selectedAnim;
	private GlobalSeq existingGlobSeq;
	private GlobalSeq customGlobSeq;
	private GlobalSeq animGlobSeq;
	private boolean removeOthers = true;

	GlobalSeqWizard(ModelHandler modelHandler, AnimFlag<T> animFlag){
		super(new MigLayout());
		this.modelHandler = modelHandler;
		this.animFlag = animFlag;
		selectedAnim = animFlag.getGlobalSeq();


		add(getTypePanel(), "wrap");

		if(!animFlag.hasGlobalSeq()){
			add(new JLabel("From Animation"), "wrap");
			add(getAnimationBox(), "wrap");
		}

		JCheckBox removeOthers = new JCheckBox("Remove all other animations?", true);
		removeOthers.addActionListener(e -> this.removeOthers = removeOthers.isSelected());
		add(removeOthers);
	}

	private void doTurn() {
		GlobalSeq globalSeq = getGlobSeqToUse();
		TreeMap<Integer, Entry<T>> entryMap = animFlag.getSequenceEntryMapCopy(selectedAnim);
		if(entryMap == null){
			entryMap = new TreeMap<>();
		}
		List<UndoAction> actions = new ArrayList<>();


		if (this.removeOthers){
			for(Sequence sequence : animFlag.getAnimMap().keySet()){
				actions.add(new RemoveFlagEntryMapAction<>(animFlag, sequence, null));
			}
		} else if (selectedAnim instanceof GlobalSeq){
			actions.add(new RemoveFlagEntryMapAction<>(animFlag, selectedAnim, null));
		}

		if(!modelHandler.getModel().contains(globalSeq)){
			actions.add(new AddSequenceAction(modelHandler.getModel(), globalSeq, null));
		} else {
			globalSeq = modelHandler.getModel().getGlobalSeqByLength(globalSeq.getLength());
		}

		if(selectedAnim != null && selectedAnim.getLength() != globalSeq.getLength()){
			double ratio = ((double) globalSeq.getLength()) / ((double) selectedAnim.getLength());
			AnimFlagUtils.scaleMapEntries(ratio, entryMap);
		}

		actions.add(new AddFlagEntryMapAction<>(animFlag, globalSeq, entryMap, null));

		CompoundAction action = new CompoundAction("Set GlobalSeq", actions, ModelStructureChangeListener.changeListener::animationParamsChanged);
		modelHandler.getUndoManager().pushAction(action.redo());
	}


	private JPanel getTypePanel() {
		JPanel typePanel = new JPanel(new MigLayout("ins 0"));

		TwiComboBox<GlobalSeq> globalSeqBox = getGlobalSeqBox();
		IntEditorJSpinner customSpinner = new IntEditorJSpinner(1000, 1, Integer.MAX_VALUE, i -> {
			if(customGlobSeq != null){
				customGlobSeq.setLength(i);
			}
		});

		SmartButtonGroup typeGroup = new SmartButtonGroup();
		JRadioButton intoExisting = typeGroup.addJRadioButton("Into Existing", e -> {
			globalSeqBox.setEnabled(true);
			customSpinner.setEnabled(false);
			existingGlobSeq = globalSeqBox.getSelected();
			customGlobSeq = null;
			animGlobSeq = null;
		});
		JRadioButton custom = typeGroup.addJRadioButton("To Custom Length", e -> {
			globalSeqBox.setEnabled(false);
			customSpinner.setEnabled(true);
			existingGlobSeq = null;
			customGlobSeq = new GlobalSeq(customSpinner.getIntValue());
			animGlobSeq = null;
		});
		JRadioButton useAnimationLength = typeGroup.addJRadioButton("Use Animation Length", e -> {
			globalSeqBox.setEnabled(false);
			customSpinner.setEnabled(false);
			existingGlobSeq = null;
			customGlobSeq = null;
			animGlobSeq = new GlobalSeq(selectedAnim == null ? 1000 : selectedAnim.getLength());
		});
		if(!modelHandler.getModel().getGeosets().isEmpty()){
			typePanel.add(intoExisting);
			typePanel.add(globalSeqBox, "wrap");
		}
		typePanel.add(custom);
		typePanel.add(customSpinner, "wrap");
		if(!animFlag.hasGlobalSeq()){
			typePanel.add(useAnimationLength, "wrap");
//			typeGroup.setSelectedName("Use Animation Length");
			useAnimationLength.setSelected(true);
			useAnimationLength.getActionListeners()[0].actionPerformed(null);
		} else {
//			typeGroup.setSelectedName("To Custom Length");
			custom.setSelected(true);
			custom.getActionListeners()[0].actionPerformed(null);
		}
		return typePanel;
	}

	private GlobalSeq getGlobSeqToUse() {
		if(existingGlobSeq != null){
			return existingGlobSeq;
		} else if(customGlobSeq != null){
			return customGlobSeq;
		} else if(animGlobSeq != null){
			return animGlobSeq;
		}
		return new GlobalSeq(1000);
	}

	private TwiComboBox<GlobalSeq> getGlobalSeqBox() {
		TwiComboBox<GlobalSeq> globalSeqBox = new TwiComboBox<>(modelHandler.getModel().getGlobalSeqs(), new GlobalSeq(1000000));
		globalSeqBox.addOnSelectItemListener(g -> existingGlobSeq = g);
		return globalSeqBox;
	}

	private TwiComboBox<Animation> getAnimationBox() {
		TwiComboBox<Animation> animations = new TwiComboBox<>(new Animation("PrototypePrototypePrototypePrototype", 0, 1));
		animations.setStringFunctionRender(this::animationName);
		animations.addItem(null);
		for(Animation anim : modelHandler.getModel().getAnims()){
			if(animFlag.hasSequence(anim)){
				animations.add(anim);
			}
		}
		animations.addOnSelectItemListener(a -> {
			selectedAnim = a;
			if(animGlobSeq != null){
				animGlobSeq.setLength(a == null ? 1000 : a.getLength());
			}
		});
		animations.setSelectedIndex(1);
		if(animGlobSeq != null){
			animGlobSeq.setLength(animations.getSelected() == null ? 1000 : animations.getSelected().getLength());
		}
		return animations;
	}

	private String animationName(Object o){
		if(o == null){
			return "None";
		} else if(o instanceof Animation){
			return ((Animation) o).getName() + "  (" + ((Animation) o).getLength() + ")";
		} else {
			return o.toString();
		}
	}

	public static void showPopup(ModelHandler modelHandler, AnimFlag<?> animFlag, String title, Component parent){
		GlobalSeqWizard<?> globalSeqWizard = new GlobalSeqWizard<>(modelHandler, animFlag);
		int option = JOptionPane.showConfirmDialog(parent, globalSeqWizard, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (option == JOptionPane.OK_OPTION) {
			globalSeqWizard.doTurn();
		}
	}
}
