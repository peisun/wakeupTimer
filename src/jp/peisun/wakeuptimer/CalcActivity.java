package jp.peisun.wakeuptimer;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Button;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;

import java.util.*;

public class CalcActivity extends Activity implements OnClickListener {
	private final String TAG = "CalcActivity";
	private boolean preview = false;
	public static final String REPEAT = "repeat";
	public static final String PREVIEW = "preview";
	public static final String LIMITTIME = "limittime";

	private static final int TICK_TIME = 1000;

	private static final int MSG_COUNTDOWN = 1;
	private static final int MSG_CANCEL = 2;
	private static final int MSG_FINISH = 3;
	private final int BUTTON_CLR = 0x0c;
	private final int BUTTON_ENTER = 0x0e;
	private final int BUTTON_CONTINUE = 0x0f;
	private final int TERM_1_SIZE = 1000;
	private final int TERM_2_SIZE = 10;
	private final int TERM_3_SIZE = 1000;

	private int mTermStepUpLine = 0;
	private int mTermStepSize = 100;


	private final int FINISH_DIALOG_ID = 1;
	private final int NEXT_CORRECT_DIALOG_ID = 2;
	private final int NEXT_DISTRACTER_DIALOG_ID = 3;
	private final int START_DIALOG_ID = 4;
	private final int PREVIEWFINISH_CORRECT_DIALOG_ID = 5;
	private final int PREVIEWFINISH_MISTAKE_DIALOG_ID = 6;

