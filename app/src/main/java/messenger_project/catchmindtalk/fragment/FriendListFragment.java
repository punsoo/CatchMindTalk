package messenger_project.catchmindtalk.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


import java.util.ArrayList;

import messenger_project.catchmindtalk.Item.FriendListItem;
import messenger_project.catchmindtalk.MyDatabaseOpenHelper;
import messenger_project.catchmindtalk.R;
import messenger_project.catchmindtalk.activity.ProfileActivity;
import messenger_project.catchmindtalk.adapter.FriendListAdapter;

import static android.app.Activity.RESULT_OK;

public class FriendListFragment extends Fragment {

    public Cursor cursor;
    public MyDatabaseOpenHelper db;
    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;
    public FriendListAdapter myListAdapter;
    public static final int sendVideoCall = 235711;
    ListView lv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.friend_list_fragment, container, false);

        ArrayList<FriendListItem> ListData = new ArrayList<>();
        ArrayList<FriendListItem> FListData = new ArrayList<>();


        String myId = getArguments().getString("userId");
        String myNickname = getArguments().getString("nickname");
        String myProfile = getArguments().getString("profile");
        String myMessage = getArguments().getString("message");

        mPref = getActivity().getSharedPreferences("login",getActivity().MODE_PRIVATE);

        FriendListItem myItem = new FriendListItem(myId,myNickname,myProfile,myMessage);

        db = new MyDatabaseOpenHelper(getContext(),"catchMindTalk",null,1);
        Cursor cursor = db.getList();

        while(cursor.moveToNext()) {

            FriendListItem addItem = new FriendListItem(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3));
            ListData.add(addItem);
            if(cursor.getInt(4) == 1){
                FListData.add(addItem);
            }

            Log.d("커서 내용물", cursor.getString(0)+"#####"+cursor.getString(1) + "" +cursor.getString(2));
        }


        lv = (ListView) rootView.findViewById(R.id.friendListView);

        myListAdapter = (new FriendListAdapter(getActivity().getApplicationContext(),myItem,FListData,ListData));

        lv.setAdapter(myListAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String userId =(String) view.getTag(R.id.userId);
                String nickname =(String) view.getTag(R.id.nickname);
                String profile =(String) view.getTag(R.id.profile);
                String message = (String) view.getTag(R.id.message);

                if(userId.equals("")){
                    return;
                }

                Intent intent = new Intent(getActivity().getApplicationContext(), ProfileActivity.class);
                intent.putExtra("position",position);
                intent.putExtra("userId",userId);
                intent.putExtra("nickname",nickname);
                intent.putExtra("profile",profile);
                intent.putExtra("message",message);
                startActivityForResult(intent,1234);

            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        String myId = getArguments().getString("userId");
        String myNickname = mPref.getString("nickname","노닉네임");
        String myProfile = getArguments().getString("profile");
        String myMessage = mPref.getString("message","노메세지");


        FriendListItem myItem= new FriendListItem(myId,myNickname,myProfile,myMessage);

        myListAdapter.changeMyItem(myItem);

        ArrayList<FriendListItem> ListData = new ArrayList<FriendListItem>();
        ArrayList<FriendListItem> FListData = new ArrayList<FriendListItem>();

        db = new MyDatabaseOpenHelper(getContext(),"catchTalk",null,1);
        Cursor cursor = db.getList();

        while(cursor.moveToNext()) {

            FriendListItem addItem = new FriendListItem(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3));
            ListData.add(addItem);
            if(cursor.getInt(4) == 1){
                FListData.add(addItem);
            }

        }

        myListAdapter.ChangeList(FListData,ListData);
        myListAdapter.sizeReset();
        myListAdapter.notifyDataSetChanged();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        Log.d("태프원",resultCode+"");

        if(resultCode == RESULT_OK) {
//            Toast.makeText(getContext(), requestCode + "###" + resultCode, Toast.LENGTH_SHORT).show();
            String friendId = data.getExtras().getString("friendId");
            String nickname = data.getExtras().getString("nickname");
            STA.sendToActivity(friendId,nickname);
        }

//        if(resultCode == sendVideoCall){
//            String friendId = data.getExtras().getString("friendId");
//            String roomId = data.getExtras().getString("roomId");
//            Toast.makeText(getContext(),friendId,Toast.LENGTH_SHORT).show();
//
//
//        }
    }



    public interface sendToActivity {
        void sendToActivity(String friendId, String nickname);
    }

    sendToActivity STA;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivity MA = (MainActivity) context;
        try {
            STA = (sendToActivity) MA;
        } catch (ClassCastException e) {
            throw new ClassCastException(MA.toString() + " must implement onSomeEventListener");
        }
    }


}