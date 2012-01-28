package jp.peisun.wakeuptimer;



import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;


public class timerService extends Service {
	private final long OneMinute = 60*1000;
	private final long INTERVAL_TIME = 24*60*1000;
	private final int THREAD_SLEEP_TIME = 1000; 
	private MediaPlayer player;
	private Snooze snooze = null;
	private int setHour = 0;
	private int setMinute = 0;
	public static final String INTENT_SETTIME = "jp.peisun.wakeupTimer.intent.setTime";
	public static final String WAKEUP_ACTION = "jp.peisun.wakeupTimer.intent.wakeupAction";
	public static final String SOUND_PALY = "jp.peisun.wakeupTimer.intent.soundPlay";
	public static final String SOUND_STOP = "jp.peisun.wakeupTimer.intent.soundStop";
	public static final String SOUND_SNOOZE = "jp.peisun.wakeupTimer.intent.soundSnooze";
	public static final String SET_HOUR = "setHour";
	public static final String SET_MINUTE = "setMinute"; 
	private AlarmManager am = null;
	private Intent intent = null;
	private PendingIntent mAlarmSender = null;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public void onCreate() {
		// TODO 自動生成されたメソッド・スタブ
		// set intentC
		intent = new Intent();
	    intent.setAction(WAKEUP_ACTION);   
	    am = (AlarmManager)getSystemService(ALARM_SERVICE);
		mAlarmSender = PendingIntent.getService(this,0, intent, 0);
        
		super.onCreate();
	}
	@Override
	public void onDestroy() {
		// TODO 自動生成されたメソッド・スタブ
		super.onDestroy();
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO 自動生成されたメソッド・スタブ
		String Action;
		if (intent != null) {
			Action = intent.getAction();
			
		} else {
			Action = "";
		}
		/* 起床時間の設定 */
		if(Action.equals(INTENT_SETTIME)){
			setHour = intent.getIntExtra(SET_HOUR, 6);
			setMinute = intent.getIntExtra(SET_MINUTE, 0);
			alarmSetTime(setHour,setMinute);
						
		}
		/* アラームの鳴動 */
		if(Action.equals(SOUND_PALY)){
			soundPlay();
		}
		/* アラームの一時停止 */
		if(Action.equals(SOUND_SNOOZE)){
			soundStop();
		}
		/* アラームの停止 */
		if(Action.equals(SOUND_STOP)){
			soundStop();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	private void alarmSetTime(int hour, int minute){
		// Schedule the alarm!
		// 0時を超えてないかったら、翌日
		// 0時を過ぎてて、指定した時間の前だったら、その日
		Calendar rightNow = Calendar.getInstance();
		GregorianCalendar calendar;
		calendar = new GregorianCalendar();
		long currentTime = System.currentTimeMillis();
		calendar.setTimeInMillis(currentTime);
		if(rightNow.get(Calendar.HOUR_OF_DAY) == calendar.get(Calendar.HOUR_OF_DAY)){
			calendar.add(Calendar.DATE, 1);
		}
		else {
			calendar.add(Calendar.DATE, 0);
		}
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
        //am.set(AlarmManager.RTC,calendar.getTimeInMillis(),mAlarmSender);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
        			calendar.getTimeInMillis(), 
        			INTERVAL_TIME,mAlarmSender);
	}
	private void setTime(int snoozeTime){
		if(snooze != null){
			snooze.cancel(true);	/* AsyncTaskのキャンセル */
		}
		snooze = new Snooze();
		snooze.execute(snoozeTime);
	}
	private Handler AlarmHandler = new Handler(){
		@Override
        public void handleMessage(Message msg) {
			if(msg.)
		}
	};
	private void soundPlay(){
		if(player == null){
			player = new MediaPlayer();
		}
		//アラーム音として設定
		player.setAudioStreamType(AudioManager.STREAM_ALARM);
		//音源を指定
		try {
			player.setDataSource(this,Settings.System.DEFAULT_NOTIFICATION_URI);
		} catch (IllegalArgumentException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		//繰り返し再生するように指定
		player.setLooping(true);
		//
		try {
			player.prepare();
		} catch (IllegalStateException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		//最初に巻き戻し
		player.seekTo(0);
		//再生開始
		player.start();

	}
	private void soundReplay(){
		//最初に巻き戻し
		player.seekTo(0);
		//再生開始
		player.start();
	}
	private void soundStop(){
		player.stop();
	}
	class Snooze extends AsyncTask<Integer,Void,Boolean> {
		long alarmTime = 0;
		@Override
		protected Boolean doInBackground(Integer... params) {
			// TODO 自動生成されたメソッド・スタブ
			int snoozeTime = params[0].intValue();
			while(this.isCancelled() == false || snoozeTime > 0){
				Thread.sleep(THREAD_SLEEP_TIME);
				snoozeTime -= THREAD_SLEEP_TIME;
			}
			if(this.isCancelled() == true) return false;
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO 自動生成されたメソッド・スタブ
			if(result){
				soundPlay();
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO 自動生成されたメソッド・スタブ
			super.onPreExecute();
		}

		@Override
		protected void onCancelled() {
			// TODO 自動生成されたメソッド・スタブ
			soundPlay();
			super.onCancelled();
		}

		
		
	}
	

}
