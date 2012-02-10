package jp.peisun.wakeuptimer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;


/*
 * 各種設定をするActivity.
 * 
 */
public class WakeupTimerActivity extends PreferenceActivity  {
    /** Called when the activity is first created. */
	private final String TAG = "WakeupTimerActivity";
	public static final String ACTION_CONFIG = "jp.peisun.wakeupTimer.intent.config";
	private static final int REVIEW_REPEAT = 2; /* preview時の計算回数 */
	
	
	public ConfigData mConfig = null;


	private Preference.OnPreferenceClickListener  onPreferenceClickListener_wakeupTime =
		new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO 自動生成されたメソッド・スタブ
				selectTimeDialog(preference);
				return true;
			}
	};
	private Preference.OnPreferenceChangeListener  onPreferenceChangeListener_wakeupTime =
		new OnPreferenceChangeListener(){
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue){
			int layoutId = preference.getLayoutResource();
	        View layoutview = (View)findViewById(layoutId);
	        String time = String.format("%02d:%02d", mConfig.hour,mConfig.minute);
	        TextView textView = (TextView)layoutview.findViewById(R.id.PreferenceValuetextView);
	        textView.setText(time);
			return true;
		}
	};
	private Preference.OnPreferenceChangeListener  onPreferenceChangeListener_ringtone =
		new OnPreferenceChangeListener(){
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue){
			selectRingtoneDialog(preference,newValue);
			return true;
		}
	};
	private Preference.OnPreferenceChangeListener  onPreferenceChangeListener_vibration =
		new OnPreferenceChangeListener(){
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue){
			checkVibration(preference,newValue);
	        // 変更を適用するために true を返す  
	        return true;
		}
	};
	private Preference.OnPreferenceChangeListener  onPreferenceChangeListener_snooze =
		new OnPreferenceChangeListener(){
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue){
			selectSnoozeDialog(preference,newValue);
	        // 変更を適用するために true を返す  
	        return true;
		}
	};
	private Preference.OnPreferenceChangeListener  onPreferenceChangeListener_repeat =
		new OnPreferenceChangeListener(){
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue){
			selectRepeatDialog(preference,newValue);
	        // 変更を適用するために true を返す  
	        return true;
		}
	};
	private Preference.OnPreferenceChangeListener  onPreferenceChangeListener_limittime =
		new OnPreferenceChangeListener(){
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue){
			selectLimitTimeDialog(preference,newValue);
	        // 変更を適用するために true を返す  
	        return true;
		}
	};
	private Preference.OnPreferenceClickListener  onPreferenceClickListener_preview =
		new OnPreferenceClickListener(){
		@Override
		public boolean onPreferenceClick(Preference preference) {
			// TODO 自動生成されたメソッド・スタブ
			selectCalcPreview(preference);
			return false;
		}
	};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.main);
        
        // 設定ファイルの
        readConfigData();
        // 起床時間
        // キーを基に、リスト設定のインスタンスを取得する  
        CharSequence cs = getText(R.string.preference_wakeupTime);  
        Preference pref = (Preference)findPreference(cs);   
        // リスナーを設定する  
        pref.setOnPreferenceClickListener(onPreferenceClickListener_wakeupTime);
        pref.setOnPreferenceChangeListener(onPreferenceChangeListener_wakeupTime);
        
        
        // アラーム音
        cs = getText(R.string.preference_ringtone);  
        pref = (Preference)findPreference(cs);
        // リスナーを設定する  
        pref.setOnPreferenceChangeListener(onPreferenceChangeListener_ringtone);
        
        
        // バイブレーション
        cs = getText(R.string.preference_vibration);  
        CheckBoxPreference cbp = (CheckBoxPreference)findPreference(cs);  
        cbp.setChecked(mConfig.mVabration);
        // リスナーを設定する  
        cbp.setOnPreferenceChangeListener(onPreferenceChangeListener_vibration);  
        
        // スヌーズ	
        cs = getText(R.string.preference_snooze);  
        ListPreference lp = (ListPreference)findPreference(cs);
        lp.setDefaultValue(mConfig.mSnoozTime);
        // リスナーを設定する  
        lp.setOnPreferenceChangeListener(onPreferenceChangeListener_snooze);
        
        // 設問数
        cs = getText(R.string.preference_repeat);  
        lp = (ListPreference)findPreference(cs);
        // リスナーを設定する  
        lp.setOnPreferenceChangeListener(onPreferenceChangeListener_repeat);
        
        // 制限時間
        cs = getText(R.string.preference_limittime);  
        lp = (ListPreference)findPreference(cs);
        // リスナーを設定する  
        lp.setOnPreferenceChangeListener(onPreferenceChangeListener_limittime);
        
        // プレビュー
        cs = getText(R.string.preference_preview);  
        pref = (Preference)findPreference(cs);   
        // リスナーを設定する  
        pref.setOnPreferenceClickListener(onPreferenceClickListener_preview);
        
		// Serviceがbootしていない場合があるので、パラメータを設定してサービスを起動しておく
		sendSetConfigIntent(mConfig); 
        
    }
    @Override
	protected void onStart() {
		// TODO 自動生成されたメソッド・スタブ
//    	CharSequence cs = getText(R.string.preference_wakeupTime);  
//        Preference pref = (Preference)findPreference(cs);

		super.onStart();

		
	}
	@Override
	protected void onResume(){
		super.onResume();
	}

	private void readConfigData()  {
		// TODO 自動生成されたメソッド・スタブ
		InputStream is = null;

		String filepath = this.getFilesDir().getAbsolutePath() + "/" +  FileConfig.xmlfile;  
		File file = new File(filepath);
		
		try {
			is = new FileInputStream(file);
		
			mConfig = FileConfig.readConfig(is);
			is.close();
			if(mConfig == null){
				mConfig = setDefaultValue();
			}
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			mConfig = setDefaultValue();
		} catch (IOException e){
			e.printStackTrace();
		}
		sendSetConfigIntent(mConfig);
		
		
	}

	private ConfigData setDefaultValue(){
		ConfigData config = new ConfigData();
		
		config.hour = Integer.parseInt(getString(R.string.wakeupHourDefault));
		config.minute = Integer.parseInt(getString(R.string.wakeupMinuteDefault));
		
		config.mSnoozTime = Long.parseLong(getString(R.string.snoozeTimeDefault));
		
		config.mLimitTime = Long.parseLong(getString(R.string.limittimeDefault));
				
		config.mCalcRepeat = Integer.parseInt(getString(R.string.repeatDefaultValue));
		
//		config.mRingtonePath = Integer.parseInt(getString(R.string.selectAlarmDefaultIndex));
		
		config.mVabration = Boolean.parseBoolean(getString(R.string.vibrationDefault));
		return config;
	}

    private void checkVibration(Preference preference, Object newValue){
		String summary;  
        if (((Boolean)newValue).booleanValue()) {  
            summary = getString(R.string.summary_vibration_true);  
        } else {  
            summary = getString(R.string.summary_vibration_false);    
        }  
        // 要約を変更する  
        ((CheckBoxPreference)preference).setSummary(summary); 
    }
    private void selectRingtoneDialog(Preference preference, Object newValue){
    	String url = (String)newValue;  
        Uri uri;  
        Ringtone ringtone;  
        if ("".equals(url)) {  
            preference.setSummary("サイレント");  
        } else {  
            uri = Uri.parse(url);  
            ringtone = RingtoneManager.getRingtone(this, uri);  
            preference.setSummary(ringtone.getTitle(this)); 
            mConfig.mRingtonePath = uri.toString();
            sendSetConfigIntent(mConfig);
        } 
    }
	private void selectSnoozeDialog(Preference preference, Object newValue){
		ListPreference listpref =(ListPreference)preference;  
        String summary = listpref.getEntry().toString();
        preference.setSummary(summary); 
        mConfig.mSnoozTime = Long.parseLong(newValue.toString());
        sendSetConfigIntent(mConfig);
	}

	private void selectLimitTimeDialog(Preference preference, Object newValue){

		ListPreference listpref = (ListPreference)preference;
		String summary =   listpref.getEntry().toString();
		listpref.setSummary(summary);
		mConfig.mLimitTime = Long.parseLong(newValue.toString());
		sendSetConfigIntent(mConfig);
		
	}
	private void selectRepeatDialog(Preference preference, Object newValue){
		ListPreference listpref = (ListPreference)preference;
		String summary = listpref.getEntry().toString();
		listpref.setSummary(summary);
		mConfig.mCalcRepeat = Integer.parseInt(newValue.toString()); 
		sendSetConfigIntent(mConfig);
		
	}
    public void selectTimeDialog(Preference pref){
    	Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int layoutId = pref.getLayoutResource();
        final View layoutview = (View)findViewById(layoutId);
    	
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
        		new TimePickerDialog.OnTimeSetListener() {
			@Override
        	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				String time = String.format("%02d:%02d", hourOfDay,minute);
				TextView textView = (TextView)layoutview.findViewById(R.id.PreferenceValuetextView);
				textView.setText(time);
				mConfig.hour = hourOfDay;
				mConfig.minute = minute;
				sendSetConfigIntent(mConfig);
        	}
        }, hour, minute, true);

        timePickerDialog.show();
    	
    }


    private void sendSetConfigIntent(ConfigData config){
    	
    	Intent intent = new Intent(timerService.SET_CONFIG);
    	
    	
    	intent.putExtra(timerService.SET_HOUR, config.hour);
    	intent.putExtra(timerService.SET_MINUTE, config.minute);
    	intent.putExtra(timerService.SNOOZE, config.mSnoozTime);
    	intent.putExtra(CalcActivity.REPEAT, config.mCalcRepeat);
    	intent.putExtra(timerService.VIBRATION, config.mVabration);
    	intent.putExtra(CalcActivity.LIMITTIME, config.mLimitTime);
    	intent.putExtra(timerService.SOUND_URI, config.mRingtonePath);
    	startService(intent);
    }

    private void selectCalcPreview(Preference preference){
    	try {
    		
    		Intent intent = new Intent(getApplicationContext(),CalcActivity.class);
    		intent.setAction("jp.peisun.wakeupTimer.intent.calcActivity");
    		intent.putExtra(CalcActivity.PREVIEW, true);
    		intent.putExtra(CalcActivity.REPEAT, REVIEW_REPEAT);
    		intent.putExtra(CalcActivity.LIMITTIME, mConfig.mLimitTime);
    		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	startActivity(intent);
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	
    }

}