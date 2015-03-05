package com.gtja.tonywang.lockpattern.activity;

import com.gtja.tonywang.lockpattern.R;
import com.gtja.tonywang.lockpattern.util.Md5Utils;
import com.gtja.tonywang.lockpattern.widget.LocusPassWordView;
import com.gtja.tonywang.lockpattern.widget.LocusPassWordView.OnCompleteListener;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private LocusPassWordView mPwdView;
	private TextView label;
	private Context mContext;
	private SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = getApplicationContext();
		sp = PreferenceManager.getDefaultSharedPreferences(this);

		label = (TextView) this.findViewById(R.id.multi_tv_token_time_hint);
		if (sp.getString("password", "").length() == 0) {
			label.setText("请设置密码");
		} else {
			label.setText("请输入密码");
		}

		mPwdView = (LocusPassWordView) this.findViewById(R.id.mPassWordView);
		mPwdView.setOnCompleteListener(new OnCompleteListener() {
			@Override
			public void onComplete(String mPassword) {
				String pwd = sp.getString("password", "");
				Md5Utils md5 = new Md5Utils();
				// boolean passed = false;

				if (pwd.length() == 0) {
					// 设置密码
					sp.edit().putString("password", md5.toMd5(mPassword, ""))
							.commit();
					label.setText("请输入密码");
					Toast.makeText(mContext, "密码设置成功!", Toast.LENGTH_LONG)
							.show();
					mPwdView.clearPassword();
					return;
				} else {
					// 校验密码
					String encodedPwd = md5.toMd5(mPassword, "");
					if (encodedPwd.equals(pwd)) {
						Toast.makeText(mContext, "密码正确", Toast.LENGTH_LONG)
								.show();
						mPwdView.markCorrect(2000);

					} else {
						Toast.makeText(mContext, "密码错误", Toast.LENGTH_LONG)
								.show();
						mPwdView.markError();
					}
				}
			}

			@Override
			public void onPasswordLenError(int min, int max) {
				Toast.makeText(getApplicationContext(),
						"密码长度为" + min + "至" + max + "!", Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			sp.edit().remove("password").commit();
			label.setText("请设置密码");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
