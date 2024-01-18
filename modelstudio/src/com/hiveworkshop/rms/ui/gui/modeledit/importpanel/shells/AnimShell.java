package com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells;

import com.hiveworkshop.rms.editor.model.Animation;

import java.util.ArrayList;
import java.util.List;

public class AnimShell extends AbstractShell {
	private final Animation anim;
	private final List<AnimShell> animDataDests = new ArrayList<>(); // animations in which bones lacking anim data should use data from this animation
	private AnimShell animDataSrc; // animation to replace this animation where bones are missing anim data
	private boolean reverse = false;
	private boolean doImport = true;
	private String name;
	private final String oldName;

	public AnimShell(final Animation anim) {
		this(anim, false);
	}

	public AnimShell(final Animation anim, boolean isFromDonating) {
		super(isFromDonating);
		this.anim = anim;
		name = anim.getName();
		oldName = anim.getName();
	}

	public Animation getAnim() {
		return anim;
	}

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

	public AnimShell setDoImport(boolean doImport) {
		this.doImport = doImport;
		return this;
	}

	public boolean isDoImport() {
		return doImport;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
//		String prefix = isFromDonating ? "& " : "# ";
		String prefix = "";
		if (oldName.equals(name)){
			return prefix + name + " " + anim.getLength();
		} else {
			return prefix + name + " " + anim.getLength() + " (" + oldName + ")";
		}
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
//			importType = ImportType.TIMESCALE_INTO;

//			if (animDataSrc != null) {
//				animDataSrc.removeAnimDataDest(this);
//				animDataSrc = null;
//			}
		}
	}

	public void addAnimDataDest(List<AnimShell> animShells) {
		animDataDests.addAll(animShells);
		for (AnimShell animShell : animShells) {
			animShell.setAnimDataSrc(this);
		}
//		importType = ImportType.TIMESCALE_INTO;
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
			}
		}
		return this;
	}

}
