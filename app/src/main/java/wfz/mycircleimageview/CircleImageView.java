package wfz.mycircleimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by Wufuzhao on 2017/4/11.
 */

public class CircleImageView extends ImageView {
    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;
    private int mBorderColor = DEFAULT_BORDER_COLOR;
    private int mBorderWidth = DEFAULT_BORDER_WIDTH;
    private final Paint mBitmapPaint = new Paint();
    private final Paint mBorderPaint = new Paint();
    private int width, height;
    private int r;
    private int drawStyle;
    public static final int Xfermode = 0;
    public static final int BitmapShader = 1;

    public CircleImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CircleImageView, defStyleAttr, 0);
        mBorderWidth = a.getDimensionPixelSize(
                R.styleable.CircleImageView_border_width, DEFAULT_BORDER_WIDTH);
        mBorderColor = a.getColor(R.styleable.CircleImageView_border_color,
                DEFAULT_BORDER_COLOR);
        drawStyle = a.getInt(R.styleable.CircleImageView_draw_style, Xfermode);
        Log.d("CircleImageView", "drawStyle-->"+drawStyle);
        a.recycle();
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        r = Math.min(width, height) / 2 - mBorderWidth;
        Log.d(getClass().getName(), "onSizeChanged-->" + w + "x" + h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap bitmap = getBitmapFromDrawable(getDrawable());
        if (bitmap == null)
            return;

        if (drawStyle == Xfermode)
            drawCircleBitmapByXfermode(canvas, bitmap);
        else
            drawCircleBitmapByBitmapShader(canvas, bitmap);
        // 绘边框
        if (mBorderWidth != 0) {
            canvas.drawCircle(width / 2, height / 2, r + mBorderWidth / 2 - 1,
                    mBorderPaint);
        }
    }

    private void drawCircleBitmapByXfermode(Canvas canvas, Bitmap bitmap) {
        Log.d("CircleImageView", "drawCircleBitmapByXfermode");
        Paint cachePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int sv = canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);
        canvas.drawCircle(width / 2, height / 2, r, cachePaint);
        cachePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        Matrix matrix = getImageMatrix(bitmap);
        if (matrix != null)
            canvas.concat(matrix);
        canvas.drawBitmap(bitmap, 0, 0, cachePaint);
        canvas.restoreToCount(sv);
    }

    private void drawCircleBitmapByBitmapShader(Canvas canvas, Bitmap bitmap) {
        Log.d("CircleImageView", "drawCircleBitmapByBitmapShader");
        Paint paint = new Paint();
        BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Matrix matrix = getImageMatrix(bitmap);
        if (matrix != null)
            bitmapShader.setLocalMatrix(matrix);
        paint.setShader(bitmapShader);
        canvas.drawCircle(width / 2, height / 2, r, paint);
    }

    public Matrix getImageMatrix(Bitmap bitmap) {
        Matrix matrix = null;
        //ScaleType.FIT_XY时，mDrawMatrix初始化为null
        if (getScaleType() == ScaleType.FIT_XY) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            matrix = new Matrix();
            matrix.postScale((float) width / w, (float) height / h);
        } else {
            matrix = getImageMatrix();
        }
        return matrix;
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        try {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }
}
