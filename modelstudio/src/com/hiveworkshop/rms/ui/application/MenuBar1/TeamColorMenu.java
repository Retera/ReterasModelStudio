package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class TeamColorMenu extends JMenu {

	public TeamColorMenu() {
		super("Team Color");
		setMnemonic(KeyEvent.VK_C);
		getAccessibleContext().setAccessibleDescription("Allows the user to control team color settings.");
		createTeamColorMenuItems();
	}

	public void updateTeamColors() {
		removeAll();
		createTeamColorMenuItems();
	}

	private void createTeamColorMenuItems() {
		for (int i = 0; i < 25; i++) {
			add(getTCMenuItem(i));
		}
	}

	private JMenuItem getTCMenuItem(int teamColor) {
		String colorNumber = ("" + (100 + teamColor)).substring(1);
		JMenuItem menuItem = new JMenuItem(WEString.getString("WESTRING_UNITCOLOR_" + colorNumber), getTCIcon(colorNumber));
		menuItem.setToolTipText("TeamColor" + colorNumber);
		menuItem.addActionListener(e -> setCurrentTeamColor(teamColor));
		return menuItem;
	}

	private ImageIcon getTCIcon(String colorNumber) {
		String iconTexturePath = "ReplaceableTextures\\TeamColor\\TeamColor" + colorNumber + ".blp";
		BufferedImage image = BLPHandler.getImage(iconTexturePath);
		return new ImageIcon(image);
	}

	private void setCurrentTeamColor(int teamColor) {
		ProgramGlobals.getPrefs().setTeamColor(teamColor).saveToFile();
		ModelStructureChangeListener.changeListener.texturesChanged();
	}
}
