package com.microsoft.o365_android_microsoft_graph_snippets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.microsoft.office365.microsoftgraphvos.EmailAddressVO;
import com.microsoft.office365.microsoftgraphvos.ItemBodyVO;
import com.microsoft.office365.microsoftgraphvos.MessageVO;
import com.microsoft.office365.microsoftgraphvos.MessageWrapperVO;
import com.microsoft.office365.microsoftgraphvos.RecipientVO;
import com.microsoft.office365.msgraphapiservices.MSGraphMailService;
import com.microsoft.office365.msgraphsnippetapp.R;
import com.microsoft.office365.msgraphsnippetapp.application.SnippetApp;
import com.microsoft.office365.msgraphsnippetapp.util.SharedPrefsUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ListBirthdayActivity extends Activity {

    int selectedContact = 1;
    CustomAdapter adapter;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_birthday);

        ArrayList<String> list = this.getIntent().getStringArrayListExtra("list");
        adapter = new CustomAdapter(this.getApplicationContext(), android.R.id.text1,
                list);

        listView = (ListView) findViewById(R.id.birthdayListView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("2222", "Item Clicked: " +adapterView.getAdapter().getItem(i));
                ListBirthdayActivity.this.selectedContact = i;
                ListBirthdayActivity.this.dispatchTakeVideoIntent();
            }
        });


    }

    static final int REQUEST_VIDEO_CAPTURE = 1;
    static final int REQUEST_EMAIL = 2;

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("2222", " " + requestCode +" "+resultCode);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            //mVideoView.setVideoURI(videoUri);
            ListBirthdayActivity.this.postToYouTube(videoUri);
        } else if (requestCode == REQUEST_EMAIL) {
            View view = this.getViewByPosition(this.selectedContact, this.listView);
            view.setBackgroundColor(Color.GREEN);

            sendEmail();
        }
    }

    private void sendEmail() {

        // Get a context so we can interrogate Resources & SharedPreferences
        SnippetApp app = SnippetApp.getApp();
        SharedPreferences prefs = SharedPrefsUtil.getSharedPreferences();

        MSGraphMailService service = create(MSGraphMailService.class);

        // load the contents
        String subject = "Did you get my Video Message?";
        String body = "Happy Birthday... enjoy Birthday Video that I sent you.";
        //String email = "sagar@androidBot.onmicrosoft.com";//getEmail();

        String email = getEmail();
        Log.d("2222", "send to "+email);
        // make it
        MessageWrapperVO msgWrapper = createMessage(subject, body, email);

        // send it
        service.createNewMail("v1.0", msgWrapper, getSetUpCallback());
        //service.createNewMail("v1.0", msgWrapper, null);
    }

    private static <T> T create(Class<T> clazz) {
        return SnippetApp.getApp().getRestAdapter().create(clazz);
    }

    private retrofit.Callback<Response> getSetUpCallback() {
        return  new Callback<Response>() {
            @Override
            public void success(Response aVoid, Response response) {
                Log.d("2222","email success");
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("2222", "email fail");
            }
        } ;
    }

    private static MessageWrapperVO createMessage(
            String msgSubject,
            String msgBody,
            String... msgRecipients) {
        MessageVO msg = new MessageVO();

        // add the recipient
        RecipientVO recipient;
        for (int ii = 0; ii < msgRecipients.length; ii++) {
            // if the recipient array does not exist, new one up
            if (null == msg.toRecipients) {
                msg.toRecipients = new RecipientVO[msgRecipients.length];
            }
            // allocate a new recipient
            recipient = new RecipientVO();
            // give them an email address
            recipient.emailAddress = new EmailAddressVO();
            // set that address to be the currently iterated-upon recipient string
            recipient.emailAddress.address = msgRecipients[ii];
            // add it to the array at the position
            msg.toRecipients[ii] = recipient;
        }

        // set the subject
        msg.subject = msgSubject;

        // create the body
        ItemBodyVO body = new ItemBodyVO();
        body.contentType = ItemBodyVO.CONTENT_TYPE_TEXT;
        body.content = msgBody;
        msg.body = body;


        MessageWrapperVO wrapper = new MessageWrapperVO();
        wrapper.message = msg;
        wrapper.saveToSentItems = true;
        return wrapper;
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    private void postToYouTube(Uri videoUri) {
        /*ContentValues content = new ContentValues(4);
        content.put(Video.VideoColumns.DATE_ADDED,
                System.currentTimeMillis() / 1000);
        content.put(Video.Media.MIME_TYPE, "video/mp4");
        content.put(MediaStore.Video.Media.DATA, "video_path");
        ContentResolver resolver = getBaseContext().getContentResolver();
        Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, content);*/

        String email = getEmail();
        String name = getName();


        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Happy Birthday "+name);
        sharingIntent.putExtra(Intent.EXTRA_TEXT,"Hey, Check out the video I created just for you on your birthday!!!");
        String[] array = new String[1];
        array[0] = email;
        sharingIntent.putExtra(Intent.EXTRA_EMAIL, array);
        sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM,videoUri);
        sharingIntent.setType("text/plain");
        startActivityForResult(Intent.createChooser(sharingIntent,"share:"),REQUEST_EMAIL);
    }

    private String getEmail() {
        String data = this.adapter.getItem(this.selectedContact);
        String email = "";
        try {
            JSONObject dataJson = new JSONObject(data);
            JSONArray emails = dataJson.getJSONArray("emailAddresses");
            JSONObject emailAdr = emails.getJSONObject(0);
            email = emailAdr.getString("address");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return email;
    }

    public String getName() {
        String data = this.adapter.getItem(this.selectedContact);
        String email = "";
        try {
            JSONObject dataJson = new JSONObject(data);
            email = dataJson.getString("displayName");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return email;
    }


    public class CustomAdapter extends ArrayAdapter<String>{

        List<String> data;

        @Override
        public int getCount() {
            return data.size();
        }

        public List<String> getData() {
            return data;
        }

        @Override
        public String getItem(int position) {
            return this.data.get(position);
        }

        public CustomAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            this.data = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);

            TextView text = (TextView) convertView.findViewById(android.R.id.text1);
            try {
                JSONObject json = new JSONObject(data.get(position));
                text.setTextColor(Color.BLACK);
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                text.setText(json.getString("displayName"));

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return convertView;
        }
    }

}
