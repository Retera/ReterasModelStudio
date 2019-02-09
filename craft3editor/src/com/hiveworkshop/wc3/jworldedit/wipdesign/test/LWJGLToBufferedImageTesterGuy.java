package com.hiveworkshop.wc3.jworldedit.wipdesign.test;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.lwjgl.LWJGLUtil;

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.hiveworkshop.wc3.gui.modeledit.MDLSnapshot;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.UnitOptionPane;

import de.wc3data.stream.BlizzardDataInputStream;

public class LWJGLToBufferedImageTesterGuy {
	public static void main(final String[] args) {
		LwjglNativesLoader.load();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final ClassNotFoundException e2) {
			e2.printStackTrace();
		} catch (final InstantiationException e2) {
			e2.printStackTrace();
		} catch (final IllegalAccessException e2) {
			e2.printStackTrace();
		} catch (final UnsupportedLookAndFeelException e2) {
			e2.printStackTrace();
		}

		MDL model;
		ModelViewManager mdlDisplay;
		try {
			final GameObject unit = UnitOptionPane.show(null);
			String field = unit.getField("file");
			if (field.endsWith(".mdl")) {
				field = field.replace(".mdl", ".mdx");
			} else {
				field += ".mdx";
			}
			System.out.println(field);
			model = new MDL(
					MdxUtils.loadModel(new BlizzardDataInputStream(MpqCodebase.get().getResourceAsStream(field))));
			mdlDisplay = MDLSnapshot.createDefaultDisplay(unit);
			JOptionPane.showMessageDialog(null, new UnitOutlinePanel(unit));
		} catch (final IOException e1) {
			throw new RuntimeException(e1);
		}
		System.out.println(model.getHeaderName());
		try {
			final MDLSnapshot mdlSnapshot = new MDLSnapshot(mdlDisplay, 32, 32, null);
			mdlSnapshot.zoomToFit();
			final BufferedImage bufferedImage = mdlSnapshot.getBufferedImage();
			// JOptionPane.showMessageDialog(null, new
			// ImageIcon(bufferedImage));
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
