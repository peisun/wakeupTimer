package jp.peisun.wakeuptimer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

/*
 * 各種設定をするActivity.
 * 
 */
public class WakeupTimerActivity extends Activity implements OnItemClickListener {
    /** Called when the activity is first created. */
	private final String TAG = "WakeupTimerActivity";
	public static final String ACTION_CONFIG = "jp.peisun.wakeupTimer.intent.config";
	private static final int REVIEW_REPEAT = 2; /* preview時の計算回数 */
	private ArrayList<MenuList> mMenuItem = new ArrayList<MenuList>();
	private HashMap<String,Integer> mMenuMap = new HashMap<String,Integer>();
	private ListView mListView = null;
	
	public ConfigData mConfig = null;
	
	private int SnoozTimeListIndex = 0;
	private int LimitTimeIndex = 0;
	private int mCalcRepeatIndex = 0;
	
	private RingtoneManager mRingtoneManager = null;
	private final Intent intentActionRingTonePicker = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);  
	private final int RINGTONE_REQUESETCODE = 9999;
//	private CheckHandler mCheckHandler = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mRingtoneManager = new RingtoneManager(this);
        mRingtoneManager.setType(timerService.RINGTON_STREAMTYPE);
        
        readConfigData();
		makeMenuList(mConfig);
		// Serviceがbootしていない場合があるので、パラメータを設定してサービスを起動しておく
		sendSetConfigIntent(mConfig); 
		
        mListView = (ListView)findViewById(R.id.menulistView);
        mListView.setOnItemClickListener(this);
        
        

//        mCheckHandler = new CheckHandler(mConfig);
        
		mListView.setAdapter(new MenuAdapter(this,mMenuItem));
        
    }
    @Override
	protected void onStart() {
		// TODO 自動生成されたメソッド・スタブ
		super.onStart();

		
	}
	@Override
	protected void onResume(){
		super.onResume();
	}
