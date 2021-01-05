package com.hiveworkshop.rms.ui.icons;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.Element;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.War3ID;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class IconUtils {
	private static final String DISABLED_PREFIX = "ReplaceableTextures\\CommandButtonsDisabled\\DIS";

	public static String getDisabledIcon(final String iconPath) {
		final String iconName;
		if (iconPath.contains("\\")) {
			iconName = iconPath.substring(iconPath.lastIndexOf('\\') + 1);
		} else {
			iconName = iconPath;
		}
		return DISABLED_PREFIX + iconName;
	}

	public static BufferedImage scale(final BufferedImage img, final int width, final int height) {
		final BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics g = newImage.getGraphics();
		g.drawImage(img, 0, 0, width, height, null);
		g.dispose();
		return newImage;
	}

	public static BufferedImage worldEditStyleIcon(final Image imageUnbuffered) {
		final Image scaledInstance = imageUnbuffered.getScaledInstance(16, 16, Image.SCALE_FAST);
		final BufferedImage bufImage = new BufferedImage(scaledInstance.getWidth(null), scaledInstance.getHeight(null),
				BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics graphics = bufImage.getGraphics();
		graphics.drawImage(scaledInstance, 0, 0, null);
		graphics.dispose();
		for (int x = 0; x < bufImage.getWidth(); x++) {
			for (int y = 0; y < bufImage.getHeight(); y++) {
				final int rgb = bufImage.getRGB(x, y);
				final int alpha = rgb >>> 24;
				if (alpha != 0) {
					bufImage.setRGB(x, y, rgb | 0xFF000000);
				}
			}
		}
		return bufImage;
	}

	public static BufferedImage getIcon(final MutableObjectData.MutableGameObject gameObject,
			final MutableObjectData.WorldEditorDataType worldEditorDataType) {
		String iconPath;
		switch (worldEditorDataType) {
			case ABILITIES -> iconPath = gameObject.getFieldAsString(War3ID.fromString("aart"), 0);
			case BUFFS_EFFECTS -> iconPath = gameObject.getFieldAsString(War3ID.fromString("fart"), 0);
			case DESTRUCTIBLES -> {
				final DataTable unitEditorData = DataTable.getWorldEditorData();
				final String category = gameObject.getFieldAsString(War3ID.fromString("bcat"), 0);
				final Element categories = unitEditorData.get("DestructibleCategories");
				if (categories.hasField(category)) {
					iconPath = categories.getField(category).split(",")[1];
				} else {
					iconPath = "ReplaceableTextures\\WorldEditUI\\DoodadPlaceholder.blp";
				}
				if (!iconPath.toLowerCase().endsWith(".blp")) {
					iconPath += ".blp";
				}
            }
			case DOODADS -> {
				final DataTable unitEditorData = DataTable.getWorldEditorData();
				final String category = gameObject.getFieldAsString(War3ID.fromString("dcat"), 0);
				final Element categories = unitEditorData.get("DoodadCategories");
				if (categories.hasField(category)) {
					iconPath = categories.getField(category, 1);
				} else {
					iconPath = "ReplaceableTextures\\WorldEditUI\\DoodadPlaceholder.blp";
				}
				if (!iconPath.toLowerCase().endsWith(".blp")) {
					iconPath += ".blp";
				}
            }
			case ITEM -> iconPath = gameObject.getFieldAsString(War3ID.fromString("iico"), 0);
			case UPGRADES -> iconPath = gameObject.getFieldAsString(War3ID.fromString("gar1"), 1);
			case UNITS -> iconPath = gameObject.getFieldAsString(War3ID.fromString("uico"), 0);
			default -> iconPath = gameObject.getFieldAsString(War3ID.fromString("uico"), 0);
		}
		final BufferedImage gameTex = BLPHandler.get().getGameTex(iconPath);
		return gameTex;
	}

	public static BufferedImage createBlank(final Color color, final int width, final int height) {
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final Graphics graphics = image.getGraphics();
		graphics.setColor(color);
		graphics.fillRect(0, 0, width, height);
		graphics.dispose();
		return image;
	}

	public static BufferedImage createColorImage(final Vec3 color, final int width, final int height) {
		final Color awtColor = new Color(color.x, color.y, color.z);
		return createBlank(awtColor, width, height);
	}

	private IconUtils() {
	}
}
