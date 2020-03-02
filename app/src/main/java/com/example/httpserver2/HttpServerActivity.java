package com.example.httpserver2;

import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HttpServerActivity extends Activity implements OnClickListener{

	private SocketServer s;
	static TextView tv;
	static TextView allView;
	private int TransferredCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_http_server);

		tv = (TextView) findViewById(R.id.textThreads);
		tv.setMovementMethod(new ScrollingMovementMethod());

		allView = (TextView) findViewById(R.id.textAll);
		allView.setText("\n Total: "+TransferredCount);

		Button btn1 = (Button)findViewById(R.id.button1);
		Button btn2 = (Button)findViewById(R.id.button2);

		btn1.setOnClickListener(this);
		btn2.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.button1) {
			s = new SocketServer(myHandler);
			s.start();
		}
		if (v.getId() == R.id.button2) {
			s.close();
			try {
				s.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public Handler myHandler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg) {
			String requestMessage = msg.getData().getString("list");
			if(requestMessage != null){
				tv.append(requestMessage + "\n \n");

				String pom = requestMessage.substring(requestMessage.lastIndexOf(":")+ 1);
				String CountString = pom.substring(1);
				boolean numeric = true;
				try {
					Integer.parseInt(CountString);
				} catch(NumberFormatException e) {
					numeric = false;
				}

				if(numeric){
					int addingSize = Integer.parseInt(CountString);
					TransferredCount += addingSize;
					allView.setText("\n Total: " + TransferredCount);
				}
			}
		}
	};
}