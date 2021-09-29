package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnimShell {
	private Animation anim;
	private Animation importAnim;
	private List<AnimShell> animShellsToTimeScaleInto = new ArrayList<>();
	private AnimShell importAnimShell;
	private boolean reverse = false;
	private ImportType importType = ImportType.IMPORTBASIC;
	private String name;
	private String oldName;

	public AnimShell(final Animation anim) {
		this.anim = anim;
		name = anim.getName();
		oldName = anim.getName();
	}

	public void setImportAnim(final Animation a) {
		importAnim = a;
	}

	public Animation getAnim() {
		return anim;
	}

	public AnimShell setAnim(Animation anim) {
		this.anim = anim;
		return this;
	}

	public Animation getImportAnim() {
		return importAnim;
	}

	public List<AnimShell> getAnimShellsToTimeScaleInto() {
		return animShellsToTimeScaleInto;
	}

	public AnimShell setAnimShellsToTimeScaleInto(List<AnimShell> animShellsToTimeScaleInto) {
		this.animShellsToTimeScaleInto = animShellsToTimeScaleInto;
		return this;
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
		this.importType = ImportType.fromInt(importType);
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

	public void addToList(AnimShell animShell) {
		animShellsToTimeScaleInto.add(animShell);
	}

	public void addToList(List<AnimShell> animShells) {
		animShellsToTimeScaleInto.addAll(animShells);
	}

	public void removeFromList(AnimShell animShell) {
		animShellsToTimeScaleInto.remove(animShell);
	}

	public void removeFromList(List<AnimShell> animShells) {
		animShellsToTimeScaleInto.removeAll(animShells);
	}

	public void setList(List<AnimShell> animShells) {
		animShellsToTimeScaleInto.removeAll(animShells);
		animShellsToTimeScaleInto.addAll(animShells);
	}


	public String getOldName() {
		return oldName;
	}

	public String displName() {
		String dispName = "";
		switch (importType) {
			case DONTIMPORT -> dispName += "\u2297";
			case IMPORTBASIC -> dispName += "\u24BE";
			case CHANGENAME -> dispName += "\u24C3";
			case TIMESCALE -> dispName += "\u24C9";
			case GLOBALSEQ -> dispName += "\u24BC";
		}
		return dispName + "  " + oldName;
	}

	public AnimShell setOldName(String oldName) {
		this.oldName = oldName;
		return this;
	}

	public AnimShell getImportAnimShell() {
		return importAnimShell;
	}

	public AnimShell setImportAnimShell(AnimShell importAnimShell) {
		this.importAnimShell = importAnimShell;
		return this;
	}

	public enum ImportType {
		DONTIMPORT("Do Not Import"), IMPORTBASIC("Import as-is"), CHANGENAME("Change name to:"), TIMESCALE("Time-scale into pre-existing:"), GLOBALSEQ("Rebuild as global sequence");
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
