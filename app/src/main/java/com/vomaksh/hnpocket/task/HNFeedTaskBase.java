package com.vomaksh.hnpocket.task;

import android.util.Log;

import com.vomaksh.hnpocket.App;
import com.vomaksh.hnpocket.model.HNFeed;
import com.vomaksh.hnpocket.parser.HNFeedParser;
import com.vomaksh.hnpocket.reuse.CancelableRunnable;
import com.vomaksh.hnpocket.server.HNCredentials;
import com.vomaksh.hnpocket.server.IAPICommand;
import com.vomaksh.hnpocket.server.IAPICommand.RequestType;
import com.vomaksh.hnpocket.server.StringDownloadCommand;
import com.vomaksh.hnpocket.util.FileUtil;
import com.vomaksh.hnpocket.util.Run;

import java.util.HashMap;

public abstract class HNFeedTaskBase extends BaseTask<HNFeed> {

    public HNFeedTaskBase(String notificationBroadcastIntentID, int taskCode) {
        super(notificationBroadcastIntentID, taskCode);
    }

    @Override
    public CancelableRunnable getTask() {
        return new HNFeedTaskRunnable();
    }

    protected abstract String getFeedURL();

    class HNFeedTaskRunnable extends CancelableRunnable {

        StringDownloadCommand mFeedDownload;

        @Override
        public void run() {
            mFeedDownload = new StringDownloadCommand(getFeedURL(), new HashMap<String, String>(), RequestType.GET, false, null,
                App.getInstance(), HNCredentials.getCookieStore(App.getInstance()));

            mFeedDownload.run();

            if (mCancelled)
                mErrorCode = IAPICommand.ERROR_CANCELLED_BY_USER;
            else
                mErrorCode = mFeedDownload.getErrorCode();

            if (!mCancelled && mErrorCode == IAPICommand.ERROR_NONE) {
                HNFeedParser feedParser = new HNFeedParser();
                try {
                    mResult = feedParser.parse(mFeedDownload.getResponseContent());
                    Run.inBackground(new Runnable() {
                        public void run() {
                            FileUtil.setLastHNFeed(mResult);
                        }
                    });
                } catch (Exception e) {
                    mResult = null;
                    Log.e("HNFeedTask", "HNFeed Parser Error :(", e);
                }
            }

            if (mResult == null)
                mResult = new HNFeed();
        }

        @Override
        public void onCancelled() {
            mFeedDownload.cancel();
        }

    }

}
