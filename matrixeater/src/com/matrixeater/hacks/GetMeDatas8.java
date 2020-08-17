package com.matrixeater.hacks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.etheller.warsmash.parsers.mdlx.MdlxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;

public class GetMeDatas8 {

	public static void main(final String[] args) {
		try {
			final InputStream footman = new FileInputStream(new File(
					"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III Beta\\Models\\Footman_SD_Reforged.mdx"));
			final MdlxModel footmanMdx = MdxUtils.loadModel(footman);
			try (OutputStream out = new FileOutputStream(new File(
					"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III Beta\\Models\\SoftwareStandardFootman.mdx"))) {
				footmanMdx.saveMdx(out);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
