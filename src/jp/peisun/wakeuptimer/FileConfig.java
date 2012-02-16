package jp.peisun.wakeuptimer;

import java.io.InputStream;
import java.io.OutputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

public class FileConfig {
	private static final String TAG = "FileConfig";
	public static final String tag_file = "config";
	public static final String tag_waketime = "wakeuptime";
	public static final String tag_alarmon = "alarmon";
	public static final String tag_ringtonepath = "ringtonepath";
	public static final String tag_vibration = "vibration";
	public static final String tag_repeat = "repeat";
	public static final String tag_limittime = "limittime";
	public static final String tag_sznooe = "sznooe";

	public static final String xmlfile = "config.xml";

	public static void writeConfig(OutputStream out,ConfigData config){
		XmlSerializer sl = null;
		
	
		try {
			
			//out = new FileOutputStream(,Context.MODE_PRIVATE);
			sl = Xml.newSerializer();
			
			sl.setOutput( out, "UTF-8" );
			sl.startDocument("UTF-8", false);    // ヘッダー
			sl.text("\n");
			sl.startTag("", tag_file);
			sl.flush();


			sl.startTag("", tag_alarmon);
			sl.text(Boolean.toString(config.mAlarmOn));
			sl.endTag("", tag_alarmon);
			sl.text("\n");
			
			sl.startTag("", tag_waketime);
			sl.text(String.format("%02d:%02d", config.hour,config.minute));
			sl.endTag("", tag_waketime);
			sl.text("\n");


			sl.startTag("", tag_ringtonepath);
			sl.text(config.mRingtonePath);
			sl.endTag("", tag_ringtonepath);
			sl.text("\n");

			sl.startTag("", tag_vibration);
			sl.text(Boolean.toString(config.mVabration));
			sl.endTag("", tag_vibration);
			sl.text("\n");

			sl.startTag("", tag_sznooe);
			sl.text(Long.toString(config.mSnoozTime));
			sl.endTag("", tag_sznooe);
			sl.text("\n");

			sl.startTag("", tag_repeat);
			sl.text(Integer.toString(config.mCalcRepeat));
			sl.endTag("", tag_repeat);
			sl.text("\n");

			sl.startTag("", tag_limittime);
			sl.text(Long.toString(config.mLimitTime));
			sl.endTag("", tag_limittime);
			sl.text("\n");



			sl.flush();

			sl.endTag("", tag_file);

			sl.endDocument();
			sl.flush();
			out.close();
		}

		catch (Exception e) {
			e.printStackTrace();
		}

	}
	public static ConfigData readConfig(InputStream is){
		XmlPullParser xmlpp = null;
		int eventType = -1;
		String start_tag = null;
		String element = null;
		ConfigData config = null;

		try {
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			xmlpp = factory.newPullParser();
			xmlpp.setInput(is, "UTF-8");
			eventType = xmlpp.getEventType();
			config = new ConfigData();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_DOCUMENT) {
				}
				else if(eventType == XmlPullParser.START_TAG) {
					start_tag = (String)xmlpp.getName();
				}
				else if(eventType == XmlPullParser.END_TAG) {
					String end_tag = (String)xmlpp.getName();
					if(end_tag.equals(start_tag) == true){
						if(end_tag.equals(tag_waketime)){
							String[] split = element.split(":");
							int mSetHour = Integer.parseInt(split[0]);
							int mSetMinute = Integer.parseInt(split[1]);
							config.hour =mSetHour;
							config.minute = mSetMinute;
						}
						else if(end_tag.equals(tag_alarmon)){
							config.mAlarmOn = Boolean.parseBoolean(element);
							Log.d(TAG,"FileConfig.read "+ config.mAlarmOn);
						}
						else if(end_tag.equals(tag_ringtonepath)){
							config.mRingtonePath = element;
						}
						else if(end_tag.equals(tag_vibration)){
							config.mVabration = Boolean.parseBoolean(element);
						}
						else if(end_tag.equals(tag_repeat)){
							config.mCalcRepeat = Integer.parseInt(element);
						}
						else if(end_tag.equals(tag_limittime)){
							config.mLimitTime = Long.parseLong(element);
						}
						else if(end_tag.equals(tag_sznooe)){
							config.mSnoozTime = Long.parseLong(element);
						}
						element = null;
						start_tag = null;
					}
				}
				else if(eventType == XmlPullParser.TEXT) {
					element = xmlpp.getText();
					if(start_tag != null){
					Log.d(TAG,"ReadFile " + start_tag + " " + element);
					}
				}
				eventType = xmlpp.next();
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return config;
	}
}

