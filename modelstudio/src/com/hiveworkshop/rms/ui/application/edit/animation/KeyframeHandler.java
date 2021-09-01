package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.SetKeyframeAction_T;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class KeyframeHandler {
	private static final Color GLASS_TICK_COVER_COLOR = new Color(100, 190, 255, 100);
	private static final Color GLASS_TICK_COVER_BORDER_COLOR = new Color(0, 80, 255, 220);
	private static final int SLIDER_SIDE_BUTTON_SIZE = 15;
	private static final int SLIDING_TIME_CHOOSER_WIDTH = 50 + (SLIDER_SIDE_BUTTON_SIZE * 2);
	private static final int VERTICAL_TICKS_HEIGHT = 10;
	private static final int VERTICAL_SLIDER_HEIGHT = 15;
	private static final int PLAY_BUTTON_SIZE = 30;
	private static final Dimension PLAY_BUTTON_DIMENSION = new Dimension(PLAY_BUTTON_SIZE, PLAY_BUTTON_SIZE);
	private static final int SIDE_OFFSETS = SLIDING_TIME_CHOOSER_WIDTH / 2;
	private static final Stroke WIDTH_2_STROKE = new BasicStroke(2);
	private static final Stroke WIDTH_1_STROKE = new BasicStroke(1);

	private final TreeMap<Integer, KeyFrame> timeToKey = new TreeMap<>();
	private final List<CopiedKeyFrame<?>> copiedKeyframes = new ArrayList<>();
	private TimeEnvironmentImpl timeEnvironment;
	JPanel timelinePanel;
	private ModelHandler modelHandler;
	private UndoManager undoManager;
	private ModelStructureChangeListener structureChangeListener;

	private TimeSliderTimeListener notifier;

	boolean useAllKFs = false;

	private boolean useAllCopiedKeyframes = false;

	public KeyframeHandler(TimeSliderTimeListener notifier, JPanel timelinePanel){
		this.notifier = notifier;
		this.timelinePanel = timelinePanel;

		structureChangeListener = ModelStructureChangeListener.changeListener;
	}

	public KeyframeHandler setModelHandler(ModelHandler modelHandler){
		this.modelHandler = modelHandler;
		if(modelHandler != null){
			undoManager = modelHandler.getUndoManager();
			timeEnvironment = modelHandler.getEditTimeEnv();
		} else {
			undoManager = null;
//			timeEnvironment = null;
		}
		return this;
	}

	public void revalidateKeyframeDisplay() {
		updateKeyframeDisplay();
//		repaint();
	}

	public Collection<IdObject> getSelectionToUse() {
		if ((modelHandler == null) || (modelHandler.getModel() == null)) {
			return Collections.emptySet();
		}
		return useAllKFs ? modelHandler.getModel().getIdObjects() : modelHandler.getModelView().getSelectedIdObjects();
	}

	public void updateKeyframeDisplay() {
		timeToKey.clear();

		Iterable<IdObject> selection = getSelectionToUse();
		for (IdObject object : selection) {
			for (AnimFlag<?> flag : object.getAnimFlags()) {
				if ((flag.getGlobalSeq() == null && timeEnvironment.getGlobalSeq() == null)
						|| (timeEnvironment.getGlobalSeq() != null && timeEnvironment.getGlobalSeq().equals(flag.getGlobalSeq()))) {
					if (flag.size() > 0) {
						TreeMap<Integer, ? extends Entry<?>> entryMap = flag.getEntryMap(timeEnvironment.getCurrentSequence());
						for (Integer time : entryMap.keySet()) {
							KeyFrame keyFrame = timeToKey.computeIfAbsent(time, k -> new KeyFrame(time));
							keyFrame.objects.add(object);
							keyFrame.timelines.add(flag);
						}
					}
				}
			}
		}
	}

	public void cutSpecificItem(Integer time, IdObject object, AnimFlag<?> flag) {
		copyKeyframes(object, flag, time);
		deleteKeyframe(flag, time);
	}

	public void cutItem(Integer time) {
		copyKeyframes(time);
		deleteKeyframes("cut keyframe", time, timeToKey.get(time).objects);
	}

	public void deleteSelectedKeyframes() {
		KeyFrame keyFrame = timeToKey.get(timeEnvironment.getEnvTrackTime());
		if (keyFrame != null) {
			deleteKeyframes("delete keyframe", timeEnvironment.getEnvTrackTime(), keyFrame.objects);
		}
		revalidateKeyframeDisplay();
	}

	public void deleteKeyframes(String actionName, int trackTime, Collection<IdObject> objects) {
		List<UndoAction> actions = new ArrayList<>();
		for (IdObject object : objects) {
			for (AnimFlag<?> flag : object.getAnimFlags()) {
				Sequence currentSequence = timeEnvironment.getCurrentSequence();
				if (flag.getEntryMap(currentSequence).containsKey(trackTime)) {
					actions.add(new RemoveFlagEntryAction<>(flag, trackTime, currentSequence, null));
				}
			}
		}
		// TODO build one action for performance, so that the structure change notifier is not called N times, where N is the number of selected timelines
		CompoundAction action = new CompoundAction(actionName, actions, () -> structureChangeListener.keyframesUpdated());
		undoManager.pushAction(action.redo());
	}
	public void deleteKeyframe(AnimFlag<?> flag, int trackTime) {
		Sequence currentSequence = timeEnvironment.getCurrentSequence();
		if (flag.getEntryMap(currentSequence).containsKey(trackTime)) {
			undoManager.pushAction(new RemoveFlagEntryAction<>(flag, trackTime, currentSequence, structureChangeListener).redo());
		}
	}

	public void copyKeyframes(int trackTime) {
		copiedKeyframes.clear();
		useAllCopiedKeyframes = false;
		for (IdObject object : getSelectionToUse()) {
			for (AnimFlag<?> flag : object.getAnimFlags()) {
				GlobalSeq currentEditorGlobalSeq = timeEnvironment.getGlobalSeq();
				if ((flag.getGlobalSeq() == null && currentEditorGlobalSeq == null)
						|| (currentEditorGlobalSeq != null && currentEditorGlobalSeq == flag.getGlobalSeq())) {
					copuKeyframes(object, flag, trackTime);
				}
			}
		}
	}

	public void copyKeyframes(IdObject object, AnimFlag<?> flag, int trackTime) {
		copiedKeyframes.clear();
		useAllCopiedKeyframes = false;
		copuKeyframes(object, flag, trackTime);
	}

	private <Q> void copuKeyframes(IdObject object, AnimFlag<Q> flag, int trackTime) {
		if (flag.getEntryMap(timeEnvironment.getCurrentSequence()).containsKey(trackTime)) {
			copiedKeyframes.add(new CopiedKeyFrame<>(object, flag, flag.getEntryAt(timeEnvironment.getCurrentSequence(), trackTime).deepCopy()));
		} else {
			Entry<Q> entry = new Entry<>(trackTime, flag.interpolateAt(timeEnvironment));

			if (flag.tans()) {
				Entry<Q> entryIn = flag.getFloorEntry(trackTime, timeEnvironment.getCurrentSequence());
				Entry<Q> entryOut = flag.getCeilEntry(trackTime, timeEnvironment.getCurrentSequence());
				int animationLength = timeEnvironment.getCurrentSequence().getLength();
//				float factor = getTimeFactor(trackTime, animationLength, entryIn.time, entryOut.time);
				float[] tbcFactor = flag.getTbcFactor(0, 0.5f, 0);
				flag.calcNewTans(tbcFactor, entryOut, entryIn, entry, animationLength);
				System.out.println("calc tans! " + entryIn + entryOut + entry);
			}
			copiedKeyframes.add(new CopiedKeyFrame<>(object, flag, entry));
		}
	}

	// to be called externally
	public void copy() {
		copyKeyframes(timeEnvironment.getEnvTrackTime());
	}

	public void cut() {
		copyKeyframes(timeEnvironment.getEnvTrackTime());
		final KeyFrame keyFrame = timeToKey.get(timeEnvironment.getEnvTrackTime());
		if (keyFrame != null) {
			deleteKeyframes("cut keyframe", timeEnvironment.getEnvTrackTime(), keyFrame.objects);
		}
		revalidateKeyframeDisplay();
	}

	public void paste() {
		pasteToAllSelected(timeEnvironment.getEnvTrackTime());
	}

	public void pasteToAllSelected(int trackTime) {
		List<UndoAction> actions = new ArrayList<>();
		for (CopiedKeyFrame<?> frame : copiedKeyframes) {
			if (getSelectionToUse().contains(frame.node) || useAllCopiedKeyframes) {
				actions.add(getUndoAction(trackTime, frame));
			}
		}
		undoManager.pushAction(new CompoundAction("paste keyframe", actions, structureChangeListener::keyframesUpdated).redo());
		revalidateKeyframeDisplay();
	}

	public void pasteToSpecificTimeline(Integer time, AnimFlag<?> flag) {
		boolean foundCopiedMatch = false;
		int mouseClickAnimationTime = time;// computeTimeFromX(e.getX());
		for (CopiedKeyFrame<?> frame : copiedKeyframes) {
			if (frame.sourceTimeline == flag) {
				// only paste to selected nodes
				undoManager.pushAction(getUndoAction(mouseClickAnimationTime, frame).redo());
				foundCopiedMatch = true;
				break;
			}
		}
		if (!foundCopiedMatch) {
			JOptionPane.showMessageDialog(timelinePanel,
					"Tell Retera to code in the ability to paste cross-node data!");
		}
		revalidateKeyframeDisplay();
	}

	public void copyAllKeyframes(int trackTime) {
		copiedKeyframes.clear();
		useAllCopiedKeyframes = true;
		for (IdObject object : modelHandler.getModel().getIdObjects()) {
			for (AnimFlag<?> flag : object.getAnimFlags()) {
				GlobalSeq currentEditorGlobalSeq = timeEnvironment.getGlobalSeq();
				if ((flag.getGlobalSeq() == null && currentEditorGlobalSeq == null)
						|| (currentEditorGlobalSeq != null && currentEditorGlobalSeq == flag.getGlobalSeq())) {
					copuKeyframes(object, flag, trackTime);
				}
			}
		}
	}

	private <T> UndoAction getUndoAction(int mouseClickAnimationTime, CopiedKeyFrame<T> frame) {
		// only paste to selected nodes
		AnimFlag<T> sourceTimeline = frame.sourceTimeline;
		Entry<T> newEntry = frame.entry.deepCopy();
		if (sourceTimeline.hasEntryAt(timeEnvironment.getCurrentSequence(), mouseClickAnimationTime)) {
			newEntry.setTime(mouseClickAnimationTime);
			return new SetKeyframeAction_T<>(sourceTimeline, newEntry, timeEnvironment.getCurrentSequence(), () -> {
				// TODO this is a hack to refresh screen while dragging
				notifier.timeChanged(timeEnvironment.getEnvTrackTime());
			});
		} else {
			newEntry.setTime(mouseClickAnimationTime);
			return new AddFlagEntryAction<>(sourceTimeline, newEntry, timeEnvironment.getCurrentSequence(), null);
		}
	}

	private int computeXFromTime(int time) {
		int widthMinusOffsets = timelinePanel.getWidth() - (SIDE_OFFSETS * 2);
		double timeRatio = (time) / (double) (timeEnvironment.getLength());
//		System.out.println("new x: " + ((widthMinusOffsets * timeRatio) + (SIDE_OFFSETS)) + " for time " + time);
		return (int) (widthMinusOffsets * timeRatio) + (SIDE_OFFSETS);
	}

	private static final class CopiedKeyFrame<T> {
		private final TimelineContainer node;
		private final AnimFlag<T> sourceTimeline;
		private final Entry<T> entry;

		public CopiedKeyFrame(TimelineContainer node, AnimFlag<T> sourceTimeline, T value, T inTan, T outTan) {
			this.node = node;
			this.sourceTimeline = sourceTimeline;
			entry = new Entry<>(0, value, inTan, outTan);
		}

		public CopiedKeyFrame(TimelineContainer node, AnimFlag<T> sourceTimeline, Entry<T> entry) {
			this.node = node;
			this.sourceTimeline = sourceTimeline;
			this.entry = entry;
		}
	}

	public final class KeyFrame {
		private int time;
		private final Set<IdObject> objects = new HashSet<>();
		private final List<AnimFlag<?>> timelines = new ArrayList<>();
		private final Rectangle renderRect;
		private int width = 8;

		private KeyFrame(int time) {
			this.time = time;
			final int currentTimePixelX = computeXFromTime(time);
			renderRect = new Rectangle(currentTimePixelX - width / 2, VERTICAL_SLIDER_HEIGHT, width, VERTICAL_TICKS_HEIGHT);
		}

		protected void reposition() {
			renderRect.x = computeXFromTime(time) - 4;
		}

		protected int getXPoint() {
			return renderRect.x + width / 2;
		}

		protected void setFrameX(int time) {
			renderRect.x = computeXFromTime(time) - width / 2;
		}
	}
}
