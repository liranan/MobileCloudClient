package com.android.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;

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

public class UploadActivity extends Activity {
	private EditText filenameText;
	private EditText uploadPathText;
	private TextView resulView;
	private ProgressBar uploadbar;
	private boolean start = true;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int length = msg.getData().getInt("size");
			uploadbar.setProgress(length);
			float num = (float) uploadbar.getProgress()
					/ (float) uploadbar.getMax();
			int result = (int) (num * 100);
			resulView.setText(result + "%");
			if (uploadbar.getProgress() == uploadbar.getMax()) {
				Toast.makeText(UploadActivity.this, R.string.success, 1).show();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload);
		Intent intent = getIntent();
		final String ip = intent.getStringExtra("ip");

		filenameText = (EditText) this.findViewById(R.id.filename);
		uploadPathText = (EditText) this.findViewById(R.id.uppath);
		uploadbar = (ProgressBar) this.findViewById(R.id.uploadbar);
		resulView = (TextView) this.findViewById(R.id.result);
		Button button = (Button) this.findViewById(R.id.upbutton);
		Button Fbutton = (Button) this.findViewById(R.id.upfinish);
//		Button button1 = (Button) this.findViewById(R.id.stop);
//		button1.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				start = false;
//
//			}
//		});
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				start = true;
				String filename = filenameText.getText().toString();
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					File uploadFile = new File(Environment
							.getExternalStorageDirectory(), filename);
					if (uploadFile.exists()) {
						uploadFile(uploadFile,ip);
					} else {
						Toast.makeText(UploadActivity.this,
								filename + R.string.filenotexist, 1).show();
					}
				} else {
					Toast.makeText(UploadActivity.this, R.string.sdcarderror, 1)
							.show();
				}
			}
		});
		
		Fbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(UploadActivity.this, ListActivity.class);
				intent.putExtra("ip", ip);
				startActivity(intent);
			}
		});
	}

	/**
	 * 上传文件
	 * 
	 * @param uploadFile
	 */
	private void uploadFile(final File uploadFile,final String ip) {
		new Thread(new Runnable() {
			@SuppressLint("NewApi")
			@Override
			public void run() {
				try {
					String uploadPath = uploadPathText.getText().toString();
					uploadbar.setMax((int) uploadFile.length());
					String head ="upload:"+ "上传文件长度（KB）=" + uploadFile.length()
							+ ";filename=" + uploadFile.getName()
							+ ";uploadPath=" + uploadPath + "\r\n";
					Socket socket = new Socket(ip, 7878);
					OutputStream outStream = socket.getOutputStream();
					outStream.write(head.getBytes("GBK"));
					
					PushbackInputStream inStream = new PushbackInputStream(
							socket.getInputStream());
					String response = StreamTool.readLine(inStream);
					String position = response.substring(response
							.indexOf("=") + 1);
					RandomAccessFile fileOutStream = new RandomAccessFile(
							uploadFile, "r");
					fileOutStream.seek(Integer.valueOf(position));
					byte[] buffer = new byte[1024];
					int len = -1;
					int length = Integer.valueOf(position);
					while (start && (len = fileOutStream.read(buffer)) != -1) {
						outStream.write(buffer, 0, len);
						length += len;
						Message msg = new Message();
						msg.getData().putInt("size", length);
						handler.sendMessage(msg);
					}
					fileOutStream.close();
					outStream.close();
					inStream.close();
					socket.close();
					
					String HashCode = getFileHash(uploadFile,"SHA-1");
					System.out.println("HashCode:"+HashCode);
					File saveFile= new File("/sdcard/HashCodeFileForVerify.txt");
					// 如果文件不存在，则创建一个文件
					if (!saveFile.exists()) {
						try {
							saveFile.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					saveFile.setWritable(Boolean.TRUE);
					RandomAccessFile OutStream = new RandomAccessFile(
							saveFile, "rwd");
					long filelength = OutStream.length();
					OutStream.seek(filelength);
					OutStream.writeBytes("\n"+uploadFile.getName().toString()+" "+HashCode);
					OutStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public static String getFileHash(File file, String mothed) {
		if (!file.isFile()) {
			System.out.println(file + "不是文件!");
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[8192];
		int len;
		try {
			digest = MessageDigest.getInstance(mothed);
			in = new FileInputStream(file);
			while ((len = in.read(buffer)) != -1) {
				digest.update(buffer, 0, len);
			}
			BigInteger bigInt = new BigInteger(1, digest.digest());
			return bigInt.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}