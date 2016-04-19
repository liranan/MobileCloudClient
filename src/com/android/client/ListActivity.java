package com.android.client;

import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.socket.utils.StreamTool;
import com.android.upload.R;

@SuppressLint("ShowToast")
public class ListActivity extends Activity {
	private ListView lv;
	private String operation = null;
	private String ip = null;
	final static int MENU_00 = Menu.FIRST;
	private Handler handler = new Handler() {
		@SuppressLint("NewApi")
		@Override
		public void handleMessage(Message msg) {
			operation = msg.getData().getString("order");
			// String response = msg.getData().getString("response");
			// if (!(response == null)) {
			// Toast.makeText(ListActivity.this, response, 1).show();
			// }
			String[] items = operation.split(";");
			// Choose(items);
			if (items[0].equals("null")) {
				Toast.makeText(ListActivity.this, "The Folder is Empty!", 1)
						.show();
			} else {
				lv.setAdapter(new ArrayAdapter<String>(ListActivity.this,
						android.R.layout.simple_list_item_1, items));
				lv.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						ListView listView = (ListView) arg0;
						final String ItemName = (String) listView
								.getItemAtPosition(arg2);
						if (ItemName.contains(".")) {
							new AlertDialog.Builder(ListActivity.this)
									.setTitle("��ѡ��")
									.setItems(
											new String[] { "���ص�����", "�ڷ�������ɾ��",
													"�����Լ���" },
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface arg0,
														int arg1) {
													// TODO Auto-generated
													// method
													// stub
													if (arg1 == 0) {
														Intent intent = new Intent();
														intent.setClass(
																ListActivity.this,
																DownloadActivity.class);
														intent.putExtra(
																"filename",
																ItemName);
														intent.putExtra("ip",
																ip);
														startActivity(intent);
													} else if (arg1 == 1) {
														Intent intent = new Intent();
														intent.setClass(
																ListActivity.this,
																DeleteActivity.class);
														intent.putExtra(
																"filename",
																ItemName);
														intent.putExtra("ip",
																ip);
														startActivity(intent);
														// DeleteFile(ip,
														// ItemName);
														// Intent intent = new
														// Intent();
														// intent.setClass(ListActivity.this,
														// ListActivity.class);
														// intent.putExtra("ip",
														// ip);
														// startActivity(intent);
													} else {
														Intent intent = new Intent();
														intent.setClass(
																ListActivity.this,
																VerifyFile.class);
														intent.putExtra(
																"filename",
																ItemName);
														intent.putExtra("ip",
																ip);
														startActivity(intent);
													}

												}
											}).create().show();
							// �����˵���ѡ�����ػ�ɾ���ļ�
						} else if (!ItemName.contains(".")) {
							// �����˵���ѡ��򿪻�ɾ���ļ���
							new AlertDialog.Builder(ListActivity.this)
									.setTitle("��ѡ��")
									.setItems(
											new String[] { "���ļ���", "�ڷ�������ɾ��" },
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface arg0,
														int arg1) {
													// TODO Auto-generated
													// method
													// stub
													if (arg1 == 0) {
														Intent intent = new Intent();
														intent.setClass(
																ListActivity.this,
																ListActivity.class);
														intent.putExtra(
																"filename",
																ItemName);
														intent.putExtra("ip",
																ip);
														startActivity(intent);
													} else {
														Intent intent = new Intent();
														intent.setClass(
																ListActivity.this,
																DeleteActivity.class);
														intent.putExtra(
																"filename",
																ItemName);
														intent.putExtra("ip",
																ip);
														startActivity(intent);
														// DeleteFile(ip,
														// ItemName);
													}
												}
											}).create().show();
						}
					}
				});
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filelist);
		lv = (ListView) this.findViewById(R.id.listview);
		Intent intent = getIntent();
		ip = intent.getStringExtra("ip");
		String Dir = intent.getStringExtra("filename");
		getDirectory(ip, Dir);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_00, 0, "�ϴ������ļ�");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_00:
			Intent intent = new Intent();
			intent.setClass(ListActivity.this, UploadActivity.class);
			intent.putExtra("ip", ip);
			startActivity(intent);
			return true;
		default:
			return false;
		}

	}

	public void getDirectory(final String ip, final String Dir) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Socket socket = new Socket(ip, 7878);
					OutputStream outStream = socket.getOutputStream();
					PushbackInputStream inStream = new PushbackInputStream(
							socket.getInputStream());
					String head = "getlist:" + Dir + "\r\n";
					outStream.write(head.getBytes("GBK"));
					outStream.flush();
					String response = StreamTool.readLine(inStream);
					// String[] items = response.split(";");
					Message msg = new Message();
					msg.getData().putString("order", response);
					handler.sendMessage(msg);
					outStream.close();
					inStream.close();
					socket.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	// public void DeleteFile(final String ip, final String filename) {
	// new Thread(new Runnable() {
	// @Override
	// public void run() {
	// try {
	// Socket socket = new Socket(ip, 7878);
	// OutputStream outStream = socket.getOutputStream();
	// outStream.write(("delete:" + filename + "\r\n")
	// .getBytes("GBK"));
	// outStream.flush();
	// PushbackInputStream inStream = new PushbackInputStream(
	// socket.getInputStream());
	// String response = StreamTool.readLine(inStream);
	// System.out.println(response);
	// outStream.close();
	// inStream.close();
	// socket.close();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }).start();
	// }
}
