package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.ExportTexture;
import com.hiveworkshop.rms.util.Vec2;

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
			drawTriangleUV(graphics, Vec2.X_AXIS, Vec2.Y_AXIS, width, height, 0, triangle, true);

		}
		if(lineColor != null){
			graphics.setColor(lineColor);
			for (Triangle triangle : modelView.getSelectedTriangles()) {
				drawTriangleUV(graphics, Vec2.X_AXIS, Vec2.Y_AXIS, width, height, 0, triangle, false);
			}
		}

		graphics.dispose();
		return image;
	}


	private static void drawTriangleUV(Graphics g, Vec2 right, Vec2 up, int width, int height, int uvLayer, Triangle t, boolean fill) {
		int[] xInt = getTriUVPoints(t, uvLayer, right, width);
		int[] yInt = getTriUVPoints(t, uvLayer, up, -height);
		if(fill){
			g.fillPolygon(xInt, yInt, 4);
		} else {
			g.drawPolyline(xInt, yInt, 4);
		}
	}

	private static int[] getTriUVPoints(Triangle t, int uvLayer, Vec2 dim, int scale){
		int[] output = new int[4];
		for (int i = 0; i < 3; i++) {
			output[i] = Math.round(t.getTVert(i, uvLayer).dot(dim)) * scale;
		}
		output[3] = output[0];
		return output;
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
