package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import java.awt.Image;

import com.hiveworkshop.rms.ui.icons.RMSIcons;

public class NodeIconPalette {
	private final Image attachmentImage;
	private final Image eventImage;
	private final Image lightImage;
	private final Image particleImage;
	private final Image particle2Image;
	private final Image ribbonImage;
	private final Image collisionImage;

	public static final NodeIconPalette SELECTED = createInternal("Select");
	public static final NodeIconPalette UNSELECTED = createInternal("");
	public static final NodeIconPalette HIGHLIGHT = createInternal("Highlight");

	public NodeIconPalette(final Image attachmentImage, final Image eventImage, final Image lightImage,
			final Image particleImage, final Image particle2Image, final Image ribbonImage,
			final Image collisionImage) {
		this.attachmentImage = attachmentImage;
		this.eventImage = eventImage;
		this.lightImage = lightImage;
		this.particleImage = particleImage;
		this.particle2Image = particle2Image;
		this.ribbonImage = ribbonImage;
		this.collisionImage = collisionImage;
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
		final NodeIconPalette palette = new NodeIconPalette(attachmentImage, eventImage, lightImage, particleImage,
				particle2Image, ribbonImage, collisionImage);
		return palette;
	}
}
