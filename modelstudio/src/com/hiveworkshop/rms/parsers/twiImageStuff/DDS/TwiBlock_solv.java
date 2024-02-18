package com.hiveworkshop.rms.parsers.twiImageStuff.DDS;

import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.Plane;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.Ray;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Arrays;

public class TwiBlock_solv {
	Ray ray = new Ray();
	Plane plane = new Plane();

	Vec3 tempV = new Vec3();
	Vec3[] tempCol = new Vec3[] {new Vec3(), new Vec3()};

	public Ray getFittedRay(Vec3ColorBlock colorBlock){

		if(colorBlock.getTotDistToCenter() == 0){
			ray.set(tempV.set(Vec3.ONE).normalize(), colorBlock.getCenterP());
		} else {
			int nCols = 1;
			tempCol[0].set(colorBlock.getBlock()[0]);
			for(Vec3 vec3 : colorBlock.getBlock()){
				if(0 < tempCol[0].distance(vec3)){
					if(nCols == 1){
						tempCol[1].set(vec3);
						nCols++;
					} else if(0 < tempCol[1].distance(vec3)){
						nCols++;
						break;
					}
				}
			}
			if(nCols == 2){
				ray.set(tempV.set(tempCol[0]).sub(tempCol[1]).normalize(), tempCol[1]);
				return ray;
			}

			tryWithAngles2(colorBlock);
		}
		return ray;
	}