//	class CheckHandler extends Handler {
//		public static final int ALARM = 1;
//		public static final int VABRATION = 2;
//		public static final int SETTIME = 3;
//		public static final int SNOOZETIME = 4;
//		public static final int LIMITTIME = 5;
//		public static final int REPEAT = 6;
//		private ConfigData config;
//		public CheckHandler(ConfigData Config){
//			config = Config;
//			
//		}
//		@Override
//        public void handleMessage(Message msg) {
//			Boolean chk;
//			
//			switch(msg.what){
//			case ALARM:
//				chk = (Boolean)msg.obj;
//				config.mAlarm = chk.booleanValue();
//				sendSetConfigIntent(config);
//				break;
//			case VABRATION:
//				chk = (Boolean)msg.obj;
//				config.mVabration = chk.booleanValue();
//				break;
//			case SETTIME:
//				config.hour = msg.arg1;
//				config.minute = msg.arg2;
//				break;
//			case SNOOZETIME:
//				Long s = (Long)msg.obj;
//				config.mSnoozTime = s.longValue();
//				break;
//			case LIMITTIME:
//				Long l = (Long)msg.obj;
//				config.mLimitTime = l.longValue();
//			case REPEAT:
//				config.mCalcRepeat = msg.arg1;
//			default:
//				break;
//			
//			}
//			sendSetConfigIntent(config);
//		}
//		
//	}
//	public void msgChecked(int what,boolean c){
//		Message msg = new Message();
//		msg.what = what;
//		msg.obj = (Object)new Boolean(c);
//		mCheckHandler.sendMessage(msg); 
//	}
//	public void msgSetTime(int h,int m){
//		Message msg = new Message();
//		msg.what = CheckHandler.SETTIME;
//		msg.arg1 = h;
//		msg.arg2 = m;
//		mCheckHandler.sendMessage(msg);
//	}
//	public void msgLong(int what,long l){
//		Message msg = new Message();
//		msg.what = what;
//		msg.obj = (Object)new Long(l);
//		mCheckHandler.sendMessage(msg);
//	}
//	public void msgInt(int what,int i){
//		Message msg = new Message();
//		msg.what = what;
//		msg.arg1 = i;
//		mCheckHandler.sendMessage(msg);
//	}

	private void writeFile(ConfigData config,File file){
		OutputStream os;
		try {
			
			os = new FileOutputStream(file);
			//os = openFileOutput(FileConfig.xmlfile,MODE_PRIVATE);
			FileConfig.writeConfig(os,config);
			os.close();
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		
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
			if(mConfig != null){
				//setMenuItem(mConfig);
			}
			else {
				mConfig = setDefaultValue();
			}
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			//e.printStackTrace();
			
			mConfig = setDefaultValue();
			writeFile(mConfig,file);
		} catch (IOException e){
			e.printStackTrace();
		}
		
		
	}

	private ConfigData setDefaultValue(){
		ConfigData config = new ConfigData();
		
		config.hour = Integer.parseInt(getString(R.string.wakeupHourDefault));
		config.minute = Integer.parseInt(getString(R.string.wakeupMinuteDefault));
		
		config.mSnoozTime = Long.parseLong(getString(R.string.snoozeTimeDefault));
		SnoozTimeListIndex = Integer.parseInt(getString(R.string.snoozeTimeDefaultIndex));
		
		config.mLimitTime = Long.parseLong(getString(R.string.limittimeDefault));
		LimitTimeIndex = Integer.parseInt(getString(R.string.limittimeDefaultIndex));
				
		config.mCalcRepeat = Integer.parseInt(getString(R.string.calcRepeatDefault));
		mCalcRepeatIndex = Integer.parseInt(getString(R.string.calcRepeatDefaultIndex));
		
		config.mRingtonePosition = Integer.parseInt(getString(R.string.selectAlarmDefaultIndex));
		config.mRingtonePath = mRingtoneManager.getRingtoneUri(config.mRingtonePosition).toString();
		
		config.mVabration = Boolean.parseBoolean(getString(R.string.vibrationDefault));
		return config;
	}
    @Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long i) {
		/*
		 * ListViewの上からの順はmMenuItemにaddした順で決まるので
		 * addした位置とメニューのテキストともにHashMapに入れておき、
		 * ViewのMenutextViewもしくはchkMenutextViewの文字列をkeyに
		 * addした位置を取得し、それと一致したらファンクションをコールする
		 */   	
    	
    	if(position == mMenuMap.get(getString(R.string.menuWakeup))){
    		selectTimeDialog();
    	}
    	else if(position == mMenuMap.get(getString(R.string.menuPreview))){
    		selectCalcPreview();
    	}
    	else if(position == mMenuMap.get(getString(R.string.menuSnooze))){
    		selectSnoozeDialog();
    	}
    	else if(position == mMenuMap.get(getString(R.string.menuRepeat))){
    		selectRepeatDialog();
    	}
    	else if(position == mMenuMap.get(getString(R.string.menuLimittime))){
    		selectLimitTimeDialog();
    	}
    	else if(position == mMenuMap.get(getString(R.string.menuAarlm))){
    		selectRingtoneDialog();
    		
//    		mConfig.mAlarm = !mConfig.mAlarm;
//    		mMenuItem.get(position).setMenuCheck(mConfig.mAlarm );
//    		CheckBox buttonView = (CheckBox)view.findViewById(R.id.checkBox1);
//    		buttonView.setChecked(mConfig.mAlarm );
//    		sendSetConfigIntent(mConfig);
//    		Log.d(TAG,"mAlarmOn "+ mMenuItem.get(2).getMenuCheck());
    	}
    	else if(position == mMenuMap.get(getString(R.string.menuVibration))){
    		mConfig.mVabration = !mConfig.mVabration;
    		mMenuItem.get(position).setMenuCheck(mConfig.mVabration);
    		CheckBox buttonView = (CheckBox)view.findViewById(R.id.checkBox1);
    		buttonView.setChecked(mConfig.mVabration);
    		sendSetConfigIntent(mConfig);
    		Log.d(TAG,"mVabrationOn "+mMenuItem.get(3).getMenuCheck());

    	}
    		
    	
    	/* これは乱暴だな
    	mListView.setAdapter(new MenuAdapter(this,mMenuItem));
    	*/
	}
	
    private int findIndexLongValue(long t,int id){
    	String tt = Long.toString(t);
		String resouce = getString(id);
		int p = 0;
		final String[] value = resouce.split(",");
		for(int i=0;i<value.length;i++){
			if(value[i].equals(tt)){
				p= i;
				break;
			}
		}
		return p;
    }
    private int findIndexLongValue(int t,int id){
    	String tt = Integer.toString(t);
		String resouce = getString(id);
		int p = 0;
		final String[] value = resouce.split(",");
		for(int i=0;i<value.length;i++){
			if(value[i].equals(tt)){
				p= i;
				break;
			}
		}
		return p;
    }
    private String findString(int idx,int id){
    	String resouce = getString(id);
    	String[] split = resouce.split(",");
		return split[idx];
    }
    private Uri findRingtoneUri(int position){
    	Uri uri = mRingtoneManager.getRingtoneUri(position);
    	return uri; 
    }
    private String findRingtoneString(int position){
    	String name;
    	
    		Cursor cursor = mRingtoneManager.getCursor();
    		cursor.moveToPosition(position);
    		name = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
    		Log.d(TAG,"RingtoneString "+ position + " "+ name);
    	
    	return name; 
    }
    private void selectRingtoneDialog(){
    	intentActionRingTonePicker.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI,mRingtoneManager.getRingtoneUri(mConfig.mRingtonePosition));
    	intentActionRingTonePicker.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.menuAarlm));
    	intentActionRingTonePicker.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
    	intentActionRingTonePicker.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
    	intentActionRingTonePicker.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,timerService.RINGTON_STREAMTYPE);
    	this.startActivityForResult(intentActionRingTonePicker,RINGTONE_REQUESETCODE);
    }
	private void selectSnoozeDialog(){

		String resouce = getString(R.string.snoozeTimeText);
		final CharSequence[] items = resouce.split(",");
		resouce = getString(R.string.snoozeTime);
		final String[] value = resouce.split(",");

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.snoozeDialogTitle));
		builder.setSingleChoiceItems(items, SnoozTimeListIndex, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				mConfig.mSnoozTime = Long.parseLong(value[item]);
				SnoozTimeListIndex = item;
			}
		});

		builder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				sendSetConfigIntent(mConfig);
				int i = mMenuMap.get(getString(R.string.menuSnooze));
    			changeMenuValue(i,String.format("%s",items[SnoozTimeListIndex]),mListView.getChildAt(i));
			}
		});
		

		AlertDialog alert = builder.create();
		alert.show();
		
	}
	private void selectLimitTimeDialog(){

		String resouce = getString(R.string.limittimeText);
		final CharSequence[] items = resouce.split(",");
		resouce = getString(R.string.limittime);
		final String[] value = resouce.split(",");

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.limittimeDialogTitle));
		builder.setSingleChoiceItems(items, LimitTimeIndex, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				mConfig.mLimitTime = Long.parseLong(value[item]);
				LimitTimeIndex = item;
			}
		});

		builder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				sendSetConfigIntent(mConfig);
				int i = mMenuMap.get(getString(R.string.menuLimittime));
    			changeMenuValue(i,String.format("%s",items[LimitTimeIndex]),mListView.getChildAt(i));
			}
		});
		

		AlertDialog alert = builder.create();
		alert.show();
		
	}
	private void selectRepeatDialog(){

		String resouce = getString(R.string.calcRepeatText);
		final CharSequence[] items = resouce.split(",");
		resouce = getString(R.string.calcRepeat);
		final String[] value = resouce.split(",");
		

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.calcRepeatDialogTitle));
		builder.setSingleChoiceItems(items, mCalcRepeatIndex, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				mConfig.mCalcRepeat = Integer.parseInt(value[item]);
				mCalcRepeatIndex = item;
			}
		});

		builder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				sendSetConfigIntent(mConfig);
				int i = mMenuMap.get(getString(R.string.menuRepeat));
    			changeMenuValue(i,String.format("%s",items[mCalcRepeatIndex]),mListView.getChildAt(i));
			}
		});
		

		AlertDialog alert = builder.create();
		alert.show();
		
	}
    public void selectTimeDialog(){
    	Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
    	
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
        		new TimePickerDialog.OnTimeSetListener() {
			@Override
        	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				mConfig.hour = hourOfDay;
				mConfig.minute = minute;
				sendSetConfigIntent(mConfig);
				int i = mMenuMap.get(getString(R.string.menuWakeup));
				changeMenuValue(i,String.format("%02d:%02d",mConfig.hour,mConfig.minute),mListView.getChildAt(i));
        	}
        }, hour, minute, true);

        timePickerDialog.show();
    	
    }

    private void shotToast(String text){
    	Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
    	toast.show();
    }
    private void sendSetConfigIntent(ConfigData config){
    	
    	Intent intent = new Intent(timerService.SET_CONFIG);
    	
    	
    	intent.putExtra(timerService.SET_HOUR, config.hour);
    	intent.putExtra(timerService.SET_MINUTE, config.minute);
    	intent.putExtra(timerService.SNOOZE, config.mSnoozTime);
    	intent.putExtra(CalcActivity.REPEAT, config.mCalcRepeat);
    	intent.putExtra(timerService.SOUND,config.mRingtonePosition);
    	intent.putExtra(timerService.VIBRATION, config.mVabration);
    	intent.putExtra(CalcActivity.LIMITTIME, config.mLimitTime);
    	intent.putExtra(timerService.SOUND_URI, config.mRingtonePath);
    	startService(intent);
    }

    private void selectCalcPreview(){
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
    private void changeMenuRoundmore(int position,String Value,View view){
    	if(mMenuItem != null){
    		MenuList menu = mMenuItem.get(position);
    		menu.setMenuValue(MenuList.ROUNDMORE,Value);
    		TextView textView = (TextView)view.findViewById(R.id.roundmoreDetailsTextView);
    		textView.setText((CharSequence)Value);
    		Log.d(TAG,"changeMenuValue:"+menu.getMenuText()+ " " +menu.getMenuType());
    		//mListView.setAdapter(new MenuAdapter(this,mMenuItem));
    	}
    }
    private void changeMenuValue(int position,String Value,View view){
    	if(mMenuItem != null){
    		MenuList menu = mMenuItem.get(position);
    		menu.setMenuValue(MenuList.TEXT,Value);
    		TextView textView = (TextView)view.findViewById(R.id.ValuetextView);
    		textView.setText((CharSequence)Value);
    		Log.d(TAG,"changeMenuValue:"+menu.getMenuText()+ " " +menu.getMenuType());
    		//mListView.setAdapter(new MenuAdapter(this,mMenuItem));
    	}
    }
//    public String[] readSetTime(){
//    	String time_text = null;
//    	String menu_text = null;
//    	menu_text = getString(R.string.wakeupTime);
//    	String[] splitText = menu_text.split(","); 
//    	try {
//    		time_text = readSetTimeFile();
//    		splitText[1] = time_text;
//    	}
//    	catch(Exception e){
//    		e.printStackTrace();
//    	}
//
//    	String[] timeSplit = splitText[1].split(":");
//    	splitText[1] = String.format("%02d:%02d", Integer.parseInt(timeSplit[0]),Integer.parseInt(timeSplit[1]));
//    	splitText[2] = MenuList.TEXT;
//    	
//        return splitText;
//    }
    private MenuList createMenuItem(int position ,String[] splitText){
    	MenuList menu = new MenuList();
        menu.setMenuText(splitText[0]);
        menu.setMenuValue(splitText[2],splitText[1]);
        menu.setMenuDetails(splitText[3]);
        
        Log.d(TAG,"makemenu:"+splitText[0]+ " " +splitText[2]+ " "+splitText[1]);
        mMenuMap.put(splitText[0], new Integer(position));
        return menu;
    }
//	private void setMenuItem(ConfigData config){
//		int i = mMenuMap.get(getString(R.string.menuWakeup));
//		mMenuItem.get(i).setMenuValue(MenuList.TEXT,String.format("%02d:%02d", config.hour,config.minute));
//		
//		i = mMenuMap.get(getString(R.string.menuSnooze));
//		SnoozTimeListIndex = findIndexLongValue(config.mSnoozTime,R.string.snoozeTime);
//		String t = findString(SnoozTimeListIndex,R.string.snoozeTimeText);
//		mMenuItem.get(i).setMenuValue(MenuList.TEXT,t);
//		
//		i = mMenuMap.get(getString(R.string.menuLimittime));
//		LimitTimeIndex = findIndexLongValue(config.mLimitTime,R.string.limittime);
//		t = findString(LimitTimeIndex,R.string.limittimeText);
//		mMenuItem.get(i).setMenuValue(MenuList.TEXT,t);
//		
//		i = mMenuMap.get(getString(R.string.menuRepeat));
//		mCalcRepeatIndex = findIndexLongValue(config.mCalcRepeat,R.string.calcRepeat);
//		t = findString(mCalcRepeatIndex,R.string.calcRepeatText);
//		mMenuItem.get(i).setMenuValue(MenuList.TEXT,t);
//		
//		i = mMenuMap.get(getString(R.string.menuAarlm));
//		mMenuItem.get(i).setMenuCheck(config.mAlarm);
//		
//		i = mMenuMap.get(getString(R.string.menuVibration));
//		mMenuItem.get(i).setMenuCheck(config.mVabration);
//	}
    private void makeMenuList(ConfigData config){
    	MenuList menu = null;
    	/*
    	 * メニューのstringsは
    	 * 0:メニュー
    	 * 1:値
    	 * 2:Type
    	 * 3:詳細
    	 */
    	int position = 0;
    	String menu_text = getString(R.string.wakeupTime);
    	String[] splitText = menu_text.split(",");
    	splitText[1] = String.format("%02d:%02d", mConfig.hour,mConfig.minute);
        mMenuItem.add(createMenuItem(position,splitText));
        
        position++;
        menu_text = getString(R.string.alarm);
        splitText = menu_text.split(",");
        splitText[1] = findRingtoneString(mConfig.mRingtonePosition);
        mMenuItem.add(createMenuItem(position,splitText));
        
        position++;
        menu_text = getString(R.string.vibration);
        splitText = menu_text.split(",");
        splitText[1] = Boolean.toString(mConfig.mVabration);
        mMenuItem.add(createMenuItem(position,splitText));
        
        position++;
        menu_text = getString(R.string.snooze);
        splitText = menu_text.split(",");
        SnoozTimeListIndex = findIndexLongValue(config.mSnoozTime,R.string.snoozeTime);
        splitText[1] = findString(SnoozTimeListIndex,R.string.snoozeTimeText);
        mMenuItem.add(createMenuItem(position,splitText));
        
        
        position++;
        menu_text = getString(R.string.repeat);
        splitText = menu_text.split(",");
        mCalcRepeatIndex = findIndexLongValue(config.mCalcRepeat,R.string.calcRepeat);
        splitText[1] = findString(mCalcRepeatIndex,R.string.calcRepeatText);
        mMenuItem.add(createMenuItem(position,splitText));
        
        position++;
        menu_text = getString(R.string.limitTime);
        splitText = menu_text.split(",");
        LimitTimeIndex = findIndexLongValue(config.mLimitTime,R.string.limittime);
        splitText[1] = findString(LimitTimeIndex,R.string.limittimeText);
        mMenuItem.add(createMenuItem(position,splitText));
        
        position++;
        menu_text = getString(R.string.preview);
        splitText = menu_text.split(",");
        mMenuItem.add(createMenuItem(position,splitText));
       
        
        
    }

    /*
     * メニューリストアダプター
     */
    class ViewHolder {
		public TextView menuView;
		public TextView detailsView;
		public TextView valueView;
	}
    class CheckViewHolder {
		public TextView menuView;
		public TextView detailsView;
		public CheckBox valueView;
	}
    class RoundmoreViewHolder {
		public TextView menuView;
		public TextView detailsView;
		public ImageView valueView;
	}
    public class MenuAdapter extends BaseAdapter {
    	private ArrayList<MenuList> items;  
		private LayoutInflater inflater;
		private Context mContext;
		public MenuAdapter(Context context,ArrayList<MenuList> item) {
			mContext = context;
			items = item;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		private View convertViewHolder(int position,View convertView,ViewGroup parent){
			ViewHolder holder;
			
//    		if(convertView == null){
    			convertView = inflater.inflate(R.layout.menu_row, parent,false);
	    		holder = new ViewHolder();
	    		holder.menuView = (TextView)convertView.findViewById(R.id.MenutextView);
	    		holder.valueView = (TextView)convertView.findViewById(R.id.ValuetextView);
	    		holder.detailsView = (TextView)convertView.findViewById(R.id.DetailsTextView);
	    		convertView.setTag(holder);
//    		}
//    		else {
//    			holder = (ViewHolder)convertView.getTag();
//    		}
    		//if(position == 0){
    			holder.menuView.setTextSize(24.0f);
    			holder.valueView.setTextSize(24.0f);
    		//}
	    	holder.menuView.setText((CharSequence)this.items.get(position).getMenuText());
	    	holder.detailsView.setText((CharSequence)this.items.get(position).getMenuDetails());
	    	holder.valueView.setText((CharSequence)this.items.get(position).getMenuValue());
	    	holder.valueView.setTag(items.get(position));
	    	return convertView;
		}
		private View convertRoundmoreViewHolder(int position,View convertView,ViewGroup parent){
			RoundmoreViewHolder holder;
			
//    		if(convertView == null){
    			convertView = inflater.inflate(R.layout.menu_roundmore, parent,false);
	    		holder = new RoundmoreViewHolder();
	    		holder.menuView = (TextView)convertView.findViewById(R.id.roundmoreMenutextView);
//	    		holder.valueView = (TextView)convertView.findViewById(R.id.ValuetextView);
	    		holder.detailsView = (TextView)convertView.findViewById(R.id.roundmoreDetailsTextView);
	    		convertView.setTag(holder);
//    		}
//    		else {
//    			holder = (ViewHolder)convertView.getTag();
//    		}
    		//if(position == 0){
    			holder.menuView.setTextSize(24.0f);
    			
    		//}
	    	holder.menuView.setText((CharSequence)this.items.get(position).getMenuText());
	    	holder.detailsView.setText((CharSequence)this.items.get(position).getMenuValue());
	    	
	    	return convertView;
		}
		private View convertCheckViewHolder(int position,View convertView,ViewGroup parent){
			CheckViewHolder holder;
			
//    		if(convertView == null){
    			convertView = inflater.inflate(R.layout.menu_check, parent,false);
	    		holder = new CheckViewHolder();
	    		holder.menuView = (TextView)convertView.findViewById(R.id.chkMenutextView);
	    		holder.valueView = (CheckBox)convertView.findViewById(R.id.checkBox1);
	    		holder.detailsView = (TextView)convertView.findViewById(R.id.chkDetailsTextView);
	    		
//    		}
//    		else {
//    			holder = (CheckViewHolder)convertView.getTag();
//    		}
    		
    		holder.menuView.setTextSize(24.0f);
    		
	    	holder.menuView.setText((CharSequence)this.items.get(position).getMenuText());
	    	holder.detailsView.setText((CharSequence)this.items.get(position).getMenuDetails());
	    	final int p = position;

	    	holder.valueView.setTag(this.items.get(position).getMenuText());
    		// 必ずsetChecked前にリスナを登録(convertView != null の場合は既に別行用のリスナが登録されている！)
	    	holder.valueView.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					String tag = (String)buttonView.getTag();
//					int position = mMenuMap.get(tag);
					
					if(tag.equals(getString(R.string.menuVibration))){
						if(mConfig.mVabration != isChecked){
							mConfig.mVabration = isChecked;
							sendSetConfigIntent(mConfig);
							Log.d("onCheckedChanged", tag+" p=" + String.valueOf(p) + ", isChecked=" + mConfig.mVabration);
						}
					}
//					items.get(position).setMenuCheck(isChecked);
					
					
				}
    		});
	    	holder.valueView.setChecked(this.items.get(position).getMenuCheck());
	    	
	    	convertView.setClickable(false);
	    	return convertView;
		}
		@Override
		public View getView(int position,View convertView,ViewGroup parent) {
			// TODO 自動生成されたメソッド・スタブ
			int menuType = this.items.get(position).getMenuType();
	    	if(MenuList.TYPE_TEXT == menuType){
	    		convertView = convertViewHolder(position,convertView,parent);
	    	}
	    	else if(MenuList.TYPE_CHECK == menuType){
	    		convertView = convertCheckViewHolder(position,convertView,parent);
	    	}
	    	else if(MenuList.TYPE_ROUNDMORE == menuType){
	    		convertView = convertRoundmoreViewHolder(position,convertView,parent);
	    	}
	    	
	    	
			return convertView;
		}

		@Override
		public int getCount() {
			// TODO 自動生成されたメソッド・スタブ
			return this.items.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO 自動生成されたメソッド・スタブ
			return this.items.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO 自動生成されたメソッド・スタブ
			return position;
		}  
    	
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO 自動生成されたメソッド・スタブ	
		if(RINGTONE_REQUESETCODE == requestCode){
			if(resultCode == RESULT_OK){
				Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			    if (uri != null) {
			    	mConfig.mRingtonePosition = mRingtoneManager.getRingtonePosition(uri);
			    	mConfig.mRingtonePath = uri.toString();
			    	int i = mMenuMap.get(getString(R.string.menuAarlm));
			    	String ringtone_name = findRingtoneString(mConfig.mRingtonePosition);
			    	Log.d(TAG,"Ringtone name "+ ringtone_name);
			    	changeMenuRoundmore(i,ringtone_name,mListView.getChildAt(i));
			    	sendSetConfigIntent(mConfig);
			    }
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	

}