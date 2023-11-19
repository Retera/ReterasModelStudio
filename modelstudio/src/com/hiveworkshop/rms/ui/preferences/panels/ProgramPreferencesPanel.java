package com.hiveworkshop.rms.ui.preferences.panels;

import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.dataSourceChooser.DataSourceChooserPanel;
import com.hiveworkshop.rms.util.ScreenInfo;

import javax.swing.*;
import java.util.List;

public final class ProgramPreferencesPanel extends JTabbedPane {
	private final DataSourceChooserPanel dataSourceChooserPanel;

	public ProgramPreferencesPanel(JFrame frame,
	                               final ProgramPreferences programPreferences,
	                               final List<DataSourceDescriptor> dataSources) {
		setPreferredSize(ScreenInfo.getSmallWindow());
		addTab("General", new GeneralPrefsPanel(programPreferences));
		addTab("Colors/Theme", new ColorPrefPanel(frame, programPreferences));
		addTab("Hotkeys", new HotkeysPrefsPanel(programPreferences));

		dataSourceChooserPanel = new DataSourceChooserPanel(dataSources);
		addTab("Warcraft Data", dataSourceChooserPanel);
	}


	public List<DataSourceDescriptor> getDataSources() {
		return dataSourceChooserPanel.getDataSourceDescriptors();
	}

}
