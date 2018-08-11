package com.hiveworkshop.wc3.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.Element;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public final class IconUtils {
	private static final String DISABLED_PREFIX = "ReplaceableTextures\\CommandButtonsDisabled\\DIS";

	public static String getDisabledIcon(final String iconPath) {
		String iconName;
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

	public static BufferedImage getIcon(final MutableGameObject gameObject,
			final WorldEditorDataType worldEditorDataType) {
		String iconPath;
		switch (worldEditorDataType) {
		case ABILITIES:
			iconPath = gameObject.getFieldAsString(War3ID.fromString("aart"), 0);
			break;
		case BUFFS_EFFECTS:
			iconPath = gameObject.getFieldAsString(War3ID.fromString("fart"), 0);
			break;
		case DESTRUCTIBLES: {
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
			break;
		}
		case DOODADS: {
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
			break;
		}
		case ITEM:
			iconPath = gameObject.getFieldAsString(War3ID.fromString("iico"), 0);
			break;
		case UPGRADES:
			iconPath = gameObject.getFieldAsString(War3ID.fromString("gar1"), 1);
			break;
		default:
		case UNITS:
			iconPath = gameObject.getFieldAsString(War3ID.fromString("uico"), 0);
			break;
		}
		final BufferedImage gameTex = BLPHandler.get().getGameTex(iconPath);
		return gameTex;
	}

	private IconUtils() {
	}
}
