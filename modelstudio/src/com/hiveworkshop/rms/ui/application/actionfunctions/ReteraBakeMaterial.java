package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.model.util.HD_Material_Layer;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.ImageUtils.ImageCreator;
import com.hiveworkshop.rms.util.*;
import de.wc3data.image.TgaFile;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class ReteraBakeMaterial {

	public static void drawStuff(ModelHandler modelHandler){
		ReteraBakeMaterial bakeMaterial = new ReteraBakeMaterial();
		JPanel panel = new JPanel(new MigLayout("fill, debug"));
		ExtLog extents = modelHandler.getModel().getExtents();
		Vec3 minExtent = extents.getMinimumExtent();
		Vec3 maxExtent = extents.getMaximumExtent();
		Mat4 modelSpaceProjection = new Mat4().setOrtho(minExtent.y, maxExtent.y, minExtent.z, maxExtent.z, minExtent.x, maxExtent.x);


		Vec3 lightDirection = new Vec3(-24.1937f, 30.4879f, 444.411f);

		Vec3 viewDirection = new Vec3(32f, 0, 128f);


		Vec3 camBackward = new Vec3().sub(viewDirection).normalize();
		Vec3 camRight = new Vec3(Vec3.Z_AXIS).cross(camBackward).normalize();
		Vec3 camUp = new Vec3(camBackward).cross(camRight).normalize();

		Mat4 cameraSpaceMatrix = new Mat4().set(camRight, camUp, camBackward);

//		Vec3 camPosition = new Vec3(viewDirection);
		Vec3 camPosition = new Vec3();
		Vec3 vecHeap = new Vec3(camPosition).negate();
		Mat4 tempMat4 = new Mat4().translate(vecHeap);

		Mat4 viewMatrix = new Mat4().set(cameraSpaceMatrix).mul(tempMat4);
		Mat4 viewProjectionMatrix = new Mat4().set(modelSpaceProjection).mul(viewMatrix);
		Collection<Triangle> triangles;
		ModelView modelView = modelHandler.getModelView();
		if(modelView.getSelectedTriangles().isEmpty()){
			Geoset geoset = modelView.getEditableGeosets().stream().findFirst().orElse(modelHandler.getModel().getGeoset(0));
			triangles = geoset.getTriangles();
		} else {
			Set<GeosetVertex> verts = new LinkedHashSet<>();
			for (GeosetVertex gv : modelView.getSelectedVertices()){
				selectLinked(gv, verts);
			}
			Set<Triangle> triangles1 = new LinkedHashSet<>();
			Set<Triangle> selectedTriangles = modelView.getSelectedTriangles();
			for (GeosetVertex gv : verts){
				if(modelView.isSelected(gv)){
					for (Triangle t : gv.getTriangles()){
						if(selectedTriangles.contains(t)) {
							triangles1.add(t);
						}
					}
				}
			}
//			for (Triangle t : selectedTriangles){
//				selectLinked(t, triangles1);
//			}
			triangles = triangles1;
		}
		BufferedImage[] bufferedImages;
		bufferedImages = bakeMaterial.getBufferedImage3(triangles, 512, 512, modelSpaceProjection);
		for(BufferedImage texture : bufferedImages) {
			panel.add(bakeMaterial.getTexturePanel(texture), "growx, wrap");
		}


		JScrollPane scrollPane = new JScrollPane(panel);
		JPanel outerPanel = new JPanel(new MigLayout("fill, ins 0"));
		outerPanel.setMinimumSize(new Dimension(100, 100));
		outerPanel.setPreferredSize(ScreenInfo.getSmallWindow());
		outerPanel.add(scrollPane, "growx, growy");
		JFrame frame = new JFrame("Woop");
		frame.setContentPane(outerPanel);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	private static void selectLinked(Triangle currentTri, Set<Triangle> selection) {
		selection.add(currentTri);
		for (GeosetVertex other : currentTri.getVerts()) {
			for (Triangle tri : other.getTriangles()) {
				if (!selection.contains(tri)) {
					selectLinked(tri, selection);
				}
			}
		}
	}
	private static void selectLinked(GeosetVertex currentVertex, Set<GeosetVertex> selection) {
		selection.add(currentVertex);
		for (Triangle tri : currentVertex.getTriangles()) {
			for (GeosetVertex other : tri.getVerts()) {
				if (!selection.contains(other)) {
					selectLinked(other, selection);
				}
			}
		}
	}

	private JPanel getTexturePanel(BufferedImage texture) {
		ZoomableImagePreviewPanel imagePreviewPanel = new ZoomableImagePreviewPanel(texture, true);
		JPanel texturePreviewPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[grow]", "[grow]"));
		texturePreviewPanel.setPreferredSize(new Dimension(texture.getWidth()+5, texture.getHeight()+5));
		texturePreviewPanel.add(imagePreviewPanel, "growx, growy");
		return texturePreviewPanel;
	}

	public BufferedImage[] getBufferedImage3(Collection<Triangle> triangles, int width, int height) {
//		Vec3 tangentLightPos;
//		Vec3 tangentViewPos;
//		Vec3 tangentFragPos;

		BufferedImage lightImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics lightGraphics = lightImage.getGraphics();
		lightGraphics.setColor(Color.BLACK);
		lightGraphics.fill3DRect(0, 0, width, height, false);
		lightGraphics.setColor(Color.WHITE);

		BufferedImage viewImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics viewGraphics = viewImage.getGraphics();
		viewGraphics.setColor(Color.BLACK);
		viewGraphics.fill3DRect(0, 0, width, height, false);
		viewGraphics.setColor(Color.WHITE);

		BufferedImage fragImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics fragGraphics = fragImage.getGraphics();
		fragGraphics.setColor(Color.BLACK);
		fragGraphics.fill3DRect(0, 0, width, height, false);
		fragGraphics.setColor(Color.WHITE);

//		BufferedImage tempImageLight = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		WritableRaster tempRasterLight = tempImageLight.getRaster();
//		BufferedImage tempImageView = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		WritableRaster tempRasterView = tempImageView.getRaster();
//		BufferedImage tempImageFrag = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		WritableRaster tempRasterFrag = tempImageLight.getRaster();

		BufferedImage tempImageLight = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster tempRasterLight = tempImageLight.getRaster();
		BufferedImage tempImageView = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster tempRasterView = tempImageView.getRaster();
		BufferedImage tempImageFrag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster tempRasterFrag = tempImageFrag.getRaster();


//		Graphics tempGraphics = tempImage.getGraphics();
//		tempGraphics.setColor(Color.BLACK);
//		tempGraphics.fill3DRect(0, 0, width, height, false);
//		tempGraphics.setColor(Color.WHITE);



		Vec3 lightDirection = new Vec3(-24.1937f, 30.4879f, 444.411f);

		Vec3 viewDirection = new Vec3(32f, 0, 128f);


		Mat4 tempMat = new Mat4();

		Vec3 tempLight = new Vec3();
		Vec3 tempView = new Vec3();
		Vec3 tempFrag = new Vec3();
		float[] tempLightA = tempLight.toArray();
		float[] tempViewA = tempView.toArray();
		float[] tempFragA = tempFrag.toArray();


		Vec2 min = new Vec2();
		Vec2 max = new Vec2();
		Vec2 temp = new Vec2();
		for (Triangle triangle : triangles) {
//			List<VertexData> vertexData = getVec3VertexDataMap2(triangle, viewDirection, lightDirection);
			VertexData[] vertexData = getVertexData(triangle, viewDirection, lightDirection);

			Vec2[] tVerts = triangle.getTVerts(0);
			temp.set(width, height);
			min.set(tVerts[0]).minimize(tVerts[1]).minimize(tVerts[2]).mul(temp);
			max.set(tVerts[0]).maximize(tVerts[1]).maximize(tVerts[2]).mul(temp);
			for(int h = (int) Math.floor(min.y); h < (int) Math.ceil(max.y); h++){
				if(0<=h && h<height){
					for(int w = (int) Math.floor(min.x); w < (int)Math.ceil(max.x); w++){
						if(0<=w && w<width){
							temp.set(w/(float)width, h/(float)height);
							Vec3 bV = getBarycentric(tVerts, temp);
							tempMat.set(vertexData[0].tangentLightPos, vertexData[1].tangentLightPos, vertexData[2].tangentLightPos).transpose();
							tempLight.set(bV).transform(tempMat).toArray(tempLightA);
//							tempLight.set(bV).toArray(tempLightA);
//					tempRasterLight.setPixel(((w%width) + width)%width, ((h%height) + height)%height, tempLightA);
//					tempRasterLight.setPixel(Math.min(Math.max(0, w), width), Math.min(Math.max(0, h), height), tempLightA);
//							tempRasterLight.setPixel(w, h, tempLightA);
							tempRasterLight.setPixel(w, h, tempLightA);


							tempMat.set(vertexData[0].tangentViewPos, vertexData[1].tangentViewPos, vertexData[2].tangentViewPos).transpose();
							tempView.set(bV).transform(tempMat).toArray(tempViewA);
//					tempRasterView.setPixel(((w%width) + width)%width, ((h%height) + height)%height, tempViewA);
//					tempRasterView.setPixel(Math.min(Math.max(0, w), width), Math.min(Math.max(0, h), height), tempViewA);
//							tempRasterView.setPixel(w, h, tempViewA);
							tempRasterView.setPixel(w, h, tempViewA);

//							Vec3 viewDir = new Vec3(geoBakingCell.tangentViewPos).sub(geoBakingCell.tangentFragPos).normalize();
							tempMat.set(vertexData[0].tangentFragPos, vertexData[1].tangentFragPos, vertexData[2].tangentFragPos).transpose();
							tempFrag.set(bV).transform(tempMat).toArray(tempFragA);
//					tempRasterFrag.setPixel(((w%width) + width)%width, ((h%height) + height)%height, tempFragA);
//					tempRasterFrag.setPixel(Math.min(Math.max(0, w), width), Math.min(Math.max(0, h), height), tempFragA);
//							tempRasterFrag.setPixel(w, h, tempFragA);
							tempRasterFrag.setPixel(w, h, tempFragA);
							if(h==40 && w == 150){
								System.out.println("bV: " + bV + ", tempLight: " + tempLight + ", tempView: " + tempView + ", tempFrag: " + tempFrag);
								System.out.println("light pixel: " + Arrays.toString(tempRasterLight.getPixel(w, h, tempLightA)));
								System.out.println("view pixel:  " + Arrays.toString(tempRasterView.getPixel(w, h, tempViewA)));
								System.out.println("frag pixel:  " + Arrays.toString(tempRasterFrag.getPixel(w, h, tempFragA)));
							}
						}

					}
				}
			}

			int[][] uvPoints = getTriUVPoints2(triangle, 0, new float[] {width, height});
			Polygon triClip = new Polygon(uvPoints[0], uvPoints[1], 4);
			lightGraphics.setClip(triClip);
			lightGraphics.drawImage(tempImageLight, 0, 0, null);

			viewGraphics.setClip(triClip);
			viewGraphics.drawImage(tempImageView, 0, 0, null);

			fragGraphics.setClip(triClip);
			fragGraphics.drawImage(tempImageFrag, 0, 0, null);

//			lightGraphics.fillPolygon(uvPoints[0], uvPoints[1], 4);
//			viewGraphics.fillPolygon(uvPoints[0], uvPoints[1], 4);
//			fragGraphics.fillPolygon(uvPoints[0], uvPoints[1], 4);
		}

		lightGraphics.dispose();
		viewGraphics.dispose();
		fragGraphics.dispose();
		return new BufferedImage[] {lightImage, viewImage, fragImage};
	}
	public BufferedImage[] getBufferedImage3(Collection<Triangle> triangles, int width, int height, Mat4 modelSpaceMat) {
//		Vec3 tangentLightPos;
//		Vec3 tangentViewPos;
//		Vec3 tangentFragPos;

		BufferedImage normImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D normGraphics = normImage.createGraphics();
		normGraphics.setColor(Color.BLACK);
		normGraphics.fill3DRect(0, 0, width, height, false);
		normGraphics.setColor(Color.WHITE);

		BufferedImage lightImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D lightGraphics = lightImage.createGraphics();
		lightGraphics.setColor(Color.BLACK);
		lightGraphics.fill3DRect(0, 0, width, height, false);
		lightGraphics.setColor(Color.WHITE);

		BufferedImage viewImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D viewGraphics = viewImage.createGraphics();
//		viewGraphics.setColor(Color.BLUE);
		viewGraphics.setColor(Color.BLACK);
		viewGraphics.fill3DRect(0, 0, width, height, false);
		viewGraphics.setColor(Color.WHITE);

		BufferedImage viewDirImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D viewDirGraphics = viewDirImage.createGraphics();
		viewDirGraphics.setColor(Color.BLACK);
		viewDirGraphics.fill3DRect(0, 0, width, height, false);
		viewDirGraphics.setColor(Color.WHITE);

		BufferedImage fragImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D fragGraphics = fragImage.createGraphics();
//		fragGraphics.setColor(Color.RED);
		fragGraphics.setColor(Color.BLACK);
		fragGraphics.fill3DRect(0, 0, width, height, false);
		fragGraphics.setColor(Color.WHITE);

//		BufferedImage tempImageLight = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		WritableRaster tempRasterLight = tempImageLight.getRaster();
//		BufferedImage tempImageView = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		WritableRaster tempRasterView = tempImageView.getRaster();
//		BufferedImage tempImageFrag = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		WritableRaster tempRasterFrag = tempImageLight.getRaster();

		BufferedImage tempImageNorm = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster tempRasterNorm = tempImageNorm.getRaster();

		BufferedImage tempImageLight = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster tempRasterLight = tempImageLight.getRaster();
		BufferedImage tempImageView = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster tempRasterView = tempImageView.getRaster();
		BufferedImage tempImageViewDir = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster tempRasterViewDir = tempImageViewDir.getRaster();
		BufferedImage tempImageFrag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster tempRasterFrag = tempImageFrag.getRaster();


//		Graphics tempGraphics = tempImage.getGraphics();
//		tempGraphics.setColor(Color.BLACK);
//		tempGraphics.fill3DRect(0, 0, width, height, false);
//		tempGraphics.setColor(Color.WHITE);



//		Vec3 lightDirection = new Vec3(-24.1937f, 30.4879f, 444.411f);
		Vec3 lightDirection = new Vec3(0,0, 444.411f);

//		Vec3 viewDirection = new Vec3(32f, 0, 128f);
		Vec3 viewDirection = new Vec3(128, 0, 0);


		Mat4 tempMat = new Mat4();

		Vec3 t1 = new Vec3();
		Vec3 t2 = new Vec3();
		Vec3 t3 = new Vec3();
		Vec3 tempTemp = new Vec3();
		Vec3 tempNorm = new Vec3();
		Vec3 tempLight = new Vec3();
		Vec3 tempView = new Vec3();
		Vec3 tempViewDir = new Vec3();
		Vec3 tempFrag = new Vec3();
		float[] tempNormA = tempNorm.toArray();
		float[] tempLightA = tempLight.toArray();
		float[] tempViewA = tempView.toArray();
		float[] tempViewDirA = tempViewDir.toArray();
		float[] tempFragA = tempFrag.toArray();
		Vec3 maxColor = new Vec3(255, 255, 255);

		Vec2 min = new Vec2();
		Vec2 max = new Vec2();
		Vec2 temp = new Vec2();
		for (Triangle triangle : triangles) {
//			List<VertexData> vertexData = getVec3VertexDataMap2(triangle, viewDirection, lightDirection);
			VertexData[] vertexData = getVertexData(triangle, viewDirection, lightDirection, modelSpaceMat);

			Vec2[] tVerts = triangle.getTVerts(0);
			temp.set(width, height);
			min.set(tVerts[0]).minimize(tVerts[1]).minimize(tVerts[2]).mul(temp);
			max.set(tVerts[0]).maximize(tVerts[1]).maximize(tVerts[2]).mul(temp);

//			for(int h = (int) Math.floor(min.y); h < (int) Math.ceil(max.y); h++){
//				if(0<=h && h<height){
//					for(int w = (int) Math.floor(min.x); w < (int)Math.ceil(max.x); w++){
//						if(0<=w && w<width){
//							temp.set(w/(float)width, h/(float)height);
//							Vec3 bV = getBarycentric(tVerts, temp);
//							tempMat.set(vertexData[0].tangentLightPos, vertexData[1].tangentLightPos, vertexData[2].tangentLightPos).transpose();
//							tempLight.set(bV).transform(tempMat).normalize().add(Vec3.ONE).scale(127.5f).toArray(tempLightA);
////							tempLight.set(bV).toArray(tempLightA);
////					tempRasterLight.setPixel(((w%width) + width)%width, ((h%height) + height)%height, tempLightA);
////					tempRasterLight.setPixel(Math.min(Math.max(0, w), width), Math.min(Math.max(0, h), height), tempLightA);
////							tempRasterLight.setPixel(w, h, tempLightA);
//							tempRasterLight.setPixel(w, h, tempLightA);
//
//
//							tempMat.set(vertexData[0].tangentViewPos, vertexData[1].tangentViewPos, vertexData[2].tangentViewPos).transpose();
//							tempView.set(bV).transform(tempMat).toArray(tempViewA);
////					tempRasterView.setPixel(((w%width) + width)%width, ((h%height) + height)%height, tempViewA);
////					tempRasterView.setPixel(Math.min(Math.max(0, w), width), Math.min(Math.max(0, h), height), tempViewA);
////							tempRasterView.setPixel(w, h, tempViewA);
//							tempRasterView.setPixel(w, h, tempViewA);
//
////							Vec3 viewDir = new Vec3(geoBakingCell.tangentViewPos).sub(geoBakingCell.tangentFragPos).normalize();
//							tempMat.set(vertexData[0].tangentFragPos, vertexData[1].tangentFragPos, vertexData[2].tangentFragPos).transpose();
//							tempFrag.set(bV).transform(tempMat).toArray(tempFragA);
////					tempRasterFrag.setPixel(((w%width) + width)%width, ((h%height) + height)%height, tempFragA);
////					tempRasterFrag.setPixel(Math.min(Math.max(0, w), width), Math.min(Math.max(0, h), height), tempFragA);
////							tempRasterFrag.setPixel(w, h, tempFragA);
//							tempRasterFrag.setPixel(w, h, tempFragA);
//
//
//							tempViewDir.set(tempView).sub(tempFrag).normalize().toArray(tempViewDirA);
////					tempRasterView.setPixel(((w%width) + width)%width, ((h%height) + height)%height, tempViewA);
////					tempRasterView.setPixel(Math.min(Math.max(0, w), width), Math.min(Math.max(0, h), height), tempViewA);
////							tempRasterView.setPixel(w, h, tempViewA);
//							tempRasterViewDir.setPixel(w, h, tempViewDirA);
//							if(h==40 && w == 150){
//								System.out.println("bV: " + bV + ", tempLight: " + tempLight + ", tempView: " + tempView + ", tempFrag: " + tempFrag);
//								System.out.println("light pixel: " + Arrays.toString(tempRasterLight.getPixel(w, h, tempLightA)));
//								System.out.println("view pixel:  " + Arrays.toString(tempRasterView.getPixel(w, h, tempViewA)));
//								System.out.println("frag pixel:  " + Arrays.toString(tempRasterFrag.getPixel(w, h, tempFragA)));
//							}
//						}
//
//					}
//				}
//			}
			for(int h = (int) Math.floor(min.y-1); h < (int) Math.ceil(max.y+1); h++){
				if(0<=h && h<height){
					for(int w = (int) Math.floor(min.x-1); w < (int)Math.ceil(max.x+1); w++){
						if(0<=w && w<width){
							temp.set(w/(float)width, h/(float)height);
							Vec3 bV = getBarycentric(tVerts, temp);
							tempMat.set(vertexData[0].tangentLightPos, vertexData[1].tangentLightPos, vertexData[2].tangentLightPos).transpose();
							tempLight.set(bV).transform(tempMat);

							tempMat.set(vertexData[0].tangentViewPos, vertexData[1].tangentViewPos, vertexData[2].tangentViewPos).transpose();
							tempView.set(bV).transform(tempMat);

							tempMat.set(vertexData[0].tangentFragPos, vertexData[1].tangentFragPos, vertexData[2].tangentFragPos).transpose();
							tempFrag.set(bV).transform(tempMat);

							tempViewDir.set(tempView).sub(tempFrag).normalize().add(Vec3.ONE).scale(127.0f).minimize(maxColor).toArray(tempViewDirA);
							tempRasterViewDir.setPixel(w, h, tempViewDirA);

//							tempMat.set(
//									t1.set(Vec3.ONE).scale(triangle.get(0).getNormal().dot(Vec3.Z_AXIS)),
//									t2.set(Vec3.ONE).scale(triangle.get(1).getNormal().dot(Vec3.Z_AXIS)),
//									t3.set(Vec3.ONE).scale(triangle.get(2).getNormal().dot(Vec3.Z_AXIS)))
//									.transpose();
//							tempNorm.set(bV).transform(tempMat);
							tempMat.set(triangle.get(0).getNormal(), triangle.get(1).getNormal(), triangle.get(2).getNormal()).transpose();
							tempNorm.set(bV).transform(tempMat);

//							tempNorm.normalize().add(Vec3.ONE).scale(127.0f).minimize(maxColor).toArray(tempNormA);
//							tempRasterNorm.setPixel(w, h, tempNormA);
							t1.set(Vec3.ONE).scale(tempNorm.normalize().dot(Vec3.Z_AXIS)).add(Vec3.ONE).scale(127.0f).minimize(maxColor).toArray(tempNormA);
							tempRasterNorm.setPixel(w, h, tempNormA);


							tempLight.normalize().add(Vec3.ONE).scale(127.0f).minimize(maxColor).toArray(tempLightA);
							tempRasterLight.setPixel(w, h, tempLightA);

							tempView.normalize().add(Vec3.ONE).scale(127.0f).minimize(maxColor).toArray(tempViewA);
							tempRasterView.setPixel(w, h, tempViewA);

							tempFrag.normalize().add(Vec3.ONE).scale(127.0f).minimize(maxColor).toArray(tempFragA);
							tempRasterFrag.setPixel(w, h, tempFragA);


							if(h==40 && w == 150 || tempTemp.set(tempRasterViewDir.getPixel(w, h, tempViewA)).equalLocs(Vec3.ZERO)){
								System.out.println("bV: " + bV + ", tempNorm: " + tempNorm + ", tempLight: " + tempLight + ", tempView: " + tempView + ", tempFrag: " + tempFrag);
								System.out.println("light pixel: " + Arrays.toString(tempRasterNorm.getPixel(w, h, tempNormA)));
								System.out.println("light pixel: " + Arrays.toString(tempRasterLight.getPixel(w, h, tempLightA)));
								System.out.println("light pixel: " + Arrays.toString(tempRasterLight.getPixel(w, h, tempLightA)));
								System.out.println("view pixel:  " + Arrays.toString(tempRasterView.getPixel(w, h, tempViewA)));
								System.out.println("viewD pixel: " + Arrays.toString(tempRasterViewDir.getPixel(w, h, tempViewDirA)));
								System.out.println("frag pixel:  " + Arrays.toString(tempRasterFrag.getPixel(w, h, tempFragA)));
							}
						}

					}
				}
			}

			int[][] uvPoints = getTriUVPoints2(triangle, 0, new float[] {width, height});
			Polygon triClip = new Polygon(uvPoints[0], uvPoints[1], 4);

			lightGraphics.setClip(triClip);
			lightGraphics.drawImage(tempImageLight, 0, 0, null);

			normGraphics.setClip(triClip);
			normGraphics.drawImage(tempImageNorm, 0, 0, null);

			viewGraphics.setClip(triClip);
			viewGraphics.drawImage(tempImageView, 0, 0, null);

			viewDirGraphics.setClip(triClip);
			viewDirGraphics.drawImage(tempImageViewDir, 0, 0, null);

			fragGraphics.setClip(triClip);
			fragGraphics.drawImage(tempImageFrag, 0, 0, null);

//			lightGraphics.fillPolygon(uvPoints[0], uvPoints[1], 4);
//			viewGraphics.fillPolygon(uvPoints[0], uvPoints[1], 4);
//			fragGraphics.fillPolygon(uvPoints[0], uvPoints[1], 4);
		}

		normGraphics.dispose();
		lightGraphics.dispose();
		viewGraphics.dispose();
		viewDirGraphics.dispose();
		fragGraphics.dispose();
		return new BufferedImage[] {normImage, lightImage, viewImage, viewDirImage, fragImage};
	}

	private void drawLineAround(Graphics2D g, Polygon tri, BufferedImage template){
		g.setClip(null);
		int[] xpoints = tri.xpoints;
		int[] ypoints = tri.ypoints;
		for(int i = 0; i < tri.npoints-1; i++){
			Color color1 = new Color(template.getRGB(xpoints[i], ypoints[i]));
			Color color2 = new Color(template.getRGB(xpoints[i + 1], ypoints[i + 1]));
			GradientPaint lP = new GradientPaint(xpoints[i], ypoints[i], color1, xpoints[i+1], ypoints[i+1], color2);
			g.setPaint(lP);
			g.drawLine(xpoints[i], ypoints[i], xpoints[i+1], ypoints[i+1]);
		}
	}


	private static List<VertexData> getVec3VertexDataMap2(Triangle triangle, Vec3 viewDirection, Vec3 lightDirection) {
		List<VertexData> vertexToData = new ArrayList<>();
		for (GeosetVertex vertex : triangle.getVerts()) {
			VertexData vertexData = getVertexData(viewDirection, lightDirection, vertex);

			vertexToData.add(vertexData);
		}
		return vertexToData;
	}

	private static VertexData[] getVertexData(Triangle triangle, Vec3 viewDirection, Vec3 lightDirection) {
		VertexData[] vertexToData = new VertexData[3];
		for (int i = 0; i < 3; i++) {
			vertexToData[i] = getVertexData(viewDirection, lightDirection, triangle.get(i));
		}
		return vertexToData;
	}
	private static VertexData[] getVertexData(Triangle triangle, Vec3 viewDirection, Vec3 lightDirection, Mat4 modelSpaceMat) {
		VertexData[] vertexToData = new VertexData[3];
		for (int i = 0; i < 3; i++) {
			vertexToData[i] = getVertexData(viewDirection, lightDirection, triangle.get(i), modelSpaceMat);
		}
		return vertexToData;
	}

	public BufferedImage getBufferedImage2(Set<Triangle> triangles, int width, int height) {

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = image.getGraphics();
		graphics.setColor(Color.BLACK);
		graphics.fill3DRect(0, 0, width, height, false);
		graphics.setColor(Color.WHITE);



		for (Triangle triangle : triangles) {
			int[][] uvPoints = getTriUVPoints2(triangle, 0, new float[] {width, height});
			graphics.fillPolygon(uvPoints[0], uvPoints[1], 4);
		}

		graphics.dispose();
		return image;
	}

	private int[][] getTriUVPoints2(Triangle t, int uvLayer, float[] scale){
		int[][] output = new int[2][4];
		for (int i = 0; i < 3; i++) {
			output[0][i] = Math.round(t.getTVert(i, uvLayer).dot(Vec2.X_AXIS) * scale[0]);
			output[1][i] = Math.round(t.getTVert(i, uvLayer).dot(Vec2.Y_AXIS) * scale[1]);
		}
		output[0][3] = output[0][0];
		output[1][3] = output[1][0];
		return output;
	}

	private Set<Triangle> getMatTriangles(Set<Material> materials, ModelView modelView) {
		Set<Geoset> geosets = new HashSet<>();
		for (Geoset geoset : modelView.getModel().getGeosets()){
			if(materials.contains(geoset.getMaterial())){
				geosets.add(geoset);
			}
		}
		return getGeoTriangles(geosets);
	}

	private Set<Triangle> getGeoTriangles(Collection<Geoset> geosets){
		Set<Triangle> triangles = new HashSet<>();
		for (Geoset geoset : geosets) {
			triangles.addAll(geoset.getTriangles());
		}

		return triangles;
	}


	public static void convertToV800BakingTextures(int targetLevelOfDetail, EditableModel model,
	                                               java.io.File outputDirectorySetting, String assetDirectoryRelativePath) {
		String outputDirectory;
		if (assetDirectoryRelativePath != null) {
			outputDirectory = outputDirectorySetting.getPath() + java.io.File.separatorChar + assetDirectoryRelativePath;
		}
		else {
			outputDirectory = outputDirectorySetting.getPath();
		}
		// Things to fix:
		// 1.) format version
		model.setFormatVersion(800);
		// 2.) materials: bake to only diffuse
		Map<Triangle, Integer> triangleToTeamColorPixelCount = new HashMap<>();
		Map<Layer, BakingInfo> layerToBakingInfo = new HashMap<>();

		Map<Material, List<Geoset>> materialToUsers = new HashMap<>();
		for (Material material : model.getMaterials()){
			if((material.getLayers().size() == 1) && (material.getShaderString().equals(Material.SHADER_HD_DEFAULT_UNIT))){
				List<Geoset> geosets = materialToUsers.computeIfAbsent(material, k -> new ArrayList<>());
				for (Geoset geo : model.getGeosets()) {
					if ((geo.getLevelOfDetail() == targetLevelOfDetail || geo.getLevelOfDetail() == -1) && geo.getMaterial().equals(material)) {
						// find geosets bound to this material, needed for baking
						geosets.add(geo);
					}
				}

			}
		}


		Vec3 viewDirection = getAltViewDirection(model);
		for (Material material : materialToUsers.keySet()) {
			List<Geoset> geosets = materialToUsers.get(material);
			for (Layer layer : material.getLayers()) {
				BakingInfo bakingInfo = new BakingInfo();
				Layer zeroLayer = material.getLayers().get(0);
				TextureBakingCell[][] bakingCells = getTextureBakingCells(zeroLayer, model.getWrappedDataSource());
				Map<GeosetVertex, VertexData> vertexToData = getVertexToData(geosets, zeroLayer, viewDirection);
				GeoBakingCell[][] geoBakingCells = getGeoBakingCells(geosets, bakingCells[0].length, bakingCells.length, vertexToData);

				BufferedImage bakedImg = getImage(bakingCells, geoBakingCells);
				bakingInfo.bakedTexturePath = getString(material, outputDirectory, bakedImg);
				layerToBakingInfo.put(layer, bakingInfo);
			}
		}
		replaceTextures(model, assetDirectoryRelativePath, layerToBakingInfo);
		fixPaths(model, assetDirectoryRelativePath);
	}

	private static void replaceTextures(EditableModel model, String assetDirectoryRelativePath, Map<Layer, BakingInfo> layerToBakingInfo) {
		for (Material material : model.getMaterials()) {
			if(material.getShaderString().equals(Material.SHADER_HD_DEFAULT_UNIT)){
				List<Layer> additionalLayers = new ArrayList<>();
				for (Layer layer : material.getLayers()) {
					BakingInfo bakingInfo = layerToBakingInfo.get(layer);
					bakingInfo.alphaFlag = layer.getVisibilityFlag();
					layer.remove(bakingInfo.alphaFlag);
//					EnumMap<HD_Material_Layer, Bitmap> shaderTextures = layer.getShaderTextures();
					Bitmap diffuseBitmap = layer.getTexture(HD_Material_Layer.DIFFUSE.ordinal());

					Bitmap emissive = layer.getTexture(HD_Material_Layer.EMISSIVE.ordinal());
					if ((emissive != null) && !emissive.getPath().toLowerCase().contains("black32")) {
						Layer layerThree = new Layer();
						layerThree.setFilterMode(FilterMode.ADDITIVE);
						layerThree.setTexture(HD_Material_Layer.DIFFUSE.ordinal(), emissive);
						layerThree.setFilterMode(FilterMode.ADDITIVE);
						layerThree.setUnshaded(true);
						layerThree.setUnfogged(true);
						additionalLayers.add(layerThree);
					}
					if (bakingInfo.bakedTexturePath != null) {
						Bitmap newBitmap = new Bitmap(diffuseBitmap);
						layer.setTexture(HD_Material_Layer.DIFFUSE.ordinal(), newBitmap);
						if (assetDirectoryRelativePath != null) {
							String usedPath = assetDirectoryRelativePath + "\\" + bakingInfo.bakedTexturePath;
							int lastIndexOfMod = usedPath.lastIndexOf(".w3mod");
							if (lastIndexOfMod != -1) {
								usedPath = usedPath.substring(lastIndexOfMod + ".w3mod/".length());
							}
							newBitmap.setPath(usedPath);
						}
						else {
							newBitmap.setPath(bakingInfo.bakedTexturePath);
						}
						if (material.getFlags().contains(Material.flag.TWO_SIDED)) {
							material.getFlags().remove(Material.flag.TWO_SIDED);
							layer.setTwoSided(true);
						}
					}
					layer.remove("EmissiveGain");
				}
				material.getLayers().addAll(additionalLayers);
			}
		}
	}

	private static void fixPaths(EditableModel model, String assetDirectoryRelativePath) {
		for (Bitmap tex : model.getTextures()) {
			String path = tex.getPath();
			if ((path != null) && !path.isEmpty()) {
				if (!path.toLowerCase().contains("_bake")) {
					int dotIndex = path.lastIndexOf('.');
					if ((dotIndex != -1) && !path.endsWith(".blp")) {
						path = path.substring(0, dotIndex);
					}
					if (!path.endsWith(".blp")) {
						if (assetDirectoryRelativePath == null) {
							path = "_HD.w3mod:" + path;
							path += ".blp";
						} else {
							path += ".tga";
						}
					}
					tex.setPath(path);
				}
			}
		}
		if (assetDirectoryRelativePath != null) {
			for (Bitmap tex : model.getTextures()) {
				String path = tex.getPath();
				if ((path != null) && !path.isEmpty()) {
					int lastIndexOfMod = path.lastIndexOf(".w3mod");
					if (lastIndexOfMod != -1) {
						lastIndexOfMod += ".w3mod/".length();
						path = path.substring(lastIndexOfMod);
						tex.setPath(path);
					}
				}
			}
		}
	}

	/**
	 * Creates and saves the file for the baked texture corresponding to this
	 * material, then returns the texture path name of the created file to be used
	 * in the model Bitmap in the future.
	 *
	 * @param workingDirectory
	 * @param outputDirectory
	 * @param model
	 * @param lod
	 * @return
	 */
	public static String getBakedHDNonEmissiveBufferedImage(Material material, DataSource workingDirectory, String outputDirectory,
	                                                        EditableModel model, int lod, Map<Triangle, Integer> triangleToTeamColorPixelCount) {
		Vec3 viewDirection = getAltViewDirection(model);
		Layer zeroLayer = material.getLayers().get(0);
		List<Geoset> geosets = model.getGeosets();

		TextureBakingCell[][] bakingCells = getTextureBakingCells(zeroLayer, model.getWrappedDataSource());
		Map<GeosetVertex, VertexData> vertexToData = getVertexToData(geosets, zeroLayer, viewDirection);
		GeoBakingCell[][] geoBakingCells = getGeoBakingCells(geosets, bakingCells[0].length, bakingCells.length, vertexToData);
		BufferedImage bakedImg = getImage(bakingCells, geoBakingCells);
		return getString(material, outputDirectory, bakedImg);
	}

	private static String getString(Material material, String outputDirectory, BufferedImage bakedImg) {
		String newTexturePath = getNewTexturePath(material);
		if(bakedImg != null){
			try {
				String pathname = outputDirectory + "/" + newTexturePath;
				TgaFile.writeTGA(bakedImg, new java.io.File(pathname));
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return newTexturePath;
	}

	private static String getNewTexturePath(Material material) {
		String diffusePath = material.getLayers().get(0).firstTexture().getPath();
		String diffuseName = diffusePath.substring(Math.max(diffusePath.lastIndexOf('/'), diffusePath.lastIndexOf('\\')) + 1);
		return diffuseName + "_baked.tga";
	}

	private static Map<GeosetVertex, VertexData> getVertexToData(List<Geoset> geosets, Layer layer, Vec3 altViewDirection) {
		Bitmap diffuseBitMap = layer.getTexture(HD_Material_Layer.DIFFUSE.ordinal());
		String diffuseTexturePath = diffuseBitMap.getRenderableTexturePath();


		Vec3 lightDirection = new Vec3(-24.1937f, 30.4879f, 444.411f);

		Vec3 viewDirection = new Vec3(32f, 0, 128f);
		if (diffuseTexturePath.toLowerCase(Locale.US).contains("portrait")) {
			viewDirection.set(altViewDirection);
		}

		return getVec3VertexDataMap(geosets, viewDirection, lightDirection);
	}

	private static Vec3 getAltViewDirection(EditableModel model) {
		Vec3 viewDirection = new Vec3(32f, 0, 128f);
		if ((model.getCameras().size() > 0)) {
			viewDirection.set(model.getCameras().get(0).getPosition());
		}
		return viewDirection;
	}

	private static GeoBakingCell[][] getGeoBakingCells(List<Geoset> geosets, int w, int h, Map<GeosetVertex, VertexData> vertexToData) {
		Mat4 tempMat = new Mat4();
		GeoBakingCell[][] geoBakingCells = new GeoBakingCell[h][w];
		for (Geoset geo : geosets) {
			// find geosets bound to this material, needed for baking
			for (Triangle tri : geo.getTriangles()) {
				// find the triangles using this material, since we need to eval them in 3d space to bake
				GeosetVertex[] verts = tri.getVerts();
				Vec2[] tv = tri.getTVerts(0);
				Vec2 min = new Vec2(tv[0]).minimize(tv[1]).minimize(tv[2]);
				Vec2 max = new Vec2(tv[0]).maximize(tv[1]).maximize(tv[2]);

				int pixelWidth = w;
				int pixelHeight = h;
				int iminX = (int) Math.floor(min.x * pixelWidth);
				int iminY = (int) Math.floor(min.y * pixelHeight);
				int imaxX = (int) Math.ceil(max.x * pixelWidth);
				int imaxY = (int) Math.ceil(max.y * pixelHeight);
				int[] polygonXPoints = {Math.round(tv[0].x * pixelWidth), Math.round(tv[1].x * pixelWidth), Math.round(tv[2].x * pixelWidth) };
				int[] polygonYPoints = {Math.round(tv[0].y * pixelHeight), Math.round(tv[1].y * pixelHeight), Math.round(tv[2].y * pixelHeight) };
				Polygon polygon = new Polygon(polygonXPoints, polygonYPoints, 3);
				VertexData[] vertexData = new VertexData[]{vertexToData.get(verts[0]), vertexToData.get(verts[1]), vertexToData.get(verts[2])};
				for (int i = iminY; i <= imaxY; i++) {
					for (int j = iminX; j <= imaxX; j++) {
						if (polygon.contains(j, i)) {
							int jToUse = ((j % pixelWidth) + pixelWidth)% pixelWidth;
							int iToUse = ((i % pixelHeight) + pixelHeight)% pixelHeight;

							Vec3 bV = getBarycentric(tv, pixelWidth, pixelHeight, jToUse, iToUse);

							geoBakingCells[iToUse][jToUse] = getGeoBakingCell(tempMat, verts, vertexData, bV);
						}
					}
				}
			}
		}
		return geoBakingCells;
	}

	private static GeoBakingCell getGeoBakingCell(Mat4 tempMat, GeosetVertex[] verts, VertexData[] vertexData, Vec3 bV) {
		GeoBakingCell bakingCell = new GeoBakingCell();
		tempMat.set(verts[0].getNormal(), verts[1].getNormal(), verts[2].getNormal()).transpose();
		bakingCell.setBarycentricNormal(new Vec3(bV).transform(0, tempMat));

		tempMat.set(verts[0], verts[1], verts[2]).transpose();
		bakingCell.setBarycentricPosition(new Vec3(bV).transform(tempMat));

		tempMat.set(verts[0].getTangent(), verts[1].getTangent(), verts[2].getTangent()).transpose();
		bakingCell.setBarycentricTangent(new Vec4(bV, verts[0].getTangent().w).transform(tempMat, 0));

		tempMat.set(vertexData[0].tangentLightPos, vertexData[1].tangentLightPos, vertexData[2].tangentLightPos).transpose();
		bakingCell.setTangentLightPos(new Vec3(bV).transform(tempMat));

		tempMat.set(vertexData[0].tangentViewPos, vertexData[1].tangentViewPos, vertexData[2].tangentViewPos).transpose();
		bakingCell.setTangentViewPos(new Vec3(bV).transform(tempMat));

		tempMat.set(vertexData[0].tangentFragPos, vertexData[1].tangentFragPos, vertexData[2].tangentFragPos).transpose();
		bakingCell.setTangentFragPos(new Vec3(bV).transform(tempMat));
		return bakingCell;
	}

	private static Vec3 getBarycentric(Vec2[] tv, double pixelWidth, double pixelHeight, double jToUse, double iToUse) {
		double unitSpaceX = jToUse / pixelWidth;
		double unitSpaceY = iToUse / pixelHeight;
		Vec2 unitSpaceV = new Vec2(unitSpaceX, unitSpaceY);

		// barycentric
		double denom = areaOfTriangle(tv[0], tv[1], tv[2]);
		double b0 = areaOfTriangle(unitSpaceV, tv[1], tv[2]) / denom;
		double b1 = areaOfTriangle(tv[0], unitSpaceV, tv[2]) / denom;
		double b2 = areaOfTriangle(tv[0], tv[1], unitSpaceV) / denom;
		return new Vec3(b0, b1, b2);
	}
	private static Vec3 getBarycentric(Vec2[] tv, Vec2 pointToEval) {
		// barycentric
//		double denom = Math.max(areaOfTriangle(tv[0], tv[1], tv[2]), Float.MIN_VALUE*10);
//		double b0 = areaOfTriangle(pointToEval, tv[1], tv[2]) / denom;
//		double b1 = areaOfTriangle(tv[0], pointToEval, tv[2]) / denom;
//		double b2 = areaOfTriangle(tv[0], tv[1], pointToEval) / denom;
//		return new Vec3(b0, b1, b2);
		double denom = Math.max(areaOfTriangle(tv[0], tv[1], tv[2]), Float.MIN_VALUE*10);
		double b0 = areaOfTriangle(pointToEval, tv[1], tv[2]);
		double b1 = areaOfTriangle(tv[0], pointToEval, tv[2]);
		double b2 = areaOfTriangle(tv[0], tv[1], pointToEval);
		if(Double.isNaN(denom) || Double.isNaN(b0) || Double.isNaN(b1) || Double.isNaN(b2)){
			System.out.println("denom: " + denom + ", b0: " + b0 + ", b1: " + b1 + ", b2: " + b2
					+ "\n\tpointToEval: " + pointToEval
					+ "\n\ttv[0]: " + tv[0]
					+ ", tv[1]: " + tv[1]
					+ ", tv[2]: " + tv[2]);
//			return new Vec3(b0, b1, b2).scale((float) (1/denom));
		}
		return new Vec3(b0, b1, b2).scale((float) (1/denom));
	}

	private static Map<GeosetVertex, VertexData> getVec3VertexDataMap(List<Geoset> geosets , Vec3 viewDirection, Vec3 lightDirection) {
		Map<GeosetVertex, VertexData> vertexToData = new HashMap<>();
		for (Geoset geo : geosets) {
			for (GeosetVertex vertex : geo.getVertices()) {
				vertexToData.put(vertex, getVertexData(viewDirection, lightDirection, vertex));
			}
		}
		return vertexToData;
	}

	private static VertexData getVertexData(Vec3 viewDirection, Vec3 lightDirection, GeosetVertex vertex) {
		// hacky fake vertex shader-like thing (should closely match with code in vertex shader for HD previewing)
		Vec3 tangent = vertex.getTangent().getVec3();
		Vec3 normal = new Vec3(vertex.getNormal());
		Vec3 temp = new Vec3(normal).scale(tangent.dot(normal));
		if (tangent.equals(temp)) {
			tangent.sub(temp);
			if (tangent.lengthSquared() != 0) {
				tangent.normalize();
			}
		}
		Vec3 binormal = new Vec3(normal).cross(tangent).scale(vertex.getTangent().w);
		if (binormal.length() != 0) {
			binormal.normalize();
		}
		Mat4 tbn = new Mat4().set(tangent, binormal, normal);

		Vec3 tangentLightPos = new Vec3(lightDirection).transform(tbn); // light position in transformed TBN space
		Vec3 tangentViewPos = new Vec3(viewDirection).transform(tbn); // view position in transformed TBN space
		Vec3 tangentFragPos = new Vec3(vertex).transform(tbn); // frag pos in transformed tbn space

		return new VertexData(tangentLightPos, tangentViewPos, tangentFragPos);
	}
	private static VertexData getVertexData(Vec3 viewDirection, Vec3 lightDirection, GeosetVertex vertex, Mat4 modelSpaceProjection) {
		// hacky fake vertex shader-like thing (should closely match with code in vertex shader for HD previewing)
		Vec3 tangent = vertex.getTangent().getVec3();
		Vec3 normal = new Vec3(vertex.getNormal());
		Vec3 temp = new Vec3(normal).scale(tangent.dot(normal));
		if (tangent.equals(temp)) {
			tangent.sub(temp);
			if (tangent.lengthSquared() != 0) {
				tangent.normalize();
			}
		}
		Vec3 binormal = new Vec3(normal).cross(tangent).scale(vertex.getTangent().w);
		if (binormal.length() != 0) {
			binormal.normalize();
		}
		Mat4 tbn = new Mat4().set(tangent, binormal, normal);

		Vec3 tangentLightPos = new Vec3(lightDirection).transform(tbn); // light position in transformed TBN space
		Vec3 tangentViewPos = new Vec3(viewDirection).transform(tbn); // view position in transformed TBN space
		Vec3 tangentFragPos = new Vec3(vertex).transform(tbn); // frag pos in transformed tbn space

		return new VertexData(tangentLightPos, tangentViewPos, tangentFragPos);
	}

//	private static BufferedImage getImage(TextureBakingCell[][] bakingCells, GeoBakingCell[][] geoBakingCells) {
//		int nShadedPixels = 0;
//		int nDiffusePixels = 0;
//		OutputCell[][] outputCells = new OutputCell[bakingCells.length][bakingCells[0].length];
//		BufferedImage bakedImg = new BufferedImage(bakingCells[0].length, bakingCells.length, BufferedImage.TYPE_INT_ARGB);
//		for (int i = 0; i < bakingCells.length; i++) {
//			for (int j = 0; j < bakingCells[i].length; j++) {
//				// fake fragment shader thing, obviously will be very wrong in some cases if the
//				// texture is used on multiple triangles in different places
//
//				Vec3 fragColorRGB = new Vec3();
//
//				TextureBakingCell textureBakingCell = bakingCells[i][j];
//				GeoBakingCell geoBakingCell = geoBakingCells[i][j];
//				outputCells[i][j] = new OutputCell();
//				OutputCell outputCell = outputCells[i][j];
//
//				float teamColorNess = ((textureBakingCell.ormRGB >> 24) & 0xFF) / 255.0f;
//				float nonTeamColorNess = 1.0f - teamColorNess;
//
//				float baseRed   = ((textureBakingCell.diffuseRGB >> 16) & 0xFF) / 255.0f;
//				float baseGreen = ((textureBakingCell.diffuseRGB >>  8) & 0xFF) / 255.0f;
//				float baseBlue  = ((textureBakingCell.diffuseRGB >>  0) & 0xFF) / 255.0f;
//				Vec3 diffuse = new Vec3(baseRed, baseGreen, baseBlue);
////				Vec3 d = new Vec3().setFromColor(new Color(textureBakingCell.diffuseRGB));
//				diffuse.scale(nonTeamColorNess);
//				if (geoBakingCell.tangentFragPos != null) {
//					float normalX = ((((textureBakingCell.normalRGB >> 16) & 0xFF) / 255.0f) * 2.0f) - 1.0f;
//					float normalY = ((((textureBakingCell.normalRGB >> 8) & 0xFF) / 255.0f) * 2.0f) - 1.0f;
//					Vec3 normal = new Vec3(normalY, normalX, (float) Math.sqrt(1.0 - ((normalX * normalX) + (normalY * normalY))));
//					Vec3 lightDir = new Vec3(0, 0, 1);// textureBakingCell.tangentViewPos;
//					lightDir.set(geoBakingCell.tangentLightPos);
//					lightDir.normalize();
//					{
//						float cosTheta = (lightDir.dot(normal) * 0.5f) + 0.5f;
//						float lambertFactor = (float) Math.max(0.0, Math.min(1.0, cosTheta));
//
//						diffuse.scale((float) Math.max(0.0, Math.min(1.0, lambertFactor)));
//					}
//					Vec3 viewDir = new Vec3(geoBakingCell.tangentViewPos).sub(geoBakingCell.tangentFragPos).normalize();
//					Vec3 reflectDir = new Vec3(lightDir).scale(-1);
//					reflect(reflectDir, normal);
//					Vec3 halfwayDir = new Vec3(lightDir).add(viewDir).normalize();
//					float spec = (float) Math.pow(Math.max(normal.dot(halfwayDir), 0.0f), 32.0f);
//					float metalness = ((textureBakingCell.ormRGB >> 0) & 0xFF) / 255.0f;
//					float roughness = ((textureBakingCell.ormRGB >> 8) & 0xFF) / 255.0f;
//					float specularX = (float) ((Math.max(-roughness + 0.5, 0.0) + metalness) * spec);
//					Vec3 specular = new Vec3(Vec3.ONE).scale(specularX);
//					// TODO maybe fresnel here
//					fragColorRGB.set(specular).add(diffuse);
//					nShadedPixels++;
//				}
//				else {
//					fragColorRGB.set(diffuse);
//					nDiffusePixels++;
//				}
//
//				int red   = Math.round(Math.min(255, fragColorRGB.x * 255f)) & 0xFF;
//				int green = Math.round(Math.min(255, fragColorRGB.y * 255f)) & 0xFF;
//				int blue  = Math.round(Math.min(255, fragColorRGB.z * 255f)) & 0xFF;
//
//				float alpha = ((textureBakingCell.diffuseRGB >> 24) & 0xFF) / 255.0f;
//				alpha *= 1.0f - (teamColorNess * Math.max(baseRed, Math.max(baseGreen, baseBlue)));
//				int alphaI = Math.round(alpha * 255f) & 0xFF;
//
//				outputCell.outputARGB = (alphaI << 24) | (red << 16) | (green << 8) | (blue << 0);
//				bakedImg.setRGB(j, i, outputCell.outputARGB);
////				geoBakingCell.outputARGB = (alphaI << 24) | (red << 16) | (green << 8) | (blue << 0);
////				bakedImg.setRGB(j, i, geoBakingCell.outputARGB);
//			}
//		}
//		System.out.println("baked texture with " + nShadedPixels + " pixels loading shader data and " + nDiffusePixels + " pixels defaulting back to diffuse data");
//		return bakedImg;
//	}

	private static BufferedImage getImage(TextureBakingCell[][] bakingCells, GeoBakingCell[][] geoBakingCells) {
		int nShadedPixels = 0;
		int nDiffusePixels = 0;

		OutputCell outputCell = new OutputCell();
		BufferedImage bakedImg = new BufferedImage(bakingCells[0].length, bakingCells.length, BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < bakingCells.length; i++) {
			for (int j = 0; j < bakingCells[i].length; j++) {
				// fake fragment shader thing, obviously will be very wrong in some cases if the
				// texture is used on multiple triangles in different places

				Vec3 fragColorRGB = new Vec3();

				TextureBakingCell textureBakingCell = bakingCells[i][j];
				GeoBakingCell geoBakingCell = geoBakingCells[i][j];

				float teamColorNess = ((textureBakingCell.ormRGB >> 24) & 0xFF) / 255.0f;
				float nonTeamColorNess = 1.0f - teamColorNess;

				float baseRed   = ((textureBakingCell.diffuseRGB >> 16) & 0xFF) / 255.0f;
				float baseGreen = ((textureBakingCell.diffuseRGB >>  8) & 0xFF) / 255.0f;
				float baseBlue  = ((textureBakingCell.diffuseRGB >>  0) & 0xFF) / 255.0f;
				Vec3 diffuse = new Vec3(baseRed, baseGreen, baseBlue);
//				Vec3 d = new Vec3().setFromColor(new Color(textureBakingCell.diffuseRGB));
				diffuse.scale(nonTeamColorNess);
				if (geoBakingCell.tangentFragPos != null) {
					Vec3 normal = getNormal(textureBakingCell.normalRGB);

					Vec3 lightDir = geoBakingCell.getLightDir();
					diffuse.scale(getLightFactor(lightDir.dot(normal)));

					Vec3 reflectDir = getReflected(lightDir, normal);
					Vec3 halfwayDir = new Vec3(lightDir).add(geoBakingCell.getViewDir()).normalize();
					float spec = (float) Math.pow(Math.max(normal.dot(halfwayDir), 0.0f), 32.0f);
					Vec3 specular = new Vec3(Vec3.ONE).scale(textureBakingCell.getSpecular(spec));
					// TODO maybe fresnel here
					fragColorRGB.set(specular).add(diffuse);
					nShadedPixels++;
				} else {
					fragColorRGB.set(diffuse);
					nDiffusePixels++;
				}

				int red   = Math.round(Math.min(255, fragColorRGB.x * 255f)) & 0xFF;
				int green = Math.round(Math.min(255, fragColorRGB.y * 255f)) & 0xFF;
				int blue  = Math.round(Math.min(255, fragColorRGB.z * 255f)) & 0xFF;

				float alpha = ((textureBakingCell.diffuseRGB >> 24) & 0xFF) / 255.0f;
				alpha *= 1.0f - (teamColorNess * Math.max(baseRed, Math.max(baseGreen, baseBlue)));
				int alphaI = Math.round(alpha * 255f) & 0xFF;

				outputCell.outputARGB = (alphaI << 24) | (red << 16) | (green << 8) | (blue << 0);
				bakedImg.setRGB(j, i, outputCell.outputARGB);
			}
		}
		System.out.println("baked texture with " + nShadedPixels + " pixels loading shader data and " + nDiffusePixels + " pixels defaulting back to diffuse data");
		return bakedImg;
	}

	private static float getLightFactor(float project) {
		float cosTheta = (project * 0.5f) + 0.5f;
		float lambertFactor = (float) Math.max(0.0, Math.min(1.0, cosTheta));

		return (float) Math.max(0.0, Math.min(1.0, lambertFactor));
	}

	private static TextureBakingCell[][] getTextureBakingCells(Layer layer, DataSource workingDirectory) {

		Bitmap diffuse = layer.getTexture(HD_Material_Layer.DIFFUSE.ordinal());
		Bitmap normal = layer.getTexture(HD_Material_Layer.VERTEX.ordinal());
		Bitmap orm = layer.getTexture(HD_Material_Layer.ORM.ordinal());
		Bitmap reflect = layer.getTexture(HD_Material_Layer.REFLECTIONS.ordinal());

		BufferedImage diffuseBI = BLPHandler.get().getTextureHelper(workingDirectory, diffuse).getBufferedImage();
		BufferedImage normalBI = BLPHandler.get().getTextureHelper(workingDirectory, normal).getBufferedImage();
		BufferedImage ormBI = BLPHandler.get().getTextureHelper(workingDirectory, orm).getBufferedImage();
		BufferedImage reflectionsBI = BLPHandler.get().getTextureHelper(workingDirectory, reflect).getBufferedImage();
		System.out.println("Reflections: " + reflectionsBI.getWidth() + " x " + reflectionsBI.getHeight());

		int diffuseTextureDataWidth = diffuseBI.getWidth();
		int diffuseTextureDataHeight = diffuseBI.getHeight();
		System.out.println("Diffuse: " + diffuseTextureDataWidth + " x " + diffuseTextureDataHeight);

		int normalTextureDataWidth = normalBI.getWidth();
		int normalTextureDataHeight = normalBI.getHeight();
		System.out.println("Normal: " + normalTextureDataWidth + " x " + normalTextureDataHeight);

		int ormTextureDataWidth = ormBI.getWidth();
		int ormTextureDataHeight = ormBI.getHeight();
		System.out.println("Orm: " + ormTextureDataWidth + " x " + ormTextureDataHeight);

//		System.out.println("Reflections: " + reflectionsTextureData.getWidth() + " x " + reflectionsTextureData.getHeight());

		int[] largestSize = getLargestSize(diffuseBI, normalBI, ormBI);

		diffuseBI = ImageCreator.getScaledImage(diffuseBI, largestSize[0], largestSize[1]);
		normalBI = ImageCreator.getScaledImage(normalBI, largestSize[0], largestSize[1]);
		ormBI = ImageCreator.getScaledImage(ormBI, largestSize[0], largestSize[1]);
		reflectionsBI = ImageCreator.getScaledImage(reflectionsBI, largestSize[0], largestSize[1]);

		Raster diffuseR = diffuseBI.getRaster();
		Raster normalR = normalBI.getRaster();
		Raster ormR = ormBI.getRaster();
		Raster reflectionsR = reflectionsBI.getRaster();

		TextureBakingCell[][] bakingCells = new TextureBakingCell[largestSize[0]][largestSize[1]];
		for (int i = 0; i < bakingCells.length; i++) {
			for (int j = 0; j < bakingCells[i].length; j++) {
				bakingCells[i][j] = new TextureBakingCell();
				bakingCells[i][j].diffuseRGB = diffuseBI.getRGB(j % diffuseTextureDataWidth,i % diffuseTextureDataHeight);
				bakingCells[i][j].normalRGB = normalBI.getRGB(j % normalTextureDataWidth, i % normalTextureDataHeight);
				bakingCells[i][j].ormRGB = ormBI.getRGB(j % ormTextureDataWidth, i % ormTextureDataHeight);

				bakingCells[i][j].diffuse = diffuseR.getPixel(j % diffuseTextureDataWidth,i % diffuseTextureDataHeight, new float[diffuseR.getNumBands()]);
				bakingCells[i][j].normal = normalR.getPixel(j % normalTextureDataWidth, i % normalTextureDataHeight, new float[normalR.getNumBands()]);
				bakingCells[i][j].orm = ormR.getPixel(j % ormTextureDataWidth, i % ormTextureDataHeight, new float[ormR.getNumBands()]);
			}
		}
		return bakingCells;
	}

	private static int[] getLargestSize(BufferedImage... images){
		int w = 0;
		int h = 0;
		for(BufferedImage image : images){
			if(image != null){
				w = Math.max(w, image.getWidth());
				h = Math.max(h, image.getHeight());
			}
		}
		return new int[]{w, h};
	}

	public static void reflect(Vec3 left, Vec3 right, Vec3 output) {
		float dot = right.dot(left);
		output.set(right).scale(-2.0f * dot).add(left);
	}
	public static void reflect(Vec3 vec3, Vec3 left) {
		float dot = vec3.dot(left);
		vec3.scale(-2.0f * dot).add(left);
	}
	public static Vec3 getReflected(Vec3 lightDir, Vec3 normal) {
		Vec3 reflection = new Vec3(lightDir).scale(-1);
		float dot = reflection.dot(normal);
		reflection.scale(-2.0f * dot).add(normal);
		return reflection;
	}


	public static double areaOfTriangle2(Vec2 v1,
	                                    Vec2 v2,
	                                    Vec2 v3) {
		double a = v1.distance(v2);
		double b = v2.distance(v3);
		double c = v1.distance(v3);
		double s = (a + b + c) / 2.0;

//		double[] sorted = getSorted(a, b, c);

//		double sqrt = Math.sqrt((sorted[0]+(sorted[1]+sorted[2]))*(sorted[2]-(sorted[0]-sorted[1]))*(sorted[2]+(sorted[0]-sorted[1]))*(sorted[0]+(sorted[1]-sorted[2])))*0.25;

		double a2 = Math.max(a, Math.max(b,c));
		double c2 = Math.min(a, Math.min(b,c));
		double b2 = a < b ? Math.max(a, Math.min(b,c)) : Math.max(b, Math.min(a,c));

		double f1 = a2 + (b2 + c2);
		double a_sub_b = a2 - b2;
		double f2 = c2 - a_sub_b;
		double f3 = c2 + a_sub_b;
		double f4 = a2 + (b2 - c2);
		double sqrt = Math.sqrt(f1 * f2 * f3 * f4) /4.0;

//		double[] sides = new double[]{a, b, c};
//		double[] sides = new double[3];
//		if(a<b){
//			if (a<c){
//				sides[2] = a;
//				if(b < c){
//					sides[0] = c;
//					sides[1] = b;
//				} else {
//					sides[0] = b;
//					sides[1] = c;
//				}
//			} else if (c <= a){
//				sides[0] = b;
//				sides[1] = a;
//				sides[2] = c;
//			}
//		} else if(b<=a){
//
//			if (a<c){
//				sides[0] = c;
//				sides[1] = a;
//				sides[2] = b;
//			} else if (c <= a){
//				sides[0] = a;
//				if(b<c){
//					sides[1] = c;
//					sides[2] = b;
//				} else {
//					sides[1] = b;
//					sides[2] = c;
//				}
//			}
//		}

		double a1 = s * (s - a) * (s - b) * (s - c);
		double sqrt2 = Math.sqrt(a1);
		if(Double.isNaN(a) || Double.isNaN(b) || Double.isNaN(c) || Double.isNaN(s) || Double.isNaN(a1) || Double.isNaN(sqrt)){

			System.out.println("\n\n"
//					+ "triTest: " + (sorted[2] - (sorted[0]-sorted[1]))
					+ "triTest: " + (c2 - a_sub_b)
					+ ", a2: " + a2
					+ ", b2: " + b2
					+ ", c2: " + c2
//					+ ", a: " + a
//					+ ", b: " + b
//					+ ", c: " + c
					+ ", s: " + s
					+ ", a1: " + a1
					+ ", sqrt: " + sqrt);
		}
		return sqrt;
	}

	private static double[] getSorted(double... values){
//		double[] sorted = new double[values.length];
		double tempI;
		double tempJ;

		for (int i = 0; i< values.length-1; i++){
			tempI = values[i];
			for (int j = i+1; j < values.length; j++){
				tempJ = values[j];
				if(tempJ<tempI){
					values[i] = tempJ;
					values[j] = tempI;
					tempI = tempJ;
				}
			}
		}
		return values;
	}


	public static double areaOfTriangle(Vec2 v1,
	                                    Vec2 v2,
	                                    Vec2 v3) {
		double a = v1.distance(v2);
		double b = v2.distance(v3);
		double c = v1.distance(v3);
		double s = (a + b + c) / 2.0;

		double a1 = Math.max(0.0, s * (s - a) * (s - b) * (s - c));
		double sqrt = Math.sqrt(a1);
//		if(Double.isNaN(a) || Double.isNaN(b) || Double.isNaN(c) || Double.isNaN(s) || Double.isNaN(a1) || Double.isNaN(sqrt)){
//
//			System.out.println("\n\n"
//					+ ", a: " + a
//					+ ", b: " + b
//					+ ", c: " + c
//					+ ", s: " + s
//					+ ", a1: " + a1
//					+ "sqrt: " + sqrt);
//		}
		return sqrt;
	}

	static Vec3 getNormal(int normalRGB) {
		float normalX = ((((normalRGB >> 16) & 0xFF) / 255.0f) * 2.0f) - 1.0f;
		float normalY = ((((normalRGB >> 8) & 0xFF) / 255.0f) * 2.0f) - 1.0f;
		return new Vec3(normalY, normalX, (float) Math.sqrt(1.0 - ((normalX * normalX) + (normalY * normalY))));
	}

	static float getSpecular(int ormRGB, float spec) {
		float metalness = ((ormRGB >> 0) & 0xFF) / 255.0f;
		float roughness = ((ormRGB >> 8) & 0xFF) / 255.0f;
		return (float) ((Math.max(-roughness + 0.5, 0.0) + metalness) * spec);
	}

	private static class BakingCell {
		float[] diffuse;
		float[] normal;
		float[] orm;
		int diffuseRGB;
		int normalRGB;
		int ormRGB;
		public Vec3 barycentricNormal;
		public Vec3 barycentricPosition;
//		public double[] barycentricTangent;
		public Vec4 barycentricTangent;
		public Vec3 tangentLightPos;
		public Vec3 tangentViewPos;
		public Vec3 tangentFragPos;
		public int outputARGB;
	}
	private static class OutputCell {
		public int outputARGB;
	}

	private static class GeoBakingCell {
		public Vec3 barycentricNormal;
		public Vec3 barycentricPosition;
		public Vec4 barycentricTangent;
		public Vec3 tangentLightPos;
		public Vec3 tangentViewPos;
		public Vec3 tangentFragPos;

		public Vec3 getBarycentricNormal() {
			return barycentricNormal;
		}

		public GeoBakingCell setBarycentricNormal(Vec3 barycentricNormal) {
			this.barycentricNormal = barycentricNormal;
			return this;
		}

		public Vec3 getBarycentricPosition() {
			return barycentricPosition;
		}

		public GeoBakingCell setBarycentricPosition(Vec3 barycentricPosition) {
			this.barycentricPosition = barycentricPosition;
			return this;
		}

		public Vec4 getBarycentricTangent() {
			return barycentricTangent;
		}

		public GeoBakingCell setBarycentricTangent(Vec4 barycentricTangent) {
			this.barycentricTangent = barycentricTangent;
			return this;
		}

		public Vec3 getTangentLightPos() {
			return tangentLightPos;
		}

		public GeoBakingCell setTangentLightPos(Vec3 tangentLightPos) {
			this.tangentLightPos = tangentLightPos;
			return this;
		}

		public Vec3 getTangentViewPos() {
			return tangentViewPos;
		}

		public GeoBakingCell setTangentViewPos(Vec3 tangentViewPos) {
			this.tangentViewPos = tangentViewPos;
			return this;
		}

		public Vec3 getTangentFragPos() {
			return tangentFragPos;
		}

		public GeoBakingCell setTangentFragPos(Vec3 tangentFragPos) {
			this.tangentFragPos = tangentFragPos;
			return this;
		}

		public Vec3 getViewDir() {
			return new Vec3(tangentViewPos).sub(tangentFragPos).normalize();
		}
		public Vec3 getLightDir() {
			return new Vec3(tangentLightPos).normalize();
		}

		float getLightFactor(Vec3 normal) {
			float project = tangentLightPos.dotNorm(normal);
			float cosTheta = (project * 0.5f) + 0.5f;
			float lambertFactor = (float) Math.max(0.0, Math.min(1.0, cosTheta));

			return (float) Math.max(0.0, Math.min(1.0, lambertFactor));
		}
//		public Vec3 getHalfwayDir() {
//			return new Vec3(lightDir).add(viewDir).normalize();;
//		}
	}

	private static class TextureBakingCell {
		float[] diffuse;
		float[] normal;
		float[] orm;
		int diffuseRGB;
		int normalRGB;
		int ormRGB;

		float getSpecular(float spec) {
			float metalness = ((ormRGB >> 0) & 0xFF) / 255.0f;
			float roughness = ((ormRGB >> 8) & 0xFF) / 255.0f;
			return (float) ((Math.max(-roughness + 0.5, 0.0) + metalness) * spec);
		}

		Vec3 getNormal() {
			float normalX = ((((normalRGB >> 16) & 0xFF) / 255.0f) * 2.0f) - 1.0f;
			float normalY = ((((normalRGB >> 8) & 0xFF) / 255.0f) * 2.0f) - 1.0f;
			return new Vec3(normalY, normalX, (float) Math.sqrt(1.0 - ((normalX * normalX) + (normalY * normalY))));
		}
	}


	private static class BakingInfo {
		String bakedTexturePath;
		AnimFlag<Float> alphaFlag;
	}
	private static class VertexData {

		private Vec3 tangentLightPos;
		private Vec3 tangentViewPos;
		private Vec3 tangentFragPos;

		public VertexData(Vec3 tangentLightPos, Vec3 tangentViewPos, Vec3 tangentFragPos) {
			this.tangentLightPos = tangentLightPos;
			this.tangentViewPos = tangentViewPos;
			this.tangentFragPos = tangentFragPos;
		}

	}
}
