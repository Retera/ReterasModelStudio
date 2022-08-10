package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.ExportTexture;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CrudeSelectionUVMask {

	public static BufferedImage getBufferedImage(ModelView modelView, int width, int height, Color lineColor) {

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = image.getGraphics();
		graphics.setColor(Color.BLACK);
		graphics.fill3DRect(0, 0, width, height, false);

		graphics.setColor(Color.WHITE);
		for (Triangle triangle : modelView.getSelectedTriangles()) {
			drawTriangle(graphics, (byte) 0, (byte) 1, width, height, triangle, true);
		}
		if(lineColor != null){
			graphics.setColor(lineColor);
			for (Triangle triangle : modelView.getSelectedTriangles()) {
				drawTriangle(graphics, (byte) 0, (byte) 1, width, height, triangle, false);
			}
		}

		graphics.dispose();
		return image;
	}


	private static void drawTriangle(Graphics g, byte a, byte b, int width, int height, Triangle t, boolean fill) {
		double[] x = t.getTVertCoords(a, 0);
		double[] y = t.getTVertCoords(b, 0);
		int[] xInt = new int[4];
		int[] yInt = new int[4];
		for (int ix = 0; ix < 3; ix++) {
			xInt[ix] = (int) Math.round(x[ix]*width);
			yInt[ix] = (int) (Math.round(y[ix]*height));
		}
//		System.out.println("x: " + xInt[0] + ", y: " + yInt[0]);
		xInt[3] = xInt[0];
		yInt[3] = yInt[0];
		if(fill){
			g.fillPolygon(xInt, yInt, 4);
		} else {
			g.drawPolyline(xInt, yInt, 4);
		}
	}

	public static void saveImage(ModelView modelView, int width, int height){
		System.out.println("save Image?");
		if(!modelView.getSelectedVertices().isEmpty()){
			System.out.println("should show dialog!");
			BufferedImage maskImage = getBufferedImage(modelView, width, height, null);
			String fileName = modelView.getModel().getName() + "_uvMask";
			ExportTexture.onClickSaveAs(maskImage, fileName, FileDialog.SAVE_TEXTURE, new FileDialog(), ProgramGlobals.getMainPanel());
		}
	}
}
