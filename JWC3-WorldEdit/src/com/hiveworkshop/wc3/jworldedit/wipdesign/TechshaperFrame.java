package com.hiveworkshop.wc3.jworldedit.wipdesign;

import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.hiveworkshop.wc3.jworldedit.objects.ObjectEditorFrame;
import com.jtattoo.plaf.smart.SmartLookAndFeel;

public class TechshaperFrame extends JFrame {
	private final TechshaperPanel techshaperPanel;

	public TechshaperFrame() {
		super("Techshaper v0.0.1");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		techshaperPanel = new TechshaperPanel();
		setContentPane(techshaperPanel);
		pack();
		setLocationRelativeTo(null);
	}

	public static void main(final String[] args) {
		try {
			// setup the look and feel properties
			final Properties props = new Properties();

			props.put("logoString", "my company");
			props.put("licenseKey", "INSERT YOUR LICENSE KEY HERE");

			props.put("selectionBackgroundColor", "180 240 197");
			props.put("menuSelectionBackgroundColor", "180 240 197");

			props.put("controlColor", "218 254 230");
			props.put("controlColorLight", "218 254 230");
			props.put("controlColorDark", "180 240 197");

			props.put("buttonColor", "218 230 254");
			props.put("buttonColorLight", "255 255 255");
			props.put("buttonColorDark", "244 242 232");

			props.put("rolloverColor", "218 254 230");
			props.put("rolloverColorLight", "218 254 230");
			props.put("rolloverColorDark", "180 240 197");

			props.put("windowTitleForegroundColor", "180 240 197");
			props.put("windowTitleBackgroundColor", "20 20 20");
			props.put("windowTitleColorLight", "150 150 150");
			props.put("windowTitleColorDark", "1 1 1");
			props.put("windowBorderColor", "218 254 230");

			// set your theme
			SmartLookAndFeel.setCurrentTheme(props);
			// select the Look and Feel
			UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");

		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final TechshaperFrame frame = new TechshaperFrame();
				frame.setVisible(true);

				final ObjectEditorFrame frame2 = new ObjectEditorFrame();
				frame2.setVisible(true);
				frame2.loadHotkeys();
			}
		});
	}
}
