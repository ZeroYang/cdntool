package com.hang.hotpack;

public class Version {
	int firSt;
	int second;
	int third;
	int jinzhi = 10;
	public Version(String verString,String jinzhi) {
		String[] strs = verString.split("\\.");
		firSt = Integer.parseInt(strs[0]);
		second = Integer.parseInt(strs[1]);
		third = Integer.parseInt(strs[2]);
		this.jinzhi = Integer.parseInt(jinzhi);
	}
	public String GetNextVersion() {
	    if (++third >= jinzhi) {
			second++;
			third = third - jinzhi;
		}
	    if (second >= jinzhi) {
	    	firSt++;
	    	second = second - jinzhi;
		}
		return firSt + "." + second + "." + third;
	}
}
