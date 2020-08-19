package com.hiveworkshop.wc3.mdl;

import com.etheller.warsmash.parsers.mdlx.MdlxHelper;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;

/**
 * Write a description of class Helper here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Helper extends Bone { // Haha blizz
	public Helper(final String name) {
		super(name);
	}

	public Helper(final int j) {
		super();
	}

	public Helper(final Helper h) {
		super(h);
	}

	public Helper(final MdlxHelper helper) {
		if ((helper.flags & 1) != 0) {
			System.err.println("MDX -> MDL error: A helper '" + helper.name + "' not flagged as helper in MDX!");
		}

		loadObject(helper);
	}

	public MdlxHelper toMdlxHelper() {
		MdlxHelper helper = new MdlxHelper();

		objectToMdlx(helper);

		return helper;
	}

	// printTo is already written as a part of bone; these two things are
	// stupidly the same
	@Override
	public IdObject copy() {
		return new Helper(this);
	}

	@Override
	public void apply(final IdObjectVisitor visitor) {
		visitor.helper(this);
	}
}
