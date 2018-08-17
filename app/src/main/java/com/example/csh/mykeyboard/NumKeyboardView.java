package com.example.csh.mykeyboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * Desc: 开台等用到的数字键盘
 *
 * @author：Jing Yang
 * @date: 2018/6/15 10:57
 */
public class NumKeyboardView extends View implements View.OnTouchListener {

    private final int KEY_TEXT = 0;

    private final int KEY_BACK = 1;

    private final int KEY_ENTER = 2;

    private final int KEY_CLEAR = 3;

    private Paint paint;

    private int keyWidth = ScreenUtils.dp2px(50);

    private int keyHeight = ScreenUtils.dp2px(40);

    private int keySpace = ScreenUtils.dp2px(5);//行间距
    private int keySpaceRow = ScreenUtils.dp2px(5);//行间距
    private int keySpaceCol = ScreenUtils.dp2px(10);//列间距

    private int keyFrame = ScreenUtils.dp2px(1);//键盘边框尺寸
    private int keyFrameColor = Color.parseColor("#b5b5b5");//键盘边框颜色
    private int keyBgColor = Color.parseColor("#ffffff");//键盘内部颜色

    private int keyTextSize = ScreenUtils.dp2px(15);
    private int keyBackTextSize = ScreenUtils.dp2px(14);//退格键文字大小
    private int keyTextColor = Color.parseColor("#333333");

    private int keyRadius = ScreenUtils.dp2px(2);

    /**
     * UI后期去掉回车键
     */
    private boolean disableEnter = false;

    private Key[][] keys = new Key[][]{
            {new Key(KEY_TEXT, "1"), new Key(KEY_TEXT, "2"), new Key(KEY_TEXT, "3")},
            {new Key(KEY_TEXT, "4"), new Key(KEY_TEXT, "5"), new Key(KEY_TEXT, "6")},
            {new Key(KEY_TEXT, "7"), new Key(KEY_TEXT, "8"), new Key(KEY_TEXT, "9")},
            {new Key(KEY_BACK, "退格"), new Key(KEY_TEXT, "0"), new Key(KEY_CLEAR, "C")},
    };
    private Key enterKey = new Key(KEY_ENTER, "");

    /**
     * 可切换的输入框
     */
    private NumKeyDisplayView[] mDisplayViews;
    /**
     * 当前接收输入字符的输入框
     */
    private NumKeyDisplayView mFocusDisplayView;

    /**
     * 回车事件
     */
    private OnActionDoneListener onActionDoneListener;

    public NumKeyboardView(Context context) {
        super(context);
        init(null);
    }

