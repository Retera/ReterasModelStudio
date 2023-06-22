package com.hiveworkshop.rms.ui.preferences;

import com.hiveworkshop.rms.filesystem.sources.*;
import com.hiveworkshop.rms.ui.preferences.listeners.WarcraftDataSourceChangeListener;

import javax.swing.*;
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
			if(profileFile != null){
				try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(profileFile))) {
					Object loadedObject = ois.readObject();
					ois.close();
					if(loadedObject instanceof SaveProfile){
						currentProfile = (SaveProfile) loadedObject;
						currentProfile.getPreferences().setNullToDefaults();
					} else {
						System.out.println("Will try to load old preferences");
						tryToLoadOldPrefs(profileFile);
					}

				} catch (final Exception e) {
//					e.printStackTrace();
					System.err.println("Failed to load preferences;\nWill try to load preferences from older version!");
					System.out.println("Will try to load old preferences");
					tryToLoadOldPrefs(profileFile);
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

	private static File getProfileFile() {
		String profileDirPath = getProfileDirPath();
		File profileFile = getProfileFile(profileDirPath, "user.profileNew");
		if (!profileFile.exists()) {
			profileFile = getProfileFile(profileDirPath, "user.profile");
		}
		if (profileFile.exists()){
			return profileFile;
		} else {
			return null;
		}
	}

	private static void tryToLoadOldPrefs(File profileFile) {
		byte[] inputBytes = getFixedFileBytes(profileFile);
		if (0 < inputBytes.length) {
			try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(inputBytes))) {
				Object loadedObject = ois.readObject();
				if(loadedObject instanceof SaveProfil2) {
					SaveProfil2 oldSaveProf = (SaveProfil2) loadedObject;

					SaveProfile saveProfile = new SaveProfile();
					saveProfile.setPreferences(oldSaveProf.getPreferences().getAsNewPrefs());
					saveProfile.setDataSources(oldSaveProf.getDataSources());
					saveProfile.setPath(oldSaveProf.getPath());
					for (String s : saveProfile.getRecent()) {
						saveProfile.addRecent(s);
					}

					currentProfile = saveProfile;

					System.out.println("Seems to successfully loaded old preferences");
				}
			} catch (Exception e2) {
				System.err.println("Failed to load old preferences");
//				e2.printStackTrace();
			}
		} else {
			System.err.println("Failed to load old preferences");
		}
	}

	public static void save() {
		if (currentProfile != null) {
			String profileDirPath = getProfileDirPath();
			final File profileDir = new File(profileDirPath);
			profileDir.mkdirs();
			File profileFile = getProfileFile(profileDirPath, "user.profileNew");
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

	private void reload() {
		dataSourceChangeNotifier = new WarcraftDataSourceChangeListener();
		isHD = computeIsHd(dataSources);
	}

	private boolean computeIsHd(final Iterable<DataSourceDescriptor> dataSources) {
		boolean hd = false;
		for (final DataSourceDescriptor desc : dataSources) {
			if (desc instanceof FolderDataSourceDescriptor) {
				if (((FolderDataSourceDescriptor) desc).getFolderPath().contains("_hd.w3mod")) {
					hd = true;
				}
			} else if (desc instanceof CascDataSourceDescriptor) {
				for (final String prefix : ((CascDataSourceDescriptor) desc).getPrefixes()) {
                    if (prefix.contains("_hd.w3mod")) {
                        hd = true;
                        break;
                    }
				}
			} else if (desc instanceof MpqDataSourceDescriptor) {
				if (((MpqDataSourceDescriptor) desc).getMpqFilePath().contains("_hd")) {
					hd = true;
				}
			} else if (desc instanceof CompoundDataSourceDescriptor) {
				hd |= computeIsHd(((CompoundDataSourceDescriptor) desc).getDataSourceDescriptors());
			}
		}
		return hd;
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

	public void addFavorite(final File fp) {
//		getFavorites().remove(fp);
		getFavorites().add(fp);
		save();
	}

	public void removeFromFavorite(final File fp) {
		if (getFavorites().contains(fp)) {
			getFavorites().remove(fp);
			save();
		}
	}

	public boolean isHd() {
		return isHD;
	}

	private static byte[] getFixedFileBytes(File profileFile) {
		// Change occurrences of "ProgramPreferences" and "SaveProfile" in the file content
		// to "ProgramPreference2" and "SaveProfil2" to allow loading of
		// old settings into renamed classes
		byte[] bytes;
		try (FileInputStream fis = new FileInputStream(profileFile)) {
			bytes = fis.readAllBytes();
		} catch (Exception e){
			bytes = new byte[0];
		}

		String prefClassName = "ProgramPreferences";
		String saveClassName = "SaveProfile";

		boolean readingPref = false;
		boolean readingSProf = false;
		int numSavedBytes = 0;

		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			if (readingPref && prefClassName.length() <= numSavedBytes
					|| readingSProf && saveClassName.length() <= numSavedBytes) {
				// text in byteList matches either "ProgramPreferences" or "SaveProfile".
				// Change last letter to a "2"
				bytes[i-1] = (byte) '2';
				numSavedBytes = 0;
				readingPref = false;
				readingSProf = false;
			} else if (readingPref && b == prefClassName.charAt(numSavedBytes)
					|| readingSProf && b == saveClassName.charAt(numSavedBytes)) {
				numSavedBytes++;
			} else {
				numSavedBytes = 0;
				readingPref = false;
				readingSProf = false;
			}

			if (!readingPref && !readingSProf && b == prefClassName.charAt(0)) {
				numSavedBytes = 1;
				readingPref = true;
			} else if (!readingPref && !readingSProf && b == saveClassName.charAt(0)) {
				numSavedBytes = 1;
				readingSProf = true;
			}
		}

		return bytes;
	}

	public static boolean testTargetFolderReadOnly(final String wcDirectory) {
		final File temp = new File(wcDirectory + "war3.mpq");
		final File datat = new File(wcDirectory + "/Data");
		if (!temp.exists() && !datat.exists()) {
			JOptionPane.showMessageDialog(null,
					"Could not find war3.mpq. Please choose a valid Warcraft III installation.",
					"WARNING: Needs WC3 Installation", JOptionPane.WARNING_MESSAGE);
			// requestNewWc3Directory();
			return false;
		}
		return true;
	}
}
