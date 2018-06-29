package demo.eco.greaper.opencvdemo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import demo.eco.greaper.opencvdemo.utils.BitmapUtils;
import demo.eco.greaper.opencvdemo.view.CropImageView;
import demo.eco.greaper.opencvdemo.view.CustomPaintView;
import demo.eco.greaper.opencvdemo.view.RotateImageView;
import demo.eco.greaper.opencvdemo.view.StickerView;
import demo.eco.greaper.opencvdemo.view.TwoItemFragment;
import demo.eco.greaper.opencvdemo.view.imagezoom.ImageViewTouch;
import demo.eco.greaper.opencvdemo.view.imagezoom.ImageViewTouchBase;

public class EditActivity extends AppCompatActivity implements View.OnTouchListener {

    public static final String IMAGE_IS_EDIT = "image_is_edit";

    public Bitmap mainBitmap;
    private LoadImageTask mLoadImageTask;
    private EditActivity mContext;

    public static int mode;
    public static int effectType;

    @BindView(R.id.edit_cancel)
    ImageButton btnCancel;
    @BindView(R.id.edit_undo)
    ImageButton btnUndo;
    @BindView(R.id.edit_befaft)
    ImageButton btnBefaft;
    @BindView(R.id.edit_redo)
    ImageButton btnRedo;
    @BindView(R.id.edit_save)
    ImageButton btnSave;
    @BindView(R.id.button_container)
    RelativeLayout buttonContainer;
    @BindView(R.id.preview_container)
    FrameLayout previewContainer;
    @BindView(R.id.controls_container)
    FrameLayout controlsContainer;
    @BindView(R.id.control_area)
    LinearLayout controlArea;
    @BindView(R.id.main_image)
    ImageViewTouch mainImage;
    @BindView(R.id.work_space)
    FrameLayout workSpace;
    @BindView(R.id.progress_bar_edit)
    ProgressBar progressBarEdit;
    @BindView(R.id.layout_main_edit)
    RelativeLayout layoutMainEdit;

    @BindView(R.id.sticker_panel)
    public StickerView mStickerView;
    @BindView(R.id.crop_panel)
    public CropImageView mCropPanel;
    @BindView(R.id.rotate_panel)
    public RotateImageView mRotatePanel;
    @BindView(R.id.custom_paint_view)
    public CustomPaintView mPaintView;

    /**
     * Different edit modes.
     */
    public static final int MODE_MAIN = 0;
    public static final int MODE_SLIDER = 1;
    public static final int MODE_FILTERS = 2;
    public static final int MODE_ENHANCE = 3;
    public static final int MODE_ADJUST = 4;
    public static final int MODE_STICKER_TYPES = 5;
    public static final int MODE_WRITE = 6;

    public static final int MODE_STICKERS = 7;
    public static final int MODE_CROP = 8;

    public static final int MODE_ROTATE = 9;
    public static final int MODE_TEXT = 10;
    public static final int MODE_PAINT = 11;
    public static final int MODE_FRAME = 12;

    public String filePath;
    public String saveFilePath;
    private int imageWidth, imageHeight;

    public MainMenuFragment mainMenuFragment;
    public SliderFragment sliderFragment;
    public RecyclerMenuFragment enhanceFragment,stickerTypesFragment, filterFragment;
    public StickersFragment stickersFragment;
    public TwoItemFragment writeFragment,adjustFragment;
    public AddTextFragment addTextFragment;
    public PaintFragment paintFragment;
    public CropFragment cropFragment;
    public RotateFragment rotateFragment;
    public FrameFragment frameFragment;

    private static String stickerType;
    public ArrayList<Bitmap> bitmapsForUndo;
    private int currentShowingIndex = -1;
    private Bitmap originalBitmap; // bitmap using before after
    private SaveImageTask mSaveImageTask;

