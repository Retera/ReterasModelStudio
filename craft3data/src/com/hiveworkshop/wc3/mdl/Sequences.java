package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.util.ArrayList;

import javax.swing.JOptionPane;

/**
 * The overarching sequence parser for Animations.
 *
 * @author Eric Theller
 * @version 11/10/2011
 */
public class Sequences {
	public static ArrayList<Animation> read(final BufferedReader mdl) {
		final ArrayList<Animation> seqs = new ArrayList<Animation>();
		String line = "";
		line = MDLReader.nextLine(mdl);
		if (!line.contains("Sequences") || line.contains("Global")) {
			// JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Unable to
			// parse sequences: Confused by opener line '"+line+"'.\nNo sequences?");
			MDLReader.reset(mdl);
		} else {
			MDLReader.mark(mdl);
			line = MDLReader.nextLine(mdl);
			while (!(line).startsWith("}")) {
				if (line.contains("Anim")) {
					MDLReader.reset(mdl);
					final Animation an = Animation.read(mdl);
					seqs.add(an);
				} else {
					JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
							"Unable to parse sequences: Confused by anim opener line '" + line + "'.");
				}
				MDLReader.mark(mdl);
				line = MDLReader.nextLine(mdl);
			}
		}
		return seqs;
	}
}