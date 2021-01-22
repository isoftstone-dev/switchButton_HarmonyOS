package com.isoftstone.switchbutton;

import ohos.agp.animation.Animator;
import ohos.agp.animation.AnimatorValue;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.*;
import ohos.agp.components.element.Element;
import ohos.agp.render.Canvas;
import ohos.agp.render.Paint;
import ohos.agp.text.Layout;
import ohos.agp.text.SimpleTextLayout;
import ohos.agp.utils.*;
import ohos.agp.window.service.Display;
import ohos.agp.window.service.DisplayManager;
import ohos.app.Context;
import ohos.global.resource.ResourceManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.MmiPoint;
import ohos.multimodalinput.event.TouchEvent;

import java.util.Optional;


/**
 * SwitchButton
 *
 * @author kyleduo
 * @since 2014-09-24
 */

@SuppressWarnings("unused")
public class SwitchButton extends AbsButton implements AbsButton.CheckedStateChangedListener,Component.DrawTask{
    private static HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x000115,"SwitchButton");
    public static final float DEFAULT_THUMB_RANGE_RATIO = 1.8f;
    public static final int DEFAULT_THUMB_SIZE_DP = 20;
    public static final int DEFAULT_THUMB_MARGIN_DP = 2;
    public static final int DEFAULT_ANIMATION_DURATION = 500;
    public static final int DEFAULT_TINT_COLOR = 0x327FC2;


    private static final int[] CHECKED_PRESSED_STATE = new int[]{ComponentState.COMPONENT_STATE_EMPTY,ComponentState.COMPONENT_STATE_CHECKED};
    private static final int[] UNCHECKED_PRESSED_STATE = new int[]{ComponentState.COMPONENT_STATE_EMPTY,ComponentState.COMPONENT_STATE_CHECKED};

    private Element mThumbDrawable,mThumbDrawable_on,mThumbDrawable_off,mBackDrawable,mBackDrawable_on,mBackDrawable_off;
    private int[][] mBackColorStates, mThumbColorStates;
    private int[] mBackColor, mThumbColor;
    private float mThumbRadius, mBackRadius;
    private RectFloat mThumbMargin;
    private float mThumbRangeRatio;
    private long mAnimationDuration;
    // fade back drawable or color when dragging or animating
    private boolean mFadeBack;
    private int mTintColor;
    private int mThumbWidth;
    private int mThumbHeight;
    private int mBackWidth = 100;
    private int mBackHeight = 60;

    private int mCurrThumbColor, mCurrBackColor, mNextBackColor, mOnTextColor, mOffTextColor;
    private Element mCurrentBackDrawable, mNextBackDrawable;
    private RectFloat mThumbRectF, mBackRectF, mSafeRectF, mTextOnRectF, mTextOffRectF;
    private Paint mPaint;
    // whether using Drawable for thumb or back
    private boolean mIsThumbUseDrawable, mIsBackUseDrawable;
    private boolean mDrawDebugRect = false;
    private AnimatorValue mProgressAnimator;
    // animation control
    private float mProgress;
    // temp position of thumb when dragging or animating
    private RectFloat mPresentThumbRectF;
    private float mStartX, mStartY, mLastX;
    private int mTouchSlop;
    private int mClickTimeout;
    private Paint mRectPaint;
    private CharSequence mTextOn;
    private CharSequence mTextOff;
    private Paint mTextPaint;
    private Layout mOnLayout;
    private Layout mOffLayout;
    private float mTextWidth;
    private float mTextHeight;
    private int mTextThumbInset;
    private int mTextExtra;
    private int mTextAdjust;
    // FIX #78,#85 : When restoring saved states, setChecked() called by super. So disable
    // animation and event listening when restoring.
    private boolean mRestoring = false;
    private boolean mReady = false;
    private boolean mCatch = false;
    private UnsetPressedState mUnsetPressedState;
    private boolean isCheck = false;
    private int status = 0;
    private final int STAUS_INIT = 0;
    private final int STAUS_OFFTOON = 1;
    private final int STAUS_ONTOOFF = 2;
    private boolean isOnToOff = false;
    private CheckedStateChangedListener mChildOnCheckedChangeListener;



    public SwitchButton(Context context, AttrSet attrSet) {
        super(context, attrSet);
        HiLog.info(label,"SwitchButton3 init");
//        addDrawTask(this::onDraw);
        init(attrSet);
        onMeasure(getWidth(),getHeight());
    }

    private void init(AttrSet attrs) {
        HiLog.info(label, "init start");
//        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
//        mClickTimeout = ViewConfiguration.getPressedStateDuration() + ViewConfiguration.getTapTimeout();
        mTouchSlop = 22;
        mClickTimeout = 164;
        mPaint = new Paint();
        mRectPaint = new Paint();
        mRectPaint.setStyle(Paint.Style.STROKE_STYLE);
        Optional<Display> display = DisplayManager.getInstance().getDefaultDisplay(this.getContext());
        Point pt = new Point();
        display.get().getSize(pt);
        HiLog.info(label, "screenDensity:" + getResourceManager().getDeviceCapability().screenDensity);

        mRectPaint.setStrokeWidth(getResourceManager().getDeviceCapability().screenDensity);

        mTextPaint = mPaint;

        mThumbRectF = new RectFloat();
        mBackRectF = new RectFloat();
        mSafeRectF = new RectFloat();
        mThumbMargin = new RectFloat();
        mTextOnRectF = new RectFloat();
        mTextOffRectF = new RectFloat();

//        mProgressAnimator = AnimatorValue.ofFloat(0, 0).setDuration(DEFAULT_ANIMATION_DURATION);
        mProgressAnimator = new AnimatorValue();
        mProgressAnimator.setDuration(DEFAULT_ANIMATION_DURATION);
        mProgressAnimator.setCurveType(Animator.CurveType.LINEAR);
//        mProgressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mProgressAnimator.setValueUpdateListener(new AnimatorValue.ValueUpdateListener(){
            @Override
            public void onUpdate(AnimatorValue animatorValue, float v) {
                setProgress(v);
                HiLog.info(label, "mProgressAnimator onUpdate:"+v);
            }
        });
        mPresentThumbRectF = new RectFloat();

        ResourceManager res = getResourceManager();
        float density = res.getDeviceCapability().screenDensity;

        Element thumbDrawable_on = null;
        Element thumbDrawable_off = null;
        int[] thumbColor = null;
        float margin = density * DEFAULT_THUMB_MARGIN_DP;
        float marginLeft = 0;
        float marginRight = 0;
        float marginTop = 0;
        float marginBottom = 0;
        float thumbWidth = 60;
        float thumbHeight = 30;
        float thumbRadius = 2;
        float backRadius = 3;
        Element backDrawable_on = null;
        Element backDrawable_off = null;
        int[] backColor = null;
        float thumbRangeRatio = DEFAULT_THUMB_RANGE_RATIO;
        int animationDuration = DEFAULT_ANIMATION_DURATION;
        boolean fadeBack = true;
        int tintColor = 0;
        String textOn = null;
        String textOff = null;
        int textThumbInset = 0;
        int textExtra = 0;
        int textAdjust = 0;

        HiLog.info(label, "解析xml数据");
        HiLog.info(label, "解析xml数据：attrs"+attrs.toString());

        thumbDrawable_on = attrs.getAttr("SwitchButton_ThumbDrawable_on").get().getElement();
        HiLog.info(label, "thumbDrawable_on:");
        thumbDrawable_off = attrs.getAttr("SwitchButton_ThumbDrawable_off").get().getElement();
        HiLog.info(label, "thumbDrawable_off:");
