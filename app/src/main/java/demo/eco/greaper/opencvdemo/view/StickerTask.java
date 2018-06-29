package demo.eco.greaper.opencvdemo.view;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.AsyncTask;

import demo.eco.greaper.opencvdemo.EditActivity;
import demo.eco.greaper.opencvdemo.utils.Matrix3;

public abstract class StickerTask extends AsyncTask<Bitmap, Void, Bitmap> {
    private Dialog dialog;

    private EditActivity mContext;
    private Matrix imageViewMatrix;

    public StickerTask(EditActivity activity,Matrix imageViewMatrix) {
        this.mContext = activity;
        this.imageViewMatrix=imageViewMatrix;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mContext.isFinishing())
            return;
        dialog = getLoadingDialog(mContext, "Saving...", false);
        dialog.show();
    }

    @Override
    protected Bitmap doInBackground(Bitmap... params) {
        // System.out.println("保存贴图!");
        Matrix touchMatrix = imageViewMatrix;

        Bitmap resultBit = Bitmap.createBitmap(params[0]).copy(
                Bitmap.Config.RGB_565, true);
        Canvas canvas = new Canvas(resultBit);

        float[] data = new float[9];
        touchMatrix.getValues(data);
        Matrix3 cal = new Matrix3(data);
        Matrix3 inverseMatrix = cal.inverseMatrix();
        Matrix m = new Matrix();
        m.setValues(inverseMatrix.getValues());

        handleImage(canvas, m);

        //BitmapUtils.saveBitmap(resultBit, mContext.saveFilePath);
        return resultBit;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        dialog.dismiss();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        onPostResult(bitmap);
        dialog.dismiss();
    }

    public abstract void handleImage(Canvas canvas, Matrix m);

    public abstract void onPostResult(Bitmap result);

    public static Dialog getLoadingDialog(Context context, String title,
                                          boolean canCancel) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCancelable(canCancel);
        dialog.setMessage(title);
        return dialog;
    }
}
