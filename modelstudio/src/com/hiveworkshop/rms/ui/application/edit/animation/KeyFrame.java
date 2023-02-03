package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.animation.SlideKeyframesAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class KeyFrame {
	private static final int SLIDER_SIDE_BUTTON_SIZE = 15;
	private static final int SLIDING_TIME_CHOOSER_WIDTH = 50 + (SLIDER_SIDE_BUTTON_SIZE * 2);
	private static final int VERTICAL_TICKS_HEIGHT = 10;
	private static final int VERTICAL_SLIDER_HEIGHT = 15;
	private static final int SIDE_OFFSETS = SLIDING_TIME_CHOOSER_WIDTH / 2;


	private final KeyframeHandler keyframeHandler;
	private boolean mouseOver = false;
	private int time = -1;
	private final Set<TimelineContainer> objects = new HashSet<>();
	private final List<AnimFlag<?>> timelines = new ArrayList<>();
	private final Rectangle renderRect;
	private int x = 0;
	private int width = 8;

	private JPanel timelinePanel;
	private TimeEnvironmentImpl timeEnvironment;

	KeyFrame(KeyframeHandler keyframeHandler, int time) {
		this.keyframeHandler = keyframeHandler;
		timelinePanel = keyframeHandler.getTimelinePanel();
		timeEnvironment = keyframeHandler.getTimeEnvironment();
		renderRect = new Rectangle(0, VERTICAL_SLIDER_HEIGHT, width, VERTICAL_TICKS_HEIGHT);
		setTime(time);
	}

	protected void reposition() {
		setTime(time);
	}

	protected int getXPoint() {
		return x;
	}

	protected void setFrameX(int time) {
		renderRect.x = computeXFromTime(time) - width / 2;
	}

	public KeyFrame addObject(IdObject object){
		objects.add(object);
		return this;
	}
	public KeyFrame addObject(TimelineContainer object){
		objects.add(object);
		return this;
	}

	public Set<TimelineContainer> getObjects() {
		return objects;
	}

	public KeyFrame addTimeline(AnimFlag<?> animFlag){
		timelines.add(animFlag);
		return this;
	}

	public List<AnimFlag<?>> getTimelines() {
		return timelines;
	}

	public Rectangle getRenderRect() {
		return renderRect;
	}

	public int getTime() {
		return time;
	}

	public KeyFrame drag(int dx){
		x = dx;
		x = Math.min(getMaxX(), x);
		x = Math.max(0, x);
		time = (int) (getLocationFraction() * timeEnvironment.getLength());
		return this;
	}

	private SlideKeyframesAction slideKeyframesAction;
	public KeyFrame dragTime(int time){
		if(time != this.time && slideKeyframesAction != null){
			slideKeyframesAction.update(time);
			this.time = Math.max(time, 0);
			this.time = Math.min(this.time, timeEnvironment.getLength());
			x = computeXFromTime(this.time);
			renderRect.x = x + SIDE_OFFSETS - width/2;
		}
		return this;
	}
	public KeyFrame initDrag(Sequence sequence, Runnable keyframeChangeCallback){
		slideKeyframesAction = new SlideKeyframesAction(time, timelines, sequence, keyframeChangeCallback);
		return this;
	}
	public SlideKeyframesAction finnishDrag(){
		SlideKeyframesAction action = slideKeyframesAction;
		slideKeyframesAction = null;
		return action;
	}


	public double getLocationFraction(){
		return x / (double) getMaxX();
	}


	public KeyFrame setTime(int time) {
		this.time = Math.max(time, 0);
		this.time = Math.min(this.time, timeEnvironment.getLength());
		x = computeXFromTime(this.time);
		renderRect.x = x + SIDE_OFFSETS - width/2;
		return this;
	}

	public boolean containsPoint(Point point){
		return renderRect.contains(point);
	}

	private int computeXFromTime(int time) {
		int maxX = getMaxX();
		double timeRatio = (time) / (double) (timeEnvironment.getLength());
//		System.out.println("new x: " + ((maxX * timeRatio) + (SIDE_OFFSETS)) + " for time " + time);
		return (int) (maxX * timeRatio);
	}

	private int getMaxX() {
		int maxX = timelinePanel.getWidth() - SLIDING_TIME_CHOOSER_WIDTH;
		return maxX;
	}

	public void drawMarker(Graphics g) {
//		boolean mouseOver = false;//mouseOverFrameTime != null && mouseOverFrameTime.equals(keyFrame.getTime());
		boolean[] transRotScalOth = getTransRotScalOth();
		GradientPaint fillPaint = getFillPaint(transRotScalOth);
		((Graphics2D) g).setPaint(fillPaint);
		g.fillRoundRect(renderRect.x, renderRect.y, renderRect.width, renderRect.height, 2, 2);
		if (mouseOver) {
			g.setColor(Color.RED);
		} else {
			GradientPaint edgePaint = getEdgePaint(transRotScalOth);
			((Graphics2D) g).setPaint(edgePaint);
		}
		g.drawRoundRect(renderRect.x, renderRect.y, renderRect.width, renderRect.height, 2, 2);

		if (mouseOver) {
			drawFloatingTime(g);
		}
	}

	public void drawFloatingTime(Graphics g) {

		FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
		g.setColor(Color.WHITE);
//		int draggingFrameTime = time;
//		if (draggingFrameTime > timeEnvironment.getLength()) {
//			draggingFrameTime = timeEnvironment.getLength();
//		} else if (draggingFrameTime < 0) {
//			draggingFrameTime = 0;
//		}
		String text = Integer.toString(time);
		int y = (renderRect.y) - 2;
		int x1 = renderRect.x + ((renderRect.width - fontMetrics.stringWidth(text)) / 2);
		g.drawString(text, x1, y);

	}
	public void drawFloatingTime(Graphics g, Color color, String extraText) {

		FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
		g.setColor(color);
//		int draggingFrameTime = time;
//		if (draggingFrameTime > timeEnvironment.getLength()) {
//			draggingFrameTime = timeEnvironment.getLength();
//		} else if (draggingFrameTime < 0) {
//			draggingFrameTime = 0;
//		}
		String text = time + extraText;
		int y = (renderRect.y) - 2;
		int x1 = renderRect.x + ((renderRect.width - fontMetrics.stringWidth(text)) / 2);
		g.drawString(text, x1, y);

	}


	public boolean[] getTransRotScalOth() {
		boolean[] transRotScalOth = new boolean[] {false, false, false, false};
		for (AnimFlag<?> af : timelines) {
			String afName = af.getName();
			transRotScalOth[0] = (afName.equals(MdlUtils.TOKEN_TRANSLATION) || transRotScalOth[0]);
			transRotScalOth[1] = (afName.equals(MdlUtils.TOKEN_ROTATION) || transRotScalOth[1]);
			transRotScalOth[2] = (afName.equals(MdlUtils.TOKEN_SCALING) || transRotScalOth[2]);
			transRotScalOth[3] |= !(afName.equals(MdlUtils.TOKEN_TRANSLATION) || afName.equals(MdlUtils.TOKEN_ROTATION) || afName.equals(MdlUtils.TOKEN_SCALING));
		}
		return transRotScalOth;
	}



	private GradientPaint getFillPaint(boolean[] transRotScalOth) {
		if (transRotScalOth[0] && ProgramGlobals.getEditorActionType() == ModelEditorActionType3.TRANSLATION) {
			return kfPaintTrans;
		} else if (transRotScalOth[1] && ProgramGlobals.getEditorActionType() == ModelEditorActionType3.ROTATION) {
			return kfPaintRot;
		} else if (transRotScalOth[2] && ProgramGlobals.getEditorActionType() == ModelEditorActionType3.SCALING) {
			return kfPaintScale;
		} else {
			if (transRotScalOth[0]){
				return kfPaintTrans;
			} else if (transRotScalOth[1]) {
				return kfPaintRot;
			} else if (transRotScalOth[2]) {
				return kfPaintScale;
			} else {
				return kfPaintTrans;
			}
		}
	}

	private GradientPaint getEdgePaint(boolean[] transRotScalOth) {
		if (transRotScalOth[0] && ProgramGlobals.getEditorActionType() == ModelEditorActionType3.TRANSLATION) {
			if(transRotScalOth[1] && transRotScalOth[2]){
				return kfPaintRotScale;
			}else if (transRotScalOth[1]){
				return kfPaintRotRot;
			} else if (transRotScalOth[2]){
				return kfPaintScaleScale;
			} else {
				return kfPaintTransTrans;
			}
		} else if (transRotScalOth[1] && ProgramGlobals.getEditorActionType() == ModelEditorActionType3.ROTATION) {
			if(transRotScalOth[0] && transRotScalOth[2]){
				return kfPaintTransScale;
			}else if (transRotScalOth[0]){
				return kfPaintTransTrans;
			} else if (transRotScalOth[2]){
				return kfPaintScaleScale;
			} else {
				return kfPaintRotRot;
			}
		} else if (transRotScalOth[2] && ProgramGlobals.getEditorActionType() == ModelEditorActionType3.SCALING) {
			if(transRotScalOth[0] && transRotScalOth[1]){
				return kfPaintTransRot;
			}else if (transRotScalOth[0]){
				return kfPaintTransTrans;
			} else if (transRotScalOth[1]){
				return kfPaintRotRot;
			} else {
				return kfPaintScaleScale;
			}
		} else {
			if (transRotScalOth[0]) {
				if(transRotScalOth[1] && transRotScalOth[2]){
					return kfPaintRotScale;
				}else if (transRotScalOth[1]){
					return kfPaintRotRot;
				} else if (transRotScalOth[2]){
					return kfPaintScaleScale;
				} else {
					return kfPaintTransTrans;
				}
			} else if (transRotScalOth[1]) {
				if (transRotScalOth[2]){
					return kfPaintScaleScale;
				} else {
					return kfPaintRotRot;
				}
			} else if (transRotScalOth[2]) {
				return kfPaintScaleScale;
			} else {
				return kfPaintTransTrans;
			}
		}
	}

	private GradientPaint getEdgePaint2(boolean[] transRotScalOth) {

		Set<transforms> transformsSet = new HashSet<>();
		Color color1 = Color.WHITE;
		Color color2 = Color.WHITE;
		if (transRotScalOth[0]){
			color1 = transforms.TRANS.color1;
			color2 = transforms.TRANS.color2;
		}
		return getEdgePaint(color1, color2);
	}

	private Set<transforms> getTransforms(boolean[] transRotScalOth){
		Set<transforms> transformsSet = new HashSet<>();
		if (transRotScalOth[0]) {
			transformsSet.add(transforms.TRANS);
		}
		if (transRotScalOth[1]) {
			transformsSet.add(transforms.ROT);
		}
		if (transRotScalOth[2]) {
			transformsSet.add(transforms.SCALE);
		}
		return transformsSet;
	}


	private GradientPaint getFillPaint(Color color1, Color color2){
		return new GradientPaint(pt1, color1, pt2, color2, true);
	}
	private GradientPaint getEdgePaint(Color color1, Color color2){
		return new GradientPaint(pt3, color1, pt4, color2, true);
	}


	private transforms getEditorType(){
		return switch (ProgramGlobals.getEditorActionType()){
			case TRANSLATION, EXTEND, EXTRUDE -> transforms.TRANS;
			case ROTATION, SQUAT -> transforms.ROT;
			case SCALING -> transforms.SCALE;
		};
	}


	private enum transforms {
		ROT(    new Color(  0, 255,   0), new Color(100, 255, 100)),
		TRANS(  new Color(  0,   0, 255), new Color(100, 100, 255)),
		SCALE(  new Color(255, 100,   0), new Color(255, 150, 100));
		Color color1;
		Color color2;
		transforms(Color color1, Color color2){
			this.color1 = color1;
			this.color2 = color2;
		}

		public Color getColor1() {
			return color1;
		}

		public Color getColor2() {
			return color2;
		}
	}



	private final Point pt1 = new Point(0, 10);
	private final Point pt2 = new Point(0, 30);
	private final GradientPaint kfPaintRot      = new GradientPaint(pt1, new Color(100, 255, 100), pt2, new Color(  0, 255,   0), true);
	private final GradientPaint kfPaintTrans    = new GradientPaint(pt1, new Color(100, 100, 255), pt2, new Color(  0,   0, 255), true);
	private final GradientPaint kfPaintScale    = new GradientPaint(pt1, new Color(255, 150, 100), pt2, new Color(255, 100,   0), true);

	private final Point pt3 = new Point(0, VERTICAL_SLIDER_HEIGHT);
	private final Point pt4 =  new Point(0, VERTICAL_TICKS_HEIGHT + VERTICAL_SLIDER_HEIGHT);
	private final GradientPaint kfPaintTransRot     = new GradientPaint(pt3, new Color(0, 0, 255), pt4, new Color(  0, 255, 0), true);
	private final GradientPaint kfPaintTransScale   = new GradientPaint(pt3, new Color(0, 0, 255), pt4, new Color(255, 100, 0), true);
	private final GradientPaint kfPaintRotScale     = new GradientPaint(pt3, new Color(0, 255, 0), pt4, new Color(255, 100, 0), true);

	private final GradientPaint kfPaintTransTrans   = new GradientPaint(pt3, new Color(  0,   0,255), pt4, new Color(  0,   0, 255), true);
	private final GradientPaint kfPaintRotRot       = new GradientPaint(pt3, new Color(  0, 255,  0), pt4, new Color(  0, 255,   0), true);
	private final GradientPaint kfPaintScaleScale   = new GradientPaint(pt3, new Color(255, 100,  0), pt4, new Color(255, 100,   0), true);
}
