package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.KeyframeActionHelpers;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;
import java.util.stream.Collectors;

public class TranslateNodesTPoseAction extends AbstractTransformAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final ModelStructureChangeListener changeListener;
	private final List<TranslateNodeTPoseAction> translNodeActions = new ArrayList<>();
	private final Set<IdObject> topNodes = new LinkedHashSet<>();
	private final Set<IdObject> allNodes = new LinkedHashSet<>();
	private final BakeGeometryTransformAction geometryTransformAction;

	private final Vec3 totTranslate = new Vec3();
	private final Vec3 deltaTranslate = new Vec3();
	private String actionName;

	public TranslateNodesTPoseAction(UndoAction addingTimelinesOrKeyframesAction,
	                                 Collection<IdObject> nodeSelection,
	                                 Collection<CameraNode> camSelection,
	                                 Collection<Geoset> geosets,
	                                 Vec3 translation,
                                     Mat4 rotMat,
									 boolean preserveAnimations,
									 GlobalSeq globalSeq,
									 Collection<Sequence> sequences,
                                     ModelStructureChangeListener changeListener){
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.changeListener = changeListener;
		totTranslate.set(translation);
		deltaTranslate.set(translation);

		topNodes.addAll(getTopNodes(nodeSelection));
		for (IdObject topNode : topNodes){
			collectSortedSelected(nodeSelection, allNodes, topNode);
		}

		geometryTransformAction = new BakeGeometryTransformAction(allNodes, geosets, rotMat, null);
		if (!translation.equalLocs(Vec3.ZERO)) {
			geometryTransformAction.calculateTransform(translation, Quat.IDENTITY, allNodes);
		}

		for (IdObject node2 : allNodes) {
			if (preserveAnimations){
				AnimFlag<Vec3> translTimeline = (Vec3AnimFlag) KeyframeActionHelpers.getNewOrCopiedTimeline(node2, MdlUtils.TOKEN_TRANSLATION, new Vec3(), globalSeq);
				if (translTimeline != null) {
					KeyframeActionHelpers.ensureSequenceKFs(sequences, translTimeline);
				}
				translNodeActions.add(new TranslateNodeTPoseAction(node2, translation, rotMat, preserveAnimations, translTimeline, null));
			} else {
				translNodeActions.add(new TranslateNodeTPoseAction(node2, translation, rotMat, preserveAnimations, null, null));
			}
		}
		if (nodeSelection.size() == 1) {
			IdObject idObject = nodeSelection.stream().findFirst().orElse(null);
			actionName = "TPose Move " + idObject.getName();
		} else {
			actionName = "TPose Move " + nodeSelection.size() + " nodes";
		}
	}

	private List<IdObject> getTopNodes(Collection<IdObject> selection) {
		return selection.stream()
				.filter(idObject -> idObject.getParent() == null || !selection.contains(idObject.getParent()))
				.collect(Collectors.toList());
	}

	private void collectSortedSelected(Collection<IdObject> selection, Set<IdObject> sortedSelection, IdObject currNode){
		if (selection.contains(currNode)) {
			sortedSelection.add(currNode);
			for (IdObject child : currNode.getChildrenNodes()) {
				collectSortedSelected(selection, sortedSelection, child);
			}
		}
	}

	public TranslateNodesTPoseAction doSetup() {
		if (addingTimelinesOrKeyframesAction != null) {
			addingTimelinesOrKeyframesAction.redo();
		}
		geometryTransformAction.doSetup();

		for (TranslateNodeTPoseAction action : translNodeActions) {
			action.doSetup();
		}
		return this;
	}

	public TranslateNodesTPoseAction updateTranslation(Vec3 delta){
		deltaTranslate.set(delta);
		totTranslate.add(delta);

		geometryTransformAction.updateTransform(delta, Quat.IDENTITY, topNodes);

		for (TranslateNodeTPoseAction action : translNodeActions) {
			action.updateTranslation(delta);
		}
		return this;
	}
	public TranslateNodesTPoseAction setTranslation(Vec3 transl){
		deltaTranslate.set(transl).sub(totTranslate);
		totTranslate.set(transl);

		geometryTransformAction.updateTransform(deltaTranslate, Quat.IDENTITY, topNodes);

		for (TranslateNodeTPoseAction action : translNodeActions) {
			action.updateTranslation(deltaTranslate);
		}
		return this;
	}

	@Override
	public TranslateNodesTPoseAction undo() {
		geometryTransformAction.undo();

		for (TranslateNodeTPoseAction action : translNodeActions) {
			action.undo();
		}
		if (addingTimelinesOrKeyframesAction != null) {
			addingTimelinesOrKeyframesAction.undo();
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public TranslateNodesTPoseAction redo() {
		if (addingTimelinesOrKeyframesAction != null) {
			addingTimelinesOrKeyframesAction.redo();
		}
		for (TranslateNodeTPoseAction action : translNodeActions) {
			action.redo();
		}
		geometryTransformAction.redo();

		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}
}
