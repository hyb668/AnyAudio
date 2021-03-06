package any.audio.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import any.audio.Fragments.DownloadFragment;
import any.audio.Fragments.ActiveTaskFragment;

/**
 * Created by Ankit on 9/25/2016.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {


        if (position == 0) {
            Fragment activeFragment = new ActiveTaskFragment();
            final Fragment downloadFragment = new DownloadFragment();

            ((ActiveTaskFragment) activeFragment).setNewDownloadItemArrivalListener(new ActiveTaskFragment.NewDownloadItemArrivalListener() {
                @Override
                public void onNewItemAdded() {
                    ((DownloadFragment) downloadFragment).refreshDownloadedList();
                }
            });

            return activeFragment;

        } else return new DownloadFragment();
    }

    @Override
    public int getCount() {
        return 2;
    }
}
