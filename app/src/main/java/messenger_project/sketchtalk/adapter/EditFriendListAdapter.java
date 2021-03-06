package messenger_project.sketchtalk.adapter;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import messenger_project.sketchtalk.Item.FriendListItem;
import messenger_project.sketchtalk.MyDatabaseOpenHelper;
import messenger_project.sketchtalk.R;
import messenger_project.sketchtalk.viewholder.FriendViewHolder;

import static android.content.Context.MODE_PRIVATE;

public class EditFriendListAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList

    public String ServerURL;
    public ArrayList<FriendListItem> listData = new ArrayList<>() ;
    public ArrayList<FriendListItem> FlistData = new ArrayList<>() ;
    public ArrayList<FriendListItem> allListData = new ArrayList<>() ;
    public ArrayList<FriendListItem> allFlistData = new ArrayList<>() ;
    public Context mContext;
    public LayoutInflater inflater ;
    public int FlistSize;
    public int listSize;
    View tmp_view;
    public MyDatabaseOpenHelper db;
    public SharedPreferences mPref;
    String myId;


    // ListViewAdapter의 생성자
    public EditFriendListAdapter(Context context,ArrayList<FriendListItem> allFListData,ArrayList<FriendListItem> allListData, ArrayList<FriendListItem> FListData,ArrayList<FriendListItem> ListData) {
        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listData = ListData;
        this.allFlistData = allFListData;
        this.allListData = allListData;
        this.FlistData = FListData;
        this.listSize = ListData.size();
        this.FlistSize = FListData.size();
        this.db = new MyDatabaseOpenHelper(mContext,"catchMindTalk",null,1);
        this.mPref = mContext.getSharedPreferences("login",MODE_PRIVATE);
        this.myId = this.mPref.getString("userId","아이디없음");
        this.ServerURL = context.getResources().getString(R.string.ServerUrl);

    }



    final View.OnClickListener deleteListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            tmp_view = v;
            String userId = (String) tmp_view.getTag(R.id.userId);
            String nickname = (String) tmp_view.getTag(R.id.nickname);
            final FriendListItem friendListItem = (FriendListItem) tmp_view.getTag(R.id.friendListItem);

            AlertDialog.Builder alt_bld = new AlertDialog.Builder(mContext);
            alt_bld.setCancelable(true);
            alt_bld.setTitle("알림");
            alt_bld.setMessage(nickname+"님을 친구목록에서 삭제하시겠습니까?");
            alt_bld.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id){

                    dialog.cancel();

                }
            });
            alt_bld.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {


                    String userId = (String) tmp_view.getTag(R.id.userId);
                    listData.remove(friendListItem);
                    allListData.remove(friendListItem);
                    if(allFlistData.contains(friendListItem)){
                        allFlistData.remove(friendListItem);
                        FlistData.remove(friendListItem);
                    }

                    db.deleteFriend(userId);
                    sizeReset();
                    DeleteThread dt = new DeleteThread(myId,userId);
                    dt.start();
                    notifyDataSetChanged();


                }
            });

            AlertDialog alert = alt_bld.create();

            alert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            alert.setCanceledOnTouchOutside(false);
            alert.show();

            Button pbtn = alert.getButton(DialogInterface.BUTTON_POSITIVE);
            pbtn.setTextColor(Color.BLACK);

            Button negbtn = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            negbtn.setTextColor(Color.BLACK);
        }
    };

    final View.OnClickListener favoriteListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            tmp_view = v;
            String userId = (String) tmp_view.getTag(R.id.userId);
            String nickname = (String) tmp_view.getTag(R.id.nickname);
            String mode = (String) tmp_view.getTag(R.id.favoriteMode);
            FriendListItem friendListItem = (FriendListItem) tmp_view.getTag(R.id.friendListItem);

            if(mode.equals("add")){

                if (allFlistData.contains(friendListItem)) {

                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(mContext);
                    alt_bld.setCancelable(true);
                    alt_bld.setTitle("알림");
                    alt_bld.setMessage(nickname + "님은 이미 즐겨찾기에 추가되어있습니다");
                    alt_bld.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                        }
                    });

                    AlertDialog alert = alt_bld.create();

                    alert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    alert.setCanceledOnTouchOutside(false);
                    alert.show();

                    Button pbtn = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                    pbtn.setTextColor(Color.BLACK);

                    return;

                }

                allFlistData.add(friendListItem);
                FlistData.add(friendListItem);
                favoriteThread ft = new favoriteThread(myId,userId,1);
                ft.start();
                sizeReset();
                db.updateFriendListFavorite(userId,1);
                notifyDataSetChanged();

            }else{

                allFlistData.remove(friendListItem);
                FlistData.remove(friendListItem);
                db.updateFriendListFavorite(userId,0);
                sizeReset();
                favoriteThread bt = new favoriteThread(myId,userId,0);
                bt.start();
                notifyDataSetChanged();

            }



        }
    };



    public void clearList() {
        listData.clear();
        FlistData.clear();
    }

    public void addItem(FriendListItem friendListItem) {
        listData.add(friendListItem);
    }
    public void addFItem(FriendListItem friendListItem) {
        FlistData.add(friendListItem);
    }

    public void sizeReset(){
        this.listSize = listData.size();
        this.FlistSize = FlistData.size();
    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        int total;
        if(FlistSize >0){
            total = 1+FlistSize+1+listSize;
        }else{
            total = 1+listSize;
        }
        return total ;
    }

