package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.SetKeyframeAction_T;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class KeyframeHandler {
//	private static final Color GLASS_TICK_COVER_COLOR = new Color(100, 190, 255, 100);
//	private static final Color GLASS_TICK_COVER_BORDER_COLOR = new Color(0, 80, 255, 220);
//	private static final int SLIDER_SIDE_BUTTON_SIZE = 15;
//	private static final int SLIDING_TIME_CHOOSER_WIDTH = 50 + (SLIDER_SIDE_BUTTON_SIZE * 2);
//	private static final int VERTICAL_TICKS_HEIGHT = 10;
//	private static final int VERTICAL_SLIDER_HEIGHT = 15;
//	private static final int PLAY_BUTTON_SIZE = 30;
//	private static final Dimension PLAY_BUTTON_DIMENSION = new Dimension(PLAY_BUTTON_SIZE, PLAY_BUTTON_SIZE);
//	private static final int SIDE_OFFSETS = SLIDING_TIME_CHOOSER_WIDTH / 2;
//	private static final Stroke WIDTH_2_STROKE = new BasicStroke(2);
//	private static final Stroke WIDTH_1_STROKE = new BasicStroke(1);

	private final TreeMap<Integer, KeyFrame> timeToKey = new TreeMap<>();
	private final List<CopiedKeyFrame<?>> copiedKeyframes = new ArrayList<>();
	private TimeEnvironmentImpl timeEnvironment;
	private final JPanel timelinePanel;
	private ModelHandler modelHandler;
	private UndoManager undoManager;
	private final ModelStructureChangeListener changeListener;

	private TimeSliderTimeListener notifier;

	boolean useAllKFs = false;

	private boolean useAllCopiedKeyframes = false;

	public KeyframeHandler(TimeSliderTimeListener notifier, JPanel timelinePanel){
		this.notifier = notifier;
		this.timelinePanel = timelinePanel;

		changeListener = ModelStructureChangeListener.changeListener;
	}

	public KeyframeHandler setModelHandler(ModelHandler modelHandler){
		this.modelHandler = modelHandler;
		if(modelHandler != null){
			undoManager = modelHandler.getUndoManager();
			timeEnvironment = modelHandler.getEditTimeEnv();
		} else {
			timeEnvironment = null;
			undoManager = null;
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
				if (isCorrectSeq(flag)) {
					TreeMap<Integer, ? extends Entry<?>> entryMap = flag.getEntryMap(timeEnvironment.getCurrentSequence());
					if (entryMap != null) {
						System.out.println(object.getName() + ": " + flag.getName());
//						TreeMap<Integer, ? extends Entry<?>> entryMap = flag.getEntryMap(timeEnvironment.getCurrentSequence());
						for (Integer time : entryMap.keySet()) {
							KeyFrame keyFrame = timeToKey.computeIfAbsent(time, k -> new KeyFrame(this, time));
							keyFrame.addObject(object);
							keyFrame.addTimeline(flag);
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
		deleteKeyframes("cut keyframe", time, timeToKey.get(time).getObjects());
	}

	public void deleteSelectedKeyframes() {
		KeyFrame keyFrame = timeToKey.get(timeEnvironment.getEnvTrackTime());
		if (keyFrame != null) {
			deleteKeyframes("delete keyframe", timeEnvironment.getEnvTrackTime(), keyFrame.getObjects());
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
		CompoundAction action = new CompoundAction(actionName, actions, () -> changeListener.keyframesUpdated());
		undoManager.pushAction(action.redo());
	}
	public void deleteKeyframe(AnimFlag<?> flag, int trackTime) {
		Sequence currentSequence = timeEnvironment.getCurrentSequence();
		if (flag.getEntryMap(currentSequence).containsKey(trackTime)) {
			undoManager.pushAction(new RemoveFlagEntryAction<>(flag, trackTime, currentSequence, changeListener).redo());
		}
	}

	public void copyKeyframes(int trackTime) {
		copiedKeyframes.clear();
		useAllCopiedKeyframes = false;
		for (IdObject object : getSelectionToUse()) {
			for (AnimFlag<?> flag : object.getAnimFlags()) {
				if (isCorrectSeq(flag)) {
					copuKeyframes(object, flag, trackTime);
				}
			}
		}
	}

	private boolean isCorrectSeq(AnimFlag<?> flag) {
		return (flag.getGlobalSeq() == null && timeEnvironment.getGlobalSeq() == null)
				|| (timeEnvironment.getGlobalSeq() != null && timeEnvironment.getGlobalSeq().equals(flag.getGlobalSeq()));
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
			deleteKeyframes("cut keyframe", timeEnvironment.getEnvTrackTime(), keyFrame.getObjects());
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
		undoManager.pushAction(new CompoundAction("paste keyframe", actions, changeListener::keyframesUpdated).redo());
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
				if (isCorrectSeq(flag)) {
					copuKeyframes(object, flag, trackTime);
				}
			}
		}
	}

	public KeyFrame getKeyFrameFromPoint(Point point) {
		for (KeyFrame key : timeToKey.values()) {
			if (key.containsPoint(point)) {
				return key;
			}
		}
		return null;
	}
	public Integer getTimeFromPoint(Point point) {
		for (KeyFrame key : timeToKey.values()) {
			if (key.containsPoint(point)) {
				return key.getTime();
			}
		}
		return null;
	}

	public KeyFrame getKeyFrame(int time){
		return timeToKey.get(time);
	}


	public KeyFrame removeFrame(int time){
		return timeToKey.remove(time);
	}

	public KeyframeHandler putFrame(int time, KeyFrame keyFrame){
		timeToKey.put(time, keyFrame);
		return this;
	}

	public Integer getNextFrame(int time) {
		return timeToKey.higherKey(time);
	}

	public Integer getPrevFrame(int time) {
		return timeToKey.lowerKey(time);
	}

	public Integer getNextFrame() {
		if (timeEnvironment != null) {
			if (!timeToKey.isEmpty()) {
				Integer frameTime = timeToKey.higherKey(timeEnvironment.getEnvTrackTime());
				if (frameTime == null) {
					frameTime = timeToKey.higherKey(0);
				}
				return frameTime;
			}
			return timeEnvironment.getEnvTrackTime();
		}
		return null;
	}

	public Integer getPrevFrame() {
		if (timeEnvironment != null) {
			if (!timeToKey.isEmpty()) {
				Integer frameTime = timeToKey.lowerKey(timeEnvironment.getEnvTrackTime());
				if (frameTime == null) {
					frameTime = timeToKey.lowerKey(timeEnvironment.getLength());
				}
				return frameTime;
			}
			return timeEnvironment.getEnvTrackTime();
		}
		return null;
	}

	public NavigableSet<Integer> getTimes() {
		return timeToKey.navigableKeySet();
	}

	public KeyFrame initDragging(Point lastMousePoint) {
		for (KeyFrame frame : timeToKey.values()) {
			if (frame.containsPoint(lastMousePoint)) {
				return frame;
			}
		}
		return null;
	}

	public void slideExistingKeyFramesForResize() {
		for (KeyFrame key : timeToKey.values()) {
			key.reposition();
		}
	}

	public boolean[] getTransRotScalOth(Integer time) {
		boolean[] transRotScalOth = new boolean[] {false, false, false, false};
		if(timeToKey.get(time) != null){
			for (AnimFlag<?> af : timeToKey.get(time).getTimelines()) {
				String afName = af.getName();
				transRotScalOth[0] = (afName.equals(MdlUtils.TOKEN_TRANSLATION) || transRotScalOth[0]);
				transRotScalOth[1] = (afName.equals(MdlUtils.TOKEN_ROTATION) || transRotScalOth[1]);
				transRotScalOth[2] = (afName.equals(MdlUtils.TOKEN_SCALING) || transRotScalOth[2]);
				transRotScalOth[3] |= !(afName.equals(MdlUtils.TOKEN_TRANSLATION) || afName.equals(MdlUtils.TOKEN_ROTATION) || afName.equals(MdlUtils.TOKEN_SCALING));
			}
		}
		return transRotScalOth;
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

//	private int computeXFromTime(int time) {
//		int widthMinusOffsets = timelinePanel.getWidth() - (SIDE_OFFSETS * 2);
//		double timeRatio = (time) / (double) (timeEnvironment.getLength());
////		System.out.println("new x: " + ((widthMinusOffsets * timeRatio) + (SIDE_OFFSETS)) + " for time " + time);
//		return (int) (widthMinusOffsets * timeRatio) + (SIDE_OFFSETS);
//	}

	public JPanel getTimelinePanel() {
		return timelinePanel;
	}

	public TimeEnvironmentImpl getTimeEnvironment() {
		return timeEnvironment;
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

	public void drawKeyframeMarkers(Graphics g) {
		for (Integer time : getTimes()) {
			getKeyFrame(time).drawMarker(g);
		}
	}

}
