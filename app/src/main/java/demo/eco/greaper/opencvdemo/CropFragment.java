package demo.eco.greaper.opencvdemo;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import demo.eco.greaper.opencvdemo.base.BaseEditFragment;
import demo.eco.greaper.opencvdemo.model.RatioItem;
import demo.eco.greaper.opencvdemo.utils.Matrix3;
import demo.eco.greaper.opencvdemo.view.CropImageView;
import demo.eco.greaper.opencvdemo.view.imagezoom.ImageViewTouchBase;

public class CropFragment extends BaseEditFragment {
    @BindView(R.id.crop_cancel)
    ImageButton cropCancel;
    @BindView(R.id.image_crop)
    LinearLayout imageCrop;
    @BindView(R.id.ratio_list_group)
    LinearLayout ratioListGroup;
    @BindView(R.id.crop_hsv)
    LinearLayout cropHsv;
    @BindView(R.id.crop_apply)
    ImageButton cropApply;
    Unbinder unbinder;
    private View mainView; // view of fragment
    private View cancel, apply;

    public static final int INDEX = 3;
    public static final String TAG = CropFragment.class.getName();
    public CropImageView mCropPanel;
    private static List<RatioItem> dataList = new ArrayList<RatioItem>();
    private List<TextView> textViewList = new ArrayList<TextView>();

    public static int SELECTED_COLOR = Color.BLUE;
    public static int UNSELECTED_COLOR = Color.BLACK;
    private CropRationClick mCropRationClick = new CropRationClick();
    public TextView selctedTextView;

    public static CropFragment newInstance() {
        CropFragment fragment = new CropFragment();
        return fragment;
    }

    @Override
    public void onShow() {
        activity.changeMode(EditActivity.MODE_CROP);
        activity.mCropPanel.setVisibility(View.VISIBLE);
        activity.mainImage.setImageBitmap(activity.mainBitmap);
        activity.mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        activity.mainImage.setScaleEnabled(false);
        RectF r = activity.mainImage.getBitmapRect();
        activity.mCropPanel.setCropRect(r);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataList.add(new RatioItem("1:1", 1f,new IconicsDrawable(this.getContext()).icon(GoogleMaterial.Icon.gmd_crop_square).sizeDp(18)));
        dataList.add(new RatioItem("3:2", 3 / 2f,new IconicsDrawable(this.getContext()).icon(GoogleMaterial.Icon.gmd_crop_3_2).sizeDp(18)));
        dataList.add(new RatioItem("Free", -1f,new IconicsDrawable(this.getContext()).icon(GoogleMaterial.Icon.gmd_crop_free).sizeDp(18)));
        dataList.add(new RatioItem("7:5",7/5f,new IconicsDrawable(this.getContext()).icon(GoogleMaterial.Icon.gmd_crop_7_5).sizeDp(18)));
        dataList.add(new RatioItem("16:9",16/9f,new IconicsDrawable(this.getContext()).icon(GoogleMaterial.Icon.gmd_crop_16_9).sizeDp(18)));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpRatioList();
        this.mCropPanel = ensureEditActivity().mCropPanel;
        onShow();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_crop, container, false);
        unbinder = ButterKnife.bind(this, mainView);
        return mainView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        unbinder.unbind();
    }

    @OnClick({R.id.crop_cancel, R.id.crop_apply})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.crop_cancel:
                backToMain();
                break;
            case R.id.crop_apply:
                applyCropImage();
                break;
        }
    }

    public void backToMain() {
        activity.changeMode(EditActivity.MODE_ADJUST);
        activity.changeBottomFragment(EditActivity.MODE_MAIN);
        activity.adjustFragment.clearSelection();
        mCropPanel.setVisibility(View.GONE);
        activity.mainImage.setScaleEnabled(true);
        if (selctedTextView != null) {
            selctedTextView.setTextColor(UNSELECTED_COLOR);
        }
        mCropPanel.setRatioCropRect(activity.mainImage.getBitmapRect(), -1);
    }

    public void applyCropImage() {
        CropImageTask task = new CropImageTask();
        task.execute(activity.mainBitmap);
    }

    private final class CropImageTask extends AsyncTask<Bitmap, Void, Bitmap> {
        private Dialog dialog;

        @Override
        protected void onCancelled() {
            super.onCancelled();
            dialog.dismiss();
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onCancelled(Bitmap result) {
            super.onCancelled(result);
            dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = getLoadingDialog(getActivity(), "Saving...",
                    false);
            dialog.show();
        }

        @SuppressWarnings("WrongThread")
        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            RectF cropRect = mCropPanel.getCropRect();
            Matrix touchMatrix = activity.mainImage.getImageViewMatrix();
            // Canvas canvas = new Canvas(resultBit);
            float[] data = new float[9];
            touchMatrix.getValues(data);
            Matrix3 cal = new Matrix3(data);
            Matrix3 inverseMatrix = cal.inverseMatrix();
            Matrix m = new Matrix();
            m.setValues(inverseMatrix.getValues());
            m.mapRect(cropRect);

            Bitmap resultBit = Bitmap.createBitmap(params[0],
                    (int) cropRect.left, (int) cropRect.top,
                    (int) cropRect.width(), (int) cropRect.height());

            return resultBit;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (result == null)
                return;

            activity.changeMainBitmap(result);
            activity.mCropPanel.setCropRect(activity.mainImage.getBitmapRect());
            backToMain();
        }
    }

    public static Dialog getLoadingDialog(Context context, String title,
                                          boolean canCancel) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCancelable(canCancel);
        dialog.setMessage(title);
        return dialog;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        resetCropView();
    }

    private void resetCropView() {
        if (null != activity && null != mCropPanel){
            mCropPanel.setVisibility(View.GONE);
            RectF r = activity.mainImage.getBitmapRect();
            activity.mCropPanel.setCropRect(r);
        }
    }

    private void setUpRatioList() {
        ratioListGroup.removeAllViews();
        imageCrop.removeAllViews();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        params.gravity = Gravity.CENTER_VERTICAL;
        params1.leftMargin = 50;
        params.weight= (float) 0.2;
        params1.weight= (float) 0.2;
        for (int i = 0, len = 5; i < len; i++) {
            ImageView image = new ImageView(activity);
            TextView text = new TextView(activity);
            image.setImageDrawable(dataList.get(i).getImage());
            text.setTextColor(UNSELECTED_COLOR);
            text.setTextSize(18);
            text.setText(dataList.get(i).getText());
            textViewList.add(text);
            ratioListGroup.addView(text, params1);
            imageCrop.addView(image,params);
            text.setTag(i);
            if (i == 0) {
                selctedTextView = text;
            }
            dataList.get(i).setIndex(i);
            text.setTag(dataList.get(i));
            text.setOnClickListener(mCropRationClick);
        }
        selctedTextView.setTextColor(SELECTED_COLOR);
    }

    private final class CropRationClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            TextView curTextView = (TextView) v;
            selctedTextView.setTextColor(UNSELECTED_COLOR);
            RatioItem dataItem = (RatioItem) v.getTag();
            selctedTextView = curTextView;
            selctedTextView.setTextColor(SELECTED_COLOR);

            mCropPanel.setRatioCropRect(activity.mainImage.getBitmapRect(),
                    dataItem.getRatio());
        }
    }
}
