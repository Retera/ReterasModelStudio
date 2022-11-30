package com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree;

import com.hiveworkshop.rms.ui.icons.RMSIcons;

import javax.swing.*;

public enum DisplayElementType {
	DUMMY("", "model.png"),
	ANIMATION("Animation", "animation.png"),
	MODEL_ROOT("Model", "model.png"),
	HEADER("Header", "model.png"),
	COMMENT("Comment", "comment.png"),
	DATA("Model", "model.png"),
	GLOBAL_SEQ("GlobalSequence", "globalseq.png"),
	TEXTURE("Texture", "bitmap.png"),
	MATERIAL("Material", "material.png"),
	TEXTURE_ANIM("TextureAnim", "textureanim.png"),
	TVERT_ANIM("TVertexAnims", "textureanim.png"),
	BONE("Bone", "bone.png"),
	HELPER("Helper", "helperhand.png"),
	LIGHT("Light", "light.png"),
	ATTACHMENT("Attachment", "attachment.png"),
	PARTICLE("ParticleEmitter", "particle1.png"),
	PARTICLE2("ParticleEmitter2", "particle2.png"),
	POPCORN("ParticleEmitterPopcorn", "popcorn.png"),
	RIBBON("RibbonEmitter", "ribbon.png"),
	EVENT_OBJECT("EventObject", "event.png"),
	COLLISION_SHAPE("CollisionShape", "collision.png"),
	CAMERA("Camera", "camera.png"),
	FACEFX("FaceFX", "fafx.png"),
	BINDPOSE("BindPose", "bindpos.png"),
	GROUP("", "model.png"),
	GEOSET_ITEM("Geoset", "geoset.png"),
	NODES("Nodes", "bone.png");

	private final String name;
	private ImageIcon icon;
//	private static final ImageIcon GROUP_ICON = new ImageIcon(IconUtils.worldEditStyleIcon(BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup.blp")));
//	private static final ImageIcon GROUP_ICON_EXPANDED = new ImageIcon(IconUtils.worldEditStyleIcon(BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup-Open.blp")));


	DisplayElementType(String name, String iconPath) {
		this.name = name;
		icon = new ImageIcon(RMSIcons.loadNodeImage(iconPath));
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public String getName() {
		return name;
	}
}
