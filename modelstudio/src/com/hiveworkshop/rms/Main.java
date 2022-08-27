package com.hiveworkshop.rms;

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.ProgramVersion;
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

	private static void startRealRMS(List<String> startupModelPaths, boolean dataPromptForced){
		try {
			LwjglNativesLoader.load();
		} catch (final Throwable th) {
			th.printStackTrace();
			SwingUtilities.invokeLater(() -> ExceptionPopup.display(th, "Could not load the LWJGL natives:"));
		}

		// Load the JAssetImporter natives.
		tryLoadJAssImp();

		try {
			final ProgramPreferences preferences = SaveProfile.get().getPreferences();
			ThemeLoadingUtils.setTheme(preferences);
		} catch (final Throwable th) {
			th.printStackTrace();
			SwingUtilities.invokeLater(() -> ExceptionPopup.display(th, "Could not set theme:"));
		}

		SwingUtilities.invokeLater(() -> tryStartup(startupModelPaths, dataPromptForced));
		SwingUtilities.invokeLater(Main::setExceptionHandler);
	}

	private static void setExceptionHandler() {
		try {
			Thread.currentThread().setUncaughtExceptionHandler(Main::exceptionCatcher);
		} catch (final Throwable th) {
			th.printStackTrace();
			SwingUtilities.invokeLater(() -> ExceptionPopup.display(th, "Error setting exception handler"));
		}
	}


	private static boolean hasOpenPopup = false;
	private static void exceptionCatcher(Thread thread, Throwable exception) {
		if (!hasOpenPopup) {
			hasOpenPopup = true;
			exception.printStackTrace();
			SwingUtilities.invokeLater(() -> ExceptionPopup.display(exception));
			hasOpenPopup = false;
		}
	}

	private static void tryStartup(List<String> startupModelPaths, boolean dataPromptForced) {
		try {
			MainFrame.create(startupModelPaths, dataPromptForced);
//			justTestFiles(startupModelPaths);
		} catch (final Throwable th) {
			System.err.println("Failed to start");
			th.printStackTrace();
			ExceptionPopup.display(th, "Failed to start RMS:");
		}
	}

	// For testing without opening any GUI.
	public static void justTestFiles(final List<String> startupModelPaths) {
		if (!startupModelPaths.isEmpty()) {
			for (final String path : startupModelPaths) {
				File file = new File(path);
				if (file.exists()) {
					System.out.println("  ~~~  Opening file: " + file + "  ~~~  ");
					ModelLoader.loadFileNoGUI(file);
				}
			}
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
			JOptionPane.showMessageDialog(null, message, "Error " + ProgramVersion.getSurrounded(), JOptionPane.ERROR_MESSAGE);
		}
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
