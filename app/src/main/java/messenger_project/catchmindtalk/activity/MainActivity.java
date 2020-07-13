package messenger_project.catchmindtalk.activity;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;


import messenger_project.catchmindtalk.MyDatabaseOpenHelper;
import messenger_project.catchmindtalk.R;
import messenger_project.catchmindtalk.adapter.TabPagerAdapter;
import messenger_project.catchmindtalk.fragment.FriendListFragment;

public class MainActivity extends AppCompatActivity implements FriendListFragment.sendToActivity{

    private Toolbar toolbar;
    private TabLa tabLayout;
    private ViewPager viewPager;
    private ChatService mService;

    public MyDatabaseOpenHelper db;
    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;

    public String userId;
    public String nickname;

    public FragmentCommunicator fragmentCommunicator;
    public int tabPosition; // 선택한 탭 위치
    public Handler handler;

    public int dNo;
    public String dFriendId;

    public NetworkChangeReceiver mNCR;

    public static final int MakeGroupActivity = 5409;
    public static final int EditChatRoom = 5828;

    BroadcastReceiver NetworkChangeUpdater;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar)findViewById(R.id.toolbar_MainActivity);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //커스터마이징 하기 위해 필요
        getSupportActionBar().setTitle("캐마톡");

        mPref = getSharedPreferences("login",MODE_PRIVATE);
        editor = mPref.edit();
        userId = mPref.getString("userId","닉네임없음");
        nickname = mPref.getString("nickname","메세지없음");

        db = new MyDatabaseOpenHelper(this,"catchTalk",null,1);

        tabPosition = 0;

        // Initializing the TabLayout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout_MainActivity);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.profile_icon_act));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chat_icon_inact));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.setting_icon_inact));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Initializing ViewPager
        viewPager = (ViewPager) findViewById(R.id.pager_MainActivity);

        TabFragment1 fragment1 = new TabFragment1();
        TabFragment2 fragment2 = new TabFragment2();
        TabFragment3 fragment3 = new TabFragment3();

        fragmentCommunicator = (FragmentCommunicator) fragment2;

        // Creating TabPagerAdapter adapter
        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount(),mPref,fragment1,fragment2,fragment3);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Set TabSelectedListener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                tabPosition = position;
                invalidateOptionsMenu();
                viewPager.setCurrentItem(position);

                if(position==0){
                    tab.setIcon(R.drawable.profile_icon_act);
                }else if(position==1){
                    tab.setIcon(R.drawable.chat_icon_act);
                }else if(position==2){
                    tab.setIcon(R.drawable.setting_icon_act);
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                if(position==0){
                    tab.setIcon(R.drawable.profile_icon_inact);
                }else if(position==1){
                    tab.setIcon(R.drawable.chat_icon_inact);
                }else if(position==2){
                    tab.setIcon(R.drawable.setting_icon_inact);
                }

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){

                if(msg.what == 1) {
                    fragmentCommunicator.notifyRecvData();
                }else if(msg.what ==2){
                    fragmentCommunicator.changeRoomList();
                }


            }
        };

        Intent serviceIntent = new Intent(this, ChatService.class);
        serviceIntent.putExtra("FromLogin",false);
        bindService(serviceIntent, mConnection, this.BIND_AUTO_CREATE);


    }


    public interface FragmentCommunicator {

        void notifyRecvData();
        void changeRoomList();
        void startChatRoomActivity(int no,String friendId, String nickname);

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            ChatService.ChatServiceBinder binder = (ChatService.ChatServiceBinder) service;
            mService = binder.getService(); //서비스 받아옴
            mService.registerCallback_2(mCallback); //콜백 등록
            mService.boundCheck_MainActivity = true;
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    public void UpdateNetwork(String type){
        if(type.equals("wifi")) {
            Intent serviceIntent = new Intent(this, ChatService.class);
            bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);

        }else{
            Intent serviceIntent = new Intent(this, ChatService.class);
            bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
        }
    }


    @Override
    public void sendToActivity(String friendId,String nickname) {
        viewPager.setCurrentItem(1);
        fragmentCommunicator.startChatRoomActivity(0,friendId,nickname);
    }

    public void sendToActivity2(int no,String friendId) {

        dNo = no;
        dFriendId = friendId;

        DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which){
                try {
                    ExitThread et = new ExitThread(dNo, dFriendId,true);
                    et.start();
                    et.join();
                    fragmentCommunicator.changeRoomList();

                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

        };


        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener(){

            @Override public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }

        };




        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("채팅방에서 나가기를 하면 대화 내용 및 채팅목록에서 모두 삭제됩니다.\n채팅방에서 나가시겠습니까?")
                .setPositiveButton("확인", exitListener)
                .setNegativeButton("취소", cancelListener)
                .create();

        dialog.show();

        Button exitBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        exitBtn.setTextColor(Color.BLACK);

        Button cancelBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        cancelBtn.setTextColor(Color.BLACK);


    }



    private ChatService.ICallback_MainActivity mCallback = new ChatService.ICallback_MainActivity() {

        public void recvData() {

            Message message= Message.obtain();
            message.what = 1;
            handler.sendMessage(message);

        }

        public void changeRoomList(){

            Message message= Message.obtain();
            message.what = 2;
            handler.sendMessage(message);

        }

    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //ActionBar 메뉴 클릭에 대한 이벤트 처리
        String txt = null;
        int id = item.getItemId();
        switch (id){
            case R.id.action_align:

                break;
            case R.id.action_search_friend:

                Intent intentSF = new Intent(this,SearchFriendActivity.class);
                startActivity(intentSF);
                break;

            case R.id.action_search_chatroom:

                Intent intentSR = new Intent(this,SearchRoomActivity.class);
                startActivity(intentSR);
                break;


            case R.id.add_friend:
                Intent intentadd = new Intent(this,AddFriendActivity.class);
                startActivity(intentadd);
                break;
            case R.id.edit_friend:

                Intent intent = new Intent(this,EditFriendActivity.class);
                startActivity(intent);
                break;

            case R.id.add_chatroom:

                Intent intentMakeGroup = new Intent(this,MakeGroupActivity.class);
                intentMakeGroup.putExtra("FCR",false);
                startActivityForResult(intentMakeGroup,MakeGroupActivity);
                break;

            case R.id.edit_chatroom:

                Intent intentEdit = new Intent(this,EditChatRoomActivity.class);
                startActivityForResult(intentEdit,EditChatRoom);

                break;


        }


        return super.onOptionsItemSelected(item);
    }




    public class ExitThread extends Thread{


        int no;
        String content;
        long now;
        String friendIdExit;
        boolean deleteDB;

        public ExitThread(int No,String FriendIdExit,boolean DeleteDB){
            this.no = No;
            this.now = System.currentTimeMillis();
            this.content = nickname + "님이 나갔습니다";
            this.friendIdExit = FriendIdExit;
            this.deleteDB = DeleteDB;
        }

        @Override
        public void run() {

            if(deleteDB){

                db.deleteRoom(no,friendIdExit);
                db.deleteChatFriendAll(no,friendIdExit);
                db.deleteMessageData(no,friendIdExit);

            }

            mService.sendExit(no,friendIdExit,content,now);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService.boundCheck_MainActivity = false;
        unbindService(mConnection);


        boolean autoLogin = mPref.getBoolean("autoLogin",false);
        Log.d("MainActivity","onDestroy"+autoLogin);
        if(!autoLogin){
            mService.terminateService();
        }

        unregisterReceiver(mNCR);
        unregisterReceiver(NetworkChangeUpdater);
        mNCR = null;

    }

}