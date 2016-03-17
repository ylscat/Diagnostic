package com.fangstar.diagnostic.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EdgeEffect;
import android.widget.Scroller;

/**
 * Created at 2016/3/15.
 *
 * @author YinLanShan
 */
public class PageScrollView extends ViewGroup {
    private static final boolean DEBUG = true;
    private static final String TAG = PageScrollView.class.getSimpleName();

    private float mOffset;
    private int mWidth;

    private float mTouchSlop;
    private float mMinimumVelocity, mMaximumVelocity, mMinFlingDistance;
    private VelocityTracker mVelocityTracker;
    private int mActivePointerId = INVALID_POINTER;
    private float mLastMotionX;
    private float mInitialMotionX;


    private static final int INVALID_POINTER = -1;

    private Scroller mScroller;
    private EdgeEffect mLeftEdge, mRightEdge;

    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SETTLING = 2;

    private int mScrollState = SCROLL_STATE_IDLE;

    private static final int MIN_DISTANCE_FOR_FLING = 25;

    public PageScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PageScrollView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setFocusable(true);
        mScroller = new Scroller(getContext());
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        final float density = context.getResources().getDisplayMetrics().density;

        mTouchSlop = configuration.getScaledPagingTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mLeftEdge = new EdgeEffect(context);
        mRightEdge = new EdgeEffect(context);

        mMinFlingDistance = density*MIN_DISTANCE_FOR_FLING;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int specW = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int specH = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        for(int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            child.measure(specW, specH);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(!changed)
            return;
        int width = r - l;
        int height = b - t;
        mWidth = width;
        for(int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            int x = i*width;
            child.layout(x, 0, x + width, height);
        }
    }

    private void setScrollState(int newState) {
        if (mScrollState == newState) {
            return;
        }

        mScrollState = newState;
        if(newState == SCROLL_STATE_IDLE) {
            setScrollingCacheEnabled(false);
        }
        else {
            setScrollingCacheEnabled(true);
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        int offset = (int)(mWidth*mOffset);
        if(child.getLeft() > offset + mWidth || child.getRight() < offset)
            return false;
        return super.drawChild(canvas, child, drawingTime);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        boolean needsInvalidate = false;
        int x = getScrollX();
        if (getChildCount() > 1) {
            if (!mLeftEdge.isFinished()) {
                final int restoreCount = canvas.save();
                final int height = getHeight() - getPaddingTop() - getPaddingBottom();
                final int width = mWidth;

                canvas.rotate(270);
                canvas.translate(-height + getPaddingTop(), x);
                mLeftEdge.setSize(height, width);
                needsInvalidate = mLeftEdge.draw(canvas);
                canvas.restoreToCount(restoreCount);
            }
            if (!mRightEdge.isFinished()) {
                final int restoreCount = canvas.save();
                final int width = getWidth();
                final int height = getHeight() - getPaddingTop() - getPaddingBottom();

                canvas.rotate(90);
                canvas.translate(-getPaddingTop(), -x - width);
                mRightEdge.setSize(height, width);
                needsInvalidate |= mRightEdge.draw(canvas);
                canvas.restoreToCount(restoreCount);
            }
        } else {
            mLeftEdge.finish();
            mRightEdge.finish();
        }

        if(needsInvalidate)
            postInvalidate();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if(mScrollState == SCROLL_STATE_DRAGGING)
                    return true;
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }

                final int pointerIndex = ev.findPointerIndex(activePointerId);
                final float x = ev.getX(pointerIndex);
                final float dx = x - mInitialMotionX;
                final float xDiff = Math.abs(dx);

                if (xDiff > mTouchSlop) {
                    if (DEBUG) Log.v(TAG, "I Move, Starting drag!");

                    requestParentDisallowInterceptTouchEvent(true);
                    setScrollState(SCROLL_STATE_DRAGGING);
                    mLastMotionX = dx > 0 ? mInitialMotionX + mTouchSlop :
                            mInitialMotionX - mTouchSlop;
                    int delta = (int)(x - mLastMotionX);
                    performDrag(delta);
                    mLastMotionX += delta;
                } else {
                    mLastMotionX = x;
                }

                break;
            }

