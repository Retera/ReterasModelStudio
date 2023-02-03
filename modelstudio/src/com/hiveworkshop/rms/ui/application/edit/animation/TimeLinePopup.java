package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class TimeLinePopup extends JPopupMenu {
	private final KeyframeHandler keyframeHandler;
	private TimeEnvironmentImpl timeEnvironment;

	private UndoManager undoManager;
	private ModelHandler modelHandler;
	private final KeyframeTransferHelper keyframeTransferHelper = new KeyframeTransferHelper();
	private final ModelStructureChangeListener changeListener;

	ScrollableJSubMenu scrollableJSubMenu;

	public TimeLinePopup(KeyframeHandler keyframeHandler){
		this.keyframeHandler = keyframeHandler;
		changeListener = ModelStructureChangeListener.changeListener;
		scrollableJSubMenu = new ScrollableJSubMenu(this, 20);
	}
	public TimeLinePopup setTimeEnvironment(TimeEnvironmentImpl timeEnvironment) {
		this.timeEnvironment = timeEnvironment;
		return this;
	}

	public TimeLinePopup setModelHandler(ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		this.undoManager = modelHandler.getUndoManager();
		this.timeEnvironment = modelHandler.getRenderModel().getTimeEnvironment();
		return this;
	}

	public void fillAndShow(Integer time, Component invoker, int x, int y, boolean objectMenus){
		removeAll();
		scrollableJSubMenu.clear();
		Sequence sequence = timeEnvironment.getCurrentSequence();

		Collection<TimelineContainer> selectionToUse = keyframeHandler.getSelectionToUse();
		Set<TimelineContainer> objects = keyframeHandler.getKeyFrameObjects(time);
		List<? extends TimelineContainer> idObjects = modelHandler.getModel().getIdObjects();

		JMenuItem timeIndicator = new JMenuItem("" + time + " (" + selectionToUse.size() + " Nodes)");
		timeIndicator.setEnabled(false);
		add(timeIndicator);
		addSeparator();

		if (!objects.isEmpty()) {
			add("Delete All").addActionListener(e -> deleteKeyframes("delete keyframe", time, sequence, objects));
			addSeparator();
		}

		if(objectMenus){
			add("Cut").addActionListener(e -> cutItem(sequence, time, selectionToUse));
		}
		add("Copy").addActionListener(e -> copyKeyframes(sequence, time, selectionToUse));
		add("Copy Frame (whole model)").addActionListener(e -> copyAllKeyframes(sequence, time, idObjects));
		add("Paste").addActionListener(e -> pasteToAllSelected(sequence, time, selectionToUse));

		if(objectMenus){
			addSeparator();
			JMenuItem nodeIndicator = new JMenuItem("");
			add(nodeIndicator).setEnabled(false);
			addSeparator();
			int nodes = 0;
			scrollableJSubMenu.addArrowsToParent();
			for (TimelineContainer object : objects) {
				if(!object.getAnimFlags().isEmpty()){
					for (AnimFlag<?> flag : object.getAnimFlags()) {
						if (flag.hasEntryAt(sequence, time)) {
							String name;
							if(object instanceof Named){
								name = ((Named) object).getName();
							} else {
								name = object.getClass().getSimpleName();
							}
							JMenu subMenu = new JMenu(name + ": " + flag.getName());

							subMenu.add("Delete").addActionListener(e -> deleteKeyframe(flag, time, sequence));
							subMenu.addSeparator();
							subMenu.add("Cut").addActionListener(e -> cutSpecificItem(sequence, time, object, flag));
							subMenu.add("Copy").addActionListener(e -> copyKeyframeAt(object, flag, sequence, time));
							subMenu.add("Paste").addActionListener(e -> pasteToSpecificTimeline(sequence, time, flag));

							scrollableJSubMenu.add(subMenu);
							nodes++;
						}
					}
//					JMenu objectMenu = getObjectMenu(time, object);
//					if(0 < objectMenu.getItemCount()){
//						subMenus.add(objectMenu);
//						nodes++;
//					}
				}
			}
			nodeIndicator.setText(nodes + " Nodes with keyframes");
		}

		show(invoker, x, y);
	}


	public void cutSpecificItem(Sequence sequence, Integer time, TimelineContainer object, AnimFlag<?> flag) {
//		copyKeyframeAt(object, flag, sequence, time);

		keyframeTransferHelper.clear();
		keyframeTransferHelper.setUseAll(false);
		keyframeTransferHelper.collectKF(sequence, time, object, flag);

		if (flag.hasEntryAt(sequence, time)) {
			undoManager.pushAction(new RemoveFlagEntryAction<>(flag, time, sequence, changeListener).redo());
		}
	}

	public void cutItem(Sequence sequence, Integer time, Collection<TimelineContainer> selectionToUse) {
//		copyKeyframes(sequence, time, selectionToUse);
		keyframeTransferHelper.clear();
		keyframeTransferHelper.setUseAll(false);
		keyframeTransferHelper.collectKFs(selectionToUse, sequence, time);
//		Set<TimelineContainer> objects = timeToKey.get(time).getObjects();
//		List<UndoAction> actions = getDeleteActions(time, sequence, objects);
		List<UndoAction> actions = getDeleteActions(time, sequence, selectionToUse);
		if(!actions.isEmpty()){
			undoManager.pushAction(new CompoundAction("cut keyframe", actions, changeListener::keyframesUpdated).redo());
		}
	}

	public void deleteSelectedKeyframes() {
//		int time = timeEnvironment.getEnvTrackTime();
//		Sequence sequence = timeEnvironment.getCurrentSequence();
//		KeyFrame keyFrame = timeToKey.get(time);
//		if (keyFrame != null) {
//			List<UndoAction> actions = getDeleteActions(time, sequence, keyFrame.getObjects());
//			undoManager.pushAction(new CompoundAction("delete keyframe", actions, changeListener::keyframesUpdated).redo());
//		}
	}
	public void deleteKeyframes(String actionName, int time, Sequence sequence, Collection<TimelineContainer> objects) {
		List<UndoAction> actions = getDeleteActions(time, sequence, objects);
		if(!actions.isEmpty()){
			undoManager.pushAction(new CompoundAction(actionName, actions, changeListener::keyframesUpdated).redo());
		}
	}


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

	public void deleteKeyframe(AnimFlag<?> flag, int time, Sequence sequence) {
		if (flag.hasEntryAt(sequence, time)) {
			undoManager.pushAction(new RemoveFlagEntryAction<>(flag, time, sequence, changeListener).redo());
		}
	}

	public void copyKeyframes(Sequence sequence, int time, Collection<TimelineContainer> selectionToUse) {
		keyframeTransferHelper.clear();
		keyframeTransferHelper.setUseAll(false);
		keyframeTransferHelper.collectKFs(selectionToUse, sequence, time);
	}

//	public void copyAllKeyframes(Sequence sequence, int time) {
//		List<IdObject> idObjects = modelHandler.getModel().getIdObjects();
//		keyframeTransferHelper.clear();
//		keyframeTransferHelper.setUseAll(true);
//		keyframeTransferHelper.collectKFs(idObjects, sequence, time);
//	}

//	public void copyModelPose(Sequence sequence, int time) {
//		List<IdObject> idObjects = modelHandler.getModel().getIdObjects();
//		keyframeTransferHelper.clear();
//		keyframeTransferHelper.setUseAll(true);
//		keyframeTransferHelper.collectKFs(idObjects, sequence, time);
//	}

	public void copyAllKeyframes(Sequence sequence, int time, Collection<? extends TimelineContainer> selectionToUse) {
		keyframeTransferHelper.clear();
		keyframeTransferHelper.setUseAll(true);
		keyframeTransferHelper.collectKFs(selectionToUse, sequence, time);
	}

	public void copyModelPose(Sequence sequence, int time, Collection<? extends TimelineContainer> selectionToUse) {
		keyframeTransferHelper.clear();
		keyframeTransferHelper.setUseAll(true);
		keyframeTransferHelper.collectKFs(selectionToUse, sequence, time);
	}

	public void copyKeyframeAt(TimelineContainer object, AnimFlag<?> flag, Sequence sequence, int time) {
		keyframeTransferHelper.clear();
		keyframeTransferHelper.setUseAll(false);
		keyframeTransferHelper.collectKF(sequence, time, object, flag);
	}

	private boolean isCorrectSeq(AnimFlag<?> flag, Sequence sequence) {
		return Objects.equals(flag.getGlobalSeq(), sequence) || flag.getGlobalSeq() == null && !(sequence instanceof GlobalSeq);
	}

	// to be called externally
	public void copy() {
//		int time = timeEnvironment.getEnvTrackTime();
//		Sequence sequence = timeEnvironment.getCurrentSequence();
//		Collection<TimelineContainer> selectionToUse = getSelectionToUse();
////		copyKeyframes(sequence, time, selectionToUse);
//		keyframeTransferHelper.clear();
//		keyframeTransferHelper.setUseAll(false);
//		keyframeTransferHelper.collectKFs(selectionToUse, sequence, time);
	}

	public void cut() {
//		int time = timeEnvironment.getEnvTrackTime();
//		Sequence sequence = timeEnvironment.getCurrentSequence();
//		Collection<TimelineContainer> selectionToUse = getSelectionToUse();
////		copyKeyframes(sequence, time, selectionToUse);
//		keyframeTransferHelper.clear();
//		keyframeTransferHelper.setUseAll(false);
//		keyframeTransferHelper.collectKFs(selectionToUse, sequence, time);
//
//		final KeyFrame keyFrame = timeToKey.get(time);
//		if (keyFrame != null) {
//			List<UndoAction> actions = getDeleteActions(time, sequence, keyFrame.getObjects());
//			undoManager.pushAction(new CompoundAction("cut keyframe", actions, changeListener::keyframesUpdated).redo());
//		}
//		revalidateKeyframeDisplay();
	}

	public void paste() {
//		int time = timeEnvironment.getEnvTrackTime();
//		Sequence sequence = timeEnvironment.getCurrentSequence();
//		Collection<TimelineContainer> selectionToUse = getSelectionToUse();
//		pasteToAllSelected(sequence, time);
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
		}
	}

	public void pasteModelPose(Sequence sequence, int time, Collection<TimelineContainer> selectionToUse) {
		List<UndoAction> actions = new ArrayList<>();
		for (KeyFrameWrapper<?> frame : keyframeTransferHelper.getWrappedKeyframes()) {
			if (keyframeTransferHelper.isUseAll() || selectionToUse.contains(frame.getNode())) {
				actions.add(frame.getSetEntryAction(sequence, time));
			}
		}
		if (!actions.isEmpty()) {
			undoManager.pushAction(new CompoundAction("paste keyframe", actions, changeListener::keyframesUpdated).redo());
		}
	}

	public void pasteToSpecificTimeline(Sequence sequence, Integer time, AnimFlag<?> flag) {
		int mouseClickAnimationTime = time;
		KeyFrameWrapper<?> kfw = keyframeTransferHelper.getKFW(flag);
		if (kfw != null) {
			undoManager.pushAction(kfw.getSetEntryAction(sequence, mouseClickAnimationTime).redo());
		}
//		else {
//			JOptionPane.showMessageDialog(timelinePanel,
//					"Tell Retera to code in the ability to paste cross-node data!");
//		}
	}
}