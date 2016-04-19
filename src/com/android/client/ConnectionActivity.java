package com.android.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.upload.R;

public class ConnectionActivity extends Activity {
	private EditText IP;
	private Button connectbutton;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect);

		IP = (EditText) this.findViewById(R.id.IP);
		connectbutton = (Button) this.findViewById(R.id.connectbutton);
		connectbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String ip = IP.getText().toString();
				if (ip == null) {
					Toast.makeText(ConnectionActivity.this, "IP地址不能为空！请重新输入。",
							1).show();
				} else {
					Intent intent = new Intent();
					intent.setClass(ConnectionActivity.this, ListActivity.class);
					intent.putExtra("ip", ip);
					startActivity(intent);
				}
			}
		});
	}
}