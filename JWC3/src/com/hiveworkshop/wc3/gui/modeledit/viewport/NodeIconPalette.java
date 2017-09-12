package com.hiveworkshop.wc3.gui.modeledit.viewport;

import java.awt.Image;

public class NodeIconPalette {
	private final Image attachmentImage;
	private final Image eventImage;
	private final Image lightImage;
	private final Image particleImage;
	private final Image particle2Image;
	private final Image ribbonImage;
	private final Image collisionImage;

	public static final NodeIconPalette SELECTED = createInternal(true);
	public static final NodeIconPalette UNSELECTED = createInternal(false);

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

	private static NodeIconPalette createInternal(final boolean isSelected) {
		final Image attachmentImage = IconUtils
				.loadImage("icons/nodes/attachment" + (isSelected ? "Select" : "") + ".png");
		final Image eventImage = IconUtils.loadImage("icons/nodes/event" + (isSelected ? "Select" : "") + ".png");
		final Image lightImage = IconUtils.loadImage("icons/nodes/light" + (isSelected ? "Select" : "") + ".png");
		final Image particleImage = IconUtils
				.loadImage("icons/nodes/particle1" + (isSelected ? "Select" : "") + ".png");
		final Image particle2Image = IconUtils
				.loadImage("icons/nodes/particle2" + (isSelected ? "Select" : "") + ".png");
		final Image ribbonImage = IconUtils.loadImage("icons/nodes/ribbon" + (isSelected ? "Select" : "") + ".png");
		final Image collisionImage = IconUtils
				.loadImage("icons/nodes/collision" + (isSelected ? "Select" : "") + ".png");
		final NodeIconPalette palette = new NodeIconPalette(attachmentImage, eventImage, lightImage, particleImage,
				particle2Image, ribbonImage, collisionImage);
		return palette;
	}
}
