package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.indieweb.indigenous.R;

public class MediaActivity extends BasePostActivity {

    String fileUrl = "";
    TextView mediaUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isMediaRequest = true;
        finishActivity = false;
        canAddImage = true;
        postType = "Media";
        setContentView(R.layout.activity_media);
        mediaUrl = findViewById(R.id.mediaUrl);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        sendBasePost(item);
    }

}
