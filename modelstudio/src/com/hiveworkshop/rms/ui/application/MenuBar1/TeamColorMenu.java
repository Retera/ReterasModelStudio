package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;

import javax.swing.*;
import java.awt.*;
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
			JMenuItem tcMenuItem = getTCMenuItem(i);
			if (tcMenuItem != null) {
				add(tcMenuItem);
			}
		}
	}

	private JMenuItem getTCMenuItem(int teamColor) {
		String colorNumber = ("" + (100 + teamColor)).substring(1);
		ImageIcon tcIcon = getTCIcon(colorNumber);
		if (tcIcon != null) {
			JMenuItem menuItem = new JMenuItem(WEString.getString("WESTRING_UNITCOLOR_" + colorNumber), tcIcon);
			menuItem.setToolTipText("TeamColor" + colorNumber);
			menuItem.addActionListener(e -> setCurrentTeamColor(teamColor));
			return menuItem;
		}
		return null;
	}

	private ImageIcon getTCIcon(String colorNumber) {
		String iconTexturePath = "ReplaceableTextures\\TeamColor\\TeamColor" + colorNumber + ".blp";
		BufferedImage image = BLPHandler.getImage(iconTexturePath);
		if (image != null) {
			return new ImageIcon(image);
		}
//		image = ImageUtils.getColorImage(getColor(colorNumber));
		return null;
	}

//	private void createTeamColorMenuItems() {
//		for (int i = 0; i < 25; i++) {
//			add(getTCMenuItem(i));
//		}
//	}
//
//	private JMenuItem getTCMenuItem(int teamColor) {
//		String colorNumber = ("" + (100 + teamColor)).substring(1);
//		JMenuItem menuItem = new JMenuItem(WEString.getString("WESTRING_UNITCOLOR_" + colorNumber), getTCIcon(colorNumber));
//		menuItem.setToolTipText("TeamColor" + colorNumber);
//		menuItem.addActionListener(e -> setCurrentTeamColor(teamColor));
//		return menuItem;
//	}
//
//	private ImageIcon getTCIcon(String colorNumber) {
//		String iconTexturePath = "ReplaceableTextures\\TeamColor\\TeamColor" + colorNumber + ".blp";
//		BufferedImage image = BLPHandler.getImage(iconTexturePath);
//		if (image == null) {
//			image = ImageUtils.getColorImage(getColor(colorNumber));
//		}
//		return new ImageIcon(image);
//	}

	private void setCurrentTeamColor(int teamColor) {
		ProgramGlobals.getPrefs().setTeamColor(teamColor).saveToFile();
		ModelStructureChangeListener.changeListener.texturesChanged();
	}

	private Color getColor(String colorNumber) {
		return switch (colorNumber){
			case "00" -> new Color(1.00f, 0.00f, 0.00f); // Red
			case "01" -> new Color(0.00f, 0.05f, 1.00f); // Blue
			case "02" -> new Color(0.01f, 0.80f, 0.48f); // Teal
			case "03" -> new Color(0.09f, 0.00f, 0.22f); // Purple
			case "04" -> new Color(1.00f, 0.98f, 0.00f); // Yellow
			case "05" -> new Color(1.00f, 0.25f, 0.00f); // Orange
			case "06" -> new Color(0.01f, 0.53f, 0.00f); // Green
			case "07" -> new Color(0.78f, 0.10f, 0.43f); // Pink
			case "08" -> new Color(0.30f, 0.31f, 0.31f); // Gray
			case "09" -> new Color(0.21f, 0.51f, 0.89f); // Light Blue
			case "10" -> new Color(0.01f, 0.12f, 0.06f); // DarkGreen
			case "11" -> new Color(0.08f, 0.02f, 0.00f); // Brown
			case "12" -> new Color(0.33f, 0.00f, 0.00f); // Maroon
			case "13" -> new Color(0.00f, 0.00f, 0.55f); // Navy
			case "14" -> new Color(0.08f, 0.83f, 1.00f); // Turqouise
			case "15" -> new Color(0.51f, 0.00f, 1.00f); // Violet
			case "16" -> new Color(0.83f, 0.62f, 0.24f); // Wheat
			case "17" -> new Color(0.94f, 0.37f, 0.26f); // Peach
			case "18" -> new Color(0.51f, 1.00f, 0.22f); // Mint
			case "19" -> new Color(0.72f, 0.48f, 0.83f); // Lavender
			case "20" -> new Color(0.02f, 0.02f, 0.02f); // Coal
			case "21" -> new Color(0.83f, 0.86f, 1.00f); // Snow
			case "22" -> new Color(0.00f, 0.18f, 0.01f); // Emerald
			case "23" -> new Color(0.37f, 0.16f, 0.03f); // Peanut
			case "24" -> new Color(0.00f, 0.00f, 0.00f); // Black
			default -> new Color(1.00f, 0.00f, 0.00f);
		};
	}
}
