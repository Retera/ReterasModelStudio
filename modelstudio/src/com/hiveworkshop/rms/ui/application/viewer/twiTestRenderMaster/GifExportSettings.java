package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GifExportSettings {
	private int timeBetweenFrames = 100;
	private int delayLast = 0;
	private int delayFirst = 0;
	private int expW = 200;
	private int expH = 200;
	private int alphaCutoff = 1;
	int offs;

	List<ByteBuffer> byteBuffers;
	List<Integer> delays;
	int framesToExp;
	int frameSpacing;
	float timePerFrame;
	int currFrame = 0;
	boolean doExp;
	int lastFrameDelay;
	int firstFrameDelay;
	ByteBuffer[] buffers;
	int[] frameDelays;
	ExportFrameViewportPanel displayPanel;
	Consumer<GifExportSettings> settingsConsumer;
	byte[] bytes;

	public GifExportSettings(ExportFrameViewportPanel displayPanel){
		this.displayPanel = displayPanel;
	}
	public GifExportSettings(Consumer<GifExportSettings> settingsConsumer){
		this.settingsConsumer = settingsConsumer;
	}
	private void doExp() {
//		doExp = true;
		if(settingsConsumer != null){
			settingsConsumer.accept(this);
		}
	}

	public boolean isDoExp() {
		return doExp;
	}

	public GifExportSettings setDoExp(boolean doExp) {
		this.doExp = doExp;
		return this;
	}

	private void doExp2(TimeEnvironmentImpl timeEnvironment) {

		if(frameSpacing != 0){
			int length = timeEnvironment.getLength();
			int frames = (length/frameSpacing);
			this.framesToExp = frames;
			this.timePerFrame = length/(float)frames;
		} else {
			this.offs = timeEnvironment.getAnimationTime();
			this.timePerFrame = timeEnvironment.getAnimationTime();
			this.framesToExp = 1;
		}
		byteBuffers = new ArrayList<>();
		delays = new ArrayList<>();

		if(settingsConsumer != null){
			settingsConsumer.accept(this);
		}
	}
	private void increment() {
//		timeEnvironment.setAnimationTime((int)(offs + timePerFrame*currFrame));
		delays.add(((int)(timePerFrame*(currFrame+1))) - ((int)(timePerFrame*currFrame)));

	}
	private void doExp4() {
		ByteBuffer[] buffers = new ByteBuffer[framesToExp];
		int[] frameDelays = new int[framesToExp];
		for(int i = 0; i<framesToExp; i++){
			buffers[i] = byteBuffers.get(i);
			frameDelays[i] = delays.get(i)/10;
//			frameDelays[i] = 1;
		}
		frameDelays[0] += firstFrameDelay/10;
		frameDelays[framesToExp-1] += lastFrameDelay/10;
	}

	public GifExportSettings addBuffer(ByteBuffer buffer){
		byteBuffers.add(buffer);
		return this;
	}

	public JPanel getSettingsPanel(){
		JButton exp = new JButton("Generate gif");
		exp.addActionListener(e -> doExp());
		IntEditorJSpinner timeBetwFrames = new IntEditorJSpinner(timeBetweenFrames, 10, 100000, i -> timeBetweenFrames = i);
		IntEditorJSpinner delayLastSpinner = new IntEditorJSpinner(delayLast, 0, 100000, i -> delayLast = i);
		IntEditorJSpinner delayFirstSpinner = new IntEditorJSpinner(delayFirst, 0, 100000, i -> delayFirst = i);
		IntEditorJSpinner withSpinner = new IntEditorJSpinner(expW, 0, 10000, i -> expW = i);
		IntEditorJSpinner heightSpinner = new IntEditorJSpinner(expH, 0, 10000, i -> expH = i);
		IntEditorJSpinner alphaSpinner = new IntEditorJSpinner(alphaCutoff, 0, 255, i -> alphaCutoff = i);
		JPanel panel = new JPanel(new MigLayout());
		panel.add(new JLabel("Frame spacing"));
		panel.add(timeBetwFrames, "wrap");
		panel.add(new JLabel("First frame linger"));
		panel.add(delayFirstSpinner, "wrap");
		panel.add(new JLabel("Last frame linger"));
		panel.add(delayLastSpinner, "wrap");
		panel.add(new JLabel("width"));
		panel.add(withSpinner, "wrap");
		panel.add(new JLabel("height"));
		panel.add(heightSpinner, "wrap");
		panel.add(new JLabel("alpha cutoff"));
		panel.add(alphaSpinner, "wrap");
		panel.add(exp, "");
		return panel;
	}

	public int getTimeBetweenFrames() {
		return timeBetweenFrames;
	}

	public GifExportSettings setTimeBetweenFrames(int timeBetweenFrames) {
		this.timeBetweenFrames = timeBetweenFrames;
		return this;
	}

	public int getDelayLast() {
		return delayLast;
	}

	public GifExportSettings setDelayLast(int delayLast) {
		this.delayLast = delayLast;
		return this;
	}

	public int getDelayFirst() {
		return delayFirst;
	}

	public GifExportSettings setDelayFirst(int delayFirst) {
		this.delayFirst = delayFirst;
		return this;
	}

	public int getExpW() {
		return expW;
	}

	public GifExportSettings setExpW(int expW) {
		this.expW = expW;
		return this;
	}

	public int getExpH() {
		return expH;
	}

	public GifExportSettings setExpH(int expH) {
		this.expH = expH;
		return this;
	}

	public int getAlphaCutoff() {
		return alphaCutoff;
	}

	public GifExportSettings setAlphaCutoff(int alphaCutoff) {
		this.alphaCutoff = alphaCutoff;
		return this;
	}

	public int getOffs() {
		return offs;
	}

	public GifExportSettings setOffs(int offs) {
		this.offs = offs;
		return this;
	}
}
