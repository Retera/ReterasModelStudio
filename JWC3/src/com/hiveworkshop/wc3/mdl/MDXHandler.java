package com.hiveworkshop.wc3.mdl;

import java.io.File;
import java.io.IOException;

public class MDXHandler {
	public static File convert(File mdxFile) {
		try {
	        File fileMDL = new File(mdxFile.getPath().substring(0,mdxFile.getPath().lastIndexOf("."))+".mdl");
	        try {
				Process jones = Runtime.getRuntime().exec(new String [] {  "mdlx/converter.exe" , "\""+mdxFile.getPath()+"\"" ,"\""+fileMDL.getPath()+"\""} );//.waitFor();
				
				boolean keepGoing = true;
				long lastSize = 0;
				int goodTicks = 0;
				while( keepGoing ) {
					Thread.sleep(10);
					
					if( lastSize == fileMDL.length() ) {
						goodTicks++;
					}
					else {
						goodTicks = 0;
					}
					if( goodTicks > 3 && lastSize > 0 ) {
						keepGoing = false;
					}
					lastSize = fileMDL.length();
				}
				jones.destroy();
				
	        } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        //BufferedImage bi = ImageIO.read(fileTGA);//TargaReader.getImage(fileTGA.getPath());//ImageIO.read(fileTGA);
	        
	        //new TestMPQ().drawBlp(bi);//myBLP.getBufferedImage());
//	        fileMDL.deleteOnExit();
	        return fileMDL;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static File compile(File mdlFile) {
		try {
	        File fileMDX = new File(mdlFile.getPath().substring(0,mdlFile.getPath().lastIndexOf("."))+".mdx");
	        try {
				Process jones = Runtime.getRuntime().exec(new String [] {  "mdlx/converter.exe" , "\""+mdlFile.getPath()+"\"" ,"\""+fileMDX.getPath()+"\""} );//.waitFor();
				
				boolean keepGoing = true;
				long lastSize = 0;
				int goodTicks = 0;
				while( keepGoing ) {
					Thread.sleep(10);
					
					if( lastSize == fileMDX.length() ) {
						goodTicks++;
					}
					else {
						goodTicks = 0;
					}
					if( goodTicks > 3 && lastSize > 0 ) {
						keepGoing = false;
					}
					lastSize = fileMDX.length();
				}
				jones.destroy();
				
	        } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        //BufferedImage bi = ImageIO.read(fileTGA);//TargaReader.getImage(fileTGA.getPath());//ImageIO.read(fileTGA);
	        
	        //new TestMPQ().drawBlp(bi);//myBLP.getBufferedImage());
//	        fileMDX.deleteOnExit();
	        return fileMDX;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
