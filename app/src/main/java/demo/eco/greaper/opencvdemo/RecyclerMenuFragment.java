package demo.eco.greaper.opencvdemo;

import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import demo.eco.greaper.opencvdemo.base.BaseEditFragment;

// Class is used in fragment has recycler view (Filter, Enhance, Sticker)
public class RecyclerMenuFragment extends BaseEditFragment {

    static int currentSelection = -1;
    int defaulticon;
    RecyclerView recyclerView;
    View fragmentView;
    int MODE;
    TypedArray iconlist,titlelist;
    static final String[] stickerPath = {"stickers/type1", "stickers/type2", "stickers/type3", "stickers/type4", "stickers/type5", "stickers/type6", "stickers/type7"};
    Bitmap currentBitmap;
    private static ArrayList<Bitmap> filterThumbs; // list thumbnails of filter
    int bmWidth = -1,bmHeight = -1;

    @Override
    public void onShow() {
        // get all thumbnails of filter mode
        if (MODE == EditActivity.MODE_FILTERS) {
            if (this.currentBitmap != activity.mainBitmap) filterThumbs = null;
            this.currentBitmap = activity.mainBitmap;
            getFilterThumbs();
        }
    }

    public void getFilterThumbs() {
        if (null!= currentBitmap) {
            GetFilterThumbsTask getFilterThumbsTask = new GetFilterThumbsTask();
            getFilterThumbsTask.execute();
        }
    }

    // resize of edit image
    private Bitmap getResizedBitmap(Bitmap bm, int divisor) {
        float scale = 1/(float)divisor;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        if (bmWidth <= 0 ) bmWidth = bm.getWidth();
        if (bmHeight <= 0) bmHeight = bm.getHeight();
        return Bitmap.createBitmap(bm, 0, 0, bmWidth, bmHeight, matrix, false);
    }

    public static RecyclerMenuFragment newInstance(int mode) {
        RecyclerMenuFragment fragment = new RecyclerMenuFragment();
        fragment.MODE = mode;
        return fragment;
    }

    public void clearCurrentSelection(){
        if(currentSelection != -1){
            mRecyclerAdapter.mViewHolder holder = (mRecyclerAdapter.mViewHolder) recyclerView.findViewHolderForAdapterPosition(currentSelection);
            if(holder != null){
                holder.wrapper.setBackgroundColor(Color.TRANSPARENT);
            }
            currentSelection = -1;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RecyclerView.LayoutManager layoutManager = null;
        int orientation = getActivity().getResources().getConfiguration().orientation;
        // set layout manager for each orientation of device
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        } else if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutManager = new LinearLayoutManager(getActivity());
        }
            recyclerView = (RecyclerView) fragmentView.findViewById(R.id.editor_recyclerview);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new mRecyclerAdapter());
