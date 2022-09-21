package com.xiaoyun.golfholedemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import java.util.ArrayList;
import java.util.List;

public class GolfHoleShapeView extends AppCompatImageView {

    public GolfHoleShapeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //clipPath绘制方式
        canvas.clipOutPath(getGolfPath());
        super.onDraw(canvas);
    }

    /**
     * 画心形曲线
     */
    private Path getHeartPath() {
        int n = 100;
        // 计算缩放比例
        float scale = getWidth() / 17f / 2f;
        // 将360度平均分为 100份，每个弧度对应一个点
        float interval = (float) (2 * Math.PI / 100);
        // 定义初始弧度degree
        float degree = 0;
        Point[] points = new Point[n];
        for (int i = 0; i < n; i++) {
            // 根据心形曲线公式，计算出每个弧度对应的点的坐标
            // 当degree = 90度的时候，x取最大值16。当degree = 180度的时候，y取最小值 -17。
            // 即保证y * scale * 17 * 2 = height的时候，曲线的与控件相切，以此计算出scale的值
            float x = (float) ((16 * Math.pow(Math.sin(degree), 3)) * scale);
            float y = (float) ((13 * Math.cos(degree) - 5 * Math.cos(2 * degree) - 2 * Math.cos(3 * degree) - Math.cos(4 * degree)) * scale);
            points[i] = new Point(x + getWidth() / 2f, -y + getHeight() / 2f);
            degree = degree + interval;
        }
        // 连线
        Path path = new Path();
        path.moveTo(points[0].x , points[0].y);
        for (int i = 1; i < n; i++) {
            path.lineTo(points[i].x, points[i].y );
        }
        path.close();
        return path;
    }

    private Path getGolfPath() {
        float radius = getWidth()/8f;
        List<Point> points = new ArrayList<>();
        Point pointFairWayTop = new Point(radius, 0);
        Point pointGreenTop = new Point(7 * radius, 0);
        Point pointGreenBottom = new Point(7 * radius, 2 * radius);
        Point pointFairWayRightBottom = new Point(2 * radius, 2 * radius);
        Point pointFairWayLeft = new Point(0, radius);
        Point pointTeeBoxLeft = new Point(0, 7 * radius);
        Point pointTeeBoxRight = new Point(2 * radius, 7 * radius);
        Path path = new Path();
        path.moveTo(pointFairWayTop.x+30, pointFairWayTop.y+30);
        path.arcTo(new RectF(6 * radius-10, 30, 8 * radius-10, 2 * radius+30), 270, 180);
        path.lineTo(pointFairWayRightBottom.x+30, pointFairWayRightBottom.y+30 );
        path.arcTo(new RectF(30, 6 * radius+30, 2 * radius+30 , 8 * radius+30), 0, 180);
        path.lineTo(pointFairWayLeft.x+30, pointFairWayLeft.y+30);
        path.arcTo(new RectF(30, 30, 2 * radius+30 , 2 * radius+30 ), 180, 90);
        path.close();
        return path;
    }

    public static class Point {
        float x;
        float y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}