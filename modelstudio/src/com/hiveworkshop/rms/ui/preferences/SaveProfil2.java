package com.hiveworkshop.rms.ui.preferences;

import com.hiveworkshop.rms.filesystem.sources.*;
import com.hiveworkshop.rms.ui.preferences.listeners.WarcraftDataSourceChangeListener;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

// this only exists for backwards compatibility to let users load old preference files
// and should be removed when deemed to be unnecessary either do to enough of the user base
// on the new format or do to prioritizing clean code over a slight inconvenience for the users
// twilac 21-04-03
public class SaveProfil2 implements Serializable {
	final static long serialVersionUID = 6L;
	static SaveProfil2 currentProfile;
	String lastDirectory;
	List<String> recent = null;
	ProgramPreference2 preferences;
	private List<DataSourceDescriptor> dataSources;

	private transient WarcraftDataSourceChangeListener dataSourceChangeNotifier = new WarcraftDataSourceChangeListener();
	private transient boolean isHD = false;

	public SaveProfil2() {}

	public List<String> getRecent() {
		if (recent == null) {
			recent = new ArrayList<>();
		}
		return recent;
	}

	public ProgramPreference2 getPreferences() {
		return preferences;
	}

	public List<DataSourceDescriptor> getDataSources() {
		return dataSources;
	}

	public String getPath() {
		return lastDirectory;
	}
}
