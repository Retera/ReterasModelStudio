package com.matrixeater.data;

import java.util.HashMap;
import java.io.*;

import javax.swing.JOptionPane;

public class SaveProfile implements Serializable {
	final static long serialVersionId = 5;
	String lastDirectory;
	static SaveProfile currentProfile;
	
	String wcDirectory = null;
	

	static boolean firstTime = true;
	public String getGameDirectory()
	{
		if( firstTime )
		{
			firstTime = false;
			testTargetFolder(wcDirectory);
		}
		return wcDirectory;
	}
	
	public void setGameDirectory(String dir)
	{
		wcDirectory = dir;
		firstTime = true;
		save();
	}
	
	public SaveProfile()
	{
		
	}
	public String getPath()
	{
		return lastDirectory;
	}
	public void setPath(String path)
	{
		lastDirectory = path;
		save();
	}
	
	public static SaveProfile get()
	{
		if( currentProfile == null )
		{
			try{
				String homeProfile = System.getProperty("user.home");
				File profileDir = new File(homeProfile+"\\AppData\\Roaming\\MatrixEater");
				File profileFile = new File(profileDir.getPath()+"\\user.profile");
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(profileFile));
				currentProfile = (SaveProfile)ois.readObject();
				ois.close();
			}
			catch (Exception e)
			{
				
			}
			if( currentProfile == null )
			{
				currentProfile = new SaveProfile();
			}
		}
		return currentProfile;
	}
	public static void main( String [] args )
	{
		SaveProfile prof = get();
//		DataGroup data = prof.getData("levels/levelControl.plc");
//		GameState state = data.getState(0);
//		System.out.println(state);
//		state.level = 1;
//		state.lives = 3;
//		state.saveName = "Test Save";
//		state.score = 0;
//		System.out.println(state);
		save();
	}
	public static void save()
	{
		if( currentProfile != null )
		{
			String homeProfile = System.getProperty("user.home");
			File profileDir = new File(homeProfile+"\\AppData\\Roaming\\MatrixEater");
			System.out.println(profileDir.mkdirs());
			System.out.println(profileDir);
			File profileFile = new File(profileDir.getPath()+"\\user.profile");
//			profileFile.delete();
			try {
				profileFile.createNewFile();
				OutputStream fos = new FileOutputStream(profileFile);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(get());
				oos.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	

	public static void testTargetFolder(String wcDirectory)
	{
		File temp = new File(wcDirectory+"mod_test_file.txt");
		boolean good = false;
		try {
			good = temp.createNewFile();
			temp.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if( !good )
		{
			JOptionPane.showMessageDialog(null, "You do not have permissions to access the chosen folder.\nYou should \"Run as Administrator\" on this program, or otherwise gain file permissions to the target folder, for the mod to work.\n\nBecause this folder is unusable, the MatrixEater will now request that you pick a different folder.","WARNING: Texture-Loader Won't Work", JOptionPane.WARNING_MESSAGE);
			requestNewWc3Directory();
		}
	}
	
	public static void requestNewWc3Directory()
	{
		String autoDir = autoWarcraftDirectory();
		
		DirectorySelector selector = new DirectorySelector(autoDir,"Welcome to the Matrix Eater! We need to make sure that the program can find your Warcraft III MPQ Archive files if the texture loader is going to work. Be advised that if any other program (Such as Magos's or another instance of the MatrixEater) is already open and already using the game's files, the MatrixEater will be unable to use them.");
		int x = JOptionPane.showConfirmDialog(null, selector, "Locating Warcraft III Directory", JOptionPane.OK_CANCEL_OPTION);
		if( x == JOptionPane.YES_OPTION )
		{
			String wcDirectory = selector.getDir();
			if( !(wcDirectory.endsWith("/") || wcDirectory.endsWith("\\")) )
			{
				wcDirectory = wcDirectory + "\\";
			}
			
			testTargetFolder(wcDirectory);
			
			get().setGameDirectory(wcDirectory);
		}
	}
	
	public static String getWarcraftDirectory()
	{
		if( get().getGameDirectory() == null )
		{
			requestNewWc3Directory();
		}
		return get().getGameDirectory();
	}

	public static String autoWarcraftDirectory()
	{
		String wcDirectory = WindowsRegistry.readRegistry("HKEY_CURRENT_USER\\Software\\Blizzard Entertainment\\Warcraft III","InstallPathX");
		if( wcDirectory == null )
		{
			wcDirectory = WindowsRegistry.readRegistry("HKEY_CURRENT_USER\\Software\\Blizzard Entertainment\\Warcraft III","InstallPathX");
		}
		if( wcDirectory == null )
		{
			wcDirectory = WindowsRegistry.readRegistry("HKEY_CURRENT_USER\\Software\\Classes\\VirtualStore\\MACHINE\\SOFTWARE\\Wow6432Node\\Blizzard Entertainment\\Warcraft III","InstallPath");
		}
		if( wcDirectory == null )
		{
			JOptionPane.showMessageDialog(null,"Error retrieving Warcraft III game directory.\nIs Warcraft III improperly installed on this machine?");
			wcDirectory = System.getProperty("user.home");
			if( wcDirectory == null )
			{
				wcDirectory = "C:\\";
			}
		}
		wcDirectory = wcDirectory.replace("\n","").replace("\r","");
		if( !(wcDirectory.endsWith("/") || wcDirectory.endsWith("\\")) )
		{
			//legacyFix(wcDirectory);
			wcDirectory = wcDirectory + "\\";
		}
		System.out.println("WC3: "+wcDirectory);
		return wcDirectory;
	}
}
