package jp.peisun.wakeuptimer;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Button;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import java.util.*;

public class CalcActivity extends Activity implements OnClickListener {
	private boolean result = false;
	public static String RESULT = "result";
	private static final int TICK_TIME = 1000;
	private static final int REMAINING_TIME = 300*1000;
	private static final int DEFAULT_REPEAT = 5;
	private volatile long remainingTime = REMAINING_TIME;
	private static final int MSG_COUNTDOWN = 1;
	private static final int MSG_CANCEL = 2;
	private final int BUTTON_CLR = 0x0c;
	private final int BUTTON_ENTER = 0x0e;
	private final int BUTTON_CONTINUE = 0x0f;
	private CountdownHandler mCountdownHandler = new CountdownHandler();
	private HashMap<Button,Integer> buttonMap = new HashMap<Button,Integer>();
	private Button button0,button1,button2,button3,button4,button5,button6,
		button7,button8,button9,buttonEnter,buttonContinue,buttonClr;
	private TextView numberView; /* 入力数字 */
	private TextView remainingTimeView; /* 残り時間 */
	private TextView expressionView; /* 計算式 */
	private boolean calculating = false; /* false:計算前/true:計算中 */
	private int keytouch = 0;
	private int answer = 0;
	private int creatAnswer = 0;
	private int mRepeat = DEFAULT_REPEAT;
	
	private Long limitTime = new Long(REMAINING_TIME);
	
