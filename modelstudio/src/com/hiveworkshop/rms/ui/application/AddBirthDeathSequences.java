package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.AddSequenceAction;
import com.hiveworkshop.rms.editor.actions.animation.RemoveSequenceAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddAnimFlagAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.SetFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.SetFlagEntryMapAction;
import com.hiveworkshop.rms.editor.actions.util.BoolAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public class AddBirthDeathSequences {

	private final List<UndoAction> undoActions = new ArrayList<>();
	private final EditableModel model;

	public AddBirthDeathSequences(ModelHandler modelHandler, float height) {
		model = modelHandler.getModel();

		Animation[] animationsToUse = getAnimationsToUse();
		Animation visibility = findAnimByName(model.getAnims(), "stand");

		List<IdObject> topLevelNodes = model.getIdObjects().stream().filter(n -> n.getParent() == null).toList();
		Animation birthAnimation = animationsToUse[0];
		Animation deathAnimation = animationsToUse[1];
		if (birthAnimation != null || deathAnimation != null) {
			Map<IdObject, Vec3AnimFlag> timelineMap = getTimelineMap(topLevelNodes);
			for (IdObject node : timelineMap.keySet()) {
				Vec3AnimFlag vec3AnimFlag = timelineMap.get(node);
				if (vec3AnimFlag != null && !node.owns(vec3AnimFlag)) {
					undoActions.add(new AddAnimFlagAction<>(node, vec3AnimFlag, null));
				}
			}

			if (birthAnimation != null) {
				replaceOrUseOldAnimation(birthAnimation, visibility, timelineMap, height, 0);
			}
			if (deathAnimation != null) {
				replaceOrUseOldAnimation(deathAnimation, visibility, timelineMap, 0, height);
			}
		}

		if (!undoActions.isEmpty()) {
			ModelStructureChangeListener structureChangeListener = ModelStructureChangeListener.changeListener;
			modelHandler.getUndoManager().pushAction(new CompoundAction("Add Birth/Death Sequences", undoActions, structureChangeListener::animationParamsChanged).redo());

			List<String> collect = Arrays.stream(animationsToUse).filter(Objects::nonNull).map(Animation::getName).collect(Collectors.toList());

			String message = "Added \"" + String.join("\" and \"", collect) + "\"";

			JOptionPane.showMessageDialog(ProgramGlobals.getMainPanel(), message, "Done", JOptionPane.INFORMATION_MESSAGE);
		} else {

			JOptionPane.showMessageDialog(ProgramGlobals.getMainPanel(), "Nothing added", "Done", JOptionPane.INFORMATION_MESSAGE);
		}

	}

	public static void riseFallBirthActionRes() {
		new AddBirthDeathSequences(ProgramGlobals.getCurrentModelPanel().getModelHandler(),-300);
	}

	private void replaceOrUseOldAnimation(Animation animation, Animation visibility, Map<IdObject, Vec3AnimFlag> timelineMap, float startHeight, float endHeight) {
		if (!model.contains(animation)) {
			undoActions.add(new AddSequenceAction(model, animation, null));
		}
		if (!animation.isNonLooping()) {
			undoActions.add(new BoolAction(animation::setNonLooping, true, "set nonLooping", null));
		}

		undoActions.addAll(generateKeyframes(animation, timelineMap, new Vec3(0, 0, startHeight), new Vec3(0, 0, endHeight)));

		if (visibility != null) {
			undoActions.addAll(getVisActions(animation, visibility));
		}
	}

	public Animation findAnimByName(List<Animation> anims, String name) {
		String lowerCase = name.toLowerCase();
		Animation bestMatch = null;
		for (Animation anim : anims) {
			String animLowerCase = anim.getName().toLowerCase();
			if (animLowerCase.equals(lowerCase)) {
				return anim;
			} else if (animLowerCase.contains(lowerCase) && bestMatch == null
					|| animLowerCase.startsWith(lowerCase)
					&& animLowerCase.length() < bestMatch.getName().length()) {
				bestMatch = anim;
			}
		}
		return bestMatch;
	}

	private List<IdObject> getTopLevelNodes() {
        List<IdObject> topLevelNodes = new ArrayList<>();
        for (IdObject obj : model.getIdObjects()) {
            if (obj.getParent() == null) {
                topLevelNodes.add(obj);
            }
        }
        return topLevelNodes;
    }

	public Map<IdObject, Vec3AnimFlag> getTimelineMap(Collection<IdObject> selection) {
		Map<IdObject, Vec3AnimFlag> timeLineMap = new HashMap<>();

		for (final IdObject node : selection) {
			Vec3AnimFlag timeline = (Vec3AnimFlag) node.find(MdlUtils.TOKEN_TRANSLATION);

			if (timeline == null) {
				Vec3AnimFlag animFlag = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION);
				animFlag.setInterpType(InterpolationType.LINEAR);
				timeLineMap.put(node, animFlag);
			} else if (!timeline.hasGlobalSeq()) {
				timeLineMap.put(node, timeline);
			}

		}
		return timeLineMap;
	}
	public List<UndoAction> generateKeyframes(Sequence animation, Map<IdObject, Vec3AnimFlag> timelineMap, Vec3 startVec, Vec3 endVec) {
		List<UndoAction> actions = new ArrayList<>();

		for (final IdObject node : timelineMap.keySet()) {
			Vec3AnimFlag timeline = timelineMap.get(node);
			if (timeline != null) {
				actions.add(getAddKeyframeAction(timeline, startVec, endVec, animation));
			}

		}
		return actions;
	}

	private UndoAction getAddKeyframeAction(Vec3AnimFlag timeline, Vec3 startVec, Vec3 endVec, Sequence animation) {
		Entry<Vec3> entry1 = new Entry<>(0, new Vec3(startVec).add(timeline.interpolateAt(animation, 0)));
		if (timeline.tans()) {
			entry1.unLinearize();
		}
		Entry<Vec3> entry2 = new Entry<>(animation.getLength(), new Vec3(endVec).add(timeline.interpolateAt(animation, animation.getLength())));
		if (timeline.tans()) {
			entry2.unLinearize();
		}

		ArrayList<Entry<Vec3>> entries = new ArrayList<>();
		entries.add(entry1);
		entries.add(entry2);
		return new SetFlagEntryAction<>(timeline, entries, animation, null);
	}

	private List<UndoAction> getVisActions(Animation animation, Animation visibility) {
		List<UndoAction> visActions = new ArrayList<>();
		for (TimelineContainer source : ModelUtils.getAllVis(model)) {
			AnimFlag<Float> visibilityFlag = source.getVisibilityFlag();
			if (visibilityFlag != null && visibilityFlag.getEntryMap(visibility) != null) {
				TreeMap<Integer, Entry<Float>> sequenceEntryMapCopy = visibilityFlag.getSequenceEntryMapCopy(visibility);
				double ratio = animation.getLength() / (double) visibility.getLength();
				AnimFlagUtils.scaleMapEntries(ratio, sequenceEntryMapCopy);
				visActions.add(new SetFlagEntryMapAction<>(visibilityFlag, animation, sequenceEntryMapCopy, null));
			}
		}
		return visActions;
	}

	private Animation[] getAnimationsToUse() {

		JPanel panel = new JPanel(new MigLayout("", "", "[][]15[][]"));

		AnimationHandeler birthHandler = new AnimationHandeler(findAnimByName(model.getAnims(), "Birth"), "Birth");
		AnimationHandeler deathHandler = new AnimationHandeler(findAnimByName(model.getAnims(), "Death"), "Death");

		if (birthHandler.shouldShow()) panel.add(getOptionPanel(birthHandler), "wrap");
		if (deathHandler.shouldShow()) panel.add(getOptionPanel(deathHandler), "wrap");

		if (panel.getComponentCount() == 0 || JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), panel, "Found Existing", JOptionPane.OK_CANCEL_OPTION)) {
			if (birthHandler.shouldDelete()) undoActions.add(new RemoveSequenceAction(model, birthHandler.getOldAnimation(), null));
			if (deathHandler.shouldDelete()) undoActions.add(new RemoveSequenceAction(model, deathHandler.getOldAnimation(), null));

			return new Animation[]{birthHandler.getAnimation(), deathHandler.getAnimation()};
		}
		return new Animation[] {null, null};
	}

	private JPanel getOptionPanel(AnimationHandeler handler) {
		JPanel panel = new JPanel(new MigLayout());
		panel.add(new JLabel("Existing \"" + handler.animName + "\" detected. What should be done?"), "wrap");
		TwiComboBox<Options> comp = new TwiComboBox<>(Options.values(), Options.ON_TOP)
				.addOnSelectItemListener(opt -> handler.opt = opt)
				.setStringFunctionRender((opt) -> ((Options) opt).getOptionString(handler.animName));
		panel.add(comp, "growx, wrap");
		return panel;
	}

	private static class AnimationHandeler {
		Options opt = Options.SKIPP;
		Animation animation;
		String animName;
		AnimationHandeler(Animation animation, String animName) {
			this.animation = animation;
			this.animName = animName;
		}

		public Animation getAnimation() {
			if (animation != null && opt == Options.ON_TOP) {
				return animation;
			} else if (animation == null || opt != Options.SKIPP) {
				return new Animation(animName, 0, 2300);
			}
			return null;
		}
		boolean shouldDelete() {
			return opt == Options.DELETE;
		}

		public Animation getOldAnimation() {
			return animation;
		}

		boolean shouldShow() {
			return animation != null;
		}
	}

	final static String REPLACESTRING = "REPLACESTRING";
	enum Options {
		SKIPP("Keep existing \"" + REPLACESTRING + "\" and don't add new \"" + REPLACESTRING + "\""),
		//		NOTHING("Nothing"),
		NOTHING("Keep existing \"" + REPLACESTRING + "\" and add new \"" + REPLACESTRING + "\""),
		DELETE("Replace existing \"" + REPLACESTRING + "\""),
		ON_TOP("Add new keyframes on top of existing \"" + REPLACESTRING + "\"");
		final String name;
		Options(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String getOptionString(String animName) {
			return name.replaceAll(REPLACESTRING, animName);
		}

		@Override
		public String toString() {
			return name;
		}
	}

}