	private CountdownHandler mCountdownHandler = new CountdownHandler();
	private HashMap<Button,Integer> buttonMap = new HashMap<Button,Integer>();
	private Button button0,button1,button2,button3,button4,button5,button6,
	button7,button8,button9,buttonEnter,buttonContinue,buttonClr;
	private TextView numberView; /* 入力数字 */
	private TextView remainingTimeView; /* 残り時間 */
	private TextView expressionView; /* 計算式 */
	private int keytouch = 0;
	private int answer = 0;
	private int creatAnswer = 0;
	private int mRepeat = 0;
	private long mLimitTime = 0;
	private boolean isCalc = true;
	private int mAnswerLength = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自動生成されたメソッド・スタブ
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calclayout);

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

		buttonContinue.setText(getString(R.string.enlargement_text));

		/* カウントダウン表示のTextView取得 */
		remainingTimeView = (TextView)findViewById(R.id.CountDownTextView);
		/* 入力数字のTextView取得 */
		numberView = (TextView)findViewById(R.id.AnswerTextView);
		numberView.setText(null);
		/* 計算式のTextView取得 */
		expressionView = (TextView)findViewById(R.id.ExpressionTextView);

		Bundle extras=getIntent().getExtras();
		if(extras != null){
			preview = extras.getBoolean(PREVIEW);

			mRepeat = extras.getInt(REPEAT);
			mLimitTime = extras.getLong(LIMITTIME);
		}
		else {
			// 発生しないとは思うが、extrasがnullの場合のテスト
			preview = false;
			mRepeat = Integer.parseInt(getString(R.string.repeatDefaultValue));
			mLimitTime = Long.parseLong(getString(R.string.limittimeDefaultValue));
		}
		

		// 設問数が-1のときはランダム
		if(mRepeat <= 0){
			mRepeat = randomRepeat(mRepeat);
		}
		// 計算問題の桁数を増やす境界条件となる値を設定
		if(mRepeat % 2 == 1){
			mTermStepUpLine = (mRepeat+1)/2;
		}
		else {
			mTermStepUpLine = mRepeat/2;
		}
		Log.d(TAG,"preview "+ preview);
		Log.d(TAG,"mRepeat " + mRepeat);
		Log.d(TAG,"mLimitTIme "+ mLimitTime);
		
		showDialog(START_DIALOG_ID);
		createExpression();
		setTextCountDown(mLimitTime);

	}
	@Override
	protected void onResume() {
		// TODO 自動生成されたメソッド・スタブ

		super.onResume();
	}
	@Override
	protected void onPause() {
		// TODO 自動生成されたメソッド・スタブ
		super.onPause();
	}
	private int randomRepeat(int repeat){
		int rpt = 0;
		// ランダムは10問より多く、19問以下
		long seed = System.currentTimeMillis(); // 現在時刻のミリ秒
		Random r = new Random(seed);
		do {
			rpt = Math.abs(r.nextInt()%20);
		}while(rpt < 10);
		return rpt;
	}
	private void createExpression(){
		long seed = System.currentTimeMillis(); // 現在時刻のミリ秒
		Random r = new Random(seed);
		int a,b,c;
		// 0は出現しないようにする
		do {
			a = Math.abs(r.nextInt() % mTermStepSize);
		}while(a == 0);
		do {
			b = Math.abs(r.nextInt() % (mTermStepSize/10));
		}while(b == 0);
		do {

			c = Math.abs(r.nextInt() % mTermStepSize);
		}while(c == 0);
		// 足し算だけではつまらないので引き算を入れる
		int plus = Math.abs(r.nextInt() % 2);
		expressionView = (TextView)findViewById(R.id.ExpressionTextView);
		if((plus == 0) && (a*b > c)){
			expressionView.setText(String.format("%d×%d-%d= ",a,b,c));
			creatAnswer = a*b-c;
		}
		else {
			expressionView.setText(String.format("%d×%d+%d= ",a,b,c));
			creatAnswer = a*b+c;

		}
		Log.d(TAG,"Answer = " + creatAnswer);
		/* 解答欄に0を入れる */
		numberView = (TextView)findViewById(R.id.AnswerTextView);
		numberView.setText("0");

		//　設問数の半分くらいで、桁を上げる
		// 3桁以上は面倒なのでやらない
		if(mTermStepUpLine == mRepeat){
			mTermStepSize *= 10;
		}

		// 解答の文字列長を保存しておく
		mAnswerLength = Integer.toString(creatAnswer).length();
	}

	private void setTextCountDown(long time){
		remainingTimeView.setText(String.format("%d",time/TICK_TIME));
	}
	private long getCount(){
		return mCountdownHandler.get();
	}
	private void startCountDown(){
		mCountdownHandler.set(mLimitTime);
		setTextCountDown(mLimitTime);
		mCountdownHandler.sleep();
	}
	private void stopCountDown(){
		mCountdownHandler.cancel();
	}
	// このHandlerは、activityがバックグランドに遷移しても生きている模様
	class CountdownHandler extends Handler {
		private volatile long mRemainingTime = 0;
		public void set(long time){
			mRemainingTime = time;
		}
		public long get(){
			return mRemainingTime;
		}
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_COUNTDOWN:
				mRemainingTime-=TICK_TIME;
				setTextCountDown(mRemainingTime);
				if(mRemainingTime <= 0){
					sendSoundStartIntent();
				}
				else {
					sleep();
				}
				break;
			case MSG_CANCEL:
				sendSoundStopIntent();
				break;
			case MSG_FINISH:
				finish();
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
		public void complete(){
			this.removeMessages(MSG_COUNTDOWN);  
			sendMessage(obtainMessage(MSG_FINISH));
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
			// 必要以上のキー入力を受け付けない
			if(keytouch <= mAnswerLength){
				answer = answer*10+value;
				setTextAnswer(answer);
			}

		}
		else if(i==BUTTON_CLR){
			keytouch = 0;
			answer = 0;
			setTextAnswer(answer);
		}
		else if(i== BUTTON_ENTER){
			stopCountDown();
			
			CharSequence cs = numberView.getText();
			String as = cs.toString();
			int a;
			if(as.length()> 0){
				a = Integer.parseInt(as);
			}
			else {
				a = -1;
			}

			keytouch = 0;
			answer = 0;

			if(a == creatAnswer){
				mRepeat--;
				if(preview){
					showDialog(PREVIEWFINISH_CORRECT_DIALOG_ID);
				}
				else {
					showDialog(NEXT_CORRECT_DIALOG_ID);
				}

			}
			else {
				if(preview){
					showDialog(PREVIEWFINISH_MISTAKE_DIALOG_ID);
				}
				else {
					showDialog(NEXT_DISTRACTER_DIALOG_ID);
				}
			}

			if(mRepeat <= 0 && preview == false){
				showDialog(FINISH_DIALOG_ID);
			}
		}
		else if(i== BUTTON_CONTINUE){
			long l = getCount();
			if(l < mLimitTime/2){
				mLimitTime = l + mLimitTime/2;
			}
			sendSoundStopIntent();
			stopCountDown();
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
	// ここで言うスヌーズは計算を拒否したことを考えている
	// よって、他のActivityに移行する場合を以下と考えている
	// 1.backキーを押される
	// 2.homeキーを押される
	// 3.他のアプリケーションによるバックグランドに遷移する場合
	// これらの場合は、スヌーズをかけて、Activityをfinish()する
	private void sendSnoozeStartIntent(){
		Intent intent = new Intent(timerService.SNOOZE_START);
		startService(intent);
	}
	private void sendSnoozeCancelIntent(){
		Intent intent = new Intent(timerService.SNOOZE_CANCEL);
		startService(intent);
	}
	private Dialog createStartDialog(){
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);

		dlg.setMessage(getString(R.string.start_message));

		dlg.setPositiveButton("OK", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO 自動生成されたメソッド・スタブ

				if(preview == false){
					sendSoundStopIntent();
					// 「やる」と言っておきながら、やらないで放っておく場合は
					// カウントダウンでアラームがなるので
					// スヌーズをかける必要はない
				}

				startCountDown();
				arg0.dismiss();
			}

		});
		// backキー対策
		// キャンセルはできないこととする
		dlg.setCancelable(false);

		return dlg.create();


	}
	private Dialog createNextDialog(String text){
		String positive_text = null;
		sendSnoozeCancelIntent();

		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setMessage(text);

		positive_text = getString(R.string.next_message);
		dlg.setMessage(text);

		dlg.setPositiveButton(positive_text, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO 自動生成されたメソッド・スタブ
				createExpression();
				startCountDown();
				dialog.dismiss();
			}

		});
		dlg.setOnCancelListener(new DialogInterface.OnCancelListener(){

			@Override
			public void onCancel(DialogInterface dialog) {
				// キャンセル(backキー)されても計算を継続させる
				createExpression();
				startCountDown();
			}

		});
		return dlg.create();


	}
	private Dialog createPreviewFinishDialog(String text){
		String positive_text = null;

		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setMessage(text);

		positive_text = "OK";
		dlg.setTitle(getString(R.string.preview_dialog_title));
		dlg.setMessage(text);


		dlg.setPositiveButton(positive_text, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO 自動生成されたメソッド・スタブ
				stopCountDown();
				isCalc = false;
				finish(); /* プレビューなら終わる */
			}

		});
		dlg.setOnCancelListener(new DialogInterface.OnCancelListener(){

			@Override
			public void onCancel(DialogInterface dialog) {
				stopCountDown();
				isCalc = false;
				finish(); /* プレビューなら終わる */
			}

		});
		return dlg.create();


	}

	private Dialog createFinishDialog(){
		sendSnoozeCancelIntent();
		AlertDialog.Builder mFinishdlg = new AlertDialog.Builder(this);
		//dlg.setTitle("TEST");
		mFinishdlg.setMessage(getString(R.string.wakeup_message));
		mFinishdlg.setPositiveButton("OK", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO 自動生成されたメソッド・スタブ
				stopCountDown();
				if(preview == false){
					Intent intent = new Intent(timerService.ACTION_FINISH);
					startService(intent);
				}
				else {
					isCalc = false;
				}
				dialog.dismiss();
				finish();
			}

		});
		mFinishdlg.setOnCancelListener(new DialogInterface.OnCancelListener(){

			@Override
			public void onCancel(DialogInterface dialog) {

				Intent intent = new Intent(timerService.ACTION_FINISH);
				startService(intent);
				stopCountDown();
				isCalc = false;
				finish(); /* プレビューなら終わる */
			}

		});
		return mFinishdlg.create();

	}
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO 自動生成されたメソッド・スタブ
		Dialog dlg = super.onCreateDialog(id);
		String title_string;
		switch(id){
		case START_DIALOG_ID:
			dlg = createStartDialog();
			break;
		case NEXT_CORRECT_DIALOG_ID:
			title_string = getString(R.string.answer_currect);
			dlg = createNextDialog(title_string);
			break;
		case NEXT_DISTRACTER_DIALOG_ID:
			title_string = getString(R.string.answer_mistake);
			dlg = createNextDialog(title_string);
			break;	
		case FINISH_DIALOG_ID:
			dlg = createFinishDialog();
			break;
		case PREVIEWFINISH_CORRECT_DIALOG_ID:
			title_string = getString(R.string.answer_currect);
			dlg = createPreviewFinishDialog(title_string);
			break;
		case PREVIEWFINISH_MISTAKE_DIALOG_ID:
			title_string = getString(R.string.answer_mistake);
			dlg = createPreviewFinishDialog(title_string);
			break;
		}
		return dlg;
	}
	@Override
	public void onBackPressed() {
		// TODO 自動生成されたメソッド・スタブ
		// Backキーが押されたらActivityは死ぬ
		// 計算を拒否したことになり、スヌーズをかける
		if(preview){
			// プレビューの場合は何もしない
		}
		else if(isCalc == true){
			// 計算中だったらスヌーズをかける
			stopCountDown();	
			sendSnoozeStartIntent();
		}
		else {
			// 計算が終わっていたら
			stopCountDown();
			sendSnoozeCancelIntent();
		}
		finish();
		super.onBackPressed();
	}
	@Override
	protected void onUserLeaveHint() {
		// TODO 自動生成されたメソッド・スタブ
		// Activityが他のアプリにより、バックグランドに入ろうとする
		// この場合も計算を拒否したこととなり、スヌーズをかける
		if(preview){
			//プレビューの場合は何もしない
		}
		else if(isCalc == true){
			// 計算中だったらスヌーズをかける
			stopCountDown();	
			sendSnoozeStartIntent();
		}
		else {
			// 計算が終わっていたら
			stopCountDown();
			sendSnoozeCancelIntent();
		}
		finish();
		super.onUserLeaveHint();
	}
	@Override
	protected void onDestroy() {
		// TODO 自動生成されたメソッド・スタブ

		super.onDestroy();
	}


}
