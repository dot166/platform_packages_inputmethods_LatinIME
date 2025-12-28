package com.android.inputmethod.keyboard.emoji;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.inputmethod.latin.R;

public class KaomojiFlowLayout extends ViewGroup {

    private String[] mKaomojis = new String[0];
    private int mTextColor = 0;
    private EmojiPageKeyboardView.OnKeyEventListener mListener;
    private final int mItemMarginPx;

    public KaomojiFlowLayout(@NonNull Context context) {
        this(context, null);
    }

    public KaomojiFlowLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mItemMarginPx = context.getResources().getDimensionPixelSize(R.dimen.kaomoji_item_margin);
    }

    public void setKaomojis(String[] kaomojis) {
        mKaomojis = kaomojis != null ? kaomojis : new String[0];
        removeAllViews();
        for (final String k : mKaomojis) {
            final Button b = new Button(getContext());
            b.setText(k);
            b.setBackground(null);
            b.setPadding(0, 0, 0, 0);
            b.setTextColor(mTextColor);
            b.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onKaomojiPress(k);
                    }
                }
            });
            final MarginLayoutParams lp = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.setMargins(mItemMarginPx, mItemMarginPx, mItemMarginPx, mItemMarginPx);
            addView(b, lp);
        }
        requestLayout();
    }

    public void setTextColor(int color) {
        mTextColor = color;
        for (int i = 0; i < getChildCount(); i++) {
            final View v = getChildAt(i);
            if (v instanceof Button) {
                ((Button)v).setTextColor(color);
            }
        }
    }

    public void addListener(EmojiPageKeyboardView.OnKeyEventListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int maxWidth = widthSize - getPaddingLeft() - getPaddingRight();

        int lineWidth = 0;
        int lineHeight = 0;
        int totalHeight = getPaddingTop() + getPaddingBottom();
        int maxLineWidth = 0;

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            measureChildWithMargins(child, MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST), 0,
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 0);
            final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            final int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            final int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            if (lineWidth + childWidth > maxWidth && lineWidth > 0) {
                totalHeight += lineHeight;
                maxLineWidth = Math.max(maxLineWidth, lineWidth);
                lineWidth = childWidth;
                lineHeight = childHeight;
            } else {
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
        }
        totalHeight += lineHeight;
        maxLineWidth = Math.max(maxLineWidth, lineWidth);

        final int measuredWidth = (widthMode == MeasureSpec.EXACTLY) ? widthSize : (maxLineWidth + getPaddingLeft() + getPaddingRight());
        final int measuredHeight = resolveSize(totalHeight, heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        final int maxWidth = width - getPaddingLeft() - getPaddingRight();
        int x = getPaddingLeft();
        int y = getPaddingTop();
        int lineHeight = 0;

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();
            final int childTotalWidth = childWidth + lp.leftMargin + lp.rightMargin;

            if (x + childTotalWidth > getPaddingLeft() + maxWidth && x > getPaddingLeft()) {
                x = getPaddingLeft();
                y += lineHeight;
                lineHeight = 0;
            }

            final int left = x + lp.leftMargin;
            final int top = y + lp.topMargin;
            child.layout(left, top, left + childWidth, top + childHeight);

            x += childTotalWidth;
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin);
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }
}
