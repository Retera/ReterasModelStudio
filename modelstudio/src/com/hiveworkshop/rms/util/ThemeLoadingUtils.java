package com.hiveworkshop.rms.util;

import com.hiveworkshop.rms.ui.preferences.GUITheme;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.jtattoo.plaf.acryl.AcrylLookAndFeel;
import com.jtattoo.plaf.aluminium.AluminiumLookAndFeel;
import com.jtattoo.plaf.hifi.HiFiLookAndFeel;
import com.jtattoo.plaf.noire.NoireLookAndFeel;
import net.infonode.gui.laf.InfoNodeLookAndFeel;
import net.infonode.gui.laf.InfoNodeLookAndFeelTheme;
import net.infonode.gui.laf.InfoNodeLookAndFeelThemes;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import java.awt.*;
import java.util.Properties;
import java.util.function.Consumer;

public class ThemeLoadingUtils {
	public static void setTheme(ProgramPreferences preferences) {
		setTheme(preferences.getTheme());
	}
	public static void setTheme(GUITheme theme) {
		switch (theme) {
			case JAVA_DEFAULT       -> trySetTheme(new MetalLookAndFeel(), properties -> MetalLookAndFeel.setCurrentTheme(new OceanTheme()));
			case SOFT_GRAY          -> trySetTheme(new InfoNodeLookAndFeel(InfoNodeLookAndFeelThemes.getSoftGrayTheme()), null);
			case BLUE_ICE           -> trySetTheme(new InfoNodeLookAndFeel(InfoNodeLookAndFeelThemes.getBlueIceTheme()), null);
			case DARK_BLUE_GREEN    -> trySetTheme(new InfoNodeLookAndFeel(InfoNodeLookAndFeelThemes.getDarkBlueGreenTheme()), null);
			case GRAY               -> trySetTheme(new InfoNodeLookAndFeel(InfoNodeLookAndFeelThemes.getGrayTheme()), null);
			case DARK               -> trySetTheme(new NoireLookAndFeel(), NoireLookAndFeel::setCurrentTheme);
			case HIFI               -> trySetTheme(new HiFiLookAndFeel(), HiFiLookAndFeel::setCurrentTheme);
			case ACRYL              -> trySetTheme(new AcrylLookAndFeel(), AcrylLookAndFeel::setCurrentTheme);
			case ALUMINIUM          -> trySetTheme(new AluminiumLookAndFeel(), AluminiumLookAndFeel::setCurrentTheme);
			case FOREST_GREEN       -> trySetTheme(new InfoNodeLookAndFeel(getRmsTheme()), null);
			case WINDOWS            -> trySetTheme(UIManager.getSystemLookAndFeelClassName());
			case WINDOWS_CLASSIC    -> trySetTheme("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
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


	private static void trySetTheme(LookAndFeel lookAndFeel, Consumer<Properties> propConsumer) {
		if(propConsumer != null){
			final Properties props = new Properties();
			props.put("logoString", "RMS");
			propConsumer.accept(props);
		}
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (final Exception exc) {
			setSystemLookAndFeel();
			exc.printStackTrace();
		}
	}

	private static void trySetTheme(String themeString) {
		try {
//			UIManager.put("desktop", new ColorUIResource(Color.WHITE));
			UIManager.setLookAndFeel(themeString);
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
