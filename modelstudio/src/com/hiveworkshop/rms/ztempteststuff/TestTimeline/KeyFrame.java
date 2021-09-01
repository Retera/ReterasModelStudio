package com.hiveworkshop.rms.ztempteststuff.TestTimeline;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.actions.util.ReversedAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.*;

public class KeyFrame extends JPanel {
	private static final int VERTICAL_TICKS_HEIGHT = 10;
	private static final int VERTICAL_SLIDER_HEIGHT = 15;
	Color rotCol = new Color(0, 255, 0, 170);
	Color scaleCol = new Color(255, 0, 0, 170);
	Color transCol = new Color(0, 0, 255, 170);
	Color elseCol = new Color(255, 255, 0, 170);
	private int time;
	private Set<IdObject> objects = new HashSet<>();
	private List<AnimFlag<?>> animFlags = new ArrayList<>();  //maybe should keep track of Entries instead of animFlags?
	private Rectangle renderRect;
	private int width = 8;
	private int height = 15;

	public KeyFrame(int time) {
		Dimension dimension = new Dimension(width, height);
		this.setSize(dimension);
		this.setMaximumSize(dimension);
		this.setMinimumSize(dimension);
		this.setPreferredSize(dimension);
		this.time = time;
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				System.out.println("clicked on keyframe at " + KeyFrame.this.time);
			}
		});
	}

	public int getTime() {
		return time;
	}

	public KeyFrame setTime(int time) {
		this.time = time;
		return this;
	}

	public Set<IdObject> getObjects() {
		return objects;
	}

	public KeyFrame addObject(IdObject idObject) {
		objects.add(idObject);
		return this;
	}

	public List<AnimFlag<?>> getAnimFlags() {
		return animFlags;
	}

	public KeyFrame addAnimationFlag(AnimFlag<?> animFlag) {
		animFlags.add(animFlag);
		return this;
	}

	@Override
	protected void paintComponent(final Graphics g) {
		drawKeyframeMarkers(g);
	}

	public void drawKeyframeMarkers(Graphics g) {
//		boolean mouseOver = timeAndKey.getValue() == mouseOverFrame;
		boolean mouseOver = false;
		boolean translation = false, rotation = false, scaling = false, other = false;
		for (AnimFlag<?> af : animFlags) {
			boolean afTranslation = "Translation".equals(af.getName());
//			translation |= afTranslation;
//			translation = translation || af.getName().equals("Translation");
			translation = translation || afTranslation;
			boolean afRotation = "Rotation".equals(af.getName());
//			rotation |= afRotation;
			rotation = rotation || afRotation;
			boolean afScaling = "Scaling".equals(af.getName());
//			scaling |= afScaling;
			scaling = scaling || afScaling;
			other |= !(afTranslation || afRotation || afScaling);
//			boolean afTranslation = "Translation".equals(af.getName());
//			translation |= afTranslation;
//			boolean afRotation = "Rotation".equals(af.getName());
//			rotation |= afRotation;
//			boolean afScaling = "Scaling".equals(af.getName());
//			scaling |= afScaling;
//			other |= !(afTranslation || afRotation || afScaling);
		}
		List<Color> colors = new ArrayList<>();
		if (scaling) {
			colors.add(scaleCol);
//			((Graphics2D) g).setPaint(keyframePaintRed);
		}
		if (rotation) {
			colors.add(rotCol);
//			((Graphics2D) g).setPaint(keyframePaint);
		}
		if (translation) {
			colors.add(transCol);
//			((Graphics2D) g).setPaint(keyframePaintBlue);
		}
		if (other) {
			colors.add(elseCol);
//			((Graphics2D) g).setPaint(keyframePaint);
		}
//		System.out.println("colors: " + colors.size());
		((Graphics2D) g).setPaint(getGrad(colors, 0));
		g.fillRoundRect(0, 0, getWidth(), getHeight(), 2, 2);
		Color color = Color.YELLOW;
		if (scaling) {
			color = Color.ORANGE;
		} else if (rotation) {
			color = Color.GREEN;
		} else if (translation) {
			color = Color.BLUE;
		}
		g.setColor(mouseOver ? Color.RED : color);
		g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 2, 2);
	}

	public void drawKeyframeMarkers(Graphics g, int loc) {
//		boolean mouseOver = timeAndKey.getValue() == mouseOverFrame;
		boolean mouseOver = false;
		boolean translation = false, rotation = false, scaling = false, other = false;
		for (AnimFlag<?> af : animFlags) {
			boolean afTranslation = "Translation".equals(af.getName());
//			translation |= afTranslation;
//			translation = translation || af.getName().equals("Translation");
			translation = translation || afTranslation;
			boolean afRotation = "Rotation".equals(af.getName());
//			rotation |= afRotation;
			rotation = rotation || afRotation;
			boolean afScaling = "Scaling".equals(af.getName());
//			scaling |= afScaling;
			scaling = scaling || afScaling;
			other |= !(afTranslation || afRotation || afScaling);
//			boolean afTranslation = "Translation".equals(af.getName());
//			translation |= afTranslation;
//			boolean afRotation = "Rotation".equals(af.getName());
//			rotation |= afRotation;
//			boolean afScaling = "Scaling".equals(af.getName());
//			scaling |= afScaling;
//			other |= !(afTranslation || afRotation || afScaling);
		}
		List<Color> colors = new ArrayList<>();
		if (scaling) {
			colors.add(scaleCol);
//			((Graphics2D) g).setPaint(keyframePaintRed);
		}
		if (rotation) {
			colors.add(rotCol);
//			((Graphics2D) g).setPaint(keyframePaint);
		}
		if (translation) {
			colors.add(transCol);
//			((Graphics2D) g).setPaint(keyframePaintBlue);
		}
		if (other) {
			colors.add(elseCol);
//			((Graphics2D) g).setPaint(keyframePaint);
		}
		System.out.println("colors: " + colors.size());
		((Graphics2D) g).setPaint(getGrad(colors, loc));
		g.fillRoundRect(loc - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT, 2, 2);
		Color color = Color.YELLOW;
		if (scaling) {
			color = Color.ORANGE;
		} else if (rotation) {
			color = Color.GREEN;
		} else if (translation) {
			color = Color.BLUE;
		}
		g.setColor(mouseOver ? Color.RED : color);
		g.drawRoundRect(loc - width / 2, VERTICAL_SLIDER_HEIGHT, width, VERTICAL_TICKS_HEIGHT, 2, 2);
	}

	private LinearGradientPaint getGrad(List<Color> colors, int loc) {
//		Color[] colors1 = {new Color(0, 255,0,170),
//				new Color(255, 0,0,170),
//				new Color(0, 0,255,170),
//				new Color(255, 255,0,170),};
		if (colors.size() == 1) {
//			System.out.println("was 1");
			colors.add(colors.get(0));
		}
		Color[] colors1 = colors.toArray(colors.toArray(new Color[0]));
		float[] fractions = new float[colors1.length];
		for (int i = 0; i < colors1.length; i++) {
			fractions[i] = i * 1f / (float) colors1.length;
		}

//		int y = VERTICAL_SLIDER_HEIGHT;
//		int h = VERTICAL_TICKS_HEIGHT;
//		int w = width;
		int y = 0;
		int h = getHeight();
		int w = getWidth();
//		return new LinearGradientPaint(0, y, 0, y+h, fractions, colors1);
		return new LinearGradientPaint(w / 2.5f + loc, y, w + loc, y + h, fractions, colors1);
	}

	private void fillWithGrad(Graphics g, List<Color> colors, int loc) {
		int nColors = colors.size();
		float[] fractions = new float[colors.size()];
//		for(int i = 0; i<nColors-1; i++){
//			Point p1 = new Point(0, 10);
//			Point p2 = new Point(0, 10);
//			((Graphics2D) g).setPaint(new GradientPaint(p1, colors.get(i), p2, colors.get(i+1)));
//		}
		for (int i = 0; i < nColors; i++) {
			fractions[i] = i * VERTICAL_TICKS_HEIGHT / (float) nColors;
		}
		Color[] colors1 = colors.toArray(colors.toArray(new Color[0]));
		MultipleGradientPaint.CycleMethod repeat = MultipleGradientPaint.CycleMethod.REPEAT;
		int type = rotCol.getColorSpace().getType();
		int scaleType = AffineTransform.TYPE_GENERAL_SCALE;

		LinearGradientPaint multipleGradientPaint = new LinearGradientPaint(0, 0, 1, 1, fractions, colors1);
//		LinearGradientPaint multipleGradientPaint = new LinearGradientPaint(fractions, colors1, repeat, type, scaleType);

//		GradientPaint paint = new GradientPaint();
		((Graphics2D) g).setPaint(multipleGradientPaint);

		g.fillRoundRect(loc - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT, 2, 2);
	}

	private boolean showTimeSliderPopup(MouseEvent mouseEvent, ModelStructureChangeListener changeListener, UndoManager undoManager, Sequence sequence) {
//		popupMenu.removeAll();
		JPopupMenu popupMenu = new JPopupMenu();

		JMenuItem timeIndicator = new JMenuItem("" + time);
		timeIndicator.setEnabled(false);
		popupMenu.add(timeIndicator);
		popupMenu.addSeparator();

		JMenuItem deleteAll = new JMenuItem("Delete All");
		deleteAll.addActionListener(e -> deleteKeyframes("delete keyframe", changeListener, time, objects, undoManager));
		popupMenu.add(deleteAll);
		popupMenu.addSeparator();

		JMenuItem cutItem = new JMenuItem("Cut");
//		cutItem.addActionListener(e -> cutItem(timeAndKey, changeListener));
		popupMenu.add(cutItem);

		JMenuItem copyItem = new JMenuItem("Copy");
//		copyItem.addActionListener(e -> copyKeyframes(changeListener, time));
		popupMenu.add(copyItem);

//		final JMenuItem copyFrameItem = new JMenuItem("Copy Frame (whole model)");
		JMenuItem copyFrameItem = new JMenuItem("Copy All Frames At " + time + "(whole model)");
//		copyFrameItem.addActionListener(e -> copyAllKeyframes(time));
		popupMenu.add(copyFrameItem);

		JMenuItem pasteItem = new JMenuItem("Paste");
//		pasteItem.addActionListener(e -> pasteToAllSelected(changeListener, time));
		popupMenu.add(pasteItem);

		popupMenu.addSeparator();

		for (IdObject object : objects) {
			for (AnimFlag<?> flag : object.getAnimFlags()) {
				TreeMap<Integer, ? extends Entry<?>> entryMap = flag.getEntryMap(sequence);
				if (!entryMap.isEmpty()) {
					JMenu subMenu = new JMenu(object.getName() + ": " + flag.getName());
					popupMenu.add(subMenu);

					JMenuItem deleteSpecificItem = new JMenuItem("Delete");
					deleteSpecificItem.addActionListener(e -> deleteKeyframe("delete keyframe", changeListener, flag, time, undoManager, sequence));
					subMenu.add(deleteSpecificItem);
					subMenu.addSeparator();

					JMenuItem cutSpecificItem = new JMenuItem("Cut");
//					cutSpecificItem.addActionListener(e -> cutSpecificItem(timeAndKey, changeListener, object, flag));
					subMenu.add(cutSpecificItem);

					JMenuItem copySpecificItem = new JMenuItem("Copy");
//					copySpecificItem.addActionListener(e -> copyKeyframes(changeListener, object, flag, timeAndKey.getKey()));
					subMenu.add(copySpecificItem);

					JMenuItem pasteSpecificItem = new JMenuItem("Paste");
//					pasteSpecificItem.addActionListener(e -> pasteToSpecificTimeline(changeListener, timeAndKey, flag));
					subMenu.add(pasteSpecificItem);
				}
			}
		}
//		popupMenu.show(TimeSliderPanel.this, mouseEvent.getX(), mouseEvent.getY());
		popupMenu.show(null, mouseEvent.getXOnScreen(), mouseEvent.getYOnScreen());
		return true;
	}

	public void deleteSelectedKeyframes(ModelStructureChangeListener changeListener, UndoManager undoManager) {
		deleteKeyframes("delete keyframe", changeListener, time, objects, undoManager);

//		revalidateKeyframeDisplay();
	}

	private void deleteKeyframes(String actionName, ModelStructureChangeListener changeListener, int trackTime, Collection<IdObject> objects, UndoManager undoManager) {
		List<UndoAction> actions = new ArrayList<>();
		for (IdObject object : objects) {
			for (AnimFlag<?> flag : object.getAnimFlags()) {
//				int flooredTimeIndex = flag.floorIndex(trackTime);
//
//				ReversedAction deleteFrameAction = getDeleteAction(actionName, changeListener, object, flag, trackTime, flooredTimeIndex);
//				if (deleteFrameAction != null) {
//					actions.add(deleteFrameAction);
//				}
			}
		}
		// TODO build one action for performance, so that the structure change notifier is not called N times, where N is the number of selected timelines
		CompoundAction action = new CompoundAction(actionName, actions);
		action.redo();
		undoManager.pushAction(action);
	}

	private void deleteKeyframe(String actionName, ModelStructureChangeListener changeListener, AnimFlag<?> flag, int trackTime, UndoManager undoManager, Sequence sequence) {
		ReversedAction deleteFrameAction = getDeleteAction(actionName, changeListener, flag, trackTime, sequence);
		if (deleteFrameAction != null) {
			deleteFrameAction.redo();
			undoManager.pushAction(deleteFrameAction);
		}
	}

	private <Q> ReversedAction getDeleteAction(String actionName, ModelStructureChangeListener changeListener, AnimFlag<Q> flag, int trackTime, Sequence sequence) {
		ReversedAction deleteFrameAction;
		TreeMap<Integer, Entry<Q>> entryMap = flag.getEntryMap(sequence);
		if (!entryMap.isEmpty()) {
			Entry<Q> entry = entryMap.get(trackTime).deepCopy().setTime(time);
			deleteFrameAction = new ReversedAction(actionName, new AddFlagEntryAction<>(flag, entry, sequence, changeListener));
		} else {
			deleteFrameAction = null;
		}
		return deleteFrameAction;
	}

