package com.hang.hotpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Utils {
	
	public static String GetNextVersion(String version){
		String[] strings = version.split("\\.");
		int num = 0;
		for (int i = 0; i < strings.length; i++) {
			num +=Integer.parseInt(strings[i])*Math.pow(10, strings.length-i -1);
		}

		StringBuilder sb = new StringBuilder(String.valueOf(++num));		
		String tempString="";
		for (int j = 0; j < sb.length()-1; j++) {
			tempString=tempString+sb.charAt(j)+".";
		}
		tempString=tempString+sb.charAt(sb.length()-1);

		/*String[] ssString = String.valueOf(++num).split("");
		String temp="";
		for (int i = 0; i < ssString.length; i++) {
			//解决split（""）分割出来第一个字符是空
			if (i!=ssString.length-1 && i!=0) {
				temp=temp+ssString[i]+".";
			}else {
				temp=temp+ssString[i];
			}
		}*/
		System.out.println(tempString);
		return tempString;
	}
	public static void copyDir(String oldPath, String newPath) {
        File file = new File(oldPath);
        //文件名称列表
        String[] filePath = file.list();
        
        if (!(new File(newPath)).exists()) {
            (new File(newPath)).mkdir();
        }
        
        for (int i = 0; i < filePath.length; i++) {
            if ((new File(oldPath + File.separator + filePath[i])).isDirectory()) {
                copyDir(oldPath  + File.separator  + filePath[i], newPath  + File.separator + filePath[i]);
            }
            
            if (new File(oldPath  + File.separator + filePath[i]).isFile()) {
                copyFile(oldPath + File.separator + filePath[i], newPath + File.separator + filePath[i]);
            }
            
        }
    }

	public static void copyFile(String oldPath, String newPath) {
		File oldFile = new File(oldPath);
		File file = new File(newPath);
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(oldFile);
			out = new FileOutputStream(file);
			System.out.println("正在复制文件"+oldPath);
			byte[] buffer = new byte[1024];
			int length = 0;
			while ((length = in.read(buffer)) != -1) {
				out.write(buffer,0,length);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (in!=null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out!=null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	public static long total = 0;
	public static void GetFloderSize(File file) {
		if (!file.exists()) {
			return;
		}
		if (file.isFile()) {
			total += file.length();
		}else {
			File[] files = file.listFiles();
			for (File file2 : files) {
				GetFloderSize(file2);
			}
		}
	}
	public static void WriteManifest(String Floder, String Manifest) {
		System.out.println("正在生成version.mainfest...");
		total = 0;
		File manifest = new File(Manifest);
		File floder = new File(Floder);
		if (!floder.exists()) {
			return;
		}
		if (manifest.exists()) {
			manifest.delete();
		}
		PrintWriter pw = null;
		GetFloderSize(floder);
		try {
			manifest.createNewFile();
			pw = new PrintWriter(new FileWriter(manifest,true));
			pw.println(total);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if (pw!=null) {
				pw.close();
			}
		}
		WriteManifestLine(floder,manifest);
	}
	public static void WriteManifestLine(File file,File manifest) {
		if (!file.exists() || file.getName().endsWith(MainClass.VERSIONNAME)) {
			return;
		}
		if (file.isFile()) {
			PrintWriter pw = null; 
			long size = file.length();
			String filename = file.getName();
			String pathString =GetRelativePath(file.getAbsolutePath());
			StringBuilder sb = new StringBuilder();
			sb.append(filename+",");
			sb.append(pathString+",");
			sb.append(size);
			try {
				pw = new PrintWriter(new FileWriter(manifest,true));
				pw.println(sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if (pw != null) {
					pw.close();
				}
			}
		}else {
			File[] files = file.listFiles();
			for (File file2 : files) {
				WriteManifestLine(file2,manifest);
			}
		}
	}
	public static String GetRelativePath(String path) {
		if (path.isEmpty()) {
			return "";
		}
		String string = path.replace('\\', '/');
		int start = string.lastIndexOf("/res/");
		int end = string.lastIndexOf("/");
		if (start < 0 || end < 0) {
			return string;
		}
		return string.substring(start, end);
	}
	  public static void delFolder(String folderPath) {  
		     try {  
		        delAllFile(folderPath); //删除完里面所有内容  
		        String filePath = folderPath;  
		        filePath = filePath.toString();  
		        java.io.File myFilePath = new java.io.File(filePath);  
		        myFilePath.delete(); //删除空文件夹  
		     } catch (Exception e) {  
		       e.printStackTrace();   
		     }  
		}  
	  public static boolean delAllFile(String path) {  
	       boolean flag = false;  
	       File file = new File(path);  
	       if (!file.exists()) {  
	         return flag;  
	       }  
	       if (!file.isDirectory()) {  
	         return flag;  
	       }  
	       String[] tempList = file.list();  
	       File temp = null;  
	       for (int i = 0; i < tempList.length; i++) {  
	          if (path.endsWith(File.separator)) {  
	             temp = new File(path + tempList[i]);  
	          } else {  
	              temp = new File(path + File.separator + tempList[i]);  
	          }  
	          if (temp.isFile()) {  
	             temp.delete();  
	          }  
	          if (temp.isDirectory()) {  
	             delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件  
	             delFolder(path + "/" + tempList[i]);//再删除空文件夹  
	             flag = true;  
	          }  
	       }  
	       return flag;  
	     }  
}
