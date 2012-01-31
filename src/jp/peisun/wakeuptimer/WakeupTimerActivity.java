package jp.peisun.wakeuptimer;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

/*
 * 各種設定をするActivity.
 * 
 */
public class WakeupTimerActivity extends Activity implements OnItemClickListener {
    /** Called when the activity is first created. */
	private ArrayList<MenuList> mMenuItem = new ArrayList<MenuList>();
	private ListView mListView = null;
	private TimePickerDialog timePickerDialog  = null;
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
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO 自動生成されたメソッド・スタブ
    	int position = arg2;
    	switch(position){
    	case 0:
    		selectTimeDialog();
    		break;
    	case 1:
    		selectCalcPreview();
    		break;
    	default:
    		break;
    	}
		
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
                }
            }, hour, minute, true);
    	}
        timePickerDialog.show();
    	
    }
    private void sendSetTimeIntent(int hour,int minute){
    	Intent intent = new Intent(timerService.INTENT_SETTIME);
    	intent.putExtra(timerService.SET_HOUR, hour);
    	intent.putExtra(timerService.SET_MINUTE, minute);
    	startService(intent);
    }
    private void selectCalcPreview(){
    	Intent intent;
    	try {
    		
    		intent = new Intent(WakeupTimerActivity.this,CalcActivity.class);
    		intent.putExtra(CalcActivity.RESULT, true);
        	startActivityForResult(intent,0);
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	
    }
    private void makeMenuList(){
    	String text = getString(R.string.wakeupTime);
        String[] splitText = text.split(",");
        MenuList menu = new MenuList();
        menu.setMenuText(splitText[0]);
        menu.setMenuValue(splitText[1]);
        mMenuItem.add(menu);
        
        text = getString(R.string.preview);
        splitText = text.split(",");
        menu = new MenuList();
        menu.setMenuText(splitText[0]);
        menu.setMenuValue(splitText[1]);
        mMenuItem.add(menu);
        
        menu = new MenuList();
        menu.setMenuText(null);
        menu.setMenuValue(null);
        mMenuItem.add(menu);
    }
    /*
     * メニューリストアダプター
     */
    class ViewHolder {
		public TextView menuView;
		public TextView valueView;
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
		
		@Override
		public View getView(int position,View convertView,ViewGroup parent) {
			// TODO 自動生成されたメソッド・スタブ
			ViewHolder holder;
    		if(convertView == null){
    			convertView = inflater.inflate(R.layout.menu_row, parent,false);
	    		holder = new ViewHolder();
	    		holder.menuView = (TextView)convertView.findViewById(R.id.MenutextView);
	    		holder.valueView = (TextView)convertView.findViewById(R.id.ValuetextView);
	    		convertView.setTag(holder);
    		}
    		else {
    			holder = (ViewHolder)convertView.getTag();
    		}
    		//if(position == 0){
    			holder.menuView.setTextSize(24.0f);
    			holder.valueView.setTextSize(24.0f);
    		//}
	    	holder.menuView.setText((CharSequence)this.items.get(position).getMenuText());
	    	holder.valueView.setText((CharSequence)this.items.get(position).getMenuValue());
	    	
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
    	private String mMenuText;
    	private String mMenuValue;
    	public String getMenuText(){ return mMenuText; }
    	public String getMenuValue(){ return mMenuValue; }
    	public void setMenuText(String text){ mMenuText = text; }
    	public void setMenuValue(String value){ mMenuValue = value; };
    	
    }
	
	
}