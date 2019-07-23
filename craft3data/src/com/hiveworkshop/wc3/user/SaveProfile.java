package com.hiveworkshop.wc3.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.datachooser.DataSourceDescriptor;
import com.hiveworkshop.wc3.user.WarcraftDataSourceChangeListener.WarcraftDataSourceChangeNotifier;

public class SaveProfile implements Serializable {
	final static long serialVersionUID = 6L;
	String lastDirectory;
	static SaveProfile currentProfile;

	List<String> recent = null;
	ProgramPreferences preferences;
	private List<DataSourceDescriptor> dataSources;

	private transient WarcraftDataSourceChangeNotifier dataSourceChangeNotifier = new WarcraftDataSourceChangeNotifier();

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

	public void addRecent(final String fp) {
		if (!getRecent().contains(fp)) {
			getRecent().add(fp);
		} else {
			getRecent().remove(fp);
			getRecent().add(fp);
		}
		if (recent.size() > 15) {
			recent.remove(0);
		}
		save();
	}

	public ProgramPreferences getPreferences() {
		return preferences;
	}

	public void setPreferences(final ProgramPreferences preferences) {
		this.preferences = preferences;
	}

	public void setDataSources(final List<DataSourceDescriptor> dataSources) {
		this.dataSources = dataSources;
		save();
		dataSourceChangeNotifier.dataSourcesChanged();
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

	public static SaveProfile get() {
		if (currentProfile == null) {
			try {
				final String homeProfile = System.getProperty("user.home");
				String profilePath = "\\AppData\\Roaming\\ReteraStudio";
				if (!System.getProperty("os.name").toLowerCase().contains("win")) {
					profilePath = "/.reteraStudio";
				}
				final File profileDir = new File(homeProfile + profilePath);
				File profileFile = new File(profileDir.getPath() + "\\user.profile");
				if (!System.getProperty("os.name").toLowerCase().contains("win")) {
					profileFile = new File(profileFile.getPath().replace('\\', '/'));
				}
				final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(profileFile));
				currentProfile = (SaveProfile) ois.readObject();
				currentProfile.preferences.reload();
				currentProfile.reload();
				ois.close();
			} catch (final Exception e) {

			}
			if (currentProfile == null) {
				currentProfile = new SaveProfile();
				currentProfile.preferences = new ProgramPreferences();
			}
		}
		return currentProfile;
	}

	private void reload() {
		dataSourceChangeNotifier = new WarcraftDataSourceChangeNotifier();
	}

	public static void save() {
		if (currentProfile != null) {
			final String homeProfile = System.getProperty("user.home");
			String profilePath = "\\AppData\\Roaming\\ReteraStudio";
			if (!System.getProperty("os.name").toLowerCase().contains("win")) {
				profilePath = "/.reteraStudio";
			}
			final File profileDir = new File(homeProfile + profilePath);
			profileDir.mkdirs();
			// System.out.println(profileDir.mkdirs());
			// System.out.println(profileDir);
			File profileFile = new File(profileDir.getPath() + "\\user.profile");
			if (!System.getProperty("os.name").toLowerCase().contains("win")) {
				profileFile = new File(profileFile.getPath().replace('\\', '/'));
			}
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
