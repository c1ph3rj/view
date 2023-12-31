package tech.c1ph3rj.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class BezierView extends View {

    private final float shadowHeight = 8f;
    public float bezierX = 0f;
    private final Paint mainPaint;
    private final Paint shadowPaint;
    private final Path mainPath;
    private final Path shadowPath;
    private final PointF[] outerArray;
    private final PointF[] innerArray;
    private PointF[] progressArray;
    private float width = 0f;
    private float height = 0f;
    private float bezierOuterWidth = 0f;
    private float bezierOuterHeight = 0f;
    private float bezierInnerWidth = 0f;
    private float bezierInnerHeight = 0f;
    private int color = 0;
    private int shadowColor = 0;
    private float progress = 0f;

    public BezierView(Context context) {
        this(context, null);
    }

    public BezierView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BezierView(Context context, AttributeSet attrs, int defStyleAttrs) {
        super(context, attrs, defStyleAttrs);
        setWillNotDraw(false);

        mainPath = new Path();
        shadowPath = new Path();
        outerArray = new PointF[11];
        innerArray = new PointF[11];
        progressArray = new PointF[11];

        for (int i = 0; i < 11; i++) {
            outerArray[i] = new PointF();
            innerArray[i] = new PointF();
            progressArray[i] = new PointF();
        }

        mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainPaint.setStrokeWidth(0f);
        mainPaint.setAntiAlias(true);
        mainPaint.setStyle(Paint.Style.FILL);

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setAntiAlias(true);
        setLayerType(LAYER_TYPE_SOFTWARE, shadowPaint);

        color = 0; // Set default color
        shadowColor = 0; // Set default shadow color

        setColor(color);
        setShadowColor(shadowColor);
    }

    public void setColor(int color) {
        this.color = color;
        mainPaint.setColor(color);
        invalidate();
    }

    public void setShadowColor(int shadowColor) {
        this.shadowColor = shadowColor;
        shadowPaint.setShadowLayer(dpToPx(4), 0f, 0f, shadowColor);
        invalidate();
    }

    public void setBezierX(float bezierX) {
        if (bezierX == this.bezierX) {
            return;
        }
        this.bezierX = bezierX;
        invalidate();
    }

    public void setProgress(float progress) {
        if (progress == this.progress) {
            return;
        }
        this.progress = progress;

        progressArray[1].x = bezierX - bezierInnerWidth / 2;
        progressArray[2].x = bezierX - bezierInnerWidth / 4;
        progressArray[3].x = bezierX - bezierInnerWidth / 4;
        progressArray[4].x = bezierX;
        progressArray[5].x = bezierX + bezierInnerWidth / 4;
        progressArray[6].x = bezierX + bezierInnerWidth / 4;
        progressArray[7].x = bezierX + bezierInnerWidth / 2;

        for (int i = 2; i <= 6; i++) {
            if (progress <= 1f) {
                progressArray[i].y = calculate(innerArray[i].y, outerArray[i].y);
            } else {
                progressArray[i].y = calculate(outerArray[i].y, innerArray[i].y);
            }
        }

        if (progress == 2f) {
            progress = 0f;
        }

        invalidate();
    }

    private float calculate(float start, float end) {
        float p = progress;
        if (p > 1f) {
            p = progress - 1f;
        }
        if (p >= 0.9f && p <= 1f) {
            calculateInner();
        }
        return (p * (end - start)) + start;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        bezierOuterWidth = dpToPx(72);
        bezierOuterHeight = dpToPx(6);
        bezierInnerWidth = dpToPx(130);
        bezierInnerHeight = dpToPx(8);
        float extra = dpToPx(shadowHeight);
        outerArray[0] = new PointF(0f, bezierOuterHeight + extra);
        outerArray[1] = new PointF((bezierX - bezierOuterWidth / 2), bezierOuterHeight + extra);
        outerArray[2] = new PointF(bezierX - bezierOuterWidth / 4, bezierOuterHeight + extra);
        outerArray[3] = new PointF(bezierX - bezierOuterWidth / 4, extra);
        outerArray[4] = new PointF(bezierX, extra);
        outerArray[5] = new PointF(bezierX + bezierOuterWidth / 4, extra);
        outerArray[6] = new PointF(bezierX + bezierOuterWidth / 4, bezierOuterHeight + extra);
        outerArray[7] = new PointF(bezierX + bezierOuterWidth / 2, bezierOuterHeight + extra);
        outerArray[8] = new PointF(width, bezierOuterHeight + extra);
        outerArray[9] = new PointF(width, height);
        outerArray[10] = new PointF(0f, height);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        mainPath.reset();
        shadowPath.reset();

        if (progress == 0f) {
            drawInner(canvas, true);
            drawInner(canvas, false);
        } else {
            drawProgress(canvas, true);
            drawProgress(canvas, false);
        }
    }

    private void drawInner(Canvas canvas, boolean isShadow) {
        Paint paint = isShadow ? shadowPaint : mainPaint;
        Path path = isShadow ? shadowPath : mainPath;

        calculateInner();

        path.lineTo(innerArray[0].x, innerArray[0].y);
        path.lineTo(innerArray[1].x, innerArray[1].y);
        path.cubicTo(
                innerArray[2].x,
                innerArray[2].y,
                innerArray[3].x,
                innerArray[3].y,
                innerArray[4].x,
                innerArray[4].y
        );
        path.cubicTo(
                innerArray[5].x,
                innerArray[5].y,
                innerArray[6].x,
                innerArray[6].y,
                innerArray[7].x,
                innerArray[7].y
        );
        path.lineTo(innerArray[8].x, innerArray[8].y);
        path.lineTo(innerArray[9].x, innerArray[9].y);
        path.lineTo(innerArray[10].x, innerArray[10].y);

        progressArray = innerArray.clone();

        canvas.drawPath(path, paint);
    }

    private void calculateInner() {
        float extra = dpToPx(shadowHeight + 5);
        float curveRadius = dpToPx(18);
        innerArray[0] = new PointF(0f, bezierInnerHeight + extra);
        innerArray[1] = new PointF((bezierX - bezierInnerWidth / 2), bezierInnerHeight + extra);
        innerArray[2] = new PointF(bezierX - bezierInnerWidth / 4, bezierInnerHeight + extra);
        innerArray[3] = new PointF(bezierX - bezierInnerWidth / 6, height - extra - curveRadius); // Adjusted control point
        innerArray[4] = new PointF(bezierX, height - extra - curveRadius);
        innerArray[5] = new PointF(bezierX + bezierInnerWidth / 6, height - extra - curveRadius); // Adjusted control point
        innerArray[6] = new PointF(bezierX + bezierInnerWidth / 4, bezierInnerHeight + extra);
        innerArray[7] = new PointF(bezierX + bezierInnerWidth / 2, bezierInnerHeight + extra);
        innerArray[8] = new PointF(width, bezierInnerHeight + extra);
        innerArray[9] = new PointF(width, height);
        innerArray[10] = new PointF(0f, height);
    }


    private void drawProgress(Canvas canvas, boolean isShadow) {
        Paint paint = isShadow ? shadowPaint : mainPaint;
        Path path = isShadow ? shadowPath : mainPath;

        path.lineTo(progressArray[0].x, progressArray[0].y);
        path.lineTo(progressArray[1].x, progressArray[1].y);
        path.cubicTo(
                progressArray[2].x,
                progressArray[2].y,
                progressArray[3].x,
                progressArray[3].y,
                progressArray[4].x,
                progressArray[4].y
        );
        path.cubicTo(
                progressArray[5].x,
                progressArray[5].y,
                progressArray[6].x,
                progressArray[6].y,
                progressArray[7].x,
                progressArray[7].y
        );
        path.lineTo(progressArray[8].x, progressArray[8].y);
        path.lineTo(progressArray[9].x, progressArray[9].y);
        path.lineTo(progressArray[10].x, progressArray[10].y);

        canvas.drawPath(path, paint);
    }

    private float dpToPx(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return dp * density;
    }
}

