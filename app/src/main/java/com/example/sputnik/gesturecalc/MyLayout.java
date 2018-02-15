package com.example.sputnik.gesturecalc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Sputnik on 2/1/2018.
 */
public class MyLayout extends android.support.v7.widget.GridLayout {
    private final float TOUCH_SLOP = 16f;
    private final float STROKE_WIDTH = 18f;

    private List<ButtonListener> buttonListeners = new ArrayList<ButtonListener>();
    private ButtonTextView clearButton;
    private PathAnimator animator;
    private PathActivator activator;
    private Rect offsetViewBounds = new Rect();
    float prevX, prevY;
    private float PX_PER_COL;
    private float PX_PER_ROW;
    private int[] buttonBelowIndex = new int[2];
    // [0] = top left x-coord, [1] = top left y-coord,
    // [2] = top right x-coord, [4] = bottom left y-coord
    private int[] clearButtonBoundary = new int[4];
    boolean prevEventDown = false;
    boolean firstClick;

    interface ButtonListener {
        void buttonPressed(String input);
    }

    public MyLayout(Context context) {
        super(context);
        setup();
    }

    public MyLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public MyLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    public void setPathAnimator(PathAnimator animator){
        this.animator = animator;
    }

    public void setPathActivator(final PathActivator activator) {
        this.activator = activator;
        activator.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                if (!firstClick){
                    firstClick = true;
                }
                clickChildButton(((PathActivator) o).getCurX(),((PathActivator) o).getCurY());
                animator.addSpecial(((PathActivator) o).getCurX(), ((PathActivator) o).getCurY());
            }
        });
    }

    void setup(){
        this.setWillNotDraw(false);
    }

    void setupSize(){
        PX_PER_COL = (float) getWidth() / (float) getColumnCount();
        PX_PER_ROW = (float) getHeight() / (float) getRowCount();

        final View clear = findViewById(R.id.clear);
        ViewTreeObserver viewTreeObserver = clear.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < 16) {
                        clear.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        clear.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    setResetButton(clear);
                }
            });
        }
    }

    int getColIndexOfLocation(float x){
        return ((int) (x) / ((int) PX_PER_COL));
    }

    int getRowIndexOfLocation(float y){
        return ((int) (y) / ((int) PX_PER_ROW));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (animator != null) {
            animator.updateCanvas(canvas);
            if (animator.isRunning()){
                invalidate();
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    public void setResetButton(View clear){
        if (!isInEditMode()) {
            this.clearButton = (ButtonTextView) clear;
            int[] layoutPos = new int[2];
            getLocationInWindow(layoutPos);
            int[] clearPos = new int[2];
            clearButton.getLocationInWindow(clearPos);
            clearButtonBoundary[0] = layoutPos[0] - clearPos[0];
            clearButtonBoundary[1] = layoutPos[1] - clearPos[1];
            clearButtonBoundary[2] = clearButtonBoundary[0] + clear.getWidth();
            clearButtonBoundary[3] = clearButtonBoundary[1] + clear.getHeight();
        }
    }

    private void resetOnDownAction(float x, float y){
        prevX = x;
        prevY = y;
        buttonBelowIndex[0] = getColIndexOfLocation(x);
        buttonBelowIndex[1] = getRowIndexOfLocation(y);
        prevEventDown = true;
    }

    private void clickChildButton(float x, float y) {
        TextView child;
        Rect offsetViewBounds = new Rect();
        int count = getChildCount();
        for (int i =0; i < count; i++) {
            child = (TextView) getChildAt(i);
            child.getDrawingRect(offsetViewBounds);
            offsetDescendantRectToMyCoords(child, offsetViewBounds);
            if (offsetViewBounds.contains((int) x,(int) y)){
                child.callOnClick();
                for (ButtonListener listener :
                        buttonListeners) {
                    listener.buttonPressed(String.valueOf(child.getText()));
                }
                return;
            }
            offsetViewBounds.setEmpty();
        }
    }

    void registerButtonListener(ButtonListener listener){
        buttonListeners.add(listener);
    }

    void unregisterButtonListener(ButtonListener listener){
        buttonListeners.remove(listener);
    }

    private void processMoveAction(float x, float y) {
        // Ignore event if it's too close to previous
        if (PathActivator.euclidDistance(x, y, prevX, prevY) <= TOUCH_SLOP) {
            return;
        }
        // If the event is over the clear button
        if (getChildAt(x - STROKE_WIDTH / 2, y - STROKE_WIDTH / 2).equals(clearButton)) {
            return;
        }
        // If the event is over a new button
        if (buttonBelowIndex[0] != getColIndexOfLocation(x) || buttonBelowIndex[1] != getRowIndexOfLocation(y)) {
            resetOnButtonChange(x, y);
        }
        if (prevEventDown) {
            clickChildButton(x, y);
            firstClick = true;
            prevEventDown = false;
        }
        animator.update(x, y, false);
        activator.update(x, y);
        prevX = x;
        prevY = y;
    }

    private void resetOnButtonChange(float x , float y){
        firstClick = false;
        activator.restart();
        buttonBelowIndex[0] = getColIndexOfLocation(x);
        buttonBelowIndex[1] = getRowIndexOfLocation(y);
        prevX = x;
        prevY = y;
    }

    private ButtonTextView getChildAt(float x, float y){
        ButtonTextView button = null;
        int count = getChildCount();
        for (int i =0; i < count; i++) {
            button = (ButtonTextView) getChildAt(i);
            button.getDrawingRect(offsetViewBounds);
            offsetDescendantRectToMyCoords(button, offsetViewBounds);
            if (offsetViewBounds.contains((int) x,(int) y)){
                return button;
            }
        }
        return button;
    }

    private void notifyButtonListeners(ButtonTextView button){
        for (ButtonListener listener :
                buttonListeners) {
            listener.buttonPressed(String.valueOf(button.getText()));
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final float eventX = ev.getX();
        final float eventY = ev.getY();
        // Event is on clear button
        if (getChildAt(eventX - STROKE_WIDTH /2, eventY - STROKE_WIDTH /2).equals(clearButton)){
            // clear any history
            final int count = ev.getHistorySize();
            for (int i = 0; i < count; i++) {
                ev.getHistoricalX(i);
            }
            animator.reset();
            notifyButtonListeners(clearButton);
            this.invalidate();
            return false;
        }

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                animator.update(eventX, eventY, true);
                animator.addSpecial(eventX, eventY);
                resetOnDownAction(eventX, eventY);
                break;
            case MotionEvent.ACTION_MOVE:
                final int history = ev.getHistorySize();
                for (int i = 0; i < history; i++){
                    processMoveAction(ev.getHistoricalX(i), ev.getHistoricalY(i));
                }
                processMoveAction(eventX, eventY);
                break;
            case MotionEvent.ACTION_UP:
                if (!firstClick) {
                    animator.addSpecial(eventX, eventY);
                    clickChildButton(eventX, eventY);
                }
                activator.reset();
        }
        this.invalidate();
        return false;
    }
}