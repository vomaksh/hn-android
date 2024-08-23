package com.vomaksh.hnpocket.login;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vomaksh.hnpocket.App;
import com.vomaksh.hnpocket.R;
import com.vomaksh.hnpocket.server.HNCredentials;
import com.vomaksh.hnpocket.task.HNLoginTask;
import com.vomaksh.hnpocket.task.ITaskFinishedHandler;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.login_dialog)
public class LoginActivity extends Activity implements ITaskFinishedHandler<Boolean>{

    private static final int TASKCODE_LOGIN = 10;
    @ViewById(R.id.user_settings_dialog_save_button)
    Button mSaveButton;

    @ViewById(R.id.user_settings_dialog_cancel_button)
    Button mCancelButton;

    @ViewById(R.id.user_settings_dialog_username)
    EditText mUsernameText;

    @ViewById(R.id.user_settings_dialog_password)
    EditText mPasswordText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_dialog);
    }

    @Click(R.id.user_settings_dialog_cancel_button)
    void exit() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Click(R.id.user_settings_dialog_save_button)
    void saveCredentials() {
        mSaveButton.setText(R.string.checking);
        HNLoginTask.start(mUsernameText.getText().toString(), mPasswordText.getText().toString(),
                this, this, TASKCODE_LOGIN);
    }

    @Override
    public void onTaskFinished(int taskCode,
            com.vomaksh.hnpocket.task.ITaskFinishedHandler.TaskResultCode code,
            Boolean result, Object tag) {
        // If the result is true that means we successfully logged in
        if (result != null && result) {
            setResult(RESULT_OK);
            // Invalidate the credentials so that future requests reset them
            HNCredentials.invalidate();
            finish();
        } else {
            // Otherwise we probably had an issue, so report that to the user
            int messageId;
            if (result != null && !result)
                messageId = R.string.error_login_failed;
            else
                messageId = code.equals(TaskResultCode.NoNetworkConnection) ? R.string.error_login_device_offline
                    : R.string.error_unknown_error;
            Toast.makeText(App.getInstance(), App.getInstance().getString(messageId), Toast.LENGTH_LONG).show();
            mSaveButton.setText(getString(R.string.check_and_save));
        }
    }
}
