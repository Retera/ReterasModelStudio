package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.VisibilityShell;
import com.hiveworkshop.rms.ui.util.colorchooser.CharIcon;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class VisListCellRenderer extends DefaultListCellRenderer {
	protected static final Vec3 recCV = new Vec3(200, 255, 255);
	protected static final Vec3 donCV = new Vec3(220, 180, 255);
	protected static final Color recC = recCV.asIntColor();
	protected static final Color donC = donCV.asIntColor();
	public VisListCellRenderer() {
	}

	public VisListCellRenderer(boolean showLength) {
		this.showLength = showLength;
	}

	private static final Vec3 selectedOwnerBgCol = new Vec3(130, 230, 170);
	private static final Vec3 timeScaleBgCol = new Vec3(150, 170, 230);
	private static final Color timeScaleBgCol1 = new Color(150, 170, 230);
	private static final Vec3 selectedOwnerFgCol = new Vec3(0, 0, 0);
	private static final Vec3 otherOwnerBgCol = new Vec3(160, 160, 160);
	private static final Vec3 otherOwnerFgCol = new Vec3(60, 60, 60);
	private static final Vec3 noOwnerBgCol = new Vec3(255, 255, 255);
	private static final Vec3 dontImpBgCol = new Vec3(240, 200, 200);
	private static final Vec3 noOwnerFgCol = new Vec3(0, 0, 0);
	private static final Vec3 hLAdjBgCol = new Vec3(0, 0, 50);
	private static final Vec3 bg = new Vec3();
	private static final Vec3 fg = new Vec3();

	boolean showLength = false;
	VisibilityShell<?> selectedVis;
	Set<VisibilityShell<?>> selectedVisShells = new HashSet<>();
	boolean destList = false;

	public VisListCellRenderer setDestList(boolean destList) {
		this.destList = destList;
		return this;
	}

	public void setSelectedVis(VisibilityShell<?> visShell) {
		selectedVisShells.clear();
		selectedVis = visShell;
	}
	public void setSelectedVisibilities(Collection<VisibilityShell<?>> visShells) {
		selectedVisShells.clear();
		selectedVis = null;
		selectedVisShells.addAll(visShells);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
//		String visName = ((VisibilityShell<?>) value).getOldName() + " " + ((VisibilityShell<?>) value).getAnim().getLength();
		VisibilityShell<?> visShell = (VisibilityShell<?>) value;
//		String visName = visShell.getName() + " " + visShell.getAnim().getLength();
		String visName = value.toString();

		super.getListCellRendererComponent(list, visName, index, isSel, hasFoc);
		VisibilityShell<?> visSource = visShell.getVisSource();
//		Vec3 bg;
//		Vec3 fg;

		if(selectedVisShells.isEmpty()){
			if (selectedVis != null && (destList && visSource == selectedVis || !destList && selectedVis.getVisSource() == visShell)) {
				bg.set(selectedOwnerBgCol);
				fg.set(selectedOwnerFgCol);
			} else if (destList && visSource != null && selectedVis != null) {
				bg.set(otherOwnerBgCol);
				fg.set(otherOwnerFgCol);
			} else {
				bg.set(noOwnerBgCol);
				fg.set(noOwnerFgCol);
			}
		} else {
			if(!destList){

				fg.set(noOwnerFgCol);
				long count = selectedVisShells.stream().filter(o -> o.getVisSource() == visShell).count();
				if (count == 0) {
					bg.set(noOwnerBgCol);
				} else if (count == selectedVisShells.size()){
					bg.set(selectedOwnerBgCol);
				} else {
					bg.set(selectedOwnerBgCol).add(noOwnerBgCol).scale(0.5f);
				}
			} else {
				if(visSource == null){
					bg.set(noOwnerBgCol);
					fg.set(noOwnerFgCol);
				} else if(selectedVisShells.contains(visSource)){
					bg.set(selectedOwnerBgCol);
					fg.set(selectedOwnerFgCol);
				} else {
					bg.set(otherOwnerBgCol);
					fg.set(otherOwnerFgCol);
				}
			}
		}


		if (isSel) {
			bg.add(hLAdjBgCol);
		}
		this.setBackground(bg.asIntColor());
		this.setForeground(fg.asIntColor());
		boolean fD = visShell.isFromDonating();
//		CharIcon charIcon = new CharIcon(fD ? "+" : "@", 7, fD ? donC : recC, 14);
		CharIcon charIcon = new CharIcon(fD ? "&" : "#", 7, fD ? donC : recC, 16, 2);
//		CharIconLabel charIconLabel = new CharIconLabel(fD ? "+" : "@", 5, fD ? donC : recC, 14);
//		setIcon(ImportPanel.animIcon); // todo choose icon based on import status
//		setIcon(charIconLabel.getIcon()); // todo choose icon based on import status
		setIcon(charIcon); // todo choose icon based on import status
		return this;
	}
}
