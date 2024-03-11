package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.RMSFileChooser;
import com.hiveworkshop.rms.ui.gui.modeledit.TextureListRenderer;
import com.hiveworkshop.rms.ui.preferences.SaveProfileNew;
import com.hiveworkshop.rms.ui.util.TwiList;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.ImageUtils.GU;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.function.Supplier;

public class TextureLoadListPanel extends JPanel{
	private int textSize = 12;
	private int imageSize = 16;
	private final List<Bitmap> bitmaps;
	private final TwiList<Bitmap> bitmapJList;
	private ZoomableImagePreviewPanel comp;

	private RMSFileChooser fileChooser;
	private final Supplier<RMSFileChooser> fileChooserSupplier;
	private final JPanel imageViewerPanel = new JPanel(new BorderLayout());
	DataSource workingDirectory = GameDataFileSystem.getDefault();

	public TextureLoadListPanel(List<Bitmap> bitmaps, int textSize, int imageSize) {
		this(bitmaps, textSize, imageSize, () -> new RMSFileChooser(SaveProfileNew.get()));
	}
	public TextureLoadListPanel(List<Bitmap> bitmaps, int textSize, int imageSize, Supplier<RMSFileChooser> fileChooserSupplier) {
		super(new MigLayout("fill, ins 0", "[grow][grow]", "[grow][]"));
		this.bitmaps = bitmaps;
		this.textSize = textSize;
		this.imageSize = imageSize;
		this.fileChooserSupplier = fileChooserSupplier;
		bitmapJList = new TwiList<>(bitmaps);
		add(getTexturesListPanel(), "growx, growy");
		add(getImageViewerPanel(), "growx, growy, wrap");
	}



	private RMSFileChooser getFileChooser() {
		if (fileChooser == null) {
			fileChooser = fileChooserSupplier.get();
		}
		return fileChooser;
	}


	private JPanel getTexturesListPanel() {
		JPanel texturesListPanel = new JPanel(new MigLayout("fill, ins 0", "[grow]", "[grow][]"));
		texturesListPanel.setBorder(BorderFactory.createTitledBorder("Textures"));
		texturesListPanel.setPreferredSize(this.getSize());

		TextureListRenderer textureListRenderer = new TextureListRenderer(null);
		textureListRenderer.setTextSize(textSize);
		textureListRenderer.setImageSize(imageSize);
		JCheckBox displayPath = new JCheckBox("Display Path");
		displayPath.addActionListener(e -> {
			textureListRenderer.setShowPath(displayPath.isSelected());
			bitmapJList.repaint();
		});

		bitmapJList.setCellRenderer(textureListRenderer);
		bitmapJList.addSelectionListener1(this::onListSelection);
		texturesListPanel.add(new JScrollPane(bitmapJList), "growx, growy, spanx, wrap");

		texturesListPanel.add(displayPath, "");
		texturesListPanel.add(Button.create("Open Images", e -> openImages2(bitmapJList, importImages())));
		return texturesListPanel;
	}
	private void onListSelection(Bitmap bitmap) {
		if (bitmap != null) {
			comp.setImage(getImage(bitmap, workingDirectory));
			comp.resetZoom();
			imageViewerPanel.revalidate();
			imageViewerPanel.repaint();
		}
	}
	private BufferedImage getImage(Bitmap bitmap, DataSource workingDirectory){
		BufferedImage texture = BLPHandler.getImage(bitmap, workingDirectory);
		if(texture != null){
			return texture;
		} else {
			int imageSize = 128;
			final BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2 = image.createGraphics();
			g2.setColor(Color.BLACK);
			int size = imageSize-6;
			GU.drawCenteredSquare(g2, imageSize/2, imageSize/2, size);
			int dist1 = (imageSize - size)/2;
			int dist2 = imageSize-dist1;
			GU.drawLines(g2, dist1, dist1, dist2, dist2, dist1, dist2, dist2, dist1);
//			g2.drawString(exc.getClass().getSimpleName() + ": " + exc.getMessage(), 15, 15);
			return image;
		}
	}

	private void openImages2(TwiList<Bitmap> bitmapJList, Bitmap[] bitmap){
		if(bitmap != null){
			bitmaps.addAll(List.of(bitmap));
			bitmapJList.listSize();
		}
	}

	public Bitmap[] importImages() {
		RMSFileChooser fileChooser = getFileChooser();
//		setFilter(FileDialog.OPEN_TEXTURE);
//		fileChooser.setCurrentDirectory(new File("C:\\Users\\twilac\\Desktop\\WC3\\troubleShootingStuff\\Ironforge Ram Rider\\Ironforge Ram Rider\\"));
		fileChooser.setMultiSelectionEnabled(true);
		final int returnValue = fileChooser.showOpenDialog(getParent());
//		File selectedFile = fileChooser.getSelectedFile();
		File[] selectedFiles = fileChooser.getSelectedFiles();
		fileChooser.setMultiSelectionEnabled(false);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			Bitmap[] bitmaps = new Bitmap[selectedFiles.length];
			for(int i = 0; i<selectedFiles.length; i++){
				bitmaps[i] = new Bitmap(selectedFiles[i].toPath().toString());
			}
			return bitmaps;
		}

		return null;
	}

	private JPanel getImageViewerPanel() {
		imageViewerPanel.setBorder(new TitledBorder(null, "Image Viewer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		comp = new ZoomableImagePreviewPanel(null);
		imageViewerPanel.add(comp);
//		add(imageViewerPanel, "w 50%:95%:95%, growy, wrap");
		return imageViewerPanel;
	}

	public List<Bitmap> getBitmaps() {
		return bitmaps;
	}
}
