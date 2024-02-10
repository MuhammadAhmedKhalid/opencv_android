package opencv.util;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class CVProcessor {

    public static Point[] sortPoints(Point[] src) {

        ArrayList<Point> srcPoints = new ArrayList<>(Arrays.asList(src));

        Point[] result = {null, null, null, null};

        Comparator<Point> sumComparator = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            sumComparator = Comparator.comparingDouble(lhs -> lhs.y + lhs.x);
        }

        Comparator<Point> diffComparator = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            diffComparator = Comparator.comparingDouble(lhs -> lhs.y - lhs.x);
        }

        // top-left corner = minimal sum
        result[0] = Collections.min(srcPoints, sumComparator);

        // bottom-right corner = maximal sum
        result[2] = Collections.max(srcPoints, sumComparator);

        // top-right corner = minimal difference
        result[1] = Collections.min(srcPoints, diffComparator);

        // bottom-left corner = maximal difference
        result[3] = Collections.max(srcPoints, diffComparator);

        return result;
    }

    public static Mat fourPointTransform(Mat src, Point[] pts) {
        Point tl = pts[0];
        Point tr = pts[1];
        Point br = pts[2];
        Point bl = pts[3];

        double widthA = Math.sqrt(Math.pow(br.x - bl.x, 2) + Math.pow(br.y - bl.y, 2));
        double widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2) + Math.pow(tr.y - tl.y, 2));

        double dw = Math.max(widthA, widthB);
        int maxWidth = Double.valueOf(dw).intValue();


        double heightA = Math.sqrt(Math.pow(tr.x - br.x, 2) + Math.pow(tr.y - br.y, 2));
        double heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2) + Math.pow(tl.y - bl.y, 2));

        double dh = Math.max(heightA, heightB);
        int maxHeight = Double.valueOf(dh).intValue();

        Mat doc = new Mat(maxHeight, maxWidth, CvType.CV_8UC4);

        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        src_mat.put(0, 0, tl.x, tl.y, tr.x, tr.y, br.x, br.y, bl.x, bl.y);
        dst_mat.put(0, 0, 0.0, 0.0, dw, 0.0, dw, dh, 0.0, dh);

        Mat m = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        Imgproc.warpPerspective(src, doc, m, doc.size());

        return doc;
    }

    public static Mat adjustBrightnessAndContrast(Mat src, double clipPercentage) {
        int histSize = 256;
        double alpha, beta;
        double minGray, maxGray;

        Mat gray;
        if (src.type() == CvType.CV_8UC1) {
            gray = src.clone();
        } else {
            gray = new Mat();
            Imgproc.cvtColor(src, gray, src.type() == CvType.CV_8UC3 ? Imgproc.COLOR_RGB2GRAY : Imgproc.COLOR_RGBA2GRAY);
        }

        if (clipPercentage == 0) {
            Core.MinMaxLocResult minMaxGray = Core.minMaxLoc(gray);
            minGray = minMaxGray.minVal;
            maxGray = minMaxGray.maxVal;
        } else {
            Mat hist = new Mat();
            MatOfInt size = new MatOfInt(histSize);
            MatOfInt channels = new MatOfInt(0);
            MatOfFloat ranges = new MatOfFloat(0, 256);
            Imgproc.calcHist(Collections.singletonList(gray), channels, new Mat(), hist, size, ranges, false);
            gray.release();

            double[] accumulator = new double[histSize];

            accumulator[0] = hist.get(0, 0)[0];
            for (int i = 1; i < histSize; i++) {
                accumulator[i] = accumulator[i - 1] + hist.get(i, 0)[0];
            }

            hist.release();

            double max = accumulator[accumulator.length - 1];
            clipPercentage = (clipPercentage * (max / 100.0));
            clipPercentage = clipPercentage / 2.0f;

            minGray = 0;
            while (minGray < histSize && accumulator[(int) minGray] < clipPercentage) {
                minGray++;
            }

            maxGray = histSize - 1;
            while (maxGray >= 0 && accumulator[(int) maxGray] >= (max - clipPercentage)) {
                maxGray--;
            }
        }

        double inputRange = maxGray - minGray;
        alpha = (histSize - 1) / inputRange;
        beta = -minGray * alpha;

        Mat result = new Mat();
        src.convertTo(result, -1, alpha, beta);

        if (result.type() == CvType.CV_8UC4) {
            Core.mixChannels(Collections.singletonList(src), Collections.singletonList(result), new MatOfInt(3, 3));
        }

        return result;
    }

    public static Mat sharpenImage(Mat src) {
        Mat sharped = new Mat();
        Imgproc.GaussianBlur(src, sharped, new Size(0, 0), 3);
        Core.addWeighted(src, 1.5, sharped, -0.5, 0, sharped);

        return sharped;
    }


}
