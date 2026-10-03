package qiblaarrow.blackfalcon.jan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.view.View;

/** Black screen, one long arrow with the Kaaba on top, and the details below it. */
public class ArrowView extends View {

    private static final int GOLD = 0xFFC9A227;
    private static final int GREEN = 0xFF3DDC84;
    private static final int GRAY = 0xFF9AA0A6;
    private static final String KAABA = "\uD83D\uDD4B"; // Kaaba emoji

    private final float dp;
    private final Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emojiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint degreesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint infoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint distancePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint coordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint smallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint messagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint creditPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path arrow = new Path();

    private boolean hasLocation;
    private double qiblaBearing;
    private float relative;
    private float heading;
    private String distanceText = "";
    private String coordText = "";
    private String statusText = "";
    private String message = "";

    public ArrowView(Context context) {
        super(context);
        dp = getResources().getDisplayMetrics().density;
        float sp = getResources().getDisplayMetrics().scaledDensity;

        arrowPaint.setStyle(Paint.Style.FILL);

        emojiPaint.setTextAlign(Paint.Align.CENTER);

        labelPaint.setColor(GRAY);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(18 * sp);

        degreesPaint.setColor(Color.WHITE);
        degreesPaint.setTextAlign(Paint.Align.CENTER);
        degreesPaint.setTypeface(Typeface.DEFAULT_BOLD);
        degreesPaint.setTextSize(52 * sp);

        infoPaint.setTextAlign(Paint.Align.CENTER);
        infoPaint.setTypeface(Typeface.DEFAULT_BOLD);
        infoPaint.setTextSize(18 * sp);

        distancePaint.setColor(Color.WHITE);
        distancePaint.setTextAlign(Paint.Align.CENTER);
        distancePaint.setTypeface(Typeface.DEFAULT_BOLD);
        distancePaint.setTextSize(18 * sp);

        coordPaint.setColor(0xFFD0D4D9);
        coordPaint.setTextAlign(Paint.Align.CENTER);
        coordPaint.setTextSize(14 * sp);

        smallPaint.setColor(GRAY);
        smallPaint.setTextAlign(Paint.Align.CENTER);
        smallPaint.setTextSize(12 * sp);

        messagePaint.setColor(Color.WHITE);
        messagePaint.setTextAlign(Paint.Align.CENTER);
        messagePaint.setTextSize(17 * sp);

        creditPaint.setColor(GOLD);
        creditPaint.setTextAlign(Paint.Align.RIGHT);
        creditPaint.setTypeface(Typeface.DEFAULT_BOLD);
        creditPaint.setTextSize(15 * sp);
    }

    /** rel = angle you must turn to face the Qibla (positive = right). */
    public void setState(boolean hasLocation, double qiblaBearing, float rel, float heading,
                         String distanceText, String coordText, String statusText, String message) {
        this.hasLocation = hasLocation;
        this.qiblaBearing = qiblaBearing;
        this.relative = rel;
        this.heading = heading;
        this.distanceText = distanceText == null ? "" : distanceText;
        this.coordText = coordText == null ? "" : coordText;
        this.statusText = statusText == null ? "" : statusText;
        this.message = message == null ? "" : message;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        canvas.drawColor(Color.BLACK);

        // mandatory credit, top-right corner
        canvas.drawText("By: Black Falcon", w - 20 * dp, 28 * dp + creditPaint.getTextSize(), creditPaint);

        float cx = w / 2f;
        float cy = h * 0.33f;
        boolean aligned = hasLocation && Math.abs(relative) <= 3f;

        if (hasLocation) {
            // long arrow, tip at the top, rotated about its centre; the Kaaba sits on the tip
            float length = Math.min(w * 0.64f, h * 0.40f);
            float half = length / 2f;
            float headLen = length * 0.28f;
            float headHalfW = length * 0.16f;
            float shaftHalfW = length * 0.045f;

            arrow.reset();
            arrow.moveTo(0f, -half);
            arrow.lineTo(headHalfW, -half + headLen);
            arrow.lineTo(shaftHalfW, -half + headLen);
            arrow.lineTo(shaftHalfW, half);
            arrow.lineTo(-shaftHalfW, half);
            arrow.lineTo(-shaftHalfW, -half + headLen);
            arrow.lineTo(-headHalfW, -half + headLen);
            arrow.close();

            arrowPaint.setColor(aligned ? GREEN : Color.WHITE);
            canvas.save();
            canvas.translate(cx, cy);
            canvas.rotate(relative);
            canvas.drawPath(arrow, arrowPaint);

            // Kaaba emoji above the tip, kept upright
            float emojiSize = length * 0.22f;
            emojiPaint.setTextSize(emojiSize);
            canvas.translate(0f, -(half + emojiSize * 0.62f));
            canvas.rotate(-relative);
            Paint.FontMetrics fm = emojiPaint.getFontMetrics();
            canvas.drawText(KAABA, 0f, -(fm.ascent + fm.descent) / 2f, emojiPaint);
            canvas.restore();
        } else {
            float y = cy;
            for (String line : message.split("\n")) {
                canvas.drawText(line, cx, y, messagePaint);
                y += messagePaint.getTextSize() * 1.5f;
            }
        }

        // details below the arrow
        canvas.drawText("Qibla Direction", cx, h * 0.675f, labelPaint);
        String deg = hasLocation ? (Math.round(qiblaBearing) % 360) + "\u00B0" : "--\u00B0";
        canvas.drawText(deg, cx, h * 0.75f, degreesPaint);

        if (hasLocation) {
            int a = Math.round(Math.abs(relative));
            String info = aligned ? "Facing the Qibla" : "Turn " + a + "\u00B0 " + (relative > 0 ? "right" : "left");
            infoPaint.setColor(aligned ? GREEN : GOLD);
            canvas.drawText(info, cx, h * 0.795f, infoPaint);
            canvas.drawText("Distance to Kaaba: " + distanceText, cx, h * 0.845f, distancePaint);
            canvas.drawText(coordText, cx, h * 0.885f, coordPaint);
            canvas.drawText("Heading " + (Math.round(heading) % 360) + "\u00B0  \u2022  " + statusText,
                    cx, h * 0.92f, smallPaint);
        } else {
            canvas.drawText(statusText, cx, h * 0.845f, smallPaint);
        }
    }
}
