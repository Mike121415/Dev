package com.android.myweibo.openapi;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.myweibo.AccessTokenKeeper;
import com.android.myweibo.Constants;
import com.android.myweibo.R;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.utils.LogUtil;

/**
 * Created by mikez on 2016/10/6.
 *
 */
/**
 * 该类主要演示了如何使用微博 OpenAPI 来获取以下内容：
 * <li>获取最新的公共微博
 * <li>获取当前登录用户及其所关注用户的最新微博
 * <li>获取当前登录用户及其所关注用户的最新微博的ID
 * <li>...
 *
 */
public class WBStatusAPIActivity extends Activity implements AdapterView.OnItemClickListener
{
    private static final String TAG = WBStatusAPIActivity.class.getName();

    /** UI 元素：ListView */
    private ListView mFuncListView;
    /** 功能列表 */
    private String[] mFuncList;
    /** 当前 Token 信息 */
    private Oauth2AccessToken mAccessToken;
    /** 用于获取微博信息流等操作的API */
    private StatusesAPI mStatusesAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_api_base_layout);

        // 获取功能列表
        mFuncList = getResources().getStringArray(R.array.statuses_func_list);
        // 初始化功能列表 ListView
        mFuncListView = (ListView)findViewById(R.id.api_func_list);
        mFuncListView.setAdapter(new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, mFuncList));
        mFuncListView.setOnItemClickListener(this);

        // 获取当前已保存过的 Token
        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        // 对statusAPI实例化
        mStatusesAPI = new StatusesAPI(this, Constants.APP_KEY, mAccessToken);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (view instanceof TextView) {
            if (mAccessToken != null && mAccessToken.isSessionValid()) {
                switch (position) {
                    case 0:
                        mStatusesAPI.friendsTimeline(0L, 0L, 10, 1, false, 0, false, mListener);
                        break;

                    case 1:
                        mStatusesAPI.mentions(0L, 0L, 10, 1, 0, 0, 0, false, mListener);
                        break;

                    case 2:
                        mStatusesAPI.update("发送一条纯文字微博", null, null, mListener);
                        break;

                    case 3:
                        Drawable drawable = getResources().getDrawable(R.drawable.ic_com_sina_weibo_sdk_logo);
                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                        mStatusesAPI.upload("发送一条带本地图片的微博", bitmap, null, null, mListener);

                        /** 可以自行拼接参数，异步请求数据 */
                    /*
                    WeiboParameters wbparams = new WeiboParameters();
                    wbparams.put("access_token", mAccessToken.getToken());
                    wbparams.put("status",       "通过API发送微博-upload");
                    wbparams.put("visible",      "0");
                    wbparams.put("list_id",      "");
                    wbparams.put("pic",          bitmap);
                    wbparams.put("lat",          "14.5");
                    wbparams.put("long",         "23.0");
                    wbparams.put("annotations",  "");

                    AsyncWeiboRunner.requestAsync(
                            "https://api.weibo.com/2/statuses/upload.json",
                            wbparams,
                            "POST",
                            mListener);*/
                        break;

                    case 4:
                        String photoURL = "http://hiphotos.baidu.com/lvpics/pic/item/b25aae51bc7a3474377abe75.jpg";
                        // 请注意：该接口暂不支持发布多图，即参数pic_id不可用（只能通过BD合作接入，不对个人申请）
                        mStatusesAPI.uploadUrlText("发送一条带网络图片的微博", photoURL, null, null, null, mListener);
                        break;

                    default:
                        break;
                }
            } else {
                Toast.makeText(WBStatusAPIActivity.this,
                        R.string.weibosdk_demo_access_token_is_empty,
                        Toast.LENGTH_LONG).show();
            }
        }

    }

    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                LogUtil.i(TAG, response);
                if (response.startsWith("{\"statuses\"")) {
                    // 调用 StatusList#parse 解析字符串成微博列表对象
                    StatusList statuses = StatusList.parse(response);  //StatusList 是否中已经获取到所有数据
                    if (statuses != null && statuses.total_number > 0) {
                        Toast.makeText(WBStatusAPIActivity.this,
                                "获取微博信息流成功, 条数: " + statuses.statusList.size(),
                                Toast.LENGTH_LONG).show();
                    }
                } else if (response.startsWith("{\"created_at\"")) {
                    // 调用 Status#parse 解析字符串成微博对象
                    Status status = Status.parse(response);
                    Toast.makeText(WBStatusAPIActivity.this,
                            "发送一送微博成功, id = " + status.id,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(WBStatusAPIActivity.this, response, Toast.LENGTH_LONG).show();
                }
            }
        }
        @Override
        public void onWeiboException(WeiboException e) {
            LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(WBStatusAPIActivity.this, info.toString(), Toast.LENGTH_LONG).show();
        }
    };


}
