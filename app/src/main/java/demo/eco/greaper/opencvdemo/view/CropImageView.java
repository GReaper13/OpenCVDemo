package demo.eco.greaper.opencvdemo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import demo.eco.greaper.opencvdemo.R;
import demo.eco.greaper.opencvdemo.utils.PaintUtil;

public class CropImageView extends View {
    private static int STATUS_IDLE = 1;// Idle
    private static int STATUS_MOVE = 2;// Mobile status
    private static int STATUS_SCALE = 3;// Zoom state

    private int CIRCLE_WIDTH = 46; // width of a control point
    private float oldx, oldy; // old of x and y, using onTouch
    private int status = STATUS_IDLE;
    private int selectedControllerCicle; // using onTouch

    // 4 rect to define background of image has a crop (background shadow behind crop)
    private RectF backUpRect = new RectF();// ON
    private RectF backLeftRect = new RectF();// left
    private RectF backRightRect = new RectF();// right
    private RectF backDownRect = new RectF();// under

    private RectF cropRect = new RectF();// A rect determind cut area (area not shadow)

    private Paint mBackgroundPaint;// background Paint (shadow)
    private Bitmap circleBit; // bitmap of control point
    private Rect circleRect = new Rect(); // coordinate of
    // 4 rect of 4 control point
    private RectF leftTopCircleRect;
    private RectF rightTopCircleRect;
    private RectF leftBottomRect;
    private RectF rightBottomRect;

    private RectF imageRect = new RectF();// Image storage location information
    private RectF tempRect = new RectF();//Temporary Storage of rectangular data,
                                        // using in invalid crop (touch to an position not suitable >> restore to temp rect

    private float ratio = -1;// ratio of crop, -1 is free


    public CropImageView(Context context) {
        super(context);
        init(context);
    }

