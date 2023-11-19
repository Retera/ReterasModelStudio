package com.hiveworkshop.rms.util;

import com.hiveworkshop.rms.ui.preferences.GUITheme;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.EditorDisplayManager;
import net.infonode.gui.laf.InfoNodeLookAndFeel;
import net.infonode.gui.laf.InfoNodeLookAndFeelTheme;
import net.infonode.gui.laf.InfoNodeLookAndFeelThemes;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;

public class ThemeLoadingUtils {
	public static void setTheme(ProgramPreferences preferences) {
		setTheme(preferences.getTheme());
	}
	public static void setTheme(GUITheme theme) {
		switch (theme) {
			case JAVA_DEFAULT -> {
			}
			case SOFT_GRAY -> trySetTheme(InfoNodeLookAndFeelThemes.getSoftGrayTheme());
			case BLUE_ICE -> trySetTheme(InfoNodeLookAndFeelThemes.getBlueIceTheme());
			case DARK_BLUE_GREEN -> trySetTheme(InfoNodeLookAndFeelThemes.getDarkBlueGreenTheme());
			case GRAY -> trySetTheme(InfoNodeLookAndFeelThemes.getGrayTheme());
			case DARK, HIFI, ACRYL, ALUMINIUM -> EditorDisplayManager.setupLookAndFeel(theme);
			case FOREST_GREEN -> trySetTheme(getRmsTheme());
			case WINDOWS -> {
				try {
					UIManager.put("desktop", new ColorUIResource(Color.WHITE));
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					System.out.println(UIManager.getLookAndFeel());
				} catch (final UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
					// handle exception
				}
			}
			case WINDOWS_CLASSIC -> {
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
				} catch (final Exception exc) {
					setSystemLookAndFeel();
				}
			}
		}
	}

	private static InfoNodeLookAndFeelTheme getRmsTheme() {
//		final InfoNodeLookAndFeelTheme rmsTheme = new InfoNodeLookAndFeelTheme("Retera Studio",
//				controlColor, primaryControlColor, backgroundColor,
//				selectedTextBackgroundColor, selectedTextColor, textColor, shadingFactor);

		final InfoNodeLookAndFeelTheme rmsTheme = new InfoNodeLookAndFeelTheme("Retera Studio",
				new Color(44, 46, 20), new Color(116, 126, 36), new Color(44, 46, 20),
				new Color(220, 202, 132), new Color(116, 126, 36), new Color(220, 202, 132));
		rmsTheme.setShadingFactor(-0.8);
		rmsTheme.setDesktopColor(new Color(60, 82, 44));
		return rmsTheme;
	}

	private static void trySetTheme(InfoNodeLookAndFeelTheme theme) {
		try {
			UIManager.setLookAndFeel(new InfoNodeLookAndFeel(theme));
		} catch (final Exception exc) {
			setSystemLookAndFeel();
			exc.printStackTrace();
		}
	}

	private static void setSystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}
}
