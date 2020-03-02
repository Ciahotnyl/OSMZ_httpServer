package com.example.httpserver2;

import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HttpServerActivity extends Activity implements OnClickListener{

	private SocketServer s;
	static TextView tv;
	static TextView allView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_http_server);

		tv = (TextView) findViewById(R.id.textThreads);
		tv.setMovementMethod(new ScrollingMovementMethod());

		allView = (TextView) findViewById(R.id.textAll);

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
				Log.d("SRV", requestMessage);
			}
		}
	};

}