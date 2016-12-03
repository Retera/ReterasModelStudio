package com.hiveworkshop.wc3.jworldedit.objects;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.sf.image4j.codec.ico.ICODecoder;

public class ObjectEditorFrame extends JFrame {
	
	UnitEditorPanel panel;
	
	public ObjectEditorFrame() {
		super("Object Editor (JWorldEdit Beta)");
		try {
			List<BufferedImage> images = ICODecoder.read(this.getClass().getResourceAsStream("worldedit.ico"));
			List<BufferedImage> finalImages = new ArrayList<BufferedImage>();
			BufferedImage lastImage = null;
			for( BufferedImage image: images ) {
				if( lastImage != null && image.getWidth() != lastImage.getWidth() ) {
					finalImages.add(lastImage);
				}
				lastImage = image;
			}
			finalImages.add(lastImage);
			setIconImages(finalImages);
		} catch (IOException e) {
			e.printStackTrace();
		}
		setContentPane(panel = new UnitEditorPanel());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		pack();
		setLocationRelativeTo(null);
		//setIconImage(BLPHandler.get().getGameTex(""));
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		ObjectEditorFrame frame = new ObjectEditorFrame();
		frame.setVisible(true);
		frame.panel.loadHotkeys();
	}

}
