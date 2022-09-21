package com.hiveworkshop.rms.ui.application.windowStuff;

import com.hiveworkshop.rms.ui.icons.RMSIcons;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class TempMainFrame extends JFrame {

	public TempMainFrame(final String title) {
		super(title);

		setBounds(0, 0, 1000, 650);
		TempRootWindow tempRootWindow = new TempRootWindow(null);
		JPanel mainPanel = new JPanel(new MigLayout("fill, ins 0, gap 0, novisualpadding, wrap 1", "[fill, grow]", "[][fill, grow]"));
		TempToolBar tempToolBar = new TempToolBar(tempRootWindow);
		mainPanel.add(tempToolBar);

		mainPanel.add(tempRootWindow);
		setContentPane(mainPanel);
//		setJMenuBar(ProgramGlobals.getMenuBar());
		setIconImage(RMSIcons.MAIN_PROGRAM_ICON);


		setLocationRelativeTo(null);
		setVisible(true);
	}

	public static void main(String[] args){
		new TempMainFrame("Wooop");
	}
}
