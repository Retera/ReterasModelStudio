package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.actionfunctions.CloseModel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.SaveProfileNew;
import com.hiveworkshop.rms.ui.preferences.dataSourceChooser.DataSourceChooserPanel;
import com.hiveworkshop.rms.util.ProgramVersion;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

public class MainFrame extends JFrame {

	public static MainFrame frame;

	public MainFrame(final String title) {
		super(title);

		setBounds(0, 0, 1000, 650);
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		setContentPane(mainPanel);
		setJMenuBar(ProgramGlobals.getMenuBar());
		setIconImage(RMSIcons.MAIN_PROGRAM_ICON);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				exitAction();
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		setLocationRelativeTo(null);
		setVisible(true);
		ProgramGlobals.linkActions(mainPanel);
	}

	public static void close() {
		frame.exitAction();
	}

	private void exitAction() {
		if (CloseModel.closeAll()) {
			System.exit(0);
		}
		getShutdownTimer().start();
	}

	private Timer getShutdownTimer() {
		Timer timer = new Timer(10000, a -> {
			// This is meant to shut down the program process if the Frame has crashed.
			// Not sure if it will work, but it's a bit of an insurance...maybe?
//					System.out.println("isValid: " + MainFrame.this.isValid());
//					System.out.println("isVisible: " + MainFrame.this.isVisible());
//					System.out.println("getGraphics: " + MainFrame.this.getGraphics());
//					System.out.println("getRootPane: " + MainFrame.this.getRootPane());
			if(!MainFrame.this.isVisible()){
				System.exit(0);
			}
		});
		timer.setRepeats(false);
		return timer;
	}

	public static void create(final List<String> startupModelPaths) {
		frame = new MainFrame("Retera Model Studio " + ProgramVersion.get());
		FileDialog fileDialog = new FileDialog();
		if (!startupModelPaths.isEmpty()) {
			for (final String path : startupModelPaths) {
				fileDialog.openFile(new File(path));
			}
		}
	}

	public static void create(final List<String> startupModelPaths, boolean dataPromptForced) {
		create(startupModelPaths);

		if (dataPromptForced || SaveProfileNew.get().getDataSources() == null) {
			DataSourceChooserPanel.showDataSourceChooser(SaveProfileNew.get().getDataSources());
		}
	}
}
