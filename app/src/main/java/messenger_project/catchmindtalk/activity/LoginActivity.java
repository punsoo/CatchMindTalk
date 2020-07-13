package messenger_project.catchmindtalk.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import messenger_project.catchmindtalk.MyDatabaseOpenHelper;
import messenger_project.catchmindtalk.R;


public class LoginActivity extends AppCompatActivity {

    public EditText userId,password;
    public String sUserId,sPassword;
    public MyDatabaseOpenHelper db;
    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;
    String ServerURL = "http://ec2-54-180-196-239.ap-northeast-2.compute.amazonaws.com";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userId = (EditText) findViewById(R.id.userIdInput);
        password = (EditText) findViewById(R.id.passwordInput);
        sUserId = userId.getText().toString();
        sPassword = password.getText().toString();
        mPref = getSharedPreferences("login",MODE_PRIVATE);
        editor = mPref.edit();


        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 1234);
            }

        }


//        if(mPref.getBoolean("autoLogin",false) == true){
//
//            Intent serviceIntent = new Intent(getApplicationContext(),ChatService.class);
//            serviceIntent.putExtra("FromLogin",true);
//            startService(serviceIntent);
//
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
//            finish();
//
//        }


    }


    public void login(View view) {


        sUserId = userId.getText().toString();
        sPassword = password.getText().toString();

        loginAT LAT = new loginAT(false);
        LAT.execute();

    }

    public void signUp(View view){

        Intent intent = new Intent(this,SignUpActivity.class);
        startActivity(intent);

    }


    public class loginAT extends AsyncTask<Void, Integer, String> {

        ProgressDialog asyncDialog = new ProgressDialog(LoginActivity.this);
        public boolean autoLoginMode;

        public loginAT(boolean ALM){
            this.autoLoginMode = ALM;
        }

        @Override
        protected void onPreExecute() {
            if(!this.autoLoginMode) {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                asyncDialog.setMessage("로그인 중입니다...");

                // show dialog
                asyncDialog.show();

            }
            super.onPreExecute();
        }




        @Override
        protected String doInBackground(Void... unused) {


            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){

            }


            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId=" + sUserId + "&password=" + sPassword + "";
            try {
                /* 서버연결 */
                Log.d("전전",sUserId + sPassword);
                URL url = new URL(ServerURL+"/login.php");
                Log.d("후후",sUserId + sPassword);
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


                /* 서버 -> 안드로이드 파라메터값 전달 */
//                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
//                    return null;

                InputStream is = null;
                BufferedReader in = null;
                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is));
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                    int maxLogSize = 1000;
                    for(int i = 0; i <= line.length() / maxLogSize; i++) {
                        int start = i * maxLogSize;
                        int end = (i+1) * maxLogSize;
                        end = end > line.length() ? line.length() : end;
                    }


                }
                data = buff.toString().trim();
                int maxLogSize = 1000;
                for(int i = 0; i <= data.length() / maxLogSize; i++) {
                    int start = i * maxLogSize;
                    int end = (i+1) * maxLogSize;
                    end = end > data.length() ? data.length() : end;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            if(!this.autoLoginMode) {
                asyncDialog.dismiss();
            }
            login_check(s);

        }

    }

    public void login_check(String data){


        if(data.equals("비밀번호")){
            Toast.makeText(this,"비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
        }else if(data.equals("아이디")){
            Toast.makeText(this,"일치하는 아이디가 없습니다",Toast.LENGTH_SHORT).show();
        }else{
            try {

                db = new MyDatabaseOpenHelper(this, "catchMindTalk", null, 1);

                db.clearFriendList();
                db.createFriendList();
                db.createChatRoomList();
                db.createChatMessageList(sUserId);

                JSONArray dataArray = new JSONArray(data);
                JSONArray friendListArray = dataArray.getJSONArray(0);
                JSONArray chatRoomListArray = dataArray.getJSONArray(1);




                for(int i=0;i<friendListArray.length();i++) {


                    JSONObject jobject = new JSONObject(friendListArray.get(i).toString());

                    String friendId = (String) jobject.getString("friendId");
                    String nickname = (String) jobject.getString("nickname");
                    String profileMessage = (String) jobject.getString("profileMessage");
                    String profileImageUpdateTime = (String) jobject.getString("profileImageUpdateTime");
                    int bookmark = (int) jobject.getInt("bookmark");
                    Log.d("friendListArray", friendId+" | "+nickname+" | "+profileMessage+" | "+profileImageUpdateTime+" | "+bookmark);

                    db.insertFriendData(friendId,nickname,profileMessage,profileImageUpdateTime,bookmark);

                    if(i==0){
                        editor.putString("userId",friendId);
                        editor.putString("nickname",nickname);
                        editor.putString("profileMessage",profileMessage);
                        editor.putString("profileImageUpdateTime",profileImageUpdateTime);
                        editor.commit();
                    }

                }


                for(int i=0;i<chatRoomListArray.length();i++) {

                    JSONObject jobject = new JSONObject(chatRoomListArray.get(i).toString());

                    String roomId = (String) jobject.getString("roomId");
                    Log.d("chatRoomListArray", roomId);

                    db.insertChatRoomData(roomId);

                }



                editor.putBoolean("autoLogin",true);
                editor.putString("autoLoginId",sUserId);
                editor.putString("autoLoginPassword",sPassword);

                editor.commit();

//                Intent serviceIntent = new Intent(getApplicationContext(),ChatService.class);
//                startService(serviceIntent);

//                Intent intent = new Intent(this, MainActivity.class);
//                startActivity(intent);
//                finish();

            }catch(JSONException e){

            }
        }




    }

}