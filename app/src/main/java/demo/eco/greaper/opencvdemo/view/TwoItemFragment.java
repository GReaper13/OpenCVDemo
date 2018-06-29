package demo.eco.greaper.opencvdemo.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import demo.eco.greaper.opencvdemo.EditActivity;
import demo.eco.greaper.opencvdemo.R;
import demo.eco.greaper.opencvdemo.base.BaseEditFragment;

public class TwoItemFragment extends BaseEditFragment {
    int mode;
    @BindView(R.id.item1iconimage)
    ImageView item1iconimage;
    @BindView(R.id.item1text)
    TextView item1text;
    @BindView(R.id.menu_item1)
    LinearLayout menuItem1;
    @BindView(R.id.item2iconimage)
    ImageView item2iconimage;
    @BindView(R.id.item2text)
    TextView item2text;
    @BindView(R.id.menu_item2)
    LinearLayout menuItem2;
    Unbinder unbinder;

    public static TwoItemFragment newInstance(int mode) {
        TwoItemFragment fragment = new TwoItemFragment();
        fragment.mode = mode;
        return fragment;
    }

    @Override
    public void onShow() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_two_item, container, false);
        unbinder = ButterKnife.bind(this, view);

        if (mode == EditActivity.MODE_WRITE) {
            item1iconimage.setImageResource(R.drawable.ic_text);
            item2iconimage.setImageResource(R.drawable.ic_paint);

            item1text.setText("Text");
            item2text.setText("Paint");
        }else {
            item1iconimage.setImageResource(R.drawable.ic_crop);
            item2iconimage.setImageResource(R.drawable.ic_rotate);

            item1text.setText("Crop");
            item2text.setText("Rotate");
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        unbinder.unbind();
    }

    @OnClick({R.id.menu_item1, R.id.menu_item2})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.menu_item1:
                clearSelection();
                menuItem1.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.md_grey_200));
                firstItemClicked();
                break;
            case R.id.menu_item2:
                clearSelection();
                menuItem2.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.md_grey_200));
                secondItemClicked();
                break;
        }
    }

    public void clearSelection() {
        menuItem1.setBackgroundColor(Color.TRANSPARENT);
        menuItem2.setBackgroundColor(Color.TRANSPARENT);
    }

    private void firstItemClicked() {
        if (mode == EditActivity.MODE_ADJUST){
            activity.changeMode(EditActivity.MODE_CROP);
            activity.changeBottomFragment(EditActivity.MODE_CROP);
        }else if (mode == EditActivity.MODE_WRITE){
            activity.changeMode(EditActivity.MODE_TEXT);
            activity.changeBottomFragment(EditActivity.MODE_TEXT);
        }
    }

    private void secondItemClicked() {
        if (mode == EditActivity.MODE_ADJUST){
            activity.changeMode(EditActivity.MODE_ROTATE);
            activity.changeBottomFragment(EditActivity.MODE_ROTATE);
        }else if (mode == EditActivity.MODE_WRITE){
            activity.changeMode(EditActivity.MODE_PAINT);
            activity.changeBottomFragment(EditActivity.MODE_PAINT);
        }
    }
}
