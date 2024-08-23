package com.vomaksh.hnpocket;

import android.app.Activity;

import com.vomaksh.hnpocket.task.ITaskFinishedHandler;

public abstract class BasePostActivity extends Activity implements ITaskFinishedHandler<Boolean> {

    @Override
    public void onTaskFinished(int taskCode, com.vomaksh.hnpocket.task.ITaskFinishedHandler.TaskResultCode code,
        Boolean result, Object tag) {
        // TODO Auto-generated method stub
        
    }

}
