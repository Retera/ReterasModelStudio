package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.AnimShell;
import com.hiveworkshop.rms.ui.util.colorchooser.CharIcon;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AnimListCellRenderer extends DefaultListCellRenderer {
	protected static final Vec3 recCV = new Vec3(200, 255, 255);
	protected static final Vec3 donCV = new Vec3(220, 180, 255);
	protected static final Color recC = recCV.asIntColor();
	protected static final Color donC = donCV.asIntColor();

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
	AnimShell selectedAnim;
	Set<AnimShell> selectedAnims = new HashSet<>();
	boolean markDontImp;
	boolean destList = false;

	public AnimListCellRenderer() {
	}

	public AnimListCellRenderer(boolean showLength) {
		this.showLength = showLength;
	}

	public AnimListCellRenderer setMarkDontImp(boolean markDontImp) {
		this.markDontImp = markDontImp;
		return this;
	}

	public AnimListCellRenderer setDestList(boolean destList) {
		this.destList = destList;
		return this;
	}

	public void setSelectedAnim(AnimShell animShell) {
		selectedAnims.clear();
		selectedAnim = animShell;
	}
	public void setSelectedAnims(Collection<AnimShell> animShells) {
		selectedAnims.clear();
		selectedAnim = null;
		selectedAnims.addAll(animShells);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
		AnimShell animShell = (AnimShell) value;
		String animName = animShell.getDisplayName();

		super.getListCellRendererComponent(list, animName, index, isSel, hasFoc);
		AnimShell animDataSrc = animShell.getAnimDataSrc();

		if(selectedAnims.isEmpty()){
			if (selectedAnim != null && (destList && animDataSrc == selectedAnim || !destList && selectedAnim.getAnimDataSrc() == animShell)) {
				bg.set(selectedOwnerBgCol);
				fg.set(selectedOwnerFgCol);
			} else if (destList && animDataSrc != null && selectedAnim != null) {
				bg.set(otherOwnerBgCol);
				fg.set(otherOwnerFgCol);
			} else if (!animShell.isDoImport() && markDontImp) {
				bg.set(dontImpBgCol);
				fg.set(otherOwnerFgCol);
			} else if (animShell.getImportType() == AnimShell.ImportType.TIMESCALE_INTO) {
				bg.set(timeScaleBgCol);
				fg.set(noOwnerFgCol);
			} else {
				bg.set(noOwnerBgCol);
				fg.set(noOwnerFgCol);
			}
		} else {
			if(!destList){
				bg.set(Vec3.ZERO);
				fg.set(noOwnerFgCol);
				for (AnimShell as : selectedAnims){
					if(as.getAnimDataSrc() == animShell){
						bg.add(selectedOwnerBgCol);
					} else {
						bg.add(noOwnerBgCol);
					}
				}
				bg.scale(1f/(float) selectedAnims.size());
			} else {
				if(!animShell.isDoImport() && markDontImp){
					bg.set(dontImpBgCol);
					fg.set(otherOwnerFgCol);
				} else if(animDataSrc == null){
					bg.set(noOwnerBgCol);
					fg.set(noOwnerFgCol);
				} else if(selectedAnims.contains(animDataSrc)){
					bg.set(selectedOwnerBgCol);
					fg.set(selectedOwnerFgCol);
				} else {
					bg.set(otherOwnerBgCol);
					fg.set(otherOwnerFgCol);
				}
			}
//			if (selectedAnim != null && (destList && animDataSrc == selectedAnim || !destList && selectedAnim.getAnimDataSrc() == animShell)) {
//				bg.set(selectedOwnerBgCol);
//				fg.set(selectedOwnerFgCol);
//			} else if (destList && animDataSrc != null && selectedAnim != null) {
//				bg.set(otherOwnerBgCol);
//				fg.set(otherOwnerFgCol);
//			} else if (!animShell.isDoImport() && markDontImp) {
//				bg.set(dontImpBgCol);
//				fg.set(otherOwnerFgCol);
//			} else if (animShell.getImportType() == AnimShell.ImportType.TIMESCALE_INTO) {
//				bg.set(timeScaleBgCol);
//				fg.set(noOwnerFgCol);
//			} else {
//				bg.set(noOwnerBgCol);
//				fg.set(noOwnerFgCol);
//			}
		}


		if (isSel) {
			bg.add(hLAdjBgCol);
		}
		this.setBackground(bg.asIntColor());
		this.setForeground(fg.asIntColor());
		boolean fD = animShell.isFromDonating();
		CharIcon charIcon = new CharIcon(fD ? "&" : "#", 7, fD ? donC : recC, 16, 2);
		setIcon(charIcon); // todo choose icon based on import status
		return this;
	}

	private String getAnimName(AnimShell value){
		if(showLength) {
			return value.getOldName() + " (" + value.getAnim().getLength() + ")";
		}
		return value.getOldName();
	}
}
