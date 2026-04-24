package com.matrixeater.imp;
import hiveworkshop.localizationmanager.localizationmanager;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.owens.oobjloader.builder.Build;

public class OBJImportSettingsPanel extends JPanel {

	private final JLabel titleLabel;

	public OBJImportSettingsPanel(final Build builder) {
		titleLabel = new JLabel(LocalizationManager.getInstance().get("matrixeater.frame.objimportsettingspanel"));
		final Font smallFont = new Font("Arial",Font.BOLD,16);
		final Font medFont = new Font("Arial",Font.BOLD,28);
		final Font bigFont = new Font("Arial",Font.BOLD,46);
	}

	public static void main(final String[] args) {
		final JFrame testFrame = new JFrame(LocalizationManager.getInstance().get("matrixeater.frame.objimportsettingspanel"));
		testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		testFrame.setContentPane(new OBJImportSettingsPanel(null));
		testFrame.pack();
		testFrame.setLocationRelativeTo(null);
		testFrame.setVisible(true);
	}
}
