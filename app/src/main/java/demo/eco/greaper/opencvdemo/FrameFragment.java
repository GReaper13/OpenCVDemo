package demo.eco.greaper.opencvdemo;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import demo.eco.greaper.opencvdemo.base.BaseEditFragment;

public class FrameFragment extends BaseEditFragment {

    private static Bitmap original;
    @BindView(R.id.cancel)
    ImageButton cancel;
    @BindView(R.id.frameRecyler)
    RecyclerView frameRecyler;
    @BindView(R.id.done)
    ImageButton done;
    Unbinder unbinder;
    private ArrayList<Bitmap> arrayList = null;
    private Bitmap lastBitmap;
    private int lastFrame = 99;
    View frameView;

    public static FrameFragment newInstance(Bitmap bmp) {
        Bundle args = new Bundle();
        FrameFragment fragment = new FrameFragment();
        fragment.setArguments(args);
        original = bmp.copy(Bitmap.Config.ARGB_8888, true);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        cancel.setImageResource(R.drawable.ic_close_black_24dp);
        done.setImageResource(R.drawable.ic_done_black_24dp);
        onShow();
        setUpLayoutManager();
        recyclerView rv = new recyclerView();
        frameRecyler.setAdapter(rv);
    }

    //Helper methods
    //set linearLayoutManager
    private void setUpLayoutManager() {
        LinearLayoutManager linearLayoutManager;
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        frameRecyler.setLayoutManager(linearLayoutManager);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        frameView = inflater.inflate(R.layout.fragment_frame, container, false);
        unbinder = ButterKnife.bind(this, frameView);
        return frameView;
    }

