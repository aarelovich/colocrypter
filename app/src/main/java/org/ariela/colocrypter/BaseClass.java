package org.ariela.colocrypter;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by ariela on 2/18/18.
 */

public class BaseClass extends AppCompatActivity {

    private boolean timeoutFlag = false;
    private boolean isActive = true;

    private int logOutTime;

    private CountDownTimer logOutTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int t = 300000;
        if (Aux.appData !=  null){
            System.err.println("Attempting to invokoe get configuration integer");
            System.err.println("Time is " + Aux.CONFIG_LOGOUT_TIME);
            t = Aux.appData.getConfigurationInteger(Aux.CONFIG_LOGOUT_TIME);
            if (t == -1) t = Aux.DEFAULT_TIME_OUT;
        }
        setLogOutTime(t);
    }

    public void setLogOutTime(int t){
        logOutTime = t;
        startTimer();
    }

    public int getLogOutTime() {
        return logOutTime;
    }

    public void startTimer(){
        if (logOutTimer != null) logOutTimer.cancel();
        logOutTimer = new CountDownTimer(logOutTime, logOutTime) {
            @Override
            public void onTick(long l) {
                // Nothing to do.
            }

            @Override
            public void onFinish() {
                timeoutFlag = true;
                if (isActive) logOut();
            }
        }.start();
    }


    @Override
    public void onStart(){
        super.onStart();
        if ((!isActive) && (timeoutFlag)){
            logOut();
        }
        else{
            isActive = true;
            startTimer();
        }
    }

    protected void logOut(){
        Aux.appData.clear();
        Intent intent = new Intent(this,LoginView.class);
        startActivity(intent);
    }

    @Override
    public void onStop(){
        super.onStop();
        isActive = false;
    }



}
