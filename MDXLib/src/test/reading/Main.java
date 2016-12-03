package test.reading;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import wc3Data.mdl.MDL;
import wc3Data.mdx.BlizzardDataInputStream;
import wc3Data.mdx.MdxModel;
import wc3Data.mdx.MdxUtils;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		System.out.println(Integer.toBinaryString(0x1));
//		System.out.println(Integer.toBinaryString(0x2));
//		System.out.println(Integer.toBinaryString(0x4));
//		System.out.println(Integer.toBinaryString(0x8));
//		System.out.println(Integer.toBinaryString(0x10));
//		System.out.println(Integer.toBinaryString(0x20));
//		System.out.println(Integer.toBinaryString(0x40));
//		System.out.println(Integer.toBinaryString(0x80));
		try (BlizzardDataInputStream bdis = new BlizzardDataInputStream(new FileInputStream("PackHorse.mdx"))) {
			MdxModel packHorse = MdxUtils.loadModel(bdis);
			MDL model = packHorse.toMDL();
			model.printTo(new File("PackHorse.mdl"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
