package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderGeoset;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class ExportStaticMesh extends ActionFunction {
	public ExportStaticMesh(){
		super(TextKey.EXPORT_STATIC_MESH, ExportStaticMesh::askExp);
		setMenuItemMnemonic(KeyEvent.VK_X);
	}

	public static void askExp(ModelHandler modelHandler){
		if (!(ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE)) {
			JOptionPane.showMessageDialog(ProgramGlobals.getMainPanel(),
					"You must be in the Animation Editor to use that!",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int i = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(),
				"Remove all nodes in the exported model?",
				"Remove All Nodes?", JOptionPane.YES_NO_CANCEL_OPTION);
		if(i != JOptionPane.CANCEL_OPTION){
			exportAnimatedToStaticMesh(modelHandler, i == JOptionPane.YES_OPTION);
		}
	}

	public static void exportAnimatedToStaticMesh(ModelHandler modelHandler, boolean removeBones) {
		RenderModel sourceRenderModel = modelHandler.getRenderModel();
		EditableModel model = modelHandler.getModel();
		Animation stand = new Animation("Stand", 100, 1100);

		TimeEnvironmentImpl renderEnv = sourceRenderModel.getTimeEnvironment();
		Sequence currentAnimation = renderEnv.getCurrentSequence();
		int trackTime = renderEnv.getEnvTrackTime();

		String s = getAnimationName(model, trackTime, currentAnimation);
		EditableModel frozenModel = CloneEditableModel.deepClone(model, model.getHeaderName() + s);
		if (frozenModel.getFileRef() != null) {
			frozenModel.setFilePath(frozenModel.getFileRef().getPath().replaceFirst("(?<=\\w)\\.(?=md[lx])", s + "."));
		}

		Sequence sequenceInstance = getSequenceInstance(currentAnimation, frozenModel);
		System.out.println("sequence instance: " + sequenceInstance);

		RenderModel frozenRenderModel = new RenderModel(frozenModel, null);
		frozenRenderModel.setShouldForceAnimation(true);
		TimeEnvironmentImpl frozenRenderEnv = frozenRenderModel.getTimeEnvironment();
		frozenRenderEnv.setSequence(sequenceInstance);
		frozenRenderEnv.setAnimationTime(trackTime);
		frozenRenderModel.refreshFromEditor();
		frozenRenderModel.updateNodes(true);
		frozenRenderModel.updateGeosets();


		Bone boneRoot = null;
		if(removeBones){
			boneRoot = getNewRoot();
			frozenModel.clearAllIdObjects();
			frozenModel.add(boneRoot);
		} else {
			// freeze bones
			freezeNodes(frozenModel, frozenRenderModel);
			removeNotVisibleNodes(frozenModel, frozenRenderEnv);
		}
		processGeosets(frozenRenderModel, frozenModel, boneRoot);

//		for(Material material : frozenModel.getMaterials()){
//			for(Layer layer : material.getLayers()){
//				// Freeze layer stuff?
//			}
//		}

		for (Camera camera : frozenModel.getCameras()){
			freezeNode(frozenRenderModel, camera.getSourceNode(), MdlUtils.TOKEN_ROTATION);
			freezeNode(frozenRenderModel, camera.getTargetNode());
		}

		frozenModel.clearAnimations();
		frozenModel.add(stand);
		setSingeEntryForRemaining(stand, frozenRenderEnv, frozenModel);

		File.onClickSaveAs(null, FileDialog.SAVE_MODEL, frozenModel);
	}

	private static void removeNotVisibleNodes(EditableModel frozenModel, TimeEnvironmentImpl frozenRenderEnv) {
		List<IdObject> nodesToRemove = new ArrayList<>();
		for (IdObject idObject : frozenModel.getIdObjects()){
			if (!(idObject instanceof Bone)){
				float renderVisibility = idObject.getRenderVisibility(frozenRenderEnv);
				if (renderVisibility<0.01) {
					nodesToRemove.add(idObject);
				}
			}
		}
		for (IdObject idObject : nodesToRemove){
			frozenModel.remove(idObject);
		}
	}
	private static void freezeNodes(EditableModel frozenModel, RenderModel frozenRenderModel) {
				freezeNodes(frozenRenderModel, frozenModel.getBones());
//				freezeNodes(frozenRenderModel, frozenModel.getLights());
				freezeNodes(frozenRenderModel, frozenModel.getHelpers());
				freezeNodes(frozenRenderModel, frozenModel.getAttachments());
//				freezeNodes(frozenRenderModel, frozenModel.getParticleEmitters());
//				freezeNodes(frozenRenderModel, frozenModel.getParticleEmitter2s());
//				freezeNodes(frozenRenderModel, frozenModel.getPopcornEmitters());
//				freezeNodes(frozenRenderModel, frozenModel.getRibbonEmitters());
				freezeNodes(frozenRenderModel, frozenModel.getEvents());
				freezeNodes(frozenRenderModel, frozenModel.getColliders());

	}

	private static void freezeNodes(RenderModel frozenRenderModel, List<? extends AnimatedNode> nodes) {
		for (AnimatedNode node : nodes){
			freezeNode(frozenRenderModel, node);
		}
	}
	private static void freezeNode(RenderModel frozenRenderModel, AnimatedNode node, String... flagsToAddBack) {
		RenderNode<?> renderNode = frozenRenderModel.getRenderNode(node);
		if(renderNode != null){
			List<AnimFlag<?>> fixedFlagsToAddBack = new ArrayList<>();
			for(String flag : flagsToAddBack){
				AnimFlag<?> animFlag = interpolateAndClear(frozenRenderModel.getTimeEnvironment(), node.find(flag));
				if (animFlag != null){
					fixedFlagsToAddBack.add(animFlag);
				}
			}

			node.setPivotPoint(renderNode.getPivot());
			node.clearAnimFlags();
			node.addAll(fixedFlagsToAddBack);
		}
	}
	private static <Q> AnimFlag<Q> interpolateAndClear(TimeEnvironmentImpl renderEnv, AnimFlag<Q> animFlag) {
		if(animFlag != null && animFlag.hasSequence(renderEnv.getCurrentSequence())
//				|| animFlag != null && animFlag.hasGlobalSeq()
		){
			Q o = animFlag.interpolateAt(renderEnv);
			animFlag.clear();
			if(o != null){
				animFlag.addEntry(renderEnv.getEnvTrackTime(), o, renderEnv.getCurrentSequence());
				return animFlag;
			}
		}
		return null;
	}


	private static Sequence getSequenceInstance(Sequence currentAnimation, EditableModel frozenModel) {
		// Finds the actual Sequence object in the model copy
		Sequence sequenceInstance = null;
		if(currentAnimation != null){
			for (Sequence sequence : frozenModel.getAllSequences()){
				if(sequence.equals(currentAnimation)){
					sequenceInstance = sequence;
					break;
				} else if (sequence.getLength() == currentAnimation.getLength()
						&& ((sequence instanceof GlobalSeq && currentAnimation instanceof GlobalSeq)
							|| (sequence instanceof Animation && currentAnimation instanceof Animation
								&& sequence.getName().equals(currentAnimation.getName())))){
					sequenceInstance = sequence;
				}
			}
		}
		return sequenceInstance;
	}


	private static Bone getNewRoot() {
		Bone boneRoot = new Bone("Bone_Root");
		boneRoot.setPivotPoint(new Vec3(0, 0, 0));
		return boneRoot;
	}

	private static void setSingeEntryForRemaining(Animation stand, TimeEnvironmentImpl renderEnv, EditableModel frozenModel) {
		List<AnimFlag<?>> allAnimFlags = ModelUtils.getAllAnimFlags(frozenModel);
		for (AnimFlag<?> flag : allAnimFlags) {
			if (!flag.hasGlobalSeq() && flag.size() > 0) {
				addFlagEntry(renderEnv, stand, flag);
			}
		}
	}

	private static <Q> void addFlagEntry(TimeEnvironmentImpl renderEnv, Animation stand, AnimFlag<Q> flag) {
		Q value = flag.interpolateAt(renderEnv);
//		flag.setInterpType(InterpolationType.DONT_INTERP);
		flag.clear();
		flag.addEntry(0, value, stand);
	}

	private static void processGeosets(RenderModel frozenRenderModel, EditableModel frozenModel, Bone boneRoot) {
		List<Geoset> geosetsToRemove = new ArrayList<>();
		for(Geoset geoset : frozenModel.getGeosets()){
			RenderGeoset renderGeoset = frozenRenderModel.getRenderGeoset(geoset);
			Vec4 renderColor = renderGeoset.getRenderColor();

			geoset.setStaticAlpha(renderColor.w);
			geoset.setStaticColor(renderColor.getVec3());
			geoset.clearAnimFlags();
			if(0.01 < renderGeoset.getRenderColor().w){
				applyVertexTransforms(boneRoot, geoset, renderGeoset);
			} else {
				geosetsToRemove.add(geoset);
			}
		}

		for (Geoset geoset : geosetsToRemove) {
			frozenModel.remove(geoset);
		}
	}

	private static void applyVertexTransforms(Bone boneRoot, Geoset geoset, RenderGeoset renderGeoset) {
		for(GeosetVertex vertex : geoset.getVertices()){
			RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(vertex);
			vertex.set(renderVert.getRenderPos());
			vertex.setNormal(renderVert.getRenderNorm());
			if (vertex.getTangent() != null) {
				vertex.setTangent(renderVert.getRenderTang());
			}

			if(boneRoot != null){
				if (vertex.getSkinBones() != null) {
					vertex.setSkinBones(new Bone[] {boneRoot, null, null, null}, new short[] {255, 0, 0, 0});
				} else {
					vertex.clearBoneAttachments();
					vertex.addBoneAttachment(boneRoot);
				}
			}
		}
	}

	private static String getAnimationName(EditableModel model, int trackTime, Sequence currentAnimation) {
		if(currentAnimation instanceof Animation){
			return "_" + currentAnimation.getName() + "At" + trackTime;
		} else if (currentAnimation instanceof GlobalSeq){
			return "_GlobalSeq" + model.getGlobalSeqId((GlobalSeq) currentAnimation) + "At" + trackTime;
		}
		return "_At" + trackTime;
	}
}