    public NumKeyboardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setOnTouchListener(this);
        paint = new Paint();

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.NumKeyboardView);
        keyWidth = a.getDimensionPixelSize(R.styleable.NumKeyboardView_keyWidth,
                ScreenUtils.dp2px(50));
        keyHeight = a.getDimensionPixelSize(R.styleable.NumKeyboardView_keyHeight,
                ScreenUtils.dp2px(40));
        keySpaceRow = a.getDimensionPixelSize(R.styleable.NumKeyboardView_keySpace,
                ScreenUtils.dp2px(5));
        keySpaceCol = a.getDimensionPixelSize(R.styleable.NumKeyboardView_keySpace,
                ScreenUtils.dp2px(10));
        keyTextSize = a.getDimensionPixelSize(R.styleable.NumKeyboardView_keyTextSize,
                ScreenUtils.dp2px(14));
        keyRadius = a.getDimensionPixelSize(R.styleable.NumKeyboardView_keyRadius,
                ScreenUtils.dp2px(2));
        a.recycle();
    }

    public void setFocusDisplayView(NumKeyDisplayView mDisplayView) {
        this.mFocusDisplayView = mDisplayView;
        if (mFocusDisplayView != null) {
            mFocusDisplayView.setSelected(true);
        }
    }

    public void setDisplayViews(NumKeyDisplayView[] mDisplayViews) {
        this.mDisplayViews = mDisplayViews;
        if (mDisplayViews != null) {
            for (int i = 0; i < mDisplayViews.length; i++) {
                mDisplayViews[i].setOnClickListener(displayListener);
                mDisplayViews[i].setOnFocusChangeListener(onFocusChangeListener);
            }
        }
    }

    public void setOnActionDoneListener(OnActionDoneListener onActionDoneListener) {
        this.onActionDoneListener = onActionDoneListener;
    }

    private OnFocusChangeListener onFocusChangeListener = (v, hasFocus) -> {
        if (hasFocus) {
            ((NumKeyDisplayView) v).setCursorVisible(false);
            // 当获取焦点时选中全部，这种做法可以比较完美的实现需求
            v.postDelayed(() -> {
                ((NumKeyDisplayView) v).setSelection(0, ((NumKeyDisplayView) v).getText().length());
                ((NumKeyDisplayView) v).setCursorVisible(true);
            }, 100);
        }
    };

    private OnClickListener displayListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mFocusDisplayView = (NumKeyDisplayView) v;
            for (int i = 0; i < mDisplayViews.length; i++) {
                if (mDisplayViews[i] != mFocusDisplayView) {
                    mDisplayViews[i].setSelected(false);
                }
            }
            v.setSelected(true);
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawKeys(canvas);
        if (!disableEnter) {
            drawEnter(canvas);
        }
    }

    private void drawKeys(Canvas canvas) {
        for (int i = 0; i < keys.length; i++) {
            Key[] rowKeys = keys[i];
            for (int j = 0; j < rowKeys.length; j++) {
                Key key = rowKeys[j];
                if (key.keyType == KEY_BACK) {
                    drawBack(i, j, key, canvas);
                } else {
                    drawKey(i, j, key, canvas);
                }
            }
        }
    }

    /**
     * 普通数字键
     *
     * @param row 行
     * @param col 列
     * @param key
     * @param canvas
     */
    private void drawKey(int row, int col, Key key, Canvas canvas) {
        RectF rect = new RectF();
        drawKeyBackground(rect, row, col, canvas);

        paint.setColor(keyTextColor);
        paint.setTextSize(keyTextSize);
        float textWidth = paint.measureText(key.text);
        float x = rect.left + (keyWidth - textWidth) / 2;
        float y = rect.top + keyHeight / 3 * 2;
        // y是基准线，不是顶部
        canvas.drawText(key.text, x, y, paint);
    }

    /**
     * 退格键
     *
     * @param row
     * @param col
     * @param key
     * @param canvas
     */
    private void drawBack(int row, int col, Key key, Canvas canvas) {
        RectF rect = new RectF();
        drawKeyBackground(rect, row, col, canvas);

        paint.setColor(keyTextColor);
        paint.setTextSize(keyBackTextSize);
        float textWidth = paint.measureText(key.text);
        float x = rect.left + (keyWidth - textWidth) / 2;
        float y = rect.top + keyHeight / 3 * 2;
        canvas.drawText(key.text, x, y, paint);

//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_key_back);
//        Matrix matrix = new Matrix();
//        matrix.setTranslate(rect.left + (keyWidth - bitmap.getWidth()) / 2, rect.top + keyHeight / 3 * 2);
//        canvas.drawBitmap(bitmap, matrix, paint);
    }

    private void drawKeyBackground(RectF rect, int row, int col, Canvas canvas) {
        rect.left = col * keyWidth + col * keySpaceCol;
        rect.top = row * keyHeight + row * keySpaceRow;
        rect.right = rect.left + keyWidth;
        rect.bottom = rect.top + keyHeight;
        paint.setColor(keyFrameColor);
        canvas.drawRoundRect(rect, keyRadius, keyRadius, paint);

        RectF rectFram = new RectF();
        rectFram.left = rect.left+keyFrame;
        rectFram.top = rect.top+keyFrame;
        rectFram.right = rect.right-keyFrame;
        rectFram.bottom = rect.bottom-keyFrame;
        paint.setColor(keyBgColor);
        canvas.drawRoundRect(rectFram, keyRadius, keyRadius, paint);
    }

    /**
     * 回车键
     *
     * @param canvas
     */
    private void drawEnter(Canvas canvas) {
        Rect rect = new Rect();
        rect.left = 3 * keyWidth + 3 * keySpaceCol;
        rect.top = 0;
        rect.right = rect.left + keyWidth;
        rect.bottom = keyHeight * 4 + 3 * keySpaceRow;
        paint.setColor(keyFrameColor);
        canvas.drawRect(rect, paint);

        RectF rectFram = new RectF();
        rectFram.left = rect.left+keyFrame;
        rectFram.top = rect.top+keyFrame;
        rectFram.right = rect.right-keyFrame;
        rectFram.bottom = rect.bottom-keyFrame;
        paint.setColor(keyBgColor);
        canvas.drawRoundRect(rectFram, keyRadius, keyRadius, paint);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_key_enter);
        Matrix matrix = new Matrix();
        matrix.setTranslate(rect.left + (keyWidth - bitmap.getWidth()) / 2, (rect.bottom - bitmap.getHeight()) / 2);
        canvas.drawBitmap(bitmap, matrix, paint);
    }

    private float lastX;
    private float lastY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float offsetX = event.getX() - lastX;
                float offsetY = event.getY() - lastY;
                if (offsetX >= -50 && offsetX <= 50
                        && offsetY >= -50 && offsetY <= 50) {
                    performClickKey(lastX, lastY);
                }
                break;
        }
        return true;
    }

    private void performClickKey(float x, float y) {
        Key key = findKey(x, y);
        if (key != null) {
            if (mFocusDisplayView != null) {
                switch (key.keyType) {
                    case KEY_BACK:
                        mFocusDisplayView.backDelete();
                        break;
                    case KEY_CLEAR:
                        mFocusDisplayView.clearText();
                        break;
                    case KEY_ENTER:
                        if (!disableEnter) {
                            onEnter();
                        }
                        break;
                    default:
                        mFocusDisplayView.sendText(key.text);
                        break;
                }
            }
        }
    }

    private void onEnter() {
        if (mDisplayViews == null) {
            if (onActionDoneListener != null) {
                onActionDoneListener.onDone();
            }
        } else {
            for (int i = 0; i < mDisplayViews.length; i++) {
                if (mDisplayViews[i].isSelected()) {
                    if (i == mDisplayViews.length - 1) {
                        if (onActionDoneListener != null) {
                            onActionDoneListener.onDone();
                        }
                    } else {
                        mFocusDisplayView.setSelected(false);
                        mFocusDisplayView = mDisplayViews[i + 1];
                        mFocusDisplayView.setSelected(true);
                    }
                    break;
                }
            }
        }
    }

    private Key findKey(float x, float y) {
        int row = (int) (y / (keyHeight + keySpaceRow));
        int col = (int) (x / (keyWidth + keySpaceCol));
        if (row < 4) {
            if (col < 3) {
                return keys[row][col];
            } else {
                return enterKey;
            }
        }
        return null;
    }

    private class Key {
        int keyType;
        String text;

        public Key(int keyType, String key) {
            this.keyType = keyType;
            this.text = key;
        }
    }

    public interface OnActionDoneListener {
        void onDone();
    }
}
