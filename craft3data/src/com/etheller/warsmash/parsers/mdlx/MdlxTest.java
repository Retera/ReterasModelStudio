package com.etheller.warsmash.parsers.mdlx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.hiveworkshop.wc3.mdx.MdxUtils;

public class MdlxTest {

	public static void main(final String[] args) {
		try (FileInputStream stream = new FileInputStream(
				new File("C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\ArcaneEpic13.mdx"))) {
			final MdlxModel model = MdxUtils.loadMdlx(stream);
			try (FileOutputStream mdlStream = new FileOutputStream(new File(
					"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\Test\\MyOutAutomated.mdl"))) {
				MdxUtils.saveMdx(model, mdlStream);
			}
		}
		catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (final IOException e) {
			e.printStackTrace();
		}

		System.out.println("Created MDL, now reparsing to MDX");

		try (FileInputStream stream = new FileInputStream(
				new File("C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\Test\\MyOutAutomated.mdl"))) {
			final MdlxModel model = MdxUtils.loadMdlx(stream);
			try (FileOutputStream mdlStream = new FileOutputStream(new File(
					"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\Test\\MyOutAutomatedMDX.mdx"))) {

				MdxUtils.saveMdx(model, mdlStream);
			}
		}
		catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (final IOException e) {
			e.printStackTrace();
		}

		try (FileInputStream stream = new FileInputStream(
				new File("C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\Test\\MyOutAutomatedMDX.mdx"))) {
			final MdlxModel model = MdxUtils.loadMdlx(stream);
			try (FileOutputStream mdlStream = new FileOutputStream(new File(
					"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\Test\\MyOutAutomatedMDXBack2MDL.mdl"))) {
				MdxUtils.saveMdx(model, mdlStream);
			}
		}
		catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
