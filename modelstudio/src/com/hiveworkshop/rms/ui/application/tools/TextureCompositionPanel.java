package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.blp.ImageUtils;
import com.hiveworkshop.rms.ui.application.RMSFileChooser;
import com.hiveworkshop.rms.ui.preferences.SaveProfileNew;
import com.hiveworkshop.rms.ui.util.TwiPopup;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.TwiComboBox;
import de.wc3data.image.TgaFile;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
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
	private RMSFileChooser fileChooser;
	private int thumbnailSize = 64;
	List<Bitmap> bitmaps = new ArrayList<>();
	ZoomableImagePreviewPanel imagePreviewPanel;
	private ZoomableImagePreviewPanel comp;

	EnumMap<Channel, TextureHolder> channelTextureHolderEnumMap = new EnumMap<>(Channel.class);

	public TextureCompositionPanel() {
		super(new MigLayout("fill", "[grow]", "[grow]"));
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Load Images", new TextureLoadListPanel(bitmaps, 12, 32, this::getFileChooser));
		tabbedPane.addTab("Combine Images", getImagePanel());

		add(tabbedPane, "growx, growy");
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

	private JPanel getSubImagesPanel() {
		JPanel subImagesPanel = new JPanel(new MigLayout("fill, ins 0, wrap 2", "[][grow]", ""));
		for (Channel channel : Channel.values()) {
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

	private JPanel getSubImagePanel(Channel channel, TextureHolder textureHolder) {
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
		for (Channel channel1 : Channel.values()) {
			channelGroup.addJRadioButton(channel1.getName(), e -> textureHolder.setChannel(channel1));
		}
		channelGroup.setSelectedIndex(textureHolder.channel.ordinal());
		panel.add(channelGroup.getButtonPanel(), "");

		JCheckBox invert = new JCheckBox("invert", false);
		invert.addActionListener(e -> textureHolder.setInverted(invert.isSelected()));
		panel.add(invert, "");

		return panel;
	}
	private JPanel getCombImagePanel() {
		imagePreviewPanel = new ZoomableImagePreviewPanel(null);
		imagePreviewPanel.setImage(ImageUtils.getColorImage(Color.GRAY, 512));
		JPanel previewPanel = new JPanel(new MigLayout("fill", "[grow]", "[grow]"));
		previewPanel.setBorder(BorderFactory.createTitledBorder("Previewer"));
//		previewPanel.setLayout(new BorderLayout());
		previewPanel.add(imagePreviewPanel, "growx, growy");
		previewPanel.revalidate();
		return previewPanel;
	}

	private void updateCombinedImage() {
		BufferedImage combinedChannels = getCombined(channelTextureHolderEnumMap);
		imagePreviewPanel.setImage(combinedChannels);
//		imagePreviewPanel.setImage(getBufferedDeriChannels(channelTextureHolderEnumMap, width, height));
//		imagePreviewPanel.resetZoom();
		imagePreviewPanel.repaint();
	}


	public static void showPanel(JComponent parent) {
		TextureCompositionPanel textureManager = new TextureCompositionPanel();
//		textureManager.setSize(new Dimension(800, 650));

		textureManager.setPreferredSize(new Dimension(800, textureManager.getPreferredSize().height));
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
		private final ImageIcon currImageIcon;
		private final JLabel label;
		private final Runnable updater;

		public TextureHolder(Channel channel, Runnable updater) {
			this.channel = channel;
			this.updater = updater;
			currImageIcon = new ImageIcon(ImageUtils.getColorImage(Color.DARK_GRAY)
					.getScaledInstance(thumbnailSize, thumbnailSize, Image.SCALE_DEFAULT));
			label = new JLabel(currImageIcon);
		}

		public TextureHolder setChannel(Channel channel) {
			this.channel = channel;
			if (bufferedImage != null) {
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
//			if (ProgramGlobals.getCurrentModelPanel() != null) {
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
			if (bufferedImage != null) {
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

		Channel(String name) {
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

		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				sourceRaster.getPixel(w, h, srcPixelData);

				switch (channel) {
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

	private static int[] getRGBADataBands(BufferedImage bufferedImage) {
		return switch (bufferedImage.getType()) {
			case BufferedImage.TYPE_INT_BGR  -> new int[]{2, 1, 0};
			case BufferedImage.TYPE_INT_RGB  -> new int[]{0, 1, 2};
			case BufferedImage.TYPE_INT_ARGB -> new int[]{0, 1, 2, 3};
			default -> new int[]{0, 1, 2, 3};
		};
	}

	private static void fillRasterData(float[] pixelData, float... data) {
		for (int i = 0; i < pixelData.length && i < data.length; i++) {
			pixelData[i] = data[i];
		}
	}

	public BufferedImage fillChannelFromChannel(BufferedImage destination, BufferedImage src, int destChannel, int srcChannel, boolean invert) {
		WritableRaster sourceRasters = src.getRaster();
		WritableRaster destImageRaster = destination.getRaster();

		float[] srcPixelData = new float[4];
		float[] pixelData = new float[4];


		for (int h = 0; h < destination.getHeight(); h++) {
			for (int w = 0; w < destination.getWidth(); w++) {
				destImageRaster.getPixel(w, h, pixelData);
				if (sourceRasters != null && h < sourceRasters.getHeight() && w < sourceRasters.getWidth()) {
					sourceRasters.getPixel(w, h, srcPixelData);
					if (invert) {
						pixelData[destChannel] = 255 - srcPixelData[srcChannel];
					} else {
						pixelData[destChannel] = srcPixelData[srcChannel];
					}
				} else if (destChannel < 3) {
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

	public static void main(String[] args) {
		showPanel(null);
	}

	private RMSFileChooser getFileChooser() {
		if (fileChooser == null) {
			fileChooser = new RMSFileChooser(SaveProfileNew.get());
		}
		return fileChooser;
	}

	private void export() {
		RMSFileChooser fileChooser = getFileChooser();
		int returnValue = fileChooser.showSaveDialog(getParent());
		File selectedFile = fileChooser.getSelectedFile();
		if (returnValue == JFileChooser.APPROVE_OPTION && selectedFile != null) {

			BufferedImage bufferedImage = getCombined(channelTextureHolderEnumMap);
			final String name = selectedFile.getName();
			String fileExtension = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
//			if (fileExtension.equals("bmp") || fileExtension.equals("jpg") || fileExtension.equals("jpeg")) {
//				JOptionPane.showMessageDialog(getParent(),
//						"Warning: Alpha channel was converted to black. Some data will be lost" +
//								"\nif you convert this texture back to Warcraft BLP.");
//				bufferedImage = ImageUtils.removeAlphaChannel(bufferedImage);
//			}

			if (trySave(selectedFile, bufferedImage, fileExtension)) {
				JOptionPane.showMessageDialog(this, "Image Saved!", "Image Saved", JOptionPane.PLAIN_MESSAGE);
			}

		}
	}

	private boolean trySave(File selectedFile, BufferedImage bufferedImage, String fileExtension) {
		try {
			if (fileExtension.equals("tga")) {
				TgaFile.writeTGA(bufferedImage, selectedFile);
				return true;
			} else {
				if (!ImageIO.write(bufferedImage, fileExtension, selectedFile)) {
					JOptionPane.showMessageDialog(getParent(), "Could not write file.\nFile type unknown or unavailable");
					return false;
				} else {
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			TwiPopup.quickDismissPopup(this, "Failed to save: \"" + selectedFile.getName() + "\"", "Failed To Save Image");
		}
		return false;
	}

	private BufferedImage getCombined(Map<Channel, TextureHolder> channelMap) {
		int width = 0;
		int height = 0;
		for (TextureHolder textureHolder : channelMap.values()) {
			if (textureHolder != null) {
				width = Math.max(width, textureHolder.getWidth());
				height = Math.max(height, textureHolder.getHeight());
			}
		}
		return getBufferedImageCombinedChannels(channelMap, width, height);
	}

	public BufferedImage getBufferedImageCombinedChannels(Map<Channel, TextureHolder> channelMap, int width, int height) {

		WritableRaster[] sourceRasters = new WritableRaster[4];
		Channel[] srcChannels = new Channel[4];
		boolean[] srcInvert = new boolean[4];
		for (Channel channel : Channel.values()) {
			TextureHolder textureHolder = channelMap.get(channel);
			if (textureHolder.isActive && textureHolder.getBufferedImage() != null) {
				sourceRasters[channel.ordinal()] = textureHolder.getBufferedImage().getRaster();
			}
			srcChannels[channel.ordinal()] = textureHolder.getChannel();
			srcInvert[channel.ordinal()] = textureHolder.isInverted();
		}

		BufferedImage channelImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster channelImageRaster = channelImage.getRaster();


		float[] srcPixelData = new float[4];
		float[] pixelData = new float[4];

		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				for (int i = 0; i < 4; i++) {
					WritableRaster sourceRaster = sourceRasters[i];
					if (sourceRaster != null && h < sourceRaster.getHeight() && w < sourceRaster.getWidth()) {
						sourceRaster.getPixel(w, h, srcPixelData);
						if (srcInvert[i]) {
							pixelData[i] = 255 - srcPixelData[srcChannels[i].ordinal()];
						} else {
							pixelData[i] = srcPixelData[srcChannels[i].ordinal()];
						}
					} else if (i < 3) {
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

	public BufferedImage getDerivateChannels(Map<Channel, TextureHolder> channelMap, int width, int height) {
		// Returns an image by calculating the change in value from pixel to pixel
		// Sort of resembles "find edge"
		BufferedImage channelImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		fillImage(channelImage);
		for (Channel channel : Channel.values()) {
			TextureHolder textureHolder = channelMap.get(channel);
			if (textureHolder.isActive && textureHolder.getBufferedImage() != null) {
				diff(channelImage, textureHolder.getBufferedImage(), channel.ordinal(), textureHolder.getChannel().ordinal());
			}
		}
		return channelImage;
	}

	private void fillImage(BufferedImage destination) {
		float[] pixelData = new float[4];
		WritableRaster destImageRaster = destination.getRaster();

		for (int h = 0; h < destination.getHeight(); h++) {
			for (int w = 0; w < destination.getWidth(); w++) {
				for (int i = 0; i < 4; i++) {
					if (i < 3) {
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

		if (sourceRasters != null) {
			for (int h = 0; h < sourceRasters.getHeight(); h++) {
				for (int w = 0; w < sourceRasters.getWidth(); w++) {
					destImageRaster.getPixel(w, h, pixelData);

					if (w - 1 < 0) {
						srcPixelData[0][0] = -1;
						srcPixelData[0][1] = -1;
						srcPixelData[0][2] = -1;
						srcPixelData[0][3] = -1;
					} else {
						sourceRasters.getPixel(w-1, h, srcPixelData[0]);
					}

					sourceRasters.getPixel(w, h, srcPixelData[1]);

					if (sourceRasters.getWidth() <= w + 1) {
						srcPixelData[2][0] = -1;
						srcPixelData[2][1] = -1;
						srcPixelData[2][2] = -1;
						srcPixelData[2][3] = -1;
					} else {
						sourceRasters.getPixel(w + 1, h, srcPixelData[2]);
					}

					float diff1 = srcPixelData[1][srcChannel] - srcPixelData[0][srcChannel];
					float diff2 = srcPixelData[2][srcChannel] - srcPixelData[1][srcChannel];
					if (srcPixelData[0][srcChannel] == -1) {
						srcRes[srcChannel] = 128 + diff2;
//						System.out.println("diff2: " + (127 + diff2));
					} else if (srcPixelData[2][srcChannel] == -1) {
						srcRes[srcChannel] = 128 + diff1;
//						System.out.println("diff1: " + (127 + diff1));
					} else {
						srcRes[srcChannel] = 128 + (diff1 + diff2)/2f;
					}
					if (srcRes[srcChannel] < 0 || 255 < srcRes[srcChannel]) {
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

}
