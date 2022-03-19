package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.icons.RMSIcons;

import java.awt.*;

public class NodeIconPalette {
	private final Image attachmentImage;
	private final Image eventImage;
	private final Image lightImage;
	private final Image particleImage;
	private final Image particle2Image;
	private final Image ribbonImage;
	private final Image collisionImage;
	private final Image boneImage;
	private final Image helperImage;

	public static final NodeIconPalette SELECTED = createInternal("Select");
	public static final NodeIconPalette UNSELECTED = createInternal("");
	public static final NodeIconPalette HIGHLIGHT = createInternal("Highlight");

	public NodeIconPalette(final Image attachmentImage, final Image eventImage, final Image lightImage,
	                       final Image particleImage, final Image particle2Image, final Image ribbonImage,
	                       final Image collisionImage, final Image boneImage, final Image helperImage) {
		this.attachmentImage = attachmentImage;
		this.eventImage = eventImage;
		this.lightImage = lightImage;
		this.particleImage = particleImage;
		this.particle2Image = particle2Image;
		this.ribbonImage = ribbonImage;
		this.collisionImage = collisionImage;
		this.boneImage = boneImage;
		this.helperImage = helperImage;

	}

	public Image getAttachmentImage() {
		return attachmentImage;
	}

	public Image getEventImage() {
		return eventImage;
	}

	public Image getLightImage() {
		return lightImage;
	}

	public Image getParticleImage() {
		return particleImage;
	}

	public Image getParticle2Image() {
		return particle2Image;
	}

	public Image getRibbonImage() {
		return ribbonImage;
	}

	public Image getCollisionImage() {
		return collisionImage;
	}

	private static NodeIconPalette createInternal(final String template) {
		final Image attachmentImage = RMSIcons.loadNodeImage("attachment" + template + ".png");
		final Image eventImage = RMSIcons.loadNodeImage("event" + template + ".png");
		final Image lightImage = RMSIcons.loadNodeImage("light" + template + ".png");
		final Image particleImage = RMSIcons.loadNodeImage("particle1" + template + ".png");
		final Image particle2Image = RMSIcons.loadNodeImage("particle2" + template + ".png");
		final Image ribbonImage = RMSIcons.loadNodeImage("ribbon" + template + ".png");
		final Image collisionImage = RMSIcons.loadNodeImage("collision" + template + ".png");
		final Image boneImage = RMSIcons.loadNodeImage("bone.png");
		final Image helperImage = RMSIcons.loadNodeImage("helperhand.png");

		return new NodeIconPalette(attachmentImage, eventImage, lightImage, particleImage,
				particle2Image, ribbonImage, collisionImage, boneImage, helperImage);
	}

	public Image getObjectImage(IdObject object) {
		if (object instanceof Attachment) {
			return attachmentImage;
		} else if (object instanceof EventObject) {
			return eventImage;
		} else if (object instanceof Light) {
			return lightImage;
		} else if (object instanceof ParticleEmitter) {
			return particleImage;
		} else if (object instanceof ParticleEmitter2) {
			return particle2Image;
		} else if (object instanceof RibbonEmitter) {
			return ribbonImage;
		} else if (object instanceof CollisionShape) {
			return collisionImage;
		} else if (object instanceof Helper) {
			return helperImage;
		} else {
			return boneImage;
		}

	}
}
