package com.hiveworkshop.rms.ui.browsers.jworldedit;

import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;

import javax.swing.*;

public class WorldEditArt {
	private final DataTable worldEditorData;

	public WorldEditArt(final DataTable worldEditorData) {
		this.worldEditorData = worldEditorData;
	}

	public ImageIcon getIcon(final String iconName) {
		String iconTexturePath = worldEditorData.get("WorldEditArt").getField(iconName);
		if (!iconTexturePath.endsWith(".blp")) {
			iconTexturePath += ".blp";
		}
		return new ImageIcon(BLPHandler.get().getGameTex(iconTexturePath));
	}
}
