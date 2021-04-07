package com.easyfitness.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.easyfitness.R;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;


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
    private List<String> songList;
    private String currentFile = "";
    private String currentPath = "";
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
            barSongTitle.setText(currentFile);
            musicPlay.setImageResource(R.drawable.ic_pause);
            updateProgressBar();
        }
    };
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
                    fileChooserDialog.chooseDirectory(currentPath);
                    break;
                case R.id.playerLoop:
                    if (isReplayOn) {
                        isReplayOn = false;
                        musicReplay.setImageResource(R.drawable.ic_replay_blue);
                    } else {
                        isReplayOn = true;
                        musicReplay.setImageResource(R.drawable.ic_replay_black);
                    }

                    break;
                case R.id.playerRandom:
                    if (isRandomOn) {
                        isRandomOn = false;
                        musicRandom.setImageResource(R.drawable.ic_random_blue);
                    } else {
                        isRandomOn = true;
                        musicRandom.setImageResource(R.drawable.ic_random_black);
                    }

                    break;
            }
        }
    };

    public MusicController(AppCompatActivity activity) {
        mActivity = activity;

        // Create DirectoryChooserDialog and register a callback
        fileChooserDialog =
                new FileChooserDialog(this.mActivity, file -> {
                    currentFile = file;
                    currentPath = getParentDirPath(currentFile);
                    buildSongList(currentPath);
                    currentIndexSongList = songList.indexOf(getFileName(file));
                    newSongSelected = true;
                    Play();
                    savePreferences();
/*
                mediaPlayer.reset();
                mediaPlayer.setDataSource(file);
                mediaPlayer.prepare();
                mediaPlayer.start();
                musicPlay.setImageResource(R.drawable.pause);
                isStopped = false;
*/
                });

        fileChooserDialog.setNewFolderEnabled(false);
        fileChooserDialog.setDisplayFolderOnly(false);
        fileChooserDialog.setFileFilter("mp3;3gp;mp4;aac;ts;flac;mid;ogg;mkv;wav");

    }

    public static String getParentDirPath(String fileOrDirPath) {
        boolean endsWithSlash = fileOrDirPath.endsWith(File.separator);
        return fileOrDirPath.substring(0, fileOrDirPath.lastIndexOf(File.separatorChar,
                fileOrDirPath.length() - (endsWithSlash ? 2 : 1)));
    }

    public static String getFileName(String fileOrDirPath) {
        return fileOrDirPath.substring(fileOrDirPath.lastIndexOf(File.separatorChar) + 1);
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
        if (currentIndexSongList < 0)
            if (currentPath.isEmpty())
                fileChooserDialog.chooseDirectory(currentPath);
            else {
                currentIndexSongList = 0;
                buildSongList(currentPath);
                currentFile = songList.get(0);
                newSongSelected = true;
                Play();
            }
        else {
            try {
                if (newSongSelected) {
                    newSongSelected = false;
                    currentFile = songList.get(currentIndexSongList);
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(currentPath + File.separator + currentFile);
                    mediaPlayer.prepareAsync();
                    isStopped = false;
                    isPaused = false;
                } else if (isPaused) { // differe de STOP
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

    private void buildSongList(String path) {
        songList = fileChooserDialog.getFiles(currentPath);
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
        currentPath = settings.getString("currentPath", "");
    }

    private void savePreferences() {
        // Restore preferences
        SharedPreferences settings = mActivity.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("currentPath", currentPath);
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
