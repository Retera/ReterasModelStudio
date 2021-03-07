package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.NodeAnimationModelEditor;
import com.hiveworkshop.rms.ui.application.edit.animation.NodeAnimationSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation.AddKeyframeAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation.AddTimelineAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation.TranslationKeyframeAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.util.*;

public class AddBirthDeathSequences {

    static void riseFallBirthActionRes(MainPanel mainPanel) {
        final int confirmed = JOptionPane.showConfirmDialog(mainPanel,
                "This will permanently alter model. Are you sure?", "Confirmation",
                JOptionPane.OK_CANCEL_OPTION);
        if (confirmed != JOptionPane.OK_OPTION) {
            return;
        }

        replaceOrUseOldAnimation(mainPanel, "Birth", -300, 0);

        replaceOrUseOldAnimation(mainPanel, "Death", 0, -300);

        JOptionPane.showMessageDialog(mainPanel, "Done!");
    }

    private static void replaceOrUseOldAnimation(MainPanel mainPanel, String name, int offsetStart, int offsetEnd) {
        final EditableModel model = mainPanel.currentModelPanel().getModelViewManager().getModel();
        final Animation lastAnim = model.getAnim(model.getAnimsSize() - 1);

        Animation animation = getAnimationToUse(mainPanel, name, model, lastAnim);
        if (animation == null) return;
        setNewAnimation(model, animation, mainPanel, offsetStart, offsetEnd);

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

    private static Animation getAnimationToUse(MainPanel mainPanel, String name, EditableModel model, Animation lastAnim) {
        final Animation oldAnimation = model.findAnimByName(name);
        Animation animation = new Animation(name, lastAnim.getEnd() + 300, lastAnim.getEnd() + 2300);

        boolean removeOldAnimation = false;
        if (oldAnimation != null) {
            final String[] choices = {"Ignore", "Delete", "Overwrite"};
            final Object x = JOptionPane.showInputDialog(mainPanel,
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
            mainPanel.modelStructureChangeListener.animationsRemoved(Collections.singletonList(oldAnimation));
        }
        return animation;
    }

    private static void setNewAnimation(EditableModel model, Animation animation, MainPanel mainPanel, int startOffset, int endOffset) {
        Vec3 startVec = new Vec3(0, 0, startOffset);
        Vec3 endVec = new Vec3(0, 0, endOffset);
        model.add(animation);
        mainPanel.modelStructureChangeListener.animationsAdded(Collections.singletonList(animation));
        createKeyframes(mainPanel, model, animation.getStart(), animation.getEnd(), startVec, endVec);
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
                final List<Integer> times = new ArrayList<>();
                final List<Vec3> values = new ArrayList<>();
                trans = new Vec3AnimFlag("Translation", times, values);
                trans.setInterpType(InterpolationType.LINEAR);
//                trans = AnimFlag.createEmpty2018(name, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
                System.out.println(trans + " " + trans.getTimes() + " " + trans.getValues() + " " + trans.getInterpolationType());
                b.add(trans);
//                b.getAnimFlags().add(trans);
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
    public static void createKeyframes(MainPanel mainPanel, EditableModel model, int trackTime1, int trackTime2, Vec3 startVec, Vec3 endVec) {
        RenderModel renderModel = mainPanel.currentModelPanel.getEditorRenderModel();
        ModelStructureChangeListener structureChangeListener = ModelStructureChangeListenerImplementation.getModelStructureChangeListener(mainPanel);
        NodeAnimationSelectionManager nodeAnimationSelectionManager = new NodeAnimationSelectionManager(renderModel);

        final NodeAnimationModelEditor nodeAnimationModelEditor = new NodeAnimationModelEditor(mainPanel.currentModelPanel.getModelViewManager(),
                mainPanel.prefs, nodeAnimationSelectionManager, renderModel, structureChangeListener);

        final Set<IdObject> selection = new HashSet<>(getRootObjects(model));
        final List<UndoAction> actions = new ArrayList<>();
        // TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from a TimeEnvironmentImpl render environment, and never from the anim previewer impl

        final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();

        generateKeyframes(trackTime1, selection, actions, timeEnvironmentImpl, "Translation", structureChangeListener, renderModel, startVec);
        new TranslationKeyframeAction(new CompoundAction("setup", actions), trackTime1, timeEnvironmentImpl.getGlobalSeq(), selection, nodeAnimationModelEditor);

        generateKeyframes(trackTime2, selection, actions, timeEnvironmentImpl, "Translation", structureChangeListener, renderModel, endVec);
        new TranslationKeyframeAction(new CompoundAction("setup", actions), trackTime2, timeEnvironmentImpl.getGlobalSeq(), selection, nodeAnimationModelEditor);
    }

    //
    public static void generateKeyframes(int time, Set<IdObject> selection, List<UndoAction> actions, TimeEnvironmentImpl timeEnvironmentImpl, String name, ModelStructureChangeListener structureChangeListener, RenderModel renderModel, Vec3 vec3) {
        for (final IdObject node : selection) {
            Vec3AnimFlag translationTimeline = (Vec3AnimFlag) node.find(name, timeEnvironmentImpl.getGlobalSeq());

            if (translationTimeline == null) {
                translationTimeline = Vec3AnimFlag.createEmpty2018(name, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
                node.add(translationTimeline);

                final AddTimelineAction addTimelineAction = new AddTimelineAction(node, translationTimeline, structureChangeListener);
                structureChangeListener.timelineAdded(node, translationTimeline);
                actions.add(addTimelineAction);
            }

            final AddKeyframeAction keyframeAction = createTranslationKeyframe2(time, node, renderModel, translationTimeline, structureChangeListener, vec3);
            if (keyframeAction != null) {
                actions.add(keyframeAction);
            }
        }
    }

    public static AddKeyframeAction createTranslationKeyframe2(int trackTime, IdObject idObject, RenderModel renderModel, Vec3AnimFlag translationFlag,
                                                               ModelStructureChangeListener structureChangeListener, Vec3 vec3) {
        // TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
        final int floorIndex = translationFlag.floorIndex(trackTime);

        if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex).equals(trackTime))) {
            return null;
        } else {
            return getAddKeyframeAction(idObject, translationFlag, structureChangeListener, trackTime, floorIndex, vec3);
        }
    }

    private static AddKeyframeAction getAddKeyframeAction(IdObject idObject, Vec3AnimFlag timeline, ModelStructureChangeListener structureChangeListener, int trackTime, int floorIndex, Vec3 vec3) {
        final int insertIndex = floorIndex + 1;
        timeline.getTimes().add(insertIndex, trackTime);

        final Vec3 keyframeValue = new Vec3(vec3);
        timeline.getValues().add(insertIndex, keyframeValue);

        if (timeline.getInterpolationType().tangential()) {
            final Vec3 inTan = new Vec3(vec3);
            timeline.getInTans().add(insertIndex, inTan);

            final Vec3 outTan = new Vec3(vec3);
            timeline.getOutTans().add(insertIndex, outTan);

            structureChangeListener.keyframeAdded(idObject, timeline, trackTime);
            return new AddKeyframeAction(idObject, timeline, trackTime, keyframeValue, inTan, outTan, structureChangeListener);
        } else {
            structureChangeListener.keyframeAdded(idObject, timeline, trackTime);
            return new AddKeyframeAction(idObject, timeline, trackTime, keyframeValue, structureChangeListener);
        }
    }

}
