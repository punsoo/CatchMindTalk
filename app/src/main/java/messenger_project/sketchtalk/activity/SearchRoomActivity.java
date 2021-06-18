package messenger_project.sketchtalk.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import messenger_project.sketchtalk.Item.ChatRoomItem;
import messenger_project.sketchtalk.MyDatabaseOpenHelper;
import messenger_project.sketchtalk.R;
import messenger_project.sketchtalk.adapter.SearchRoomAdapter;


public class SearchRoomActivity extends AppCompatActivity {


    ListView roomList;
    ImageView backBtn;
    ImageView searchBtn;
    EditText editText;

    SearchRoomAdapter searchRoomAdapter;

    ArrayList<ChatRoomItem> searchRoomList;
    ArrayList<ChatRoomItem> allList;

    HashMap<String, String> nicknameList;

    public MyDatabaseOpenHelper db;

    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_room);

        mPref = getSharedPreferences("login", MODE_PRIVATE);
        String myId = mPref.getString("userId", "닉없음");

        backBtn = (ImageView) findViewById(R.id.search_room_back);
        editText = (EditText) findViewById(R.id.search_room_editText);
        searchBtn = (ImageView) findViewById(R.id.search_room_search);

        roomList = (ListView) findViewById(R.id.list_room_search);

        searchRoomList = new ArrayList<>();
        allList = new ArrayList<>();

        nicknameList = new HashMap<>();

        db = new MyDatabaseOpenHelper(this, "catchMindTalk", null, 1);

        Cursor CRC = db.getChatRoomListJoinOnMessageList(myId); //ChatRoomCursor

        while (CRC.moveToNext()) {

            int UnreadNum = db.getChatRoomUnReadNum(myId, CRC.getInt(0), CRC.getString(1), CRC.getLong(2));
            Vector<String[]> ChatRoomMemberList = db.getChatRoomMemberList(CRC.getInt(0), CRC.getString(1));

            ChatRoomItem addItem = new ChatRoomItem(CRC.getInt(0), CRC.getString(1), CRC.getLong(2), CRC.getString(3), CRC.getInt(4), CRC.getString(10), CRC.getLong(11), CRC.getInt(12), ChatRoomMemberList, UnreadNum);
            searchRoomList.add(addItem);
            allList.add(addItem);


        }


        searchRoomAdapter = new SearchRoomAdapter(this, searchRoomList, myId);
        roomList.setAdapter(searchRoomAdapter);


        final TextWatcher editTextWatcher = new TextWatcher() {


            @Override
            public void afterTextChanged(Editable s) {

                searchRoomAdapter.notifyDataSetChanged();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String editContent = editText.getText().toString();
                searchRoomAdapter.clearList();

                for (int i = 0; i < allList.size(); i++) {

                    Vector<String[]> ChatRoomMemberList = allList.get(i).getChatRoomMemberList();
                    boolean isContained = false;
                    for (int j = 0; j < ChatRoomMemberList.size(); j++) {
                        if(ChatRoomMemberList.get(j)[1].contains(editContent)){
                            isContained = true;
                            break;
                        }
                    }
                    if(isContained) {
                        searchRoomAdapter.addCRItem(allList.get(i));
                    }

                }
            }


        };

        editText.addTextChangedListener(editTextWatcher);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        roomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int roomId = (int) view.getTag(R.id.roomId);
                String friendId = (String) view.getTag(R.id.friendId);
                String roomName = (String) view.getTag(R.id.roomName);



                Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                intent.putExtra("roomId",roomId);
                intent.putExtra("friendId",friendId);
                intent.putExtra("roomName",roomName);

                startActivity(intent);
                finish();
            }
        });


    }

}