//            thumbColor = ta.getColorStateList(R.styleable.SwitchButton_kswThumbColor);
        margin = attrs.getAttr("SwitchButton_kswThumbMargin").get().getFloatValue();
        HiLog.info(label, "margin:");
        thumbWidth = attrs.getAttr("SwitchButton_kswThumbWidth").get().getFloatValue();
        HiLog.info(label, "thumbWidth:" + thumbWidth);
        thumbHeight = attrs.getAttr("SwitchButton_kswThumbHeight").get().getFloatValue();
        HiLog.info(label, "thumbHeight:" + thumbHeight);
        thumbRadius = attrs.getAttr("SwitchButton_kswThumbRadius").get().getFloatValue();
        HiLog.info(label, "thumbRadius:" + thumbRadius);
        backRadius = attrs.getAttr("SwitchButton_kswBackRadius").get().getFloatValue();
        HiLog.info(label, "backRadius:" + backRadius);
        backDrawable_on = attrs.getAttr("SwitchButton_BackDrawable_on").get().getElement();
        backDrawable_off = attrs.getAttr("SwitchButton_BackDrawable_off").get().getElement();
//            backColor = ta.getColorStateList(R.styleable.SwitchButton_kswBackColor);
        thumbRangeRatio = attrs.getAttr("SwitchButton_kswThumbRangeRatio").get().getFloatValue();
        HiLog.info(label, "thumbRangeRatio:" + thumbRangeRatio);
//        animationDuration = attrs.getAttr("SwitchButton_animationDuration").get().getIntegerValue();
//        fadeBack = attrs.getAttr("SwitchButton_kswFadeBack").get().getBoolValue();
        tintColor = attrs.getAttr("SwitchButton_tintColor").get().getColorValue().getValue();
        HiLog.info(label, "tintColor:" + tintColor);
        textOn = attrs.getAttr("SwitchButton_kswTextOn").get().getStringValue();
        HiLog.info(label, "textOn:" + textOn);
        textOff = attrs.getAttr("SwitchButton_kswTextOff").get().getStringValue();
        HiLog.info(label, "textOff:" + textOff);
//        textThumbInset = attrs.getAttr("SwitchButton_kswTextThumbInset").get().getIntegerValue();
//        textExtra = attrs.getAttr("SwitchButton_kswTextExtra").get().getIntegerValue();
//        textThumbInset = attrs.getAttr("SwitchButton_kswTextAdjust").get().getIntegerValue();

        mBackWidth = attrs.getAttr("SwitchButton_BackWidth").get().getIntegerValue();
        mBackHeight = attrs.getAttr("SwitchButton_BackHeight").get().getIntegerValue();
        setFocusable(1);
        setClickable(true);

        // text
        mTextOn = textOn;
        mTextOff = textOff;
        mTextThumbInset = textThumbInset;
        mTextExtra = textExtra;
        mTextAdjust = textAdjust;

        // thumb drawable and color
        mThumbDrawable_on = thumbDrawable_on;
        mThumbDrawable_off = thumbDrawable_off;
        mThumbDrawable = thumbDrawable_off;
        mThumbDrawable.setStateColorList(ColorUtils.ThumbColorstates, ColorUtils.ThumbColors);
        mThumbColor = thumbColor;
        mIsThumbUseDrawable = thumbDrawable_on != null && thumbDrawable_off != null ;
        mTintColor = tintColor;
        if (mTintColor == 0) {
//            mTintColor = getThemeAccentColorOrDefault(getContext(), DEFAULT_TINT_COLOR);
            mTintColor = DEFAULT_TINT_COLOR;
        }
        if (!mIsThumbUseDrawable && mThumbColor == null) {
            HiLog.info(label, "设置Thumb背景色");
            mThumbColor = ColorUtils.generateThumbColorWithTintColor(mTintColor);
            HiLog.info(label, "设置Thumb背景色mThumbColor："+mThumbColor.length);
            HiLog.info(label, "设置Thumb背景色成功");
            mCurrThumbColor =  ColorUtils.getThumbColorForStates(ComponentState.COMPONENT_STATE_EMPTY);
        }

        // thumbSize
        mThumbWidth = ceil(thumbWidth);
        mThumbHeight = ceil(thumbHeight);

        // back drawable and color
        mBackDrawable_on = backDrawable_on;
        mBackDrawable_off = backDrawable_off;
        mBackDrawable = mBackDrawable_off;
        mBackColor = backColor;
        mBackDrawable.setStateColorList(ColorUtils.BackColorstates, ColorUtils.BackColors);
        mIsBackUseDrawable = backDrawable_on != null && backDrawable_off != null;
        if (!mIsBackUseDrawable && mBackColor == null) {
            HiLog.info(label, "设置Back背景色");
            mBackColor = ColorUtils.generateBackColorWithTintColor(mTintColor);
//            mBackDrawable.setStateColorList(ColorUtils.BackColorstates, mBackColor);
            HiLog.info(label, "设置Back背景色成功");
            mCurrBackColor = ColorUtils.getBlackColorForStates(ComponentState.COMPONENT_STATE_EMPTY);
            mNextBackColor = ColorUtils.getBlackColorForStates(ComponentState.COMPONENT_STATE_CHECKED);
            HiLog.info(label,"onDraw:mCurrBackColor:"+mCurrBackColor);
            HiLog.info(label,"onDraw:mNextBackColor:"+mNextBackColor);
        }

        // margin
        HiLog.info(label, "定义mThumbMargin");
        mThumbMargin.modify(marginLeft, marginTop, marginRight, marginBottom);
        HiLog.info(label, "定义mThumbMargin成功");
        // size & measure params must larger than 1
        mThumbRangeRatio = mThumbMargin.getWidth() >= 0 ? Math.max(thumbRangeRatio, 1) : thumbRangeRatio;

        mThumbRadius = thumbRadius;
        mBackRadius = backRadius;
        mAnimationDuration = animationDuration;
        mFadeBack = fadeBack;

