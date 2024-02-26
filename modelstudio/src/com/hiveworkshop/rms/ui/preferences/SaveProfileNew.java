package com.hiveworkshop.rms.ui.preferences;

import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.ui.preferences.listeners.WarcraftDataSourceChangeListener;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

public class SaveProfileNew {
	private static SaveProfileNew currentProfile;

	private String lastDirectory;
	private FileListTracker recent;

	private FileListTracker favoriteDirectories;
	private DataSourceTracker dataSources;

	private transient ProgramPreferences preferences;

	private transient WarcraftDataSourceChangeListener dataSourceChangeNotifier = new WarcraftDataSourceChangeListener();
	private transient boolean isHD = false;

	private static final String[] oldSavePaths = {"user.profileNew2", "user.profileNew", "user.profile"};
	private static final String trms_prefs_fn = "TRMS_settings.txt";
	private static final String trms_files_fn = "TRMS_file_paths.txt";


	public static SaveProfileNew get() {
		if (currentProfile == null) {
			File dirPath = getSettingsDirPath();
			File pathsFile = new File(dirPath, trms_files_fn);
			if (pathsFile.exists()) {
				String pathsString = readStringFrom(pathsFile);
				if (!pathsString.isBlank()) {
					currentProfile = new SaveProfileNew();
					currentProfile.fromString(pathsString);
				}
			} else {
				File oldProfileFile = getOldProfileFile();
				if (oldProfileFile != null) {
					currentProfile = tryGetFromSerialized(oldProfileFile);
				}
			}
			if (currentProfile == null) {
				currentProfile = new SaveProfileNew();
			}
			currentProfile.getPreferences().setNullToDefaults();

			File prefsFile = new File(getSettingsDirPath(), trms_prefs_fn);
			if (prefsFile.exists()) {
				String prefsString = readStringFrom(prefsFile);
				if (!prefsString.isBlank()) {
					currentProfile.getPreferences().fromString(prefsString);
				}
			}
		}
		return currentProfile;
	}

	public SaveProfileNew set(SaveProfileNew saveProfile) {
		lastDirectory = saveProfile.lastDirectory;
		getPreferences().setFromOther(saveProfile.getPreferences());

		for (File file : saveProfile.getFavorites().getFiles()) {
			getFavorites().add(file);
		}

		for (File file : saveProfile.getRecent().getFiles()) {
			getRecent().add(file);
		}
		getDataSources2().addAll(saveProfile.getDataSources2().getDataSourceDescriptors());
		return this;
	}

