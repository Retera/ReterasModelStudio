package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.model.util.Mesh;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Vec3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ScriptActions {


	public static String incName(String name) {
		String output = name;

//        int depth = 1;
//        boolean continueLoop = name != null && output.length()>0;
//        while (continueLoop) {
//            char c = '0';
//            try {
//                c = output.charAt(output.length() - depth);
//            } catch (final IndexOutOfBoundsException e) {
//                // c remains '0', name is not changed (this should only happen if name is "9" or "99...9")
//                continueLoop = false;
//            }
//            for (char n = '0'; (n < '9') && continueLoop; n++) {
//                if (c == n) {
//                    char x = c;
//                    x++;
//                    output = output.substring(0, output.length() - depth) + x
//                            + output.substring((output.length() - depth) + 1);
//                    continueLoop = false;
//                }
//            }
//            if (c == '9') {
//                output = output.substring(0, output.length() - depth) + 0
//                        + output.substring((output.length() - depth) + 1);
//            } else if (continueLoop) {
//                output = output.substring(0, (output.length() - depth) + 1) + 1
//                        + output.substring((output.length() - depth) + 1);
//                continueLoop = false;
//            }
//            depth++;
//        }
//        if (output == null) {
//            output = "name error";
//        } else if (output.equals(name)) {
//            output = output + "_edit";
//        }

		if (output != null) {
			for (int offsetFromEnd = 1; offsetFromEnd <= output.length(); offsetFromEnd++) {
				char charAt = output.charAt(output.length() - offsetFromEnd);
				if ('0' <= charAt && charAt <= '8') {
					int numberLocation = output.length() - offsetFromEnd;
					output = output.substring(0, numberLocation) + (charAt + 1) + output.substring((numberLocation) + 1);
					break;
				} else if (charAt == '9') {
					int numberLocation = output.length() - offsetFromEnd;
					output = output.substring(0, numberLocation) + "0" + output.substring(numberLocation + 1);
					if (numberLocation == 0) {
						output = 1 + output; // if name == "999...9" -> output = "1000...0" instead of "000...0"
					}
				} else { // charAt is not a digit
					int numberLocation = output.length() - offsetFromEnd;
					output = output.substring(0, numberLocation + 1) + "1" + output.substring(numberLocation + 1);
					break;
				}
			}
		}

		if (output == null) {
			output = "name error";
		}
		if (output.equals(name)) {
			output = output + "_edit";
		}

		return output;
	}

	public static void jokeButtonClickResponse() {
		StringBuilder sb = new StringBuilder();
		for (File file : new File(
				"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\CustomMapData\\LuaFpsMap\\Maps\\MultiplayerFun004")
				.listFiles()) {
			if (!file.getName().toLowerCase().endsWith("_init.txt")) {
				sb.setLength(0);
				try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
					String line;
					while ((line = reader.readLine()) != null) {
						if (line.contains("BlzSetAbilityActivatedIcon")) {
							int startIndex = line.indexOf('"') + 1;
							int endIndex = line.lastIndexOf('"');
							String dataString = line.substring(startIndex, endIndex);
							sb.append(dataString);
						}
					}
				} catch (final IOException e1) {
					e1.printStackTrace();
				}
				String dataString = sb.toString();
				EditableModel model = ProgramGlobals.getCurrentModelPanel().getModel();
				for (int i = 0; (i + 23) < dataString.length(); i += 24) {
					Geoset geo = new Geoset();
					model.add(geo);
					geo.setMaterial(new Material(new Layer(FilterMode.BLEND, new Bitmap("textures\\white.blp"))));
					String data = dataString.substring(i, i + 24);
					int x = Integer.parseInt(data.substring(0, 3));
					int y = Integer.parseInt(data.substring(3, 6));
					int z = Integer.parseInt(data.substring(6, 9));
					int sX = Integer.parseInt(data.substring(9, 10));
					int sY = Integer.parseInt(data.substring(10, 11));
					int sZ = Integer.parseInt(data.substring(11, 12));
					int red = Integer.parseInt(data.substring(12, 15));
					int green = Integer.parseInt(data.substring(15, 18));
					int blue = Integer.parseInt(data.substring(18, 21));
					int alpha = Integer.parseInt(data.substring(21, 24));
					GeosetAnim forceGetGeosetAnim = geo.forceGetGeosetAnim();
					forceGetGeosetAnim.setStaticColor(new Vec3(blue / 255.0, green / 255.0, red / 255.0));
					forceGetGeosetAnim.setStaticAlpha(alpha / 255.0);
					System.out.println(x + "," + y + "," + z);

					Mesh mesh = ModelUtils.createBox(new Vec3(x, y, z).scale(10),
							new Vec3(x + sX, y + sY, z + sZ).scale(10), 1, 1, 1, geo);
					geo.addVerticies(mesh.getVertices());
					geo.addTriangles(mesh.getTriangles());
				}
			}

		}
		ModelStructureChangeListener.changeListener.geosetsUpdated();
	}
}
