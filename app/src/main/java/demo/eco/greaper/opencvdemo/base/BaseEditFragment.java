package demo.eco.greaper.opencvdemo.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import demo.eco.greaper.opencvdemo.EditActivity;

public abstract  class BaseEditFragment extends Fragment {
    protected EditActivity activity;

    protected EditActivity ensureEditActivity(){
        if(activity==null){
            activity = (EditActivity) getActivity();
        }
        return activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ensureEditActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        MyApplication.getRefWatcher(getActivity()).watch(this);
    }

    // This method used to show image in edit activity
    public abstract void onShow();
}
