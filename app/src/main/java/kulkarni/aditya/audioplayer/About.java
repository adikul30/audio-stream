package kulkarni.aditya.audioplayer;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class About extends AppCompatActivity {

    ImageView newsLink,iplLink,firebaseAppLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle("About Me");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        newsLink = (ImageView) findViewById(R.id.news_app_link);
        iplLink = (ImageView) findViewById(R.id.ipl_app_link);
        firebaseAppLink = (ImageView) findViewById(R.id.firebase_app_link);

        newsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleIntent("https://github.com/adikul30/MaterialNews");
            }
        });

        iplLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleIntent("https://github.com/adikul30/IPL");
            }
        });

        firebaseAppLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleIntent("https://github.com/adikul30/IoT-FCM");
            }
        });

    }

    private void handleIntent(String uri){
        Intent githubRepo = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(githubRepo);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
