package me.buddhabhu.surfaceviewexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {

    private Context mContext;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;
    private Path mPath;

    private Bitmap mBitmap;

    private int mBitmapX;
    private int mBitmapY;

    private RectF mWinnerRect;

    private int mViewWidth;
    private int mViewHeight;

    private boolean mRunning;

    private Thread mGameThread;

    private FlashLightCone mFlashLightCone;

    private void init(Context context){
        this.mContext = context;
        mSurfaceHolder = getHolder();
        mPaint = new Paint();
        mPaint.setColor(Color.DKGRAY);
        mPath = new Path();
    }

    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void setUpBitmap() {
        mBitmapX = (int) Math.floor(Math.random() * (mViewWidth - mBitmap.getWidth()));
        mBitmapY = (int) Math.floor(Math.random() * (mViewHeight - mBitmap.getHeight()));

        mWinnerRect = new RectF(mBitmapX, mBitmapY, mBitmapX + mBitmap.getWidth(),
                mBitmapY + mBitmap.getHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mViewWidth = w;
        mViewHeight = h;

        mFlashLightCone = new FlashLightCone(mViewWidth, mViewHeight);

        mPaint.setTextSize(mViewHeight / 5);

        mBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.android);
        setUpBitmap();
    }

    @Override
    public void run() {
        Canvas canvas;

        while(mRunning) {
            if(mSurfaceHolder.getSurface().isValid()) {
                int x = mFlashLightCone.getX();
                int y = mFlashLightCone.getY();
                int radius = mFlashLightCone.getRadius();

                canvas = mSurfaceHolder.lockCanvas();
                canvas.save();

                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(mBitmap, mBitmapX, mBitmapY, mPaint);

                mPath.addCircle(x, y, radius, Path.Direction.CCW);

                // Set the circle as the clipping path using the DIFFERENCE operator,
                // so that's what's inside the circle is clipped (not drawn).
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    canvas.clipPath(mPath, Region.Op.DIFFERENCE);
                } else {
                    canvas.clipOutPath(mPath);
                }

                // Fill everything outside of the circle with black.
                canvas.drawColor(Color.BLACK);

                if (x > mWinnerRect.left && x < mWinnerRect.right
                        && y > mWinnerRect.top && y < mWinnerRect.bottom) {
                    canvas.drawColor(Color.WHITE);
                    canvas.drawBitmap(mBitmap, mBitmapX, mBitmapY, mPaint);
                    canvas.drawText(
                            "WIN!", mViewWidth / 3, mViewHeight / 2, mPaint);
                }

                // Drawing is finished, so you need to rewind the path, restore the canvas,
                // and release the lock on the canvas.
                mPath.rewind();
                canvas.restore();
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void pause() {
        mRunning = false;
        try {
            // Stop the thread (rejoin the main thread)
            mGameThread.join();
        } catch (InterruptedException e) {

        }
    }

    public void resume() {
        mRunning = true;
        mGameThread = new Thread(this);
        mGameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        // Invalidate() is inside the case statements because there are
        // many other motion events, and we don't want to invalidate
        // the view for those.
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setUpBitmap();
                updateFrame((int) x, (int) y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                updateFrame((int) x, (int) y);
                invalidate();
                break;
            default:

        }
        return true;
    }

    private void updateFrame(int newX, int newY) {
        mFlashLightCone.update(newX, newY);
    }

}