    /**
     * Number of times image has been edited. Indicates whether image has been edited or not.
     */
    protected int mOpTimes = 0;
    protected boolean isBeenSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);
        initView();
        getData();
        // check can load image by memory if can
        checkInitImageLoader();


    }

    /**
     * Gets the image to be loaded from the intent and displays this image.
     */
    private void getData() {
        if (null != getIntent() && null != getIntent().getExtras()){
            Bundle bundle = getIntent().getExtras();
            filePath = bundle.getString(AppUtils.KEY_PATH_INTENT);
            saveFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/DEMO_" + System.currentTimeMillis() + ".png";
            loadImage(filePath);
            return;
        }
        Toast.makeText(mContext, "Error on get Picture", Toast.LENGTH_SHORT).show();
    }

    /**
     * Called from onCreate().
     * Initializes all view objects and fragments to be used.
     */
    private void initView() {
        mContext = this;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        imageWidth = metrics.widthPixels / 2;
        imageHeight = metrics.heightPixels / 2;

        bitmapsForUndo = new ArrayList<>();
//        progressBarEdit.getIndeterminateDrawable()
//                .setColorFilter(ColorPalette.getLighterColor(getPrimaryColor()), PorterDuff.Mode.SRC_ATOP);

        btnBefaft.setOnTouchListener(this);

        mode = MODE_FILTERS;

        mainMenuFragment = MainMenuFragment.newInstance();
        sliderFragment = SliderFragment.newInstance();
        filterFragment = RecyclerMenuFragment.newInstance(MODE_FILTERS);
        enhanceFragment = RecyclerMenuFragment.newInstance(MODE_ENHANCE);
        stickerTypesFragment = RecyclerMenuFragment.newInstance(MODE_STICKER_TYPES);
        adjustFragment = TwoItemFragment.newInstance(MODE_ADJUST);
        writeFragment = TwoItemFragment.newInstance(MODE_WRITE);
        addTextFragment = AddTextFragment.newInstance();
        paintFragment = PaintFragment.newInstance();
        cropFragment = CropFragment.newInstance();
        rotateFragment = RotateFragment.newInstance();

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (R.id.edit_befaft == v.getId()){
            if (MotionEvent.ACTION_DOWN == event.getAction()){
                switch (mode){
                    case MODE_SLIDER:
                        mainImage.setImageBitmap(mainBitmap);
                        break;
                    default:
                        mainImage.setImageBitmap(originalBitmap);
                }
            }else if (MotionEvent.ACTION_UP == event.getAction()){
                switch (mode){
                    case MODE_SLIDER:
                        mainImage.setImageBitmap(sliderFragment.filterBit);
                        break;
                    default:
                        mainImage.setImageBitmap(mainBitmap);
                }
            }
        }
        return true;
    }

    @OnClick({R.id.edit_cancel, R.id.edit_undo, R.id.edit_redo, R.id.edit_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.edit_cancel:
                onBackPressed();
                break;
            case R.id.edit_undo:
                onUndoPressed();
                break;
            case R.id.edit_redo:
                onRedoPressed();
                break;
            case R.id.edit_save:
                if (mOpTimes != 0)
                    doSaveImage();
                else
                    onSaveDone();
                break;
        }
    }

    private final class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {

            return BitmapUtils.getSampledBitmap(params[0], imageWidth,
                    imageHeight);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (mainBitmap != null) {
                mainBitmap.recycle();
                mainBitmap = null;
                System.gc();
            }
            mainBitmap = result;
            mainImage.setImageBitmap(result);
            // TODO view zoom
            mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
            originalBitmap = mainBitmap.copy(mainBitmap.getConfig(), true);
            addToUndoList();
            setInitialFragments();
        }
    }

    public void loadImage(String filepath) {
        if (mLoadImageTask != null) {
            mLoadImageTask.cancel(true);

        }
        mLoadImageTask = new LoadImageTask();
        mLoadImageTask.execute(filepath);
    }

    private void setInitialFragments() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.controls_container, mainMenuFragment)
                .commit();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.preview_container, filterFragment)
                .commit();

        setButtonsVisibility();
    }

    public void changeMainBitmap(Bitmap newBit) {
        if (mainBitmap != null) {
            if (!mainBitmap.isRecycled()) {
                mainBitmap.recycle();
            }
        }
        mainBitmap = newBit;
        mainImage.setImageBitmap(mainBitmap);
        // TODO zoom list
        mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        addToUndoList();
        setButtonsVisibility();
        increaseOpTimes();
    }

    //Increment no. of times the image has been edited
    public void increaseOpTimes() {
        mOpTimes++;
        isBeenSaved = false;
    }

    private void setButtonsVisibility() {
        btnSave.setVisibility(View.VISIBLE);
        btnBefaft.setVisibility(View.VISIBLE);
        // undo and redo
        if (currentShowingIndex > 0) {
            btnUndo.setColorFilter(Color.BLACK);
            btnUndo.setEnabled(true);
        } else {
            btnUndo.setColorFilter(getResources().getColor(R.color.md_grey_300));
            btnUndo.setEnabled(false);
        }
        if (currentShowingIndex + 1 < bitmapsForUndo.size()) {
            btnRedo.setColorFilter(Color.BLACK);
            btnRedo.setEnabled(true);
        } else {
            btnRedo.setColorFilter(getResources().getColor(R.color.md_grey_300));
            btnRedo.setEnabled(false);
        }


        switch (mode) {
            case MODE_STICKERS:
            case MODE_CROP:
            case MODE_ROTATE:
            case MODE_TEXT:
            case MODE_PAINT:
                btnSave.setVisibility(View.INVISIBLE);
                btnBefaft.setVisibility(View.INVISIBLE);
                break;
            case MODE_SLIDER:
                btnSave.setVisibility(View.INVISIBLE);
                break;
        }
    }

    public void changeMode(int to_mode) {
        EditActivity.mode = to_mode;
        highLightSelectedOption(to_mode);
    }

    private void highLightSelectedOption(int mode) {
        switch (mode) {
            case MODE_FILTERS:
            case MODE_ENHANCE:
            case MODE_ADJUST:
            case MODE_STICKER_TYPES:
            case MODE_FRAME:
            case MODE_WRITE:
                mainMenuFragment.highLightSelectedOption(mode);
                break;
            case MODE_STICKERS:
            case MODE_TEXT:
            case MODE_PAINT:
            case MODE_CROP:
            case MODE_ROTATE:
            case MODE_SLIDER:


        }
    }

    // This method using each change mode >> change fragment bottom
    public void changeBottomFragment(int mode) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.controls_container, getFragment(mode))
                .commit();

        setButtonsVisibility();
    }

    // This method using each change mode >> change fragment middle
    public void changeMiddleFragment(int index) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.preview_container, getFragment(index))
                .commit();
    }

    public Fragment getFragment(int mode) {
        switch (mode) {
            case MODE_MAIN:
                return mainMenuFragment;
            case MODE_SLIDER:
                sliderFragment = SliderFragment.newInstance();
                return sliderFragment;
            case MODE_FILTERS:
                return filterFragment;
            case MODE_ENHANCE:
                return enhanceFragment;
            case MODE_STICKER_TYPES:
                return stickerTypesFragment;
            case MODE_STICKERS:
                stickersFragment = StickersFragment.newInstance(addStickerImages(stickerType));
                return stickersFragment;
            case MODE_WRITE:
                return writeFragment;
            case MODE_ADJUST:
                return adjustFragment;
            case MODE_TEXT:
                return addTextFragment;
            case MODE_PAINT:
                return paintFragment;
            case MODE_CROP:
                return cropFragment;
            case MODE_ROTATE:
                return rotateFragment;
            case MODE_FRAME:
                return frameFragment = FrameFragment.newInstance(mainBitmap);
        }
        return mainMenuFragment;
    }

    private ArrayList<String> addStickerImages(String folderPath) {
        ArrayList<String> pathList = new ArrayList<>();
        try {
            String[] files = getAssets().list(folderPath);

            for (String name : files) {
                pathList.add(folderPath + File.separator + name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pathList;
    }


    public void showProgressBar() {
        progressBarEdit.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBarEdit.setVisibility(View.GONE);
    }

    //
    public void setEffectType(int type, int mode) {
        effectType = 100 * mode + type;
    }

    public void setStickerType(String stickerType) {
        this.stickerType = stickerType;
    }

    // Using Undo and Redo
    private void addToUndoList() {
        try {
            TODO:
// implement a more efficient way, like storing only the difference of bitmaps or
            // steps followed to edit
            recycleBitmapList(++currentShowingIndex);
            bitmapsForUndo.add(mainBitmap.copy(mainBitmap.getConfig(), true));
        } catch (OutOfMemoryError error) {
            /**
             * When outOfMemory exception throws then to make space, remove the last edited step
             * from list and added the new operation in the end.
             */
            bitmapsForUndo.get(1).recycle();
            bitmapsForUndo.remove(1);
            bitmapsForUndo.add(mainBitmap.copy(mainBitmap.getConfig(), true));
        }
    }

    private void recycleBitmapList(int fromIndex) {
        while (fromIndex < bitmapsForUndo.size()) {
            bitmapsForUndo.get(fromIndex).recycle();
            bitmapsForUndo.remove(fromIndex);
        }
    }

    private Bitmap getUndoBitmap() {
        if (currentShowingIndex - 1 >= 0)
            currentShowingIndex -= 1;
        else currentShowingIndex = 0;

        return bitmapsForUndo
                .get(currentShowingIndex)
                .copy(bitmapsForUndo.get(currentShowingIndex).getConfig(), true);
    }


    private Bitmap getRedoBitmap() {
        if (currentShowingIndex + 1 <= bitmapsForUndo.size())
            currentShowingIndex += 1;
        else currentShowingIndex = bitmapsForUndo.size() - 1;

        return bitmapsForUndo
                .get(currentShowingIndex)
                .copy(bitmapsForUndo.get(currentShowingIndex).getConfig(), true);
    }

    private void onUndoPressed() {
        if (mainBitmap != null) {
            if (!mainBitmap.isRecycled()) {
                mainBitmap.recycle();
            }
        }
        mainBitmap = getUndoBitmap();
        mainImage.setImageBitmap(mainBitmap);
        // TODO zoom view
        mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        setButtonsVisibility();
    }

    private void onRedoPressed() {

        if (mainBitmap != null) {
            if (!mainBitmap.isRecycled()) {
                mainBitmap.recycle();
            }
        }
        mainBitmap = getRedoBitmap();
        mainImage.setImageBitmap(mainBitmap);
        // TODO zoom view
        mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        setButtonsVisibility();
    }

    // Using before after
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
            originalBitmap = null;
        }
        if (mLoadImageTask != null) {
            mLoadImageTask.cancel(true);
        }

        if (mSaveImageTask != null) {
            mSaveImageTask.cancel(true);
        }

        recycleBitmapList(0);
    }

    @Override
    public void onBackPressed() {
        // ask when back on mode (discard change) and save file
        super.onBackPressed();
    }

    // Save file
    protected void doSaveImage() {
        if (mSaveImageTask != null) {
            mSaveImageTask.cancel(true);
        }

        mSaveImageTask = new SaveImageTask();
        mSaveImageTask.execute(mainBitmap);
    }

    private final class SaveImageTask extends AsyncTask<Bitmap, Void, Boolean> {
        private Dialog dialog;

        @Override
        protected Boolean doInBackground(Bitmap... params) {
            if (TextUtils.isEmpty(saveFilePath))
                return false;

            return BitmapUtils.saveBitmap(params[0], saveFilePath);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            dialog.dismiss();
        }

        @Override
        protected void onCancelled(Boolean result) {
            super.onCancelled(result);
            dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = getLoadingDialog(mContext, "Saving...", false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            dialog.dismiss();

            if (result) {
                resetOpTimes();
//                onSaveTaskDone();
                onSaveDone();
            } else {
                Log.d("App", "Error");
            }
        }
    }

    public static Dialog getLoadingDialog(Context context, String title,
                                          boolean canCancel) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCancelable(canCancel);
        dialog.setMessage(title);
        return dialog;
    }

    public void resetOpTimes() {
        isBeenSaved = true;
    }

    private void onSaveDone() {
        Toast.makeText(mContext, "Save file sucessfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(mContext, MainActivity.class));
        finish();
    }

    public boolean canAutoExit() {
        return isBeenSaved || mOpTimes == 0;
    }

    private void checkInitImageLoader() {
        if (!ImageLoader.getInstance().isInited()) {
            initImageLoader();
        }
    }

    private void initImageLoader() {
        File cacheDir = com.nostra13.universalimageloader.utils.StorageUtils.getCacheDirectory(this);
        int MAXMEMONRY = (int) (Runtime.getRuntime().maxMemory());
        // System.out.println("dsa-->"+MAXMEMONRY+"   "+(MAXMEMONRY/5));//.memoryCache(new
        // LruMemoryCache(50 * 1024 * 1024))
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this).memoryCacheExtraOptions(480, 800).defaultDisplayImageOptions(defaultOptions)
                .diskCacheExtraOptions(480, 800, null).threadPoolSize(3)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(MAXMEMONRY / 5))
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
                .imageDownloader(new BaseImageDownloader(this)) // default
                .imageDecoder(new BaseImageDecoder(false)) // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()).build();

        ImageLoader.getInstance().init(config);
    }
}
