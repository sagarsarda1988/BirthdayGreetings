/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.microsoft.office365.msgraphsnippetapp;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.o365_android_microsoft_graph_snippets.ListBirthdayActivity;
import com.microsoft.office365.msgraphsnippetapp.snippet.AbstractSnippet;
import com.microsoft.office365.msgraphsnippetapp.snippet.SnippetContent;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;
import timber.log.Timber;

import static android.R.layout.simple_spinner_dropdown_item;
import static android.R.layout.simple_spinner_item;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.microsoft.office365.msgraphsnippetapp.R.id.btn_run;
import static com.microsoft.office365.msgraphsnippetapp.R.id.progressbar;
import static com.microsoft.office365.msgraphsnippetapp.R.id.spinner;
import static com.microsoft.office365.msgraphsnippetapp.R.id.txt_desc;
import static com.microsoft.office365.msgraphsnippetapp.R.id.txt_hyperlink;
import static com.microsoft.office365.msgraphsnippetapp.R.id.txt_request_url;
import static com.microsoft.office365.msgraphsnippetapp.R.id.txt_response_body;
import static com.microsoft.office365.msgraphsnippetapp.R.id.txt_response_headers;
import static com.microsoft.office365.msgraphsnippetapp.R.id.txt_status_code;
import static com.microsoft.office365.msgraphsnippetapp.R.id.txt_status_color;
import static com.microsoft.office365.msgraphsnippetapp.R.string.clippy;
import static com.microsoft.office365.msgraphsnippetapp.R.string.req_url;
import static com.microsoft.office365.msgraphsnippetapp.R.string.response_body;
import static com.microsoft.office365.msgraphsnippetapp.R.string.response_headers;

