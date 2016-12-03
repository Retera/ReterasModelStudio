package com.hiveworkshop.wc3.jworldedit.models;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.sf.image4j.codec.ico.ICODecoder;

import com.hiveworkshop.wc3.jworldedit.objects.ObjectEditorFrame;

public class ObjectEditorModelingFrame extends JFrame {
	
	UnitEditorModelSelector panel;
	
	public ObjectEditorModelingFrame() {
		super("Object Editor - Model Selection");
		try {
			List<BufferedImage> images = ICODecoder.read(ObjectEditorFrame.class.getResourceAsStream("worldedit.ico"));
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
		JPanel overPanel = new JPanel();
		panel = new UnitEditorModelSelector();
		JButton ok = new JButton("OK!");
		JButton cancel = new JButton("Cancel");
		overPanel.add(panel);
		overPanel.add(cancel);
		overPanel.add(ok);
		GroupLayout layout = new GroupLayout(overPanel);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addComponent(panel)
				.addGroup(layout.createSequentialGroup()
						.addComponent(cancel) 
						.addComponent(ok) )
				);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(panel)
				.addGroup(layout.createParallelGroup()
						.addComponent(cancel) 
						.addComponent(ok) )
				);
		overPanel.setLayout(layout);
		setContentPane(overPanel);
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
		
		ObjectEditorModelingFrame frame = new ObjectEditorModelingFrame();
		frame.setVisible(true);
		frame.panel.loadHotkeys();
	}

}
