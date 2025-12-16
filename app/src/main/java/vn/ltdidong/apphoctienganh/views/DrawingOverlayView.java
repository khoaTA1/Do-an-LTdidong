package vn.ltdidong.apphoctienganh.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawingOverlayView extends View {

    private Paint paint;
    private Path path;
    private OnDrawListener listener;

    public interface OnDrawListener {
        void onFinishDrawing(RectF bounds);
    }

    public DrawingOverlayView(Context context) {
        super(context);
        init();
    }

    public DrawingOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f);
        paint.setAntiAlias(true);
        path = new Path();
    }

    public void setOnDrawListener(OnDrawListener listener) {
        this.listener = listener;
    }

    public void clear() {
        path.reset();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.reset();
                path.moveTo(x, y);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                if (listener != null) {
                    RectF bounds = new RectF();
                    path.computeBounds(bounds, true);
                    // Check if bounds are too small (accidental tap)
                    if (bounds.width() > 20 && bounds.height() > 20) {
                        listener.onFinishDrawing(bounds);
                    } else {
                        clear(); // Clear accidental taps
                    }
                }
                return true;
        }
        return super.onTouchEvent(event);
    }
}
