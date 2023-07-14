package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.MathUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TimeLinePanel extends JPanel {
	private static final int SLIDER_SIDE_BUTTON_SIZE = 15;
	private static final int SLIDER_TIME_BUTTON_SIZE = 50;
	private static final int SLIDING_TIME_CHOOSER_WIDTH = SLIDER_TIME_BUTTON_SIZE + (SLIDER_SIDE_BUTTON_SIZE * 2);
	private boolean drawing;
	private TimeEnvironmentImpl timeEnvironment;
	private TimeSlider timeSlider;
	private TimeBarPainter timeBarPainter;
	private KeyframeHandler keyframeHandler;
	private TimeLineMouseListener mouseAdapter;
	double zoom = 1;
//	int realStart = -20;
//	int realEnd = 50;
	ModelHandler modelHandler;
	TimeSliderPanel timeSliderPanel;
	public TimeLinePanel(TimeSliderPanel timeSliderPanel) {
		super(new MigLayout("fill"));
		this.timeSliderPanel = timeSliderPanel;
		timeSlider = new TimeSlider(this);
		timeBarPainter = new TimeBarPainter(timeSlider);
		keyframeHandler = new KeyframeHandler(this);
		mouseAdapter = new TimeLineMouseListener(timeSliderPanel, keyframeHandler, timeSlider);

		addComponentListener(getComponentAdapter());

		MouseAdapter mouseWAdapter = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
			}

//			@Override
//			public void mousePressed(MouseEvent e) {
//				super.mousePressed(e);
//				updateTime(getTimeFromX(e.getX()));
//			}

			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				super.mouseExited(e);
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				onScroll(e);
			}

//			@Override
//			public void mouseDragged(MouseEvent e) {
//				updateTime(getTimeFromX(e.getX()));
//			}

			@Override
			public void mouseMoved(MouseEvent e) {
				super.mouseMoved(e);
			}
		};
//		this.addMouseListener(mouseAdapter);
//		this.addMouseMotionListener(mouseAdapter);
		this.addMouseWheelListener(mouseWAdapter);
		this.addMouseListener(mouseAdapter);
		this.addMouseMotionListener(mouseAdapter);
	}

	public TimeSlider getTimeSlider() {
		return timeSlider;
	}

	public TimeLineMouseListener getMouseAdapter() {
		return mouseAdapter;
	}

	public TimeBarPainter getTimeBarPainter() {
		return timeBarPainter;
	}

	public KeyframeHandler getKeyframeHandler() {
		return keyframeHandler;
	}

	private ComponentAdapter getComponentAdapter() {
		return new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				keyframeHandler.slideExistingKeyFramesForResize();
				if (timeEnvironment != null) {
					calculateTickTime();
					timeSlider.setFromTimeFraction(timeEnvironment.getTimeRatio());
//					timeChooserRect.x = computeSliderXFromTime();
				}
			}
		};
	}

	public void setModelHandler(ModelHandler modelHandler) {
		keyframeHandler.setModelHandler(modelHandler);
		mouseAdapter.setModelHandler(modelHandler);
		this.modelHandler = modelHandler;
		if(modelHandler != null) {
			timeEnvironment = modelHandler.getRenderModel().getTimeEnvironment();
//			timeEnvironment.addChangeListener(timeSliderPanel);
			timeBarPainter.setTimeEnvironment(timeEnvironment);
		} else {
			timeBarPainter.setTimeEnvironment(null);
		}
	}


//	int tickTime = 50;
//	int realStart = -20;
//	int realEnd = 50;
	private void calculateTickTime() {
		if (timeEnvironment != null) {
			int width = Math.max(1, getWidth());
			int length = timeEnvironment.getLength();


//			int minTimePerPix = 45 * length / width;
			int minTimePerPix = 35 * length / width;
//			int minTimePerPix = 18 * length / width;

			float tickTime = .5f;
//			System.out.println("Math.log10(realTimeSpan)+1: " + (Math.log10(length) + 1));
//			System.out.println("realTimeSpan: " + length + ", minPix: " + minTimePerPix);

			for (int i = 0; i < Math.log10(length) + 1; i++) {
				double pow = Math.pow(10, i);
				if (pow > minTimePerPix) {
					tickTime = (float) (pow/2f);
//					System.out.println("  pow (i=" + i + "): " + (pow/2f));
					break;
				} else if (2 * pow > minTimePerPix) {
					tickTime = (float) (2 * (pow/2f));
//					System.out.println("  pow (i=" + i + "): " + (pow/2f));
					break;
				} else if (5 * pow > minTimePerPix) {
					tickTime = (float) (5 * (pow/2f));
//					System.out.println("  pow (i=" + i + "): " + (pow/2f));
					break;
				}
			}

//			System.out.println("tickTime: " + (tickTime));
//			double timePerTick = length / (double) tickTime;

//			int tickStep = (int) (width / timePerTick);
//			timeBarPainter.setTickStep(tickStep);
//			timeBarPainter.setTickStep((int) timePerTick);
//			timeBarPainter.setTickStep((int) tickTime);
			timeBarPainter.setTickStep(Math.max(.5f, tickTime));
		}
	}
