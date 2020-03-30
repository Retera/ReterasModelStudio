package com.matrixeater.hacks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.hiveworkshop.wc3.mdx.MdxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class GetMeDatas8 {

	public static void main(final String[] args) {
		try {
			final InputStream footman = new BlizzardDataInputStream(new FileInputStream(new File(
					"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III Beta\\Models\\Footman_SD_Reforged.mdx")));
			final MdxModel footmanMdx = MdxUtils.loadModel(new BlizzardDataInputStream(footman));
			try (BlizzardDataOutputStream out = new BlizzardDataOutputStream(new File(
					"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III Beta\\Models\\SoftwareStandardFootman.mdx"))) {
				footmanMdx.save(out);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
