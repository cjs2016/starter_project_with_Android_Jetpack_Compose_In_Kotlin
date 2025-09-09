package kr.cjs.catty;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.view.MotionEvent;

public class MyCanvasView extends View{

    public enum Shape {
        RECTANGLE,
        CIRCLE,
        NONE
    }


    private Paint textPaint;
    private Paint paint;
    private Bitmap myBitmap;
    private RectF redRect;
    private float circleCenterX, circleCenterY, circleRadius;
    private Shape draggedShape = Shape.NONE;
    private float lastTouchX;
    private float lastTouchY;



    public MyCanvasView(Context context){
        super(context);
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(50f);
        paint = new Paint();


        redRect = new RectF(500,400,600,600);
        circleCenterX = 450;
        circleCenterY = 125;
        circleRadius = 75;
    }


    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
//        String textToDraw = "Hello, Android!";
//        canvas.drawText(textToDraw, 100,100,textPaint);

        paint.setColor(Color.RED);
        canvas.drawRect(redRect,paint);

        paint.setColor(Color.BLUE);
        canvas.drawCircle(circleCenterX,circleCenterY,circleRadius,paint);

//        paint.setColor(Color.GREEN);
//        paint.setStrokeWidth(10);
//        canvas.drawLine(50,250,550,250,paint);
//
//        if(myBitmap != null){
//            canvas.drawBitmap(myBitmap,550,550,null);
//        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

            float x = event.getX();
            float y = event.getY();

            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    if (redRect.contains(x,y)){
                        draggedShape = Shape.RECTANGLE;
                    } else {
                        double dx = x - circleCenterX;
                        double dy = y - circleCenterY;
                        double distance = Math.sqrt(dx * dx + dy * dy);
                        if (distance < circleRadius * circleRadius) {
                            draggedShape = Shape.CIRCLE;
                        }
                    }

                    lastTouchX = x;
                    lastTouchY = y;
                    return (draggedShape != Shape.NONE);


                case MotionEvent.ACTION_MOVE:
                    if(draggedShape != Shape.NONE){
                        float dx = x - lastTouchX;
                        float dy = y - lastTouchY;

                        if(draggedShape == Shape.RECTANGLE){
                            redRect.offset(dx,dy);
                        } else if (draggedShape == Shape.CIRCLE){
                            circleCenterX += dx;
                            circleCenterY += dy;
                        }

                        lastTouchX = x;
                        lastTouchY = y;
                        invalidate();

                    }
                    break;

                case MotionEvent.ACTION_UP:
                    draggedShape = Shape.NONE;
                    break;

        }

        return super.onTouchEvent(event);
    }

}
