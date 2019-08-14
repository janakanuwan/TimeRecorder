package com.nus.hci.timerrecord;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private static final String OBSTACLE = "OBSTACLE";
    private static final String STANDING_START = "STANDING_START";
    private static final String STANDING_STOP = "STANDING_STOP";
    private static final String STAIR_START = "STAIR_START";
    private static final String STAIR_STOP = "STAIR_STOP";
    private static final String TIMER_START = "TIMER_START";
    private static final String TIMER_STOP = "TIMER_STOP";

    private static final int PERMISSION_REQUEST_CODE = 10;
    private static final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final int COLOR_OFF = Color.GRAY;
    private static final int COLOR_ON = Color.RED;
    private static final int COLOR_ON_SPECIAL = Color.BLUE;
    private static final int VIBRATION_MILLIS = 300;

    private static long millis = System.currentTimeMillis();
    private static boolean timerOn = false;
    private static boolean stairOn = false;
    private static boolean standOn = false;
    private static String data = "";

    private TextView txtTime;
    private Button btnObstacle;
    private ToggleButton btnToggleStair;
    private ToggleButton btnToggleStand;
    private ToggleButton btnToggleTimer;
    private Button btnExport;

    private Vibrator vibrator;

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initializeUI();

        if (!hasAllPermissions()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
            Toast.makeText(this, "Please grant the permission to start the application", Toast.LENGTH_LONG).show();
        } else {
            enableUI();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        vibrator.cancel();
        vibrator = null;
        stopTimer();
    }

    private boolean hasAllPermissions() {
        for (String permission : MainActivity.PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, permission + " is NOT granted");
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            Log.d(TAG, "Granted permissions:" + Arrays.toString(permissions) + ", " + Arrays.toString(grantResults));
            if (hasAllPermissions()) {
                enableUI();
            }
        }
    }

    private void initializeUI() {
        txtTime = findViewById(R.id.txtTime);
        btnObstacle = findViewById(R.id.btnObstacle);
        btnToggleStair = findViewById(R.id.btnToggleStair);
        btnToggleStand = findViewById(R.id.btnToggleStand);
        btnToggleTimer = findViewById(R.id.btnToggleTimer);
        btnExport = findViewById(R.id.btnExport);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        disableUI();
        configureButtons();

        txtTime.setText(getResources().getString(R.string.start_timing));
    }

    private void disableUI() {
        btnObstacle.setEnabled(false);
        btnToggleStair.setEnabled(false);
        btnToggleStand.setEnabled(false);
        btnToggleTimer.setEnabled(false);
        btnExport.setEnabled(false);
    }

    private void enableUI() {
        btnObstacle.setEnabled(true);
        btnToggleStair.setEnabled(true);
        btnToggleStand.setEnabled(true);
        btnToggleTimer.setEnabled(true);
        btnExport.setEnabled(true);
    }

    private static void setColorOff(Button button) {
        button.setBackgroundColor(COLOR_OFF);
    }

    private static void setColorOn(Button button) {
        button.setBackgroundColor(COLOR_ON);
    }

    private static void setColorOnSpecial(Button button) {
        button.setBackgroundColor(COLOR_ON_SPECIAL);
    }

    private void giveFeedback() {
        Toast toast = Toast.makeText(this, "Time: " + getElapsedTimeString(), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MILLIS, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(VIBRATION_MILLIS);
        }
    }

    private void configureButtons() {
        if (stairOn) {
            btnToggleStair.setChecked(true);
            setColorOn(btnToggleStair);
        } else {
            setColorOff(btnToggleStair);
        }
        btnToggleStair.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                stairOn = isChecked;
                if (isChecked) {
                    data += (STAIR_START + getElapsedSecondsWithNewLine());
                    setColorOnSpecial(btnToggleStair);
                    giveFeedback();
                } else {
                    data += (STAIR_STOP + getElapsedSecondsWithNewLine());
                    setColorOff(btnToggleStair);
                    giveFeedback();
                }
            }
        });

        if (standOn) {
            btnToggleStand.setChecked(true);
            setColorOn(btnToggleStand);
        } else {
            setColorOff(btnToggleStand);
        }
        btnToggleStand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                standOn = isChecked;
                if (isChecked) {
                    data += (STANDING_START + getElapsedSecondsWithNewLine());
                    setColorOn(btnToggleStand);
                    giveFeedback();
                } else {
                    data += (STANDING_STOP + getElapsedSecondsWithNewLine());
                    setColorOff(btnToggleStand);
                    giveFeedback();
                }
            }
        });

        if (timerOn) {
            btnToggleTimer.setChecked(true);
            setColorOn(btnToggleTimer);
            startTimer(false);
        } else {
            setColorOff(btnToggleTimer);
        }
        btnToggleTimer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                timerOn = isChecked;
                if (isChecked) {
                    startTimer(true);
                    setColorOn(btnToggleTimer);
                    giveFeedback();
                } else {
                    stopTimer();
                    setColorOff(btnToggleTimer);
                    giveFeedback();
                }
            }
        });

        btnObstacle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data += (OBSTACLE + getElapsedSecondsWithNewLine());
                giveFeedback();
            }
        });
        btnObstacle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    setColorOff(btnObstacle);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    setColorOnSpecial(btnObstacle);
                }
                return false;
            }

        });


        final Context context = this;
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Data: " + data);
                showFileNameDialog(context);
            }
        });
    }

    private void showFileNameDialog(Context context) {
        final EditText input = new EditText(context);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("File Name")
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String fileName = input.getText().toString() + ".csv";
                        Log.d(TAG, "File Name: " + fileName);
                        try {
                            FileUtil.writeFile(data.getBytes(), fileName);
                        } catch (IOException e) {
                            Log.e(TAG, "Error in writing file:" + fileName, e);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.show();
    }

    private static String getElapsedSecondsWithNewLine() {
        long timeElapsed = System.currentTimeMillis() - millis + 500;
        timeElapsed = timeElapsed / 1000;
        return "," + timeElapsed + "\r\n";
    }

    private void startTimer(boolean updateData) {
        stopTimer();
        timer = new Timer();
        millis = System.currentTimeMillis();
        if (updateData) {
            data += (TIMER_START + getElapsedSecondsWithNewLine());
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTime();
            }
        }, 0, 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            data += (TIMER_STOP + getElapsedSecondsWithNewLine());
            timer.cancel();
        }
        timer = null;
    }


    private void updateTime() {
        final String timeString = getElapsedTimeString();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtTime.setText(timeString);
            }
        });
    }

    private String getElapsedTimeString() {
        long timeElapsed = System.currentTimeMillis() - millis;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(timeElapsed) % 24,
                TimeUnit.MILLISECONDS.toMinutes(timeElapsed) % 60,
                TimeUnit.MILLISECONDS.toSeconds(timeElapsed) % 60);
    }

}
