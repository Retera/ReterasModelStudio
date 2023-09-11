package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.AddEventTrackAction;
import com.hiveworkshop.rms.editor.actions.animation.AddSequenceAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryMapAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class CombineAnimations extends ActionFunction {
	public CombineAnimations(){
		super(TextKey.BACK2BACK_ANIMATION, CombineAnimations::combineAnimations);
	}

	public static void combineAnimations(ModelHandler modelHandler) {
		EditableModel model = modelHandler.getModel();
		List<Animation> anims = model.getAnims();
		Animation[] array = anims.toArray(new Animation[0]);
		Object choice = JOptionPane.showInputDialog(ProgramGlobals.getMainPanel(),
				"Pick the first animation",
				"Choose 1st Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
		Animation animation = (Animation) choice;

		Object choice2 = JOptionPane.showInputDialog(ProgramGlobals.getMainPanel(),
				"Pick the second animation",
				"Choose 2nd Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
		Animation animation2 = (Animation) choice2;

		String nameChoice = JOptionPane.showInputDialog(ProgramGlobals.getMainPanel(),
				"What should the combined animation be called?");
		if (nameChoice != null) {
			int anim1Length = animation.getLength();
			int anim2Length = animation2.getLength();
			int totalLength = anim1Length + anim2Length;

			int animTrackEnd = ModelUtils.animTrackEnd(model);
			int newStart = animTrackEnd + 1000;
			Animation newAnimation = new Animation(nameChoice, newStart, newStart + totalLength);
			newAnimation.setNonLooping(true);
			newAnimation.setExtents(animation.getExtents().deepCopy());

			List<UndoAction> undoActions = new ArrayList<>();
			undoActions.add(new AddSequenceAction(model, newAnimation, null));

			ModelUtils.doForAnimFlags(model, animFlag -> copyFromInterval(animation, animation2, newAnimation, animFlag, undoActions));

			model.getEvents().forEach(e -> copyFromInterval(animation, animation2, newAnimation, e, undoActions));

			modelHandler.getUndoManager().pushAction(new CompoundAction(TextKey.BACK2BACK_ANIMATION.getTranslation(), undoActions, ModelStructureChangeListener.changeListener::animationParamsChanged).redo());
			JOptionPane.showMessageDialog(ProgramGlobals.getMainPanel(),
					"DONE! Made a combined animation called " + newAnimation.getName(), "Success",
					JOptionPane.PLAIN_MESSAGE);
		}
	}

	private static <Q> void copyFromInterval(Animation source1, Animation source2, Animation animation, AnimFlag<Q> af, List<UndoAction> undoActions) {
		if (!af.hasGlobalSeq()) {
			TreeMap<Integer, Entry<Q>> sequenceEntryMapCopy = af.getSequenceEntryMapCopy(source1);
			TreeMap<Integer, Entry<Q>> sequenceEntryMap2 = af.getEntryMap(source2);
			if (sequenceEntryMap2 != null) {
				if (sequenceEntryMapCopy == null) {
					sequenceEntryMapCopy = new TreeMap<>();
				}
				if (!sequenceEntryMap2.isEmpty()) {
					for (Integer time : sequenceEntryMap2.keySet()) {
						int newTime = time + source1.getLength();
						sequenceEntryMapCopy.put(newTime, sequenceEntryMap2.get(time).deepCopy().setTime(newTime));
					}
				}

			}
			if (sequenceEntryMapCopy != null) {
				undoActions.add(new AddFlagEntryMapAction<>(af, animation, sequenceEntryMapCopy, null));
			}
		}
	}

	private static void copyFromInterval(Animation source1, Animation source2, Animation animation, EventObject eventObj, List<UndoAction> undoActions) {
		if (!eventObj.hasGlobalSeq()) {
			TreeSet<Integer> eventTrack1 = eventObj.getEventTrack(source1);
			TreeSet<Integer> eventTrack2 = eventObj.getEventTrack(source2);
			if ((eventObj.hasSequence(source1)) || eventObj.hasSequence(source2)) {
				TreeSet<Integer> newEventTracks = new TreeSet<>();
				if (eventTrack1 != null) {
					newEventTracks.addAll(eventTrack1);
				}
				if (eventTrack2 != null) {
					for (Integer i : eventTrack2) {
						newEventTracks.add(i + source1.getLength());
					}
				}
				undoActions.add(new AddEventTrackAction(eventObj, animation, newEventTracks, null));
			}
		}
	}
}
