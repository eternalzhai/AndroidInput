package com.eternal.widget.input;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eternal.alert.base.BaseAlert;
import com.eternal.alert.conventional.Alert;


public class Input extends LinearLayout {

    public Input(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.initView(context, attrs, defStyle);
    }

    public Input(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Input(Context context) {
        this(context, null, 0);
    }

    // error tip type normal error hint toast alert
    public static final int TIP_TYPE_NORMAL = 0x00000000;
    public static final int TIP_TYPE_ERROR = 0x00000001;
    public static final int TIP_TYPE_HINT = 0x00000002;
    public static final int TIP_TYPE_TOAST = 0x00000003;
    public static final int TIP_TYPE_ALERT = 0x00000004;

    public static final CharSequence mDefTipEmpty = new String("输入不能为空");
    public static final CharSequence mDefTipPattern = new String("输入格式不正确");

    private ImageView mIcon;
    private TextView mLabel;
    private EditText mEdit;
    private ImageView mClear;

    TextWatcher mTextWatcher;

    // error tip used
    private int mTipType = TIP_TYPE_NORMAL;
    private CharSequence mTipEmpty;
    private CharSequence mTipPattern;
    private String mPattern;
    private boolean mShowSoftInputOnError;
    private CharSequence mHintText;
    private ColorStateList mHintTextColor;

    private OnEmptyChanged mOnEmptyChanged;

    private void initView(Context context, AttributeSet attrs, int defStyle) {

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        int iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20.0f, metrics);
        int clearSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20.0f, metrics);
        int editLRPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.0f, metrics);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Input);

        Drawable iconDrawable = a.getDrawable(R.styleable.Input_inputIcon);
        iconSize = a.getDimensionPixelSize(R.styleable.Input_inputIconSize, iconSize);

        CharSequence labelText = a.getText(R.styleable.Input_inputLabel);
        int labelTextSize = a.getDimensionPixelSize(R.styleable.Input_inputLabelSize, 16);
        ColorStateList labelTextColor = a.getColorStateList(R.styleable.Input_inputLabelColor);

        Drawable clearDrawable = a.getDrawable(R.styleable.Input_inputClear);
        clearSize = a.getDimensionPixelSize(R.styleable.Input_inputClearSize, clearSize);

        mTipType = a.getInt(R.styleable.Input_inputTipType, TIP_TYPE_NORMAL);
        mTipEmpty = a.getText(R.styleable.Input_inputTipEmpty);
        mTipPattern = a.getText(R.styleable.Input_inputTipPattern);
        mPattern = a.getString(R.styleable.Input_inputPattern);
        mShowSoftInputOnError = a.getBoolean(R.styleable.Input_inputShowSoftInputOnError, false);

        if (TextUtils.isEmpty(mTipEmpty))
            mTipEmpty = mDefTipEmpty;
        if (TextUtils.isEmpty(mTipPattern))
            mTipPattern = mDefTipPattern;

        a.recycle();

        // icon
        mIcon = new ImageView(context);
        mIcon.setBackgroundResource(android.R.color.transparent);
        if (null == iconDrawable) {
            mIcon.setVisibility(View.GONE);
        } else {
            mIcon.setImageDrawable(iconDrawable);
        }

        // label
        mLabel = new TextView(context);
        mLabel.setTextColor(labelTextColor != null ? labelTextColor : ColorStateList.valueOf(0xFF000000));
        mLabel.setBackgroundResource(android.R.color.transparent);
        mLabel.setSingleLine(true);
        mLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, labelTextSize);
        if (TextUtils.isEmpty(labelText)) {
            mLabel.setVisibility(View.GONE);
        } else {
            mLabel.setText(labelText);
        }

        // edit
        mEdit = new EditText(context, attrs);
        mEdit.setBackgroundResource(android.R.color.transparent);
        // mEdit.setGravity(Gravity.TOP);
        mEdit.setPadding(editLRPadding, 0, editLRPadding, 0);
        mEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null != mTextWatcher)
                    mTextWatcher.onTextChanged(s, start, before, count);
                postInvalidate();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (null != mTextWatcher)
                    mTextWatcher.beforeTextChanged(s, start, count, after);
                postInvalidate();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    mClear.setVisibility((isEnabled()) ? View.INVISIBLE : View.GONE);
                    clearTip();

                    mEdit.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mEdit, 0);

                    if (null != mOnEmptyChanged)
                        mOnEmptyChanged.changed(false);
                } else {
                    mClear.setVisibility((isEnabled()) ? View.VISIBLE : View.GONE);
                    if (null != mOnEmptyChanged)
                        mOnEmptyChanged.changed(true);
                }
                if (null != mTextWatcher)
                    mTextWatcher.afterTextChanged(s);
                postInvalidate();
            }
        });

        // for error tip save the old hint text and hint color
        mHintText = mEdit.getHint();
        mHintTextColor = mEdit.getHintTextColors();

        // ic_input_clear
        mClear = new ImageView(context);
        mClear.setBackgroundResource(android.R.color.transparent);
        mClear.setVisibility(View.INVISIBLE);
        if (null != clearDrawable) {
            mClear.setImageDrawable(clearDrawable);
        }
        mClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdit.setText("");
                mEdit.setError(null);
                mEdit.requestFocus();
                postInvalidate();
            }
        });

        addView(mIcon, new LayoutParams(iconSize, iconSize));
        addView(mLabel, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(mEdit, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
        addView(mClear, new LayoutParams(clearSize, clearSize));
        postInvalidate();

    }

    public boolean isInputError() {

        String text = mEdit.getText().toString().trim();

        if (TextUtils.isEmpty(text)) {
            tip(mTipEmpty);
            return true;
        }

//        if ((!TextUtils.isEmpty(mPattern)) && (!Pattern.compile(mPattern).matcher(text).matches())) {
//            tip(mTipPattern);
//            return true;
//        }

        return false;
    }

    public void tip(CharSequence tip) {

        if (TextUtils.isEmpty(tip) || null == mEdit)
            return;

        switch (mTipType) {
            case TIP_TYPE_ERROR:
                SpannableString spannableTip = new SpannableString(tip + "　　");
                spannableTip.setSpan(new ForegroundColorSpan(Color.BLACK), 0, mTipEmpty.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                mEdit.setError(spannableTip);
                mEdit.requestFocus();
                showSoftInputOnError();
                break;
            case TIP_TYPE_HINT:
                mEdit.setText("");
                mEdit.setHint(tip);
                mEdit.setHintTextColor(ColorStateList.valueOf(0xFFFFACAC));
                mEdit.requestFocus();
                showSoftInputOnError();
                break;
            case TIP_TYPE_TOAST:
                Toast.makeText(getContext(), tip, Toast.LENGTH_SHORT).show();
                mEdit.requestFocus();
                showSoftInputOnError();
                break;
            case TIP_TYPE_ALERT:
                //if context is not a activity?
                if (getContext() instanceof Activity)
                    Alert.create(getContext(), Alert.ERROR_TYPE).title(tip).rightBtnClick(new BaseAlert.OnClick<Alert>() {
                        @Override
                        public void onClick(Alert alert, int i) {
                            alert.dismiss();
                            postDelayed(new Runnable() {
                                public void run() {
                                    mEdit.requestFocus();
                                    showSoftInputOnError();
                                }
                            }, 300);
                        }
                    }).show();
                break;
            default:
                break;
        }
    }

    public void clearTip() {
        if (null == mEdit)
            return;

        switch (mTipType) {
            case TIP_TYPE_ERROR:
                mEdit.setError(null);
                mEdit.requestFocus();
                break;
            case TIP_TYPE_HINT:
                mEdit.setHint(mHintText != null ? mHintText : "");
                mEdit.setHintTextColor(mHintTextColor != null ? mHintTextColor : ColorStateList.valueOf(0xFF999999));
                mEdit.requestFocus();
                break;
            case TIP_TYPE_TOAST:
                mEdit.requestFocus();
                break;
            case TIP_TYPE_ALERT:
                mEdit.requestFocus();
                break;
            default:
                break;
        }
    }

    public void showSoftInputOnError() {
        if (!mShowSoftInputOnError)
            return;
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEdit, 0);
    }

    public void addTextChangedListener(TextWatcher watcher) {
        this.mTextWatcher = watcher;
    }

    public void setOnEmptyChanged(OnEmptyChanged onEmptyChanged) {
        this.mOnEmptyChanged = onEmptyChanged;
    }

    public interface OnEmptyChanged {
        public void changed(boolean notEmpty);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (null == mEdit || null == mClear)
            return;

        mEdit.setEnabled(enabled);
        if (enabled) {
            mClear.setVisibility((TextUtils.isEmpty(mEdit.getText())) ? View.INVISIBLE : View.VISIBLE);
            mEdit.setSelection(mEdit.getText().length());
        } else {
            mClear.setVisibility(View.GONE);
        }
    }

    public boolean requestInputFocus() {
        super.requestFocus();
        return mEdit.requestFocus();
    }

    public void setIcon(int resId) {
        mIcon.setImageResource(resId);
        mIcon.setVisibility(View.VISIBLE);
    }

    public void setLabel(CharSequence text) {
        mLabel.setText(text);
        mLabel.setVisibility(View.VISIBLE);
    }

    public void setInput(CharSequence text) {
        mEdit.setText(text);
        mEdit.requestFocus();
        mEdit.setSelection((null == text) ? 0 : text.length());
    }

    public Editable getInput() {
        return mEdit.getText();
    }

    public void setClear(int resId) {
        mClear.setImageResource(resId);
    }

    public ImageView getIcon() {
        return mIcon;
    }

    public TextView getLabel() {
        return mLabel;
    }

    public EditText getEdit() {
        return mEdit;
    }

    public ImageView getClear() {
        return mClear;
    }

}
