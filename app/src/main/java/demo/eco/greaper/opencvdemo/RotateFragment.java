package demo.eco.greaper.opencvdemo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.shchurov.horizontalwheelview.HorizontalWheelView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import demo.eco.greaper.opencvdemo.base.BaseEditFragment;
import demo.eco.greaper.opencvdemo.view.RotateImageView;
import demo.eco.greaper.opencvdemo.view.imagezoom.ImageViewTouchBase;

public class RotateFragment extends BaseEditFragment {

    public static final String TAG = RotateFragment.class.getName();
    @BindView(R.id.rotate_cancel)
    ImageButton rotateCancel;
    @BindView(R.id.tvAngle)
    TextView tvAngle;
    @BindView(R.id.horizontalWheelView)
    HorizontalWheelView horizontalWheelView;
    @BindView(R.id.rotate_apply)
    ImageButton rotateApply;

    Unbinder unbinder;
    private View mainView;;
    private RotateImageView mRotatePanel;


    public static RotateFragment newInstance() {
        RotateFragment fragment = new RotateFragment();
        return fragment;
    }

    @Override
    public void onShow() {
        activity.changeMode(EditActivity.MODE_ROTATE);
        activity.mainImage.setImageBitmap(activity.mainBitmap);
        activity.mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        activity.mainImage.setVisibility(View.GONE);

        activity.mRotatePanel.addBit(activity.mainBitmap, activity.mainImage.getBitmapRect());
        activity.mRotatePanel.reset();
        activity.mRotatePanel.setVisibility(View.VISIBLE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_rotate, container, false);
        unbinder = ButterKnife.bind(this, mainView);
        initViews();
        setupListeners();
        updateUi();
        onShow();
        return mainView;
    }

    private void initViews() {
        this.mRotatePanel = ensureEditActivity().mRotatePanel;
    }

    private void setupListeners() {
        horizontalWheelView.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                updateUi();
            }
        });
    }

    private void updateUi() {
        updateText();
        updateImage();
    }

    private void updateText() {
        String text = String.format(Locale.US, "%.0f°", horizontalWheelView.getDegreesAngle());
        tvAngle.setText(text);
    }

    private void updateImage() {
        int angle = (int) horizontalWheelView.getDegreesAngle();
        mRotatePanel.rotateImage(angle);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        unbinder.unbind();
    }

    @OnClick({R.id.rotate_cancel, R.id.rotate_apply})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rotate_cancel:
                backToMain();
                break;
            case R.id.rotate_apply:
                applyRotateImage();
                break;
        }
    }

    public void backToMain() {
        activity.changeMode(EditActivity.MODE_ADJUST);
        activity.changeBottomFragment(EditActivity.MODE_MAIN);
        activity.adjustFragment.clearSelection();
        activity.mainImage.setVisibility(View.VISIBLE);
        this.mRotatePanel.setVisibility(View.GONE);
    }

    public void applyRotateImage() {
        SaveRotateImageTask task = new SaveRotateImageTask();
        task.execute(activity.mainBitmap);
    }

    private final class SaveRotateImageTask extends
            AsyncTask<Bitmap, Void, Bitmap> {
        //private Dialog dialog;

        @Override
        protected void onCancelled() {
            super.onCancelled();
            //dialog.dismiss();
        }

        @Override
        protected void onCancelled(Bitmap result) {
            super.onCancelled(result);
            //dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //dialog = EditBaseActivity.getLoadingDialog(getActivity(), R.string.saving_image,
            //        false);
            //dialog.show();
        }

        @SuppressWarnings("WrongThread")
        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            RectF imageRect = mRotatePanel.getImageNewRect();
            Bitmap originBit = params[0];
            Bitmap result = Bitmap.createBitmap((int) imageRect.width(),
                    (int) imageRect.height(), Bitmap.Config.ARGB_4444);
            Canvas canvas = new Canvas(result);
            int w = originBit.getWidth() >> 1;
            int h = originBit.getHeight() >> 1;
            float centerX = imageRect.width() / 2;
            float centerY = imageRect.height() / 2;

            float left = centerX - w;
            float top = centerY - h;

            RectF dst = new RectF(left, top, left + originBit.getWidth(), top
                    + originBit.getHeight());
            canvas.save();
            canvas.scale(mRotatePanel.getScale(), mRotatePanel.getScale(),
                    imageRect.width() / 2, imageRect.height() / 2);
            canvas.rotate(mRotatePanel.getRotateAngle(), imageRect.width() / 2,
                    imageRect.height() / 2);

            canvas.drawBitmap(originBit, new Rect(0, 0, originBit.getWidth(),
                    originBit.getHeight()), dst, null);
            canvas.restore();

            //saveBitmap(result, activity.saveFilePath);// 保存图片
            return result;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            //dialog.dismiss();
            if (result == null)
                return;

            activity.changeMainBitmap(result);
            backToMain();
        }
    }
}
