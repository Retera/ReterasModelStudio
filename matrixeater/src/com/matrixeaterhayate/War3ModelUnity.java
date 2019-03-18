package com.matrixeaterhayate;

import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class War3ModelUnity {

	private JFrame frmWarmodelunity;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					final War3ModelUnity window = new War3ModelUnity();
					window.frmWarmodelunity.setVisible(true);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public War3ModelUnity() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmWarmodelunity = new JFrame();
		frmWarmodelunity.setTitle("War3ModelUnity");
		frmWarmodelunity.setIconImage(Toolkit.getDefaultToolkit()
				.getImage(War3ModelUnity.class.getResource("/com/matrixeaterhayate/icons/ATCRune3.png")));
		frmWarmodelunity.setBounds(100, 100, 684, 546);
		frmWarmodelunity.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
