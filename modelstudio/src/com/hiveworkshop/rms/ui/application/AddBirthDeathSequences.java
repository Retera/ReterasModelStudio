package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.AddTimelineAction;
import com.hiveworkshop.rms.editor.actions.animation.TranslationKeyframeAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddBirthDeathSequences {

	public static void riseFallBirthActionRes() {
		final int confirmed = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(),
				"This will permanently alter model. Are you sure?", "Confirmation",
				JOptionPane.OK_CANCEL_OPTION);
		if (confirmed != JOptionPane.OK_OPTION) {
			return;
		}

		replaceOrUseOldAnimation("Birth", -300, 0);

		replaceOrUseOldAnimation("Death", 0, -300);

		JOptionPane.showMessageDialog(ProgramGlobals.getMainPanel(), "Done!");
    }

	private static void replaceOrUseOldAnimation(String name, int offsetStart, int offsetEnd) {
		final EditableModel model = ProgramGlobals.getCurrentModelPanel().getModelView().getModel();
		final Animation lastAnim = model.getAnim(model.getAnimsSize() - 1);

		Animation animation = getAnimationToUse(name, model, lastAnim);
		if (animation == null) return;
		setNewAnimation(model, animation, offsetStart, offsetEnd);

		final Animation stand = findAnimByName(model, "stand");
		if (stand != null) {
			setAnimationVisibilityFlag(model, stand, animation);
		}

		if (!animation.isNonLooping()) {
			animation.setNonLooping(true);
		}
		if (!model.contains(animation)) {
			model.add(animation);
		}
	}

	private static Animation getAnimationToUse(String name, EditableModel model, Animation lastAnim) {
		Animation oldAnimation = findAnimByName(model, name);
		Animation animation = new Animation(name, lastAnim.getEnd() + 300, lastAnim.getEnd() + 2300);

		boolean removeOldAnimation = false;
		if (oldAnimation != null) {
			String[] choices = {"Ignore", "Delete", "Overwrite"};
			Object x = JOptionPane.showInputDialog(ProgramGlobals.getMainPanel(),
					"Existing " + name.toLowerCase() + " detected. What should be done with it?", "Question",
					JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
			if (x == choices[1]) {
				removeOldAnimation = true;
			} else if (x == choices[2]) {
				animation = oldAnimation;
			} else {
				return null;
			}
		}
        if (removeOldAnimation) {
	        for (AnimFlag<?> af : ModelUtils.getAllAnimFlags(model)) {
		        if (((af.getTypeId() == 1) || (af.getTypeId() == 2) || (af.getTypeId() == 3))) {
			        af.deleteAnim(oldAnimation);
		        }
	        }
	        for (EventObject e : model.getEvents()) {
		        e.deleteAnim(oldAnimation);
	        }

	        model.remove(oldAnimation);
	        ModelStructureChangeListener.changeListener.animationParamsChanged();
        }
		return animation;
	}

	public static Animation findAnimByName(EditableModel model, String name) {
		for (final Animation anim : model.getAnims()) {
			if (anim.getName().toLowerCase().contains(name.toLowerCase())) {
				return anim;
			}
		}
		return null;
	}

	private static void setNewAnimation(EditableModel model, Animation animation, int startOffset, int endOffset) {
		Vec3 startVec = new Vec3(0, 0, startOffset);
		Vec3 endVec = new Vec3(0, 0, endOffset);
		model.add(animation);
		ModelStructureChangeListener.changeListener.animationParamsChanged();
		createKeyframes(model, animation, startVec, endVec);
	}

	private static List<IdObject> getRootObjects(EditableModel model) {
        final List<IdObject> roots = new ArrayList<>();
        for (final IdObject obj : model.getIdObjects()) {
            if (obj.getParent() == null) {
                roots.add(obj);
            }
        }
        return roots;
    }

    private static void addAnimationFlags(Animation animation, IdObject obj, int startOffset, int endOffset) {
        if (obj instanceof Bone) {
	        final Bone b = (Bone) obj;
	        Vec3AnimFlag translation = null;
	        boolean globalSeq = false;
            for (final AnimFlag<?> af : b.getAnimFlags()) {
                if (af.getTypeId() == AnimFlag.TRANSLATION) {
                    if (af.hasGlobalSeq()) {
	                    globalSeq = true;
	                    break;
                    } else {
	                    translation = (Vec3AnimFlag) af;
                    }
                }
            }
	        if (globalSeq) {
		        return;
	        }
	        if (translation == null) {
		        translation = new Vec3AnimFlag("Translation");
		        translation.setInterpType(InterpolationType.LINEAR);
		        b.add(translation);
	        }
	        translation.addEntry(0, new Vec3(0, 0, startOffset), animation);
	        translation.addEntry(animation.getLength(), new Vec3(0, 0, endOffset), animation);
        }
    }

    private static void setAnimationVisibilityFlag(EditableModel model, Animation stand, Animation animation) {
//        for (final VisibilitySource source : model.getAllVisibilitySources()) {
	    for (final VisibilitySource source : ModelUtils.getAllVis(model)) {
		    final FloatAnimFlag dummy = new FloatAnimFlag("dummy");
		    final AnimFlag<?> af = source.getVisibilityFlag();
		    AnimFlagUtils.copyFrom(dummy, af);
		    af.deleteAnim(animation);
		    AnimFlagUtils.copyFrom(af, dummy, stand, animation);
	    }
    }

	// most of the code below are modified versions of code from AnimatedNode and NodeAnimationModelEditor
	public static void createKeyframes(EditableModel model, Animation animation, Vec3 startVec, Vec3 endVec) {
		RenderModel renderModel = ProgramGlobals.getCurrentModelPanel().getEditorRenderModel();
		ModelStructureChangeListener structureChangeListener = ModelStructureChangeListener.changeListener;

		final Set<IdObject> selection = new HashSet<>(getRootObjects(model));
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from a TimeEnvironmentImpl render environment, and never from the anim previewer impl

		final TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getTimeEnvironment();
		timeEnvironmentImpl.setAnimationTime(0);

		List<UndoAction> actions1 = generateKeyframes(0, selection, timeEnvironmentImpl, "Translation", startVec);
		TranslationKeyframeAction setup1 = new TranslationKeyframeAction(new CompoundAction("setup", actions1, structureChangeListener::keyframesUpdated), selection, renderModel);

		timeEnvironmentImpl.setAnimationTime(animation.getLength());
		List<UndoAction> actions2 = generateKeyframes(animation.getLength(), selection, timeEnvironmentImpl, "Translation", endVec);
		TranslationKeyframeAction setup2 = new TranslationKeyframeAction(new CompoundAction("setup", actions2, structureChangeListener::keyframesUpdated), selection, renderModel);
	}

	//
	public static List<UndoAction> generateKeyframes(int time, Set<IdObject> selection, TimeEnvironmentImpl timeEnvironmentImpl, String name, Vec3 vec3) {
		List<UndoAction> actions = new ArrayList<>();
		for (final IdObject node : selection) {
			Vec3AnimFlag timeline = (Vec3AnimFlag) node.find(name, timeEnvironmentImpl.getGlobalSeq());

			if (timeline == null) {
				timeline = new Vec3AnimFlag(name, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());

				final UndoAction addTimelineAction = new AddTimelineAction(node, timeline);
				actions.add(addTimelineAction);
			}

			final UndoAction keyframeAction = getAddKeyframeAction(timeline, time, vec3, timeEnvironmentImpl.getCurrentSequence());
			if (keyframeAction != null) {
				actions.add(keyframeAction.redo());
			}
		}
		return actions;
	}


	private static UndoAction getAddKeyframeAction(Vec3AnimFlag timeline, int trackTime, Vec3 vec3, Sequence animation) {
		if (timeline.hasEntryAt(animation, trackTime)) {
			Entry<Vec3> entry = new Entry<>(trackTime, vec3);

			if (timeline.getInterpolationType().tangential()) {
				entry.unLinearize();
			}

			return new AddFlagEntryAction<>(timeline, entry, animation, null);
		}
		return null;
	}

}