//        mProgressAnimator.setDuration(mAnimationDuration);

        // sync checked status
//        if (isChecked()) {
            setProgress(0);
//        }
//        setClickedListener(this::onClick);
//        setCheckedStateChangedListener(this::onCheckedChanged);
        setTouchEventListener(this::onTouchEvent);
//        setClickedListener(this::onClick);

        HiLog.info(label, "init finish");
    }

//    private static int getThemeAccentColorOrDefault(Context context, @SuppressWarnings("SameParameterValue") int defaultColor) {
//        int colorAttr;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            colorAttr = android.R.attr.colorAccent;
//        } else {
//            //Get colorAccent defined for AppCompat
//            colorAttr = context.getResources().getIdentifier("colorAccent", "attr", context.getPackageName());
//        }
//        TypedValue outValue = new TypedValue();
//        boolean resolved = context.getTheme().resolveAttribute(colorAttr, outValue, true);
//        return resolved ? outValue.data : defaultColor;
//    }

    private Layout makeLayout(CharSequence text) {
//        return new StaticLayout(text, mTextPaint, (int) Math.ceil(Layout.getDesiredWidth(text, mTextPaint)), Layout.Alignment.ALIGN_CENTER, 1.f, 0, false);
       HiLog.info(label,"创建开关文字控件：left："+getLeft()+",top:"+getTop()+",right:"+getRight()+",bottom:"+getBottom()+",minwidth:"+getMinWidth());

           return new SimpleTextLayout(text.toString(),mTextPaint,new Rect(),100);

    }



    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /*
         * ensure textLayout
         */
        HiLog.info(label,"widthMeasureSpec:"+widthMeasureSpec+",heightMeasureSpec:"+heightMeasureSpec);
        if (mOnLayout == null && !TextTool.isNullOrEmpty(mTextOn)) {
            mOnLayout = makeLayout(mTextOn);
        }
        if (mOffLayout == null && !TextTool.isNullOrEmpty(mTextOff)) {
            mOffLayout = makeLayout(mTextOff);
        }

        float onWidth = mOnLayout != null ? mOnLayout.getWidth() : 0;
        float offWidth = mOffLayout != null ? mOffLayout.getWidth() : 0;
        HiLog.info(label,"onWidth :"+onWidth);
        HiLog.info(label,"offWidth :"+offWidth);
        if (onWidth != 0 || offWidth != 0) {
            mTextWidth = Math.max(onWidth, offWidth);
        } else {
            mTextWidth = 0;
        }

        float onHeight = mOnLayout != null ? mOnLayout.getHeight() : 0;
        float offHeight = mOffLayout != null ? mOffLayout.getHeight() : 0;
        if (onHeight != 0 || offHeight != 0) {
            mTextHeight = Math.max(onHeight, offHeight);
        } else {
            mTextHeight = 0;
        }
        HiLog.info(label,"mTextWidth :"+mTextWidth);
        HiLog.info(label,"mTextHeight :"+mTextHeight);

        setComponentSize(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    /**
     * SwitchButton use this formula to determine the final size of thumb, background and itself.
     * <p>
     * textWidth = max(onWidth, offWidth)
     * thumbRange = thumbWidth * rangeRatio
     * textExtraSpace = textWidth + textExtra - (moveRange - thumbWidth + max(thumbMargin.left, thumbMargin.right) + textThumbInset)
     * backWidth = thumbRange + thumbMargin.left + thumbMargin.right + max(textExtraSpace, 0)
     * contentSize = thumbRange + max(thumbMargin.left, 0) + max(thumbMargin.right, 0) + max(textExtraSpace, 0)
     *
     * @param widthMeasureSpec widthMeasureSpec
     * @return measuredWidth
     */
    private int measureWidth(int widthMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int measuredWidth = widthSize;

        HiLog.info(label,"widthSize:"+widthSize+",widthMode:"+widthMode);
        if (mThumbWidth == 0 && mIsThumbUseDrawable) {
            mThumbWidth = mThumbDrawable.getWidth();
        }

        int moveRange;
        int textWidth = ceil(mTextWidth);
        // how much the background should extend to fit text.
        int textExtraSpace;
        int contentSize;

        if (mThumbRangeRatio == 0) {
            mThumbRangeRatio = DEFAULT_THUMB_RANGE_RATIO;
        }

        if (widthMode == MeasureSpec.PRECISE) {
            contentSize = widthSize - getPaddingLeft() - getPaddingRight();

            if (mThumbWidth != 0) {
                moveRange = ceil(mThumbWidth * mThumbRangeRatio);
                textExtraSpace = textWidth + mTextExtra - (moveRange - mThumbWidth + ceil(Math.max(mThumbMargin.left, mThumbMargin.right)));
                mBackWidth = ceil(moveRange + mThumbMargin.left + mThumbMargin.right + Math.max(textExtraSpace, 0));
                if (mBackWidth < 0) {
                    mThumbWidth = 0;
                }
                if (moveRange + Math.max(mThumbMargin.left, 0) + Math.max(mThumbMargin.right, 0) + Math.max(textExtraSpace, 0) > contentSize) {
                    mThumbWidth = 0;
                }
            }

            if (mThumbWidth == 0) {
                contentSize = widthSize - getPaddingLeft() - getPaddingRight();
                moveRange = ceil(contentSize - Math.max(mThumbMargin.left, 0) - Math.max(mThumbMargin.right, 0));
                if (moveRange < 0) {
                    mThumbWidth = 0;
                    mBackWidth = 0;
                    return measuredWidth;
                }
                mThumbWidth = ceil(moveRange / mThumbRangeRatio);
                mBackWidth = ceil(moveRange + mThumbMargin.left + mThumbMargin.right);
                if (mBackWidth < 0) {
                    mThumbWidth = 0;
                    mBackWidth = 0;
                    return measuredWidth;
                }
                textExtraSpace = textWidth + mTextExtra - (moveRange - mThumbWidth + ceil(Math.max(mThumbMargin.left, mThumbMargin.right)));
                if (textExtraSpace > 0) {
                    // since backWidth is determined by view width, so we can only reduce thumbSize.
                    mThumbWidth = mThumbWidth - textExtraSpace;
                }
                if (mThumbWidth < 0) {
                    mThumbWidth = 0;
                    mBackWidth = 0;
                    return measuredWidth;
                }
            }
        } else {
            /*
            If parent view want SwitchButton to determine it's size itself, we calculate the minimal
            size of it's content. Further more, we ignore the limitation of widthSize since we want
            to display SwitchButton in its actual size rather than compress the shape.
             */
            if (mThumbWidth == 0) {
                /*
                If thumbWidth is not set, use the default one.
                 */
                mThumbWidth = ceil(getResourceManager().getDeviceCapability().screenDensity * DEFAULT_THUMB_SIZE_DP);
                HiLog.info(label,"-----417-----mThumbWidth-----:"+mThumbWidth);
            }
            if (mThumbRangeRatio == 0) {
                mThumbRangeRatio = DEFAULT_THUMB_RANGE_RATIO;
            }
            HiLog.info(label,"-----422-----mThumbWidth-----:"+mThumbWidth);
            HiLog.info(label,"-----422-----mThumbRangeRatio-----:"+mThumbRangeRatio);
            moveRange = ceil(mThumbWidth * mThumbRangeRatio);
            textExtraSpace = ceil(textWidth + mTextExtra - (moveRange - mThumbWidth + Math.max(mThumbMargin.left, mThumbMargin.right) + mTextThumbInset));
            HiLog.info(label,"-----425-----textExtraSpace-----:"+textExtraSpace);
            mBackWidth = ceil(moveRange + mThumbMargin.left + mThumbMargin.right + Math.max(0, textExtraSpace));
            HiLog.info(label,"-----427-----mBackWidth-----:"+mBackWidth);
            if (mBackWidth < 0) {
                mThumbWidth = 0;
                mBackWidth = 0;
                return measuredWidth;
            }
            contentSize = ceil(moveRange + Math.max(0, mThumbMargin.left) + Math.max(0, mThumbMargin.right) + Math.max(0, textExtraSpace));
            HiLog.info(label,"-----434-----contentSize-----:"+contentSize);
            measuredWidth = Math.max(contentSize, contentSize + getPaddingLeft() + getPaddingRight());
        }
        HiLog.info(label,"-----437-----measuredWidth-----:"+measuredWidth);
        return measuredWidth;
    }

    private int measureHeight(int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int measuredHeight = heightSize;
        HiLog.info(label,"heightSize:"+heightSize+",heightMode:"+heightMode);
        if (mThumbHeight == 0 && mIsThumbUseDrawable) {
            mThumbHeight = mThumbDrawable.getHeight();
        }
        int contentSize;
        int textExtraSpace;
        if (heightMode == MeasureSpec.PRECISE) {
            if (mThumbHeight != 0) {
                /*
                If thumbHeight has been set, we calculate backHeight and check if there is enough room.
                 */
                mBackHeight = ceil(mThumbHeight + mThumbMargin.top + mThumbMargin.bottom);
                mBackHeight = ceil(Math.max(mBackHeight, mTextHeight));
                if (mBackHeight + getPaddingTop() + getPaddingBottom() - Math.min(0, mThumbMargin.top) - Math.min(0, mThumbMargin.bottom) > heightSize) {
                    // No enough room, we set thumbHeight to zero to calculate these value again.
                    mThumbHeight = 0;
                }
            }

            if (mThumbHeight == 0) {
                mBackHeight = ceil(heightSize - getPaddingTop() - getPaddingBottom() + Math.min(0, mThumbMargin.top) + Math.min(0, mThumbMargin.bottom));
                if (mBackHeight < 0) {
                    mBackHeight = 0;
                    mThumbHeight = 0;
                    return measuredHeight;
                }
                mThumbHeight = ceil(mBackHeight - mThumbMargin.top - mThumbMargin.bottom);
            }
            if (mThumbHeight < 0) {
                mBackHeight = 0;
                mThumbHeight = 0;
                return measuredHeight;
            }
        } else {
            if (mThumbHeight == 0) {
                mThumbHeight = ceil(getResourceManager().getDeviceCapability().screenDensity * DEFAULT_THUMB_SIZE_DP);
            }
            mBackHeight = ceil(mThumbHeight + mThumbMargin.top + mThumbMargin.bottom);
            if (mBackHeight < 0) {
                mBackHeight = 0;
                mThumbHeight = 0;
                return measuredHeight;
            }
            textExtraSpace = ceil(mTextHeight - mBackHeight);
            if (textExtraSpace > 0) {
                mBackHeight += textExtraSpace;
                mThumbHeight += textExtraSpace;
            }
            contentSize = Math.max(mThumbHeight, mBackHeight);

            measuredHeight = Math.max(contentSize, contentSize + getPaddingTop() + getPaddingBottom());
            measuredHeight = Math.max(measuredHeight, getMinHeight());
        }

        return measuredHeight;
    }
//
//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//        if (w != oldw || h != oldh) {
//            setup();
//        }
//    }

    private int ceil(double dimen) {
        return (int) Math.ceil(dimen);
    }

    /**
     * set up the rect of back and thumb
     */
    private void setup() {
        if (mThumbWidth == 0 || mThumbHeight == 0 || mBackWidth == 0 || mBackHeight == 0) {
            HiLog.info(label,"setup:return");
            return;
        }

        if (mThumbRadius == -1) {
            mThumbRadius = Math.min(mThumbWidth, mThumbHeight) / 2f;
        }
        if (mBackRadius == -1) {
            mBackRadius = Math.min(mBackWidth, mBackHeight) / 2f;
        }

        HiLog.info(label,"mThumbRadius:"+mThumbRadius);
        HiLog.info(label,"mBackRadius:"+mBackRadius);
        HiLog.info(label,"getPaddingLeft:"+getPaddingLeft());
        HiLog.info(label,"getPaddingRight:"+getPaddingRight());
        int contentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int contentHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        HiLog.info(label,"contentWidth:"+contentWidth);
        HiLog.info(label,"contentWidth:"+contentWidth);
        // max range of drawing content, when thumbMargin is negative, drawing range is larger than backWidth
        int drawingWidth = ceil(mBackWidth - Math.min(0, mThumbMargin.left) - Math.min(0, mThumbMargin.right));
        int drawingHeight = ceil(mBackHeight - Math.min(0, mThumbMargin.top) - Math.min(0, mThumbMargin.bottom));
        HiLog.info(label,"drawingWidth:"+drawingWidth);
        HiLog.info(label,"drawingHeight:"+drawingHeight);
        float thumbTop;
        if (contentHeight <= drawingHeight) {
            thumbTop = getPaddingTop() + Math.max(0, mThumbMargin.top);
        } else {
            // center vertical in content area
            thumbTop = getPaddingTop() + Math.max(0, mThumbMargin.top) + (contentHeight - drawingHeight + 1) / 2f;
        }
        HiLog.info(label,"thumbTop:"+thumbTop);
        float thumbLeft;
        if (contentWidth <= mBackWidth) {
            thumbLeft = getPaddingLeft() + Math.max(0, mThumbMargin.left);
        } else {
            thumbLeft = getPaddingLeft() + Math.max(0, mThumbMargin.left) + (contentWidth - drawingWidth + 1) / 2f;
        }
        HiLog.info(label,"thumbLeft:"+thumbLeft);

        mThumbRectF.modify(thumbLeft, thumbTop, thumbLeft + mThumbWidth, thumbTop + mThumbHeight);
        HiLog.info(label,"设置mThumbRectF:");
        float backLeft = mThumbRectF.left - mThumbMargin.left;
        mBackRectF.modify(backLeft,
                mThumbRectF.top - mThumbMargin.top,
                backLeft + mBackWidth,
                mThumbRectF.top - mThumbMargin.top + mBackHeight);
        HiLog.info(label,"设置mBackRectF:");
        mSafeRectF.modify(mThumbRectF.left, 0, mBackRectF.right - mThumbMargin.right - mThumbRectF.getWidth(), 0);
        HiLog.info(label,"设置mSafeRectF:");
        float minBackRadius = Math.min(mBackRectF.getWidth(), mBackRectF.getHeight()) / 2.f;
        mBackRadius = Math.min(minBackRadius, mBackRadius);

        if (mBackDrawable != null) {
            mBackDrawable.setBounds((int) mBackRectF.left, (int) mBackRectF.top, ceil(mBackRectF.right), ceil(mBackRectF.bottom));
        }

        if (mOnLayout != null) {
            float onLeft = mBackRectF.left + (mBackRectF.getWidth() + mTextThumbInset - mThumbWidth - mThumbMargin.right - mOnLayout.getWidth()) / 2f - mTextAdjust;
            float onTop = mBackRectF.top + (mBackRectF.getHeight() - mOnLayout.getHeight()) / 2;
            mTextOnRectF.modify(onLeft, onTop, onLeft + mOnLayout.getWidth(), onTop + mOnLayout.getHeight());
        }

        if (mOffLayout != null) {
            float offLeft = mBackRectF.right - (mBackRectF.getWidth() + mTextThumbInset - mThumbWidth - mThumbMargin.left - mOffLayout.getWidth()) / 2f - mOffLayout.getWidth() + mTextAdjust;
            float offTop = mBackRectF.top + (mBackRectF.getHeight() - mOffLayout.getHeight()) / 2;
            mTextOffRectF.modify(offLeft, offTop, offLeft + mOffLayout.getWidth(), offTop + mOffLayout.getHeight());
        }

        mReady = true;
    }


    @Override
    public void invalidate() {
        addDrawTask(this::onDraw);
    }

    @Override
    public void onDraw(Component component, Canvas canvas){
        float progress;
        HiLog.info(label,"onDraw：isCheck"+isCheck);
        HiLog.info(label,"onDraw：isOnToOff"+isOnToOff);
        HiLog.info(label,"onDraw：mProgress："+mProgress);
        if(isOnToOff){
            progress = mProgress;
        }else{
            progress = mProgress;
        }
        HiLog.info(label,"onDraw：progress："+progress);
        HiLog.info(label,"onDraw");
        if (!mReady) {
            HiLog.info(label,"onDraw:setup");
            setup();
        }
        if (!mReady) {
            HiLog.info(label,"onDraw:return");
            return;
        }

        // fade back
        if (mIsBackUseDrawable) {
            if (mFadeBack && mCurrentBackDrawable != null && mNextBackDrawable != null) {
                // fix #75, 70%A + 30%B != 30%B + 70%A, order matters when mix two layer of different alpha.
                // So make sure the order of on/off layers never change during slide from one endpoint to another.
                Element below = isChecked() ? mCurrentBackDrawable : mNextBackDrawable;
                Element above = isChecked() ? mNextBackDrawable : mCurrentBackDrawable;

                int alpha = (int) (255 * getProgress());
                below.setAlpha(alpha);
                below.drawToCanvas(canvas);
                alpha = 255 - alpha;
                above.setAlpha(alpha);
                above.drawToCanvas(canvas);
            } else {
                HiLog.info(label,"onDraw:619");
                mBackDrawable.setAlpha(255);
                mBackDrawable.drawToCanvas(canvas);
            }
        } else {
            if (mFadeBack) {
                HiLog.info(label,"onDraw:624");
                int alpha;
                int colorAlpha;

                // fix #75
                int belowColor = isChecked() ? mCurrBackColor : mNextBackColor;
                int aboveColor = isChecked() ? mNextBackColor : mCurrBackColor;
                HiLog.info(label,"onDraw:mCurrBackColor:"+mCurrBackColor);
                HiLog.info(label,"onDraw:mNextBackColor:"+mNextBackColor);
                HiLog.info(label,"onDraw:belowColor:"+belowColor);
                HiLog.info(label,"onDraw:aboveColor:"+aboveColor);
                // 当前背景色
                alpha = (int) (255 * getProgress());
                colorAlpha = Color.alpha(belowColor);
                colorAlpha = colorAlpha * alpha / 255;
                RgbColor rgbColor = new RgbColor(belowColor);
//                    mPaint.setARGB(colorAlpha, Color.red(belowColor), Color.green(belowColor), Color.blue(belowColor));
                Color color = new Color(Color.argb(colorAlpha, rgbColor.getRed(), rgbColor.getGreen(), rgbColor.getBlue()));
//                Color color = new Color(belowColor);
//                HiLog.info(label,"onDraw:color:"+color.getValue());
                mPaint.setColor(color);
                HiLog.info(label,"onDraw:color:"+color);
                canvas.drawRoundRect(mBackRectF, mBackRadius, mBackRadius, mPaint);

                // next back
                alpha = 255 - alpha;
                colorAlpha = Color.alpha(aboveColor);
                colorAlpha = colorAlpha * alpha / 255;
//                    mPaint.setARGB(colorAlpha, Color.red(aboveColor), Color.green(aboveColor), Color.blue(aboveColor));
                RgbColor rgbColor2 = new RgbColor(aboveColor);
                Color color2 = new Color(Color.argb(colorAlpha, rgbColor2.getRed(), rgbColor2.getGreen(), rgbColor2.getBlue()));
//                Color color2 = new Color(aboveColor);
                mPaint.setColor(color2);
                HiLog.info(label,"onDraw:color2:"+color2);
                canvas.drawRoundRect(mBackRectF, mBackRadius, mBackRadius, mPaint);

                mPaint.setAlpha(255);
            } else {
                HiLog.info(label,"onDraw:653");
                mPaint.setColor(new Color(mCurrBackColor));
                canvas.drawRoundRect(mBackRectF, mBackRadius, mBackRadius, mPaint);
            }
        }

         //text
        Layout switchText =progress > 0.5 ? mOnLayout : mOffLayout;
        HiLog.info(label,"onDraw:switchText:"+switchText.getHeight()+",width:"+switchText.getWidth());
        RectFloat textRectF = progress > 0.5 ? mTextOnRectF : mTextOffRectF;
        if (switchText != null && textRectF != null) {
            int alpha = (int) (255 * (progress >= 0.75 ? progress * 4 - 3 : progress < 0.25 ? 1 - progress * 4 : 0));
            int textColor = progress > 0.5 ? mOnTextColor : mOffTextColor;
            int colorAlpha = Color.alpha(textColor);
            colorAlpha = colorAlpha * alpha / 255;

            RgbColor textColorRgb = new RgbColor(textColor);
            Color color2 = new Color(Color.argb(colorAlpha, textColorRgb.getRed(), textColorRgb.getGreen(), textColorRgb.getBlue()));
            mTextPaint.setColor(color2);

            canvas.save();
            canvas.translate(textRectF.left, textRectF.top);
            switchText.drawText(canvas);
            HiLog.info(label,switchText.toString());
            canvas.restore();
        }

        // thumb滑块
        mPresentThumbRectF.modify(mThumbRectF);
        HiLog.info(label,"onDraw:设置mPresentThumbRectF:");
        setOffsetOfRectF(mPresentThumbRectF,progress * mSafeRectF.getWidth(), 0);
        HiLog.info(label,"onDraw:设置mPresentThumbRectF成功:");
        if (mIsThumbUseDrawable) {
            HiLog.info(label,"onDraw:mThumbDrawable:");
            mThumbDrawable.setBounds((int) mPresentThumbRectF.left, (int) mPresentThumbRectF.top, ceil(mPresentThumbRectF.right), ceil(mPresentThumbRectF.bottom));
            mThumbDrawable.drawToCanvas(canvas);
        } else {
            mPaint.setColor(new Color(mCurrThumbColor));
            canvas.drawRoundRect(mPresentThumbRectF, mThumbRadius, mThumbRadius, mPaint);
            HiLog.info(label,"onDraw:drawRoundRect:");
        }

        if (mDrawDebugRect) {
            mRectPaint.setColor(new Color(Color.getIntColor("#AA0000")));
            canvas.drawRect(mBackRectF, mRectPaint);
            mRectPaint.setColor(new Color(Color.getIntColor("#0000FF")));
            canvas.drawRect(mPresentThumbRectF, mRectPaint);
            mRectPaint.setColor(new Color(Color.getIntColor("#000000")));
            Point startPoint = new Point(mSafeRectF.left, mThumbRectF.top);
            Point endPoint = new Point( mSafeRectF.right, mThumbRectF.top);
            canvas.drawLine(startPoint,endPoint, mRectPaint);
            mRectPaint.setColor(new Color(Color.getIntColor("#00CC00")));
            canvas.drawRect(getProgress() > 0.5 ? mTextOnRectF : mTextOffRectF, mRectPaint);
        }
    }

    private RectFloat setOffsetOfRectF(RectFloat rectFloat,float offsetX,float offsetY){
        float left = rectFloat.left + offsetX;
        float right = rectFloat.right + offsetX;
        float top = rectFloat.top + offsetY;
        float bottom = rectFloat.bottom + offsetY;
        rectFloat.modify(left,top,right,bottom);
        return rectFloat;
    }


    protected void drawableStateChanged(int states) {
        mReady = false;
        if (!mIsThumbUseDrawable && mThumbColor != null) {
            mCurrThumbColor = ColorUtils.getThumbColorForStates(states);
            HiLog.info(label, "修改滑块颜色:"+mCurrThumbColor);
        } else {
            if(getProgress() >= 0.5){
                mThumbDrawable = mThumbDrawable_on;
            }else{
                mThumbDrawable = mThumbDrawable_off;
            }

        }

        int[] nextState = isChecked() ? UNCHECKED_PRESSED_STATE : CHECKED_PRESSED_STATE;

        if (!mIsBackUseDrawable && mBackColor != null) {
            mCurrBackColor = ColorUtils.getThumbColorForStates(states);
            mNextBackColor = ColorUtils.getThumbColorForNextStates(states);
            HiLog.info(label, "修改轨迹颜色:"+mNextBackColor);
        } else {
            if(getProgress() >= 0.5){
                mBackDrawable = mBackDrawable_on;
            }else{
                mBackDrawable = mBackDrawable_off;
            }
        }
    }

    public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
        {
            int action = touchEvent.getAction();
            MmiPoint point = touchEvent.getPointerPosition(touchEvent.getIndex());
            float deltaX = point.getX() - mStartX;
            float deltaY = point.getY() - mStartY;

            switch (action) {
                case TouchEvent.PRIMARY_POINT_DOWN:
                    mStartX = point.getX();
                    mStartY = point.getY();
                    mLastX = mStartX;
                    setPressState(true);
                    break;

                case TouchEvent.POINT_MOVE:
                    float x =  point.getX();
                    drawableStateChanged(isCheck?ComponentState.COMPONENT_STATE_CHECKED:ComponentState.COMPONENT_STATE_EMPTY);
                    setProgress(getProgress() + (x - mLastX) / mSafeRectF.getWidth());
                    mLastX = x;
                    if (!mCatch && (Math.abs(deltaX) > mTouchSlop / 2f || Math.abs(deltaY) > mTouchSlop / 2f)) {
                        if (deltaY == 0 || Math.abs(deltaX) > Math.abs(deltaY)) {
                            catchView();
                        } else if (Math.abs(deltaY) > Math.abs(deltaX)) {
                            return false;
                        }
                    }
                    break;

                case TouchEvent.CANCEL:
                case TouchEvent.PRIMARY_POINT_UP:
                    mCatch = false;
                    float time = touchEvent.getOccurredTime() - touchEvent.getStartTime();
                    if (Math.abs(deltaX) < mTouchSlop && Math.abs(deltaY) < mTouchSlop && time < mClickTimeout) {
                        HiLog.info(label,"Log_onTouchEvent simulateClick");
                        simulateClick();
                    } else {
                        boolean nextStatus = getStatusBasedOnPos();
                        if (nextStatus != isChecked()) {
                            setChecked(nextStatus);
                        }
                        else {
                            setProgress(nextStatus?1:0);
                        }
                    }
                    if (isPressed()) {
                        if (mUnsetPressedState == null) {
                            mUnsetPressedState = new UnsetPressedState();
                        }
                    }
                    break;

                default:
                    break;
            }
            return true;
        }
    }



    /**
     * return the status based on position of thumb
     *
     * @return whether checked or not
     */
    private boolean getStatusBasedOnPos() {
        return getProgress() > 0.5f;
    }

    @Override
    public boolean simulateClick() {
        HiLog.info(label,"simulateClick");
        setChecked(!isCheck);
        return true;
    }

    /**
     * processing animation
     *
     * @param checked checked or unChecked
     */
    protected void animateToState(boolean checked) {
        if (mProgressAnimator == null) {
            return;
        }
        if (mProgressAnimator.isRunning()) {
            mProgressAnimator.cancel();
        }
        mProgressAnimator.setDuration(mAnimationDuration);
        mProgressAnimator.start();
    }

    private void catchView() {
        ComponentParent parent = getComponentParent();
        if (parent != null) {
//            parent.requestDisallowInterceptTouchEvent(true);
        }
        mCatch = true;
    }

    @Override
    public void setChecked(final boolean checked) {
        // animate before super.setChecked() become user may call setChecked again in OnCheckedChangedListener
        HiLog.info(label,"checked:"+checked);
//        if (isChecked() != checked) {
//            animateToState(checked);
//        }
        isCheck = checked;
        if(mChildOnCheckedChangeListener != null){
            mChildOnCheckedChangeListener.onCheckedChanged(this,isCheck);
        }
        setProgress(isCheck?1:0);
        drawableStateChanged(isCheck?ComponentState.COMPONENT_STATE_CHECKED:ComponentState.COMPONENT_STATE_EMPTY);
//        if(isCheck){
//            isOnToOff = false;
//        }else{
//            isOnToOff = true;
//        }
//        if (mRestoring) {
//            setCheckedImmediatelyNoEvent(checked);
//        } else {
//            super.setChecked(checked);
//        }
    }

    public boolean isChecked() {
        return isCheck;
    }

    public void setCheckedNoEvent(final boolean checked) {
        if (mChildOnCheckedChangeListener == null) {
            setChecked(checked);
        } else {
            setCheckedStateChangedListener(null);
            setChecked(checked);
            setCheckedStateChangedListener(mChildOnCheckedChangeListener);
        }
    }

    public void setCheckedImmediatelyNoEvent(boolean checked) {
        if (mChildOnCheckedChangeListener == null) {
            setCheckedImmediately(checked);
        } else {
            setCheckedStateChangedListener(null);
            setCheckedImmediately(checked);
            setCheckedStateChangedListener(mChildOnCheckedChangeListener);
        }
    }

    public void setCheckedImmediately(boolean checked) {
        setChecked(checked);
        if (mProgressAnimator != null && mProgressAnimator.isRunning()) {
            mProgressAnimator.cancel();
        }
        setProgress(checked ? 1 : 0);
        invalidate();
    }

    private float getProgress() {
        return mProgress;
    }

    private void setProgress(final float progress) {
        float tempProgress = progress;
        if (tempProgress > 1) {
            tempProgress = 1;
        } else if (tempProgress < 0) {
            tempProgress = 0;
        }
        this.mProgress = tempProgress;
        HiLog.info(label,"progress:"+mProgress);
        invalidate();
    }


    public void toggleImmediately() {
        setCheckedImmediately(!isChecked());
    }

    public void toggleNoEvent() {
        if (mChildOnCheckedChangeListener == null) {
            toggle();
        } else {
            setCheckedStateChangedListener(null);
            toggle();
            setCheckedStateChangedListener(mChildOnCheckedChangeListener);
        }
    }

    public void toggle() {
        setChecked(!isChecked());
    }

    public void toggleImmediatelyNoEvent() {
        if (mChildOnCheckedChangeListener == null) {
            toggleImmediately();
        } else {
            super.setCheckedStateChangedListener(null);
            toggleImmediately();
            super.setCheckedStateChangedListener(mChildOnCheckedChangeListener);
        }
    }

    @Override
    public void setCheckedStateChangedListener(CheckedStateChangedListener onCheckedChangeListener) {
//        super.setCheckedStateChangedListener(onCheckedChangeListener);
        mChildOnCheckedChangeListener = onCheckedChangeListener;
    }


