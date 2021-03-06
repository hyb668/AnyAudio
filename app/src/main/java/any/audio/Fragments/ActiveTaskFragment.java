package any.audio.Fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import any.audio.Config.Constants;
import any.audio.Interfaces.DownloadCancelListener;
import any.audio.Models.DownloadTaskModel;
import any.audio.Adapters.LiveDownloadListAdapter;
import any.audio.R;
import any.audio.SharedPreferences.SharedPrefrenceUtils;
import any.audio.helpers.TaskHandler;

/**
 * Created by Ankit on 9/25/2016.
 */

public class ActiveTaskFragment extends Fragment {


    private static final String TAG = "ActiveTaskFragment";
    private ListView liveDownloadListView;
    private LiveDownloadListAdapter adapter;
    private NewDownloadItemArrivalListener newDownloadItemArrivalListener;

    DownloadCancelListener downloadCancelListener = new DownloadCancelListener() {
        @Override
        public void onDownloadCancel(String taskID) {
            cancelItem(taskID);

        }
    };
    private ProgressUpdateBroadcastReceiver receiver;
    private boolean mReceiverRegistered;

    public ActiveTaskFragment() {
    }

    private void cancelItem(String taskID) {

        TaskHandler handler = TaskHandler.getInstance(getActivity());
        // remove specific task
        handler.removeTask(taskID);
        handler.setCancelled(true);
        adapter.setDownloadingList(getTasksList());
        liveDownloadListView.setAdapter(adapter);
    }

    public void setNewDownloadItemArrivalListener(NewDownloadItemArrivalListener newDownloadItemArrivalListener) {
        this.newDownloadItemArrivalListener = newDownloadItemArrivalListener;
    }

    public static double roundOf(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_active_task, container, false);
        liveDownloadListView = (ListView) fragmentView.findViewById(R.id.liveDownloadListView);
        adapter = LiveDownloadListAdapter.getInstance(getActivity());
        adapter.setOnDownloadCancelListener(downloadCancelListener);
        adapter.setDownloadingList(getTasksList());
        liveDownloadListView.setAdapter(adapter);

        return fragmentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        SharedPrefrenceUtils.getInstance(activity).setActiveFragmentAttachedState(true);
        if (!mReceiverRegistered)
            registerForBroadcastListen(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        SharedPrefrenceUtils.getInstance(getActivity()).setActiveFragmentAttachedState(false);
        if (mReceiverRegistered)
            unRegisterBroadcast();
    }

    private ArrayList<DownloadTaskModel> getTasksList() {
        ArrayList<DownloadTaskModel> list = new ArrayList<>();
        //get tasks list from taskhandler
        //get title from sf
        ArrayList<String> taskIDs = TaskHandler.getInstance(getActivity()).getTaskSequence();
        for (String t_id : taskIDs) {
            String title = SharedPrefrenceUtils.getInstance(getActivity()).getTaskTitle(t_id);
            list.add(new DownloadTaskModel(title, 0, t_id, ""));
        }
        return list;
    }

    private int getPosition(String taskID) {
        int pos = -1;
        ArrayList<DownloadTaskModel> list = getTasksList();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).taskID.equals(taskID)) {
                pos = i;
                return pos;
            }
        }
        return pos;
    }

    private void updateItem(int position, int progress, String contentSize) {

        if (position != -1) {
            ArrayList<DownloadTaskModel> old_list = getTasksList();

            for (int i = 0; i < old_list.size(); i++) {
                if (i == position) {
                    old_list.set(i, new DownloadTaskModel(old_list.get(i).Title, progress, old_list.get(i).taskID, String.valueOf(inMB(contentSize)) + " Mb"));
                }
            }

            adapter.setDownloadingList(old_list);
            liveDownloadListView.setAdapter(adapter);

            int start = liveDownloadListView.getFirstVisiblePosition();
            int end = liveDownloadListView.getLastVisiblePosition();

            if (start <= position && end >= position) {

                View view = liveDownloadListView.getChildAt(position);
                liveDownloadListView.getAdapter().getView(position, view, liveDownloadListView);

            }
        } else {
            // refressing the tasks list
            adapter.setDownloadingList(getTasksList());
            liveDownloadListView.setAdapter(adapter);

            if (newDownloadItemArrivalListener != null)
                newDownloadItemArrivalListener.onNewItemAdded();
        }
    }

    private double inMB(String bytes) {
        if (bytes != null) {
            double inBytes = Double.parseDouble(bytes);
            double inMB = ((inBytes / 1024) / 1024);
            inMB = roundOf(inMB, 2);

            return inMB;

        } else return 0;
    }

    private void registerForBroadcastListen(Activity activity) {
        receiver = new ProgressUpdateBroadcastReceiver();
        activity.registerReceiver(receiver, new IntentFilter(Constants.ACTION_DOWNLOAD_PROGRESS_UPDATE_BROADCAST));
        mReceiverRegistered = true;

    }

    private void unRegisterBroadcast() {
        getActivity().unregisterReceiver(receiver);
        mReceiverRegistered = false;
    }

    public void makeToast(String msg) {

        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();

    }

    public void log(String msg) {
        Log.d(TAG, msg);
    }

    public class ProgressUpdateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Constants.ACTION_DOWNLOAD_PROGRESS_UPDATE_BROADCAST)) {


                final String taskID = intent.getStringExtra(Constants.EXTRA_TASK_ID);
                final String progress = intent.getStringExtra(Constants.EXTRA_PROGRESS);
                final String contentSize = intent.getStringExtra(Constants.EXTRA_CONTENT_SIZE);
                final boolean isDownloadingHealthy = intent.getBooleanExtra(Constants.EXTRAA_FLAG_DOWNLOAD_STATE, false);

                if (!isDownloadingHealthy) {
                    Log.d("DowloadingTest", " Something goes wrong. removing item");
                    cancelItem(taskID);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateItem(getPosition(taskID), Integer.valueOf(progress), contentSize);
                    }
                }, 700);

            }
        }
    }

    public interface NewDownloadItemArrivalListener {
        void onNewItemAdded();
    }

}
