package com.cretin.www.wheelsruflibrary.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.cretin.www.wheelsruflibrary.R;
import com.cretin.www.wheelsruflibrary.listener.RotateListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cretin on 2017/12/26.
 */

public class WheelSurfPanView extends View {
    private Context mContext;
    //记录视图的大小
    private int mWidth;
    //记录当前有几个分类
    private Paint mPaint;
    //文字画笔
    private Paint mTextPaint;
    //圆环图片
    private Bitmap mYuanHuan;
    //大图片
    private Bitmap mMain;
    //中心点横坐标
    private int mCenter;
    //绘制扇形的半径 减掉50是为了防止边界溢出  具体效果你自己注释掉-50自己测试
    private int mRadius;
    //每一个扇形的角度
    private float mAngle;

    //动画回调监听
    private RotateListener rotateListener;

    public RotateListener getRotateListener() {
        return rotateListener;
    }

    public void setRotateListener(RotateListener rotateListener) {
        this.rotateListener = rotateListener;
    }

    public WheelSurfPanView(Context context) {
        super(context);
        init(context, null);
    }

    public WheelSurfPanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WheelSurfPanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    //当前类型 1 自定义模式 2 暴力模式
    private int mType;
    //最低圈数 默认值3 也就是说每次旋转都会最少转3圈
    private int mMinTimes;
    //分类数量 如果数量为负数  通过代码设置样式
    private int mTypeNum = 6;
    //每个扇形旋转的时间
    private int mVarTime = 75;
    //文字描述集合
    private String[] mDeses;
    //背景颜色
    private Integer[] mColors;
    //整个旋转图的背景 只有类型为2时才需要
    private Integer mMainImgRes;
    //圆环的图片引用
    private Integer mHuanImgRes;
    //文字大小
    private float mTextSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SHIFT, 30, getResources().getDisplayMetrics());
    //文字颜色
    private int mTextColor;

    public void setmType(int mType) {
        this.mType = mType;
    }

    public void setmMinTimes(int mMinTimes) {
        this.mMinTimes = mMinTimes;
    }

    public void setmVarTime(int mVarTime) {
        this.mVarTime = mVarTime;
    }

    public void setmTypeNum(int mTypeNum) {
        this.mTypeNum = mTypeNum;
    }

    public void setmDeses(String[] mDeses) {
        this.mDeses = mDeses;
    }

    public void setmColors(Integer[] mColors) {
        this.mColors = mColors;
    }

    public void setmMainImgRes(Integer mMainImgRes) {
        this.mMainImgRes = mMainImgRes;
    }

    public void setmHuanImgRes(Integer mHuanImgRes) {
        this.mHuanImgRes = mHuanImgRes;
    }

    public void setmTextSize(float mTextSize) {
        this.mTextSize = mTextSize;
    }

    public void setmTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        setBackgroundColor(Color.TRANSPARENT);

        if ( attrs != null ) {
            //获得这个控件对应的属性。
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.wheelSurfView);
            try {
                mType = typedArray.getInteger(R.styleable.wheelSurfView_type, 1);
                mVarTime = typedArray.getInteger(R.styleable.wheelSurfView_vartime, 0);
                mMinTimes = typedArray.getInteger(R.styleable.wheelSurfView_minTimes, 3);
                mTypeNum = typedArray.getInteger(R.styleable.wheelSurfView_typenum, 0);

                if ( mTypeNum == -1 ) {
                    //用代码去配置这些参数
                } else {
                    if ( mVarTime == 0 )
                        mVarTime = 75;

                    if ( mTypeNum == 0 )
                        throw new RuntimeException("找不到分类数量mTypeNum");

                    //每一个扇形的角度
                    mAngle = ( float ) (360.0 / mTypeNum);

                    if ( mType == 1 ) {
                        mHuanImgRes = typedArray.getResourceId(R.styleable.wheelSurfView_huanImg, 0);
                        if ( mHuanImgRes == 0 )
                            mYuanHuan = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.yuanhuan);
                        else {
                            mYuanHuan = BitmapFactory.decodeResource(mContext.getResources(), mHuanImgRes);
                        }
                        //文字颜色 默认粉红色
                        mTextColor = typedArray.getColor(R.styleable.wheelSurfView_textColor, Color.parseColor("#ff00ff"));

                        //描述
                        int nameArray = typedArray.getResourceId(R.styleable.wheelSurfView_deses, -1);
                        if ( nameArray == -1 ) throw new RuntimeException("找不到描述");
                        mDeses = context.getResources().getStringArray(nameArray);
                        //颜色
                        int colorArray = typedArray.getResourceId(R.styleable.wheelSurfView_colors, -1);
                        if ( colorArray == -1 ) throw new RuntimeException("找不到背景颜色");
                        String[] colorStrs = context.getResources().getStringArray(colorArray);
                        if ( mDeses == null ||  colorStrs == null )
                            throw new RuntimeException("找不到描述或图片或背景颜色资源");
                        if ( mDeses.length != mTypeNum )
                            throw new RuntimeException("资源或描述的长度和mTypeNum不一致");
                        mColors = new Integer[mTypeNum];
                        //分析背景颜色
                        for ( int i = 0; i < colorStrs.length; i++ ) {
                            try {
                                mColors[i] = Color.parseColor(colorStrs[i]);
                            } catch ( Exception e ) {
                                throw new RuntimeException("颜色值有误");
                            }
                        }
                        //文字画笔
                        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//                        字符间距
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mTextPaint.setLetterSpacing(0.3f);
                        }
                        //设置画笔颜色
                        mTextPaint.setColor(mTextColor);
                        //设置字体大小
                        mTextPaint.setTextSize(mTextSize);
                    } else if ( mType == 2 ) {
                        mMainImgRes = typedArray.getResourceId(R.styleable.wheelSurfView_mainImg, 0);
                        //直接大图
                        if ( mMainImgRes == 0 )
                            throw new RuntimeException("类型为2必须要传大图mMainImgRes");
                        mMain = BitmapFactory.decodeResource(mContext.getResources(), mMainImgRes);
                    } else {
                        throw new RuntimeException("类型type错误");
                    }
                }
            } finally { //回收这个对象
                typedArray.recycle();
            }
        }

        //其他画笔
        mPaint = new Paint();
        //设置填充样式
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        //设置抗锯齿
        mPaint.setAntiAlias(true);
        //设置边界模糊
        mPaint.setDither(true);
    }


    //目前的角度
    private float currAngle = 0;
    //记录上次的位置
    private int lastPosition;

    /**
     * 开始转动
     * pos 位置 1 开始 这里的位置上是按照逆时针递增的 比如当前指的那个选项是第一个  那么他左边的那个是第二个 以此类推
     */
    public void startRotate(final int pos) {
        //最低圈数是mMinTimes圈
        int newAngle = ( int ) (360 * mMinTimes + (pos - 1) * mAngle + currAngle - (lastPosition == 0 ? 0 : ((lastPosition - 1) * mAngle)));
        //计算目前的角度划过的扇形份数
        int num = ( int ) ((newAngle - currAngle) / mAngle);
        ObjectAnimator anim = ObjectAnimator.ofFloat(WheelSurfPanView.this, "rotation", currAngle, newAngle);
        currAngle = newAngle;
        lastPosition = pos;
        // 动画的持续时间，执行多久？
        anim.setDuration(num * mVarTime);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //将动画的过程态回调给调用者
                if ( rotateListener != null )
                    rotateListener.rotating(animation);
            }
        });
        final float[] f = {0};
        anim.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float t) {
                float f1 = ( float ) (Math.cos((t + 1) * Math.PI) / 2.0f) + 0.5f;
                Log.e("HHHHHHHh", "" + t + "     " + (f[0] - f1));
                f[0] = ( float ) (Math.cos((t + 1) * Math.PI) / 2.0f) + 0.5f;
                return f[0];
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //当旋转结束的时候回调给调用者当前所选择的内容
                if ( rotateListener != null ) {
                    if ( mType == 1 ) {
                        //去空格和前后空格后输出
                        String des = mDeses[(mTypeNum - pos + 1) %
                                mTypeNum].trim().replaceAll(" ", "");
                        rotateListener.rotateEnd(pos, des);
                    } else {
                        rotateListener.rotateEnd(pos, "");
                    }
                }
            }
        });
        // 正式开始启动执行动画
        anim.start();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //视图是个正方形的 所以有宽就足够了 默认值是500 也就是WRAP_CONTENT的时候
        int desiredWidth = 800;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int width;

        //Measure Width
        if ( widthMode == MeasureSpec.EXACTLY ) {
            //Must be this size
            width = widthSize;
        } else if ( widthMode == MeasureSpec.AT_MOST ) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //将测得的宽度保存起来
        mWidth = width;

        mCenter = mWidth / 2;
        //绘制扇形的半径 减掉50是为了防止边界溢出  具体效果你自己注释掉-50自己测试
        mRadius = mWidth / 2 - 50;

        //MUST CALL THIS
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ( mTypeNum == -1 ) {
            //先不管
        } else {
            if ( mType == 1 ) {
                // 计算初始角度
                // 从最上面开始绘制扇形会好看一点
                float startAngle = -mAngle / 2 - 90;
                for ( int i = 0; i < mTypeNum; i++ ) {
                    //设置绘制时画笔的颜色
                    mPaint.setColor(mColors[i%2]);
                    //画一个扇形
                    RectF rect = new RectF(mCenter - mRadius, mCenter - mRadius, mCenter
                            + mRadius, mCenter + mRadius);
                    canvas.drawArc(rect, startAngle, mAngle, true, mPaint);
                    mTextPaint.setColor(mTextColor);
                    drawText(startAngle, mDeses[i], mRadius, mTextPaint, canvas);
                    //重置开始角度
                    startAngle = startAngle + mAngle;
                }

                //最后绘制圆环
                Rect mDestRect = new Rect(0, 0, mWidth, mWidth);
                canvas.drawBitmap(mYuanHuan, null, mDestRect, mPaint);
            } else {
                //大圆盘
                Rect mDestRect = new Rect(0, 0, mWidth, mWidth);
                canvas.drawBitmap(mMain, null, mDestRect, mPaint);
            }
        }
    }

    //绘制文字
    private void drawText(float startAngle, String string, int radius, Paint textPaint, Canvas canvas) {
        //创建绘制路径
        Path circlePath = new Path();
        //范围也是整个圆盘
        RectF rect = new RectF(mCenter - radius, mCenter - radius, mCenter
                + radius, mCenter + radius);
        //给定扇形的范围
        circlePath.addArc(rect, startAngle, mAngle);
        String startText = null;
        String endText = null;
        //测量文字的宽度
        float textWidth = textPaint.measureText(string);
        //水平偏移
        int hOffset = (int) (mRadius * 2 * Math.PI / mDeses.length / 2 - textWidth / 2);
        //计算弧长 处理文字过长换行
        int l = (int) ((360 / mDeses.length) * Math.PI * mRadius / 180);
        if (textWidth > l * 4 / 5) {
            int index = string.length() / 2;
            startText = string.substring(0, index);
            endText = string.substring(index, string.length());

            float startTextWidth = textPaint.measureText(startText);
            float endTextWidth = textPaint.measureText(endText);
            //水平偏移
            hOffset = (int) (mRadius * 2 * Math.PI /  mDeses.length / 2 - startTextWidth / 2);
            int endHOffset = (int) (mRadius * 2 * Math.PI /  mDeses.length / 2 - endTextWidth / 2);
            //文字高度
            int h = (int) ((textPaint.ascent() + textPaint.descent()) * 1.5);

            //根据路径绘制文字
            //hOffset 水平的偏移量 vOffset 垂直的偏移量
            canvas.drawTextOnPath(startText, circlePath, hOffset, mRadius / 3, textPaint);
            canvas.drawTextOnPath(endText, circlePath, endHOffset, mRadius / 3 - h, textPaint);
        } else {
            //根据路径绘制文字
            canvas.drawTextOnPath(string, circlePath, hOffset, mRadius /3, textPaint);
        }
    }

    //再一次onDraw
    public void show() {
        //做最后的准备工作 检查数据是否合理
        if ( mType == 1 ) {
            if ( mHuanImgRes == null || mHuanImgRes == 0 )
                mYuanHuan = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.yuanhuan);
            else {
                mYuanHuan = BitmapFactory.decodeResource(mContext.getResources(), mHuanImgRes);
            }
            //文字颜色 默认粉红色
            if ( mTextColor == 0 )
                mTextColor = Color.parseColor("#ff00ff");

//            if ( mDeses.length != mColors.length ) {
//                throw new RuntimeException("Deses和Colors数量必须与mTypeNum一致");
//            }
        } else {
            //直接大图
            if ( mMainImgRes == null || mMainImgRes == 0 )
                throw new RuntimeException("类型为2必须要传大图mMainImgRes");
            mMain = BitmapFactory.decodeResource(mContext.getResources(), mMainImgRes);
        }

        if ( mTextPaint == null ) {
            //文字画笔
            mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            //设置抗锯齿
            mTextPaint.setAntiAlias(true);
            //设置边界模糊
            mTextPaint.setDither(true);
            //设置画笔颜色
            mTextPaint.setColor(mTextColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTextPaint.setLetterSpacing(0.3f);
            }
            //设置字体大小
            mTextPaint.setTextSize(mTextSize);
        }
        if ( mTypeNum != 0 )
            mAngle = ( float ) (360.0 / mTypeNum);
        if ( mVarTime == 0 )
            mVarTime = 75;

        //重绘
        invalidate();
    }
}