//        this.mStickerView = activity.mStickerView;
        onShow();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.editor_recyclerview, container, false);
        return fragmentView;
    }

    private class GetFilterThumbsTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (filterThumbs==null) {
                filterThumbs = new ArrayList<>();
                bmWidth = currentBitmap.getWidth();
                bmHeight = currentBitmap.getHeight();
                int leng = (titlelist!=null) ? titlelist.length() : 0;
                for (int i = 0; i < leng; i++) {
                    filterThumbs.add(AppUtils.processImage(getResizedBitmap(currentBitmap, 5), (i + 100 * EditActivity.MODE_FILTERS), 100));
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void bitmaps) {
            super.onPostExecute(bitmaps);
            if (filterThumbs == null)
                return;

            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    class mRecyclerAdapter extends RecyclerView.Adapter<mRecyclerAdapter.mViewHolder>{

        class mViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title;
            LinearLayout wrapper;
            View view;
            mViewHolder(View itemView) {
                super(itemView);
                view = itemView;
                icon = (ImageView)itemView.findViewById(R.id.editor_item_image);
                title = (TextView)itemView.findViewById(R.id.editor_item_title);
                wrapper = (LinearLayout)itemView.findViewById(R.id.ll_effect_wrapper);
            }
        }

        mRecyclerAdapter() {
            defaulticon = R.drawable.ic_photo_filter;
            switch (MODE) {
                case EditActivity.MODE_FILTERS:
                    titlelist = getActivity().getResources().obtainTypedArray(R.array.filter_titles);
                    break;
                case EditActivity.MODE_ENHANCE:
                    iconlist = getActivity().getResources().obtainTypedArray(R.array.enhance_icons);
                    titlelist = getActivity().getResources().obtainTypedArray(R.array.enhance_titles);
                    break;
                case EditActivity.MODE_STICKER_TYPES:
                    iconlist = getActivity().getResources().obtainTypedArray(R.array.sticker_icons);
                    titlelist = getActivity().getResources().obtainTypedArray(R.array.sticker_titles);
                    break;
            }
        }

        @Override
        public mRecyclerAdapter.mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.editor_iconitem,parent,false);
            return new mViewHolder(view);
        }

        @Override
        public void onBindViewHolder(mRecyclerAdapter.mViewHolder holder, final int position) {

            if (MODE == EditActivity.MODE_STICKER_TYPES){
                holder.itemView.setTag(stickerPath[position]);
            }
            int iconImageSize = (int) getActivity().getResources().getDimension(R.dimen.icon_item_image_size_recycler);
            int midRowSize = (int) getActivity().getResources().getDimension(R.dimen.editor_mid_row_size);


            holder.icon.setScaleType(ImageView.ScaleType.FIT_CENTER);

            if (MODE == EditActivity.MODE_FILTERS) {
                if (currentBitmap!=null && filterThumbs!=null && filterThumbs.size() > position) {
                    iconImageSize = (int) getActivity().getResources().getDimension(R.dimen.icon_item_image_size_filter_preview);
                    midRowSize = (int) getActivity().getResources().getDimension(R.dimen.editor_filter_mid_row_size);
                    holder.icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    holder.icon.setImageBitmap(filterThumbs.get(position));
                }else {
                    holder.icon.setImageResource(defaulticon);
                }
            }else {
                holder.icon.setImageResource(iconlist.getResourceId(position, defaulticon));
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(iconImageSize,iconImageSize);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            holder.icon.setLayoutParams(layoutParams);
            holder.title.setText(titlelist.getString(position));
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(midRowSize,midRowSize);
            layoutParams.gravity = Gravity.CENTER;
            holder.wrapper.setLayoutParams(layoutParams2);
            holder.wrapper.setBackgroundColor(Color.TRANSPARENT);


            if(currentSelection == position)
                holder.wrapper.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_grey_200));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    highlightSelectedOption(position, v);
                    itemClicked(position,v);
                }
            });
        }

        private void highlightSelectedOption(int position, View v) {
            int color = ContextCompat.getColor(v.getContext(), R.color.md_grey_200);

            if(currentSelection != position){
                notifyItemChanged(currentSelection);
                ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            }

            if(currentSelection != -1 && recyclerView.findViewHolderForAdapterPosition(currentSelection) != null) {
                ((mRecyclerAdapter.mViewHolder) recyclerView.findViewHolderForAdapterPosition(currentSelection))
                        .wrapper
                        .setBackgroundColor(Color.TRANSPARENT);
            }

            ((mViewHolder) recyclerView.findViewHolderForAdapterPosition(position))
                    .wrapper
                    .setBackgroundColor(color);

            currentSelection = position;
        }

        @Override
        public int getItemCount() {
            return titlelist.length();
        }

        void itemClicked(int pos, View view){
            switch (MODE){
                case EditActivity.MODE_FILTERS:
                    activity.setEffectType(pos,MODE);
                    activity.changeMode(EditActivity.MODE_SLIDER);
                    activity.changeBottomFragment(EditActivity.MODE_SLIDER);
                    break;

                case EditActivity.MODE_ENHANCE:
                    activity.setEffectType(pos,MODE);
                    activity.changeMode(EditActivity.MODE_SLIDER);
                    activity.changeBottomFragment(EditActivity.MODE_SLIDER);
                    break;

                case EditActivity.MODE_STICKER_TYPES:
                    String data = (String) view.getTag();
                    activity.setStickerType(data);
                    activity.changeMode(EditActivity.MODE_STICKERS);
                    activity.changeBottomFragment(EditActivity.MODE_STICKERS);
                    break;
            }
        }
    }
}