    @Override
    public void onShow() {
        asyncThumbs asyncThumbs = new asyncThumbs();
        asyncThumbs.execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.cancel, R.id.done})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                activity.mainImage.setImageBitmap(original);
                setVisibilty(false);
                break;
            case R.id.done:
                activity.changeMainBitmap(lastBitmap);
                backToMain();
                break;
        }
    }

    public void backToMain() {
        setVisibilty(false);
        activity.changeMode(EditActivity.MODE_MAIN);
    }

    private void setVisibilty(Boolean visibility) {
        if (visibility) {
            cancel.setVisibility(View.VISIBLE);
            done.setVisibility(View.VISIBLE);
        } else {
            cancel.setVisibility(View.GONE);
            done.setVisibility(View.GONE);
        }
    }

    private class recyclerView extends android.support.v7.widget.RecyclerView.Adapter<recyclerView.viewHolder> {

        @Override
        public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(getContext(), R.layout.frame, null);
            return new viewHolder(view);
        }
        @Override
        public void onBindViewHolder(final viewHolder holder, final int position) {
            holder.imageView.setImageBitmap(arrayList.get(position));
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!checkVisibility()) {
                        setVisibilty(true);
                    }
                    if (lastFrame != position) {
                        loadFrame(position);
                    }
                }
            });
        }
        @Override
        public int getItemCount() {
            return arrayList.size();
        }
        public class viewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {

            private ImageView imageView;
            public viewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.frames);
            }
        }
    }

    private boolean checkVisibility() {
        return cancel.getVisibility() == View.VISIBLE ? true : false;
    }

    //start asyncFrame.execute()
    private void loadFrame(int pos) {
        new asyncFrame().execute(pos);
    }

    private class asyncFrame extends AsyncTask<Integer, Integer, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activity.showProgressBar();
        }
        @Override
        protected Bitmap doInBackground(Integer... params) {
            return drawFrame(params[0]);
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            activity.mainImage.setImageBitmap(bitmap);
            lastBitmap = bitmap;
            activity.hideProgressBar();
        }
        /**
         * @param pos selected name of frame from assets
         */
        private Bitmap drawFrame(int pos) {
            InputStream is;
            try {
                if (original != null && pos < 11) {
                    is = getResources().getAssets().open("frames" + File.separator + pos + ".png");
                    Offset of;
                    of = offset(pos);
                    int width = of.getWidth();
                    int height = of.getHeight();
                    Bitmap main = original;
                    Bitmap temp = main.copy(Bitmap.Config.ARGB_8888, true);
                    Bitmap frame = BitmapFactory.decodeStream(is).copy(Bitmap.Config.ARGB_8888, true);
                    is.close();
                    Bitmap draw = Bitmap.createScaledBitmap(frame, (2 * (width)) + temp.getWidth(), (2 * (height)) + temp.getHeight(), false);
                    of = null;
                    of = offset(draw);
                    int widthForTemp = of.getWidth();
                    int heightForTemp = of.getHeight();
                    //calculate offset after scaling
                    Bitmap latestBmp = Bitmap.createBitmap(2 * (widthForTemp) + temp.getWidth(), 2 * (heightForTemp) + temp.getHeight(), Bitmap.Config.ARGB_8888);
                    Bitmap frameNew = Bitmap.createScaledBitmap(frame, (2 * (widthForTemp)) + temp.getWidth(), (2 * (heightForTemp)) + temp.getHeight(), false);
                    frame.recycle();
                    Canvas can = new Canvas(latestBmp);
                    can.drawBitmap(temp, widthForTemp, heightForTemp, null);
                    can.drawBitmap(frameNew, 0, 0, null);
                    frame.recycle();
                    temp.recycle();
                    frameNew.recycle();
                    lastFrame = pos;
                    return latestBmp;
                } else {
                    Bitmap temp = original.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas can = new Canvas(temp);
                    is = getResources().getAssets().open("frames" + File.separator + pos + ".png");
                    Bitmap frame = BitmapFactory.decodeStream(is);
                    is.close();
                    Bitmap frameNew = Bitmap.createScaledBitmap(frame, temp.getWidth(), temp.getHeight(), false);
                    can.drawBitmap(frameNew, 0, 0, null);
                    frameNew.recycle();
                    frame.recycle();
                    lastFrame = pos;
                    return temp;
                }

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                return null;
            }

        }
        //get offset object
        private Offset offset(int pos) {
            int point_x = 0;
            int point_y = 0;
            int width_off = 0;
            int height_off = 0;
            Bitmap temp = null;
            try {
                temp = BitmapFactory.decodeStream(getResources().getAssets().open("frames" + File.separator + pos + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (temp.getPixel(point_x, temp.getHeight() / 2) != 0) {
                width_off++;
                point_x++;
            }
            while (temp.getPixel(temp.getWidth() / 2, point_y) != 0) {
                height_off++;
                point_y++;
            }
            return new Offset(width_off + 2, height_off + 2);
        }
        private Offset offset(Bitmap bitmap) {
            int point_x = 0;
            int point_y = 0;
            int width_off = 0;
            int height_off = 0;
            Bitmap temp;
            if (bitmap.isMutable()) {
                temp = bitmap;
            } else {
                temp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            }
            while (temp.getPixel(point_x, temp.getHeight() / 2) != 0) {
                width_off++;
                point_x++;
            }
            while (temp.getPixel(temp.getWidth() / 2, point_y) != 0) {
                height_off++;
                point_y++;
            }
            return new Offset(width_off + 2, height_off + 2);
        }
        //Offset class determines the offset of selected frame
        private class Offset {

            private int width, height;
            private Offset(int width, int height) {
                this.width = width;
                this.height = height;
            }
            public int getWidth() {
                return width;
            }
            public void setWidth(int width) {
                this.width = width;
            }
            public int getHeight() {
                return height;
            }
            public void setHeight(int height) {
                this.height = height;
            }
        }
    }

    // Asynchronous loading of thumbnails
    private class asyncThumbs extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            arrayList = new ArrayList<>();
            InputStream is = null;
            Bitmap tempBitmap;
            String frameFolder = "frames";
            AssetManager assetmanager = getResources().getAssets();
            try {
                String str[] = assetmanager.list("frames");
                for (int file = 0; file < str.length; file++) {
                    //sort according to name
                    is = assetmanager.open(frameFolder + File.separator + file + ".png");
                    tempBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(is), 140, 160, false);
                    tempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, new ByteArrayOutputStream());
                    arrayList.add(tempBitmap);
                }
                is.close();

            } catch (IOException IOE) {
                Log.i("App", "getAssets: " + IOE.getMessage());
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (arrayList == null)
                return;
            frameRecyler.getAdapter().notifyDataSetChanged();
        }
    }
}