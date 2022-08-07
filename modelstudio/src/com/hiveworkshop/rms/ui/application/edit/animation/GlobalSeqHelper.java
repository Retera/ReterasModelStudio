package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.AddSequenceAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryMapAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveFlagEntryMapAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;

public class GlobalSeqHelper {

	public static void showNewGlobSeqPopup(Component parent, String title, ModelHandler modelHandler) {
		GlobalSeq sequence = getNewSeq(parent, title);
		if(sequence != null){
			EditableModel model = modelHandler.getModel();
			UndoManager undoManager = modelHandler.getUndoManager();
			if (model.contains(sequence)) {
				JOptionPane.showMessageDialog(parent,
						"A Global Sequence with that length already exists." +
								"\nThis program does not support multiple Global Sequences of the same length." +
								"\nInstead, simply add animation data to the sequence of that length which already exists.",
						"Error", JOptionPane.ERROR_MESSAGE);
			} else {
				undoManager.pushAction(new AddSequenceAction(model, sequence, ModelStructureChangeListener.changeListener).redo());
			}
		}
	}

	public static GlobalSeq getNewSeq(Component parent, String title) {
		IntEditorJSpinner spinner = new IntEditorJSpinner(1000, 1, null);
		JPanel panel = new JPanel(new MigLayout());
		panel.add(new JLabel("Choose GlobalSeq"), "wrap");
		panel.add(spinner);
		int option = JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (option == JOptionPane.OK_OPTION) {
			return new GlobalSeq(spinner.getIntValue());
		}
		return null;
	}

	public static void getNewSeq(Component parent, String title, Consumer<GlobalSeq> globalSeqConsumer) {
		IntEditorJSpinner spinner = new IntEditorJSpinner(1000, 1, null);
		JPanel panel = new JPanel(new MigLayout());
		panel.add(new JLabel("Choose GlobalSeq"), "wrap");
		panel.add(spinner);
		int option = JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (option == JOptionPane.OK_OPTION) {
			globalSeqConsumer.accept(new GlobalSeq(spinner.getIntValue()));
		}
	}

	public static GlobalSeq chooseSeq(GlobalSeq current, List<GlobalSeq> globalSeqs, String title, Component parent) {
		TwiComboBox<GlobalSeq> globalSeqBox = new TwiComboBox<>(globalSeqs, new GlobalSeq(1000000));
		globalSeqBox.selectOrFirst(current);
		JPanel panel = new JPanel(new MigLayout());
		panel.add(new JLabel("Choose GlobalSeq"), "wrap");
		panel.add(globalSeqBox);

		int option = JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (option == JOptionPane.OK_OPTION) {
			return globalSeqBox.getSelected();
		}
		return null;
	}
	public static GlobalSeq chooseSeq2(GlobalSeq current, List<GlobalSeq> globalSeqs, String title, Component parent) {
		GlobalSeq[] globalSeq = new GlobalSeq[1];
		chooseSeq(current, globalSeqs, (g) -> globalSeq[0] = g, title, parent);
		return globalSeq[0];
	}
	public static void chooseSeq(GlobalSeq current, List<GlobalSeq> globalSeqs, Consumer<GlobalSeq> globalSeqConsumer, String title, Component parent) {
		TwiComboBox<GlobalSeq> globalSeqBox = new TwiComboBox<>(globalSeqs, new GlobalSeq(1000000));
		globalSeqBox.selectOrFirst(current);
		JPanel panel = new JPanel(new MigLayout());
		panel.add(new JLabel("Choose GlobalSeq"), "wrap");
		panel.add(globalSeqBox);

		int option = JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (option == JOptionPane.OK_OPTION) {
			globalSeqConsumer.accept(globalSeqBox.getSelected());
		}
	}

	public static <Q> void timescaleGlobalSeq(ModelHandler modelHandler, AnimFlag<Q> animFlag, String title, Component parent){
		TwiComboBox<GlobalSeq> globalSeqBox = new TwiComboBox<>(modelHandler.getModel().getGlobalSeqs(), new GlobalSeq(1000000));
		JPanel panel = new JPanel(new MigLayout());
		panel.add(new JLabel("Choose GlobalSeq"), "wrap");
		panel.add(globalSeqBox);

		Animation[] animation = new Animation[1];

		TwiComboBox<Animation> animations = new TwiComboBox<>(new Animation("PrototypePrototypePrototypePrototype", 0, 1));
		animations.addItem(null);
		for(Animation anim : modelHandler.getModel().getAnims()){
			if(animFlag.hasSequence(anim)){
				animations.add(anim);
			}
		}
		animations.addOnSelectItemListener(a -> animation[0] = a);


		panel.add(new JLabel("From Animation"), "wrap");
		panel.add(animations, "wrap");

		JCheckBox removeOthers = new JCheckBox("Remove all other animations?", true);
		panel.add(removeOthers);

		// Keyframes from existing animation
		//      timescale?
		// Remove all other animations?


		int option = JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (option == JOptionPane.OK_OPTION) {
			GlobalSeq globalSeq = globalSeqBox.getSelected();
			TreeMap<Integer, Entry<Q>> entryMap = animFlag.getSequenceEntryMapCopy(animation[0]);
			if(entryMap == null){
				entryMap = new TreeMap<>();
			}
			List<UndoAction> actions = new ArrayList<>();


			if (removeOthers.isSelected()){
				for(Sequence sequence : animFlag.getAnimMap().keySet()){
					actions.add(new RemoveFlagEntryMapAction<>(animFlag, sequence, null));
				}
			}

			if(!modelHandler.getModel().contains(globalSeq)){
				actions.add(new AddSequenceAction(modelHandler.getModel(), globalSeq, null));
			}

			actions.add(new AddFlagEntryMapAction<>(animFlag, globalSeq, entryMap, null));

			CompoundAction action = new CompoundAction("Set to GlobalSeq", actions, ModelStructureChangeListener.changeListener::animationParamsChanged);
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}
}
