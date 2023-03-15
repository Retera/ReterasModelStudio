package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
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
	private final BakeGeometryTransformAction geometryTransformAction;

	private final Vec3 totTranslate = new Vec3();
	private final Vec3 deltaTranslate = new Vec3();

	public TranslateNodesTPoseAction(UndoAction addingTimelinesOrKeyframesAction,
	                                 Collection<IdObject> nodeSelection,
	                                 Collection<CameraNode> camSelection,
	                                 Collection<Geoset> geosets,
	                                 Vec3 translation,
                                     Mat4 rotMat,
                                     ModelStructureChangeListener changeListener){
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.changeListener = changeListener;
		topNodes.addAll(getTopNodes(nodeSelection));
		totTranslate.set(translation);
		deltaTranslate.set(translation);


		geometryTransformAction = new BakeGeometryTransformAction(topNodes, geosets, rotMat, null);
		if(!translation.equalLocs(Vec3.ZERO)) {
			geometryTransformAction.calculateTransform(translation, Quat.IDENTITY, topNodes);
		}

		for (IdObject node2 : nodeSelection) {
			translNodeActions.add(new TranslateNodeTPoseAction(node2, translation, rotMat, null));
		}
	}

	private List<IdObject> getTopNodes(Collection<IdObject> selection) {
		return selection.stream()
				.filter(idObject -> idObject.getParent() == null || !selection.contains(idObject.getParent()))
				.collect(Collectors.toList());
	}

	public TranslateNodesTPoseAction doSetup() {
		if(addingTimelinesOrKeyframesAction != null){
			addingTimelinesOrKeyframesAction.redo();
		}
		geometryTransformAction.doSetup();

		for(TranslateNodeTPoseAction action : translNodeActions){
			action.doSetup();
		}
		return this;
	}

	public TranslateNodesTPoseAction updateTranslation(Vec3 delta){
		deltaTranslate.set(delta);
		totTranslate.add(delta);

		geometryTransformAction.updateTransform(delta, Quat.IDENTITY, topNodes);

		for(TranslateNodeTPoseAction action : translNodeActions){
			action.updateTranslation(delta);
		}
		return this;
	}
	public TranslateNodesTPoseAction setTranslation(Vec3 transl){
		deltaTranslate.set(transl).sub(totTranslate);
		totTranslate.set(transl);

		geometryTransformAction.updateTransform(deltaTranslate, Quat.IDENTITY, topNodes);

		for(TranslateNodeTPoseAction action : translNodeActions){
			action.updateTranslation(deltaTranslate);
		}
		return this;
	}

	@Override
	public TranslateNodesTPoseAction undo() {
		geometryTransformAction.undo();

		for(TranslateNodeTPoseAction action : translNodeActions){
			action.undo();
		}
		if(addingTimelinesOrKeyframesAction != null){
			addingTimelinesOrKeyframesAction.undo();
		}
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public TranslateNodesTPoseAction redo() {
		if(addingTimelinesOrKeyframesAction != null){
			addingTimelinesOrKeyframesAction.redo();
		}
		for(TranslateNodeTPoseAction action : translNodeActions){
			action.redo();
		}
		geometryTransformAction.redo();

		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "TPose move " + "node.getName()";
	}
}
