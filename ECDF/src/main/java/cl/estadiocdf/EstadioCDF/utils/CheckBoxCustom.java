package cl.estadiocdf.EstadioCDF.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;
import android.widget.CheckBox;

import cl.estadiocdf.EstadioCDF.R;

/**
 * Created by Esteban- on 24-06-14.
 */
public class CheckBoxCustom extends CheckBox {


    public CheckBoxCustom(Context context) {

        super(context);
    }

    public CheckBoxCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckBoxCustom(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void setChecked(boolean checked) {
        if(checked){

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.check_on);

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int newWidth = 27;
            int newHeight = 27;

            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;

            this.playSoundEffect(SoundEffectConstants.CLICK);
            Matrix matrix = new Matrix();

            matrix.postScale(scaleWidth, scaleHeight);

            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    width, height, matrix, true);

            Drawable drawable = new BitmapDrawable(resizedBitmap); ;
            this.setButtonDrawable(drawable);
        }
        else{
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.check_off);

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int newWidth = 27;
            int newHeight = 27;

            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;


            Matrix matrix = new Matrix();

            matrix.postScale(scaleWidth, scaleHeight);
            this.playSoundEffect(SoundEffectConstants.CLICK);
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    width, height, matrix, true);

            Drawable drawable = new BitmapDrawable(resizedBitmap); ;
            this.setButtonDrawable(drawable);
        }
        super.setChecked(checked);
    }


    @Override
    public void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
        super.setSoundEffectsEnabled(soundEffectsEnabled);
    }
}
