package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.blp.ImageUtils;
import com.hiveworkshop.rms.ui.browsers.jworldedit.RMSFileChooser;
import com.hiveworkshop.rms.ui.gui.modeledit.TextureListRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.TwiComboBox;
import de.wc3data.image.TgaFile;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TextureCompositionPanel extends JPanel {
//	private final FileDialog fileDialog;
	private int thumbnailSize = 64;
	List<Bitmap> bitmaps = new ArrayList<>();
	ZoomableImagePreviewPanel imagePreviewPanel;
	private ZoomableImagePreviewPanel comp;

	EnumMap<Channel, TextureHolder> channelTextureHolderEnumMap = new EnumMap<>(Channel.class);

	public TextureCompositionPanel(){
		super(new MigLayout("fill", "[grow]", "[grow]"));
//		fileDialog = new FileDialog(this);
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Load Images", getLoadImagesPanel());
		tabbedPane.addTab("Combine Images", getImagePanel());

		add(tabbedPane, "growx, growy");

//		add(getImagePanel(), "growx, growy");
	}

	private JPanel getImagePanel() {
		JPanel imagePanel = new JPanel(new MigLayout("fill", "[][grow]", "[grow]"));
		imagePanel.add(getSubImagesPanel(), "growy");

		JPanel combImagePanel = new JPanel(new MigLayout("fill, ins 0", "[grow]", "[grow][]"));
		combImagePanel.add(getCombImagePanel(), "growx, growy, wrap");
		JButton export = new JButton("Export");
		export.addActionListener(e -> export());
		combImagePanel.add(export);
		imagePanel.add(combImagePanel, "growx, growy");

		return imagePanel;
	}

	private JButton getFileDialogButton1() {
		JButton open_images = new JButton("Open Images");
//		open_images.addActionListener(e -> openImages(fileDialog.importImages()));
		return open_images;
	}
	private JButton getFileDialogButton2(TwiList<Bitmap> bitmapJList) {
		JButton open_images = new JButton("Open Images");
		open_images.addActionListener(e -> openImages2(bitmapJList, importImages()));
		return open_images;
	}

	private JPanel getLoadImagesPanel(){
		JPanel panel = new JPanel(new MigLayout("fill, ins 0", "[grow][grow]", "[grow][]"));
		TwiList<Bitmap> bitmapJList = new TwiList<>(bitmaps);
		panel.add(getTexturesListPanel(bitmapJList), "growx, growy");
		panel.add(getImageViewerPanel(), "growx, growy, wrap");
		panel.add(getFileDialogButton2(bitmapJList));
		return panel;
	}



	private final JPanel imageViewerPanel = new JPanel(new BorderLayout());
	DataSource workingDirectory = GameDataFileSystem.getDefault();

	private JPanel getTexturesListPanel(TwiList<Bitmap> bitmapJList) {
		JPanel texturesListPanel = new JPanel(new MigLayout("fill, ins 0", "[grow]", "[grow][]"));
		texturesListPanel.setBorder(BorderFactory.createTitledBorder("Textures"));
		texturesListPanel.setPreferredSize(this.getSize());

		TextureListRenderer textureListRenderer = new TextureListRenderer(workingDirectory);
		textureListRenderer.setTextSize(12);
		textureListRenderer.setImageSize(32);
		JCheckBox displayPath = new JCheckBox("Display Path");
		displayPath.addActionListener(e -> {
			textureListRenderer.setShowPath(displayPath.isSelected());
			bitmapJList.repaint();
		});

		bitmapJList.setCellRenderer(textureListRenderer);
		bitmapJList.addSelectionListener1(this::onListSelection);
		texturesListPanel.add(new JScrollPane(bitmapJList), "growx, growy, wrap");

		texturesListPanel.add(displayPath, "");
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



	private JPanel getImageViewerPanel() {
		imageViewerPanel.setBorder(new TitledBorder(null, "Image Viewer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		comp = new ZoomableImagePreviewPanel(null);
		imageViewerPanel.add(comp);
//		add(imageViewerPanel, "w 50%:95%:95%, growy, wrap");
		return imageViewerPanel;
	}

	private void openImages(Bitmap[] bitmap){
		if(bitmap != null){
			bitmaps.addAll(List.of(bitmap));
		}
	}
	private void openImages2(TwiList<Bitmap> bitmapJList, Bitmap[] bitmap){
		if(bitmap != null){
			bitmaps.addAll(List.of(bitmap));
			bitmapJList.listSize();
		}
	}

	private JPanel getSubImagesPanel() {
		JPanel subImagesPanel = new JPanel(new MigLayout("fill, ins 0, wrap 2", "[][grow]", ""));
		for (Channel channel : Channel.values()){
			TextureHolder value = channelTextureHolderEnumMap.computeIfAbsent(channel, k -> new TextureHolder(channel, this::updateCombinedImage));
			subImagesPanel.add(getActiveCheckbox(value));
			subImagesPanel.add(getSubImagePanel(channel, value));
		}
		return subImagesPanel;
	}

	private JCheckBox getActiveCheckbox(TextureHolder value) {
		JCheckBox activeCheckbox = new JCheckBox("", true);
		activeCheckbox.addActionListener(e -> value.setActive(activeCheckbox.isSelected()));
		return activeCheckbox;
	}

	private JPanel getSubImagePanel(Channel channel, TextureHolder textureHolder){
		JPanel panel = new JPanel(new MigLayout("ins 0, gap 0"));
		panel.setBorder(BorderFactory.createTitledBorder(channel.getName()));
		TwiComboBox<Bitmap> imageChooser = new TwiComboBox<>(bitmaps, new Bitmap("PrototypePrototype is a prototype"));
		imageChooser.addMouseWheelListener(e -> imageChooser.incIndex(e.getWheelRotation()));

		panel.add(imageChooser, "spanx, wrap");
		panel.add(textureHolder.getLabel(), "growx, growy");
		imageChooser.addOnSelectItemListener(o -> textureHolder.setPath(o));
//		imageChooser.setStringFunctionRender(o -> o == null ? "None" : ((Bitmap)o).getRenderableTexturePath());
		imageChooser.setStringFunctionRender(o -> o == null ? "None" : ((Bitmap)o).getName());

		SmartButtonGroup channelGroup = new SmartButtonGroup();
		channelGroup.setPanelConst("ins 0, gap 0");
		for(Channel channel1 : Channel.values()){
			channelGroup.addJRadioButton(channel1.getName(), e -> textureHolder.setChannel(channel1));
		}
		channelGroup.setSelectedIndex(textureHolder.channel.ordinal());
		panel.add(channelGroup.getButtonPanel(), "");

		JCheckBox invert = new JCheckBox("invert", false);
		invert.addActionListener(e -> textureHolder.setInverted(invert.isSelected()));
		panel.add(invert, "");

		return panel;
	}
	private JPanel getCombImagePanel(){
		imagePreviewPanel = new ZoomableImagePreviewPanel(null);
		imagePreviewPanel.setImage(ImageUtils.getColorImage(Color.GRAY, 512));
		JPanel previewPanel = new JPanel(new MigLayout("fill", "[grow]", "[grow]"));
		previewPanel.setBorder(BorderFactory.createTitledBorder("Previewer"));
//		previewPanel.setLayout(new BorderLayout());
		previewPanel.add(imagePreviewPanel, "growx, growy");
		previewPanel.revalidate();
		return previewPanel;
	}

	private void updateCombinedImage(){
		int width = 0;
		int height = 0;
		for (TextureHolder textureHolder : channelTextureHolderEnumMap.values()){
			if(textureHolder != null){
				width = Math.max(width, textureHolder.getWidth());
				height = Math.max(height, textureHolder.getHeight());
			}
		}
		imagePreviewPanel.setImage(getBufferedImageCombinedChannels(channelTextureHolderEnumMap, width, height));
//		imagePreviewPanel.setImage(getBufferedDeriChannels(channelTextureHolderEnumMap, width, height));
//		imagePreviewPanel.resetZoom();
		imagePreviewPanel.repaint();
	}


	public static void showPanel(JComponent parent) {
		TextureCompositionPanel textureManager = new TextureCompositionPanel();
//		textureManager.setSize(new Dimension(800, 650));
		textureManager.setPreferredSize(new Dimension(800, 650));
		textureManager.revalidate();
//		FramePopup.show(textureManager, ProgramGlobals.getMainPanel(), "Edit Textures");
		FramePopup.show(textureManager, parent, "Edit Textures");
	}

	private class TextureHolder {
		private Channel channel;
		private Bitmap bitmap;
		private boolean isActive = true;
		private boolean isInverted = false;
		private BufferedImage bufferedImage;
		private int width = 0;
		private int height = 0;
		private ImageIcon currImageIcon = new ImageIcon(ImageUtils.getColorImage(Color.DARK_GRAY).getScaledInstance(thumbnailSize, thumbnailSize, Image.SCALE_DEFAULT));
		private JLabel label = new JLabel(currImageIcon);
		private Runnable updater;

		public TextureHolder(Channel channel, Runnable updater){
			this.channel = channel;
			this.updater = updater;
		}

		public TextureHolder setChannel(Channel channel) {
			this.channel = channel;
			if(bufferedImage != null){
				setBufferedImage(bufferedImage);
			}
			updater.run();
			return this;
		}

		public Channel getChannel() {
			return channel;
		}

		public TextureHolder setPath(Bitmap path) {
			this.bitmap = path;
//			if(ProgramGlobals.getCurrentModelPanel() != null){
//				setBufferedImage(BLPHandler.getImage(bitmap, ProgramGlobals.getCurrentModelPanel().getModel().getWrappedDataSource()));
//			} else {
//				setBufferedImage(BLPHandler.getImage(bitmap, GameDataFileSystem.getDefault()));
//			}
			setBufferedImage(BLPHandler.getImage(bitmap, GameDataFileSystem.getDefault()));
			return this;
		}

		public Bitmap getBitmap() {
			return bitmap;
		}

		public TextureHolder setActive(boolean active) {
			isActive = active;
			updater.run();
			return this;
		}

		public TextureHolder setInverted(boolean inverted) {
			isInverted = inverted;
			updater.run();
			return this;
		}

		public TextureHolder setBufferedImage(BufferedImage bufferedImage) {
			this.bufferedImage = bufferedImage;
			if(bufferedImage != null){
				width = bufferedImage.getWidth();
				height = bufferedImage.getHeight();
				currImageIcon.setImage(getBufferedImageIsolateChannel(bufferedImage, channel).getScaledInstance(thumbnailSize, thumbnailSize, Image.SCALE_DEFAULT));
			} else {
				width = 0;
				height = 0;
			}
			updater.run();
			label.repaint();
			return this;
		}

		public BufferedImage getBufferedImage() {
			return bufferedImage;
		}

		public boolean isActive() {
			return isActive;
		}

		public boolean isInverted() {
			return isInverted;
		}

		public JLabel getLabel() {
			return label;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}
	}

	private enum Channel {
		RED("Red"),
		GREEN("Green"),
		BLUE("Blue"),
		ALPHA("Alpha");

		private final String name;

		Channel(String name){
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public BufferedImage getBufferedImageIsolateChannel(BufferedImage bufferedImage, Channel channel) {
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		WritableRaster sourceRaster = bufferedImage.getRaster();

		BufferedImage channelImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster channelImageRaster = channelImage.getRaster();


		float[] srcPixelData = new float[sourceRaster.getNumBands()];
		float[] pixelData = new float[4];
		int[] dataBands = getRGBADataBands(bufferedImage);

		for(int h = 0; h<height; h++){
			for(int w = 0; w<width; w++){
				sourceRaster.getPixel(w, h, srcPixelData);

				switch (channel){
					case RED -> fillRasterData(pixelData, srcPixelData[dataBands[0]], srcPixelData[dataBands[0]], srcPixelData[dataBands[0]], 255.0f);
					case GREEN -> fillRasterData(pixelData, srcPixelData[dataBands[1]], srcPixelData[dataBands[1]], srcPixelData[dataBands[1]], 255.0f);
					case BLUE -> fillRasterData(pixelData, srcPixelData[dataBands[2]], srcPixelData[dataBands[2]], srcPixelData[dataBands[2]], 255.0f);
					case ALPHA -> fillRasterData(pixelData, srcPixelData[dataBands[3]], srcPixelData[dataBands[3]], srcPixelData[dataBands[3]], 255.0f);
//					case RGB -> fillRasterData(pixelData, srcPixelData[dataBands[0]], srcPixelData[dataBands[1]], srcPixelData[dataBands[2]], 255.0f);
				}
				channelImageRaster.setPixel(w, h, pixelData);
			}
		}
		channelImage.setData(channelImageRaster);

		return channelImage;
	}

	public BufferedImage getBufferedImageCombinedChannels(Map<Channel, TextureHolder> channelMap, int width, int height) {

		WritableRaster[] sourceRasters = new WritableRaster[4];
		Channel[] srcChannels = new Channel[4];
		boolean[] srcInvert = new boolean[4];
		for(Channel channel : Channel.values()){
			TextureHolder textureHolder = channelMap.get(channel);
			if(textureHolder.isActive && textureHolder.getBufferedImage() != null){
				sourceRasters[channel.ordinal()] = textureHolder.getBufferedImage().getRaster();
			}
			srcChannels[channel.ordinal()] = textureHolder.getChannel();
			srcInvert[channel.ordinal()] = textureHolder.isInverted();
		}

		BufferedImage channelImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster channelImageRaster = channelImage.getRaster();


		float[] srcPixelData = new float[4];
		float[] pixelData = new float[4];

		for(int h = 0; h<height; h++){
			for(int w = 0; w<width; w++){
				for(int i = 0; i<4; i++){
					WritableRaster sourceRaster = sourceRasters[i];
					if(sourceRaster != null && h<sourceRaster.getHeight() && w<sourceRaster.getWidth()){
						sourceRaster.getPixel(w, h, srcPixelData);
						if(srcInvert[i]){
							pixelData[i] = 255 - srcPixelData[srcChannels[i].ordinal()];
						} else {
							pixelData[i] = srcPixelData[srcChannels[i].ordinal()];
						}
					} else if(i<3){
						pixelData[i] = 0;
					} else {
						pixelData[i] = 255f;
					}
				}

				channelImageRaster.setPixel(w, h, pixelData);
			}
		}
		channelImage.setData(channelImageRaster);

		return channelImage;
	}


	public BufferedImage fillChannelFromChannel(BufferedImage destination, BufferedImage src, int destChannel, int srcChannel, boolean invert) {
		WritableRaster sourceRasters = src.getRaster();
		WritableRaster destImageRaster = destination.getRaster();

		float[] srcPixelData = new float[4];
		float[] pixelData = new float[4];


		for(int h = 0; h < destination.getHeight(); h++){
			for(int w = 0; w < destination.getWidth(); w++){
				destImageRaster.getPixel(w, h, pixelData);
				if(sourceRasters != null && h < sourceRasters.getHeight() && w < sourceRasters.getWidth()){
					sourceRasters.getPixel(w, h, srcPixelData);
					if(invert){
						pixelData[destChannel] = 255 - srcPixelData[srcChannel];
					} else {
						pixelData[destChannel] = srcPixelData[srcChannel];
					}
				} else if(destChannel<3){
					pixelData[destChannel] = 0;
				} else {
					pixelData[destChannel] = 255f;
				}

				destImageRaster.setPixel(w, h, pixelData);
			}
		}
		destination.setData(destImageRaster);

		return destination;
	}


	public BufferedImage getBufferedDeriChannels(Map<Channel, TextureHolder> channelMap, int width, int height) {
		BufferedImage channelImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		fillImage(channelImage);
		for(Channel channel : Channel.values()){
			TextureHolder textureHolder = channelMap.get(channel);
			if(textureHolder.isActive && textureHolder.getBufferedImage() != null){
				diff(channelImage, textureHolder.getBufferedImage(), channel.ordinal(), textureHolder.getChannel().ordinal());
			}
		}
		return channelImage;
	}

	private void fillImage(BufferedImage destination){
		float[] pixelData = new float[4];
		WritableRaster destImageRaster = destination.getRaster();

		for(int h = 0; h<destination.getHeight(); h++){
			for(int w = 0; w<destination.getWidth(); w++){
				for(int i = 0; i<4; i++){
					if(i<3){
						pixelData[i] = 0;
					} else {
						pixelData[i] = 255f;
					}
				}

				destImageRaster.setPixel(w, h, pixelData);
			}
		}
	}

	public BufferedImage diff(BufferedImage destination, BufferedImage src, int destChannel, int srcChannel) {
		// testing edge detection
		WritableRaster sourceRasters = src.getRaster();
		WritableRaster destImageRaster = destination.getRaster();

		float[][] srcPixelData = new float[3][4];
		float[] srcRes = new float[4];
		float[] pixelData = new float[4];

		if(sourceRasters != null){
			for(int h = 0; h < sourceRasters.getHeight(); h++){
				for(int w = 0; w < sourceRasters.getWidth(); w++){
					destImageRaster.getPixel(w, h, pixelData);

					if(w-1<0){
						srcPixelData[0][0] = -1;
						srcPixelData[0][1] = -1;
						srcPixelData[0][2] = -1;
						srcPixelData[0][3] = -1;
					} else {
						sourceRasters.getPixel(w-1, h, srcPixelData[0]);
					}

					sourceRasters.getPixel(w, h, srcPixelData[1]);

					if(w+1>=sourceRasters.getWidth()){
						srcPixelData[2][0] = -1;
						srcPixelData[2][1] = -1;
						srcPixelData[2][2] = -1;
						srcPixelData[2][3] = -1;
					} else {
						sourceRasters.getPixel(w+1, h, srcPixelData[2]);
					}

					float diff1 = srcPixelData[1][srcChannel] - srcPixelData[0][srcChannel];
					float diff2 = srcPixelData[2][srcChannel] - srcPixelData[1][srcChannel];
					if(srcPixelData[0][srcChannel] == -1){
						srcRes[srcChannel] = 128 + diff2;
//						System.out.println("diff2: " + (127 + diff2));
					} else if (srcPixelData[2][srcChannel] == -1){
						srcRes[srcChannel] = 128 + diff1;
//						System.out.println("diff1: " + (127 + diff1));
					} else {
						srcRes[srcChannel] = 128 + (diff1 + diff2)/2f;
					}
					if(srcRes[srcChannel]<0 || srcRes[srcChannel] > 255){
						System.out.println("pixel [" + w + ", " + h + "]: " + srcRes[srcChannel]);
					}


					pixelData[destChannel] = srcRes[srcChannel];

					destImageRaster.setPixel(w, h, pixelData);
				}
			}
			destination.setData(destImageRaster);
		}

		return destination;
	}

	private static int[] getRGBADataBands(BufferedImage bufferedImage) {
		return switch (bufferedImage.getType()){
			case BufferedImage.TYPE_INT_BGR ->  new int[]{2, 1, 0};
			case BufferedImage.TYPE_INT_RGB ->  new int[]{0, 1, 2};
			case BufferedImage.TYPE_INT_ARGB ->  new int[]{0, 1, 2, 3};
			default -> new int[]{0, 1, 2, 3};
		};
	}


	private static void fillRasterData(float[] pixelData, float... data){
		for (int i = 0; i<pixelData.length && i< data.length; i++){
			pixelData[i] = data[i];
		}
	}

	public static void main(String[] args) {
		showPanel(null);
	}

	private final JFileChooser fileChooser = new RMSFileChooser();
	public Bitmap[] importImages() {
//		setFilter(FileDialog.OPEN_TEXTURE);
		fileChooser.setCurrentDirectory(new File("C:\\Users\\twilac\\Desktop\\WC3\\troubleShootingStuff\\Ironforge Ram Rider\\Ironforge Ram Rider\\"));
		fileChooser.setMultiSelectionEnabled(true);
		final int returnValue = fileChooser.showOpenDialog(getParent());
		File selectedFile = fileChooser.getSelectedFile();
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

	private void export(){
		int returnValue = fileChooser.showSaveDialog(getParent());
		boolean saved = false;
		File selectedFile = fileChooser.getSelectedFile();
		if (returnValue == JFileChooser.APPROVE_OPTION && selectedFile != null){

			try {
				int width = 0;
				int height = 0;
				for (TextureHolder textureHolder : channelTextureHolderEnumMap.values()){
					if(textureHolder != null){
						width = Math.max(width, textureHolder.getWidth());
						height = Math.max(height, textureHolder.getHeight());
					}
				}
				BufferedImage bufferedImage = getBufferedImageCombinedChannels(channelTextureHolderEnumMap, width, height);
				final String name = selectedFile.getName();
				String fileExtension = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
//		        if (fileExtension.equals("bmp") || fileExtension.equals("jpg") || fileExtension.equals("jpeg")) {
//			        JOptionPane.showMessageDialog(getParent(),
//					        "Warning: Alpha channel was converted to black. Some data will be lost" +
//							        "\nif you convert this texture back to Warcraft BLP.");
//			        bufferedImage = ImageUtils.removeAlphaChannel(bufferedImage);
//		        }
		        if(fileExtension.equals("tga")){
		            TgaFile.writeTGA(bufferedImage, selectedFile);
			        saved = true;
		        } else {
			        boolean write;
				        write = ImageIO.write(bufferedImage, fileExtension, selectedFile);
			        if (!write) {
		                JOptionPane.showMessageDialog(getParent(), "Could not write file.\nFile type unknown or unavailable");
		            }
			        saved = write;
		        }
	        } catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		if(saved){
			JOptionPane.showMessageDialog(this, "Image Saved!", "Image Saved", JOptionPane.PLAIN_MESSAGE);
		}
	}
}
