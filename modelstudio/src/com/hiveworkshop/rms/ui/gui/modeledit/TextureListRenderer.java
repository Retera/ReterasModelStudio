package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class TextureListRenderer extends DefaultListCellRenderer {
	DataSource wrappedDataSource;
	HashMap<Bitmap, ImageIcon> map = new HashMap<>();
	HashMap<Bitmap, Boolean> validImageMap = new HashMap<>();
	Font theFont = new Font("Arial", Font.PLAIN, 18);
	Color orgFgColor = getForeground();
	BufferedImage noImage;
	boolean showPath = false;
	int imageSize = 64;

	public TextureListRenderer(final EditableModel model) {
		this(model.getWrappedDataSource());
	}
	public TextureListRenderer(DataSource wrappedDataSource) {
		this.wrappedDataSource = wrappedDataSource;
		noImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = noImage.getGraphics();
		graphics.setColor(new Color(255, 255, 255, 0));
		graphics.fillRect(0, 0, imageSize, imageSize);
		graphics.dispose();
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean iss, final boolean chf) {
		if(value instanceof Bitmap){
			String text;
			if(showPath){
				text = ((Bitmap) value).getRenderableTexturePath();
			} else {
				text = ((Bitmap) value).getName();
			}
			Color fgColor = orgFgColor;
			ImageIcon myIcon = map.get(value);
			if (myIcon == null) {
				BufferedImage bufferedImage = BLPHandler.getImage(((Bitmap) value), wrappedDataSource);
				if (bufferedImage == null) {
//					System.out.println("could not load icon for \"" + text + "\"");
					bufferedImage = noImage;
					validImageMap.put((Bitmap) value, false);
				} else {
					validImageMap.put((Bitmap) value, true);
				}
				myIcon = new ImageIcon(bufferedImage.getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH));
				map.put((Bitmap) value, myIcon);
			}
			if (!validImageMap.get(value)) {
				fgColor = Color.gray;
			}
			super.getListCellRendererComponent(list, text, index, iss, chf);
			setIcon(myIcon);
			setFont(theFont);
			setForeground(fgColor);
		}

		return this;
	}

	public TextureListRenderer setShowPath(boolean showPath) {
		this.showPath = showPath;
		return this;
	}

	public TextureListRenderer setImageSize(int imageSize) {
		this.imageSize = imageSize;
		map.clear();
		return this;
	}

	public TextureListRenderer setTextSize(int size){
		theFont = new Font("Arial", Font.PLAIN, size);
		return this;
	}
}
