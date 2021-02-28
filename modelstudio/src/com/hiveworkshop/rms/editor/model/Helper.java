package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.parsers.mdlx.MdlxHelper;

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

	public MdlxHelper toMdlxHelper(EditableModel model) {
		final MdlxHelper helper = new MdlxHelper();

		objectToMdlx(helper, model);

		return helper;
	}

	// printTo is already written as a part of bone; these two things are stupidly the same
	@Override
	public Helper copy() {
		return new Helper(this);
	}

	@Override
	public void apply(final IdObjectVisitor visitor) {
		visitor.helper(this);
	}
}