//	private void calculateTickTime22() {
//		if (timeEnvironment != null) {
//			int width = Math.max(1, getWidth());
//			int length = timeEnvironment.getLength();
//			int minPxPerTick = 40;
//			int maxPxPerTick = 100;
//
//			float pxPerTime = width/(float)length;
//			float timePerPx = length/(float)width;
//
//
//
//			int realStart = (int) (0 - (length * zoom));
//			int realEnd = (int) (length + (length * zoom));
////			int realStart = (int) (0 - (length * 0.08 * zoom));
////			int realEnd = (int) (length + (length * 0.08 * zoom));
//
//			int realTimeSpan = realEnd - realStart;
//			int minTimePerPix = 45 * realTimeSpan / width;
//
//			int tickTime = 1;
//			System.out.println("Math.log10(realTimeSpan)+1: " + (Math.log10(realTimeSpan) + 1));
//			System.out.println("realTimeSpan: " + realTimeSpan + ", minPix: " + minTimePerPix);
//
//			for (int i = 0; i < Math.log10(realTimeSpan) + 1; i++) {
//				double pow = Math.pow(10, i);
//				if (pow > minTimePerPix) {
//					tickTime = (int) pow;
//					System.out.println("pow (i=" + i + "): " + pow);
//					break;
//				} else if (2 * pow > minTimePerPix) {
//					tickTime = 2 * (int) pow;
//					System.out.println("pow (i=" + i + "): " + pow);
//					break;
//				} else if (5 * pow > minTimePerPix) {
//					tickTime = 5 * (int) pow;
//					System.out.println("pow (i=" + i + "): " + pow);
//					break;
//				}
//			}
//
//			System.out.println("tickTime: " + tickTime);
//			double timePerTick = realTimeSpan / (double) tickTime;
//
////			int tickStep = (int) (width / timePerTick);
////			timeBarPainter.setTickStep(tickStep);
////			timeBarPainter.setTickStep((int) timePerTick);
//			timeBarPainter.setTickStep((int) tickTime);
//		}
//	}
//	private void calculateTickTime1() {
//		if (timeEnvironment != null) {
//			int interv = timeEnvironment.getLength();
//			int realStart = (int) (0 - (interv * zoom));
//			int realEnd = (int) (interv + (interv * zoom));
////			int realStart = (int) (0 - (interv * 0.08 * zoom));
////			int realEnd = (int) (interv + (interv * 0.08 * zoom));
//
//			int realTimeSpan = realEnd - realStart;
//			int width = Math.max(1, getWidth());
//			int minTimePerPix = 45 * realTimeSpan / width;
//
//			int tickTime = 1;
//			System.out.println("Math.log10(realTimeSpan)+1: " + (Math.log10(realTimeSpan) + 1));
//			System.out.println("realTimeSpan: " + realTimeSpan + ", minPix: " + minTimePerPix);
//
//			for (int i = 0; i < Math.log10(realTimeSpan) + 1; i++) {
//				double pow = Math.pow(10, i);
//				if (pow > minTimePerPix) {
//					tickTime = (int) pow;
//					System.out.println("pow (i=" + i + "): " + pow);
//					break;
//				} else if (2 * pow > minTimePerPix) {
//					tickTime = 2 * (int) pow;
//					System.out.println("pow (i=" + i + "): " + pow);
//					break;
//				} else if (5 * pow > minTimePerPix) {
//					tickTime = 5 * (int) pow;
//					System.out.println("pow (i=" + i + "): " + pow);
//					break;
//				}
//			}
//
//			System.out.println("tickTime: " + tickTime);
//			double timePerTick = realTimeSpan / (double) tickTime;
//
////			int tickStep = (int) (width / timePerTick);
////			timeBarPainter.setTickStep(tickStep);
////			timeBarPainter.setTickStep((int) timePerTick);
//			timeBarPainter.setTickStep((int) tickTime);
//		}
//	}

	private void onScroll(MouseWheelEvent e) {
		Container parent = getParent();
		if (parent != null) {
			if (e.getWheelRotation() > 0) {
				zoom /= 1.2;
			} else {
				zoom *= 1.2;
			}
//			zoom = MathUtils.clamp(zoom, 1, 18.85);
			zoom = MathUtils.clamp(zoom, 1, 95.4);
			if (zoom > 0) {
//				System.out.println("parent: "+ parent + "\nself: " + this + "\npar.par: " + parent.getParent());
				if(parent.getParent() instanceof JScrollPane){
					int widthBef = getWidth();
					int widthAft = (int) (parent.getWidth() * zoom);
					int eFromEdge = e.getPoint().x - getVisibleRect().x;
					int eNewX = (int) ((e.getPoint().x/(float)widthBef) * widthAft);
					int newVisX = eNewX-eFromEdge;

					Rectangle bounds = getBounds();
					bounds.x = -newVisX;

					this.setPreferredSize(new Dimension((int) (parent.getWidth() * zoom), getHeight()));
					SwingUtilities.invokeLater(() -> setBounds(bounds));
				} else {
					this.setPreferredSize(new Dimension((int) (parent.getWidth() * zoom), getHeight()));
				}
//			if (zoom > 0) {
////				System.out.println("parent: "+ parent + "\nself: " + this + "\npar.par: " + parent.getParent());
//				if(parent.getParent() instanceof JScrollPane){
//					int widthBef = getWidth();
//					int widthAft = (int) (parent.getWidth() * zoom);
//					int eFromEdge = e.getPoint().x - getVisibleRect().x;
//					int eNewX = (int) ((e.getPoint().x/(float)widthBef) * widthAft);
//					int newVisX = eNewX-eFromEdge;
//
//					System.out.println("VisibleRect: " + getVisibleRect() + ", e.pos: " + e.getPoint());
//					System.out.println("Bounds: " + getBounds());
//
//					System.out.println("eFromEdge: " + eFromEdge + ", eNewX: " + eNewX + ", newVisX: " + newVisX);
////					Rectangle visibleRect = getVisibleRect();
//					Rectangle visibleRect = getBounds();
//					visibleRect.x = -newVisX;
//
//					JScrollPane scrollPane = (JScrollPane) parent.getParent();
//					JScrollBar scrollBar = scrollPane.getHorizontalScrollBar();
//					int vBef = scrollBar.getValue();
//					float caAm = vBef / (float) widthBef;
//
//					float adj = (e.getX() - vBef) / (float) widthBef;
//					this.setPreferredSize(new Dimension((int) (parent.getWidth() * zoom), getHeight()));
//					int vAft = (int) (widthAft * caAm);
////					scrollBar.setValue(vAft);
////					scrollBar.setValue((int) (scrollBar.getValue() + (adj*zoom)));
//
////					scrollBar.setValue((int) (vAft * (adj*zoom)));
////					scrollPane.
////					SwingUtilities.invokeLater(() -> scrollBar.setValue(newVisX));
////					SwingUtilities.invokeLater(() -> scrollPane.getHorizontalScrollBar().setValue(newVisX));
//					SwingUtilities.invokeLater(() -> setBounds(visibleRect));
////					setBounds(visibleRect);
////					SwingUtilities.invokeLater(() -> scrollRectToVisible(visibleRect));
////					scrollBar.setValue((int) newVisX);
//					System.out.println("vBef: " + vBef + ", wBef: " + widthBef
//							+ "   vAft: " + vAft + ", wAft: " + widthAft
//							+ ", e.getX(): " + e.getX() + ", adj: " + adj + ", adj*zoom: " + (adj*zoom) + ", vAft2: " + (int) (vAft * (adj*zoom)));
//				} else {
//					this.setPreferredSize(new Dimension((int) (parent.getWidth() * zoom), getHeight()));
//				}

//				if (zoom <= 1) {
//				}

//				if (zoom >= 1) {
//					updateRealLimits();
//				}
			}
		}
		revalidate();
		repaint();
//		SwingUtilities.invokeLater(this::updateKeyframes);
	}

	private void onScroll1(boolean b) {
		Container parent = getParent();
		if (parent != null) {
			if (b) {
				zoom /= 1.2;
			} else {
				zoom *= 1.2;
			}
			MathUtils.clamp(zoom, 1, 18.85);
			if (zoom > 0) {
				this.setPreferredSize(new Dimension((int) (parent.getWidth() * zoom), getHeight()));
				if(parent instanceof JScrollPane){

				}

//				if (zoom <= 1) {
//				}

//				if (zoom >= 1) {
//					updateRealLimits();
//				}
			}
		}
		revalidate();
		repaint();
//		SwingUtilities.invokeLater(this::updateKeyframes);
	}

