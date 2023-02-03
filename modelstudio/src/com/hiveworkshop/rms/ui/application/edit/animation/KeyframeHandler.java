package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
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

	private final TreeMap<Integer, KeyFrame> timeToKey = new TreeMap<>();
	private final KeyframeTransferHelper keyframeTransferHelper = new KeyframeTransferHelper();
	private TimeEnvironmentImpl timeEnvironment;
	private final JPanel timelinePanel;
	private ModelHandler modelHandler;
	private UndoManager undoManager;
	private final ModelStructureChangeListener changeListener;
	Collection<TimelineContainer> collectionToUse = new HashSet<>();

	boolean useAllKFs = false;
	boolean visKFs = true;


	public KeyframeHandler(JPanel timelinePanel){
		this.timelinePanel = timelinePanel;

		changeListener = ModelStructureChangeListener.changeListener;
	}

	public KeyframeHandler setModelHandler(ModelHandler modelHandler){
		this.modelHandler = modelHandler;
		if(modelHandler != null){
			undoManager = modelHandler.getUndoManager();
			timeEnvironment = modelHandler.getRenderModel().getTimeEnvironment();
		} else {
			timeEnvironment = null;
			undoManager = null;
		}
		return this;
	}

	public void revalidateKeyframeDisplay() {
		updateKeyframeDisplay();
	}

	public Collection<TimelineContainer> getSelectionToUse() {
		collectionToUse.clear();
		if ((modelHandler == null) || (modelHandler.getModel() == null)) {
			return collectionToUse;
		}
		if(useAllKFs){
			collectionToUse.addAll(modelHandler.getModelView().getEditableIdObjects());
		} else {
			collectionToUse.addAll(modelHandler.getModelView().getSelectedIdObjects());
		}
		if(useAllKFs && visKFs){
			for(Geoset geoset: modelHandler.getModelView().getEditableGeosets()){
				if(modelHandler.getModelView().isEditable(geoset) && !geoset.getAnimFlags().isEmpty()){
					collectionToUse.add(geoset);
				}
			}
		}
		return collectionToUse;
	}

	public KeyframeHandler setShowAllKFs(boolean useAllKFs){
		this.useAllKFs = useAllKFs;
		updateKeyframeDisplay();
		return this;
	}

	public void updateKeyframeDisplay() {
		timeToKey.clear();
		if(timeEnvironment != null) {
			Sequence sequence = timeEnvironment.getCurrentSequence();
			Collection<TimelineContainer> selection = getSelectionToUse();

			for (TimelineContainer object : selection) {
				for (AnimFlag<?> flag : object.getAnimFlags()) {
					if (isCorrectSeq(flag, sequence)) {
						TreeMap<Integer, ? extends Entry<?>> entryMap = flag.getEntryMap(sequence);
						if (entryMap != null) {
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
	}

//	public void cutSpecificItem(Sequence sequence, Integer time, TimelineContainer object, AnimFlag<?> flag) {
////		copyKeyframeAt(object, flag, sequence, time);
//
//		keyframeTransferHelper.clear();
//		keyframeTransferHelper.setUseAll(false);
//		keyframeTransferHelper.collectKF(sequence, time, object, flag);
//
//		if (flag.hasEntryAt(sequence, time)) {
//			undoManager.pushAction(new RemoveFlagEntryAction<>(flag, time, sequence, changeListener).redo());
//		}
//	}
//
//	public void cutItem(Sequence sequence, Integer time, Collection<TimelineContainer> selectionToUse) {
////		copyKeyframes(sequence, time, selectionToUse);
//		keyframeTransferHelper.clear();
//		keyframeTransferHelper.setUseAll(false);
//		keyframeTransferHelper.collectKFs(selectionToUse, sequence, time);
//		Set<TimelineContainer> objects = timeToKey.get(time).getObjects();
//		List<UndoAction> actions = getDeleteActions(time, sequence, objects);
//		if(!actions.isEmpty()){
//			undoManager.pushAction(new CompoundAction("cut keyframe", actions, changeListener::keyframesUpdated).redo());
//		}
//	}

	public void deleteSelectedKeyframes() {
		int time = timeEnvironment.getEnvTrackTime();
		Sequence sequence = timeEnvironment.getCurrentSequence();
		KeyFrame keyFrame = timeToKey.get(time);
		if (keyFrame != null) {
			List<UndoAction> actions = getDeleteActions(time, sequence, keyFrame.getObjects());
			undoManager.pushAction(new CompoundAction("delete keyframe", actions, changeListener::keyframesUpdated).redo());
		}
		revalidateKeyframeDisplay();
	}
//	public void deleteKeyframes(String actionName, int time, Sequence sequence, Collection<TimelineContainer> objects) {
//		List<UndoAction> actions = getDeleteActions(time, sequence, objects);
//		undoManager.pushAction(new CompoundAction(actionName, actions, changeListener::keyframesUpdated).redo());
//	}


	private List<UndoAction> getDeleteActions(int time, Sequence sequence, Collection<TimelineContainer> objects) {
		List<UndoAction> actions = new ArrayList<>();
		for (TimelineContainer object : objects) {
			for (AnimFlag<?> flag : object.getAnimFlags()) {
				if (flag.hasEntryAt(sequence, time)) {
					actions.add(new RemoveFlagEntryAction<>(flag, time, sequence, null));
				}
			}
		}
		return actions;
	}

//	public void deleteKeyframe(AnimFlag<?> flag, int time, Sequence sequence) {
//		if (flag.hasEntryAt(sequence, time)) {
//			undoManager.pushAction(new RemoveFlagEntryAction<>(flag, time, sequence, changeListener).redo());
//		}
//	}
//
//	public void copyKeyframes(Sequence sequence, int time, Collection<TimelineContainer> selectionToUse) {
//		keyframeTransferHelper.clear();
//		keyframeTransferHelper.setUseAll(false);
//		keyframeTransferHelper.collectKFs(selectionToUse, sequence, time);
//	}
//
//	public void copyAllKeyframes(Sequence sequence, int time) {
//		List<IdObject> idObjects = modelHandler.getModel().getIdObjects();
//		keyframeTransferHelper.clear();
//		keyframeTransferHelper.setUseAll(true);
//		keyframeTransferHelper.collectKFs(idObjects, sequence, time);
//	}
//
//	public void copyModelPose(Sequence sequence, int time) {
//		List<IdObject> idObjects = modelHandler.getModel().getIdObjects();
//		keyframeTransferHelper.clear();
//		keyframeTransferHelper.setUseAll(true);
//		keyframeTransferHelper.collectKFs(idObjects, sequence, time);
//	}
//
//	public void copyAllKeyframes(Sequence sequence, int time, Collection<TimelineContainer> selectionToUse) {
//		keyframeTransferHelper.clear();
//		keyframeTransferHelper.setUseAll(true);
//		keyframeTransferHelper.collectKFs(selectionToUse, sequence, time);
//	}
//
//	public void copyModelPose(Sequence sequence, int time, Collection<TimelineContainer> selectionToUse) {
//		keyframeTransferHelper.clear();
//		keyframeTransferHelper.setUseAll(true);
//		keyframeTransferHelper.collectKFs(selectionToUse, sequence, time);
//	}
//
//	public void copyKeyframeAt(TimelineContainer object, AnimFlag<?> flag, Sequence sequence, int time) {
//		keyframeTransferHelper.clear();
//		keyframeTransferHelper.setUseAll(false);
//		keyframeTransferHelper.collectKF(sequence, time, object, flag);
//	}

	private boolean isCorrectSeq(AnimFlag<?> flag, Sequence sequence) {
		return Objects.equals(flag.getGlobalSeq(), sequence) || flag.getGlobalSeq() == null && !(sequence instanceof GlobalSeq);
	}

	// to be called externally
	public void copy() {
		int time = timeEnvironment.getEnvTrackTime();
		Sequence sequence = timeEnvironment.getCurrentSequence();
		Collection<TimelineContainer> selectionToUse = getSelectionToUse();
//		copyKeyframes(sequence, time, selectionToUse);
		keyframeTransferHelper.clear();
		keyframeTransferHelper.setUseAll(false);
		keyframeTransferHelper.collectKFs(selectionToUse, sequence, time);
	}

	public void cut() {
		int time = timeEnvironment.getEnvTrackTime();
		Sequence sequence = timeEnvironment.getCurrentSequence();
		Collection<TimelineContainer> selectionToUse = getSelectionToUse();
//		copyKeyframes(sequence, time, selectionToUse);
		keyframeTransferHelper.clear();
		keyframeTransferHelper.setUseAll(false);
		keyframeTransferHelper.collectKFs(selectionToUse, sequence, time);

		final KeyFrame keyFrame = timeToKey.get(time);
		if (keyFrame != null) {
			List<UndoAction> actions = getDeleteActions(time, sequence, keyFrame.getObjects());
			undoManager.pushAction(new CompoundAction("cut keyframe", actions, changeListener::keyframesUpdated).redo());
		}
		revalidateKeyframeDisplay();
	}

	public void paste() {
		int time = timeEnvironment.getEnvTrackTime();
		Sequence sequence = timeEnvironment.getCurrentSequence();
		Collection<TimelineContainer> selectionToUse = getSelectionToUse();
		pasteToAllSelected(sequence, time, selectionToUse);
	}

	public void pasteToAllSelected(Sequence sequence, int time, Collection<TimelineContainer> selectionToUse) {
		List<UndoAction> actions = new ArrayList<>();
		for (KeyFrameWrapper<?> frame : keyframeTransferHelper.getWrappedKeyframes()) {
			if (frame.isFromExisting() && (keyframeTransferHelper.isUseAll() || selectionToUse.contains(frame.getNode()))) {
				actions.add(frame.getSetEntryAction(sequence, time));
			}
		}
		if (!actions.isEmpty()) {
			undoManager.pushAction(new CompoundAction("paste keyframe", actions, changeListener::keyframesUpdated).redo());
			revalidateKeyframeDisplay();
		}
	}

//	public void pasteModelPose(Sequence sequence, int time, Collection<TimelineContainer> selectionToUse) {
//		List<UndoAction> actions = new ArrayList<>();
//		for (KeyFrameWrapper<?> frame : keyframeTransferHelper.getWrappedKeyframes()) {
//			if (keyframeTransferHelper.isUseAll() || selectionToUse.contains(frame.getNode())) {
//				actions.add(frame.getSetEntryAction(sequence, time));
//			}
//		}
//		if (!actions.isEmpty()) {
//			undoManager.pushAction(new CompoundAction("paste keyframe", actions, changeListener::keyframesUpdated).redo());
//			revalidateKeyframeDisplay();
//		}
//	}
//
//	public void pasteToSpecificTimeline(Sequence sequence, Integer time, AnimFlag<?> flag) {
//		int mouseClickAnimationTime = time;
//		KeyFrameWrapper<?> kfw = keyframeTransferHelper.getKFW(flag);
//		if (kfw != null) {
//			undoManager.pushAction(kfw.getSetEntryAction(sequence, mouseClickAnimationTime).redo());
//		} else {
//			JOptionPane.showMessageDialog(timelinePanel,
//					"Tell Retera to code in the ability to paste cross-node data!");
//		}
//		revalidateKeyframeDisplay();
//	}

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

	public Boolean hasKeyFrameAt(int time){
		return timeToKey.containsKey(time);
	}

	public KeyFrame getKeyFrame(int time){
		return timeToKey.get(time);
	}

	public Set<TimelineContainer> getKeyFrameObjects(int time){
		if(timeToKey.get(time) != null){
			return timeToKey.get(time).getObjects();
		} else {
			return Collections.emptySet();
		}
	}


	public KeyFrame removeFrame(int time) {
		return timeToKey.remove(time);
	}

	public KeyframeHandler putFrame(int time, KeyFrame keyFrame) {
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
		tempKeyframe = null;
		for (KeyFrame frame : timeToKey.values()) {
			if (frame.containsPoint(lastMousePoint)) {
				return frame.initDrag(timeEnvironment.getCurrentSequence(), this::revalidateKeyframeDisplay);
			}
		}
		return null;
	}

	KeyFrame tempKeyframe;
	public KeyFrame dragFrame(KeyFrame draggingFrame, int newTime){
		int oldTime = draggingFrame.getTime();
		if(oldTime != newTime){
			KeyFrame tempFrame = removeFrame(newTime);
			removeFrame(oldTime);
			draggingFrame.dragTime(newTime);
			putFrame(newTime, draggingFrame);
			if(tempKeyframe != null){
				System.out.println("setting keyframe at " + tempKeyframe.getTime());
				putFrame(tempKeyframe.getTime(), tempKeyframe);
			}
			tempKeyframe = tempFrame;
		}
		return draggingFrame;
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

	public void drawKeyframeMarkers(Graphics g) {
		for (Integer time : getTimes()) {
			getKeyFrame(time).drawMarker(g);
		}
	}

}