	private WaitTimeThread mWaitThread = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自動生成されたメソッド・スタブ
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calclayout);
		Bundle extras=getIntent().getExtras();
		if(extras != null){
			result = extras.getBoolean(RESULT);
		}
		/* ボタンの生成 */
		button0 = (Button)findViewById(R.id.button0);
		button0.setOnClickListener(this);
		button1 = (Button)findViewById(R.id.button1);
		button1.setOnClickListener(this);
		button2 = (Button)findViewById(R.id.button2);
		button2.setOnClickListener(this);
		button3 = (Button)findViewById(R.id.button3);
		button3.setOnClickListener(this);
		button4 = (Button)findViewById(R.id.button4);
		button4.setOnClickListener(this);
		button5 = (Button)findViewById(R.id.button5);
		button5.setOnClickListener(this);
		button6 = (Button)findViewById(R.id.button6);
		button6.setOnClickListener(this);
		button7 = (Button)findViewById(R.id.button7);
		button7.setOnClickListener(this);
		button8 = (Button)findViewById(R.id.button8);
		button8.setOnClickListener(this);
		button9 = (Button)findViewById(R.id.button9);
		button9.setOnClickListener(this);
		buttonClr = (Button)findViewById(R.id.button14);
		buttonClr.setOnClickListener(this);
		buttonEnter = (Button)findViewById(R.id.buttonEnter);
		buttonEnter.setOnClickListener(this);
		buttonContinue = (Button)findViewById(R.id.buttonContinue);
		buttonContinue.setOnClickListener(this);
		
		/* ボタンマップの作成 */
		buttonMap.put(button0, 0);
		buttonMap.put(button1, 1);
		buttonMap.put(button2, 2);
		buttonMap.put(button3, 3);
		buttonMap.put(button4, 4);
		buttonMap.put(button5, 5);
		buttonMap.put(button6, 6);
		buttonMap.put(button7, 7);
		buttonMap.put(button8, 8);
		buttonMap.put(button9, 9);
		buttonMap.put(buttonEnter, BUTTON_ENTER);
		buttonMap.put(buttonContinue, BUTTON_CONTINUE);
		buttonMap.put(buttonClr, BUTTON_CLR);
		
		/* カウントダウン表示のTextView取得 */
		remainingTimeView = (TextView)findViewById(R.id.CountDownTextView);
		/* 入力数字のTextView取得 */
		numberView = (TextView)findViewById(R.id.AnswerTextView);
		numberView.setText(null);
		/* 計算式のTextView取得 */
		expressionView = (TextView)findViewById(R.id.ExpressionTextView);
	}
	@Override
	protected void onResume() {
		// TODO 自動生成されたメソッド・スタブ
		showStartDialog();
		createExpression();
		super.onResume();
	}
	@Override
	protected void onPause() {
		// TODO 自動生成されたメソッド・スタブ
		super.onPause();
	}
	private void createExpression(){
		long seed = System.currentTimeMillis(); // 現在時刻のミリ秒
		Random r = new Random(seed);
		int a = Math.abs(r.nextInt()%1000);
		int b = Math.abs(r.nextInt()%100);
		int c = Math.abs(r.nextInt()%1000);
		expressionView.setText(String.format("%d×%d+%d= ",a,b,c));
		creatAnswer = a*b+c;
	}
	
	private void setTextCountDown(long time){
		remainingTimeView.setText(String.format("%d",time/TICK_TIME));
	}
	private void startCountDown(){
		remainingTime = REMAINING_TIME;
		mCountdownHandler.set(REMAINING_TIME);
		setTextCountDown(REMAINING_TIME);
		mCountdownHandler.sleep();
	}
	private void stopCoundDown(){
		mCountdownHandler.cancel();
	}
	class CountdownHandler extends Handler {
		long mRemainingTime = 0;
		public void set(long time){
			mRemainingTime = time;
		}
		public void add(long time){
			mRemainingTime += time;
		}
		@Override
        public void handleMessage(Message msg) {
			switch(msg.what){
				case MSG_COUNTDOWN:
					mRemainingTime-=TICK_TIME;
					setTextCountDown(mRemainingTime);
					if(mRemainingTime <= 0){
						this.cancel();
						sendSoundStopIntent();
					}
					else {
						sleep();
					}
					break;
				case MSG_CANCEL:
					sendSoundStopIntent();
					break;
				default:
					break;
			}
		}
		public void sleep(){
			this.removeMessages(MSG_COUNTDOWN);  
			sendMessageDelayed(obtainMessage(MSG_COUNTDOWN), TICK_TIME); 
		}
		public void cancel(){
			this.removeMessages(MSG_COUNTDOWN);  
			sendMessage(obtainMessage(MSG_CANCEL));
		}
	}
	private void setTextAnswer(int answer){
		numberView.setText(String.format("%d", answer));
	}
	@Override
	public void onClick(View v) {
		// TODO 自動生成されたメソッド・スタブ
		Integer i = buttonMap.get((Button)v);
		int value = i.intValue();
		if(value >= 0 && value <= 9){
			keytouch++;
			answer = answer*10+value;
			setTextAnswer(answer);
			
		}
		else if(i==BUTTON_CLR){
			keytouch = 0;
			answer = 0;
			setTextAnswer(answer);
		}
		else if(i== BUTTON_ENTER){
			CharSequence cs = numberView.getText();
			String as = cs.toString();
			int a = Integer.parseInt(as);
			if(a == creatAnswer){
				showNextDialog("正解です");
				mRepeat--;
			}
			else {
				showNextDialog("間違いです");
			}
			if(mRepeat <= 0){
				showFinishDilag();
			}
		}
		else if(i== BUTTON_CONTINUE){
			remainingTime = REMAINING_TIME/2;
			setTextCountDown(REMAINING_TIME/2);
			startCountDown();
		}	
	}
	private void sendSoundStartIntent(){
		Intent intent = new Intent(timerService.SOUND_PALY);
		startService(intent);
	}
	private void sendSoundStopIntent(){
		Intent intent = new Intent(timerService.SOUND_STOP);
		startService(intent);
	}
	private void sendSnoozeStartIntent(){
		Intent intent = new Intent(timerService.SNOOZE_COUNTDOWON);
		startService(intent);
	}
	private void sendSnoozeCancelIntent(){
		Intent intent = new Intent(timerService.SNOOZE_CANCEL);
		startService(intent);
	}
	private void showStartDialog(){
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		
		//dlg.setTitle("TEST");
		dlg.setMessage("計算を開始します。");
		
		dlg.setPositiveButton("OK", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO 自動生成されたメソッド・スタブ
				sendSoundStopIntent();
				sendSnoozeStartIntent(); /* 「やる」と言っておきながら、やらなかったら困るのでスヌーズをかけておく */
				startCountDown();
			}
		});
		
		dlg.show();
		
		calculating = true; /* 計算中 */
	}
	private void showNextDialog(String text){
		sendSnoozeCancelIntent();
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		//dlg.setTitle("TEST");
		dlg.setMessage(text);
		dlg.setPositiveButton("次です", null);
		dlg.show();
		startCountDown();
		sendSnoozeStartIntent(); /* 問題を解いたところで寝てしまったら困るからスヌーズをかける */
	}
	private void showFinishDilag(){
		sendSnoozeCancelIntent();
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		//dlg.setTitle("TEST");
		dlg.setMessage("おしまいです。¥n起きましたか？");
		dlg.setPositiveButton("OK", null);
		dlg.show();
		stopCoundDown();
		
		calculating = false;
	}
	private void startWaitThread(){
		Long wait = new Long(REMAINING_TIME);
		mWaitThread = new WaitTimeThread();
		mWaitThread.execute(wait);
	}
	/*
	 * 入力待ちのスレッド
	 */
	class WaitTimeThread extends AsyncTask<Long,Void,Void> {
		long mWait;
		@Override
		protected Void doInBackground(Long... params) {
			// TODO 自動生成されたメソッド・スタブ
			mWait = params[0].longValue();
			while(this.isCancelled()==false || mWait >= 0){
				mWait--;
				try {
					Thread.sleep(TICK_TIME);
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
					break;
				}
			}
			return null;
		}

		@Override
		protected void onCancelled() {
			// TODO 自動生成されたメソッド・スタブ
			sendSoundStopIntent();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO 自動生成されたメソッド・スタブ
			sendSoundStartIntent();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO 自動生成されたメソッド・スタブ
			sendSoundStopIntent();
			super.onPreExecute();
		}
		
	}
	/*
	 * 制限時間のスレッド
	 */
	class timeLimit extends AsyncTask<Void,Long,Boolean> {
		
		public void Continue(long time){
			synchronized(limitTime){
				long value = limitTime.longValue();
				value += time;
				limitTime = new Long(value);
			}
			
		}
		@Override
		protected Boolean doInBackground(Void... arg0) {
			// TODO 自動生成されたメソッド・スタブ
			long value;
			Boolean rt = new Boolean(true);
			while(this.isCancelled() == false){
				try {
					Thread.sleep(TICK_TIME);
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				synchronized(limitTime){
					value = limitTime.longValue();
					value -= TICK_TIME;
					limitTime = new Long(value);
				}
					if(value <= 0){
						rt = false;
						break;
					}
					publishProgress(new Long(value));
				
				
			}
			return rt;
		}

		@Override
		protected void onCancelled() {
			// TODO 自動生成されたメソッド・スタブ
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO 自動生成されたメソッド・スタブ
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO 自動生成されたメソッド・スタブ
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Long... values) {
			// TODO 自動生成されたメソッド・スタブ
			setTextCountDown(values[0].longValue());
			super.onProgressUpdate(values);
		}
		
	}

}