//    private void setDrawableState(Element element) {
//        if (drawable != null) {
//            int[] myDrawableState = getDrawableState();
//            drawable.setState(myDrawableState);
//            invalidate();
//        }
//    }

    public boolean isDrawDebugRect() {
        return mDrawDebugRect;
    }

    public void setDrawDebugRect(boolean drawDebugRect) {
        mDrawDebugRect = drawDebugRect;
        invalidate();
    }

    public long getAnimationDuration() {
        return mAnimationDuration;
    }

    public void setAnimationDuration(long animationDuration) {
        mAnimationDuration = animationDuration;
    }

    public Element getThumbDrawable() {
        return mThumbDrawable;
    }

    public void setThumbDrawable(Element thumbDrawable) {
        mThumbDrawable = thumbDrawable;
        mIsThumbUseDrawable = mThumbDrawable != null;
//        refreshDrawableState();
        mReady = false;
//        requestLayout();
        invalidate();
    }

//    public void setThumbDrawableRes(int thumbDrawableRes) {
//        setThumbDrawable(getDrawableCompat(getContext(), thumbDrawableRes));
//    }

    public Element getBackDrawable() {
        return mBackDrawable;
    }

    public void setBackDrawable(Element backDrawable) {
        mBackDrawable = backDrawable;
        mIsBackUseDrawable = mBackDrawable != null;
//        refreshDrawableState();
        mReady = false;
//        requestLayout();
        invalidate();
    }