//    @Override
//    public boolean isEnabled(int position) {
//       if(position <5){
//           return false;
//       }else{
//           return true;
//       }
//    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        FriendViewHolder viewHolder;
        String friendId = "";
        String nickname = "";
        String profileImageUpdateTime = "";


        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.friendlist_item_edit, parent, false);

            viewHolder = new FriendViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.profile_image_view);
            viewHolder.nickname = (TextView) convertView.findViewById(R.id.NicknameTextView);
            viewHolder.section = (LinearLayout) convertView.findViewById(R.id.sectionHeader);
            viewHolder.sectionTxt = (TextView) convertView.findViewById(R.id.sectionText);
            viewHolder.deleteBtn = (Button) convertView.findViewById(R.id.friendDelete);
            viewHolder.favoriteBtn = (Button) convertView.findViewById(R.id.friendFavorite);
            viewHolder.profile_container = (RelativeLayout) convertView.findViewById(R.id.profile_container);
            viewHolder.deleteBtn.setOnClickListener(deleteListener);
            viewHolder.favoriteBtn.setOnClickListener(favoriteListener);

            convertView.setTag(viewHolder);

        }else{
            viewHolder = (FriendViewHolder) convertView.getTag();
        }



        if (this.FlistSize > 0) {

            if (position == 0) {
                viewHolder.sectionTxt.setText("즐겨찾기");
                viewHolder.section.setVisibility(View.VISIBLE);
                viewHolder.profile_container.setVisibility(View.GONE);

            } else if (position < (1 + FlistSize)) {

                viewHolder.nickname.setText(FlistData.get(position - 1).getNickname());
                viewHolder.section.setVisibility(View.GONE);
                viewHolder.profile_container.setVisibility(View.VISIBLE);
                friendId = FlistData.get(position-1).getId();
                nickname = FlistData.get(position-1).getNickname();
                profileImageUpdateTime = FlistData.get(position-1).getProfileImageUpdateTime();
                if(profileImageUpdateTime.equals("none")){
                    viewHolder.icon.setImageResource(R.drawable.default_profile_image);
                }else {
                    Glide.with(mContext).load(ServerURL+"/profile_image/" + friendId + ".png")
                            .error(R.drawable.default_profile_image)
                            .signature(new ObjectKey(profileImageUpdateTime))
                            .into(viewHolder.icon);
                }
                viewHolder.deleteBtn.setVisibility(View.GONE);

                viewHolder.favoriteBtn.setTag(R.id.favoriteMode,"remove");
                viewHolder.favoriteBtn.setTag(R.id.userId,friendId);
                viewHolder.favoriteBtn.setTag(R.id.nickname,nickname);
                viewHolder.favoriteBtn.setTag(R.id.friendListItem,FlistData.get(position-1));
                viewHolder.favoriteBtn.setText("즐겨찾기 해제");


            } else if (position == (1 + FlistSize)) {
                viewHolder.sectionTxt.setText("친구");
                viewHolder.section.setVisibility(View.VISIBLE);
                viewHolder.profile_container.setVisibility(View.GONE);

            } else {
                viewHolder.nickname.setText(listData.get(position - 2 - FlistSize).getNickname());
                viewHolder.section.setVisibility(View.GONE);
                viewHolder.profile_container.setVisibility(View.VISIBLE);
                friendId = listData.get(position-2-FlistSize).getId();
                nickname = listData.get(position-2-FlistSize).getNickname();
                profileImageUpdateTime = listData.get(position-2-FlistSize).getProfileImageUpdateTime();
                if(profileImageUpdateTime.equals("none")){
                    viewHolder.icon.setImageResource(R.drawable.default_profile_image);
                }else {
                    Glide.with(mContext).load(ServerURL+"/profile_image/" + friendId + ".png")
                            .error(R.drawable.default_profile_image)
                            .signature(new ObjectKey(profileImageUpdateTime))
                            .into(viewHolder.icon);
                }
                viewHolder.deleteBtn.setVisibility(View.VISIBLE);
                viewHolder.deleteBtn.setTag(R.id.userId,friendId);
                viewHolder.deleteBtn.setTag(R.id.nickname,nickname);

                viewHolder.favoriteBtn.setTag(R.id.friendListItem,listData.get(position-2-FlistSize));
                viewHolder.favoriteBtn.setTag(R.id.favoriteMode,"add");
                viewHolder.favoriteBtn.setTag(R.id.userId,friendId);
                viewHolder.favoriteBtn.setTag(R.id.nickname,nickname);
                viewHolder.favoriteBtn.setText("즐겨찾기 등록");

            }


        } else {

            if (position == 0) {
                viewHolder.sectionTxt.setText("친구");
                viewHolder.section.setVisibility(View.VISIBLE);
                viewHolder.profile_container.setVisibility(View.GONE);

            } else {
                viewHolder.nickname.setText(listData.get(position - 1).getNickname());
                viewHolder.section.setVisibility(View.GONE);
                viewHolder.profile_container.setVisibility(View.VISIBLE);
                friendId = listData.get(position-1).getId();
                nickname = listData.get(position-1).getNickname();
                profileImageUpdateTime = listData.get(position-1).getProfileImageUpdateTime();

                if(profileImageUpdateTime.equals("none")){
                    viewHolder.icon.setImageResource(R.drawable.default_profile_image);
                }else {
                    Glide.with(mContext).load(ServerURL+"/profile_image/" + friendId + ".png")
                            .error(R.drawable.default_profile_image)
                            .signature(new ObjectKey(profileImageUpdateTime))
                            .into(viewHolder.icon);
                }

                viewHolder.deleteBtn.setVisibility(View.VISIBLE);
                viewHolder.deleteBtn.setTag(R.id.userId,friendId);
                viewHolder.deleteBtn.setTag(R.id.nickname,nickname);
                viewHolder.deleteBtn.setTag(R.id.friendListItem,listData.get(position-1));

                viewHolder.favoriteBtn.setTag(R.id.friendListItem,listData.get(position-1));
                viewHolder.favoriteBtn.setTag(R.id.favoriteMode,"add");
                viewHolder.favoriteBtn.setTag(R.id.userId,friendId);
                viewHolder.favoriteBtn.setTag(R.id.nickname,nickname);
                viewHolder.favoriteBtn.setText("즐겨찾기 등록");

            }

        }



        convertView.setTag(R.id.userId,friendId);
        convertView.setTag(R.id.nickname,nickname);
        convertView.setTag(R.id.profileImageUpdateTime,profileImageUpdateTime);

        Log.d("힘들다",""+position);

        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listData.get(position) ;
    }

    public class DeleteThread extends Thread {

        String sUserId;
        String sFriendId;

        public DeleteThread(String userId,String friendId) {
            this.sUserId = userId;
            this.sFriendId = friendId;
        }


        @Override
        public void run() {

            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId=" + sUserId + "&friendId=" + sFriendId + "";
            try {
                /* 서버연결 */
                URL url = new URL(ServerURL + "/deleteFriend.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                /* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

                InputStream is = null;
                BufferedReader in = null;

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }



    public class favoriteThread extends Thread {

        String sUserId;
        String sFriendId;
        int sMode;

        public favoriteThread(String userId,String friendId,int mode) {
            this.sUserId = userId;
            this.sFriendId = friendId;
            this.sMode = mode;
        }


        @Override
        public void run() {

            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId=" + sUserId + "&friendId=" + sFriendId + "&mode=" +sMode +"";
            try {
                /* 서버연결 */
                URL url = new URL(ServerURL + "/favoriteFriend.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                /* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

                InputStream is = null;
                BufferedReader in = null;

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();
                Log.d("favoriteThreadResult",data.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }



    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
//    public void addItem(String id,Drawable icon, String name, String message) {
//        ListViewItem item = new ListViewItem(id,name,message);
//
//        item.setIcon(icon);
//        item.setName(name);
//        item.setMessage(message);
//
//        listData.add(item);
//    }
}