            case MotionEvent.ACTION_DOWN: {
                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mLastMotionX = mInitialMotionX = ev.getX();
                mActivePointerId = ev.getPointerId(0);

                mScroller.computeScrollOffset();
                if (mScrollState == SCROLL_STATE_SETTLING) {
                    // Let the user 'catch' the pager as it animates.
                    mScroller.abortAnimation();
                    requestParentDisallowInterceptTouchEvent(true);
                    setScrollState(SCROLL_STATE_DRAGGING);
                }

                if (DEBUG) Log.v(TAG, "I Down at " + mInitialMotionX + "state="+mScrollState);
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                if (DEBUG) Log.v(TAG, "I UP");
                return false;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return mScrollState == SCROLL_STATE_DRAGGING;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mScroller.abortAnimation();

                // Remember where the motion event started
                mLastMotionX = mInitialMotionX = ev.getX();
                mActivePointerId = ev.getPointerId(0);
                if(DEBUG) Log.v(TAG, "T Down");
                break;

            case MotionEvent.ACTION_MOVE:
                int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if(mScrollState == SCROLL_STATE_DRAGGING) {

                    final float x = ev.getX(activePointerIndex);
                    int dx = (int)(x - mLastMotionX);
                    performDrag(dx);
                    mLastMotionX += dx;
                    if(DEBUG) Log.v(TAG, "T Move mx=" + mLastMotionX);
                }
                else {
                    final float x = ev.getX(activePointerIndex);
                    final float dx = x - mInitialMotionX;
                    final float xDiff = Math.abs(dx);

                    if (xDiff > mTouchSlop) {
                        if (DEBUG) Log.v(TAG, "T Move, Starting drag!");

                        requestParentDisallowInterceptTouchEvent(true);
                        setScrollState(SCROLL_STATE_DRAGGING);
                        mLastMotionX = dx > 0 ? mInitialMotionX + mTouchSlop :
                                mInitialMotionX - mTouchSlop;
                        int delta = (int)(x - mLastMotionX);
                        performDrag(delta);
                        mLastMotionX += delta;
                    } else {
                        mLastMotionX = x;
                        if(DEBUG) Log.v(TAG, "T Move IDLE");
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getXVelocity(mActivePointerId);
                    activePointerIndex = ev.findPointerIndex(mActivePointerId);
                    final float x = ev.getX(activePointerIndex);
                    final int totalDelta = (int) (x - mInitialMotionX);
                    int nextPage = determineTargetPage(initialVelocity, totalDelta);
                    scrollToPage(nextPage);

                    mActivePointerId = INVALID_POINTER;
                    if(mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                }
                if(DEBUG) Log.v(TAG, "T UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    scrollToPage(Math.round(mOffset));
                    mActivePointerId = INVALID_POINTER;
                    if(mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                }
                if(DEBUG) Log.v(TAG, "T CANCEL");
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mInitialMotionX = mLastMotionX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        final ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private void setScrollingCacheEnabled(boolean enable) {
        for(int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            child.setDrawingCacheEnabled(enable);
        }
    }

    private void performDrag(int delta) {
        int sx = getScrollX();
        int max = Math.max(0, (getChildCount() - 1)*mWidth);
        int x = sx - delta;
        if(x < 0) {
            mLeftEdge.onPull((float)-x/mWidth);
            if(DEBUG)
                Log.v(TAG, "left edge " + (-x));
            x = 0;
            invalidate();
        }
        else if(x > max) {
            mRightEdge.onPull((float)(x - max)/mWidth);
            if(DEBUG)
                Log.v(TAG, "right edge " + (x - max));
            x = max;
            invalidate();
        }

        if(x != sx) {
            scrollTo(x, 0);
        }

        mOffset = (float)x/mWidth;
    }

    private int determineTargetPage(int velocity, int deltaX) {
        int targetPage;
        int currentPage = (int)mOffset;
        if (Math.abs(deltaX) > mMinFlingDistance && Math.abs(velocity) > mMinimumVelocity) {
            if(velocity > 0)
                targetPage = currentPage;
            else {
                int max = Math.max(0, getChildCount() - 1);
                targetPage = Math.min(currentPage + 1, max);
            }
        } else {
            targetPage = Math.round(mOffset);
        }
        if(DEBUG) {
            Log.v(TAG, String.format("v:%d dx:%d, current=%d, next=%d", velocity, deltaX,
                    currentPage, targetPage));
        }

        return targetPage;
    }

    public void scrollToPage(int page) {
        final int finalX = page*mWidth;
        final int startX = getScrollX();
        int duration = computeDuration((double)Math.abs(finalX - startX)/mWidth);
        if(DEBUG) Log.v(TAG, String.format("scrollToPage sx=%d, fx=%d du=%d",
                startX, finalX, duration));
        setScrollState(SCROLL_STATE_SETTLING);
        mScroller.startScroll(startX, 0, finalX - startX, 0, duration);
        postInvalidate();
    }

    private int computeDuration(double distance) {
        return (int)(Math.sqrt(distance)*1000);
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            int x = mScroller.getCurrX();
            int oldX = getScrollX();

            if (oldX != x) {
                scrollTo(x, 0);
                if(mWidth != 0)
                    mOffset = (float)x/mWidth;
            }
            else
                postInvalidate();

            return;
        }

        if(mScrollState == SCROLL_STATE_SETTLING)
            setScrollState(SCROLL_STATE_IDLE);
    }
}
