package com.hiveworkshop.rms.ui.util;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class FbxLoadingInfo {
	final static float giga = 1000000000;
	final static float mega = 1000000;
	final static float kilo = 1000;
	Timer timer;
	JLabel timeLabel = new JLabel("00000000");
	JFrame frame;
	long initMillis;
	long startMillis;
	File file;
	long timeEst;
	String fileSize;

	public FbxLoadingInfo(File file){
		this.file = file;
		timeEst = getTimeEst();
		long fSize = file.length();
		if (giga < fSize) {
			fileSize = (getRoundedValue(fSize, giga)) + " GB";
		} else if (mega < fSize) {
			fileSize = (getRoundedValue(fSize, mega)) + " MB";
		} else if (kilo < fSize) {
			fileSize = (getRoundedValue(fSize, kilo)) + " kB";
		} else {
			fileSize = fSize + " B";
		}
	}

	float getRoundedValue(long v, float si){
		return Math.round(v / si * 100)/100f;
	}

	long getTimeEst(){
		long fileSize = file.length();
		System.out.println("File size: " + fileSize + " bytes");
		int kbSize = (int) (fileSize / 1000);
		double d1 = 10;
		double d2 = 0.1 * kbSize;
		double d22 = 0.0001 * kbSize;
		double d3 = 2.5 * d22 * d22;
		double d4 = d1 + d2 + d3;

		return (long) ((int) d4);
	}

	public JPanel getInfoPanel (File file) {
		JPanel panel = new JPanel(new MigLayout());
		panel.add(new JLabel("Opening \"" + file.getName() + "\""), "spanx, wrap");
		panel.add(new JLabel("Size: "), "");
		panel.add(new JLabel(fileSize), "wrap");
		panel.add(new JLabel("Est time left: "), "");

		panel.add(timeLabel);

		return panel;
	}

	public JFrame getFrame() {
//		frame = new JFrame("Opening FBX");
		frame = new JFrame("Opening " + file.getName());
		frame.setContentPane(getInfoPanel(file));
		frame.setLocationRelativeTo(ProgramGlobals.getMainPanel());
		frame.pack();
		timer = new Timer(200, e -> timeLabel.setText("" + ((int)((initMillis - System.currentTimeMillis())/1000))));
		return frame;
	}

	public void start(){
		getFrame().setVisible(true);
		startMillis = System.currentTimeMillis();
		initMillis = System.currentTimeMillis() + getTimeEst();
		timeLabel.setText("" + ((int)((initMillis - System.currentTimeMillis())/1000)));


		LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startMillis), ZoneId.systemDefault());
		LocalDateTime localDateTime2 = LocalDateTime.ofInstant(Instant.ofEpochMilli(initMillis), ZoneId.systemDefault());
//		System.out.println("(" + localDateTime.toLocalTime().truncatedTo(ChronoUnit.SECONDS) + ") " + "est load time: " + timeEst + "s" + "(~"+ localDateTime2.toLocalTime().truncatedTo(ChronoUnit.SECONDS) + ")");
		System.out.println("(" + localDateTime.toLocalTime().truncatedTo(ChronoUnit.MILLIS) + ") " + "est load time: " + timeEst + "ms" + "(~"+ localDateTime2.toLocalTime().truncatedTo(ChronoUnit.MILLIS) + ")");

		timer.start();
	}

	public void stop(){
		timer.stop();
		System.out.println("took " + (System.currentTimeMillis() - startMillis) + "ms to load \"" + file.getName() + "\"");
		frame.dispose();
	}
}
