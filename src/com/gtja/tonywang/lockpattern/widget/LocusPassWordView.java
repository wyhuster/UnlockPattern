package com.gtja.tonywang.lockpattern.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.gtja.tonywang.lockpattern.R;
import com.gtja.tonywang.lockpattern.model.Point;
import com.gtja.tonywang.lockpattern.util.MathUtil;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class LocusPassWordView extends View {
	private float width = 0;
	private float height = 0;

	// 是否已初始化mPoints和各种paint
	private boolean isInit = false;
	// 9个点
	private Point[][] mPoints = new Point[3][3];
	// 外圈的半径,初始化为width/10,内圈半径为dotRadius/4
	private float dotRadius = 0;
	// 选择的点
	private List<Point> sPoints = new ArrayList<Point>();
	// 是否正在画密码，ACTION_DOWN在一个point上时设置为true，ACTION_UP时设置为false
	private boolean checking = false;
	private boolean isTouch = true;

	private Paint arrowPaint;
	private Paint linePaint;
	private Paint selectedPaint;
	private Paint errorPaint;
	private Paint correctPaint;
	private Paint normalPaint;

	private long CLEAR_TIME = 1000; // 清除时间
	private int pwdMaxLen = 9; // 最长密码
	private int pwdMinLen = 4; // 最短密码

	private int dotColor = 0xff929292; // 内圈normal颜色
	private int outterDotColor = 0xffd9d9d9; // 外圈normal颜色
	private int selectedColor = 0xff0596f6;// 内圈select颜色
	private int outterSelectedColor = 0xff8cbad8;// 外圈select颜色
	private int errorColor = 0xffea0945;// 内圈error颜色
	private int outterErrorColor = 0xff901032; // 外圈error颜色
	private int correctColor = 0xff33ff33;// 内圈correct颜色
	private int outterCorrectColor = 0xff33ff99; // 外圈correct颜色

	public LocusPassWordView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		getAttrs(context, attrs);
	}

	public LocusPassWordView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getAttrs(context, attrs);
	}

	public LocusPassWordView(Context context) {
		super(context);
	}

	/**
	 * 获取自定义属性值
	 * 
	 * @param context
	 * @param attrs
	 */
	private void getAttrs(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.unlock_pattern);
		pwdMinLen = a
				.getInteger(R.styleable.unlock_pattern_password_min_len, 4);
		pwdMaxLen = a
				.getInteger(R.styleable.unlock_pattern_password_max_len, 9);

		CLEAR_TIME = a.getInteger(R.styleable.unlock_pattern_clear_time, 1000);

		dotColor = a.getColor(R.styleable.unlock_pattern_inner_normal_color,
				0xff929292);
		outterDotColor = a.getColor(
				R.styleable.unlock_pattern_outer_normal_color, 0xffd9d9d9);
		selectedColor = a.getColor(
				R.styleable.unlock_pattern_inner_select_color, 0xff0596f6);
		outterSelectedColor = a.getColor(
				R.styleable.unlock_pattern_outer_select_color, 0xff8cbad8);
		errorColor = a.getColor(R.styleable.unlock_pattern_inner_error_color,
				0xffea0945);
		outterErrorColor = a.getColor(
				R.styleable.unlock_pattern_outer_error_color, 0xff901032);
		correctColor = a.getColor(
				R.styleable.unlock_pattern_inner_correct_color, 0xff33ff33);
		outterCorrectColor = a.getColor(
				R.styleable.unlock_pattern_outer_correct_color, 0xff33ff99);

		a.recycle();
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (!isInit) {
			// 初始化9个点 mPoints数组（x,y,index），
			// 初始化外圈半径dotRadius为width/10，
			initPointsAndRadius();
			// 初始化各种paint（三种状态的圈paint，线paint，箭头paint）
			initPaints();
			isInit = true;
		}
		drawToCanvas(canvas);
	}

	private void drawToCanvas(Canvas canvas) {
		// 线和箭头的paint默认颜色
		int line_color = selectedColor;
		// 画内外圈
		for (int i = 0; i < mPoints.length; i++) {
			for (int j = 0; j < mPoints[i].length; j++) {
				Point p = mPoints[i][j];
				switch (p.state) {
				case Point.STATE_CHECK:
					selectedPaint.setColor(outterSelectedColor);
					canvas.drawCircle(p.x, p.y, dotRadius, selectedPaint);
					selectedPaint.setColor(selectedColor);
					canvas.drawCircle(p.x, p.y, dotRadius / 4, selectedPaint);
					break;
				case Point.STATE_CHECK_ERROR:
					errorPaint.setColor(outterErrorColor);
					canvas.drawCircle(p.x, p.y, dotRadius, errorPaint);
					errorPaint.setColor(errorColor);
					canvas.drawCircle(p.x, p.y, dotRadius / 4, errorPaint);

					// 线和箭头的paint换成错误颜色
					line_color = errorColor;
					break;
				case Point.STATE_CHECK_CORRECT:
					correctPaint.setColor(outterCorrectColor);
					canvas.drawCircle(p.x, p.y, dotRadius, correctPaint);
					correctPaint.setColor(correctColor);
					canvas.drawCircle(p.x, p.y, dotRadius / 4, correctPaint);

					// 线和箭头的paint换成错误颜色
					line_color = correctColor;
					break;
				default:
					normalPaint.setColor(outterDotColor);
					canvas.drawCircle(p.x, p.y, dotRadius, normalPaint);
					normalPaint.setColor(dotColor);
					canvas.drawCircle(p.x, p.y, dotRadius / 4, normalPaint);
					break;
				}

			}
		}

		arrowPaint.setColor(line_color);
		linePaint.setColor(line_color);

		// 画连接线和箭头
		if (sPoints.size() > 0) {
			// 依次遍历选择的点，连续的两个点之间画连接线和箭头
			Point tp = sPoints.get(0);
			for (int i = 1; i < sPoints.size(); i++) {
				Point p = sPoints.get(i);
				drawLine(tp, p, canvas, linePaint);
				drawArrow(canvas, arrowPaint, tp, p, dotRadius / 4, 38);
				tp = p;
			}
			// 画线过程中没有碰到点，只画线，不画箭头
			if (this.movingNoPoint) {
				drawLine(tp, new Point(moveingX, moveingY, -1), canvas,
						linePaint);
			}
		}

	}

	private void drawLine(Point start, Point end, Canvas canvas, Paint paint) {
		double d = MathUtil.distance(start.x, start.y, end.x, end.y);
		float rx = (float) ((end.x - start.x) * dotRadius / 4 / d);
		float ry = (float) ((end.y - start.y) * dotRadius / 4 / d);
		canvas.drawLine(start.x + rx, start.y + ry, end.x - rx, end.y - ry,
				paint);
	}

	private void drawArrow(Canvas canvas, Paint paint, Point start, Point end,
			float arrowHeight, int angle) {
		double d = MathUtil.distance(start.x, start.y, end.x, end.y);
		float sin_B = (float) ((end.x - start.x) / d);
		float cos_B = (float) ((end.y - start.y) / d);
		float tan_A = (float) Math.tan(Math.toRadians(angle));
		float h = (float) (d - arrowHeight - dotRadius * 1.1);
		float l = arrowHeight * tan_A;
		float a = l * sin_B;
		float b = l * cos_B;
		float x0 = h * sin_B;
		float y0 = h * cos_B;
		float x1 = start.x + (h + arrowHeight) * sin_B;
		float y1 = start.y + (h + arrowHeight) * cos_B;
		float x2 = start.x + x0 - b;
		float y2 = start.y + y0 + a;
		float x3 = start.x + x0 + b;
		float y3 = start.y + y0 - a;
		Path path = new Path();
		path.moveTo(x1, y1);
		path.lineTo(x2, y2);
		path.lineTo(x3, y3);
		path.close();
		canvas.drawPath(path, paint);
	}

	private void initPointsAndRadius() {
		width = this.getWidth();
		height = this.getHeight();
		float x = 0;
		float y = 0;

		if (width > height) {
			x = (width - height) / 2;
			width = height;
		} else {
			y = (height - width) / 2;
			height = width;
		}

		int leftPadding = 15;
		float dotPadding = width / 3 - leftPadding;
		float middleX = width / 2;
		float middleY = height / 2;

		mPoints[0][0] = new Point(x + middleX - dotPadding, y + middleY
				- dotPadding, 1);
		mPoints[0][1] = new Point(x + middleX, y + middleY - dotPadding, 2);
		mPoints[0][2] = new Point(x + middleX + dotPadding, y + middleY
				- dotPadding, 3);
		mPoints[1][0] = new Point(x + middleX - dotPadding, y + middleY, 4);
		mPoints[1][1] = new Point(x + middleX, y + middleY, 5);
		mPoints[1][2] = new Point(x + middleX + dotPadding, y + middleY, 6);
		mPoints[2][0] = new Point(x + middleX - dotPadding, y + middleY
				+ dotPadding, 7);
		mPoints[2][1] = new Point(x + middleX, y + middleY + dotPadding, 8);
		mPoints[2][2] = new Point(x + middleX + dotPadding, y + middleY
				+ dotPadding, 9);

		Log.d("jerome", "canvas width:" + width);
		dotRadius = width / 10;
	}

	private void initPaints() {
		arrowPaint = new Paint();
		arrowPaint.setColor(selectedColor);
		arrowPaint.setStyle(Style.FILL);
		arrowPaint.setAntiAlias(true);

		linePaint = new Paint();
		linePaint.setColor(selectedColor);
		linePaint.setStyle(Style.STROKE);
		linePaint.setAntiAlias(true);
		linePaint.setStrokeWidth(dotRadius / 9);

		selectedPaint = new Paint();
		selectedPaint.setStyle(Style.STROKE);
		selectedPaint.setAntiAlias(true);
		selectedPaint.setStrokeWidth(dotRadius / 6);

		errorPaint = new Paint();
		errorPaint.setStyle(Style.STROKE);
		errorPaint.setAntiAlias(true);
		errorPaint.setStrokeWidth(dotRadius / 6);

		correctPaint = new Paint();
		correctPaint.setStyle(Style.STROKE);
		correctPaint.setAntiAlias(true);
		correctPaint.setStrokeWidth(dotRadius / 6);

		normalPaint = new Paint();
		normalPaint.setStyle(Style.STROKE);
		normalPaint.setAntiAlias(true);
		normalPaint.setStrokeWidth(dotRadius / 9);
	}

	/**
	 * 根据touch坐标，判断点击的是哪个point
	 * 
	 * @param x
	 * @param y
	 * @return 点击的point
	 */
	private Point checkSelectPoint(float x, float y) {
		for (int i = 0; i < mPoints.length; i++) {
			for (int j = 0; j < mPoints[i].length; j++) {
				Point p = mPoints[i][j];
				if (MathUtil
						.checkInRound(p.x, p.y, dotRadius, (int) x, (int) y)) {
					return p;
				}
			}
		}
		return null;
	}

	/**
	 * 清空已选择点sPoints，设置isTouch为true
	 */
	private void reset() {
		for (Point p : sPoints) {
			p.state = Point.STATE_NORMAL;
		}
		sPoints.clear();
		enableTouch();
	}

	/**
	 * 判断当前touch的点p是不是重复的点
	 * 
	 * @param p
	 * @return
	 */
	private int crossPoint(Point p) {
		// reset
		if (sPoints.contains(p)) {
			if (sPoints.get(sPoints.size() - 1).index != p.index) {
				// 当前点p在已选择的点中，但不是已选择的最后一个点
				// 需要画多余的无箭头的线
				return 2;
			} else {
				// 且当前点p在已选择的点中，且是已选择的最后一个点
				// 不需要画多余的线
				return 1;
			}
		} else {
			// 当前点p不在已选择的点中,没有cross
			// 需要添加点p到已选择的点中，不需要画多余的线，会画两点之间的线
			return 0;
		}
	}

	/**
	 * 添加点到已选择点sPoints中
	 * 
	 * @param point
	 */
	private void addPoint(Point point) {
		if (sPoints.size() > 0) {
			Point lastPoint = sPoints.get(sPoints.size() - 1);
			int dx = Math.abs(lastPoint.getColNum() - point.getColNum());
			int dy = Math.abs(lastPoint.getRowNum() - point.getRowNum());
			if ((dx > 1 || dy > 1) && (dx == 0 || dy == 0 || dx == dy)) {
				// if ((dx > 1 || dy > 1) && (dx != 2 * dy) && (dy != 2 * dx)) {
				int middleIndex = (point.index + lastPoint.index) / 2 - 1;
				Point middlePoint = mPoints[middleIndex / 3][middleIndex % 3];
				if (middlePoint.state != Point.STATE_CHECK) {
					middlePoint.state = Point.STATE_CHECK;
					sPoints.add(middlePoint);
				}
			}
		}
		sPoints.add(point);
	}

	/**
	 * 根据选择的点，得到密码字符串
	 * 
	 * @param points
	 * @return
	 */
	private String toPointString() {
		if (sPoints.size() >= pwdMinLen && sPoints.size() <= pwdMaxLen) {
			StringBuffer sf = new StringBuffer();
			for (Point p : sPoints) {
				sf.append(p.index);
			}
			return sf.toString();
		} else {
			return "";
		}
	}

	// 移动过程中是否碰到点，用于在画线的时候判断，是否需要画箭头
	boolean movingNoPoint = false;
	// 当前移动的坐标,onDraw里面用到
	float moveingX, moveingY;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 判断是否允许touch
		if (!isTouch) {
			return false;
		}

		movingNoPoint = false;

		float ex = event.getX();
		float ey = event.getY();
		boolean isFinish = false;
		Point p = null;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// 重新画连线
			// 取消task任务，1s后连线消失的调度任务
			if (task != null) {
				task.cancel();
				task = null;
				// Log.d("task", "task cancel()");
			}
			// 清空选择的点
			reset();
			// 获取到当前点击的点
			p = checkSelectPoint(ex, ey);
			if (p != null) {
				// 开始画密码
				checking = true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (checking) {
				// 移动过程中touch的点
				p = checkSelectPoint(ex, ey);
				if (p == null) {
					movingNoPoint = true;
					moveingX = ex;
					moveingY = ey;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			p = checkSelectPoint(ex, ey);
			// 停止画线
			checking = false;
			isFinish = true;
			break;
		}
		// 画线过程中,判断是否需要画多余的线,是否选择了新点
		if (!isFinish && checking && p != null) {

			int rk = crossPoint(p);
			if (rk == 2) {
				// 当前点p在已选择的点中，但不是已选择的最后一个点
				// 需要画多余的无箭头的线
				movingNoPoint = true;
				moveingX = ex;
				moveingY = ey;
			} else if (rk == 0) {
				// 当前点p不在已选择的点中,没有cross
				// 需要添加点p到已选择的点中，不需要画多余的线，会画两点之间的线
				p.state = Point.STATE_CHECK;
				addPoint(p);
			}
			// rk == 1

		}

		// TOUCH_UP画线完成
		if (isFinish && sPoints.size() != 0) {
			if (sPoints.size() == 1) {
				// 只选择了一个点，画线未完成
				reset();
			} else if (sPoints.size() < pwdMinLen || sPoints.size() > pwdMaxLen) {
				// 允许的密码长度不符合条件
				mCompleteListener.onPasswordLenError(pwdMinLen, pwdMaxLen);
				error();
				clearPassword();
			} else if (mCompleteListener != null) {
				// 检验密码过程中不允许touch
				disableTouch();
				mCompleteListener.onComplete(toPointString());
			}
		}
		this.postInvalidate();
		return true;
	}

	/**
	 * 密码错误,内部设置所有已选点的state为错误
	 */
	private void error() {
		for (Point p : sPoints) {
			p.state = Point.STATE_CHECK_ERROR;
		}
	}

	/**
	 * 密码错误,外部设置所有已选点的state为错误,并且在{@CLEAR_TIME}时间之后清除密码
	 */
	public void markError() {
		markError(CLEAR_TIME);
	}

	/**
	 * 密码错误,外部设置所有已选点的state为错误,并且在time时间之后清除密码
	 * 
	 * @param time
	 */
	public void markError(final long time) {
		for (Point p : sPoints) {
			p.state = Point.STATE_CHECK_ERROR;
		}
		this.clearPassword(time);
	}

	/**
	 * 密码错误,外部设置所有已选点的state为正确,并且在time时间之后清除密码
	 * 
	 * @param time
	 */
	public void markCorrect(final long time) {
		for (Point p : sPoints) {
			p.state = Point.STATE_CHECK_CORRECT;
		}
		this.clearPassword(time);
	}

	/**
	 * 允许touch
	 */
	public void enableTouch() {
		isTouch = true;
	}

	/**
	 * 不允许touch
	 */
	public void disableTouch() {
		isTouch = false;
	}

	private Timer timer = new Timer();
	private TimerTask task = null;

	/**
	 * 定时{@CLEAR_TIME}之后清空已选择的密码
	 */
	public void clearPassword() {
		clearPassword(CLEAR_TIME);
	}

	/**
	 * 定时time之后清空已选择的密码
	 * 
	 * @param time
	 */
	public void clearPassword(final long time) {
		if (time > 1) {
			if (task != null) {
				task.cancel();
				Log.d("task", "clearPassword cancel()");
			}
			postInvalidate();
			task = new TimerTask() {
				public void run() {
					reset();
					postInvalidate();
				}
			};
			Log.d("task", "clearPassword schedule(" + time + ")");
			timer.schedule(task, time);
		} else {
			reset();
			postInvalidate();
		}

	}

	// 手势密码输入完成监听器
	private OnCompleteListener mCompleteListener;

	/**
	 * 设置手势密码输入完成监听器, 外部调用方法
	 * 
	 * @param mCompleteListener
	 */
	public void setOnCompleteListener(OnCompleteListener mCompleteListener) {
		this.mCompleteListener = mCompleteListener;
	}

	public interface OnCompleteListener {

		/**
		 * 手势密码设置完成
		 * 
		 * @param password
		 */
		public void onComplete(String password);

		/**
		 * 密码长度太长或太短
		 * 
		 * @param min
		 * @param max
		 */
		public void onPasswordLenError(int min, int max);
	}

	/**
	 * 根据密码直接设置图案
	 * 
	 * @param password
	 */
	public void setRecordSelect(String password) {
		if (password == null || password.length() == 0) {
			return;
		}
		int length = password.length();
		int[] nums = new int[length];
		for (int i = 0; i < length; i++) {
			nums[i] = password.charAt(i) - 48;
		}
		for (int index : nums) {
			int col = (index - 1) % 3;
			int row = (index - 1) / 3;
			Point p = mPoints[row][col];
			p.state = Point.STATE_CHECK;
			sPoints.add(p);
		}
		postInvalidate();
	}
}
