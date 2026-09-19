package com.hiveworkshop.wc3.gui.modeledit.componenttree;

import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.ParticleEmitterPopcorn;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.TextureAnim;

/**
 * The kinds of model component the Model tab can create, copy, paste and
 * delete, with the tree group each one lives under.
 */
public enum ComponentKind {
	SEQUENCE("Sequence", "Sequences", "animation.png"),
	GLOBAL_SEQUENCE("Global Sequence", "GlobalSequences", "globalseq.png"),
	TEXTURE("Texture", "Textures", "bitmap.png"),
	MATERIAL("Material", "Materials", "material.png"),
	TEXTURE_ANIM("Texture Anim", "TVertexAnims", "textureanim.png"),
	GEOSET("Geoset", "Geosets", "geoset.png"),
	GEOSET_ANIM("Geoset Anim", "GeosetAnims", "geoanim.png"),
	BONE("Bone", "Nodes", "bone.png"),
	HELPER("Helper", "Nodes", "helperhand.png"),
	LIGHT("Light", "Nodes", "light.png"),
	ATTACHMENT("Attachment", "Nodes", "attachment.png"),
	PARTICLE_EMITTER("Particle Emitter", "Nodes", "particle1.png"),
	PARTICLE_EMITTER2("Particle Emitter 2", "Nodes", "particle2.png"),
	POPCORN("Popcorn Emitter", "Nodes", "popcorn.png"),
	RIBBON("Ribbon Emitter", "Nodes", "ribbon.png"),
	EVENT_OBJECT("Event Object", "Nodes", "event.png"),
	COLLISION_SHAPE("Collision Shape", "Nodes", "collision.png"),
	CAMERA("Camera", "Cameras", "camera.png");

	public static final String NODES_GROUP = "Nodes";

	private final String displayName;
	private final String groupName;
	private final String iconName;

	ComponentKind(final String displayName, final String groupName, final String iconName) {
		this.displayName = displayName;
		this.groupName = groupName;
		this.iconName = iconName;
	}

	public String getDisplayName() {
		return displayName;
	}

	/** Name of the {@code ChooseableDummyItem} group row in the Model tab tree. */
	public String getGroupName() {
		return groupName;
	}

	public String getIconName() {
		return iconName;
	}

	public boolean isNode() {
		return NODES_GROUP.equals(groupName);
	}

	/**
	 * Classifies a model component. Returns null for things the Model tab shows
	 * but does not treat as a component (header, comment, face effects, bind
	 * pose).
	 */
	public static ComponentKind of(final Object item) {
		if (item instanceof Animation) {
			return SEQUENCE;
		}
		if (item instanceof Integer) {
			return GLOBAL_SEQUENCE;
		}
		if (item instanceof Bitmap) {
			return TEXTURE;
		}
		if (item instanceof Material) {
			return MATERIAL;
		}
		if (item instanceof TextureAnim) {
			return TEXTURE_ANIM;
		}
		if (item instanceof Geoset) {
			return GEOSET;
		}
		if (item instanceof GeosetAnim) {
			return GEOSET_ANIM;
		}
		if (item instanceof Camera) {
			return CAMERA;
		}
		if (item instanceof IdObject) {
			// Helper extends Bone, so it must be tested first
			if (item instanceof Helper) {
				return HELPER;
			}
			if (item instanceof Bone) {
				return BONE;
			}
			if (item instanceof Light) {
				return LIGHT;
			}
			if (item instanceof Attachment) {
				return ATTACHMENT;
			}
			if (item instanceof ParticleEmitter2) {
				return PARTICLE_EMITTER2;
			}
			if (item instanceof ParticleEmitter) {
				return PARTICLE_EMITTER;
			}
			if (item instanceof ParticleEmitterPopcorn) {
				return POPCORN;
			}
			if (item instanceof RibbonEmitter) {
				return RIBBON;
			}
			if (item instanceof EventObject) {
				return EVENT_OBJECT;
			}
			if (item instanceof CollisionShape) {
				return COLLISION_SHAPE;
			}
		}
		return null;
	}

	/** The kind a "New" on the given group row should create by default. */
	public static ComponentKind defaultForGroup(final String groupName) {
		for (final ComponentKind kind : values()) {
			if (kind.groupName.equals(groupName)) {
				return kind;
			}
		}
		return null;
	}
}