//    public void setBackDrawableRes(int backDrawableRes) {
//        setBackDrawable(getDrawableCompat(getContext(), backDrawableRes));
//    }

    public int[] getBackColor() {
        return mBackColor;
    }

    public void setBackColor(int[] backColor) {
        mBackColor = backColor;
        if (mBackColor != null) {
            setBackDrawable(null);
        }
        invalidate();
    }

    public void setBackColorRes(int backColorRes) {
        setBackColor(ColorUtils.generateBackColorWithTintColor(backColorRes));
    }

    public int[] getThumbColor() {
        return mThumbColor;
    }

    public void setThumbColor(int[] thumbColor) {
        mThumbColor = thumbColor;
        if (mThumbColor != null) {
            setThumbDrawable(null);
        }
        invalidate();
    }

//    public void setThumbColorRes(int thumbColorRes) {
//        setThumbColor(getColorStateListCompat(getContext(), thumbColorRes));
//    }

    public float getThumbRangeRatio() {
        return mThumbRangeRatio;
    }

    public void setThumbRangeRatio(float thumbRangeRatio) {
        mThumbRangeRatio = thumbRangeRatio;
        // We need to mark "ready" to false since requestLayout may not cause size changed.
        mReady = false;
//        requestLayout();
    }

    public RectFloat getThumbMargin() {
        return mThumbMargin;
    }

    public void setThumbMargin(RectFloat thumbMargin) {
        if (thumbMargin == null) {
            setThumbMargin(0, 0, 0, 0);
        } else {
            setThumbMargin(thumbMargin.left, thumbMargin.top, thumbMargin.right, thumbMargin.bottom);
        }
    }

    public void setThumbMargin(float left, float top, float right, float bottom) {
        mThumbMargin.modify(left, top, right, bottom);
        mReady = false;
//        requestLayout();
    }

    public void setThumbSize(int width, int height) {
        mThumbWidth = width;
        mThumbHeight = height;
        mReady = false;
//        requestLayout();
    }

    public int getThumbWidth() {
        return mThumbWidth;
    }

    public int getThumbHeight() {
        return mThumbHeight;
    }

    public float getThumbRadius() {
        return mThumbRadius;
    }

    public void setThumbRadius(float thumbRadius) {
        mThumbRadius = thumbRadius;
        if (!mIsThumbUseDrawable) {
            invalidate();
        }
    }

    public Point getBackSizeF() {
        return new Point(mBackRectF.getWidth(), mBackRectF.getHeight());
    }

    public float getBackRadius() {
        return mBackRadius;
    }

    public void setBackRadius(float backRadius) {
        mBackRadius = backRadius;
        if (!mIsBackUseDrawable) {
            invalidate();
        }
    }

    public boolean isFadeBack() {
        return mFadeBack;
    }

    public void setFadeBack(boolean fadeBack) {
        mFadeBack = fadeBack;
    }

    public int getTintColor() {
        return mTintColor;
    }

    public void setTintColor(@SuppressWarnings("SameParameterValue") int tintColor) {
        mTintColor = tintColor;
        mThumbColor = ColorUtils.generateThumbColorWithTintColor(mTintColor);
        mBackColor = ColorUtils.generateBackColorWithTintColor(mTintColor);
        mIsBackUseDrawable = false;
        mIsThumbUseDrawable = false;
        // call this method to refresh color states
//        refreshDrawableState();
        invalidate();
    }

    public void setText(CharSequence onText, CharSequence offText) {
        mTextOn = onText;
        mTextOff = offText;

        mOnLayout = null;
        mOffLayout = null;

        mReady = false;
//        requestLayout();
        invalidate();
    }

    public CharSequence getTextOn() {
        return mTextOn;
    }

    public CharSequence getTextOff() {
        return mTextOff;
    }

    public void setTextThumbInset(int textThumbInset) {
        mTextThumbInset = textThumbInset;
        mReady = false;
//        requestLayout();
        invalidate();
    }

    public void setTextExtra(int textExtra) {
        mTextExtra = textExtra;
        mReady = false;
//        requestLayout();
        invalidate();
    }

    public void setTextAdjust(int textAdjust) {
        mTextAdjust = textAdjust;
        mReady = false;
//        requestLayout();
        invalidate();
    }

