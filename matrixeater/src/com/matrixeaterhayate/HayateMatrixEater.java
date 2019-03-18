package com.matrixeaterhayate;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.hiveworkshop.wc3.gui.BLPHandler;

public class HayateMatrixEater extends JPanel {

	public static void main(final String[] args) {
		final JFrame frame = new JFrame("New Hayate Matrix Eater");
		final HayateMatrixEater hayateMatrixEater = new HayateMatrixEater();
		frame.setIconImage(BLPHandler.get().getGameTex("Textures\\Blue_star2.blp"));
		frame.setContentPane(hayateMatrixEater);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}
