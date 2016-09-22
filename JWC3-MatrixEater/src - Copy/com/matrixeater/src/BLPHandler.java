package com.matrixeater.src;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class BLPHandler {
	
	public BLPHandler()
	{
		
	}

	public BufferedImage getGameTex(String filepath)
	{
		File blpFile = MPQHandler.get().getGameFile(filepath);
		File tga = convertBLPtoTGA(blpFile);
		
		try {
			return mpqlib.TargaReader.getImage(tga.getPath());//ImageIO.read(tga);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public BufferedImage getCustomTex(String filepath)
	{
		File blpFile = new File(filepath);
		File tga;
		try {
			tga = convertBLPtoTGA(blpFile, File.createTempFile("customtex", ".tga"));//+(int)(Math.random()*50)
			System.out.println(tga.getPath());
			//mpqlib.TestMPQ.draw(mpqlib.TargaReader.getImage(tga.getPath()));
			return mpqlib.TargaReader.getImage(tga.getPath());//ImageIO.read(tga);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public File convertBLPtoTGA(File blpFile)
	{
		try {
	        File fileTGA = new File(blpFile.getPath().substring(0,blpFile.getPath().lastIndexOf("."))+".tga");
	        try {
				Runtime.getRuntime().exec(new String [] {  "blplabcl/blplabcl.exe" , "\""+blpFile.getPath()+"\"" ,"\""+fileTGA.getPath()+"\"" ,"-type0", "-q256","-opt2"} ).waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        //BufferedImage bi = ImageIO.read(fileTGA);//TargaReader.getImage(fileTGA.getPath());//ImageIO.read(fileTGA);
	        
	        //new TestMPQ().drawBlp(bi);//myBLP.getBufferedImage());
	        fileTGA.deleteOnExit();
	        return fileTGA;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public File convertBLPtoTGA(File blpFile, File fileTGA)
	{
		try {
	        try {
				Runtime.getRuntime().exec(new String [] {  "blplabcl/blplabcl.exe" , "\""+blpFile.getPath()+"\"" ,"\""+fileTGA.getPath()+"\"" ,"-type0", "-q256","-opt2"} ).waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        //BufferedImage bi = ImageIO.read(fileTGA);//TargaReader.getImage(fileTGA.getPath());//ImageIO.read(fileTGA);
	        
	        //new TestMPQ().drawBlp(bi);//myBLP.getBufferedImage());
	        fileTGA.deleteOnExit();
	        return fileTGA;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static BLPHandler current;
	public static BLPHandler get()
	{
		if( current == null )
			current = new BLPHandler();
		return current;
	}
}