	Vec3 cl1 = new Vec3();
	Vec3 cl2 = new Vec3();
	Vec3 cl1_2 = new Vec3();
	Vec3 cl2_2 = new Vec3();
	Vec3 cl3 = new Vec3();
	Vec3 cl4 = new Vec3();
	Vec3 cl3_2 = new Vec3();
	Vec3 cl4_2 = new Vec3();
	private void tryWithAngles2(Vec3ColorBlock colorBlock) {
		cl1.set(Vec3.ZERO);
		cl2.set(Vec3.ZERO);
		cl3.set(Vec3.ZERO);
		cl4.set(Vec3.ZERO);

		int nCl1 = 0;
		int nCl2 = 0;
		int nCl3 = 0;
		int nCl4 = 0;

		int degLim = 90;
		for(int itt = 0; itt<4; itt++){
			for(Vec3 point : colorBlock.getBlockSubCenter()){
				if(point.degAngleTo(Vec3.ONE)< degLim){
					cl1.add(point);
//				System.out.println("top: " + tempV);
					nCl1++;
				} else {
					cl4.add(point);
//				System.out.println("bot: " + tempV);
					nCl4++;
				}
			}
			if(nCl1 == 0){
				degLim = (int)(degLim * 1.3);
			} else if (nCl4 == 0){
				degLim = (int) (degLim * .7);
			} else {
				break;
			}
		}

		if(cl1.distance(cl4)<=0.00001){
			cl1.add(colorBlock.getBlockSubCenter()[0]);
		}

//		System.out.println("cl1: (" + nCl1 + ") " + cl1 + ", cl4: (" + nCl4 + ") " + cl4);
		cl1.scale(1f/Math.max(1, nCl1));
		cl4.scale(1f/Math.max(1, nCl4));
//		System.out.println("cl1: " + cl1 + ", cl4: " + cl4);
		tempV.set(cl1).sub(cl4).scale(.25f);
//		System.out.println("tempV: " + tempV);
		cl2.set(cl1).sub(tempV);
		cl1.add(tempV);
		cl3.set(cl4).add(tempV);
		cl4.sub(tempV);


//		System.out.println("cl1: " + cl1 + ", cl2: " + cl2 + ", cl3: " + cl3 + ", cl4: " + cl4);
		for(int itt = 0; itt<10; itt++){
			cl1_2.set(Vec3.ZERO);
			cl2_2.set(Vec3.ZERO);
			cl3_2.set(Vec3.ZERO);
			cl4_2.set(Vec3.ZERO);
			nCl1 = 0;
			nCl2 = 0;
			nCl3 = 0;
			nCl4 = 0;
			for(Vec3 point : colorBlock.getBlockSubCenter()){
				float distance1 = cl1.distance(point);
				float distance2 = cl2.distance(point);
				float distance3 = cl3.distance(point);
				float distance4 = cl4.distance(point);
				if(distance1<=distance2){
					cl1_2.add(point);
					nCl1++;
				} else if(distance2 <= distance3){
					cl2_2.add(point);
					nCl2++;
				} else if(distance3 <= distance4){
					cl3_2.add(point);
					nCl3++;
				} else {
					cl4_2.add(point);
					nCl4++;
				}
			}

			cl1_2.scale(1f/Math.max(1, nCl1));
			cl2_2.scale(1f/Math.max(1, nCl2));
			cl3_2.scale(1f/Math.max(1, nCl3));
			cl4_2.scale(1f/Math.max(1, nCl4));
//			System.out.println("cl1: " + cl1_2 + ", cl2: " + cl2_2 + ", cl3: " + cl3_2 + ", cl4: " + cl4_2);
			if(cl1.distance(cl1_2)<=0.0001 && cl2.distance(cl2_2)<=0.0001 && cl3.distance(cl3_2)<=0.0001 && cl4.distance(cl4_2)<=0.0001){
				break;
			} else {
				if(nCl1 == 0 && nCl4 != 0){
					cl1_2.set(cl2_2);
					tempV.set(cl4_2).sub(cl1_2).scale(1f/3);
					cl2_2.add(tempV);
					cl3_2.set(cl4_2).sub(tempV);
				} else if(nCl1 == 0){
					cl1_2.set(cl2_2);
					cl4_2.set(cl3_2);
					tempV.set(cl4_2).sub(cl1_2).scale(1f/3);
					cl2_2.add(tempV);
					cl3_2.sub(tempV);
				} else if(nCl4 == 0){
					cl4_2.set(cl3_2);
					tempV.set(cl4_2).sub(cl1_2).scale(1f/3);
					cl2_2.set(cl1_2).add(tempV);
					cl3_2.sub(tempV);
				}
				cl1.set(cl1_2);
				cl2.set(cl2_2);
				cl3.set(cl3_2);
				cl4.set(cl4_2);
			}
		}
		cl1.set(cl1_2);
		cl2.set(cl2_2);
		cl3.set(cl3_2);
		cl4.set(cl4_2);
//		System.out.println("cl1: (" + nCl1 + ") " + cl1_2 + ", cl2: (" + nCl2 + ") " + cl2_2 + ", cl3: (" + nCl3 + ") " + cl3_2 + ", cl4: (" + nCl4 + ") " + cl4_2);
//		System.out.println("cl1: " + cl1 + ", cl2: " + cl2 + ", cl3: " + cl3 + ", cl4: " + cl4);
//		ray.set(tempV.set(boundMax).sub(centerP).normalize(), centerP);
//		ray.set(tempV.set(cl1).sub(cl4).normalize(), centerP);
		ray.set(tempV.set(cl1).add(cl2).sub(cl4).sub(cl3).normalize(), colorBlock.getCenterP());

	}


	public Vec3[] getPointsOnLine(Vec3[] points){
		Vec3[] linePoints = new Vec3[points.length];
//		System.out.println("color dir: " + ray.getDir() + " color offset: " + ray.getPoint());
		for(int i = 0; i<points.length; i++){
			plane.set(ray.getDir(), points[i]);
//			linePoints[i] = new Vec3(plane.getIntersectP(ray)).negate();
			linePoints[i] = new Vec3(ray.getPoint()).addScaled(ray.getDir(), plane.getIntersectBad(ray)).intify();
			float intersect = plane.getIntersectBad(ray);
//			System.out.println("\tloc color: " + plane.getPoint() + " p:" + points[i] + ", intersectF: " + intersect);
			tempV.set(ray.getPoint()).addScaled(ray.getDir(), intersect);
//			System.out.println("\tlinePoint: " + linePoints[i] + ", orgPoint: " + points[i] + ", alt: " + tempV);
		}
		return linePoints;
	}


	public Vec3[] getPointsOnLine(Vec3[] points, Ray ray){
		Vec3[] linePoints = new Vec3[points.length];
		for(int i = 0; i<points.length; i++){
			plane.set(ray.getDir(), points[i]);
			linePoints[i] = new Vec3(ray.getPoint()).addScaled(ray.getDir(), plane.getIntersectBad(ray)).intify();

//			float intersect = plane.getIntersect(ray);
//			tempV.set(ray.getPoint()).addScaled(ray.getDir(), intersect);
		}
		return linePoints;
	}