//    @Override
//    public void onClick(Component component) {
//         HiLog.info(label,"onClick");
//        drawableStateChanged();
//
//    }


//    @Override
//    public void onClick(Component component) {
//        HiLog.info(label,"onClick");
////        setProgress(0);
//        setChecked(!isCheck);
//    }

    @Override
    public boolean isClickable() {
        HiLog.info(label,"isClickable"+isChecked());
        return super.isClickable();
    }



    @Override
    public void onCheckedChanged(AbsButton absButton, boolean b) {
        HiLog.info(label,"onCheckedChanged");
    }


    //    @Override
//    public Parcelable onSaveInstanceState() {
//        Parcelable superState = super.onSaveInstanceState();
//        SavedState ss = new SavedState(superState);
//        ss.onText = mTextOn;
//        ss.offText = mTextOff;
//        return ss;
//    }
//
//    @Override
//    public void onRestoreInstanceState(Parcelable state) {
//        SavedState ss = (SavedState) state;
//        setText(ss.onText, ss.offText);
//        mRestoring = true;
//        super.onRestoreInstanceState(ss.getSuperState());
//        mRestoring = false;
//    }

//    /**
//     * Copied from compat library
//     *
//     * @param context context
//     * @param id      id
//     * @return Drawable
//     */
//    private Element getDrawableCompat(Context context, int id) {
//        final int version = Build.VERSION.SDK_INT;
//        if (version >= 21) {
//            return context.getDrawable(id);
//        } else {
//            //noinspection deprecation
//            return context.getResources().getDrawable(id);
//        }
//        ohos.global.resource.ResourceManager resManager = getResourceManager();
//        try {
//            Element element = resManager.getElement(id);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (NotExistException e) {
//            e.printStackTrace();
//        } catch (WrongTypeException e) {
//            e.printStackTrace();
//        }
//
//    }

