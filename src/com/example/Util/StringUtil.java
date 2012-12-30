package com.example.Util;

public class StringUtil {

	
	public static String[] replaceR(String str){
		String[] s = {"",""};
		String strReturn ="";
		if (str.indexOf("\r\n") != -1) {
			s[1] = "\r\n";
			s[0] = str.replaceAll("\r\n", "");
			return s;
		} else if(str.indexOf("\n") != -1) {
			s[1] = "\n";
			s[0] = str.replaceAll("\n", "");
			return s;  
		} else if(str.indexOf("\r") != -1){
			s[1] = "\r";
			s[0] = str.replaceAll("\r", "");
		}
		s[0] = str;
		return s;
	}
	
	/**@function ½ØÈ¡iÖ®Ç°×Ö·û´®*/
	public static String subString(String s,int i){
		if(s.length()>i){
			return s.substring(0, i);
		}
		return s;
	}
}