	Vec3[] colors = {new Vec3(), new Vec3(), new Vec3(), new Vec3()};
	Vec3 color0 = new Vec3();
	Vec3 color1 = new Vec3();
	Vec3 color2 = new Vec3();
	Vec3 color3 = new Vec3();
	float oneThird = 1f / 3f;
	float twoThird = 2f / 3f;


	public int[] getColorIndices(Vec3[] points, Vec3[] colors) {
		int[] colorInds = new int[points.length];
		for (int i = 0; i< points.length; i++) {
			float lastDist = 500;
//			System.out.println("point: " + points[i]);
			for (int cI = 0; cI< colors.length; cI++){
				float dist = points[i].distance(colors[cI]);
//				System.out.println("\tcolors[cI]: " + colors[cI] + ", dist: " + dist);
				if(dist < lastDist){
//					System.out.println("smaller!");
					colorInds[i] = cI;
					lastDist = dist;
				}
			}
		}

		return colorInds;
	}

	float[] tempC = new float[3];
	Vec3 tempV2 = new Vec3();
	public Vec3[] getColors(Vec3[] linePoints, Ray ray) {
		float lastMinDist = 500;
		float lastMaxDist = -1;
		tempV.set(ray.getPoint()).addScaled(ray.getDir(), 255);
		for (Vec3 linePoint : linePoints) {
//			scaleVec(tempV2.set(linePoint));
			tempV2.set(linePoint);
//			scaleVec();
			float dist = tempV.distance(tempV2);
			if(dist < lastMinDist){
				clampVec(colors[1].set(tempV2), colMax);
				lastMinDist = dist;
			}
			if(lastMaxDist < dist){
				clampVec(colors[0].set(tempV2), colMax);
				lastMaxDist = dist;
			}
		}

//		System.out.println("colors[1]: " + colors[1] + ", colors[0]: " + colors[0]);

		int c0 = DDSUtils.ColorTo565(colors[0].toArray(tempC));
		int c1 = DDSUtils.ColorTo565(colors[1].toArray(tempC));
		int colorAdj = 8;
		if(c0 < c1){
			tempV.set(colors[0]);
			colors[0].set(colors[1]);
			colors[1].set(tempV);
		} else if(c0 == c1) {
			tempV.set(colors[0]);
			if(colors[0].y<(colMax.y-(colorAdj*2)) && colorAdj < colors[1].y){
				colors[0].addScaled(Vec3.Y_AXIS, colorAdj);
				colors[1].addScaled(Vec3.Y_AXIS, -colorAdj);
			} else if(colors[0].y <= colorAdj*2) {
				colors[0].addScaled(Vec3.Y_AXIS, colorAdj*2);
			} else if(colorAdj*2 < colors[1].y){
				colors[1].addScaled(Vec3.Y_AXIS, -colorAdj*2);
			}
			c0 = DDSUtils.ColorTo565(colors[0].toArray(tempC));
			c1 = DDSUtils.ColorTo565(colors[1].toArray(tempC));
			if(c0 == c1){
				System.out.println("Same color!" + tempV + " -> " + colors[0] + " " + c0  + ", " + colors[1] + " " + c1);
			}
		}

		colors[2].set(colors[0]).lerp(colors[1], oneThird);
		colors[3].set(colors[0]).lerp(colors[1], twoThird);
		return colors;
	}