//	private void updateRealLimits() {
//		if (timeEnvironment != null) {
//			int interv = timeEnvironment.getLength();
//			realStart = (int) (0 - (interv * 0.08 * zoom));
//			realEnd = (int) (interv + (interv * 0.08 * zoom));
//		}
//	}
	public void setDrawing(final boolean drawing) {
		this.drawing = drawing;
		mouseAdapter.setTimelineVisible(drawing);
	//	System.out.println("is drawing: " + drawing);
	//	for (Component component : getComponents()) {
	//		component.setEnabled(drawing);
	//	}
	//	repaint();
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		if (!drawing || timeEnvironment == null) {
			return;
		}
		int width = getWidth();
		timeBarPainter.drawTimeBar(g, width);
		if (SLIDING_TIME_CHOOSER_WIDTH <= width) {
			// keyframes
			keyframeHandler.drawKeyframeMarkers(g);
			// time label of dragged keyframe
			if (mouseAdapter.isDraggingKeyframe()) {
				mouseAdapter.getDraggingFrame().drawFloatingTime(g, Color.WHITE, mouseAdapter.getDraggingTimeDiffString());
			} else if (mouseAdapter.isHoveringKeyframe()) {
				mouseAdapter.getHoveringFrame().drawFloatingTime(g);
			}
			// time slider and glass covering current tick
			timeSlider.drawTimeSlider(g, timeEnvironment.getEnvTrackTime(), keyframeHandler.hasKeyFrameAt(timeEnvironment.getEnvTrackTime()));
		}

	}
}
