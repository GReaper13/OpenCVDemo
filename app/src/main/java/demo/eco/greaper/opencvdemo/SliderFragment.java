package demo.eco.greaper.opencvdemo;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import demo.eco.greaper.opencvdemo.base.BaseEditFragment;
import demo.eco.greaper.opencvdemo.view.imagezoom.ImageViewTouchBase;

public class SliderFragment extends BaseEditFragment implements SeekBar.OnSeekBarChangeListener {

    View sliderFragment;
    @BindView(R.id.seekbar_cancel)
    ImageButton imgCancel;
    @BindView(R.id.slider)
    SeekBar slider;
    @BindView(R.id.seekbar_apply)
    ImageButton imgApply;
    Unbinder unbinder;

    public Bitmap filterBit;

    Bitmap currentBitmap;

    @Override
    public void onShow() {
        if (activity!=null) {
            setDefaultSeekBarProgress();
            activity.changeMode(EditActivity.MODE_SLIDER);
            currentBitmap = activity.mainBitmap;
            activity.mainImage.setImageBitmap(activity.mainBitmap);
            // TODO zoom view
            activity.mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
            activity.mainImage.setScaleEnabled(false);
            defaultApply();
        }
    }

    private void defaultApply() {
        ProcessImageTask processImageTask = new ProcessImageTask();
        processImageTask.execute(slider.getProgress());
    }

    public static SliderFragment newInstance() {
        SliderFragment fragment = new SliderFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sliderFragment = inflater.inflate(R.layout.slider_fragment, container, false);
        unbinder = ButterKnife.bind(this, sliderFragment);
        return sliderFragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        imgCancel.setImageResource(R.drawable.ic_close_black_24dp);
        imgApply.setImageResource(R.drawable.ic_done_black_24dp);

        slider.setMax(100);
        setDefaultSeekBarProgress();
        slider.setOnSeekBarChangeListener(this);

        onShow();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.seekbar_cancel, R.id.seekbar_apply})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.seekbar_cancel:
                backToMain();
                break;
            case R.id.seekbar_apply:
                if (filterBit!=null) {
                    activity.changeMainBitmap(filterBit);
                    filterBit = null;
                }
                if (EditActivity.effectType / 100 ==  EditActivity.MODE_FILTERS){
                    activity.filterFragment.onShow();
                }
                backToMain();
                break;
        }
    }

    // This method used to set default for each mode
    private void setDefaultSeekBarProgress() {
        if (null != slider) {
            switch (EditActivity.effectType/100) {
                case EditActivity.MODE_FILTERS:
                    slider.setProgress(100);
                    break;
                case EditActivity.MODE_ENHANCE:
                    switch (EditActivity.effectType % 300) {
                        case 2:
                        case 6:
                        case 7:
                        case 8:
                            slider.setProgress(0);
                            break;
                        case 0:
                        case 1:
                        case 3:
                        case 4:
                        case 5:
                            slider.setProgress(50);
                            break;
                    }
                    break;
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        ProcessImageTask processImageTask = new ProcessImageTask();
        processImageTask.execute(seekBar.getProgress());

    }

    // This async task is used to create async task when seek bar is being changed
    private final class ProcessImageTask extends AsyncTask<Integer, Void, Bitmap> {
        private Bitmap srcBitmap;
        int val;

        @Override
        protected Bitmap doInBackground(Integer... params) {
            val = params[0];
            if (srcBitmap != null && !srcBitmap.isRecycled()) {
                srcBitmap.recycle();
            }

            srcBitmap = Bitmap.createBitmap(currentBitmap.copy(
                    Bitmap.Config.RGB_565, true));
            Log.d("my_app", EditActivity.effectType + " " + val);
            return AppUtils.processImage(srcBitmap, EditActivity.effectType, val);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            activity.hideProgressBar();
            if (result == null)
                return;
            filterBit = result;
            activity.mainImage.setImageBitmap(filterBit);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activity.showProgressBar();
        }

    }

    public void resetBitmaps(){
        if (null != filterBit) {
            filterBit.recycle();
        }
        filterBit = null;
        if (null != currentBitmap) {
            currentBitmap.recycle();
        }
        currentBitmap = null;
    }

    public void backToMain(){
        if (null != activity) {
            currentBitmap = null;
            activity.mainImage.setImageBitmap(activity.mainBitmap);
            activity.changeMode(EditActivity.effectType / 100);
            activity.changeBottomFragment(EditActivity.MODE_MAIN);
            // TODO zoom view
            activity.mainImage.setScaleEnabled(true);

            switch (activity.mode)
            {
                case EditActivity.MODE_FILTERS:
                    activity.filterFragment.clearCurrentSelection();
                    break;

//                case EditActivity.MODE_ENHANCE:
//                    activity.enhanceFragment.clearCurrentSelection();
//                    break;

                default:
                    break;
            }
        }
    }
}
