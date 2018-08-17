package com.example.csh.mykeyboard;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Desc:用于显示NumKeyboardView输入字符的view
 *
 * @author：Jing Yang
 * @date: 2018/6/15 14:10
 */
public class NumKeyDisplayView extends AppCompatEditText implements View.OnTouchListener {

    private StringBuffer mTextBuffer;

    public NumKeyDisplayView(Context context) {
        super(context);
        init();
    }

    public NumKeyDisplayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mTextBuffer = new StringBuffer();
        setOnTouchListener(this);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        mTextBuffer = new StringBuffer(text);
    }

    /**
     * 接收输入字符
     * @param text
     */
    public void sendText(String text) {

        if (".".equals(text) && mTextBuffer.toString().contains(".")) {
            return;
        }
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (start != end) {
            // 选中部分文字，先删除这部分文字
            mTextBuffer.delete(start, end);
        }
        if(mTextBuffer.length()>=2){
            return;
        }
        mTextBuffer.insert(start, text);
        setText(mTextBuffer.toString());
        // 移动游标
        setSelection(start + 1);
    }

    /**
     * 回删
     */
    public void backDelete() {
        String str = mTextBuffer.toString();
        if (str.length() > 0) {
            int start = getSelectionStart();
            int end = getSelectionEnd();
            if (start == end) {
                // 游标已处于最左侧
                if (start == 0) {
                    return;
                }
                mTextBuffer.deleteCharAt(start - 1);
                setText(mTextBuffer.toString());
                // 移动游标
                setSelection(start - 1);
            }
            else {
                mTextBuffer.delete(start, end);
                setText(mTextBuffer.toString());
                // 移动游标
                setSelection(mTextBuffer.toString().length());
            }
        }
    }

    /**
     * 清空
     */
    public void clearText() {
        mTextBuffer = new StringBuffer();
        setText("");
    }

    private long lastTouch;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 这里是为了能让EditText处于select状态，并能触发onClick事件
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (System.currentTimeMillis() - lastTouch > 50) {
                    v.performClick();
                    v.requestFocus();
                }
                lastTouch = System.currentTimeMillis();
                break;
        }
        // 这里是为了不弹出系统键盘
        EditText edit = (EditText) v;
        int type = edit.getInputType();
        if(Build.VERSION.SDK_INT < 14) {
            edit.setInputType(0);
        }
        hideSoftInputMethod(edit);
        if(Build.VERSION.SDK_INT < 14) {
            edit.setInputType(type);
        }
        // return false的目的在于使游标依然有效
        return false;
    }

    private void hideSoftInputMethod(EditText edit) {
        getActivity().getWindow().setSoftInputMode(3);
        int currentVersion = Build.VERSION.SDK_INT;
        String methodName = null;
        if(currentVersion >= 16) {
            methodName = "setShowSoftInputOnFocus";
        } else if(currentVersion >= 14) {
            methodName = "setSoftInputShownOnFocus";
        }

        if(methodName == null) {
            edit.setInputType(0);
        } else {
            Class cls = EditText.class;

            try {
                Method setShowSoftInputOnFocus = cls.getMethod(methodName, new Class[]{Boolean.TYPE});
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(edit, new Object[]{Boolean.valueOf(false)});
            } catch (NoSuchMethodException var8) {
                edit.setInputType(0);
                var8.printStackTrace();
            } catch (IllegalAccessException var9) {
                var9.printStackTrace();
            } catch (IllegalArgumentException var10) {
                var10.printStackTrace();
            } catch (InvocationTargetException var11) {
                var11.printStackTrace();
            }
        }

    }

    /**
     * Android 5.0以下，TintContextWrapper不能强转为Activity，需要向上寻找
     * @return
     */
    private Activity getActivity() {
        // Gross way of unwrapping the Activity so we can get the FragmentManager
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        throw new IllegalStateException("The NumKeyDisplayView's Context is not an Activity.");
    }
}
