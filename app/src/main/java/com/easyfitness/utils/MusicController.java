package com.easyfitness.utils;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.easyfitness.MainActivity;
import com.easyfitness.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static com.easyfitness.MainActivity.OPEN_MUSIC_FILE;


public class MusicController {

    static final int MUSICCONTROLLER = 1563540;
    static final int MUSICCONTROLLER_PLAY_CLICK = MUSICCONTROLLER;
    public static String PREFS_NAME = "music_prefsfile";
    private final UnitConverter utils = new UnitConverter();
    // Handler to update UI timer, progress bar etc,.
    private final Handler mHandler = new Handler();
    AppCompatActivity mActivity = null;
    NoisyAudioStreamReceiver myNoisyAudioStreamReceiver = null;
    // Music Controller
    private ImageButton musicPlay = null;
    private ImageButton musicReplay = null;
    private ImageButton musicRandom = null;
    private TextView barSongTitle = null;
    private TextView barSongTime = null;
    private SeekBar seekProgressBar = null;
    private boolean isStopped = true;
    private boolean isPaused = false;
    private boolean newSongSelected = false;
    private boolean isReplayOn = false;
    private boolean isRandomOn = false;
    private final Random randomIntGenerator = new Random();
    private FileChooserDialog fileChooserDialog = null;
    private Uri currentFile = null;
    private Uri currentPath = null;
    private int currentIndexSongList = -1;
    private MediaPlayer mediaPlayer;
    /*
     * Moves the cursor of the progress bar to accelerate a song.
     */
    private final SeekBar.OnSeekBarChangeListener seekBarTouch = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!isStopped && fromUser) {
                mediaPlayer.seekTo((int) (mediaPlayer.getDuration() * (progress / 100.0)));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    /**
     * Background Runnable thread
     */
    private final Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    long totalDuration = mediaPlayer.getDuration();
                    long currentDuration = mediaPlayer.getCurrentPosition();

                    // Displaying Total Duration time
                    barSongTime.setText(utils.milliSecondsToTimer(currentDuration) + "/" + utils.milliSecondsToTimer(totalDuration));

                    // Updating progress bar
                    int progress = utils.getProgressPercentage(currentDuration, totalDuration);
                    //Log.d("Progress", "" + progress);
                    seekProgressBar.setProgress(progress);

                    // Running this thread after 200 milliseconds
                    mHandler.postDelayed(this, 201);
                }
            }
        }
    };
    private IntentFilter intentFilter = null;
    //private OnFocusChangeListener touchRazEdit = new View.OnFocusChangeListener() {
    private final OnCompletionListener songCompletion = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (isRandomOn) {
                NextRandom();
            } else {
                if (currentIndexSongList + 1 < songList.size()) {
                    Next();
                } else {
                    if (isReplayOn) {
                        newSongSelected = true;
                        currentIndexSongList = 0;
                        Play();
                    } else {
                        /* release mediaplayer */
                        Stop();
                    }
                }
            }
        }
    };
    //private OnFocusChangeListener touchRazEdit = new View.OnFocusChangeListener() {
    private final OnPreparedListener mediaplayerReady = new OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mediaPlayer.start();
            mActivity.registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
            mediaPlayer.setOnCompletionListener(songCompletion);
            barSongTitle.setText(getFileName(currentFile));
            musicPlay.setImageResource(R.drawable.ic_pause);
            updateProgressBar();
        }
    };

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor =mActivity.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private final OnClickListener playerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnPreparedListener(mediaplayerReady);
                loadPreferences();
            }

            switch (v.getId()) {
                case R.id.playerPlay:
                    if (mediaPlayer.isPlaying()) {
                        Pause();
                    } else {
                        Play();
                    }
                    break;
                case R.id.playerStop:
                    Stop();
                    break;
                case R.id.playerNext:
                    if (isRandomOn) {
                        NextRandom();
                    } else {
                        Next();
                    }
                    break;
                case R.id.playerPrevious:
                    if (isRandomOn) {
                        NextRandom();
                    } else {
                        Previous();
                    }
                    break;
                case R.id.playerList:
                    if (isExternalStoragePermissionDenied()) {
                        requestPermissionForReading();
                    } else {
                        chooseDirectory();
                    }
                    break;
                case R.id.playerLoop:
                    if (isReplayOn) {
                        isReplayOn = false;
                        musicReplay.setImageResource(R.drawable.ic_replay_white);
                    } else {
                        isReplayOn = true;
                        musicReplay.setImageResource(R.drawable.ic_replay_green);
                    }

                    break;
                case R.id.playerRandom:
                    if (isRandomOn) {
                        isRandomOn = false;
                        musicRandom.setImageResource(R.drawable.ic_random_white);
                    } else {
                        isRandomOn = true;
                        musicRandom.setImageResource(R.drawable.ic_random_green);
                    }

                    break;
            }
        }
    };

    public MusicController(AppCompatActivity activity) {
        mActivity = activity;
    }

    public static String getParentDirPath(String fileOrDirPath) {
        boolean endsWithSlash = fileOrDirPath.endsWith(File.separator);
        return fileOrDirPath.substring(0, fileOrDirPath.lastIndexOf(File.separatorChar,
                fileOrDirPath.length() - (endsWithSlash ? 2 : 1)));
    }

    public void initView() {
        // Music controller
        musicPlay = mActivity.findViewById(R.id.playerPlay);
        ImageButton musicStop = mActivity.findViewById(R.id.playerStop);
        ImageButton musicNext = mActivity.findViewById(R.id.playerNext);
        ImageButton musicPrevious = mActivity.findViewById(R.id.playerPrevious);
        ImageButton musicList = mActivity.findViewById(R.id.playerList);
        musicReplay = mActivity.findViewById(R.id.playerLoop);
        musicRandom = mActivity.findViewById(R.id.playerRandom);
        //playerTopLayout = (LinearLayout) mActivity.findViewById(R.id.playerTopLayout);

        barSongTitle = mActivity.findViewById(R.id.playerSongTitle);
        barSongTitle.setSingleLine(true);
        barSongTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        barSongTitle.setHorizontallyScrolling(true);
        barSongTitle.setSelected(true);

        seekProgressBar = mActivity.findViewById(R.id.playerSeekBar);
        seekProgressBar.setMax(100);
        seekProgressBar.setProgress(0);

        barSongTime = mActivity.findViewById(R.id.playerSongProgress);

        musicPlay.setOnClickListener(playerClick);
        musicStop.setOnClickListener(playerClick);
        musicNext.setOnClickListener(playerClick);
        musicPrevious.setOnClickListener(playerClick);
        musicList.setOnClickListener(playerClick);
        musicReplay.setOnClickListener(playerClick);
        musicRandom.setOnClickListener(playerClick);
        //playerTopLayout.setOnTouchListener(progressBarTouch);
        seekProgressBar.setOnSeekBarChangeListener(seekBarTouch);

        myNoisyAudioStreamReceiver = new NoisyAudioStreamReceiver();
        intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    }

    public void Play() {
        // Play song
        if (currentIndexSongList < 0) {
            if (isExternalStoragePermissionDenied()) {
                requestPermissionForReading();
            } else {
                if (currentPath == null)
                    chooseDirectory();
                else {
                    currentIndexSongList = 0;
                    buildSongList(currentPath);
                    if (songList.size()!=0) {
                        currentFile = songList.get(currentIndexSongList);
                        newSongSelected = true;
                        Play();
                    } else {
                        currentPath = null;
                        currentFile = null;
                        currentIndexSongList = -1;
                    }
                }
            }
        } else {
            try {
                if (newSongSelected) {
                    newSongSelected = false;
                    if (songList.size()!=0) {
                        currentFile = songList.get(currentIndexSongList);
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(mActivity.getApplicationContext(), currentFile);
                        mediaPlayer.prepareAsync();
                        isStopped = false;
                        isPaused = false;
                    } else {
                        currentPath = null;
                        currentFile = null;
                        currentIndexSongList = -1;
                    }
                } else if (isPaused) { // different from STOP
                    mediaPlayer.start();
                    mActivity.registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
                    musicPlay.setImageResource(R.drawable.ic_pause);
                    updateProgressBar();
                    isStopped = false;
                    isPaused = false;
                }
            } catch (IllegalArgumentException | SecurityException
                    | IllegalStateException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void Pause() {
        mediaPlayer.pause();
        try {
            mActivity.unregisterReceiver(myNoisyAudioStreamReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        // Changing Button Image to pause image
        isPaused = true;
        musicPlay.setImageResource(R.drawable.ic_play_arrow);
    }

    public void Stop() {
        mediaPlayer.stop();
        try {
            mActivity.unregisterReceiver(myNoisyAudioStreamReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        isStopped = true;
        isPaused = false;
        barSongTitle.setText("");
        seekProgressBar.setProgress(0);
        barSongTime.setText("");
        currentIndexSongList = -1;
        // Changing Button Image to play image
        musicPlay.setImageResource(R.drawable.ic_play_arrow);
    }

    public void Next() {
        /* load the new source */
        if (currentIndexSongList >= 0) {
            if (currentIndexSongList + 1 < songList.size()) {
                currentIndexSongList = currentIndexSongList + 1;
                newSongSelected = true;
                Play();
            } else if (isReplayOn) {
                currentIndexSongList = 0;
                newSongSelected = true;
                Play();
            }
        }
    }

    public void NextRandom() {
        /* load the new source randomly */
        if (currentIndexSongList >= 0) {
            int randomSongListIndex = randomIntGenerator.nextInt(songList.size());
            while (currentIndexSongList == randomSongListIndex) {
                randomSongListIndex = randomIntGenerator.nextInt(songList.size());
            }
            currentIndexSongList = randomSongListIndex;
            newSongSelected = true;
            Play();
        }
    }

    public void Previous() {
        /* load the new source */
        if (currentIndexSongList > 0) {
            currentIndexSongList = currentIndexSongList - 1;
            newSongSelected = true;
            Play();
        } else if (isReplayOn) {
            currentIndexSongList = songList.size() - 1;
            newSongSelected = true;
            Play();
        }
    }

    public void chooseDirectory() {
        //fileChooserDialog.chooseDirectory(currentPath);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        //intent.setType("audio/*");
        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);

        mActivity.startActivityForResult(intent, OPEN_MUSIC_FILE);
    }

    public void OpenMusicFileIntentResult(Uri folder) {
        //
        currentPath = folder; //getParentDirPath(currentFile);
        buildSongList(currentPath);
        if (songList.size()!=0) {
            currentFile = songList.get(0);
            currentIndexSongList = 0; //.indexOf(getFileName(file));
            newSongSelected = true;
            Play();
            savePreferences();
        } else {
            currentIndexSongList = -1;
            currentPath = null;
            currentFile = null;
            newSongSelected = false;
        }
    }
    private boolean isExternalStoragePermissionDenied() {
        return ContextCompat.checkSelfPermission(mActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionForReading() {
        int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 103;
        ActivityCompat.requestPermissions(mActivity,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
    }

    class Audio {
        private final Uri uri;
        private final String name;
        private final int duration;

        public Audio(Uri uri, String name, int duration) {
            this.uri = uri;
            this.name = name;
            this.duration = duration;
        }
    }

    List<Uri> songList = new ArrayList<Uri>();

    private void buildSongList(Uri path) {
        DocumentFile documentsTree = DocumentFile.fromTreeUri(mActivity.getApplication(), path);
        DocumentFile[] childDocuments = documentsTree.listFiles();
        songList.clear();
        for (DocumentFile document:childDocuments) {
            if (document.isFile()) {
                if (document.getType().contains("audio")) {
                    songList.add((document.getUri()));
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            songList.sort((m1, m2) -> m1.getPath().compareTo(m2.getPath()));
        }
    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 200);
    }

    private void loadPreferences() {
        // Restore preferences
        SharedPreferences settings = mActivity.getSharedPreferences(PREFS_NAME, 0);
        currentPath = Uri.parse(settings.getString("currentPath", ""));
    }

    private void savePreferences() {
        // Restore preferences
        SharedPreferences settings = mActivity.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("currentPath", currentPath.getPath());
        boolean x = editor.commit();
    }

    // Helper method to delete currentPath for testing purpose
    private void deleteSpecificPreferences(String preferenceName) {
        SharedPreferences settings = mActivity.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(preferenceName);
        boolean x = editor.commit();
    }

    public void releaseMediaPlayer() {
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private void showMP3Player(boolean showit) {
        if (showit) {
            //this.ba.showMP3Player();
        } else {

        }
    }


    private class NoisyAudioStreamReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                Pause();
                Log.d("Message", "HeadPhone Unplugged");
            }
        }

    }

}
