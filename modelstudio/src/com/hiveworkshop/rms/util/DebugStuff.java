package com.hiveworkshop.rms.util;

import net.miginfocom.swing.MigLayout;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class DebugStuff {

	public static void printScreenInfo() {
		GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		org.lwjgl.opengl.DisplayMode displayMode = Display.getDisplayMode();
		DisplayMode desktopDisplayMode = Display.getDesktopDisplayMode();

//		environment.getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();

		System.out.println("OpenGL displayMode size: " + displayMode.getWidth() + " x " + displayMode.getHeight());
		System.out.println("OpenGL desktopDisplayMode size: " + desktopDisplayMode.getWidth() + " x " + desktopDisplayMode.getHeight());
		Point centerPoint = environment.getCenterPoint();
		System.out.println("GE center point: " + centerPoint);

		GraphicsDevice[] devices = environment.getScreenDevices();
		for(GraphicsDevice device : devices){
			System.out.println("GD: " + device
					+ ", \nid-string: " + device.getIDstring()
					+ ", \ntype: " + device.getType()
					+ ", \nconfig: " + device.getDefaultConfiguration()
					+ ", \nconfigs: " + device.getConfigurations().length
					+ ", \ndisplayMode: " + device.getDisplayMode()
					+ ", \ndisplayModes: " + device.getDisplayModes().length
					+ ", \nAvailableAcceleratedMemory: " + device.getAvailableAcceleratedMemory()
					+ ""
			);
//			for(DisplayMode dm : device.getDisplayModes()){
//				System.out.println(dm);
//			}
		}
		float yRatio = (float) (displayMode.getHeight() / Toolkit.getDefaultToolkit().getScreenSize().getHeight());

		Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
		System.out.println("Toolkit screenSize: " + defaultToolkit.getScreenSize());
		System.out.println("Toolkit screenDPI: " + defaultToolkit.getScreenResolution());
	}

	public static void showSystemInfoPopup() {

		boolean hasTempDir = new File(System.getProperty("java.io.tmpdir")).exists();

		JPanel panel = new JPanel(new MigLayout("wrap 2, fillx"));
		panel.setPreferredSize(ScreenInfo.getSmallWindow());
		String[] propertiesToCheck = {"os.name", "os.arch", "java.runtime.name", "java.io.tmpdir", "user.home"};

		for(String property : propertiesToCheck){
			panel.add(new JLabel(property + ": "));
			panel.add(new JLabel("" + System.getProperty(property)));
		}

		panel.add(new JLabel("has \"java.io.tmpdir\": "));
		panel.add(new JLabel("" + hasTempDir));

//		panel.add(new JLabel("java.library.path: "), "wrap");
//		JTextArea infoLabel = getMultiLineLabel(("" + System.getProperty("java.library.path")).replaceAll(";", "; "));
//		panel.add(infoLabel, "spanx, growx, wrap");


		panel.add(new JLabel("APP_SANDBOX_CONTAINER_ID:"), "wrap");
		JTextArea infoLabel2 = getMultiLineLabel(("" + System.getenv("APP_SANDBOX_CONTAINER_ID")).replaceAll(";", "; "));
		panel.add(infoLabel2, "span 2");


		JOptionPane.showMessageDialog(null, panel, "Twi-RMS - System Info", JOptionPane.INFORMATION_MESSAGE);
	}

	private static JTextArea getMultiLineLabel(String string) {
		// Will throw if added to parent container whose size is not set
		JTextArea infoLabel2 = new JTextArea(string);
		infoLabel2.setEditable(false);
		infoLabel2.setOpaque(false);
		infoLabel2.setLineWrap(true);
		infoLabel2.setWrapStyleWord(true);
		return infoLabel2;
	}
}
