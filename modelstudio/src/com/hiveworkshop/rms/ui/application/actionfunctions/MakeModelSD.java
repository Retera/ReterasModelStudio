package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
import com.hiveworkshop.rms.editor.actions.mesh.DeleteGeosetAction;
import com.hiveworkshop.rms.editor.actions.model.DeleteFaceEffectAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.SetBitmapPathAction;
import com.hiveworkshop.rms.editor.actions.model.header.SetFormatVersionAction;
import com.hiveworkshop.rms.editor.actions.model.material.AddMaterialAction;
import com.hiveworkshop.rms.editor.actions.model.material.SetMaterialShaderStringAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.editor.actions.nodes.SetAttachmentPathAction;
import com.hiveworkshop.rms.editor.actions.nodes.SetParticleEmitterPathAction;
import com.hiveworkshop.rms.editor.actions.tools.ConvertToMatricesAction;
import com.hiveworkshop.rms.editor.actions.util.BoolAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.model.util.Mesh;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MakeModelSD extends ActionFunction {
	public MakeModelSD() {
		super(TextKey.HD_TO_SD, (modelHandler) -> convertToV800(modelHandler, 1));
	}


	/**
	 * Please, for the love of Pete, don't actually do this.
	 */
	public static void convertToV800(ModelHandler modelHandler, int targetLevelOfDetail) {
		if (modelHandler != null && modelHandler.getModel().getFormatVersion() != 800) {
			List<UndoAction> undoActions = new ArrayList<>();
			EditableModel model = modelHandler.getModel();
			// Things to fix:
			// 1.) format version
			undoActions.add(new SetFormatVersionAction(800, model, null));
			// 2.) materials: only diffuse
			for (Bitmap tex : model.getTextures()) {
				UndoAction action = convertBitmapPath(tex);
				if (action != null) undoActions.add(action);
			}
			for (Material material : model.getMaterials()) {
				undoActions.add(new SetMaterialShaderStringAction(model, material, "Shader_SD_FixedFunction", null));
			}
			// 3.) geosets:
			// - Convert skin to matrices & vertex groups
			List<Geoset> wrongLOD = model.getGeosets().stream()
					.filter(geoset -> geoset.getLevelOfDetail() != targetLevelOfDetail)
					.collect(Collectors.toList());
			for (Geoset geoset : model.getGeosets()) {
				// Try to use targetLevelOfDetail, but if no geosets with lod = targetLevelOfDetail is found use all geosets
				if (geoset.getLevelOfDetail() == targetLevelOfDetail || wrongLOD.size() == model.getGeosets().size()) {
					undoActions.add(new ConvertToMatricesAction(geoset, null));
				}
			}
			// - Eradicate anything that isn't LOD==X
			if (model.getGeosets().size() > wrongLOD.size() && !wrongLOD.isEmpty()) {
				undoActions.add(new DeleteGeosetAction(model, wrongLOD, null));
			}
			// 4.) remove popcorn
			// - add hero glow from popcorn if necessary
			for (ParticleEmitterPopcorn popcorn : model.getPopcornEmitters()) {
				if (popcorn.getPath().toLowerCase().contains("hero_glow")) {
					undoActions.add(replaceHeroGlowEffect(model));
				}
			}
			undoActions.add(new DeleteNodesAction(model.getPopcornEmitters(), null, model));

			// 6.) fix dump bug with paths:
			for (ParticleEmitter emitter : model.getParticleEmitters()) {
				String path = emitter.getPath();
				if (path != null) {
					path = path.replace('/', '\\');
					undoActions.add(new SetParticleEmitterPathAction(emitter, path, null));
				}
			}
			for (Attachment emitter : model.getAttachments()) {
				String path = emitter.getPath();
				if (path != null) {
					path = path.replace('/', '\\');
					undoActions.add(new SetAttachmentPathAction(emitter, path, null));
				}
			}
			if (model.isUseBindPose()) {
				undoActions.add(new BoolAction(model::setUseBindPose, false, "", null));
			}

			undoActions.add(new DeleteFaceEffectAction(new ArrayList<>(model.getFaceEffects()), model, null));
			modelHandler.getUndoManager().pushAction(new CompoundAction("Make model SD", undoActions, ModelStructureChangeListener.changeListener::nodesUpdated).redo());
		}
	}

	private static UndoAction replaceHeroGlowEffect(EditableModel model) {
		System.out.println("HERO HERO HERO");
		Bone dummyHeroGlowNode = new Bone("hero_reforged");
		// this model needs hero glow
		Mesh heroGlowPlane = ModelUtils.getPlaneMesh2(new Vec3(64, 64, 0), new Vec3(-64, -64, 0), 1, 1);

		Geoset heroGlow = new Geoset();
		heroGlow.getVertices().addAll(heroGlowPlane.getVertices());
		for (GeosetVertex gv : heroGlow.getVertices()) {
			gv.setGeoset(heroGlow);
			gv.clearBoneAttachments();
			gv.addBoneAttachment(dummyHeroGlowNode);
		}
		heroGlow.getTriangles().addAll(heroGlowPlane.getTriangles());
		heroGlow.setUnselectable(true);

		Bitmap heroGlowBitmap = new Bitmap("", 2);
		Layer layer = new Layer(FilterMode.ADDITIVE, heroGlowBitmap);
		layer.setUnshaded(true);
		layer.setUnfogged(true);
		Material heroGlowMaterial = new Material(layer);
		heroGlow.setMaterial(heroGlowMaterial);

		UndoAction addNodeAction = new AddNodeAction(model, dummyHeroGlowNode, null);
		UndoAction addGeosetAction = new AddGeosetAction(heroGlow, model, null);
		UndoAction addMaterialAction = new AddMaterialAction(heroGlowMaterial, model, null);


		return new CompoundAction("Add Hero Glow", null, addNodeAction, addMaterialAction, addGeosetAction);
	}

	private static UndoAction convertBitmapPath(Bitmap tex) {
		String path = tex.getPath();
		if (path != null && !path.isEmpty()) {
			int dotIndex = path.lastIndexOf('.');
			if (dotIndex != -1 && !path.endsWith(".blp")) {
				path = (path.substring(0, dotIndex));
			}
			if (!path.endsWith(".blp")) {
				path += ".blp";
			};
			path = path.replace('/', '\\');

			return new SetBitmapPathAction(tex, path, null);
		}
		return null;
	}
}
