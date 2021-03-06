package com.hand.dot.data.templates;

import com.hand.dot.util.StringUtil;

public class LetterPack extends PdfTemplate{

	//[届け先]郵便番号
	private String toPost;
	//[届け先]おところ
	private String toAddres;
	//[届け先]おなまえ
	private String toName;
	//[届け先]電話番号
	private String toTel;
	//[依頼主]郵便番号
	private String fromPost;
	//[依頼主]おところ
	private String fromAddres;
	//[依頼主]おなまえ
	private String fromName;
	//[依頼主]電話番号
	private String fromTel;
	//品名
	private String itemName;

	public String getToPost() {
		return toPost;
	}
	public void setToPost(String toPost) {
		this.toPost = StringUtil.appendWithSeparators(StringUtil.removeHyphen(toPost));
	}
	public String getToAddres() {
		return toAddres;
	}
	public void setToAddres(String toAddres) {
		this.toAddres = toAddres;
	}
	public String getToName() {
		return toName;
	}
	public void setToName(String toName) {
		this.toName = toName;
	}
	public String getToTel() {
		return toTel;
	}
	public void setToTel(String toTel) {
		if(toTel != null && toTel.equals("0")){
			this.toTel = "";
			return;
		}
		this.toTel = toTel;
	}
	public String getFromPost() {
		return fromPost;
	}
	public void setFromPost(String fromPost) {
		this.fromPost = StringUtil.appendWithSeparators(StringUtil.removeHyphen(fromPost));
	}
	public String getFromAddres() {
		return fromAddres;
	}
	public void setFromAddres(String fromAddres) {
		this.fromAddres = fromAddres;
	}
	public String getFromName() {
		return fromName;
	}
	public void setFromName(String fromName) {
		this.fromName = fromName;
	}
	public String getFromTel() {
		return fromTel;
	}
	public void setFromTel(String fromTel) {
		this.fromTel = fromTel;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

}
