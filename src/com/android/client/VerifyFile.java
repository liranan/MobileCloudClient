package com.android.client;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.socket.utils.StreamTool;
import com.android.upload.R;

public class VerifyFile extends Activity {
	private String ip;
	private TextView verifyfilenameView;
	private TextView verifyfileresultView;
	private TextView verifytimeView;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String result = msg.getData().getString("result");
			Long time = msg.getData().getLong("time");
			verifytimeView.setText("验证时间:"+(double)(time)/(double)1000+"S");
			final String[] item = result.split(":");
			if (item[0].equals("1")) {
				verifyfileresultView.setText("完整性检验成功：文件完整!");
			} else if (item[0].equals("-1")) {
				verifyfileresultView.setText("完整性检验失败：本地没有该文件上传记录!");
			} else {
				verifyfileresultView.setText("完整性检验成功：文件损坏!");
				new AlertDialog.Builder(VerifyFile.this)
						.setTitle("文件损坏，是否删除？")
						.setItems(new String[] { "是", "否" },
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// TODO Auto-generated
										// method
										// stub
										if (arg1 == 0) {
											Intent intent = new Intent();
											intent.setClass(VerifyFile.this,
													DeleteActivity.class);
											intent.putExtra("filename", item[1]);
											intent.putExtra("ip", ip);
											startActivity(intent);
										}
									}
								}).create().show();
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.verify);

		verifyfilenameView = (TextView) this.findViewById(R.id.verifyfilename);
		verifyfileresultView = (TextView) this.findViewById(R.id.verifyresult);
		verifytimeView = (TextView) this.findViewById(R.id.verifytime);

		Intent intent = getIntent();
		final String filename = intent.getStringExtra("filename");
		verifyfilenameView.setText("完整性验证文件： "
				+ filename.substring(filename.lastIndexOf("/") + 1));
		ip = intent.getStringExtra("ip");

		Button Vbutton = (Button) this.findViewById(R.id.verifybutton);
		Button Fbutton = (Button) this.findViewById(R.id.verifyfinish);
		Vbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				VerifyFile(ip, filename);
			}
		});

		Fbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(VerifyFile.this, ListActivity.class);
				intent.putExtra("ip", ip);
				startActivity(intent);
			}
		});
	}

	public void VerifyFile(final String ip, final String filename) {
		new Thread(new Runnable() {
			@SuppressWarnings("null")
			@Override
			public void run() {
				try {
					long time1 = System.currentTimeMillis();
					Socket socket = new Socket(ip, 7878);
					OutputStream outStream = socket.getOutputStream();
					outStream.write(("verify:" + filename + "\r\n")
							.getBytes("GBK"));
					outStream.flush();
					System.out.println("Verify:" + filename);
					PushbackInputStream inStream = new PushbackInputStream(
							socket.getInputStream());
					String response = StreamTool.readLine(inStream);
					// response==HashCode?
					File Vfile = new File("/sdcard/HashCodeFileForVerify.txt");
					if (!Vfile.exists()) {
						Message msg = new Message();
						msg.getData().putString("result", "-1:" + filename);
						handler.sendMessage(msg);
					}
					FileInputStream fis = new FileInputStream(Vfile);
					DataInputStream dataIn = new DataInputStream(fis);
					// String[][] FileHash = null;
					String temp = null;
					// int i = 0;
					Boolean fig = true;
					Boolean fig2 = true;
					String result = null;
					while ((temp = dataIn.readLine()) != null && fig) {
						String[] item = temp.split(" ");
						// FileHash[i][1] = item[0];
						// FileHash[i][2] = item[1];
						// i++;
						if (item[0].equals(filename.substring(filename
								.lastIndexOf("/") + 1))) {
							if (item[1].equals(response)) {
								fig = false;
								result = "1";
							} else {
								fig2 = false;
								result = "0";
							}
						}
					}
					long time2 = System.currentTimeMillis();
					if (fig == true && fig2 == true) {
						Message msg = new Message();
						msg.getData().putString("result", "-1:" + filename);
						msg.getData().putLong("time",time2-time1);
						handler.sendMessage(msg);
					} else {
						Message msg = new Message();
						msg.getData().putString("result",
								result + ":" + filename);
						msg.getData().putLong("time",time2-time1);
						handler.sendMessage(msg);
					}

					dataIn.close();
					fis.close();

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