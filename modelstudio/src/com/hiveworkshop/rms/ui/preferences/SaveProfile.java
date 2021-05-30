package com.hiveworkshop.rms.ui.preferences;

import com.hiveworkshop.rms.filesystem.sources.*;
import com.hiveworkshop.rms.ui.preferences.listeners.WarcraftDataSourceChangeListener;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SaveProfile implements Serializable {
	final static long serialVersionUID = 6L;
	String lastDirectory;
	static SaveProfile currentProfile;

	List<String> recent = null;
	ProgramPreferences preferences;
	private List<DataSourceDescriptor> dataSources;

	private transient WarcraftDataSourceChangeListener.WarcraftDataSourceChangeNotifier dataSourceChangeNotifier
			= new WarcraftDataSourceChangeListener.WarcraftDataSourceChangeNotifier();
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

	public static SaveProfile get() {
		if (currentProfile == null) {
			try {
				String homeProfile = System.getProperty("user.home");
				File profileDir = new File(homeProfile + getProfilePath());
				File profileFile = getProfileFile(profileDir);
				if (!profileFile.exists()) {
					profileFile = getOldProfileFile(profileDir);
				}

				final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(profileFile));
				currentProfile = (SaveProfile) ois.readObject();
				currentProfile.getPreferences().setNullToDefaults();
				ois.close();

			} catch (final Exception e) {
				Exception e3 = new Exception("Failed to load new style preferences;\nWill try to load as old style!\n", e);
				e3.printStackTrace();
				System.out.println("Will try to load old preferences");
				tryToLoadOldPrefs();
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

	private static void tryToLoadOldPrefs() {
		try {
			String homeProfile = System.getProperty("user.home");
			File profileDir = new File(homeProfile + getProfilePath());
			File profileFile = getProfileFile(profileDir);
			if (!profileFile.exists()) {
				profileFile = getOldProfileFile(profileDir);
			}
			File tempFile = new File(profileFile.getPath() + "_temp");

			if (tempFile.createNewFile()) {
				FileInputStream fis = new FileInputStream(profileFile);
				final FileOutputStream fos = new FileOutputStream(tempFile);
				byte[] bytes = fis.readAllBytes();

				ArrayList<Byte> byteList = new ArrayList<>();
				String prefClass = "ProgramPreferences";
				String saveClass = "SaveProfile";

				for (byte b : bytes) {
					if (byteList.size() < prefClass.length() && b == prefClass.charAt(byteList.size())) {
						byteList.add(b);
					} else if (byteList.size() < saveClass.length() && b == saveClass.charAt(byteList.size())) {
						byteList.add(b);
					} else if (byteList.size() != 0) {
						if (byteList.size() == prefClass.length() || byteList.size() == saveClass.length()) {
							byteList.set(byteList.size() - 1, (byte) '2');
						}
						for (byte b2 : byteList) {
							fos.write(b2);
						}
						byteList.clear();
						fos.write(b);
					} else {
						fos.write(b);
					}
				}
				fos.close();

				final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tempFile));
				SaveProfil2 oldSaveProf = (SaveProfil2) ois.readObject();

				SaveProfile saveProfile = new SaveProfile();
				saveProfile.setPreferences(oldSaveProf.getPreferences().getAsNewPrefs());
				saveProfile.setDataSources(oldSaveProf.getDataSources());
				saveProfile.setPath(oldSaveProf.getPath());
				for (String s : saveProfile.getRecent()) {
					saveProfile.addRecent(s);
				}

				currentProfile = saveProfile;

				ois.close();
				tempFile.delete();
				System.out.println("seems to successfully loaded new setting");
			}
		} catch (Exception e2) {
			System.out.println("failed to load old settings");
			e2.printStackTrace();
		}
	}

	public static void save() {
		if (currentProfile != null) {
			final String homeProfile = System.getProperty("user.home");
			String profilePath = getProfilePath();
			final File profileDir = new File(homeProfile + profilePath);
			profileDir.mkdirs();
			File profileFile = getProfileFile(profileDir);
			System.out.println(profileFile.getPath());
			// profileFile.delete();

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

	public void addDataSourceChangeListener(final WarcraftDataSourceChangeListener listener) {
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

	public static File getProfileFile(File profileDir) {
		File profileFile = new File(profileDir.getPath() + "\\user.profileNew");
		if (!System.getProperty("os.name").toLowerCase().contains("win")) {
			profileFile = new File(profileFile.getPath().replace('\\', '/'));
		}
		return profileFile;
	}

	// To not overwrite old prefs
	// (mostly for twilacs convinience; makes it possible to open old snapshots without specifying the game path)
	public static File getOldProfileFile(File profileDir) {
		File profileFile = new File(profileDir.getPath() + "\\user.profile");
		if (!System.getProperty("os.name").toLowerCase().contains("win")) {
			profileFile = new File(profileFile.getPath().replace('\\', '/'));
		}
		return profileFile;
	}

	private void reload() {
		dataSourceChangeNotifier = new WarcraftDataSourceChangeListener.WarcraftDataSourceChangeNotifier();
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

	public static String getProfilePath() {
		String profilePath = "\\AppData\\Roaming\\ReteraStudioBeta";
		if (!System.getProperty("os.name").toLowerCase().contains("win")) {
			profilePath = "/.reteraStudioBeta";
		}
		return profilePath;
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

	public boolean isHd() {
		return isHD;
	}
}
