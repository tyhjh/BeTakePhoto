package com.example.tyhj.betakephoto;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * Created by _Tyhj on 2016/7/2.
 */
public class AotoFitTextview extends TextureView {
    int mwidth=0;
    int mheight=0;
    public AotoFitTextview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void setAspectRatio(int width,int height){
        mheight=height;
        mwidth=width;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width=MeasureSpec.getSize(widthMeasureSpec);
        int height=MeasureSpec.getSize(heightMeasureSpec);
        if(0==mwidth||0==mheight){
            setMeasuredDimension(width,height);
        }else {
            if(width<height*mwidth/mheight){
                setMeasuredDimension(width,width*mheight/mwidth);
            }else {
                setMeasuredDimension(height*mwidth/mheight,height);
            }
        }
    }
}
