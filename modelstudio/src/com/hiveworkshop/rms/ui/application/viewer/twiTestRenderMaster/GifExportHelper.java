package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.twiImageStuff.TwiWriteGif;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GifExportHelper {
	private List<ByteBuffer> byteBuffers;
	private List<Integer> delays;
	private List<Integer> renderTimes;
	private int offs;

	JPanel gifPanel;
	JLabel gifLabel;
	JButton saveGif;
	byte[] bytes;

	ExportFrameViewportCanvas viewport;
	GifExportSettings gifExportSettings;
	RenderModel renderModel;
	TimeEnvironmentImpl timeEnvironment;

	public GifExportHelper(ViewportCanvas viewport) {
		if (viewport instanceof ExportFrameViewportCanvas) {
			this.viewport = (ExportFrameViewportCanvas)viewport;
			((ExportFrameViewportCanvas)viewport).setOnDoneRunnable(this::doExport);
		}
		gifExportSettings = new GifExportSettings(this::initGenerateFrames);
		gifPanel = createGifPanel();
	}

	public GifExportHelper setRenderModel(RenderModel renderModel) {
		this.renderModel = renderModel;
		if (renderModel != null) {
			timeEnvironment = renderModel.getTimeEnvironment();
		} else {
			timeEnvironment = null;
		}
		return this;
	}

	private JPanel createGifPanel() {
		JPanel gifPanel = new JPanel(new MigLayout("fill"));
		gifLabel = new JLabel();
		saveGif = new JButton("Save gif");
		saveGif.addActionListener(e -> saveGif());
		saveGif.setEnabled(false);
		gifPanel.add(gifLabel, "");
		gifPanel.add(saveGif, "top");
		return gifPanel;
	}

	public JPanel getGifPanel() {
		return gifPanel;
	}

	public JPanel getSettingsPanel() {
		return gifExportSettings.getSettingsPanel();
	}

	private void updateIcon(byte[] bytes) {
		this.bytes = bytes;
		if (bytes != null) {
			gifLabel.setIcon(new ImageIcon(bytes));
			saveGif.setEnabled(true);
		} else {
			gifLabel.setIcon(null);
			saveGif.setEnabled(false);
		}
	}

	private void saveGif() {
		if (bytes != null) {
			saveBytes("image.gif", new FileNameExtensionFilter("GIF Image", "gif"), bytes);
		}
	}

	public void initGenerateFrames(GifExportSettings settings) {
		if (timeEnvironment != null) {
			delays = new ArrayList<>();
			renderTimes = new ArrayList<>();
			this.gifExportSettings = settings;
			this.offs = settings.getOffs();

			int framesToExp;
			float timePerFrame;
			if (settings.getTimeBetweenFrames() != 0 && timeEnvironment.getCurrentSequence() != null) {
				int length = timeEnvironment.getLength();
				int frames = (length/settings.getTimeBetweenFrames());
				framesToExp = frames;
				timePerFrame = length/(float)frames;
			} else {
				this.offs = timeEnvironment.getAnimationTime();
				timePerFrame = timeEnvironment.getAnimationTime();
				framesToExp = 1;
			}

			for (int i = 0; i< framesToExp; i++) {
				renderTimes.add(offs + (int)(timePerFrame *i));
				delays.add(((int)(timePerFrame *(i+1))) - ((int)(timePerFrame *i)));
			}

			Dimension expDimension = new Dimension(settings.getExpW(), settings.getExpH());
			byteBuffers = new ArrayList<>();
			viewport.initGenerateFrames(byteBuffers, renderTimes, expDimension);
		}
	}


	private void doExport() {
		int framesToExp = byteBuffers.size();
		ByteBuffer[] buffers = new ByteBuffer[framesToExp];
		int[] frameDelays = new int[framesToExp];
		for (int i = 0; i<framesToExp; i++) {
			buffers[i] = byteBuffers.get(i);
			frameDelays[i] = delays.get(i)/10;
//			frameDelays[i] = 1;
		}
		frameDelays[0] += gifExportSettings.getDelayFirst()/10;
		frameDelays[framesToExp-1] += gifExportSettings.getDelayLast()/10;
		byte[] asBytes = TwiWriteGif.getAsBytes(buffers, frameDelays, gifExportSettings);
//		byteArrayConsumer.accept(asBytes);
		updateIcon(asBytes);
	}

	public void saveBytes(String suggestedName, FileNameExtensionFilter filter, byte[] bytes) {
		FileDialog fileDialog = new FileDialog();
		String fileName = suggestedName.replaceAll(".+[\\\\/](?=.+)", "");
		File selectedFile = fileDialog.getSaveFile(fileName, Collections.singletonList(filter));
		if (selectedFile != null) {
			String expExt = fileDialog.getExtensionOrNull(fileName);
			String saveExt = fileDialog.getExtensionOrNull(selectedFile);
			if (expExt != null && (saveExt == null || !saveExt.equals(expExt))) {
				selectedFile = new File(selectedFile.getPath() + "." + expExt);
			}
			try (FileOutputStream fileOutputStream = new FileOutputStream(selectedFile)) {
				fileOutputStream.write(bytes);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}

		}
	}
//	public void saveStream(String suggestedName, FileNameExtensionFilter filter, InputStream resourceAsStream) {
//		com.hiveworkshop.rms.ui.application.FileDialog fileDialog = new FileDialog(this);
//		String fileName = suggestedName.replaceAll(".+[\\\\/](?=.+)", "");
//		File selectedFile = fileDialog.getSaveFile(fileName, Collections.singletonList(filter));
//		if (selectedFile != null) {
//			String expExt = fileDialog.getExtensionOrNull(fileName);
//			String saveExt = fileDialog.getExtensionOrNull(selectedFile);
//			if (expExt != null && (saveExt == null || !saveExt.equals(expExt))) {
//				selectedFile = new File(selectedFile.getPath() + "." + expExt);
//			}
//			try {
//				Files.copy(resourceAsStream, selectedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//			} catch (IOException ioException) {
//				ioException.printStackTrace();
//			}
//		}
//	}
}
