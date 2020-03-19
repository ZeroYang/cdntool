package com.hang.hotpack;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MainClass {

	/**
	 * @param args
	 */
	public static final String VERSIONNAME = "version.manifest";
	public static void main(String[] args) {
		String ismerge = "0";//0为不合并文件夹
		String server = "192.168.1.211";
		String port = "22";
		String username = "root";
		String password = "zwwxtest";
		String cdnpath = "android_tw_google";
		String uploadfloder = "res_201810301951";
		String isUpload = "1"; //1:上传到服务器  0:不上传到服务器
		String jinzhi = "10";
	//	String mergeFloder = "2.3.6 2.3.7";
		String nowpath = System.getProperty("user.dir");
		ismerge = args[0];
		if (ismerge.equals("1") && args.length > 1) {
			String mergeFloder = args[1];
			if (mergeFloder.isEmpty()) {
				return;
			}
			MergeFloderAndMainfest(mergeFloder);
			return;
		}
		
		server = args[1];
		port = args[2];
		username = args[3];
		password = args[4];
		cdnpath = args[5];
		uploadfloder = args[6];
		isUpload = args[7]; 
		if (args.length > 8) {
			jinzhi = args[8];
		}
		
		String openshellcom = "cd web"+"/"+cdnpath;
		String uplodad = nowpath+File.separator+uploadfloder;
		
		
		//判断传入的资源文件是否存在 若不存在则退出
		File uploadfile = new File(uplodad);
		if (!uploadfile.exists()) {
			System.out.println("Error,未找到文件夹");
			return;
		} 
		Ftp mFtp=null;
		try {
			String floderName = "";
			if (Integer.parseInt(isUpload) > 0) {
				//获得服务器连接
				mFtp = Ftp.getSftpUtil(server, Integer.parseInt(port), username, password);
				//获得服务器当前版本号
				String curentVersion =  mFtp.exceCommond(openshellcom+";cat version.txt");
				if(curentVersion.isEmpty()){
					System.out.println("没有找到version.txt,或者version.txt不符合规范");
					return;
				}
				//获得下个版本号
				Version version =  new Version(curentVersion, jinzhi);
				floderName = version.GetNextVersion();
			}else {
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
				floderName = "Version"+df.format(new Date());;
			}
			//res文件路径
			String resPath = nowpath+File.separator+floderName+File.separator+"res";
			//资源包路径
			String VerPath = nowpath+File.separator+ floderName;
			//本地创建下个版本号文件夹，并将资源文件更名为res 然后复制进去
			File Versionfile = new File(nowpath + File.separator +floderName);
			if (Versionfile.exists()) {
				Utils.delFolder(nowpath + File.separator +floderName);
			}
			File file = new File(resPath);
			file.mkdirs();
			Utils.copyDir(uplodad, resPath);
			//删除md5文件
			File md5File = new File(resPath + File.separator + "md5.txt");
			if (md5File.exists()) {
				System.out.println("删除md5.txt");
				md5File.delete();
			}
			//写入version.manifest文件
			Utils.WriteManifest(resPath, VerPath + File.separator + VERSIONNAME); 
			if (Integer.parseInt(isUpload) > 0) {
				//上传至服务器
				mFtp.upLoadFile(VerPath, "web/"+cdnpath);
				//修改服务器版本号
				mFtp.exceCommond(openshellcom+";echo "+floderName+" > version.txt"); 
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (mFtp != null) {
				mFtp.closeChannel();
			}
			
		} 

	}
    public static Map<Integer, String> sortMapByKey(Map<Integer, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<Integer, String> sortMap = new TreeMap<Integer, String>(
                new MapKeyComparator());

        sortMap.putAll(map);

        return sortMap;
    }
    static void MergeFloderAndMainfest(String floderString){
		String nowpath = System.getProperty("user.dir");
		Map<Integer, String> mmMap = new HashMap<Integer, String>();
    	int temp = 0;
		String mergeFloder = floderString;
		String[] floders = mergeFloder.split(" ");
		for (String string : floders) {
			File ssfile = new File(nowpath + File.separator +string);
			if (!ssfile.exists()) {
				System.out.println(string +"不存在，合并失败");
				return;
			}
			String value = string.replace(".", "");
			int key;
			try {
				key = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				key = 100000 + temp;
				temp++;
			}
			if (mmMap.containsValue(value)) {
				System.out.println("有相同名字文件夹，合并失败");
				return;
			}
			mmMap.put(key, string);
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
		String floderName = "Merge"+df.format(new Date());
		String mergePath = nowpath + File.separator + floderName;
		File file = new File(mergePath);
		if (file.exists()) {
			Utils.delFolder(mergePath);
		}
		file.mkdirs();
		Map<Integer, String> resMap = sortMapByKey(mmMap);
		for (Map.Entry<Integer, String> value : resMap.entrySet()) {
			Utils.copyDir(nowpath + File.separator + value.getValue(), nowpath + File.separator + floderName);
		}
		Utils.WriteManifest(mergePath + File.separator + "res", mergePath + File.separator + VERSIONNAME); 
    }

}
class MapKeyComparator implements Comparator<Integer>{

    @Override
    public int compare(Integer str1, Integer str2) {
        
        return str1.compareTo(str2);
    }
    
}

