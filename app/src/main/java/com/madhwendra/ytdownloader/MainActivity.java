package com.madhwendra.ytdownloader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.huber.youtubeExtractor.YouTubeUriExtractor;
import at.huber.youtubeExtractor.YtFile;

public class MainActivity extends AppCompatActivity {

    Button download;
    EditText url;
    String videoUrl;
    String VideoId;
    String title;
    String vId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        url = findViewById(R.id.url);
        download = findViewById(R.id.downloadBtn);
        CheckPermission();

        YouTubePlayerView youTubePlayerView = findViewById(R.id.youtube_player_view);
        getLifecycle().addObserver(youTubePlayerView);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(videoUrl));
                request.setTitle(title);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title + ".mp4");
                @SuppressLint({"StaticFieldLeak", "ServiceCast"}) DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                request.allowScanningByMediaScanner();
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                downloadManager.enqueue(request);
                Toast.makeText(MainActivity.this, "Downloading Your Video", Toast.LENGTH_SHORT).show();
            }
        });
        url.addTextChangedListener(new

            TextWatcher() {
                @Override
                public void beforeTextChanged (CharSequence s,int start, int count, int after){

                }

                @Override
                public void onTextChanged (CharSequence s,int start, int before, int count){
                    String values = url.getText().toString();
                    extractVideoId(values);
                    YouTubeUriExtractor youTubeUriExtractor = new YouTubeUriExtractor(MainActivity.this) {
                        @SuppressLint("StaticFieldLeak")
                        @Override
                        public void onUrisAvailable(String videoId, String videoTitle, SparseArray<YtFile> ytFiles) {
                            if (ytFiles != null) {
                                int tag = 22;
                                VideoId = videoId;

                                videoUrl = ytFiles.get(tag).getUrl();
                                if (videoTitle.length() > 55) {
                                    title = videoTitle.substring(0, 55) + ".mp4";
                                } else {
                                    title = videoTitle + ".mp4";
                                }
                            }
                        }
                    };
                    youTubeUriExtractor.execute(values);

                    youTubePlayerView.setVisibility(View.VISIBLE);
                    youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                        @Override
                        public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                            youTubePlayer.loadVideo(vId,0);
                        }
                    });
                }

                @Override
                public void afterTextChanged (Editable s){


                }
            });
        }

    private void CheckPermission() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(MainActivity.this, "Permission Required", Toast.LENGTH_SHORT).show();
                        finish();

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();
    }

    private String extractVideoId(String values) {

        Pattern pattern = Pattern.compile(
                "^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(values);
        if (matcher.matches()){
            vId = matcher.group(1);
        }
        return vId;
    }
}