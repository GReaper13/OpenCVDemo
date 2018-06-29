package demo.eco.greaper.opencvdemo;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import demo.eco.greaper.opencvdemo.base.BaseEditFragment;

public class MainMenuFragment extends BaseEditFragment {

    @BindView(R.id.filtericonimage)
    ImageView filtericonimage;
    @BindView(R.id.menu_filter)
    LinearLayout menuFilter;
    @BindView(R.id.enhanceiconimage)
    ImageView enhanceiconimage;
    @BindView(R.id.menu_enhance)
    LinearLayout menuEnhance;
    @BindView(R.id.cropiconimage)
    ImageView cropiconimage;
    @BindView(R.id.menu_adjust)
    LinearLayout menuAdjust;
    @BindView(R.id.stickericonimage)
    ImageView stickericonimage;
    @BindView(R.id.menu_sticker)
    LinearLayout menuSticker;
    @BindView(R.id.frameiconimage)
    ImageView frameiconimage;
    @BindView(R.id.menu_frame)
    LinearLayout menuFrame;
    @BindView(R.id.texticonimage)
    ImageView texticonimage;
    @BindView(R.id.menu_write)
    LinearLayout menuWrite;
    Unbinder unbinder;
    Context context;

    public static MainMenuFragment newInstance() {
        MainMenuFragment fragment = new MainMenuFragment();
        return fragment;
    }

    @Override
    public void onShow() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editor_main, container, false);
        context = getActivity();
        unbinder = ButterKnife.bind(this, view);
        highLightSelectedOption(EditActivity.mode);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        unbinder.unbind();
    }

    @OnClick({R.id.menu_filter, R.id.menu_enhance, R.id.menu_adjust, R.id.menu_sticker, R.id.menu_frame, R.id.menu_write})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.menu_filter:
                activity.changeMode(EditActivity.MODE_FILTERS);
                activity.sliderFragment.resetBitmaps();
                activity.changeMiddleFragment(EditActivity.MODE_FILTERS);
                break;
            case R.id.menu_enhance:
                activity.changeMode(EditActivity.MODE_ENHANCE);
                activity.sliderFragment.resetBitmaps();
                activity.changeMiddleFragment(EditActivity.MODE_ENHANCE);
                break;
            case R.id.menu_adjust:
                activity.changeMode(EditActivity.MODE_ADJUST);
                activity.changeMiddleFragment(EditActivity.MODE_ADJUST);
                break;
            case R.id.menu_sticker:
                activity.changeMode(EditActivity.MODE_STICKER_TYPES);
                activity.changeMiddleFragment(EditActivity.MODE_STICKER_TYPES);
                break;
            case R.id.menu_frame:
                activity.changeMode(EditActivity.MODE_FRAME);
                activity.changeMiddleFragment(EditActivity.MODE_FRAME);
                break;
            case R.id.menu_write:
                activity.changeMode(EditActivity.MODE_WRITE);
                activity.changeMiddleFragment(EditActivity.MODE_WRITE);
                break;
        }
    }

    // This method used to highlight the mode is selecting
    public void highLightSelectedOption(int mode) {
        menuFilter.setBackgroundColor(Color.TRANSPARENT);
        menuEnhance.setBackgroundColor(Color.TRANSPARENT);
        menuAdjust.setBackgroundColor(Color.TRANSPARENT);
        menuSticker.setBackgroundColor(Color.TRANSPARENT);
        menuWrite.setBackgroundColor(Color.TRANSPARENT);
        menuFrame.setBackgroundColor(Color.TRANSPARENT);
        int color = ContextCompat.getColor(context, R.color.md_grey_200);
        switch (mode) {
            case 2:
                menuFilter.setBackgroundColor(color);
                break;
            case 3:
                menuEnhance.setBackgroundColor(color);
                break;
            case 4:
                menuAdjust.setBackgroundColor(color);
                break;
            case 5:
                menuSticker.setBackgroundColor(color);
                break;
            case 6:
                menuWrite.setBackgroundColor(color);
                break;
            case 12:
                menuFrame.setBackgroundColor(color);
                break;
            default:
                break;
        }
    }
}
