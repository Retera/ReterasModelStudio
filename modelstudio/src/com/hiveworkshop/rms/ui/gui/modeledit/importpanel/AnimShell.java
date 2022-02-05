package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnimShell {
	private final Animation anim;
	private final List<AnimShell> animDataDests = new ArrayList<>();
	private AnimShell animDataSrc; // animation to replace this animation
	private boolean reverse = false;
	private ImportType importType = ImportType.IMPORT_BASIC;
	private String name;
	private final String oldName;
	private final boolean isFromDonating;

	public AnimShell(final Animation anim) {
		this(anim, false);
	}

	public AnimShell(final Animation anim, boolean isFromDonating) {
		this.anim = anim;
		name = anim.getName();
		oldName = anim.getName();
		this.isFromDonating = isFromDonating;
	}

	public Animation getAnim() {
		return anim;
	}

//	public AnimShell setAnim(Animation anim) {
//		this.anim = anim;
//		return this;
//	}

	public Animation getAnimDataSrcAnim() {
		if (animDataSrc == null) {
			return null;
		}
		return animDataSrc.getAnim();
	}

	public boolean isReverse() {
		return reverse;
	}

	public AnimShell setReverse(boolean reverse) {
		this.reverse = reverse;
		return this;
	}

	public ImportType getImportType() {
		return importType;
	}

	public AnimShell setImportType(int importType) {
		if(0 <= importType && importType < ImportType.values().length) {
			this.importType = ImportType.fromInt(importType);
		}
		return this;
	}

	public AnimShell setImportType(ImportType importType) {
		this.importType = importType;
		return this;
	}

	public String getName() {
		return name;
	}

	public AnimShell setName(String name) {
		this.name = name;
		return this;
	}

	public List<AnimShell> getAnimDataDests() {
		return animDataDests;
	}

	public void addAnimDataDest(AnimShell animShell) {
		if (!animDataDests.contains(animShell)) {
			animDataDests.add(animShell);
			animShell.setAnimDataSrc(this);
			importType = ImportType.TIMESCALE_INTO;
			if (animDataSrc != null) {
				animDataSrc.removeAnimDataDest(this);
				animDataSrc = null;
			}
		}
	}

	public void addAnimDataDest(List<AnimShell> animShells) {
		animDataDests.addAll(animShells);
		for (AnimShell animShell : animShells) {
			animShell.setAnimDataSrc(this);
		}
		importType = ImportType.TIMESCALE_INTO;
		if (animDataSrc != null) {
			animDataSrc.removeAnimDataDest(this);
			animDataSrc = null;
		}
	}

	public void removeAnimDataDest(AnimShell animShell) {
//		animDataDests.remove(animShell);
		if (animDataDests.remove(animShell) && animShell.getAnimDataSrc() == this) {
			animShell.setAnimDataSrc(null);
		}
	}

	public void removeAnimDataDest(List<AnimShell> animShells) {
//		animDataDests.removeAll(animShells);
		for (AnimShell animShell : animShells) {
			animShell.setAnimDataSrc(null);
		}
	}

	public void setAnimDataDestList(List<AnimShell> animShells) {
		for (AnimShell animShell : animDataDests) {
			animShell.setAnimDataSrc(null);
		}
		animDataDests.clear();
		for (AnimShell animShell : animShells) {
			animShell.setAnimDataSrc(this);
		}
	}


	public String getOldName() {
		return oldName;
	}

	public String displName() {
		String dispName = "";
		switch (importType) {
			case DONT_IMPORT -> dispName += "\u2297";
			case IMPORT_BASIC -> dispName += "\u24BE";
			case CHANGE_NAME -> dispName += "\u24C3";
			case TIMESCALE_INTO -> dispName += "\u24C9";
			case GLOBALSEQ -> dispName += "\u24BC";
		}
		return dispName + "  " + oldName;
	}

	public AnimShell getAnimDataSrc() {
		return animDataSrc;
	}

	public AnimShell setAnimDataSrc(AnimShell animDataSrc) {
		if(this.animDataSrc != animDataSrc){
			if (this.animDataSrc != null) {
				this.animDataSrc.removeAnimDataDest(this);
			}
			this.animDataSrc = animDataSrc;
			if (animDataSrc != null) {
				animDataSrc.addAnimDataDest(this);
				for (AnimShell animShell : animDataDests) {
					animShell.setAnimDataSrc(null);
				}
				importType = ImportType.TIMESCALE_RECEIVE;
			}
		}
		return this;
	}

	public boolean isFromDonating() {
		return isFromDonating;
	}

	public enum ImportType {
		DONT_IMPORT("Do Not Import"),
		IMPORT_BASIC("Import as-is"),
		CHANGE_NAME("Change name to:"),
		//		TIMESCALE_INTO("Time-scale into pre-existing:"),
		TIMESCALE_INTO("Time-scale into:"),
		TIMESCALE_RECEIVE("Replace with:"),
		GLOBALSEQ("Rebuild as global sequence");
		String dispText;

		ImportType(String s) {
			dispText = s;
		}

		public static String[] getDispList() {
			return Arrays.stream(values()).map(ImportType::getDispText).toArray(String[]::new);
		}

		public static ImportType fromInt(int i) {
			return values()[i];
		}

		public String getDispText() {
			return dispText;
		}

		@Override
		public String toString() {
			return dispText;
		}
	}

}
