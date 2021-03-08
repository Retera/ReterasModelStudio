package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Animation;

import java.util.ArrayList;
import java.util.List;

class AnimShell {
	Animation anim;
	Animation importAnim;
	List<AnimShell> animShellList = new ArrayList<>();
	AnimShell importAnimShell;
	private boolean doImport = true;
	private boolean reverse = false;
	private int importType = 0;
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

	public List<AnimShell> getAnimShellList() {
		return animShellList;
	}

	public AnimShell setAnimShellList(List<AnimShell> animShellList) {
		this.animShellList = animShellList;
		return this;
	}

	public boolean isDoImport() {
		return doImport;
	}

	public AnimShell setDoImport(boolean doImport) {
		this.doImport = doImport;
		return this;
	}

	public boolean isReverse() {
		return reverse;
	}

	public AnimShell setReverse(boolean reverse) {
		this.reverse = reverse;
		return this;
	}

	public int getImportType() {
		return importType;
	}

	public AnimShell setImportType(int importType) {
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
		animShellList.add(animShell);
	}

	public void addToList(List<AnimShell> animShells) {
		animShellList.addAll(animShells);
	}

	public void removeFromList(AnimShell animShell) {
		animShellList.remove(animShell);
	}

	public void removeFromList(List<AnimShell> animShells) {
		animShellList.removeAll(animShells);
	}

	public void setList(List<AnimShell> animShells) {
		animShellList.removeAll(animShells);
		animShellList.addAll(animShells);
	}


	public String getOldName() {
		return oldName;
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
}
