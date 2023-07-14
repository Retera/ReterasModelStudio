package com.hiveworkshop.rms.ui.application.edit.animation.altTimeline;

import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.util.MathUtils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.TreeMap;
import java.util.function.Function;

public class TempFloatGraph {
	private Function<Integer, Float> graphLineFunction;
	private int[] ints;
	private float[] floats;
	private TreeMap<Integer, Entry<Float>> entryMap = new TreeMap<>();
	private Entry<Float> selectedEntry;
	int tanOffs = 10;

	public TempFloatGraph(Function<Integer, Float> graphLineFunction, int[] ints, float[] floats){
		for(int i : ints){
			Entry<Float> entry = new Entry<>(i, graphLineFunction.apply(i));
			entryMap.put(i, entry);
		}
		this.graphLineFunction = this::interpolateAt;
		this.ints = ints;
		this.floats = floats;

	}
	public TempFloatGraph(Function<Integer, Float> graphLineFunction, int[] ints){
		int timeOff = 15;
		for(int i : ints){
			Entry<Float> entry = new Entry<>(i, graphLineFunction.apply(i), graphLineFunction.apply(i-timeOff), graphLineFunction.apply(i+timeOff));
			entryMap.put(i, entry);
		}
		this.graphLineFunction = this::interpolateAt;
		this.ints = ints;
	}

	public void drawGraphLine(Graphics g, int tStart, int tEnd, int res){
		int step = (tEnd - tStart)/(res + 1);
		g.setColor(Color.BLACK);
		for(int i = tStart; i<= tEnd; i+=step){
			Float v1 = interpolateAt(i);
			Float v2 = interpolateAt(i + step);
			if(v1 != null && v2 != null){
				int vi1 = (int) (0 + v1);
				int vi2 = (int) (0 + v2);
				g.drawLine(i, vi1, i+step, vi2);
			}
		}
		if(selectedEntry != null && selectedEntry.getInTan() != null && selectedEntry.getInTan() != null){
			g.setColor(Color.MAGENTA);
			g.drawLine(selectedEntry.getTime()-tanOffs, (int) (0+selectedEntry.getInTan()), selectedEntry.getTime(),((int)(0+selectedEntry.getValue())));
			g.drawLine(selectedEntry.getTime()+tanOffs, (int) (0+selectedEntry.getOutTan()), selectedEntry.getTime(), ((int)(0+selectedEntry.getValue())));
		}
	}
	public void drawGraphMarkers(Graphics g, int tStart, int tEnd, int res){
		for(int i : entryMap.keySet()){
			if(tStart <= i && i <= tEnd){
				Entry<Float> entry = entryMap.get(i);
				if(selectedEntry == entry){
					g.setColor(Color.MAGENTA);
					Float inTan = entry.getInTan();
					Float outTan = entry.getOutTan();
					if(inTan != null && outTan != null){
						g.drawOval(i-2-tanOffs, (int) (inTan-2), 4, 4);
						g.drawOval(i-2+tanOffs, (int) (outTan-2), 4, 4);
					}
				} else {
					g.setColor(Color.BLACK);
				}
				Float vi = entry.getValue();
				if(vi != null){
					g.fillOval(i-2, (int) (vi-2), 4, 4);
					g.drawOval(i-2, (int) (vi-2), 4, 4);

				}
			}
		}
	}

	public Entry<Float> pointIsOnValue(MouseEvent e){
		if(selectedEntry != null
				&& selectedEntry.getInTan() != null
				&& (Math.abs(e.getX()-selectedEntry.getTime()) == tanOffs || Math.abs(e.getX()-selectedEntry.getTime())-5 <= 5)){
			if (Math.abs(e.getY() - selectedEntry.getInTan()) <= 5){
				return selectedEntry;
			} else if(Math.abs(e.getY() - selectedEntry.getOutTan()) <= 5){
				return selectedEntry;
			}

		}
		for(int i : entryMap.keySet()){
			if(Math.abs(e.getX()-i) <= 5 && Math.abs(e.getY() - entryMap.get(i).value) <= 5){
				return entryMap.get(i);
			}
		}
		return null;
	}


