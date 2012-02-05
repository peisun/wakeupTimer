package jp.peisun.wakeuptimer;

import java.lang.reflect.Method;

/*
 * メニューリスト
 */
public class MenuList {
	public static final String TEXT = "text";
	public static final String CHECK = "check";
	public static final int TYPE_TEXT= 1;
	public static final int TYPE_CHECK = 2;
	private String mMenuText;
	private String mMenuValue;
	private int mPosition;
	private int mType;
	private String mDetails;
	private boolean mCheck;
	public Method mInvoke;
	public String getMenuText(){ return mMenuText; }
	public String getMenuValue(){ return mMenuValue; }
	public int getMenuType(){ return mType; }
	public String getMenuDetails(){ return mDetails; }
	public int getMenuPosition(){ return mPosition; }
	public boolean getMenuCheck(){ return mCheck; }
	public void setMenuCheck(boolean check){ mCheck = check; }
	public void setMenuText(String text){ mMenuText = text; }
	public void setMenuPosition(String value){ mPosition = Integer.parseInt(value); }
	public void setMenuDetails(String value){ mDetails = value; }
	public void setMenuValue(String text,String value){
		if(text != null){
			if(text.equals(TEXT)){
				mType = TYPE_TEXT;
				mMenuValue = value;
			}
			else if(text.equals(CHECK)){
				mType = TYPE_CHECK;
				mCheck = Boolean.parseBoolean(value);
			}
		}
	}
	
}
