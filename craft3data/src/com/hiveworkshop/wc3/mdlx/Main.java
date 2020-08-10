package com.hiveworkshop.wc3.mdlx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdx.MdxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;


public class Main {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
//		System.out.println(Integer.toBinaryString(0x1));
//		System.out.println(Integer.toBinaryString(0x2));
//		System.out.println(Integer.toBinaryString(0x4));
//		System.out.println(Integer.toBinaryString(0x8));
//		System.out.println(Integer.toBinaryString(0x10));
//		System.out.println(Integer.toBinaryString(0x20));
//		System.out.println(Integer.toBinaryString(0x40));
//		System.out.println(Integer.toBinaryString(0x80));
		try (BlizzardDataInputStream bdis = new BlizzardDataInputStream(new FileInputStream("PackHorse.mdx"))) {
			final MdxModel packHorse = MdxUtils.loadModel(bdis);
//			for( MaterialChunk.Material mat: packHorse.materialChunk.material ) {
//				mat.layerChunk.layer[0].shadingFlags |= 0x4;
//				mat.layerChunk.layer[0].shadingFlags |= 0x8;
//				mat.layerChunk.layer[0].shadingFlags |= 0x10;
//			}
			final BlizzardDataOutputStream out = new BlizzardDataOutputStream(new File("PackHorse_modified.mdx"));
			packHorse.save(out);
			out.close();
			final MdxModel packHorse2 = MdxUtils.loadModel(new BlizzardDataInputStream(new FileInputStream("PackHorse_modified.mdx")));
			final EditableModel model = packHorse.toMDL();
			model.setName("PackHorse_MDLified");
			final MdxModel packHorseMdx = new MdxModel(model);
			final BlizzardDataOutputStream out2 = new BlizzardDataOutputStream(new File("PackHorse_MXfied.mdx"));
			packHorseMdx.save(out2);
			out2.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
