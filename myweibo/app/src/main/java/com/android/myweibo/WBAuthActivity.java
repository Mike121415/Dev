package com.android.myweibo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import java.text.SimpleDateFormat;

/**
 * Created by mikez on 2016/9/11.
 */
public class WBAuthActivity extends Activity
{
    private static final String TAG = "weibosdk";

    /** 显示认证后的信息，如 AccessToken */
    private TextView mTokenText;

    private AuthInfo mAuthInfo;

    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;

    /** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
    private SsoHandler mSsoHandler;
    private Context   otherContext;

    /**
     * @see {@link Activity#onCreate}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_layout);
        // 获取 Token View，并让提示 View 的内容可滚动（小屏幕可能显示不全）
        mTokenText = (TextView) findViewById(R.id.token_text_view);
        TextView hintView = (TextView) findViewById(R.id.obtain_token_hint);
        hintView.setMovementMethod(new ScrollingMovementMethod());

        // 创建微博实例
        //mWeiboAuth = new WeiboAuth(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        // 快速授权时，请不要传入 SCOPE，否则可能会授权不成功
        /*
         *创建微博授权类对象，将应用程序的信息保存
         * */
        mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        mSsoHandler = new SsoHandler(WBAuthActivity.this, mAuthInfo);

        findViewById(R.id.obtain_token_via_web).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSsoHandler.authorizeWeb(new AuthListener());
            }
        });
    }


    /**
     * 微博认证授权回调类。
     * 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用 {@link SsoHandler#authorizeCallBack} 后，
     *    该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
     */
    class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            //从这里获取用户输入的 电话号码信息
            String  phoneNum =  mAccessToken.getPhoneNum();
            if (mAccessToken.isSessionValid()) {
                // 显示 Token
                updateTokenView(false);

                // 保存 Token 到 SharedPreferences
                AccessTokenKeeper.writeAccessToken(WBAuthActivity.this, mAccessToken);
                Toast.makeText(WBAuthActivity.this,R.string.weibosdk_demo_toast_auth_success, Toast.LENGTH_SHORT).show();
               /*
               try {
                    otherContext= createPackageContext("com.android.myweibo", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
                } catch (PackageManager.NameNotFoundException e)
                {
                    e.printStackTrace();
                }
                SharedPreferences sharedPreferences = otherContext.getSharedPreferences(AccessTokenKeeper.PREFERENCES_NAME, Context.MODE_APPEND);
                String key_ID=sharedPreferences.getString(AccessTokenKeeper.KEY_UID, "default");
                String key_access_token=sharedPreferences.getString(AccessTokenKeeper.KEY_ACCESS_TOKEN, "key_access_token default");
                String refresh_token=sharedPreferences.getString(AccessTokenKeeper.KEY_REFRESH_TOKEN, "refresh_token default");
                Long key_expires_in=sharedPreferences.getLong(AccessTokenKeeper.KEY_EXPIRES_IN, 0L);
               Toast.makeText(WBAuthActivity.this,key_ID,Toast.LENGTH_SHORT).show();

                * 将 Key_ID 和其他需要打印显示的东西 打印到日志中

                Log.d("WBAuthActivity","Key_ID"+key_ID+" KEY_ACCESS_TOKEN"+key_access_token+" KEY_REFRESH_TOKEN"+refresh_token+" KEY_EXPIRES_IN"+key_expires_in.toString());
                */



            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                String message = getString(R.string.weibosdk_demo_toast_auth_failed);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(WBAuthActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel() {
            Toast.makeText(WBAuthActivity.this,
                    R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(WBAuthActivity.this,
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    /**
     * 显示当前 Token 信息。
     *
     * @param hasExisted 配置文件中是否已存在 token 信息并且合法
     */
    private void updateTokenView(boolean hasExisted) {
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                new java.util.Date(mAccessToken.getExpiresTime()));
        String format = getString(R.string.weibosdk_demo_token_to_string_format_1);
        mTokenText.setText(String.format(format, mAccessToken.getToken(), date));

        String message = String.format(format, mAccessToken.getToken(), date);
        if (hasExisted) {
            message = getString(R.string.weibosdk_demo_token_has_existed) + "\n" + message;
        }
        mTokenText.setText(message);
    }
}
