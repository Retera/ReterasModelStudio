package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.mdx.GeosetChunk;

/**
 * MinimumExt,MaximumExt,BoundsRad
 *
 * Eric Theller 11/10/2011
 */
public class ExtLog {
	private Vertex minimumExtent;
	private Vertex maximumExtent;
	private double boundsRadius = -99;
	static double DEFAULT_BOUNDSRADIUS = 100.00;
	static Vertex DEFAULT_MINEXT = new Vertex(-100, -100, -100);
	static Vertex DEFAULT_MAXEXT = new Vertex(100, 100, 100);

	private ExtLog() {
	}

	public ExtLog(final double boundsRadius) {
		this.boundsRadius = boundsRadius;
	}

	public ExtLog(final GeosetChunk.Geoset.Extent source) {
		minimumExtent = new Vertex(source.minimumExtent);
		maximumExtent = new Vertex(source.maximumExtent);
		boundsRadius = source.bounds;
	}

	public ExtLog(final Vertex minE, final Vertex maxE) {
		minimumExtent = minE;
		maximumExtent = maxE;
	}

	public ExtLog(final Vertex minE, final Vertex maxE, final double boundsRad) {
		minimumExtent = minE;
		maximumExtent = maxE;
		boundsRadius = boundsRad;
	}

	public ExtLog(final float[] minE, final float[] maxE, final double boundsRad) {
		minimumExtent = new Vertex(minE);
		maximumExtent = new Vertex(maxE);
		boundsRadius = boundsRad;
	}

	public ExtLog(final ExtLog other) {
		minimumExtent = other.minimumExtent;
		maximumExtent = other.maximumExtent;
		boundsRadius = other.boundsRadius;
	}

	public void setMinExt(final Vertex v) {
		minimumExtent = v;
	}

	public void setMaxExt(final Vertex v) {
		maximumExtent = v;
	}

	public void setBounds(final double b) {
		boundsRadius = b;
	}

	public static ExtLog parseText(final String[] line) {
		if (line[0].contains("Extent") || line[0].contains("BoundsRadius")) {
			final ExtLog extLog = new ExtLog();
			for (int i = 0; i < line.length; i++) {
				if (line[i].contains("MinimumExtent")) {
					extLog.setMinExt(Vertex.parseText(line[i].split("MinimumExtent ")[1]));
				} else if (line[i].contains("MaximumExtent")) {
					extLog.setMinExt(Vertex.parseText(line[i].split("MaximumExtent ")[1]));
				} else if (line[i].contains("BoundsRadius")) {
					String s = line[i].split("BoundsRadius ")[1];
					s = s.substring(0, s.length() - 1);
					try {
						extLog.setBounds(Double.parseDouble(s));
					} catch (final NumberFormatException e) {
						JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
								"Error {" + s + "}: BoundsRadius could not be interpreted.");
					}
				} else {
					JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
							"Unable to parse ExtLog; unrecognized input: " + line[i]);
				}
			}
			return extLog;
		} else {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Unable to parse ExtLog: Missing or unrecognized open statement.");
		}
		return null;
	}

	public static ExtLog read(final BufferedReader mdl) {
		String line = MDLReader.nextLine(mdl);
		// System.out.println("Starting ExtLog with :"+line);
		if (line.contains("Extent") || line.contains("BoundsRadius")) {
			final ExtLog extLog = new ExtLog();
			MDLReader.mark(mdl);
			while (!((line).contains("\t}")) && (line.contains("Extent") || line.contains("BoundsRadius"))) {
				if (line.contains("MinimumExtent")) {
					extLog.setMinExt(Vertex.parseText(line.split("MinimumExtent ")[1]));
				} else if (line.contains("MaximumExtent")) {
					extLog.setMaxExt(Vertex.parseText(line.split("MaximumExtent ")[1]));
				} else if (line.contains("BoundsRadius")) {
					String s = line.split("BoundsRadius ")[1];
					s = s.substring(0, s.length() - 1);
					try {
						extLog.setBounds(Double.parseDouble(s));
					} catch (final NumberFormatException e) {
						JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
								"Error {" + s + "}: BoundsRadius could not be interpreted.");
					}
				} else {
					JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
							"Unable to parse ExtLog; unrecognized input: " + line);
				}
				MDLReader.mark(mdl);
				line = MDLReader.nextLine(mdl);
			}
			MDLReader.reset(mdl);
			return extLog;
		} else {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Unable to parse ExtLog: Missing or unrecognized open statement: " + line);
		}
		return null;
	}

	public void printTo(final PrintWriter writer, final int tabHeight) {
		String tabs = "";
		for (int i = 0; i < tabHeight; i++) {
			tabs = tabs + "\t";
		}
		if (minimumExtent != null) {
			writer.println(tabs + "MinimumExtent " + minimumExtent.toString() + ",");
		}
		if (maximumExtent != null) {
			writer.println(tabs + "MaximumExtent " + maximumExtent.toString() + ",");
		}
		if (boundsRadius != -99) {
			writer.println(tabs + "BoundsRadius " + MDLReader.doubleToString(boundsRadius) + ",");
		}
	}

	public Vertex getMinimumExtent() {
		return minimumExtent;
	}

	public void setMinimumExtent(final Vertex minimumExtent) {
		this.minimumExtent = minimumExtent;
	}

	public Vertex getMaximumExtent() {
		return maximumExtent;
	}

	public void setMaximumExtent(final Vertex maximumExtent) {
		this.maximumExtent = maximumExtent;
	}

	public double getBoundsRadius() {
		return boundsRadius;
	}

	public void setBoundsRadius(final double boundsRadius) {
		this.boundsRadius = boundsRadius;
	}
}