	public Vec3[] getColors(Vec3[] linePoints) {
		float lastMinDist = 500;
		float lastMaxDist = -1;
		tempV.set(ray.getPoint()).addScaled(ray.getDir(), 255);
		for (Vec3 linePoint : linePoints) {
//			scaleVec(tempV2.set(linePoint));
			tempV2.set(linePoint);
//			scaleVec();
			float dist = tempV.distance(tempV2);
			if(dist < lastMinDist){
				clampVec(colors[1].set(tempV2), colMax);
				lastMinDist = dist;
			}
			if(lastMaxDist < dist){
				clampVec(colors[0].set(tempV2), colMax);
				lastMaxDist = dist;
			}
		}

//		System.out.println("colors[1]: " + colors[1] + ", colors[0]: " + colors[0]);

		int c0 = DDSUtils.ColorTo565(colors[0].toArray(tempC));
		int c1 = DDSUtils.ColorTo565(colors[1].toArray(tempC));
		int colorAdj = 8;
		if(c0 < c1){
			tempV.set(colors[0]);
			colors[0].set(colors[1]);
			colors[1].set(tempV);
		} else if(c0 == c1) {
			tempV.set(colors[0]);
			if(colors[0].y<(colMax.y-(colorAdj*2)) && colorAdj < colors[1].y){
				colors[0].addScaled(Vec3.Y_AXIS, colorAdj);
				colors[1].addScaled(Vec3.Y_AXIS, -colorAdj);
			} else if(colors[0].y <= colorAdj*2) {
				colors[0].addScaled(Vec3.Y_AXIS, colorAdj*2);
			} else if(colorAdj*2 < colors[1].y){
				colors[1].addScaled(Vec3.Y_AXIS, -colorAdj*2);
			}
			c0 = DDSUtils.ColorTo565(colors[0].toArray(tempC));
			c1 = DDSUtils.ColorTo565(colors[1].toArray(tempC));
			if(c0 == c1){
				System.out.println("Same color!" + tempV + " -> " + colors[0] + " " + c0  + ", " + colors[1] + " " + c1);
			}
		}

		colors[2].set(colors[0]).lerp(colors[1], oneThird);
		colors[3].set(colors[0]).lerp(colors[1], twoThird);
		return colors;
	}


