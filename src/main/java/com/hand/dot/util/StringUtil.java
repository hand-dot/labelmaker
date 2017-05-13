package com.hand.dot.util;

import org.apache.commons.lang3.text.StrBuilder;

public class StringUtil {
	/**
	 * 文字の間に半角スペースを挿入します。
	 * 例:1234567→1*2*3*4*5*6*7
	 * @param str
	 * @return
	 */
	public static String appendWithSeparators(String str){
        StrBuilder sb = new StrBuilder();
        String[] arr = str.replaceAll(" ", "").split("");
        sb.appendWithSeparators(arr, " ");
        return sb.toString().trim();
   }

	public static String removeHyphen(String str){
		return str.replaceAll("-", "");
	}
}
