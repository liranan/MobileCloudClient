package com.android.client;

import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.socket.utils.StreamTool;
import com.android.upload.R;

public class DeleteActivity extends Activity {
	private String ip;
	private TextView deleteView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.delete);

		deleteView = (TextView) this.findViewById(R.id.deletView);

		Intent intent = getIntent();
		String filename = intent.getStringExtra("filename");
		deleteView.setText("Delete " + filename);
		ip = intent.getStringExtra("ip");
		DeleteFile(ip, filename);
		Button button = (Button) this.findViewById(R.id.debutton);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(DeleteActivity.this, "Deleted Successfully!", 1)
				.show();
				Intent intent = new Intent();
				intent.setClass(DeleteActivity.this, ListActivity.class);
				intent.putExtra("ip", ip);
				startActivity(intent);
			}
		});
	}

	public void DeleteFile(final String ip, final String filename) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Socket socket = new Socket(ip, 7878);
					OutputStream outStream = socket.getOutputStream();
					outStream.write(("delete:" + filename + "\r\n")
							.getBytes("GBK"));
					outStream.flush();
					System.out.println("delete:" + filename);
					PushbackInputStream inStream = new PushbackInputStream(
							socket.getInputStream());
					String response = StreamTool.readLine(inStream);
					outStream.close();
					inStream.close();
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}