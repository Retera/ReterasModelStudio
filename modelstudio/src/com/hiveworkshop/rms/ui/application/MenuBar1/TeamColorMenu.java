package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;

import javax.swing.*;

public class TeamColorMenu extends JMenu {

	public TeamColorMenu() {
		super("Team Color");
		getAccessibleContext().setAccessibleDescription("Allows the user to control team color settings.");
		createTeamColorMenuItems();
	}

	public void updateTeamColors() {
		removeAll();
		createTeamColorMenuItems();
	}

	private void createTeamColorMenuItems() {
		for (int i = 0; i < 25; i++) {
			String colorNumber = String.format("%2s", i).replace(' ', '0');
			try {
				String colorName = WEString.getString("WESTRING_UNITCOLOR_" + colorNumber);
				String iconTexturePath = "ReplaceableTextures\\TeamColor\\TeamColor" + colorNumber + ".blp";
				ImageIcon icon = new ImageIcon(BLPHandler.getGameTex(iconTexturePath));
				JMenuItem menuItem = new JMenuItem(colorName, icon);
				add(menuItem);
				int teamColor = i;
				menuItem.addActionListener(e -> setCurrentTeamColor(teamColor));
			} catch (Exception e) {
				// load failed
				break;
			}
		}
	}

	private void setCurrentTeamColor(int teamColor) {
		Material.teamColor = teamColor;
//		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
//		if (modelPanel != null) {
//			modelPanel.getAnimationViewer().reloadAllTextures();
//			modelPanel.getPerspArea().reloadAllTextures();
//
////			modelPanel.reloadComponentBrowser();
//		}
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
		ProgramGlobals.getPrefs().setTeamColor(teamColor);
	}
}