//	private void cutItem(ModelStructureChangeListener changeListener, UndoActionListener undoManager) {
//		copyKeyframes(changeListener, time);
//		deleteKeyframes("cut keyframe", changeListener, time, objects, undoManager);
//	}
//
////	List<TimeSliderPanel.CopiedKeyFrame> copiedKeyframes;
//	private void copyKeyframes(ModelStructureChangeListener structureChangeListener, int trackTime) {
////		copiedKeyframes.clear();
////		useAllCopiedKeyframes = false;
//		for (IdObject object : objects) {
//			for (AnimFlag<?> flag : object.getAnimFlags()) {
////				Integer currentEditorGlobalSeq = timeEnvironmentImpl.getGlobalSeq();
//				Integer currentEditorGlobalSeq = null;
//				if (((flag.getGlobalSeq() == null) && (currentEditorGlobalSeq == null)) || ((currentEditorGlobalSeq != null) && currentEditorGlobalSeq.equals(flag.getGlobalSeq()))) {
//					copuKeyframes(object, flag, trackTime);
//				}
//			}
//		}
//	}
//
//	private void copyKeyframes(ModelStructureChangeListener structureChangeListener, IdObject object, AnimFlag<?> flag, int trackTime) {
////		copiedKeyframes.clear();
////		useAllCopiedKeyframes = false;
//		copuKeyframes(object, flag, trackTime);
//	}
//
//	private void copuKeyframes(IdObject object, AnimFlag<?> flag, int trackTime) {
//		int flooredTimeIndex = flag.floorIndex(trackTime);
//		if ((flooredTimeIndex != -1) && (flooredTimeIndex < flag.getTimes().size()) && (flag.getTimes().get(flooredTimeIndex).equals(trackTime))) {
//			Object value = flag.getValues().get(flooredTimeIndex);
//			copiedKeyframes.add(new TimeSliderPanel.CopiedKeyFrame(object, flag, new AnimFlag.Entry(flag.getEntry(flooredTimeIndex))));
////			if (flag.tans()) {
////				copiedKeyframes.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value), AnimFlag.cloneValue(flag.getInTans().get(flooredTimeIndex)), AnimFlag.cloneValue(flag.getOutTans().get(flooredTimeIndex))));
////			} else {
////				copiedKeyframes.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value), null, null));
////			}
//		} else {
//			Object value = flag.interpolateAt(timeEnvironmentImpl);
//			if (flag.tans()) {
//				copiedKeyframes.add(new TimeSliderPanel.CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value), AnimFlag.cloneValue(value), AnimFlag.cloneValue(value)));
//			} else {
//				copiedKeyframes.add(new TimeSliderPanel.CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value), null, null));
//			}
//		}
//	}
//
//	private void copyAllKeyframes(int trackTime) {
//		copiedKeyframes.clear();
//		useAllCopiedKeyframes = true;
//		for (IdObject object : modelView.getModel().getIdObjects()) {
//			for (AnimFlag<?> flag : object.getAnimFlags()) {
//				Integer currentEditorGlobalSeq = timeEnvironmentImpl.getGlobalSeq();
//				if (((flag.getGlobalSeq() == null) && (currentEditorGlobalSeq == null)) || ((currentEditorGlobalSeq != null) && currentEditorGlobalSeq.equals(flag.getGlobalSeq()))) {
//					copuKeyframes(object, flag, trackTime);
//				}
//			}
//		}
//	}
}
