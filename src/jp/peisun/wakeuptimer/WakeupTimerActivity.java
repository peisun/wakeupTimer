package jp.peisun.wakeuptimer;

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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
	private static final String TITLE_ERROR = "エラー";
	private static final int REVIEW_REPEAT = 2; /* preview時の計算回数 */
	private static final String ERROR_WRITE_FILE_SETTIME = "起床時間をファイルに書き込めませんでした。%n次回起動時に起床時間を設定できないです";
	private ArrayList<MenuList> mMenuItem = new ArrayList<MenuList>();
	private HashMap<String,Integer> mMenuMap = new HashMap<String,Integer>();
	private ListView mListView = null;
	
	private volatile boolean mAlarmOn = true;
	private volatile boolean mVibrationOn = true;
	private String[] SnoozTimeList =null;
	private int SnoozTimeListIndex = 0;
	private String[] LimitTime = null;
	private int LimitTimeIndex = 0;
	private volatile long mSnoozTime = 0;
	private volatile long mLimitTime = 0;
	private volatile int mCalcRepeat = 0;
	private int mCalcRepeatIndex = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        makeMenuList();
        mListView = (ListView)findViewById(R.id.menulistView);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(new MenuAdapter(this,mMenuItem));
        
        
		mSnoozTime = Long.parseLong(getString(R.string.snoozeTimeDefault));
		SnoozTimeListIndex = Integer.parseInt(getString(R.string.snoozeTimeDefaultIndex));
		
		mLimitTime = Long.parseLong(getString(R.string.limittimeDefault));
		LimitTimeIndex = Integer.parseInt(getString(R.string.limittimeDefaultIndex));
				
		mCalcRepeat = Integer.parseInt(getString(R.string.calcRepeatDefault));
		mCalcRepeatIndex = Integer.parseInt(getString(R.string.calcRepeatDefaultIndex));
		
		mAlarmOn = Boolean.parseBoolean(getString(R.string.alarmDefault));
		mVibrationOn = Boolean.parseBoolean(getString(R.string.vibrationDefault));
    }
    @Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long i) {
		/*
		 * ListViewの上からの順はmMenuItemにaddした順で決まるので
		 * addした位置とメニューのテキストともにHashMapに入れておき、
		 * ViewのMenutextViewもしくはchkMenutextViewの文字列をkeyに
		 * addした位置を取得し、それと一致したらファンクションをコールする
		 */   	
    	TextView menuview = (TextView)view.findViewById(R.id.MenutextView);
    	
    	if(position == mMenuMap.get(getString(R.string.menuWakeup))){
    		selectTimeDialog();
    	}
    	else if(position == mMenuMap.get(getString(R.string.menuPreview))){
    		selectCalcPreview();
    	}
    	else if(position == mMenuMap.get(getString(R.string.menuSnooze))){
    		selectSnoozeDialog();
    	}
    	else if(position == mMenuMap.get(getString(R.string.menuComputational))){
    		selectComputationalDialog();
    	}
    	else if(position == mMenuMap.get(getString(R.string.menuLimittime))){
    		selectLimitTimeDialog();
    	}
//    	else if(position == mMenuMap.get(getString(R.string.menuAarlm))){
//    		mAlarmOn = !mAlarmOn;
//    		mMenuItem.get(position).setMenuCheck(mAlarmOn);
//    		CheckBox buttonView = (CheckBox)view.findViewById(R.id.checkBox1);
//    		buttonView.setChecked(mAlarmOn);
//    		sendSoundSetIntent(mAlarmOn);
//    		Log.d(TAG,"mAlarmOn "+ mMenuItem.get(2).getMenuCheck());
//    	}
//    	else if(position == mMenuMap.get(getString(R.string.menuVibration))){
//    		mVibrationOn = !mVibrationOn;
//    		mMenuItem.get(position).setMenuCheck(mVibrationOn);
//    		CheckBox buttonView = (CheckBox)view.findViewById(R.id.checkBox1);
//    		buttonView.setChecked(mVibrationOn);
//    		sendVibrationIntent(mVibrationOn);
//    		Log.d(TAG,"mVibrationOn "+mMenuItem.get(3).getMenuCheck());
//
//    	}
    		
    	
    	/* これは乱暴だな
    	mListView.setAdapter(new MenuAdapter(this,mMenuItem));
    	*/
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
				mSnoozTime = Long.parseLong(value[item]);
				SnoozTimeListIndex = item;
			}
		});

		builder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				sendSnoozeTimeIntent(mSnoozTime);
				int i = mMenuMap.get(getString(R.string.menuSnooze));
    			changeMenuList(i,String.format("%s",items[SnoozTimeListIndex]),mListView.getChildAt(i));
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
				mLimitTime = Long.parseLong(value[item]);
				LimitTimeIndex = item;
			}
		});

		builder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				sendLimitTimeIntent(mLimitTime);
				int i = mMenuMap.get(getString(R.string.menuLimittime));
    			changeMenuList(i,String.format("%s",items[LimitTimeIndex]),mListView.getChildAt(i));
			}
		});
		

		AlertDialog alert = builder.create();
		alert.show();
		
	}
	private void selectComputationalDialog(){

		String resouce = getString(R.string.calcRepeatText);
		final CharSequence[] items = resouce.split(",");
		resouce = getString(R.string.calcRepeat);
		final String[] value = resouce.split(",");
		

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.calcRepeatDialogTitle));
		builder.setSingleChoiceItems(items, mCalcRepeatIndex, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				mCalcRepeat = Integer.parseInt(value[item]);
				mCalcRepeatIndex = item;
			}
		});

		builder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				sendCalcRepeatIntent(mCalcRepeat);
				int i = mMenuMap.get(getString(R.string.menuComputational));
    			changeMenuList(i,String.format("%s",items[mCalcRepeatIndex]),mListView.getChildAt(i));
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
        		sendSetTimeIntent(hourOfDay,minute);
        		try {
        			writeSetTime(hourOfDay,minute);
        			Date d = new Date();
        			d.setHours(hourOfDay);
        			d.setMinutes(minute);
        			int i = mMenuMap.get(getString(R.string.menuWakeup));
        			changeMenuList(i,String.format("%tH:%tM", d,d),mListView.getChildAt(i));
        		}
        		catch(Exception e){
        			showErrorDialog(ERROR_WRITE_FILE_SETTIME);
        		}
        	}
        }, hour, minute, true);

        timePickerDialog.show();
    	
    }
	private void showErrorDialog(String text){
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		
		dlg.setTitle(TITLE_ERROR);
		dlg.setMessage(text);
		dlg.setPositiveButton("OK", null);
		dlg.show();
		
	}
    private void shotToast(String text){
    	Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
    	toast.show();
    }
    private void sendCalcRepeatIntent(int mCalcRepeat){
    	Intent intent = new Intent(timerService.SNOOZE_TIME);
    	intent.putExtra(timerService.SNOOZE, mCalcRepeat);
    	startService(intent);
    }
    private void sendSnoozeTimeIntent(long value){
    	Intent intent = new Intent(timerService.SNOOZE_TIME);
    	intent.putExtra(timerService.SNOOZE, value);
    	startService(intent);
    }
    private void sendLimitTimeIntent(long value){
    	Intent intent = new Intent(timerService.SET_LIMITTIME);
    	intent.putExtra(CalcActivity.LIMITTIME, value);
    	startService(intent);
    }
    private void sendVibrationIntent(boolean value){
    	Intent intent = new Intent(timerService.VIBRATION_SET);
    	intent.putExtra(timerService.VIBRATION, value);
    	startService(intent);
    }
    private void sendSoundSetIntent(boolean value){
    	Intent intent = new Intent(timerService.SOUND_SET);
    	intent.putExtra(timerService.SOUND, value);
    	startService(intent);
    }
    private void sendSetTimeIntent(int hour,int minute){
    	Intent intent = new Intent(timerService.INTENT_SETTIME);
    	intent.putExtra(timerService.SET_HOUR, hour);
    	intent.putExtra(timerService.SET_MINUTE, minute);
    	startService(intent);
    }
    private void selectCalcPreview(){
    	try {
    		
    		Intent intent = new Intent(getApplicationContext(),CalcActivity.class);
    		intent.putExtra(CalcActivity.PREVIEW, true);
    		intent.putExtra(CalcActivity.REPEAT, REVIEW_REPEAT);
    		intent.putExtra(CalcActivity.LIMITTIME, mLimitTime);
        	startActivityForResult(intent,0);
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	
    }
    private void changeMenuList(int position,String Value,View view){
    	if(mMenuItem != null){
    		MenuList menu = mMenuItem.get(position);
    		menu.setMenuValue(MenuList.TEXT,Value);
    		TextView textView = (TextView)view.findViewById(R.id.ValuetextView);
    		textView.setText((CharSequence)Value);
    		Log.d(TAG,"changeMenuList:"+menu.getMenuText()+ " " +menu.getMenuType());
    		//mListView.setAdapter(new MenuAdapter(this,mMenuItem));
    	}
    }
    public String[] readSetTime(){
    	String time_text = null;
    	String menu_text = null;
    	menu_text = getString(R.string.wakeupTime);
    	String[] splitText = menu_text.split(","); 
    	try {
    		time_text = readSetTimeFile();
    		splitText[1] = time_text;
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}

    	String[] timeSplit = splitText[1].split(":");
    	Date d = new Date();
    	d.setHours(Integer.parseInt(timeSplit[0]));
    	d.setMinutes(Integer.parseInt(timeSplit[1]));
    	splitText[1] = String.format("%tH:%tM", d,d);
    	splitText[2] = MenuList.TEXT;
    	
        return splitText;
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
    private void makeMenuList(){
    	MenuList menu = null;
    	/*
    	 * メニューのstringsは
    	 * 0:メニュー
    	 * 1:値
    	 * 2:Type
    	 * 3:詳細
    	 */
    	int position = 0;
    	String[] splitText = readSetTime();
        mMenuItem.add(createMenuItem(position,splitText));
        
        position++;
        String menu_text = getString(R.string.alarm);
        splitText = menu_text.split(",");
        menu = createMenuItem(position,splitText);
        mMenuItem.add(menu);
        
        position++;
        menu_text = getString(R.string.vibration);
        splitText = menu_text.split(",");
        mMenuItem.add(createMenuItem(position,splitText));
        
        position++;
        menu_text = getString(R.string.snooze);
        splitText = menu_text.split(",");
        mMenuItem.add(createMenuItem(position,splitText));
        
        
        position++;
        menu_text = getString(R.string.computational);
        splitText = menu_text.split(",");
        mMenuItem.add(createMenuItem(position,splitText));
        
        position++;
        menu_text = getString(R.string.limitTime);
        splitText = menu_text.split(",");
        mMenuItem.add(createMenuItem(position,splitText));
        
        position++;
        menu_text = getString(R.string.preview);
        splitText = menu_text.split(",");
        mMenuItem.add(createMenuItem(position,splitText));
       
        
        
    }
    public String readSetTimeFile() throws Exception {
    	InputStream in = openFileInput(timerService.FILE_SETTIME);  
    	ObjectInputStream ois = new ObjectInputStream(in);  
    	String setTime = (String)ois.readObject();
    	ois.close();
    	in.close();
    	return setTime;
    }
    private void writeSetTime(int hour,int minute) throws Exception {
    	OutputStream out = null;
    	ObjectOutputStream oos = null;
    	String setTime = String.format("%d:%d", hour,minute);
    	
    	out = openFileOutput(timerService.FILE_SETTIME, MODE_PRIVATE);
    	oos = new ObjectOutputStream(out);  
    	oos.writeObject(setTime);
    	oos.close();
    	out.close();
    	
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
    		// 必ずsetChecked前にリスナを登録(convertView != null の場合は既に別行用のリスナが登録されている！)
	    	holder.valueView.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					Log.i("MultipleChoiceListActivity", "p=" + String.valueOf(p) + ", isChecked=" + String.valueOf(isChecked));
					items.get(p).setMenuCheck(isChecked);
				}
    		});
	    	holder.valueView.setChecked(this.items.get(position).getMenuCheck());
	    	holder.valueView.setTag(this.items.get(position));
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
    interface MethodInvoker {
    	void invoke(WakeupTimerActivity instance);
    }
}