	public Vec3[] getColorsCluster(Vec3[] linePoints, Vec3[] points) {
		float lastMinDist = 500;
		float lastMaxDist = -1;
		tempV.set(ray.getPoint()).addScaled(ray.getDir(), 255);
		for (Vec3 linePoint : linePoints) {
//			scaleVec(tempV2.set(linePoint));
			tempV2.set(linePoint);
//			scaleVec();
			float dist = tempV.distance(tempV2);
			if(dist < lastMinDist){
				clampVec(colors[1].set(tempV2), colMax);
				lastMinDist = dist;
			}
			if(lastMaxDist < dist){
				clampVec(colors[0].set(tempV2), colMax);
				lastMaxDist = dist;
			}
		}

		System.out.println(Arrays.toString(points));
		cl1.set(colors[0]);
		cl4.set(colors[1]);
		float colorSpacing = cl1.distance(cl4)/3f;
		tempV.set(ray.getDir()).scale(colorSpacing);
		System.out.println("tempV: " + tempV);
		cl2.set(cl1).add(tempV);
		cl3.set(cl4).sub(tempV);


		int nCl1 = 0;
		int nCl2 = 0;
		int nCl3 = 0;
		int nCl4 = 0;
		System.out.println("cl1: " + cl1 + ", cl2: " + cl2 + ", cl3: " + cl3 + ", cl4: " + cl4);
		for(int itt = 0; itt<1; itt++){
			cl1_2.set(Vec3.ZERO);
			cl2_2.set(Vec3.ZERO);
			cl3_2.set(Vec3.ZERO);
			cl4_2.set(Vec3.ZERO);
			nCl1 = 0;
			nCl2 = 0;
			nCl3 = 0;
			nCl4 = 0;
//			for(Vec3 point : points){
//				float distance1 = cl1.distance(point);
//				float distance2 = cl2.distance(point);
//				float distance3 = cl3.distance(point);
//				float distance4 = cl4.distance(point);
//				if(distance1 <= distance4){
//					if(distance1<=distance2){
//						cl1_2.add(point);
//						nCl1++;
//					} else if(distance2 <= distance3){
//						cl2_2.add(point);
//						nCl2++;
//					} else {
//						cl3_2.add(point);
//						nCl3++;
//					}
//				} else {
//					if(distance2 <= distance3){
//						cl2_2.add(point);
//						nCl2++;
//					} else if(distance3 <= distance4){
//						cl3_2.add(point);
//						nCl3++;
//					} else {
//						cl4_2.add(point);
//						nCl4++;
//					}
//				}
//			}
//			for(Vec3 point : points){
			for(Vec3 point : linePoints){
				float distance1 = cl1.distance(point);
				float distance2 = cl2.distance(point);
				float distance3 = cl3.distance(point);
				float distance4 = cl4.distance(point);
				if(distance1<=distance2){
					cl1_2.add(point);
					nCl1++;
				} else if(distance2 <= distance3){
					cl2_2.add(point);
					nCl2++;
				} else if(distance3 <= distance4){
					cl3_2.add(point);
					nCl3++;
				} else {
					cl4_2.add(point);
					nCl4++;
				}
			}

//			for(Vec3 point : points){
//				float distance1 = cl1.distance(point);
//				float distance2 = cl2.distance(point);
//				float distance3 = cl3.distance(point);
//				float distance4 = cl4.distance(point);
//				if(distance1<=distance2 && distance1 < distance3 && distance1 < distance4){
//					cl1_2.add(point);
//					nCl1++;
//				} else if(distance2<distance1 && distance2 <= distance3 && distance2 < distance4){
//					cl2_2.add(point);
//					nCl2++;
//				} else if(distance3<distance1 && distance3<distance2 && distance3 <= distance4){
//					cl3_2.add(point);
//					nCl3++;
//				} else if(distance4<distance1 && distance4<distance2 && distance4 < distance3){
//					cl4_2.add(point);
//					nCl4++;
//				} else {
//					System.out.println("Fail!");
//					cl1_2.add(point);
//					cl2_2.add(point);
//					cl3_2.add(point);
//					cl4_2.add(point);
//				}
//			}
			cl1_2.scale(1f/Math.max(1, nCl1));
			cl2_2.scale(1f/Math.max(1, nCl2));
			cl3_2.scale(1f/Math.max(1, nCl3));
			cl4_2.scale(1f/Math.max(1, nCl4));
//			System.out.println("cl1: " + cl1_2 + ", cl2: " + cl2_2 + ", cl3: " + cl3_2 + ", cl4: " + cl4_2);
			System.out.println("cl1: (" + nCl1 + ") " + cl1_2 + ", cl2: (" + nCl2 + ") " + cl2_2 + ", cl3: (" + nCl3 + ") " + cl3_2 + ", cl4: (" + nCl4 + ") " + cl4_2);
//			if(cl1.distance(cl1_2)<=0.0001 && cl2.distance(cl2_2)<=0.0001 && cl3.distance(cl3_2)<=0.0001 && cl4.distance(cl4_2)<=0.0001){
//				break;
//			} else {
//				cl1.set(cl1_2);
//				cl2.set(cl2_2);
//				cl3.set(cl3_2);
//				cl4.set(cl4_2);
//			}
			if(10 < nCl1 && (nCl3 + nCl4)<3){

				clampVec(colors[1].set(cl2).add(cl3).scale(.5f), colMax);
			}
		}



//		cl1.set(cl1_2);
//		cl2.set(cl2_2);
//		cl3.set(cl3_2);
//		cl4.set(cl4_2);



		int c0 = DDSUtils.ColorTo565(colors[0].toArray(tempC));
		int c1 = DDSUtils.ColorTo565(colors[1].toArray(tempC));
		if(c0 < c1){
			tempV.set(colors[0]);
			colors[0].set(colors[1]);
			colors[1].set(tempV);
		} else if(c0 == c1) {
			System.out.println("Same color!" + colors[0]);
			if(colors[0].y<(colMax.y-1) && 0 < colors[1].y){
				colors[0].add(Vec3.Y_AXIS);
				colors[1].sub(Vec3.Y_AXIS);
			} else if(colors[0].y == 0) {
				colors[0].add(Vec3.Y_AXIS).add(Vec3.Y_AXIS);
			} else if(colors[1].y == (colMax.y-1)){
				colors[1].sub(Vec3.Y_AXIS).sub(Vec3.Y_AXIS);
			}
		}

		colors[2].set(colors[0]).lerp(colors[1], oneThird);
		colors[3].set(colors[0]).lerp(colors[1], twoThird);
		return colors;
	}

	Vec3 colMin = new Vec3();
	Vec3 colMax = new Vec3(255, 255, 255);
//	Vec3 colMax = new Vec3(63, 63, 63);
	private void clampVec(Vec3 vec, Vec3 colMax){
//		System.out.println(vec);
		vec.maximize(Vec3.ZERO).minimize(colMax);
//		System.out.println(vec);
	}

}
