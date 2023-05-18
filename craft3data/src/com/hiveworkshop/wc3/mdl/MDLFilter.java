package com.hiveworkshop.wc3.mdl;

import java.io.File;
import java.util.Locale;

import javax.swing.filechooser.FileFilter;

/**
 * Write a description of class MDLFilter here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class MDLFilter extends FileFilter {
	@Override
	public boolean accept(final File f) {
		// Special thanks to the Oracle Java tutorials for a lot of the major code
		// concepts here
		if (f.isDirectory()) {
			return true;
		}
		final String name = f.getName();
		final int perIndex = name.lastIndexOf('.');
		if (name.substring(perIndex + 1).toLowerCase(Locale.US).equals("mdl") && (perIndex > 0)
				&& (perIndex < (name.length() - 1))) {
			return true;
		}
		return false;
	}

	@Override
	public String getDescription() {
		return "Warcraft III Model Files \"-.mdl\"";
	}
}