    public CropImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CropImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mBackgroundPaint = PaintUtil.newBackgroundPaint(context);
        circleBit = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.sticker_rotate);
        circleRect.set(0, 0, circleBit.getWidth(), circleBit.getHeight());
        leftTopCircleRect = new RectF(0, 0, CIRCLE_WIDTH, CIRCLE_WIDTH);// 4 rect just init value
        rightTopCircleRect = new RectF(leftTopCircleRect);
        leftBottomRect = new RectF(leftTopCircleRect);
        rightBottomRect = new RectF(leftTopCircleRect);
    }

    // set crop to default (free, crop middle)
    public void setCropRect(RectF rect) {
        imageRect.set(rect);
        cropRect.set(rect);
        scaleRect(cropRect, 0.5f);
        invalidate();
    }

    // set crop to a ratio (click on an item), r = -1 is back to main (default)
    public void setRatioCropRect(RectF rect, float r) {
        this.ratio = r;
        if (r < 0) {
            setCropRect(rect);
            return;
        }

        imageRect.set(rect);
        cropRect.set(rect);
        // setCropRect(rect);
        // Adjustment Rect

        float h, w;
        if (cropRect.width() >= cropRect.height()) {// w>=h
            h = cropRect.height() / 2;
            w = this.ratio * h;
        } else {// w<h
            w = rect.width() / 2;
            h = w / this.ratio;
        }// end if
        float scaleX = w / cropRect.width();
        float scaleY = h / cropRect.height();
        scaleRect(cropRect, scaleX, scaleY);
        invalidate();
    }

    // scaleRect by scaleX and scaleY
    private static void scaleRect(RectF rect, float scaleX, float scaleY) {
        float w = rect.width();
        float h = rect.height();

        float newW = scaleX * w;
        float newH = scaleY * h;

        float dx = (newW - w) / 2;
        float dy = (newH - h) / 2;

        rect.left -= dx;
        rect.top -= dy;
        rect.right += dx;
        rect.bottom += dy;
    }

    // scale with the same scaleX and scaleY
    private static void scaleRect(RectF rect, float scale) {
        scaleRect(rect, scale, scale);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0)
            return;

        // Draw a black background
        backUpRect.set(0, 0, w, cropRect.top);
        backLeftRect.set(0, cropRect.top, cropRect.left, cropRect.bottom);
        backRightRect.set(cropRect.right, cropRect.top, w, cropRect.bottom);
        backDownRect.set(0, cropRect.bottom, w, h);

        canvas.drawRect(backUpRect, mBackgroundPaint);
        canvas.drawRect(backLeftRect, mBackgroundPaint);
        canvas.drawRect(backRightRect, mBackgroundPaint);
        canvas.drawRect(backDownRect, mBackgroundPaint);

        //Draw four control points
        int radius = CIRCLE_WIDTH >> 1;
        leftTopCircleRect.set(cropRect.left - radius, cropRect.top - radius,
                cropRect.left + radius, cropRect.top + radius);
        rightTopCircleRect.set(cropRect.right - radius, cropRect.top - radius,
                cropRect.right + radius, cropRect.top + radius);
        leftBottomRect.set(cropRect.left - radius, cropRect.bottom - radius,
                cropRect.left + radius, cropRect.bottom + radius);
        rightBottomRect.set(cropRect.right - radius, cropRect.bottom - radius,
                cropRect.right + radius, cropRect.bottom + radius);

        canvas.drawBitmap(circleBit, circleRect, leftTopCircleRect, null);
        canvas.drawBitmap(circleBit, circleRect, rightTopCircleRect, null);
        canvas.drawBitmap(circleBit, circleRect, leftBottomRect, null);
        canvas.drawBitmap(circleBit, circleRect, rightBottomRect, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);// Whether the event flag down passing true to consume
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                int selectCircle = isSeletedControllerCircle(x, y);
                if (selectCircle > 0) {// Select Control Points
                    ret = true;
                    selectedControllerCicle = selectCircle;// Select the record number of control points
                    status = STATUS_SCALE;// Zoom into the state
                } else if (cropRect.contains(x, y)) {// Select the internal zoom box
                    ret = true;
                    status = STATUS_MOVE;// change to move state
                } else {// no choice

                }// end if
                break;
            case MotionEvent.ACTION_MOVE:
                if (status == STATUS_SCALE) {// Zoom control
                    scaleCropController(x, y);
                } else if (status == STATUS_MOVE) {// Movement control
                    translateCrop(x - oldx, y - oldy);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                status = STATUS_IDLE;// Return to the idle state
                break;
        }

        // One action point on record
        oldx = x;
        oldy = y;

        return ret;
    }

    private void translateCrop(float dx, float dy) {
        tempRect.set(cropRect);// Storing the original data，In order to restore

        translateRect(cropRect, dx, dy);
        // Check the crop rect is touch an bound of image/screen
        float mdLeft = imageRect.left - cropRect.left;
        if (mdLeft > 0) {
            translateRect(cropRect, mdLeft, 0);
        }
        float mdRight = imageRect.right - cropRect.right;
        if (mdRight < 0) {
            translateRect(cropRect, mdRight, 0);
        }
        float mdTop = imageRect.top - cropRect.top;
        if (mdTop > 0) {
            translateRect(cropRect, 0, mdTop);
        }
        float mdBottom = imageRect.bottom - cropRect.bottom;
        if (mdBottom < 0) {
            translateRect(cropRect, 0, mdBottom);
        }

        this.invalidate();
    }

    private static final void translateRect(RectF rect, float dx, float dy) {
        rect.left += dx;
        rect.right += dx;
        rect.top += dy;
        rect.bottom += dy;
    }

    private void scaleCropController(float x, float y) {
        tempRect.set(cropRect);//Storing the original data，In order to restore
        switch (selectedControllerCicle) {
            case 1:// Upper left corner of the control point
                cropRect.left = x;
                cropRect.top = y;
                break;
            case 2:
                cropRect.right = x;
                cropRect.top = y;
                break;
            case 3:
                cropRect.left = x;
                cropRect.bottom = y;
                break;
            case 4:
                cropRect.right = x;
                cropRect.bottom = y;
                break;
        }

        // ratio = -1 is free
        if (ratio < 0) {// Arbitrary scaling ratio
            // Boundary condition detection
            validateCropRect();
            invalidate();
        } else {
            // Update clipping rectangle length and width
            // Determining the invariant point
            switch (selectedControllerCicle) {
                case 1:
                case 2:
                    cropRect.bottom = (cropRect.right - cropRect.left) / this.ratio
                            + cropRect.top;
                    break;
                case 3:
                case 4:
                    cropRect.top = cropRect.bottom
                            - (cropRect.right - cropRect.left) / this.ratio;
                    break;
            }

            // validateCropRect();
            if (cropRect.left < imageRect.left
                    || cropRect.right > imageRect.right
                    || cropRect.top < imageRect.top
                    || cropRect.bottom > imageRect.bottom
                    || cropRect.width() < CIRCLE_WIDTH
                    || cropRect.height() < CIRCLE_WIDTH) {
                cropRect.set(tempRect);
            }
            invalidate();
        }
    }

    private void validateCropRect() {
        // check min of crop
        if (cropRect.width() < CIRCLE_WIDTH) {
            cropRect.left = tempRect.left;
            cropRect.right = tempRect.right;
        }
        if (cropRect.height() < CIRCLE_WIDTH) {
            cropRect.top = tempRect.top;
            cropRect.bottom = tempRect.bottom;
        }
        // check max of crop
        if (cropRect.left < imageRect.left) {
            cropRect.left = imageRect.left;
        }
        if (cropRect.right > imageRect.right) {
            cropRect.right = imageRect.right;
        }
        if (cropRect.top < imageRect.top) {
            cropRect.top = imageRect.top;
        }
        if (cropRect.bottom > imageRect.bottom) {
            cropRect.bottom = imageRect.bottom;
        }
    }

    private int isSeletedControllerCircle(float x, float y) {
        if (leftTopCircleRect.contains(x, y))// Select the upper left control point
            return 1;
        if (rightTopCircleRect.contains(x, y))// Select the upper right control point
            return 2;
        if (leftBottomRect.contains(x, y))// Select the lower left control point
            return 3;
        if (rightBottomRect.contains(x, y))// Select the lower right control ponit
            return 4;
        return -1;
    }

    public RectF getCropRect() {
        return new RectF(this.cropRect);
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }
}