public class SnippetDetailFragment<T, Result>
        extends BaseFragment implements Callback<Result> {

    public static final String ARG_ITEM_ID = "item_id";
    private static final int UNSET = -1;
    private static final String STATUS_COLOR = "STATUS_COLOR";

    @InjectView(txt_status_code)
    protected TextView mStatusCode;

    @InjectView(txt_status_color)
    protected View mStatusColor;

    @InjectView(txt_desc)
    protected TextView mSnippetDescription;

    @InjectView(txt_request_url)
    protected TextView mRequestUrl;

    @InjectView(txt_response_headers)
    protected TextView mResponseHeaders;

    @InjectView(txt_response_body)
    protected TextView mResponseBody;

    @InjectView(spinner)
    protected Spinner mSpinner;

    @InjectView(progressbar)
    protected ProgressBar mProgressbar;

    @InjectView(btn_run)
    protected Button mRunButton;

    boolean setupDidRun = false;
    private AbstractSnippet<T, Result> mItem;

    public SnippetDetailFragment() {
    }

    @OnClick(txt_request_url)
    public void onRequestUrlClicked(TextView tv) {
        clipboard(tv);
    }

    @OnClick(txt_response_headers)
    public void onResponseHeadersClicked(TextView tv) {
        clipboard(tv);
    }

    @OnClick(txt_response_body)
    public void onResponseBodyClicked(TextView tv) {
        clipboard(tv);
    }

    private void clipboard(TextView tv) {
        int which;
        switch (tv.getId()) {
            case txt_request_url:
                which = req_url;
                break;
            case txt_response_headers:
                which = response_headers;
                break;
            case txt_response_body:
                which = response_body;
                break;
            default:
                which = UNSET;
        }
        String what = which == UNSET ? "" : getString(which) + " ";
        what += getString(clippy);
        Toast.makeText(getActivity(), what, Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // old way
            ClipboardManager clipboardManager = (ClipboardManager)
                    getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setText(tv.getText());
        } else {
            clipboard11(tv);
        }
    }

    @TargetApi(11)
    private void clipboard11(TextView tv) {
        android.content.ClipboardManager clipboardManager =
                (android.content.ClipboardManager) getActivity()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("RESTSnippets", tv.getText());
        clipboardManager.setPrimaryClip(clipData);
    }

    @OnClick(btn_run)
    public void onRunClicked(Button btn) {
        mRequestUrl.setText("");
        mResponseHeaders.setText("");
        mResponseBody.setText("");
        displayStatusCode("", getResources().getColor(R.color.transparent));
        mProgressbar.setVisibility(VISIBLE);
        mItem.request(mItem.mService, this);
    }

    @OnClick(txt_hyperlink)
    public void onDocsLinkClicked(TextView textView) {
        launchUrl(Uri.parse(mItem.getUrl()));
    }

    private void launchUrl(Uri uri) {
        Intent viewDocs = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(viewDocs);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = (AbstractSnippet<T, Result>)
                    SnippetContent.ITEMS.get(getArguments().getInt(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_snippet_detail, container, false);
        ButterKnife.inject(this, rootView);
        mSnippetDescription.setText(mItem.getDescription());
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mStatusColor.getTag()) {
            outState.putInt(STATUS_COLOR, (Integer) mStatusColor.getTag());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null != getActivity() && getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (null != activity.getSupportActionBar()) {
                activity.getSupportActionBar().setTitle(mItem.getName());
            }
        }
        if (null != savedInstanceState && savedInstanceState.containsKey(STATUS_COLOR)) {
            int statusColor = savedInstanceState.getInt(STATUS_COLOR, UNSET);
            if (UNSET != statusColor) {
                mStatusColor.setBackgroundColor(statusColor);
                mStatusColor.setTag(statusColor);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!setupDidRun) {
            setupDidRun = true;
            mProgressbar.setVisibility(View.VISIBLE);
            mItem.setUp(AbstractSnippet.sServices, getSetUpCallback());
        }
    }

    private retrofit.Callback<String[]> getSetUpCallback() {
        return new retrofit.Callback<String[]>() {
            @Override
            public void success(String[] strings, Response response) {
                if (isAdded()) {
                    mProgressbar.setVisibility(View.GONE);
                    populateSpinner(strings);
                    mRunButton.setEnabled(true);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isAdded()) {
                    displayThrowable(error.getCause());
                    mProgressbar.setVisibility(View.GONE);
                }
            }
        };
    }

    private void populateSpinner(String[] strings) {
        ArrayAdapter<String> spinnerArrayAdapter
                = new ArrayAdapter<>(
                getActivity(),
                simple_spinner_item,
                strings);
        spinnerArrayAdapter.setDropDownViewResource(simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerArrayAdapter);
    }

    @Override
    public void success(Result result, Response response) {
        if (!isAdded()) {
            // the user has left...
            return;
        }
        mProgressbar.setVisibility(GONE);
        displayResponse(response);
    }

    private void displayResponse(Response response) {
        int color = getColor(response);
        displayStatusCode(Integer.toString(response.getStatus()), getResources().getColor(color));
        displayRequestUrl(response);
        maybeDisplayResponseHeaders(response);
        maybeDisplayResponseBody(response);


    }

    private void maybeDisplayResponseBody(Response response) {
        if (null != response.getBody()) {
            String body = null;
            InputStream is = null;
            try {
                is = response.getBody().in();
                body = IOUtils.toString(is);
                String formattedJson = new JSONObject(body).toString(2);
                mResponseBody.setText(formattedJson);
                getBirthdayList(formattedJson);
                Log.d("2222" , formattedJson);
            } catch (JSONException e) {
                if (null != body) {
                    // body wasn't JSON
                    mResponseBody.setText(body);
                } else {
                    // set the stack trace as the response body
                    displayThrowable(e);
                }
            } catch (IOException e) {
                e.printStackTrace();
                displayThrowable(e);
            } finally {
                if (null != is) {
                    IOUtils.closeQuietly(is);
                }
            }
        }
    }

    private void getBirthdayList(String formattedJson) {
        try {
            JSONObject response = new JSONObject(formattedJson);
            JSONArray value = response.getJSONArray("value");

            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0, size = value.length(); i < size; i++){
                JSONObject json = value.getJSONObject(i);
                String bday = json.getString("birthday");
                Calendar cal = toCalendar(bday);
                Date now = new Date();
                Log.d("2222", cal.getTime().toString()+ " === "+now.toString());
                if(cal.getTime().getMonth() == now.getMonth() &&
                        cal.getTime().getDate() == now.getDate()){
                    list.add(json.toString());
                    Log.d("2222", "same");
                }

            }

            if(list.size() > 0) {
                Intent intent = new Intent(this.getActivity(), ListBirthdayActivity.class);
                intent.putStringArrayListExtra("list", list);
                this.getActivity().startActivity(intent);
            } else {
                Toast.makeText(this.getActivity(), "No Birthdays Today", Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /** Transform ISO 8601 string to Calendar. */
    public static Calendar toCalendar(final String iso8601string)
            {
        Calendar calendar = GregorianCalendar.getInstance();
        String s = iso8601string.replace("Z", "+00:00");
        try {
            s = s.substring(0, 22) + s.substring(23);  // to get rid of the ":"
        } catch (IndexOutOfBoundsException e) {
        }
                Date date = null;
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(s);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                calendar.setTime(date);
        return calendar;
    }

    private void maybeDisplayResponseHeaders(Response response) {
        if (null != response.getHeaders()) {
            List<Header> headers = response.getHeaders();
            String headerText = "";
            for (Header header : headers) {
                headerText += header.getName() + " : " + header.getValue() + "\n";
            }
            mResponseHeaders.setText(headerText);
        }
    }

    private void displayRequestUrl(Response response) {
        String requestUrl = response.getUrl();
        mRequestUrl.setText(requestUrl);
    }

    private void displayStatusCode(String text, int color) {
        mStatusCode.setText(text);
        mStatusColor.setBackgroundColor(color);
        mStatusColor.setTag(color);
    }

    private void displayThrowable(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String trace = sw.toString();
        mResponseBody.setText(trace);
    }

    private int getColor(Response response) {
        int color;
        switch (response.getStatus() / 100) {
            case 1:
            case 2:
                color = R.color.code_1xx;
                break;
            case 3:
                color = R.color.code_3xx;
                break;
            case 4:
            case 5:
                color = R.color.code_4xx;
                break;
            default:
                color = R.color.transparent;
        }
        return color;
    }

    @Override
    public void failure(RetrofitError error) {
        Timber.e(error, "");
        mProgressbar.setVisibility(GONE);
        if (null != error.getResponse()) {
            displayResponse(error.getResponse());
        }
    }

}