package com.hrobbie.netchat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.hrobbie.netchat.R;
import com.hrobbie.netchat.adapter.TabFragmentAdapter;
import com.hrobbie.netchat.contact.activity.AddFriendActivity;
import com.hrobbie.netchat.helper.SystemMessageUnreadManager;
import com.hrobbie.netchat.session.SessionHelper;
import com.hrobbie.netchat.team.activity.AdvancedTeamSearchActivity;
import com.hrobbie.netchat.ui.fragment.ContactListFragment;
import com.hrobbie.netchat.ui.fragment.SessionListFragment;
import com.hrobbie.netchat.ui.reminder.ReminderItem;
import com.hrobbie.netchat.ui.reminder.ReminderManager;
import com.hrobbie.netchat.view.viewpager.FadeInOutPageTransformer;
import com.hrobbie.netchat.view.viewpager.PagerSlidingTabStrip;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.drop.DropCover;
import com.netease.nim.uikit.common.ui.drop.DropManager;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.contact_selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.team.helper.TeamHelper;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Main2Activity extends UI implements ViewPager.OnPageChangeListener, ReminderManager.UnreadNumChangedCallback {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;
    @BindView(R.id.tabs)
    PagerSlidingTabStrip tabs;
    @BindView(R.id.main_tab_pager)
    ViewPager pager;
    @BindView(R.id.unread_cover)
    DropCover unreadCover;

    private ArrayList<String> mTitleList = new ArrayList<>();
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private TabFragmentAdapter adapter;

    private static final String EXTRA_APP_QUIT = "APP_QUIT";
    private static final int REQUEST_CODE_NORMAL = 1;
    private static final int REQUEST_CODE_ADVANCED = 2;
    private static final String TAG = Main2Activity.class.getSimpleName();
    private final int BASIC_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.bind(this);

        setToolBar(R.id.toolbar, R.string.app_name, R.drawable.actionbar_dark_logo);
        setTitle(R.string.app_name);

        setupPager();
        setupTabs();
        registerMsgUnreadInfoObserver(true);
        registerSystemMessageObservers(true);
        requestSystemMessageUnreadCount();
        initUnreadCover();
    }
    /**
     * 注册未读消息数量观察者
     */
    private void registerMsgUnreadInfoObserver(boolean register) {
        if (register) {
            ReminderManager.getInstance().registerUnreadNumChangedCallback(this);
        } else {
            ReminderManager.getInstance().unregisterUnreadNumChangedCallback(this);
        }
    }

    /**
     * 注册/注销系统消息未读数变化
     *
     * @param register
     */
    private void registerSystemMessageObservers(boolean register) {
        NIMClient.getService(SystemMessageObserver.class).observeUnreadCountChange(sysMsgUnreadCountChangedObserver,
                register);
    }

    private Observer<Integer> sysMsgUnreadCountChangedObserver = new Observer<Integer>() {
        @Override
        public void onEvent(Integer unreadCount) {
            SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(unreadCount);
            ReminderManager.getInstance().updateContactUnreadNum(unreadCount);
        }
    };

    /**
     * 查询系统消息未读数
     */
    private void requestSystemMessageUnreadCount() {
        int unread = NIMClient.getService(SystemMessageService.class).querySystemMessageUnreadCountBlock();
        SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(unread);
        ReminderManager.getInstance().updateContactUnreadNum(unread);
    }
    /**
     * 设置viewPager
     */
    private void setupPager() {
        mTitleList.add("会话");
        mTitleList.add("通信录");
        mFragments.add(new SessionListFragment());
        mFragments.add(new ContactListFragment());
        // CACHE COUNT
        //此处代码设置无效，不知道为啥？？？xml属性是可以的
        adapter = new TabFragmentAdapter(getSupportFragmentManager(), mFragments, mTitleList);
        pager.setOffscreenPageLimit(adapter.getCount());
        // page swtich animation
        pager.setPageTransformer(true, new FadeInOutPageTransformer());
        // ADAPTER
        pager.setAdapter(adapter);
        // TAKE OVER CHANGE
        pager.addOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // TO TABS
        tabs.onPageScrolled(position, positionOffset, positionOffsetPixels);
//        // TO ADAPTER
//        adapter.onPageScrolled(position);
    }

    @Override
    public void onPageSelected(int position) {
        // TO TABS
        tabs.onPageSelected(position);

//        selectPage(position);
//
//        enableMsgNotification(false);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // TO TABS
        tabs.onPageScrollStateChanged(state);
    }

    /**
     * 设置tab条目
     */
    private void setupTabs() {
        tabs.setOnCustomTabListener(new PagerSlidingTabStrip.OnCustomTabListener() {
            @Override
            public int getTabLayoutResId(int position) {
                return R.layout.tab_layout_main;
            }

            @Override
            public boolean screenAdaptation() {
                return true;
            }
        });
        tabs.setViewPager(pager);
//        tabs.setOnTabClickListener(adapter);
//        tabs.setOnTabDoubleTapListener(adapter);
    }

    /**
     * 初始化未读红点动画
     */
    private void initUnreadCover() {
        DropManager.getInstance().init(this, (DropCover) findView(R.id.unread_cover),
                new DropCover.IDropCompletedListener() {
                    @Override
                    public void onCompleted(Object id, boolean explosive) {
                        if (id == null || !explosive) {
                            return;
                        }

                        if (id instanceof RecentContact) {
                            RecentContact r = (RecentContact) id;
                            NIMClient.getService(MsgService.class).clearUnreadCount(r.getContactId(), r.getSessionType());
                            LogUtil.i("HomeFragment", "clearUnreadCount, sessionId=" + r.getContactId());
                        } else if (id instanceof String) {
                            if (((String) id).contentEquals("0")) {
                                List<RecentContact> recentContacts = NIMClient.getService(MsgService.class).queryRecentContactsBlock();
                                for (RecentContact r : recentContacts) {
                                    if (r.getUnreadCount() > 0) {
                                        NIMClient.getService(MsgService.class).clearUnreadCount(r.getContactId(), r.getSessionType());
                                    }
                                }
                                LogUtil.i("HomeFragment", "clearAllUnreadCount");
                            } else if (((String) id).contentEquals("1")) {
                                NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
                                LogUtil.i("HomeFragment", "clearAllSystemUnreadCount");
                            }
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        enableMsgNotification(false);
        //quitOtherActivities();
    }

    @Override
    public void onPause() {
        super.onPause();
        enableMsgNotification(true);
    }

    @Override
    protected void onDestroy() {
        pager.removeOnPageChangeListener(this);
        registerMsgUnreadInfoObserver(false);
        registerSystemMessageObservers(false);
        super.onDestroy();
    }

    @Override
    public void onUnreadNumChanged(ReminderItem item) {
        tabs.updateTab(item.getId(),item);
    }

    private void enableMsgNotification(boolean enable) {
        boolean msg = (pager.getCurrentItem() !=0);
        if (enable | msg) {
            /**
             * 设置最近联系人的消息为已读
             *
             * @param account,    聊天对象帐号，或者以下两个值：
             *                    {@link #MSG_CHATTING_ACCOUNT_ALL} 目前没有与任何人对话，但能看到消息提醒（比如在消息列表界面），不需要在状态栏做消息通知
             *                    {@link #MSG_CHATTING_ACCOUNT_NONE} 目前没有与任何人对话，需要状态栏消息通知
             */
            NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
        } else {
            NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_ALL, SessionTypeEnum.None);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(Main2Activity.this, SettingsActivity.class));
                break;
            case R.id.create_normal_team:
                ContactSelectActivity.Option option = TeamHelper.getCreateContactSelectOption(null, 50);
                NimUIKit.startContactSelect(Main2Activity.this, option, REQUEST_CODE_NORMAL);
                break;
            case R.id.create_regular_team:
                ContactSelectActivity.Option advancedOption = TeamHelper.getCreateContactSelectOption(null, 50);
                NimUIKit.startContactSelect(Main2Activity.this, advancedOption, REQUEST_CODE_ADVANCED);
                break;
            case R.id.search_advanced_team:
                AdvancedTeamSearchActivity.start(Main2Activity.this);
                break;
            case R.id.add_buddy:
                AddFriendActivity.start(Main2Activity.this);
                break;
            case R.id.search_btn:
                GlobalSearchActivity.start(Main2Activity.this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//    private void onParseIntent() {
//        Intent intent = getIntent();
//        if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
//            IMMessage message = (IMMessage) getIntent().getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
//            switch (message.getSessionType()) {
//                case P2P:
//                    SessionHelper.startP2PSession(this, message.getSessionId());
//                    break;
//                case Team:
//                    SessionHelper.startTeamSession(this, message.getSessionId());
//                    break;
//                default:
//                    break;
//            }
//        } else if (intent.hasExtra(EXTRA_APP_QUIT)) {
//            onLogout();
//            return;
//        } else if (intent.hasExtra(AVChatActivity.INTENT_ACTION_AVCHAT)) {
//            if (AVChatProfile.getInstance().isAVChatting()) {
//                Intent localIntent = new Intent();
//                localIntent.setClass(this, AVChatActivity.class);
//                startActivity(localIntent);
//            }
//        } else if (intent.hasExtra(com.netease.nim.demo.main.model.Extras.EXTRA_JUMP_P2P)) {
//            Intent data = intent.getParcelableExtra(com.netease.nim.demo.main.model.Extras.EXTRA_DATA);
//            String account = data.getStringExtra(com.netease.nim.demo.main.model.Extras.EXTRA_ACCOUNT);
//            if (!TextUtils.isEmpty(account)) {
//                SessionHelper.startP2PSession(this, account);
//            }
//        }
//    }

    // 注销
    public static void logout(Context context, boolean quit) {
        Intent extra = new Intent();
        extra.putExtra(EXTRA_APP_QUIT, quit);
        start(context, extra);
    }

    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, Intent extras) {
        Intent intent = new Intent();
        intent.setClass(context, Main2Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }
}
