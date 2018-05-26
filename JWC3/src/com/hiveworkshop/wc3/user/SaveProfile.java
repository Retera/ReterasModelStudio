package com.hiveworkshop.wc3.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.user.WarcraftDirectoryChangeListener.WarcraftDirectoryChangeNotifier;

public class SaveProfile implements Serializable {
	final static long serialVersionUID = 6L;
	String lastDirectory;
	static SaveProfile currentProfile;

	String wcDirectory = null;

	List<String> recent = null;
	ProgramPreferences preferences;

	static boolean firstTime = true;
	private static final WarcraftDirectoryChangeNotifier WC3_DIR_CHANGE_NOTIFIER = new WarcraftDirectoryChangeNotifier();

	public String getGameDirectory() {
		if (firstTime) {
			firstTime = false;
			// testTargetFolder(wcDirectory);
		}
		return wcDirectory;
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

	public void setGameDirectory(final String dir) {
		wcDirectory = dir;
		firstTime = true;
		save();
		WC3_DIR_CHANGE_NOTIFIER.directoryChanged();
	}

	public static void addWarcraftDirectoryChangeListener(final WarcraftDirectoryChangeListener listener) {
		WC3_DIR_CHANGE_NOTIFIER.subscribe(listener);
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
				String profilePath = "\\AppData\\Roaming\\JWC3";
				if (!System.getProperty("os.name").toLowerCase().contains("win")) {
					profilePath = "/.jwc3";
				}
				final File profileDir = new File(homeProfile + profilePath);
				File profileFile = new File(profileDir.getPath() + "\\user.profile");
				if (!System.getProperty("os.name").toLowerCase().contains("win")) {
					profileFile = new File(profileFile.getPath().replace('\\', '/'));
				}
				final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(profileFile));
				currentProfile = (SaveProfile) ois.readObject();
				currentProfile.preferences.reload();
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

	public static void save() {
		if (currentProfile != null) {
			final String homeProfile = System.getProperty("user.home");
			String profilePath = "\\AppData\\Roaming\\JWC3";
			if (!System.getProperty("os.name").toLowerCase().contains("win")) {
				profilePath = "/.jwc3";
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

	public static void testTargetFolder(final String wcDirectory) {
		final File temp = new File(wcDirectory + "mod_test_file.txt");
		boolean good = false;
		try {
			good = temp.createNewFile();
			temp.delete();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		if (!good) {
			JOptionPane.showMessageDialog(null,
					"You might not have permissions to access the chosen folder.\nYou should \"Run as Administrator\" on this program, or otherwise gain file permissions to the target folder, for the mod to work optimally.\n\nThe Java WC3 Libraries will permit you to use this folder, however, for read-only purposes.",
					"WARNING: Needs WC3 Installation", JOptionPane.WARNING_MESSAGE);
			// requestNewWc3Directory();
		}
	}

	public static void requestNewWc3Directory() {
		final String autoDir = autoWarcraftDirectory();

		final DirectorySelector selector = new DirectorySelector(autoDir,
				"Welcome to the Java WC3 Libraries! We need to make sure that the program can find your Warcraft III MPQ Archive files if the system is going to work.");
		final int x = JOptionPane.showConfirmDialog(null, selector, "Locating Warcraft III Directory",
				JOptionPane.OK_CANCEL_OPTION);
		if (x == JOptionPane.YES_OPTION) {
			String wcDirectory = selector.getDir();
			if (!(wcDirectory.endsWith("/") || wcDirectory.endsWith("\\"))) {
				wcDirectory = wcDirectory + "\\";
			}
			if (!System.getProperty("os.name").toLowerCase().contains("win")) {
				wcDirectory = wcDirectory.replace('\\', '/');
			}

			testTargetFolder(wcDirectory);

			get().setGameDirectory(wcDirectory);
		}
	}

	public static String getWarcraftDirectory() {
		if (get().getGameDirectory() == null) {
			requestNewWc3Directory();
		}
		return get().getGameDirectory();
	}

	public static String autoWarcraftDirectory() {
		String wcDirectory = WindowsRegistry
				.readRegistry("HKEY_CURRENT_USER\\Software\\Blizzard Entertainment\\Warcraft III", "InstallPathX");
		if (wcDirectory == null) {
			wcDirectory = WindowsRegistry
					.readRegistry("HKEY_CURRENT_USER\\Software\\Blizzard Entertainment\\Warcraft III", "InstallPathX");
		}
		if (wcDirectory == null) {
			wcDirectory = WindowsRegistry.readRegistry(
					"HKEY_CURRENT_USER\\Software\\Classes\\VirtualStore\\MACHINE\\SOFTWARE\\Wow6432Node\\Blizzard Entertainment\\Warcraft III",
					"InstallPath");
		}
		if (wcDirectory == null) {
			JOptionPane.showMessageDialog(null,
					"Error retrieving Warcraft III game directory.\nIs Warcraft III improperly installed on this machine?");
			wcDirectory = System.getProperty("user.home");
			if (wcDirectory == null) {
				wcDirectory = "C:\\";
			}
		}
		wcDirectory = wcDirectory.replace("\n", "").replace("\r", "");
		if (!(wcDirectory.endsWith("/") || wcDirectory.endsWith("\\"))) {
			// legacyFix(wcDirectory);
			wcDirectory = wcDirectory + "\\";
		}
		System.out.println("WC3: " + wcDirectory);
		return wcDirectory;
	}
}
