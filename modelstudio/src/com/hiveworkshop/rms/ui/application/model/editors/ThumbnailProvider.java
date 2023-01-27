package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class ThumbnailProvider {
	private static ThumbnailProvider defaultProvider;

	private DataSource wrappedDataSource;
	private final HashMap<Integer, HashMap<Bitmap, ImageIcon>> sizeToMap = new HashMap<>();
	private final HashMap<Bitmap, Boolean> validImageMap = new HashMap<>();
	private final BufferedImage noImage;

	public ThumbnailProvider(DataSource wrappedDataSource){
		this.wrappedDataSource = wrappedDataSource;
		noImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = noImage.getGraphics();
		graphics.setColor(new Color(255, 255, 255, 0));
		graphics.fillRect(0, 0, 64, 64);
		graphics.dispose();
	}

	public ImageIcon getImageIcon(Bitmap bitmap, int size) {
		HashMap<Bitmap, ImageIcon> bitmapIconMap = sizeToMap.computeIfAbsent(size, k -> new HashMap<>());
		return bitmapIconMap.computeIfAbsent(bitmap, k -> getNewImageIcon(bitmap, size));
	}

	private ImageIcon getNewImageIcon(Bitmap bitmap, int size) {
		BufferedImage bufferedImage = BLPHandler.getImage(bitmap, wrappedDataSource);
		if (bufferedImage == null || !BLPHandler.isBitmapFound(bitmap)) {
//					System.out.println("could not load icon for \"" + name + "\"");
			bufferedImage = noImage;
			validImageMap.put(bitmap, false);
		} else {
			validImageMap.put(bitmap, true);
		}
		return new ImageIcon(bufferedImage.getScaledInstance(size, size, Image.SCALE_SMOOTH));
	}

	public boolean isValidImage(Bitmap bitmap){
		return validImageMap.get(bitmap);
	}

	public void reload(){
		sizeToMap.clear();
		validImageMap.clear();
	}

	public void updateDataSource(DataSource dataSource){
		if(dataSource == null){
			dataSource = GameDataFileSystem.getDefault();
		}
		if(wrappedDataSource != dataSource){
			wrappedDataSource = dataSource;
			reload();
		}
	}

	private static ThumbnailProvider getDefaultProvider(){
		if(defaultProvider == null){
			defaultProvider = new ThumbnailProvider(GameDataFileSystem.getDefault());
		}
		return defaultProvider;
	}

	public static ThumbnailProvider getThumbnailProvider(EditableModel model) {
		ModelPanel modelPanel = ModelPanel.getModelPanel(model);
		if(modelPanel != null){
			return modelPanel.getThumbnailProvider();
		}
		return getDefaultProvider();
	}
}
