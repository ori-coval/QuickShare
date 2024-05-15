package com.example.quickshare.shareReceiveFile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.content.res.AppCompatResources;

import com.example.quickshare.R;

public class ConnectingAnimationView extends View {
    private String connectingText = "Connecting";
    private int dotCount = 0;
    private final Handler handler;
    private boolean isAnimating = false;
    private final Drawable bubbleBackground;

    public ConnectingAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        handler = new Handler(context.getMainLooper());

        // Load the bubble background drawable
        bubbleBackground =  AppCompatResources.getDrawable(context, R.drawable.bubble_background);//TODO test
    }

    public void startAnimation() {
        if (!isAnimating) {
            isAnimating = true;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    updateText();
                    invalidate(); // Force redraw
                    if (isAnimating) {
                        handler.postDelayed(this, 500); // Repeat every 500ms
                    }
                }
            });
        }
    }

    private void updateText() {
        dotCount = (dotCount + 1) % 4;
        String dots = ".".repeat(dotCount);
        connectingText = "Connecting" + dots;
    }

    public void stopAnimation() {
        isAnimating = false;
        invalidate(); // Force redraw to clear the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw only if animation is running
        if (isAnimating) {
            int width = getWidth();
            int height = getHeight();

            // Calculate text bounds
            Paint textPaint = new Paint();
            textPaint.setTextSize(150f);
            Rect textBounds = new Rect();
            textPaint.getTextBounds(connectingText, 0, connectingText.length(), textBounds);

            // Calculate bubble bounds
            int bubblePadding = 60; // Adjust padding as desired
            int bubbleWidth = textBounds.width() + bubblePadding * 2;
            int bubbleHeight = textBounds.height() + bubblePadding * 2;
            int bubbleLeft = (width - bubbleWidth) / 2;
            int bubbleTop = (height - bubbleHeight) / 2;

            // Draw bubble background
            bubbleBackground.setBounds(bubbleLeft, bubbleTop, bubbleLeft + bubbleWidth, bubbleTop + bubbleHeight);
            bubbleBackground.draw(canvas);

            // Draw text
            float x = (width - textBounds.width()) / 2f;
            float y = (height + textBounds.height()) / 2f;
            canvas.drawText(connectingText, x, y, textPaint);
        }
    }
}
