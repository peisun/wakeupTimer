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
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
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

	private void writeFile(ConfigData config,File file){
		OutputStream os;
		try {
			os = new FileOutputStream(file);

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
			if(mConfig == null){
				mConfig = setDefaultValue();
			}
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
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
    		
    	}
    	else if(position == mMenuMap.get(getString(R.string.menuVibration))){
    		mConfig.mVabration = !mConfig.mVabration;
    		mMenuItem.get(position).setMenuCheck(mConfig.mVabration);
    		CheckBox buttonView = (CheckBox)view.findViewById(R.id.checkBox1);
    		buttonView.setChecked(mConfig.mVabration);
    		sendSetConfigIntent(mConfig);
    		Log.d(TAG,"mVabrationOn "+mMenuItem.get(3).getMenuCheck());

    	}
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

    	}
    }
    private void changeMenuValue(int position,String Value,View view){
    	if(mMenuItem != null){
    		MenuList menu = mMenuItem.get(position);
    		menu.setMenuValue(MenuList.TEXT,Value);
    		TextView textView = (TextView)view.findViewById(R.id.ValuetextView);
    		textView.setText((CharSequence)Value);
    		Log.d(TAG,"changeMenuValue:"+menu.getMenuText()+ " " +menu.getMenuType());

    	}
    }
    private MenuList createMenuItem(int position ,String[] splitText){
    	MenuList menu = new MenuList();
        menu.setMenuText(splitText[0]);
        menu.setMenuValue(splitText[2],splitText[1]);
        menu.setMenuDetails(splitText[3]);
        
        Log.d(TAG,"makemenu:"+splitText[0]+ " " +splitText[2]+ " "+splitText[1]);
        mMenuMap.put(splitText[0], new Integer(position));
        return menu;
    }
    private void makeMenuList(ConfigData config){
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
			
    			convertView = inflater.inflate(R.layout.menu_row, parent,false);
	    		holder = new ViewHolder();
	    		holder.menuView = (TextView)convertView.findViewById(R.id.MenutextView);
	    		holder.valueView = (TextView)convertView.findViewById(R.id.ValuetextView);
	    		holder.detailsView = (TextView)convertView.findViewById(R.id.DetailsTextView);
	    		convertView.setTag(holder);

    			holder.menuView.setTextSize(24.0f);
    			holder.valueView.setTextSize(24.0f);
    			
	    	holder.menuView.setText((CharSequence)this.items.get(position).getMenuText());
	    	holder.detailsView.setText((CharSequence)this.items.get(position).getMenuDetails());
	    	holder.valueView.setText((CharSequence)this.items.get(position).getMenuValue());
	    	holder.valueView.setTag(items.get(position));
	    	return convertView;
		}
		private View convertRoundmoreViewHolder(int position,View convertView,ViewGroup parent){
			RoundmoreViewHolder holder;

    			convertView = inflater.inflate(R.layout.menu_roundmore, parent,false);
	    		holder = new RoundmoreViewHolder();
	    		holder.menuView = (TextView)convertView.findViewById(R.id.roundmoreMenutextView);

	    		holder.detailsView = (TextView)convertView.findViewById(R.id.roundmoreDetailsTextView);
	    		convertView.setTag(holder);

	    		holder.menuView.setTextSize(24.0f);
    			

	    	holder.menuView.setText((CharSequence)this.items.get(position).getMenuText());
	    	holder.detailsView.setText((CharSequence)this.items.get(position).getMenuValue());
	    	
	    	return convertView;
		}
		private View convertCheckViewHolder(int position,View convertView,ViewGroup parent){
			CheckViewHolder holder;
			
    			convertView = inflater.inflate(R.layout.menu_check, parent,false);
	    		holder = new CheckViewHolder();
	    		holder.menuView = (TextView)convertView.findViewById(R.id.chkMenutextView);
	    		holder.valueView = (CheckBox)convertView.findViewById(R.id.checkBox1);
	    		holder.detailsView = (TextView)convertView.findViewById(R.id.chkDetailsTextView);
	    		
    		
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
					
					if(tag.equals(getString(R.string.menuVibration))){
						if(mConfig.mVabration != isChecked){
							mConfig.mVabration = isChecked;
							sendSetConfigIntent(mConfig);
							Log.d("onCheckedChanged", tag+" p=" + String.valueOf(p) + ", isChecked=" + mConfig.mVabration);
						}
					}
					
					
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