package jp.peisun.wakeuptimer;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
	private static final String ERROR_WRITE_FILE_SETTIME = "起床時間をファイルに書き込めませんでした。%n次回起動時に起床時間を設定できないです";
	private ArrayList<MenuList> mMenuItem = new ArrayList<MenuList>();
	private ListView mListView = null;
	private TimePickerDialog timePickerDialog  = null;
	private volatile boolean mAlarmOn = true;
	private volatile boolean mVibrationOn = true;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        makeMenuList();
        mListView = (ListView)findViewById(R.id.menulistView);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(new MenuAdapter(this,mMenuItem));
    }
    @Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long i) {
		// TODO 自動生成されたメソッド・スタブ
    	
    	String[] menuList = getMenuList();
    	TextView menuview = (TextView)view.findViewById(R.id.MenutextView);
    	if(menuview != null){
    		String menuText = menuview.getText().toString();
    		/*
    		 * onItemClickでのif-elseifはメニューの表示の順番とは関係ない
    		 * R.id.menulistの並びで、それぞれの処理に分岐する
    		 */
    		if(menuText.equals(menuList[0])){
    			selectTimeDialog();
    		}
    		else if(menuText.equals(menuList[1])){
    			selectCalcPreview();
    		}
    	}
    	TextView chkview = (TextView)view.findViewById(R.id.chkMenutextView);
    	if(chkview != null){
    		CheckBox buttonView = (CheckBox)view.findViewById(R.id.checkBox1);
    		String menuText = chkview.getText().toString();
    		if(menuText.equals(menuList[2])){
    			mAlarmOn = !mAlarmOn;
    			mMenuItem.get(2).setMenuCheck(mAlarmOn);
    			buttonView.setChecked(mAlarmOn);
    			Log.d(TAG,"mAlarmOn "+ mMenuItem.get(2).getMenuCheck());

    		}
    		else if(menuText.equals(menuList[3])){
    			mVibrationOn = !mVibrationOn;
    			mMenuItem.get(3).setMenuCheck(mVibrationOn);
    			buttonView.setChecked(mVibrationOn);
    			Log.d(TAG,"mVibrationOn "+mMenuItem.get(3).getMenuCheck());
    		}
    	}
    	/* これは乱暴だな
    	mListView.setAdapter(new MenuAdapter(this,mMenuItem));
    	*/
	}
	
	private String[] getMenuList(){
		String menu = getString(R.string.menulist);
    	String[] menuList = menu.split(",");
    	return menuList;
	}
    private void selectTimeDialog(){
    	Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
    	if(timePickerDialog == null){
    		timePickerDialog = new TimePickerDialog(this,
            new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                	sendSetTimeIntent(hourOfDay,minute);
                	try {
                		writeSetTime(hourOfDay,minute);
                		Date d = new Date();
                    	d.setHours(hourOfDay);
                    	d.setMinutes(minute);
                		changeMenuList(0,mMenuItem.get(0).getMenuText(),String.format("%tH:%tM", d,d));
                	}
                	catch(Exception e){
                		showErrorDialog(ERROR_WRITE_FILE_SETTIME);
                	}
                }
            }, hour, minute, true);
    	}
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
        	startActivityForResult(intent,0);
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	
    }
    private void changeMenuList(int position,String text,String Value){
    	if(mMenuItem != null){
    		MenuList menu = mMenuItem.get(position);
    		menu.setMenuText(text);
    		menu.setMenuValue(MenuList.TEXT,Value);
    		Log.d(TAG,"changeMenuList:"+menu.getMenuText()+ " " +menu.getMenuType());
    		mListView.setAdapter(new MenuAdapter(this,mMenuItem));
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
    private void makeMenuList(){
    	
    	String[] splitText = readSetTime();
    	/*
    	 * メニューのstringsは
    	 * 0:メニュー
    	 * 1:値
    	 * 2:Type
    	 * 3:詳細
    	 */
        MenuList menu = new MenuList();
        menu.setMenuText(splitText[0]);
        menu.setMenuValue(splitText[2],splitText[1]);
        menu.setMenuDetails(splitText[3]);
        Log.d(TAG,"makemenu:"+splitText[0]+ " " +splitText[2]+ " "+splitText[1]);
        mMenuItem.add(menu);
        
        String menu_text = getString(R.string.alarm);
        splitText = menu_text.split(",");
        menu = new MenuList();
        menu.setMenuText(splitText[0]);
        menu.setMenuValue(splitText[2],Boolean.toString(mAlarmOn));
        menu.setMenuDetails(splitText[3]);
        Log.d(TAG,"makemenu:"+splitText[0]+ " " +splitText[2]+ " "+mAlarmOn);
        mMenuItem.add(menu);
        
        menu_text = getString(R.string.vibration);
        splitText = menu_text.split(",");
        menu = new MenuList();
        menu.setMenuText(splitText[0]);
        menu.setMenuValue(splitText[2],Boolean.toString(mVibrationOn));
        menu.setMenuDetails(splitText[3]);
        Log.d(TAG,"makemenu:"+splitText[0]+ " " +splitText[2]+ " "+mVibrationOn);
        mMenuItem.add(menu);
        
        menu_text = getString(R.string.preview);
        splitText = menu_text.split(",");
        menu = new MenuList();
        menu.setMenuText(splitText[0]);
        menu.setMenuValue(splitText[2],splitText[1]);
        menu.setMenuDetails(splitText[3]);
        Log.d(TAG,"makemenu:"+splitText[0]+ " " +splitText[2]+ " "+splitText[1]);
        mMenuItem.add(menu);
        
//        menu = new MenuList();
//        menu.setMenuText(null);
//        menu.setMenuValue(null,null);
//        mMenuItem.add(menu);
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
}