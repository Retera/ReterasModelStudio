package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.AddKeyframeAction;
import com.hiveworkshop.rms.editor.actions.animation.AddTimelineAction;
import com.hiveworkshop.rms.editor.actions.animation.TranslationKeyframeAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
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

		final Animation stand = model.findAnimByName("stand");
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
		Animation oldAnimation = model.findAnimByName(name);
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
	        // del keys
	        oldAnimation.clearData(model.getAllAnimFlags(), model.getEvents());
	        model.remove(oldAnimation);
	        ModelStructureChangeListener.changeListener.animationParamsChanged();
        }
        return animation;
    }

	private static void setNewAnimation(EditableModel model, Animation animation, int startOffset, int endOffset) {
		Vec3 startVec = new Vec3(0, 0, startOffset);
		Vec3 endVec = new Vec3(0, 0, endOffset);
		model.add(animation);
		ModelStructureChangeListener.changeListener.animationParamsChanged();
		createKeyframes(model, animation.getStart(), animation.getEnd(), startVec, endVec);
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
            Vec3AnimFlag trans = null;
            boolean globalSeq = false;
            for (final AnimFlag<?> af : b.getAnimFlags()) {
                if (af.getTypeId() == AnimFlag.TRANSLATION) {
                    if (af.hasGlobalSeq()) {
                        globalSeq = true;
                        break;
                    } else {
                        trans = (Vec3AnimFlag) af;
                    }
                }
            }
            if (globalSeq) {
                return;
            }
            if (trans == null) {
                trans = new Vec3AnimFlag("Translation");
                trans.setInterpType(InterpolationType.LINEAR);
                b.add(trans);
            }
            trans.addEntry(animation.getStart(), new Vec3(0, 0, startOffset));
            trans.addEntry(animation.getEnd(), new Vec3(0, 0, endOffset));
        }
    }

    private static void setAnimationVisibilityFlag(EditableModel model, Animation stand, Animation animation) {
//        for (final VisibilitySource source : model.getAllVisibilitySources()) {
	    for (final VisibilitySource source : model.getAllVis()) {
		    final FloatAnimFlag dummy = new FloatAnimFlag("dummy");
		    final AnimFlag<?> af = source.getVisibilityFlag();
		    dummy.copyFrom(af);
		    af.deleteAnim(animation);
		    af.copyFrom(dummy, stand.getStart(), stand.getEnd(), animation.getStart(), animation.getEnd());
	    }
    }

	// most of the code below are modified versions of code from AnimatedNode and NodeAnimationModelEditor
	public static void createKeyframes(EditableModel model, int trackTime1, int trackTime2, Vec3 startVec, Vec3 endVec) {
		RenderModel renderModel = ProgramGlobals.getCurrentModelPanel().getEditorRenderModel();
		ModelStructureChangeListener structureChangeListener = ModelStructureChangeListener.changeListener;

		final Set<IdObject> selection = new HashSet<>(getRootObjects(model));
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from a TimeEnvironmentImpl render environment, and never from the anim previewer impl

		final TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getTimeEnvironment();
		timeEnvironmentImpl.setAnimationTime(trackTime1);

		List<UndoAction> actions1 = generateKeyframes(trackTime1, selection, timeEnvironmentImpl, "Translation", renderModel, startVec);
		TranslationKeyframeAction setup1 = new TranslationKeyframeAction(new CompoundAction("setup", actions1, structureChangeListener::keyframesUpdated), selection, renderModel);

		timeEnvironmentImpl.setAnimationTime(trackTime2);
		List<UndoAction> actions2 = generateKeyframes(trackTime2, selection, timeEnvironmentImpl, "Translation", renderModel, endVec);
		TranslationKeyframeAction setup2 = new TranslationKeyframeAction(new CompoundAction("setup", actions2, structureChangeListener::keyframesUpdated), selection, renderModel);
	}

	//
	public static List<UndoAction> generateKeyframes(int time, Set<IdObject> selection, TimeEnvironmentImpl timeEnvironmentImpl, String name, RenderModel renderModel, Vec3 vec3) {
		List<UndoAction> actions = new ArrayList<>();
		for (final IdObject node : selection) {
			Vec3AnimFlag timeline = (Vec3AnimFlag) node.find(name, timeEnvironmentImpl.getGlobalSeq());

			if (timeline == null) {
				timeline = new Vec3AnimFlag(name, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());

				final AddTimelineAction addTimelineAction = new AddTimelineAction(node, timeline);
				actions.add(addTimelineAction);
			}

			final AddKeyframeAction keyframeAction = getAddKeyframeAction(timeline, time, vec3);
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}
		return actions;
	}


	private static AddKeyframeAction getAddKeyframeAction(Vec3AnimFlag timeline, int trackTime, Vec3 vec3) {
		if (timeline.hasEntryAt(trackTime)) {
			Entry<Vec3> entry = new Entry<>(trackTime, vec3);

			if (timeline.getInterpolationType().tangential()) {
				entry.unLinearize();
			}

			AddKeyframeAction addKeyframeAction = new AddKeyframeAction(timeline, entry);
			addKeyframeAction.redo();
			return addKeyframeAction;
		}
		return null;
    }

}
