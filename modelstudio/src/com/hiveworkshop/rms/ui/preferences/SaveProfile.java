package com.hiveworkshop.rms.ui.preferences;

import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.ui.preferences.listeners.WarcraftDataSourceChangeListener;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SaveProfile implements Serializable {
	final static long serialVersionUID = 6L;
	String lastDirectory;

	Set<File> favoriteDirectories;
	static SaveProfile currentProfile;

	List<String> recent = null;
	ProgramPreferences preferences;
	private List<DataSourceDescriptor> dataSources;

	private transient WarcraftDataSourceChangeListener dataSourceChangeNotifier = new WarcraftDataSourceChangeListener();
	private transient boolean isHD = false;

	public SaveProfile() {}

	public String getPath() {
		return lastDirectory;
	}

	public ProgramPreferences getPreferences() {
		if (preferences == null) {
			preferences = new ProgramPreferences();
		}
		return preferences;
	}

	public List<DataSourceDescriptor> getDataSources() {
		return dataSources;
	}

	public List<String> getRecent() {
		if (recent == null) {
			recent = new ArrayList<>();
		}
		return recent;
	}

	public Set<File> getFavorites() {
		if (favoriteDirectories == null) {
			favoriteDirectories = new TreeSet<>();
		}
		return favoriteDirectories;
	}
}
