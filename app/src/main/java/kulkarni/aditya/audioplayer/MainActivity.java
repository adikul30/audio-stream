package kulkarni.aditya.audioplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Url;

public class MainActivity extends AppCompatActivity implements SongAdapter.songOnClickHandler {

    private static final String TAG = "MainActivity";
    String API_BASE_URL = "http://starlord.hackerearth.com";

    RecyclerView recyclerView;
    SongAdapter songAdapter;
    ArrayList<Song> songs = new ArrayList<>();
    private boolean isPlaying = false;
    WebView webView;
    String decompressedUrl = "";
    private SimpleExoPlayer exoPlayer;
    private ImageButton btnPlay;
    TextView nowPlaying;
    ProgressBar progressBar;
    EditText searchET;
    ImageView songImage;

    private ExoPlayer.EventListener eventListener = new ExoPlayer.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            Log.i(TAG,"onTimelineChanged");
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.i(TAG,"onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.i(TAG,"onLoadingChanged");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            switch (playbackState){
                case ExoPlayer.STATE_ENDED:
                    toggleStatus(false);
                    exoPlayer.seekTo(0);
                    break;
                case ExoPlayer.STATE_READY:
                    progressBar.setVisibility(View.GONE);
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    break;
                case ExoPlayer.STATE_IDLE:
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.i(TAG,"onPlaybackError: "+error.getMessage());
        }

        @Override
        public void onPositionDiscontinuity() {
            Log.i(TAG,"onPositionDiscontinuity");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.songs_recycler_view);
        webView = (WebView)findViewById(R.id.dummy_web_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        nowPlaying = (TextView)findViewById(R.id.now_playing);
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        searchET = (EditText)findViewById(R.id.search_edit_text);
        songImage = (ImageView)findViewById(R.id.song_cover);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        songAdapter = new SongAdapter(this,songs,this);
        recyclerView.setAdapter(songAdapter);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(API_BASE_URL)
                        .addConverterFactory(
                                GsonConverterFactory.create()
                        );

        Retrofit retrofit =
                builder
                        .client(
                                httpClient.build()
                        )
                        .build();

        // Create a very simple REST adapter which points the API endpoint.
        WebAPI client = retrofit.create(WebAPI.class);

        Call<ArrayList<Song>> call = client.getSongList();

        // Execute the call asynchronously. Get a positive or negative callback.
        call.enqueue(new Callback<ArrayList<Song>>() {
            @Override
            public void onResponse(@NonNull Call<ArrayList<Song>> call, @NonNull Response<ArrayList<Song>> response) {
                // The network call was a success and we got a response
                songAdapter.setList(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<ArrayList<Song>> call, @NonNull Throwable t) {
                // the network call was a failure
            }
        });

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchText = editable.toString().toLowerCase(Locale.getDefault());
                Log.d("SearchQuery", searchText);
                songAdapter.filter(searchText);
            }
        });
    }

    private void playFromURL(Uri uri){
        if(exoPlayer!=null)exoPlayer.release();
        TrackSelector trackSelector = new DefaultTrackSelector();

        LoadControl loadControl = new DefaultLoadControl();

        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "audioplayer"), null);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource audioSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
        exoPlayer.addListener(eventListener);

        exoPlayer.prepare(audioSource);

        btnPlay.requestFocus();
        toggleStatus(true);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleStatus(!isPlaying);
            }
        });
        exoPlayer.setPlayWhenReady(true);

    }

    private void toggleStatus(boolean play){
        isPlaying = play;
        Log.d("isPlaying",String.valueOf(isPlaying));
        exoPlayer.setPlayWhenReady(play);
        if(!isPlaying){
            btnPlay.setImageResource(android.R.drawable.ic_media_play);
        }else{
            btnPlay.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    @Override
    public void onClick(int position, String url, String title, final String imageUrl) {
        nowPlaying.setText(title);
        progressBar.setVisibility(View.VISIBLE);
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                decompressedUrl = webView.getUrl();
                Log.d("url",decompressedUrl);
                playFromURL(Uri.parse(decompressedUrl));
                getImage(imageUrl);
            }
        });
    }

    public void getImage(String imageUrl){
        webView.loadUrl(imageUrl);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                String decompressedUrl = webView.getUrl();
                Log.d("imageUrl",decompressedUrl);
                songAdapter.setImage(decompressedUrl,songImage);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_about) {
            startActivity(new Intent(MainActivity.this, About.class));
        }

        return super.onOptionsItemSelected(item);
    }

}