//    /**
//     * Copied from compat library
//     *
//     * @param context context
//     * @param id      id
//     * @return ColorStateList
//     */
//    private int[] getColorStateListCompat(Context context, int id) {
//        final int version = Build.VERSION.SDK_INT;
//        if (version >= 23) {
//            return context.getColorStateList(id);
//        } else {
//            //noinspection deprecation
//            return context.getResources().getColorStateList(id);
//        }
//
//
//    }

//    static class SavedState extends BaseSavedState {
//        CharSequence onText;
//        CharSequence offText;
//
//        SavedState(Sequenceable  superState) {
//            super(superState);
//        }
//
//        private SavedState(Parcel in) {
//            super(in);
//            onText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
//            offText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
//        }
//
//        @Override
//        public void writeToParcel(Parcel out, int flags) {
//            super.writeToParcel(out, flags);
//            TextUtils.writeToParcel(onText, out, flags);
//            TextUtils.writeToParcel(offText, out, flags);
//        }
//
//        public static final Parcelable.Creator<SavedState> CREATOR
//                = new Parcelable.Creator<SavedState>() {
//            public SavedState createFromParcel(Parcel in) {
//                return new SavedState(in);
//            }
//
//            public SavedState[] newArray(int size) {
//                return new SavedState[size];
//            }
//        };
//    }

    private final class UnsetPressedState implements Runnable {
        @Override
        public void run() {
            setPressState(false);
        }
    }
}