	private static SaveProfileNew tryGetFromSerialized(File profileFile) {
		System.out.println("loading saveProfile from: \"" + profileFile.getPath() + "\", " + profileFile.exists());
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(profileFile))) {
			Object loadedObject = ois.readObject();
			if (loadedObject instanceof SaveProfileNew saveProfileNew) {
				return saveProfileNew;
			} else if (loadedObject instanceof SaveProfile saveProfile) {
				return new SaveProfileNew().setFromOldPrefs(saveProfile);
			}

		} catch (final Exception e) {
//			e.printStackTrace();
			System.out.println("Failed to load serialized preferences!");
		}
		return null;
	}


	private static String readStringFrom(File file) {
		if (file != null && file.exists()) {
			System.out.println("reading file: " + file);
			try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
				StringBuilder sb = new StringBuilder();
				r.lines().forEach(l -> sb.append(l).append("\n"));
				return sb.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	private static File getOldProfileFile() {
		File profileDirPath = getSettingsDirPath();
		for (String savePath : oldSavePaths) {
			File profileFile = new File(profileDirPath, savePath);
			if (profileFile.exists()) {
				return profileFile;
			}
		}
		return null;
	}

	private SaveProfileNew setFromOldPrefs(SaveProfile oldSaveProf) {
		if (oldSaveProf != null) {
			getPreferences().setFromOther(oldSaveProf.getPreferences());
			setDataSources(oldSaveProf.getDataSources());
			setPath(oldSaveProf.getPath());
			for (String s : oldSaveProf.getRecent()) {
				addRecent(s);
			}

			System.out.println("Seems to successfully loaded old preferences");
		}
		return this;
	}

	public static void save() {
		if (currentProfile != null) {
			currentProfile.save2();
		}
	}

	public void save2() {
		System.out.println("saving saveProfile!");
		File profileDir = getSettingsDirPath();
		if (profileDir.canWrite()) {
			profileDir.mkdirs();

			String saveString = toSaveString();
			writeToFile(saveString, new File(profileDir, trms_files_fn));

			String prefString = getPreferences().toString();
			writeToFile(prefString, new File(profileDir, trms_prefs_fn));
		}
	}

	private static void writeToFile(String string, File file) {
		System.out.println("Writing: " + file.getPath());
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			outputStream.write(string.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ProgramPreferences getPreferences() {
		if (preferences == null) {
			preferences = new ProgramPreferences();
		}
		return preferences;
	}

	public void setPreferences(final ProgramPreferences preferences) {
		this.preferences = preferences;
	}


	public void setDataSources(final List<DataSourceDescriptor> dataSources) {
		this.dataSources = new DataSourceTracker(dataSources);
		isHD = this.dataSources.isHd();
//		this.dataSources = dataSources;
//		isHD = computeIsHd(dataSources);
//		save();
//		dataSourceChangeNotifier.dataSourcesChanged();
	}

	public List<DataSourceDescriptor> getDataSources() {
		return dataSources.getDataSourceDescriptors();
	}
	public DataSourceTracker getDataSources2() {
		return dataSources;
	}

	public void addDataSourceChangeListener(final Runnable listener) {
		if (dataSourceChangeNotifier == null) {
			dataSourceChangeNotifier = new WarcraftDataSourceChangeListener();
		}
		dataSourceChangeNotifier.subscribe(listener);
	}

	public SaveProfileNew() {
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
		isHD = dataSources.isHd();
	}

	private static File getSettingsDirPath() {
		final String homeProfile = System.getProperty("user.home");
		if (!System.getProperty("os.name").toLowerCase().contains("win")) {
			return new File(homeProfile + "/.reteraStudioBeta");
		}
		return new File(homeProfile + "\\AppData\\Roaming\\ReteraStudioBeta");
	}

	public void clearRecent() {
		getRecent().clear();
		save();
	}

	public FileListTracker getRecent() {
		if (recent == null) {
			recent = new FileListTracker();
		}
		return recent;
	}

	public void addRecent(final String fp) {
		getRecent().remove(fp);
		getRecent().add(fp);
		if (recent.size() > 15) {
			recent.removeFirst();
		}
		save();
	}

	public void addRecentSetPath(File file) {
		lastDirectory = file.getParent();
		getRecent().remove(file.getPath());
		getRecent().add(file.getPath());
		if (recent.size() > 15) {
			recent.removeFirst();
		}
		save();
	}

	public void removeFromRecent(final String fp) {
		if (getRecent().remove(fp)) {
			save();
		}
	}

	public FileListTracker getFavorites() {
		if (favoriteDirectories == null) {
			favoriteDirectories = new FileListTracker();
		}
		return favoriteDirectories;
	}

	public boolean addFavorite(final File fp) {
		if (getFavorites().add(fp)) {
			getFavorites().sort();
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
	public Collection<File> getFavoritesC() {
		return getFavorites().getFiles();
	}


	public boolean isHd() {
		return isHD;
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


	public void printSaveString() {
		System.out.println("\n\nSaveProfile fields:");
		String saveString = toSaveString();
		System.out.println(saveString);
		System.out.println("\n\n\n");
		fromString(saveString);
//		save();
	}
	public String toSaveString() {
		Field[] declaredFields = SaveProfileNew.class.getDeclaredFields();

		StringBuilder sb = new StringBuilder();
		for (Field field : declaredFields) {
			if (notTransientNorStatic(field)) {
				try {
					String name = field.getName();
					Object o = field.get(this);
//					System.out.println("field: " + name + ", " + o);

					if (o instanceof Collection<?> collection) {
						sb.append(name).append(" = ").append("[\n");
						for (Object e : collection) {
//							System.out.println("\titem " + e.getClass().getSimpleName() + ": " + e);

							if (e instanceof String || e instanceof File) {
								sb.append("\t\"").append(e).append("\",\n");
							} else {
								sb.append("\t").append(e).append(",\n");
							}
						}
						sb.append("];\n");
					} else if (o != null){
						if (o instanceof String || o instanceof File) {
							sb.append(name).append(" = \"").append(o).append("\";\n");
						} else {
							sb.append(name).append(" = ").append(o).append(";\n");
						}
					}

				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return sb.toString();
	}
	public void fromString(String s) {
		Field[] declaredFields = this.getClass().getDeclaredFields();
		TreeMap<Integer, Field> offsetToField = new TreeMap<>();
		for (Field field : declaredFields) {
			if (notTransientNorStatic(field)) {
				String name = field.getName();
				int indexOf = s.indexOf(name + " = ");
				if (indexOf != -1) {
					offsetToField.put(indexOf, field);
				}
			}
		}

//		LinkedHashMap<Field, String> fieldStringsMap = new LinkedHashMap<>();

		for (Integer offs : offsetToField.keySet()) {
			Field field = offsetToField.get(offs);
			String name = field.getName();
			String fieldString = s.strip().split(name + " = ")[1];
			Integer nOffs = offsetToField.higherKey(offs);
			if (nOffs != null) {
				String nextName = offsetToField.get(nOffs).getName();
				fieldString = fieldString.strip().split(nextName + " = ")[0];
			}

//			fieldStringsMap.put(field, fieldString);
			parseField(field, fieldString);
		}

//		System.out.println("\n\nfieldStringsMap: ");
//		for (Field field : fieldStringsMap.keySet()) {
//			System.out.println("" + field.getName() + " = {" + fieldStringsMap.get(field) + "}");
//		}
	}

	private void parseField(Field field, String strip) {
		String s1 = strip.replaceAll("(^\")|(\"?;\\s*$)", "");
//		System.out.println("" + field.getName() + " - " + field.getType() + " [" + s1 +"]");

		try {
			if (field.getType().getSuperclass() == Enum.class) {
				field.set(this, Enum.valueOf((Class<Enum>) field.getType(), s1));

			} else if (field.getType() == Float.class) {
				field.set(this, Float.parseFloat(s1));
			} else if (field.getType() == Integer.class) {
				field.set(this, Integer.parseInt(s1));
			} else if (field.getType() == Boolean.class) {
				field.set(this, Boolean.parseBoolean(s1));
			} else if (field.getType() == String.class) {
				System.out.println("String: " + field.getName() + " [" + s1 +"] from [" + strip + "]");
				field.set(this, s1);
			} else if (field.getType() == File.class) {
				field.set(this, new File(s1.strip()));
			} else if (field.getType() == FileListTracker.class) {
				field.set(this, new FileListTracker().fromString(s1));
			} else if (field.getType() == DataSourceTracker.class) {
				field.set(this, new DataSourceTracker().fromString(s1));
//			} else if (field.getType() == Set.class) {
//				System.out.println("\tSet! " + field.getType());
//				String[] split = s1.split(",\n");
//
////								field.set(this, Color.getColor(s1));
////								field.set(this, Color.decode(s1));
//			} else if (field.getType() == List.class) {
//				System.out.println("\tList! " + field.getType());
////								field.set(this, Color.getColor(s1));
////								field.set(this, Color.decode(s1));
//			} else if (field.getType() == Color.class) {
////								field.set(this, Color.getColor(s1));
////								field.set(this, Color.decode(s1));
//			} else {
//				System.out.println("\tUNKNOWN TYPE, super type: " + field.getType().getSuperclass());
//				System.out.println("\t  getNestHost: " + field.getType().getNestHost());
//				System.out.println("\t  componentType: " + field.getType().componentType());
//				for (Class<?> interf : field.getType().getInterfaces()) {
//					System.out.println("\t\t" + interf.getSimpleName());
//				}

			}
//							field.set(this, s1);

		} catch (Exception e) {
			System.out.println("Failed to parse [" + field.getName() + ", " + field.getType() + "]");
			e.printStackTrace();
		}
	}

	private boolean notTransientNorStatic(Field field) {
		int modifiers = field.getModifiers();
		return !Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers);
	}
}
