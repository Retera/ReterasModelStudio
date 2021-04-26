package com.hiveworkshop.rms;

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPanel;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPanel;
import com.hiveworkshop.rms.ui.preferences.DataSourceChooserPanel;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.ThemeLoadingUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

	public static void main(final String[] args) throws IOException {
		final boolean hasArgs = args.length > 0;
		final List<String> startupModelPaths = new ArrayList<>();
		if ((args.length > 1) && args[0].equals("-convert")) {
			runAsConverter(args[1]);
			return;
		} else if (hasArgs &&
				(args[0].endsWith(".mdx")
						|| args[0].endsWith(".mdl")
						|| args[0].endsWith(".blp")
						|| args[0].endsWith(".dds")
						|| args[0].endsWith(".obj"))) {
			startupModelPaths.addAll(Arrays.asList(args));
		}
		final boolean dataPromptForced = hasArgs && args[0].equals("-forcedataprompt");
		startRealRMS(startupModelPaths, dataPromptForced);
	}

	private static void startRealRMS(List<String> startupModelPaths, boolean dataPromptForced) throws IOException {
		try {
			LwjglNativesLoader.load();

			// Load the jassimp natives.
			tryLoadJAssImp();

			final ProgramPreferences preferences = SaveProfile.get().getPreferences();
			ThemeLoadingUtils.setTheme(preferences);
			setupExceptionHandling();
			SwingUtilities.invokeLater(() -> tryStartup(startupModelPaths, dataPromptForced));
		} catch (final Throwable th) {
			th.printStackTrace();
			SwingUtilities.invokeLater(() -> ExceptionPopup.display(th));
			if (!dataPromptForced) {
				startRealRMS(null, true);
//                main(new String[] {"-forcedataprompt"});
			} else {
				SwingUtilities.invokeLater(() -> startupFailDialog());
			}
		}
	}

	private static void setupExceptionHandling() {
		SwingUtilities.invokeLater(() -> Thread.currentThread().setUncaughtExceptionHandler((thread, exception) -> {
			exception.printStackTrace();
			ExceptionPopup.display(exception);
		}));
	}

	private static void tryStartup(List<String> startupModelPaths, boolean dataPromptForced) {
		try {
			final List<DataSourceDescriptor> dataSources = SaveProfile.get().getDataSources();

			if ((dataSources == null) || dataPromptForced) {
				if (!showDataSourceChooser(dataSources)) return;
			}

//                    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
			MainFrame.create(startupModelPaths);
		} catch (final Throwable th) {
			th.printStackTrace();
			ExceptionPopup.display(th);
			if (!dataPromptForced) {
				new Thread(() -> {
					try {
						startRealRMS(null, true);
//                        main(new String[]{"-forcedataprompt"});
					} catch (final IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}).start();
			} else {
				startupFailDialog();
			}
		}
	}

	private static boolean showDataSourceChooser(List<DataSourceDescriptor> dataSources) {
		final DataSourceChooserPanel dataSourceChooserPanel = new DataSourceChooserPanel(dataSources);

		int opt = JOptionPane.showConfirmDialog(null, dataSourceChooserPanel,
				"Retera Model Studio " + MainFrame.getVersion() + ": Setup", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (opt == JOptionPane.OK_OPTION) {
			SaveProfile.get().setDataSources(dataSourceChooserPanel.getDataSourceDescriptors());
			SaveProfile.save();
			GameDataFileSystem.refresh(SaveProfile.get().getDataSources());

			// cache priority order...
			UnitOptionPanel.dropRaceCache();
			DataTable.dropCache();
			ModelOptionPanel.dropCache();
			WEString.dropCache();
			BLPHandler.get().dropCache();
			return true;
		} else {
			return false;
		}
	}

	private static void tryLoadJAssImp() {
		try {
			final SharedLibraryLoader loader = new SharedLibraryLoader();
			loader.load("jassimp-natives");
		} catch (final Exception e) {
			e.printStackTrace();
			String message =
					"The C++ natives to parse FBX models failed to load. " +
							"You will not be able to open FBX until you install the necessary software" +
							"\nand restart Retera Model Studio." +
							"\n\nMaybe you are missing some Visual Studio Runtime dependency?" +
							"\n\nNext up I will show you the error message that says why " +
							"these C++ jassimp natives failed to load," +
							"\nin case you want to copy them and ask for help. " +
							"Once you press OK on that error popup, you can probably still use" +
							"\nRetera Model Studio just fine for everything else.";
			JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
			ExceptionPopup.display(e);
		}
	}

	private static void startupFailDialog() {
		JOptionPane.showMessageDialog(null,
				"Retera Model Studio startup sequence has failed for two attempts. The program will now exit.",
				"Error", JOptionPane.ERROR_MESSAGE);
		System.exit(-1);
	}

	private static void runAsConverter(final String path) throws IOException {
		final EditableModel model = MdxUtils.loadEditable(new File(path));
		if (path.toLowerCase().endsWith(".mdx")) {
			MdxUtils.saveMdl(model, new File(path.substring(0, path.lastIndexOf('.')) + ".mdl"));
		} else if (path.toLowerCase().endsWith(".mdl")) {
			MdxUtils.saveMdx(model, new File(path.substring(0, path.lastIndexOf('.')) + ".mdx"));
		}
	}
}
