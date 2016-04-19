package com.android.client;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.socket.utils.StreamTool;
import com.android.upload.R;

public class DownloadActivity extends Activity {
	private TextView resulView;
	private TextView filenameText;
	private EditText saveDirText;
	private ProgressBar downloadbar;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int length = msg.getData().getInt("size");
			downloadbar.setProgress(length);
			float num = (float) downloadbar.getProgress()
					/ (float) downloadbar.getMax();
			int result = Math.round(num * 100);
			resulView.setText(result + "%");
			if (result == 100) {
				Toast.makeText(DownloadActivity.this, R.string.downsuccess, 1)
						.show();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download);
		Intent intent = getIntent();
		final String ip = intent.getStringExtra("ip");
		final String filename = intent.getStringExtra("filename");
		// final String path = filename.substring(0, filename.lastIndexOf("/"));
		// final String name = filename.substring(filename.lastIndexOf("/") +
		// 1);

		filenameText = (TextView) this.findViewById(R.id.downfilename);
		filenameText.setText("下载文件:"
				+ filename.substring(filename.lastIndexOf("/") + 1));
		downloadbar = (ProgressBar) this.findViewById(R.id.downloadbar);
		resulView = (TextView) this.findViewById(R.id.downresult);
		saveDirText = (EditText) this.findViewById(R.id.saveDir);
		Button button = (Button) this.findViewById(R.id.downbutton);
		Button Fbutton = (Button) this.findViewById(R.id.downfinish);
		// Button button1 = (Button) this.findViewById(R.id.stop);
		// button1.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// start = false;
		//
		// }
		// });
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					Environment.getExternalStorageDirectory();
					// File saveDir =
					// Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
					/*
					 * getExternalStoragePublicDirectiory(Environment.
					 * DIRECTORY_MUSIC);
					 */
					// getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
					String saveDir = saveDirText.getText().toString();
					download(filename, saveDir, ip);
				} else {
					Toast.makeText(getApplicationContext(), 
							R.string.sdcarderror, Toast.LENGTH_LONG).show();
				}
			}
		});
		
		Fbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(DownloadActivity.this, ListActivity.class);
				intent.putExtra("ip", ip);
				startActivity(intent);
			}
		});
	}

	/**
	 * 下载文件
	 * 
	 * @param uploadFile
	 */
	@SuppressLint("NewApi")
	private void download(final String filename, final String saveDir,
			final String ip) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String name = filename.substring(filename.lastIndexOf("/") + 1);
					System.out.println("name:" + name);
					String head = "download:" + filename + "\r\n";
					Socket socket = new Socket(ip, 7878);
					OutputStream outStream = socket.getOutputStream();
					outStream.write(head.getBytes("GBK"));

					// String response = StreamTool.readLine(inStream);
					// String position = response.substring(response
					// .indexOf("=") + 1);
//					File filepath = new File(saveDir);
//					System.out.println("saveDir:" + saveDir);
//					if (!filepath.exists())
//						filepath.mkdirs();
//					File saveFile = new File(saveDir + name);

					// File file=this.getFilesDir();//打开私有目录
					File file = new File(saveDir);
					if(!file.exists()){
						file.mkdirs();
					}
					String path = file.getAbsolutePath();// 获取路径
					File saveFile= new File(path, name);
					// 如果文件不存在，则创建一个文件
					if (!saveFile.exists()) {
						try {
							saveFile.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					saveFile.setWritable(Boolean.TRUE);
					PushbackInputStream inStream = new PushbackInputStream(
							socket.getInputStream());
					String response = StreamTool.readLine(inStream);
					System.out.println("response:"+response);
					downloadbar.setMax(Integer.parseInt(response));
					RandomAccessFile fileOutStream = new RandomAccessFile(
							saveFile, "rwd");
					// FileOutputStream fileOutStream = new
					// FileOutputStream(saveFile);
					System.out.println(inStream.available());
					int length = 0;
					byte[] buffer = new byte[1024];
					int len = -1;
					while ((len = inStream.read(buffer)) != -1) {
						System.out.println("len:"+len);
						fileOutStream.write(buffer, 0, len);
						length += len;
						Message msg = new Message();
						msg.getData().putInt("size", length);
						handler.sendMessage(msg);
					}
					fileOutStream.close();
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