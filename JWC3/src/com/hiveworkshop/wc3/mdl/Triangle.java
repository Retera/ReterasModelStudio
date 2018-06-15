package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class Triangle {
	GeosetVertex[] verts = new GeosetVertex[3];
	int[] vertIds = new int[3];
	Geoset geoset;

	public Triangle(final GeosetVertex a, final GeosetVertex b, final GeosetVertex c, final Geoset geoRef) {
		verts[0] = a;
		verts[1] = b;
		verts[2] = c;
		geoset = geoRef;
	}

	public Triangle(final int a, final int b, final int c, final Geoset geoRef) {
		vertIds[0] = a;
		vertIds[1] = b;
		vertIds[2] = c;
		verts[0] = geoRef.getVertex(a);
		verts[1] = geoRef.getVertex(b);
		verts[2] = geoRef.getVertex(c);
		geoset = geoRef;
	}

	public Triangle(final GeosetVertex a, final GeosetVertex b, final GeosetVertex c) {
		verts[0] = a;
		verts[1] = b;
		verts[2] = c;
		geoset = null;
	}

	public Triangle(final int a, final int b, final int c) {
		vertIds[0] = a;
		vertIds[1] = b;
		vertIds[2] = c;
		// m_verts[0] = geoRef.getVertex(a);
		// m_verts[1] = geoRef.getVertex(b);
		// m_verts[2] = geoRef.getVertex(c);
		geoset = null;
	}

	public void setGeoRef(final Geoset geoRef) {
		geoset = geoRef;
	}

	public void updateVertexRefs() {
		verts[0] = geoset.getVertex(vertIds[0]);
		verts[1] = geoset.getVertex(vertIds[1]);
		verts[2] = geoset.getVertex(vertIds[2]);
	}

	public void updateVertexIds() {
		// Potentially this procedure could lag a bunch in the way I wrote it,
		// but it will
		// change vertex ids to match a changed geoset, assuming the geoset
		// still contains the
		// vertex
		vertIds[0] = geoset.getVertexId(verts[0]);
		vertIds[1] = geoset.getVertexId(verts[1]);
		vertIds[2] = geoset.getVertexId(verts[2]);
	}

	public void forceVertsUpdate() {
		if (!verts[0].triangles.contains(this)) {
			verts[0].triangles.add(this);
		}
		if (!verts[1].triangles.contains(this)) {
			verts[1].triangles.add(this);
		}
		if (!verts[2].triangles.contains(this)) {
			verts[2].triangles.add(this);
		}
	}

	public void updateVertexIds(final Geoset geoRef) {
		geoset = geoRef;
		updateVertexIds();
	}

	public void updateVertexRefs(final ArrayList<GeosetVertex> list) {
		verts[0] = list.get(vertIds[0]);
		verts[1] = list.get(vertIds[1]);
		verts[2] = list.get(vertIds[2]);
	}

	public boolean containsRef(final GeosetVertex v) {
		return verts[0] == v || verts[1] == v || verts[2] == v;
	}

	public boolean contains(final GeosetVertex v) {
		return verts[0].equalLocs(v) || verts[1].equalLocs(v) || verts[2].equalLocs(v);
	}

	public GeosetVertex get(final int index) {
		return verts[index];
	}

	public int getId(final int index) {
		return vertIds[index];
	}

	public void set(final int index, final GeosetVertex v) {
		verts[index] = v;
		vertIds[index] = geoset.getVertexId(v);
	}

	public int indexOf(final GeosetVertex v) {
		int out = -1;
		for (int i = 0; i < verts.length && out == -1; i++) {
			if (verts[i].equalLocs(v)) {
				out = i;
			}
		}
		return out;
	}

	public boolean equalLocs(final Triangle t) {
		boolean equal = true;
		for (int i = 0; i < 3 && equal; i++) {
			if (!t.verts[i].equalLocs(verts[i]) || t.vertIds[i] != vertIds[i]) {
				equal = false;
			}
		}
		return equal;
	}

	public boolean sameVerts(final Triangle t) {
		boolean equal = true;
		for (int i = 0; i < 3 && equal; i++) {
			if (!contains(t.verts[i])) {
				equal = false;
			}
		}
		return equal;
	}

	public int indexOfRef(final GeosetVertex v) {
		int out = -1;
		for (int i = 0; i < verts.length && out == -1; i++) {
			if (verts[i] == v) {
				out = i;
			}
		}
		return out;
	}

	public boolean equalRefsNoIds(final Triangle t) {
		boolean equal = true;
		for (int i = 0; i < 3 && equal; i++) {
			if (t.verts[i] != verts[i]) {
				equal = false;
			}
		}
		return equal;
	}

	public boolean equalRefs(final Triangle t) {
		boolean equal = true;
		for (int i = 0; i < 3 && equal; i++) {
			if (t.verts[i] != verts[i] || t.vertIds[i] != vertIds[i]) {
				equal = false;
			}
		}
		return equal;
	}

	public GeosetVertex[] getAll() {
		return verts;
	}

	public int[] getIntCoords(final byte dim) {
		final int[] output = new int[3];
		for (int i = 0; i < 3; i++) {
			output[i] = (int) (verts[i].getCoord(dim));
		}
		return output;
	}

	public double[] getCoords(final byte dim) {
		final double[] output = new double[3];
		for (int i = 0; i < 3; i++) {
			output[i] = (verts[i].getCoord(dim));
		}
		return output;
	}

	public double[] getTVertCoords(final byte dim, final int layerId) {
		final double[] output = new double[3];
		for (int i = 0; i < 3; i++) {
			output[i] = (verts[i].getTVertex(layerId).getCoord(dim));
		}
		return output;
	}

	public static ArrayList<Triangle> parseText(final String[] input) {
		// Usually triangles come in a single entry with all of them, so we
		// parse the input into an ArrayList
		final ArrayList<Triangle> output = new ArrayList<>();
		for (int l = 1; l < input.length; l++) {
			final String[] s = input[l].split(",");
			s[0] = s[0].substring(4, s[0].length());
			final int s_size = MDLReader.occurrencesIn(input[l], ",");
			s[s_size - 1] = s[s_size - 1].substring(0, s[s_size - 1].length() - 2);
			for (int t = 0; t < s_size - 1; t += 3)// s[t+3].equals("")||
			{
				for (int i = 0; i < 3; i++) {
					s[t + i] = s[t + i].substring(1);
				}
				try {
					output.add(new Triangle(Integer.parseInt(s[t]), Integer.parseInt(s[t + 1]),
							Integer.parseInt(s[t + 2])));
				} catch (final NumberFormatException e) {
					JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
							"Error: Unable to interpret information in Triangles: " + s[t] + ", " + s[t + 1] + ", or "
									+ s[t + 2]);
				}
			}
		}
		return output;
	}

	public static ArrayList<Triangle> read(final BufferedReader mdl) {
		// Usually triangles come in a single entry with all of them, so we
		// parse the input into an ArrayList
		final ArrayList<Triangle> output = new ArrayList<>();
		String line = "";
		while (!(line = MDLReader.nextLine(mdl)).contains("\t}")) {
			// System.out.println("Interpreting "+line+" for Triangles");
			final String[] s = line.split(",");
			s[0] = s[0].substring(4, s[0].length());
			final int s_size = MDLReader.occurrencesIn(",", line);
			// System.out.println("We broke it into "+s_size+" parts.");
			s[s_size - 1] = s[s_size - 1].substring(0, s[s_size - 1].length() - 2);
			for (int t = 0; t < s_size - 1; t += 3)// s[t+3].equals("")||
			{
				for (int i = 0; i < 3; i++) {
					s[t + i] = s[t + i].substring(1);
				}
				try {
					output.add(new Triangle(Integer.parseInt(s[t]), Integer.parseInt(s[t + 1]),
							Integer.parseInt(s[t + 2])));
				} catch (final NumberFormatException e) {
					JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
							"Error: Unable to interpret information in Triangles: " + s[t] + ", " + s[t + 1] + ", or "
									+ s[t + 2]);
				}
			}
		}
		return output;
	}

	public static ArrayList<Triangle> read(final BufferedReader mdl, final Geoset geoRef) {
		// Usually triangles come in a single entry with all of them, so we
		// parse the input into an ArrayList
		final ArrayList<Triangle> output = new ArrayList<>();
		String line = "";
		while (!(line = MDLReader.nextLine(mdl)).contains("\t}")) {
			// System.out.println("Interpreting "+line+" for Triangles");
			final String[] s = line.split(",");
			s[0] = s[0].substring(4, s[0].length());
			final int s_size = MDLReader.occurrencesIn(",", line);
			// System.out.println("We broke it into "+s_size+" parts.");
			s[s_size - 1] = s[s_size - 1].substring(0, s[s_size - 1].length() - 2);
			for (int t = 0; t < s_size - 1; t += 3)// s[t+3].equals("")||
			{
				for (int i = 0; i < 3; i++) {
					s[t + i] = s[t + i].substring(1);
				}
				try {
					output.add(new Triangle(Integer.parseInt(s[t]), Integer.parseInt(s[t + 1]),
							Integer.parseInt(s[t + 2]), geoRef));
				} catch (final NumberFormatException e) {
					JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
							"Error: Unable to interpret information in Triangles: " + s[t] + ", " + s[t + 1] + ", or "
									+ s[t + 2]);
				}
			}
		}
		return output;
	}

	@Override
	public String toString() {
		return vertIds[0] + ", " + vertIds[1] + ", " + vertIds[2];
	}

	/**
	 * Flips the triangle's orientation, and optionally the normal vectors for all the triangle's components.
	 */
	public void flip(final boolean flipNormals) {
		GeosetVertex tempVert;
		int tempVertId;
		tempVert = verts[2];
		tempVertId = vertIds[2];
		verts[2] = verts[1];
		vertIds[2] = vertIds[1];
		verts[1] = tempVert;
		vertIds[1] = tempVertId;
		if (flipNormals) {
			for (final GeosetVertex geosetVertex : verts) {
				final Normal normal = geosetVertex.getNormal();
				if (normal != null) {
					// Flip normals, preserve lighting!
					normal.inverse();
				}
			}
		}
	}

	public Geoset getGeoset() {
		return geoset;
	}

	public void setGeoset(final Geoset geoset) {
		this.geoset = geoset;
	}

	public GeosetVertex[] getVerts() {
		return verts;
	}

	public void setVerts(final GeosetVertex[] verts) {
		this.verts = verts;
	}

	public int[] getVertIds() {
		return vertIds;
	}

	public void setVertIds(final int[] vertIds) {
		this.vertIds = vertIds;
	}

	public Vertex getFacingVector() {
		// NOTE does allocation
		final Vertex firstEdge = verts[0].delta(verts[1]);
		final Vertex secondEdge = verts[1].delta(verts[2]);
		return firstEdge.crossProduct(secondEdge);
	}
}