	public void shiftPoint(int xOld, int xNew, int yNew){
		if(selectedEntry != null
				&& selectedEntry.getInTan() != null
				&& Math.abs(xOld-selectedEntry.getTime()) == tanOffs){
			if (xOld-selectedEntry.getTime() == tanOffs){
				selectedEntry.setOutTan((float) yNew);
			} else if(xOld-selectedEntry.getTime() == -tanOffs){
				selectedEntry.setInTan((float) yNew);
			}
		} else {
			Entry<Float> remove = entryMap.remove(xOld);
			if(remove != null){
				remove.setTime(xNew);
				remove.setValue((float) yNew);
				entryMap.put(xNew, remove);
			}
		}
	}

	public boolean hasSelected(){
		return selectedEntry != null;
	}

	public TempFloatGraph setSelected(Entry<Float> selectedEntry) {
		this.selectedEntry = selectedEntry;
		return this;
	}

	public Float interpolateAt(int time) {
		int sequenceLength = 100;

		Integer lastKeyframeTime = entryMap.floorKey(sequenceLength);
		Integer firstKeyframeTime = entryMap.ceilingKey(0);


		if (lastKeyframeTime == null
				|| firstKeyframeTime == null
				|| lastKeyframeTime < firstKeyframeTime
				|| sequenceLength < time
				|| time < 0) {
			return 0.0f;
		}

		// either no keyframes before animationEnd,
		// no keyframes after animationStart,
		// no keyframes in animation
		// or time is outside of animation
		// only one keyframe in the animation
		if (lastKeyframeTime.equals(firstKeyframeTime)) {
			return entryMap.get(lastKeyframeTime).getValue();
		}

		Integer floorTime = entryMap.floorKey(time);
		if (floorTime == null || floorTime < 0) {
			floorTime = lastKeyframeTime;
		}

		Integer ceilTime = entryMap.ceilingKey(time);
		if (ceilTime == null || ceilTime > sequenceLength) {
			ceilTime = firstKeyframeTime;
		}

		if (floorTime.equals(ceilTime)) {
			return entryMap.get(floorTime).getValue();
		}

		float timeFactor = getTimeFactor(time, sequenceLength, floorTime, ceilTime);

		return getInterpolatedValue(floorTime, ceilTime, timeFactor);
	}

	public Float getInterpolatedValue(Integer floorTime, Integer ceilTime, float timeFactor) {
		Entry<Float> entryFloor = entryMap.get(floorTime);
		Entry<Float> entryCeil = entryMap.get(ceilTime);
		return getInterpolatedValue(entryFloor, entryCeil, timeFactor);
	}

	public Float getInterpolatedValue(Entry<Float> entryFloor, Entry<Float> entryCeil, float timeFactor) {
		Float floorValue = entryFloor.getValue();
		Float floorOutTan = entryFloor.getOutTan();

		Float ceilValue = entryCeil.getValue();
		Float ceilInTan = entryCeil.getInTan();

		if(ceilInTan != null){
			return MathUtils.hermite(floorValue, floorOutTan, ceilInTan, ceilValue, timeFactor);
		} else {
			return MathUtils.lerp(floorValue, ceilValue, timeFactor);
		}
	}


	protected float getTimeFactor(int time, int animationLength, Integer floorTime, Integer ceilTime) {
		int timeBetweenFrames = ceilTime - floorTime;

		// if ceilTime wrapped, add animation length
		if (timeBetweenFrames < 0) {
			timeBetweenFrames = timeBetweenFrames + animationLength;
		}

		int timeFromKF = time - floorTime;
		// if floorTime wrapped, add animation length
		if (timeFromKF < 0) {
			timeFromKF = timeFromKF + animationLength;
		}

		return timeFromKF / (float) timeBetweenFrames;
	}
}
