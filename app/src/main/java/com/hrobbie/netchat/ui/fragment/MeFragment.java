package com.hrobbie.netchat.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.hrobbie.netchat.R;
import com.hrobbie.netchat.adapter.SettingsAdapter;
import com.hrobbie.netchat.avchat.activity.AVChatSettingsActivity;
import com.hrobbie.netchat.contact.activity.UserProfileSettingActivity;
import com.hrobbie.netchat.model.SettingTemplate;
import com.hrobbie.netchat.model.SettingType;
import com.hrobbie.netchat.ui.activity.AboutActivity;
import com.hrobbie.netchat.ui.activity.CustomNotificationActivity;
import com.hrobbie.netchat.ui.activity.Main3Activity;
import com.hrobbie.netchat.ui.activity.NoDisturbActivity;
import com.hrobbie.netchat.ui.activity.SettingsActivity;
import com.hrobbie.netchat.utills.cache.DemoCache;
import com.hrobbie.netchat.utills.cache.Preferences;
import com.hrobbie.netchat.utills.cache.UserPreferences;
import com.netease.nim.uikit.session.audio.MessageAudioControl;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.avchat.AVChatNetDetectCallback;
import com.netease.nimlib.sdk.avchat.AVChatNetDetector;
import com.netease.nimlib.sdk.lucene.LuceneService;
import com.netease.nimlib.sdk.mixpush.MixPushService;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.settings.SettingsService;
import com.netease.nimlib.sdk.settings.SettingsServiceObserver;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MeFragment extends BaseFragment implements SettingsAdapter.SwitchChangeListener {
    private static final int TAG_HEAD = 1;
    private static final int TAG_NOTICE = 2;
    private static final int TAG_NO_DISTURBE = 3;
    private static final int TAG_CLEAR = 4;
    private static final int TAG_CUSTOM_NOTIFY = 5;
    private static final int TAG_ABOUT = 6;
    private static final int TAG_SPEAKER = 7;

    private static final int TAG_NRTC_SETTINGS = 8;
    private static final int TAG_NRTC_NET_DETECT = 9;

    private static final int TAG_MSG_IGNORE = 10;
    private static final int TAG_RING = 11;
    private static final int TAG_LED = 12;
    private static final int TAG_NOTICE_CONTENT = 13; // 通知栏提醒配置
    private static final int TAG_CLEAR_INDEX = 18; // 清空全文检索缓存
    private static final int TAG_MULTIPORT_PUSH = 19; // 桌面端登录，是否推送
    private static final int TAG_JS_BRIDGE = 20; // js bridge

    private static final int TAG_NOTIFICATION_STYLE = 21; // 通知栏展开、折叠
//    ListView listView;
    SettingsAdapter adapter;
    @BindView(R.id.settings_listview)
    ListView listView;
    Unbinder unbinder;
    private List<SettingTemplate> items = new ArrayList<SettingTemplate>();
    private String noDisturbTime;
    private SettingTemplate disturbItem;
    private SettingTemplate clearIndexItem;
    private SettingTemplate notificationItem;

    @Override
    protected void lazyLoad() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        initUI();

        registerObservers(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        // android2.3以下版本 布局混乱的问题
        if (Build.VERSION.SDK_INT <= 10) {
            adapter = null;
            initAdapter();
            adapter.notifyDataSetChanged();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registerObservers(false);
    }

    private void registerObservers(boolean register) {
        NIMClient.getService(SettingsServiceObserver.class).observeMultiportPushConfigNotify(pushConfigObserver, register);
    }

    Observer<Boolean> pushConfigObserver = new Observer<Boolean>() {
        @Override
        public void onEvent(Boolean aBoolean) {
            Toast.makeText(getActivity(), "收到multiport push config：" + aBoolean, Toast.LENGTH_SHORT).show();
        }
    };

    private void initData() {
        if (UserPreferences.getStatusConfig() == null || !UserPreferences.getStatusConfig().downTimeToggle) {
            noDisturbTime = getString(R.string.setting_close);
        } else {
            noDisturbTime = String.format("%s到%s", UserPreferences.getStatusConfig().downTimeBegin,
                    UserPreferences.getStatusConfig().downTimeEnd);
        }
    }

    private void initUI() {
        initItems();
//        listView = (ListView) findViewById(R.id.settings_listview);
        View footer = LayoutInflater.from(getActivity()).inflate(R.layout.settings_logout_footer, null);
        listView.addFooterView(footer);

        initAdapter();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingTemplate item = items.get(position);
                onListItemClick(item);
            }
        });
        View logoutBtn = footer.findViewById(R.id.settings_button_logout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void initAdapter() {
        adapter = new SettingsAdapter(getActivity(), this, items);
        listView.setAdapter(adapter);
    }

    private void initItems() {
        items.clear();

        items.add(new SettingTemplate(TAG_HEAD, SettingType.TYPE_HEAD));
        notificationItem = new SettingTemplate(TAG_NOTICE, getString(R.string.msg_notice), SettingType.TYPE_TOGGLE,
                UserPreferences.getNotificationToggle());
        items.add(notificationItem);
        items.add(SettingTemplate.addLine());
        items.add(new SettingTemplate(TAG_RING, getString(R.string.ring), SettingType.TYPE_TOGGLE,
                UserPreferences.getRingToggle()));
        items.add(new SettingTemplate(TAG_LED, getString(R.string.led), SettingType.TYPE_TOGGLE,
                UserPreferences.getLedToggle()));
        items.add(SettingTemplate.addLine());
        items.add(new SettingTemplate(TAG_NOTICE_CONTENT, getString(R.string.notice_content), SettingType.TYPE_TOGGLE,
                UserPreferences.getNoticeContentToggle()));
//        items.add(new SettingTemplate(TAG_NOTIFICATION_STYLE, getString(R.string.notification_folded), SettingType.TYPE_TOGGLE,
//                UserPreferences.getNotificationFoldedToggle()));
        items.add(SettingTemplate.addLine());
        disturbItem = new SettingTemplate(TAG_NO_DISTURBE, getString(R.string.no_disturb), noDisturbTime);
        items.add(disturbItem);
        items.add(SettingTemplate.addLine());
        items.add(new SettingTemplate(TAG_MULTIPORT_PUSH, getString(R.string.multiport_push), SettingType.TYPE_TOGGLE,
                !NIMClient.getService(SettingsService.class).isMultiportPushOpen()));

        items.add(SettingTemplate.makeSeperator());

        items.add(new SettingTemplate(TAG_SPEAKER, getString(R.string.msg_speaker), SettingType.TYPE_TOGGLE,
                com.netease.nim.uikit.UserPreferences.isEarPhoneModeEnable()));

        items.add(SettingTemplate.makeSeperator());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            items.add(new SettingTemplate(TAG_NRTC_SETTINGS, getString(R.string.nrtc_settings)));
            items.add(SettingTemplate.addLine());
            items.add(new SettingTemplate(TAG_NRTC_NET_DETECT, "音视频通话网络探测"));
            items.add(SettingTemplate.makeSeperator());
        }

        items.add(new SettingTemplate(TAG_MSG_IGNORE, "过滤通知",
                SettingType.TYPE_TOGGLE, UserPreferences.getMsgIgnore()));

        items.add(SettingTemplate.makeSeperator());

        items.add(new SettingTemplate(TAG_CLEAR, getString(R.string.about_clear_msg_history)));
        items.add(SettingTemplate.addLine());
        clearIndexItem = new SettingTemplate(TAG_CLEAR_INDEX, getString(R.string.clear_index), getIndexCacheSize() + " M");
        items.add(clearIndexItem);

        items.add(SettingTemplate.makeSeperator());

        items.add(new SettingTemplate(TAG_CUSTOM_NOTIFY, getString(R.string.custom_notification)));
        items.add(SettingTemplate.addLine());
        items.add(new SettingTemplate(TAG_JS_BRIDGE, getString(R.string.js_bridge_demonstration)));
        items.add(SettingTemplate.makeSeperator());

        items.add(new SettingTemplate(TAG_ABOUT, getString(R.string.setting_about)));

    }

    private void onListItemClick(SettingTemplate item) {
        if (item == null) return;

        switch (item.getId()) {
            case TAG_HEAD:
                UserProfileSettingActivity.start(getActivity(), DemoCache.getAccount());
                break;
            case TAG_NO_DISTURBE:
//                startNoDisturb();
                break;
            case TAG_CUSTOM_NOTIFY:
                CustomNotificationActivity.start(getActivity());
                break;
            case TAG_ABOUT:
                startActivity(new Intent(getActivity(), AboutActivity.class));
                break;
            case TAG_CLEAR:
                NIMClient.getService(MsgService.class).clearMsgDatabase(true);
                Toast.makeText(getActivity(), R.string.clear_msg_history_success, Toast.LENGTH_SHORT).show();
                break;
            case TAG_CLEAR_INDEX:
                clearIndex();
                break;
            case TAG_NRTC_SETTINGS:
                startActivity(new Intent(getActivity(), AVChatSettingsActivity.class));
                break;
            case TAG_NRTC_NET_DETECT:
                netDetectForNrtc();
                break;
            case TAG_JS_BRIDGE:
//                startActivity(new Intent(SettingsActivity.this, JsBridgeActivity.class));
                break;
            default:
                break;
        }
    }

    private void netDetectForNrtc() {
        AVChatNetDetector.startNetDetect(new AVChatNetDetectCallback() {
            @Override
            public void onDetectResult(String id,
                                       int code,
                                       int loss,
                                       int rttMax,
                                       int rttMin,
                                       int rttAvg,
                                       int mdev,
                                       String info) {
                String msg = code == 200 ?
                        ("loss:" + loss + ", rtt min/avg/max/mdev = " + rttMin + "/" + rttAvg + "/" + rttMax + "/" + mdev + " ms")
                        : ("error:" + code);
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 注销
     */
    private void logout() {
        removeLoginState();
        Main3Activity.logout(getActivity(), false);

        getActivity().finish();
        NIMClient.getService(AuthService.class).logout();
    }

    /**
     * 清除登陆状态
     */
    private void removeLoginState() {
        Preferences.saveUserToken("");
    }

    @Override
    public void onSwitchChange(SettingTemplate item, boolean checkState) {
        switch (item.getId()) {
            case TAG_NOTICE:
                setMessageNotify(checkState);
                break;
            case TAG_SPEAKER:
                com.netease.nim.uikit.UserPreferences.setEarPhoneModeEnable(checkState);
                MessageAudioControl.getInstance(getActivity()).setEarPhoneModeEnable(checkState);
                break;
            case TAG_MSG_IGNORE:
                UserPreferences.setMsgIgnore(checkState);
                break;
            case TAG_RING:
                UserPreferences.setRingToggle(checkState);
                StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
                config.ring = checkState;
                UserPreferences.setStatusConfig(config);
                NIMClient.updateStatusBarNotificationConfig(config);
                break;
            case TAG_LED:
                UserPreferences.setLedToggle(checkState);
                StatusBarNotificationConfig config1 = UserPreferences.getStatusConfig();
                StatusBarNotificationConfig demoConfig = DemoCache.getNotificationConfig();
                if (checkState && demoConfig != null) {
                    config1.ledARGB = demoConfig.ledARGB;
                    config1.ledOnMs = demoConfig.ledOnMs;
                    config1.ledOffMs = demoConfig.ledOffMs;
                } else {
                    config1.ledARGB = -1;
                    config1.ledOnMs = -1;
                    config1.ledOffMs = -1;
                }
                UserPreferences.setStatusConfig(config1);
                NIMClient.updateStatusBarNotificationConfig(config1);
                break;
            case TAG_NOTICE_CONTENT:
                UserPreferences.setNoticeContentToggle(checkState);
                StatusBarNotificationConfig config2 = UserPreferences.getStatusConfig();
                config2.titleOnlyShowAppName = checkState;
                UserPreferences.setStatusConfig(config2);
                NIMClient.updateStatusBarNotificationConfig(config2);
                break;
            case TAG_MULTIPORT_PUSH:
                updateMultiportPushConfig(!checkState);
                break;
            case TAG_NOTIFICATION_STYLE:
                UserPreferences.setNotificationFoldedToggle(checkState);
                config = UserPreferences.getStatusConfig();
                config.notificationFolded = checkState;
                UserPreferences.setStatusConfig(config);
                NIMClient.updateStatusBarNotificationConfig(config);
            default:
                break;
        }
        item.setChecked(checkState);
    }

    private void setMessageNotify(final boolean checkState) {
        // 如果接入第三方推送（小米），则同样应该设置开、关推送提醒
        // 如果关闭消息提醒，则第三方推送消息提醒也应该关闭。
        // 如果打开消息提醒，则同时打开第三方推送消息提醒。
        NIMClient.getService(MixPushService.class).enable(checkState).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                Toast.makeText(getActivity(), R.string.user_info_update_success, Toast.LENGTH_SHORT).show();
                notificationItem.setChecked(checkState);
                setToggleNotification(checkState);
            }

            @Override
            public void onFailed(int code) {
                notificationItem.setChecked(!checkState);
                // 这种情况是客户端不支持第三方推送
                if (code == ResponseCode.RES_UNSUPPORT) {
                    notificationItem.setChecked(checkState);
                    setToggleNotification(checkState);
                } else if (code == ResponseCode.RES_EFREQUENTLY) {
                    Toast.makeText(getActivity(), R.string.operation_too_frequent, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), R.string.user_info_update_failed, Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    private void setToggleNotification(boolean checkState) {
        try {
            setNotificationToggle(checkState);
            NIMClient.toggleNotification(checkState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setNotificationToggle(boolean on) {
        UserPreferences.setNotificationToggle(on);
    }

    private void startNoDisturb() {
        NoDisturbActivity.startActivityForResult(getActivity(), UserPreferences.getStatusConfig(), noDisturbTime, NoDisturbActivity.NO_DISTURB_REQ);
    }

    private String getIndexCacheSize() {
        long size = NIMClient.getService(LuceneService.class).getCacheSize();
        return String.format("%.2f", size / (1024.0f * 1024.0f));
    }

    private void clearIndex() {
        NIMClient.getService(LuceneService.class).clearCache();
        clearIndexItem.setDetail("0.00 M");
        adapter.notifyDataSetChanged();
    }

    private void updateMultiportPushConfig(final boolean checkState) {
        NIMClient.getService(SettingsService.class).updateMultiportPushConfig(checkState).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                Toast.makeText(getActivity(), "设置成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int code) {
                Toast.makeText(getActivity(), "设置失败,code:" + code, Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }





    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
