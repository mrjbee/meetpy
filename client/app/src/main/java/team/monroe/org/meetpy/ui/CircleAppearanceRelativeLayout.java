package team.monroe.org.meetpy.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class CircleAppearanceRelativeLayout extends RelativeLayout {

    private Bitmap originImageBitmap;
    private RectF componentSize;

    private Paint maskDefinePaint;
    private Paint maskApplyPaint;
    private Paint backgroundPaint;

    private float fraction = 1f;
    private PointF center = new PointF(100,100);

    public CircleAppearanceRelativeLayout(Context context) {
        super(context);
        init();
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);

        maskDefinePaint = new Paint();
        maskDefinePaint.setColor(Color.BLACK);

        maskApplyPaint = new Paint();
        maskApplyPaint.setAntiAlias(true);
        maskApplyPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.BLACK);
        backgroundPaint.setStyle(Paint.Style.FILL);
    }

    public CircleAppearanceRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleAppearanceRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleAppearanceRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    public void draw(Canvas canvas) {

        if (fraction == 0f){
            return;
        }

        if (fraction == 1f){
            componentSize = null;
            super.draw(canvas);
            return;
        }

        if (componentSize == null ||
                componentSize.width() != getWidth() ||
                componentSize.height() != getHeight()){
            componentSize = new RectF(0,0,getWidth(),getHeight());
            originImageBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_4444);
            super.draw(new Canvas(originImageBitmap));
        }
        backgroundPaint.setAlpha((int) (150*fraction));
        canvas.drawRect(componentSize,backgroundPaint);
        canvas.drawCircle(center.x, center.y, calculateRadius(componentSize), maskDefinePaint);
        canvas.drawBitmap(originImageBitmap, 0, 0, maskApplyPaint);
    }

    private float calculateRadius(RectF componentSize) {
        return fraction * Math.max(componentSize.width(), componentSize.height());
    }

    public float getFraction() {
        return fraction;
    }

    public void setFraction(float fraction) {
        this.fraction = fraction;
    }

    public PointF getCenter() {
        return center;
    }

    public void setCenter(PointF center) {
        this.center = center;
    }

    public void setCenter(float x, float y) {
        this.center.set(x,y);
    }
}
