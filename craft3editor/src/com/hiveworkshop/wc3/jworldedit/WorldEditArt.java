package com.hiveworkshop.wc3.jworldedit;

import javax.swing.ImageIcon;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.units.DataTable;

public class WorldEditArt {
	private final DataTable worldEditorData;

	public WorldEditArt(final DataTable worldEditorData) {
		this.worldEditorData = worldEditorData;
	}

	public ImageIcon getIcon(final String iconName) {
		String iconTexturePath = worldEditorData.get("WorldEditArt").getField(iconName);
		if (!iconTexturePath.toString().endsWith(".blp")) {
			iconTexturePath += ".blp";
		}
		return new ImageIcon(BLPHandler.get().getGameTex(iconTexturePath));
	}
}
