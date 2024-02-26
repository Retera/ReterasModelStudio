package com.hiveworkshop.rms.ui.preferences;

import com.hiveworkshop.rms.filesystem.sources.*;
import com.hiveworkshop.rms.ui.preferences.listeners.WarcraftDataSourceChangeListener;

import java.io.*;
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

	public void clearRecent() {
		getRecent().clear();
		save();
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

	public static SaveProfile get() {
		if (currentProfile == null) {
			File profileFile = getProfileFile();
			if (profileFile != null) {
				System.out.println("loading saveProfile from: \"" + profileFile.getPath() + "\"");
				try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(profileFile))) {
					Object loadedObject = ois.readObject();
					ois.close();
					if (loadedObject instanceof SaveProfile) {
						currentProfile = (SaveProfile) loadedObject;
						currentProfile.getPreferences().setNullToDefaults();
					}

				} catch (final Exception e) {
					System.err.println("Failed to load preferences");
				}
			}
			if (currentProfile == null) {
				currentProfile = new SaveProfile();
				currentProfile.preferences = new ProgramPreferences();
			} else if (currentProfile.preferences == null) {
				currentProfile.preferences = new ProgramPreferences();
			}
		}
		return currentProfile;
	}

	private static final String[] savePaths = {"user.profileNew2", "user.profileNew", "user.profile"};
	private static File getProfileFile() {
		String profileDirPath = getProfileDirPath();
		for (String savePath : savePaths) {
			File profileFile = getProfileFile(profileDirPath, savePath);
			if (profileFile.exists()) {
				return profileFile;
			}
		}
		return null;
	}

	public static void save() {
		if (currentProfile != null) {
			String profileDirPath = getProfileDirPath();
			final File profileDir = new File(profileDirPath);
			profileDir.mkdirs();
			File profileFile = getProfileFile(profileDirPath, savePaths[0]);
			System.out.println(profileFile.getPath());

			try {
				profileFile.createNewFile();
				final OutputStream fos = new FileOutputStream(profileFile);
				final ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(get());
				oos.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	public ProgramPreferences getPreferences() {
		return preferences;
	}

	public void setPreferences(final ProgramPreferences preferences) {
		this.preferences = preferences;
	}

	public void setDataSources(final List<DataSourceDescriptor> dataSources) {
		this.dataSources = dataSources;
		isHD = computeIsHd(dataSources);
		save();
//		dataSourceChangeNotifier.dataSourcesChanged();
	}

	public List<DataSourceDescriptor> getDataSources() {
		return dataSources;
	}

	public void addDataSourceChangeListener(final Runnable listener) {
		if (dataSourceChangeNotifier == null) {
			dataSourceChangeNotifier = new WarcraftDataSourceChangeListener();
		}
		dataSourceChangeNotifier.subscribe(listener);
	}

	public SaveProfile() {

	}

	public String getPath() {
		return lastDirectory;
	}

	public void setPath(final String path) {
		lastDirectory = path;
		save();
	}

	public void addRecentSetPath(File file) {
		lastDirectory = file.getParent();
		getRecent().remove(file.getPath());
		getRecent().add(file.getPath());
		if (recent.size() > 15) {
			recent.remove(0);
		}
		save();
	}

	private void reload() {
		dataSourceChangeNotifier = new WarcraftDataSourceChangeListener();
		isHD = computeIsHd(dataSources);
	}

	private boolean computeIsHd(final Iterable<DataSourceDescriptor> dataSources) {
		for (final DataSourceDescriptor desc : dataSources) {
			if (desc instanceof FolderDataSourceDescriptor fDesc && fDesc.getPath().contains("_hd.w3mod")
					|| desc instanceof MpqDataSourceDescriptor mDesc && mDesc.getPath().contains("_hd")
					|| desc instanceof CompoundDataSourceDescriptor compDesc && computeIsHd(compDesc.getDataSourceDescriptors())) {
				return true;
			} else if (desc instanceof CascDataSourceDescriptor cDesc) {
				for (final String prefix : cDesc.getPrefixes()) {
					if (prefix.contains("_hd.w3mod")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static String getProfileDirPath() {
		final String homeProfile = System.getProperty("user.home");
		if (!System.getProperty("os.name").toLowerCase().contains("win")) {
			return homeProfile + "/.reteraStudioBeta";
		}
		return homeProfile + "\\AppData\\Roaming\\ReteraStudioBeta";
	}

	public static File getProfileFile(String dirPath, String fileName) {
		String profilePath = dirPath + "\\" + fileName;
		if (!System.getProperty("os.name").toLowerCase().contains("win")) {
			return new File(profilePath.replace('\\', '/'));
		}
		return new File(profilePath);
	}

	public void addRecent(final String fp) {
		getRecent().remove(fp);
		getRecent().add(fp);
		if (recent.size() > 15) {
			recent.remove(0);
		}
		save();
	}

	public void removeFromRecent(final String fp) {
		if (getRecent().contains(fp)) {
			getRecent().remove(fp);
			save();
		}
	}

	public boolean addFavorite(final File fp) {
		if (getFavorites().add(fp)) {
			save();
			return true;
		}
		return false;
	}

	public boolean removeFromFavorite(final File fp) {
		if (getFavorites().remove(fp)) {
			save();
			return true;
		}
		return false;
	}

	public boolean isHd() {
		return isHD;
	}

}
