package com.hiveworkshop.rms.ui.application.edit.animation.altTimeline;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class TempGraphHolder {
	List<TempFloatGraph> graphList = new ArrayList<>();
	Float dragPointValue;
	Integer dragPointTime;

	public TempGraphHolder() {

	}
	public void addGraph(TempFloatGraph graph){
		graphList.add(graph);
	}
	public void paintGraphs(Graphics g){
		for(TempFloatGraph graph : graphList){
			graph.drawGraphLine(g, 0, 100, 20);
		}
		for(TempFloatGraph graph : graphList){
			graph.drawGraphMarkers(g, 0, 100, 20);
		}
		if(dragPointTime != null && dragPointValue != null){
			g.setColor(new Color(255, 0, 255, 128));
			g.fillOval(dragPointTime-2, (int) (dragPointValue-2), 4, 4);
			g.drawOval(dragPointTime-2, (int) (dragPointValue-2), 4, 4);

		}
	}

	public TempFloatGraph getMousePointGraph(MouseEvent e){
		for(TempFloatGraph graph : graphList){
			if(graph.pointIsOnValue(e) != null){
				return graph;
			}
		}
		return null;
	}

	public TempGraphHolder setDragPoint(Integer dragPointTime, Float dragPointValue) {
		System.out.println("dragPointTime set!: " + dragPointTime + ", value: " + dragPointValue);
		this.dragPointTime = dragPointTime;
		this.dragPointValue = dragPointValue;
		return this;
	}
}