package com.hrobbie.netchat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hrobbie.netchat.R;
import com.hrobbie.netchat.adapter.ViewPagerFragmentAdapter;
import com.hrobbie.netchat.contact.activity.AddFriendActivity;
import com.hrobbie.netchat.helper.SystemMessageUnreadManager;
import com.hrobbie.netchat.team.activity.AdvancedTeamSearchActivity;
import com.hrobbie.netchat.ui.fragment.BaseFragment;
import com.hrobbie.netchat.ui.fragment.ContactListFragment;
import com.hrobbie.netchat.ui.fragment.SessionListFragment;
import com.hrobbie.netchat.ui.reminder.ReminderItem;
import com.hrobbie.netchat.ui.reminder.ReminderManager;
import com.hrobbie.netchat.utills.DensityUtil;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.drop.DropCover;
import com.netease.nim.uikit.common.ui.drop.DropManager;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.contact_selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.team.helper.TeamHelper;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Main3Activity extends UI implements ReminderManager.UnreadNumChangedCallback, ViewPager.OnPageChangeListener {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;
    @BindView(R.id.vp_main)
    ViewPager vpMain;
    @BindView(R.id.rb_1)
    RadioButton rb1;
    @BindView(R.id.tv_1)
    TextView tv1;
    @BindView(R.id.rl_1)
    RelativeLayout rl1;
    @BindView(R.id.rb_2)
    RadioButton rb2;
    @BindView(R.id.tv_2)
    TextView tv2;
    @BindView(R.id.rl_2)
    RelativeLayout rl2;
    @BindView(R.id.tab_layout)
    LinearLayout tabLayout;

    private List<BaseFragment> fragments = new ArrayList<>();
    private Context mContext;
    //默认跳转第一个界面
    private int index = 0;

    private static final String EXTRA_APP_QUIT = "APP_QUIT";
    private static final int REQUEST_CODE_NORMAL = 1;
    private static final int REQUEST_CODE_ADVANCED = 2;
    private static final String TAG = Main2Activity.class.getSimpleName();
    private final int BASIC_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        ButterKnife.bind(this);

        mContext = this;
        initData();
        initViewPager();
        initView();

        registerMsgUnreadInfoObserver(true);
        registerSystemMessageObservers(true);
        requestSystemMessageUnreadCount();
//        initUnreadCover();

    }
    private void initViewPager() {
        //给viewpager设置适配器
        vpMain.setAdapter(new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragments));
        vpMain.setOffscreenPageLimit(fragments.size()-1);
        vpMain.addOnPageChangeListener(this);
    }

    private void initData() {


        fragments.add(new SessionListFragment());
        fragments.add(new ContactListFragment());

        Intent intent = getIntent();
        index=intent.getIntExtra("Main",0);


    }

    private void initView() {
        setToolBar(R.id.toolbar, R.string.app_name, R.drawable.actionbar_dark_logo);
        setTitle(R.string.app_name);

        vpMain.setCurrentItem(index);
        messageTips(-1,tv1);//-2:表示新消息用红点的方式显示,
        messageTips(-1,tv2);//-2:表示新消息用红点的方式显示
    }
    @OnClick({R.id.rl_1, R.id.rl_2})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rl_1:
                changeRadioButton(0);
                vpMain.setCurrentItem(0,false);
                break;
            case R.id.rl_2:
                changeRadioButton(1);
                vpMain.setCurrentItem(1,false);
                break;
        }
    }

    //-1:没有提示
    //-2:表示新消息用红点的方式显示,

    private void messageTips(int num, TextView tv) {
        if (num == -1) {
            tv.setVisibility(View.GONE);
        } else if (num == -2) {
            tv.setVisibility(View.VISIBLE);
            tv.setText("");
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) tv.getLayoutParams();
            layoutParams.height = DensityUtil.dip2px(this, 10);
            layoutParams.width = DensityUtil.dip2px(this, 10);
            tv.setLayoutParams(layoutParams);
        } else if (num >= 0 && num <= 99) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(num + "");
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) tv.getLayoutParams();
            layoutParams.height = DensityUtil.dip2px(this, 16);
            layoutParams.width = DensityUtil.dip2px(this, 16);
            tv.setLayoutParams(layoutParams);
        } else if (num >= 100) {
            tv.setVisibility(View.VISIBLE);
            tv.setText("99+");
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) tv.getLayoutParams();
            layoutParams.height = DensityUtil.dip2px(this, 16);
            layoutParams.width = DensityUtil.dip2px(this, 16);
            tv.setTextSize(DensityUtil.sp2px(this, 3));
            tv.setLayoutParams(layoutParams);
        } else {
            tv.setVisibility(View.GONE);
        }

    }

    /**
     * 根据index更改radioButton的选中状态
     *
     * @param index
     */
    private void changeRadioButton(int index) {

        switch (index) {
            case 0:
                rb1.setChecked(true);
                rb2.setChecked(false);

                break;
            case 1:
                rb1.setChecked(false);
                rb2.setChecked(true);
                break;
            case 2:
                rb1.setChecked(false);
                rb2.setChecked(false);
                break;
        }
    }


    /**
     * 在oneFragment中更新，底部导航栏的数字
     *
     * @param num
     */
    public void updateOne(int num) {
        messageTips(num, tv1);
    }

    /**
     * 在TwoFragment中更新，底部导航栏的数字
     *
     * @param num
     */
    public void updateTwo(int num) {
        messageTips(num, tv2);
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
     * 初始化未读红点动画
     */
//    private void initUnreadCover() {
//        DropManager.getInstance().init(this, (DropCover) findView(R.id.unread_cover),
//                new DropCover.IDropCompletedListener() {
//                    @Override
//                    public void onCompleted(Object id, boolean explosive) {
//                        if (id == null || !explosive) {
//                            return;
//                        }
//
//                        if (id instanceof RecentContact) {
//                            RecentContact r = (RecentContact) id;
//                            NIMClient.getService(MsgService.class).clearUnreadCount(r.getContactId(), r.getSessionType());
//                            LogUtil.i("HomeFragment", "clearUnreadCount, sessionId=" + r.getContactId());
//                        } else if (id instanceof String) {
//                            if (((String) id).contentEquals("0")) {
//                                List<RecentContact> recentContacts = NIMClient.getService(MsgService.class).queryRecentContactsBlock();
//                                for (RecentContact r : recentContacts) {
//                                    if (r.getUnreadCount() > 0) {
//                                        NIMClient.getService(MsgService.class).clearUnreadCount(r.getContactId(), r.getSessionType());
//                                    }
//                                }
//                                LogUtil.i("HomeFragment", "clearAllUnreadCount");
//                            } else if (((String) id).contentEquals("1")) {
//                                NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
//                                LogUtil.i("HomeFragment", "clearAllSystemUnreadCount");
//                            }
//                        }
//                    }
//                });
//    }

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
        vpMain.removeOnPageChangeListener(this);
        registerMsgUnreadInfoObserver(false);
        registerSystemMessageObservers(false);
        super.onDestroy();
    }

    private void enableMsgNotification(boolean enable) {
        boolean msg = (vpMain.getCurrentItem() !=0);
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
    public void onUnreadNumChanged(ReminderItem item) {
        switch (item.getId()){
            case 0:
                if(item.getUnread()<=0){
                    updateOne(-1);
                }else{
                    updateOne(item.getUnread());
                }
                break;
            case 2:
                if(item.getUnread()<=0){
                    updateTwo(-1);
                }else{
                    updateTwo(item.getUnread());
                }
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
// 如果是最后一个引导界面的话，就出现按钮
        //如果不是最后一个的话，就不出现
        changeRadioButton(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

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
                startActivity(new Intent(Main3Activity.this, SettingsActivity.class));
                break;
            case R.id.create_normal_team:
                ContactSelectActivity.Option option = TeamHelper.getCreateContactSelectOption(null, 50);
                NimUIKit.startContactSelect(Main3Activity.this, option, REQUEST_CODE_NORMAL);
                break;
            case R.id.create_regular_team:
                ContactSelectActivity.Option advancedOption = TeamHelper.getCreateContactSelectOption(null, 50);
                NimUIKit.startContactSelect(Main3Activity.this, advancedOption, REQUEST_CODE_ADVANCED);
                break;
            case R.id.search_advanced_team:
                AdvancedTeamSearchActivity.start(Main3Activity.this);
                break;
            case R.id.add_buddy:
                AddFriendActivity.start(Main3Activity.this);
                break;
            case R.id.search_btn:
                GlobalSearchActivity.start(Main3Activity.this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

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
