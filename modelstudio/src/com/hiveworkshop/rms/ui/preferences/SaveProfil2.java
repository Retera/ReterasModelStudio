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

	private transient WarcraftDataSourceChangeListener.WarcraftDataSourceChangeNotifier dataSourceChangeNotifier
			= new WarcraftDataSourceChangeListener.WarcraftDataSourceChangeNotifier();
	private transient boolean isHD = false;

	public SaveProfil2() {

	}

	public static SaveProfil2 get() {
		if (currentProfile == null) {
			try {
				System.out.println("save get 1");
				final String homeProfile = System.getProperty("user.home");
				System.out.println("save get 2");
				String profilePath = getProfilePath();
				System.out.println("save get 3");
				final File profileDir = new File(homeProfile + profilePath);
				System.out.println("save get 4");
				File profileFile = getProfileFile(profileDir);
				System.out.println("save get 5");
				final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(profileFile));
				System.out.println("save get 6");
				System.out.println(currentProfile.getPreferences());
//				System.out.println(ois.readFields());
				System.out.println("save get 7");
				currentProfile.preferences.reload();
				System.out.println("save get 8");
				currentProfile.reload();
				System.out.println("save get 9");
				ois.close();
//				System.out.println("Ugg not set: " + currentProfile.getPreferences().ugg);
//				currentProfile.getPreferences().setNullToDefaults();
//				System.out.println("Ugg set: " + currentProfile.getPreferences().ugg);

			} catch (final Exception e) {
				e.printStackTrace();
			}
			if (currentProfile == null) {
				currentProfile = new SaveProfil2();
				currentProfile.preferences = new ProgramPreference2();
			}
		}
		return currentProfile;
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

	public static File getProfileFile(File profileDir) {
		File profileFile = new File(profileDir.getPath() + "\\user2.profile");
		if (!System.getProperty("os.name").toLowerCase().contains("win")) {
			profileFile = new File(profileFile.getPath().replace('\\', '/'));
		}
		return profileFile;
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

	public ProgramPreference2 getPreferences() {
		return preferences;
	}

	public void setPreferences(final ProgramPreference2 preferences) {
		this.preferences = preferences;
	}

	public List<DataSourceDescriptor> getDataSources() {
		return dataSources;
	}

	public void setDataSources(final List<DataSourceDescriptor> dataSources) {
		this.dataSources = dataSources;
		isHD = computeIsHd(dataSources);
		save();
		dataSourceChangeNotifier.dataSourcesChanged();
	}

	public void addDataSourceChangeListener(final WarcraftDataSourceChangeListener listener) {
		dataSourceChangeNotifier.subscribe(listener);
	}

	public String getPath() {
		return lastDirectory;
	}

	public void setPath(final String path) {
		lastDirectory = path;
		save();
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
