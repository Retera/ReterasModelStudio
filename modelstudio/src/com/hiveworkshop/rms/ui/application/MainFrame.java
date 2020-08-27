package com.hiveworkshop.rms.ui.application;


import com.hiveworkshop.rms.ui.icons.RMSIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

public class MainFrame extends JFrame {
	public static final Image MAIN_PROGRAM_ICON = new ImageIcon(RMSIcons.loadProgramImage("retera.jpg"))
			.getImage();
	public static MainFrame frame;
	public static MainPanel panel;
	public static JMenuBar menuBar;

	public static MainPanel getPanel() {
		return panel;
	}

	public MainFrame(final String title) {
		super(title);

		setBounds(0, 0, 1000, 650);
		panel = new MainPanel();
		setContentPane(panel);
		menuBar = panel.createMenuBar();
		setJMenuBar(menuBar);// MainFrame.class.getResource("ImageBin/DDChicken2.png")
		setIconImage(MAIN_PROGRAM_ICON);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				if (panel.closeAll()) {
					System.exit(0);
				}
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		setLocationRelativeTo(null);
		setVisible(true);
	}

	public static void create(final List<String> startupModelPaths) {
		frame = new MainFrame("Retera Model Studio v0.04.2020.08.09 Nightly Build");
		panel.init();
		if (!startupModelPaths.isEmpty()) {
			for (final String path : startupModelPaths) {
				panel.openFile(new File(path));
			}
		}
	}
}
