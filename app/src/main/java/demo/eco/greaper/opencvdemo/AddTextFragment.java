package demo.eco.greaper.opencvdemo;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import demo.eco.greaper.opencvdemo.base.BaseEditFragment;
import demo.eco.greaper.opencvdemo.color.ColorPalette;
import demo.eco.greaper.opencvdemo.font.FontPickerDialog;
import demo.eco.greaper.opencvdemo.view.StickerTask;
import demo.eco.greaper.opencvdemo.view.TextStickerView;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;

import static android.graphics.Color.WHITE;

public class AddTextFragment extends BaseEditFragment implements TextWatcher, FontPickerDialog.FontPickerDialogListener {
    public static final int INDEX = 5;
    private View mainView;

    private EditText mInputText;
    private ImageView mTextColorSelector;
    private TextStickerView mTextStickerView;

    private InputMethodManager imm;
    private SaveTextStickerTask mSaveTask;

    public static AddTextFragment newInstance() {
        return new AddTextFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mainView = inflater.inflate(R.layout.fragment_add_text, null);
        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTextStickerView = (TextStickerView) getActivity().findViewById(R.id.text_sticker_panel);

        View cancel = mainView.findViewById(R.id.text_cancel);
        View apply = mainView.findViewById(R.id.text_apply);
        ImageButton ibFontChoice = (ImageButton) mainView.findViewById(R.id.text_font);

        ((ImageButton) cancel).setColorFilter(Color.BLACK);
        ((ImageButton) apply).setColorFilter(Color.BLACK);

        mInputText = (EditText) mainView.findViewById(R.id.text_input);
        mTextColorSelector = (ImageView) mainView.findViewById(R.id.text_color);
        mTextColorSelector.setImageDrawable(new IconicsDrawable(activity).icon(GoogleMaterial.Icon.gmd_format_color_fill).sizeDp(24));

        cancel.setOnClickListener(new BackToMenuClick());
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyTextImage();
            }
        });
        mTextColorSelector.setOnClickListener(new SelectColorBtnClick());
        mInputText.addTextChangedListener(this);
        boolean focus = mInputText.requestFocus();
        if (focus) {
            imm.showSoftInput(mInputText, InputMethodManager.SHOW_IMPLICIT);
        }
        mTextStickerView.setEditText(mInputText);
        onShow();

        ibFontChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFontChoiceBox();
            }
        });
    }

    private void showFontChoiceBox() {
        DialogFragment dialogFragment = FontPickerDialog.newInstance(this);
        dialogFragment.show(getFragmentManager(), "fontPicker");
    }

    @Override
    public void afterTextChanged(Editable s) {
        String text = s.toString().trim();
        mTextStickerView.setText(text);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void onFontSelected(FontPickerDialog dialog) {
        mTextStickerView.setTextTypeFace(Typeface.createFromFile(dialog.getSelectedFont()));
    }

    private final class SelectColorBtnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            textColorDialog();
        }

    }

    private void textColorDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        final View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.color_piker_accent, null);
        final LineColorPicker colorPicker = (LineColorPicker) dialogLayout.findViewById(R.id.color_picker_accent);
        final TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.cp_accent_title);
        dialogTitle.setText("Text color");
        colorPicker.setColors(ColorPalette.getAccentColors(activity.getApplicationContext()));
        changeTextColor(WHITE);
        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                mTextColorSelector.setColorFilter(c);
                dialogTitle.setBackgroundColor(c);
                changeTextColor(colorPicker.getColor());

            }
        });
        dialogBuilder.setView(dialogLayout);
        dialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeTextColor(WHITE);
                dialog.cancel();
            }
        });
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                changeTextColor(colorPicker.getColor());
            }
        });
        dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        dialogBuilder.show();
    }

    private void changeTextColor(int newColor) {
        mTextStickerView.setTextColor(newColor);
    }

    public void hideInput() {
        if (getActivity() != null && getActivity().getCurrentFocus() != null && isInputMethodShow()) {
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public boolean isInputMethodShow() {
        return imm.isActive();
    }

    private final class BackToMenuClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            backToMain();
        }
    }

    public void backToMain() {
        hideInput();
        activity.changeMode(EditActivity.MODE_WRITE);
        activity.writeFragment.clearSelection();
        activity.changeBottomFragment(EditActivity.MODE_MAIN);
        activity.mainImage.setVisibility(View.VISIBLE);
        mTextStickerView.clearTextContent();
        mTextStickerView.setVisibility(View.GONE);
    }

    @Override
    public void onShow() {
        activity.changeMode(EditActivity.MODE_TEXT);
        activity.mainImage.setImageBitmap(activity.mainBitmap);
        activity.addTextFragment.getmTextStickerView().mainImage = activity.mainImage;
        activity.addTextFragment.getmTextStickerView().mainBitmap = activity.mainBitmap;
        mTextStickerView.setVisibility(View.VISIBLE);
        mInputText.clearFocus();
    }

    public TextStickerView getmTextStickerView(){return mTextStickerView;}

    public void applyTextImage() {
        if (mSaveTask != null) {
            mSaveTask.cancel(true);
        }

        mSaveTask = new SaveTextStickerTask(activity, activity.mainImage.getImageViewMatrix());
        mSaveTask.execute(activity.mainBitmap);
    }

    private final class SaveTextStickerTask extends StickerTask {

        public SaveTextStickerTask(EditActivity activity, Matrix imageViewMatrix) {
            super(activity, imageViewMatrix);
        }

        @Override
        public void handleImage(Canvas canvas, Matrix m) {
            float[] f = new float[9];
            m.getValues(f);
            int dx = (int) f[Matrix.MTRANS_X];
            int dy = (int) f[Matrix.MTRANS_Y];
            float scale_x = f[Matrix.MSCALE_X];
            float scale_y = f[Matrix.MSCALE_Y];
            canvas.save();
            canvas.translate(dx, dy);
            canvas.scale(scale_x, scale_y);
            mTextStickerView.drawText(canvas, mTextStickerView.layout_x,
                    mTextStickerView.layout_y, mTextStickerView.mScale, mTextStickerView.mRotateAngle);
            canvas.restore();
        }

        @Override
        public void onPostResult(Bitmap result) {
            mTextStickerView.clearTextContent();
            mTextStickerView.resetView();
            activity.changeMainBitmap(result);
            backToMain();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        resetTextStickerView();
    }

    private void resetTextStickerView() {
        if (null != mTextStickerView) {
            mTextStickerView.clearTextContent();
            mTextStickerView.setVisibility(View.GONE);
        }
    }

}
