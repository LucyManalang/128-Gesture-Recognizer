package comp128.gestureRecognizer;

import edu.macalester.graphics.Point;

import java.util.Deque;
import java.util.Iterator;
import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Recognizer to recognize 2D gestures. Uses the $1 gesture recognition algorithm.
 */
public class Recognizer {
    private ArrayList<Template> templates;
    private Iterator<Point>  dequeIterator;
    private Deque<Point> outputDeque;
    private double width;
    private double height;
    private int size;
    private int scale;

    /**
     * Constructs a recognizer object
     */
    public Recognizer() {
        templates = new ArrayList<Template>();
        scale = 250;
        size = 64;
    }


    /**
     * Create a template to use for matching
     * 
     * @param name of the template
     * @param points in the template gesture's path
     */
    public void addTemplate(String name, Deque<Point> path) {
        path = resamplePoints(path, size);
        path = rotatePoints(path);
        path = squarePoints(path);
        templates.add(new Template(name, path));
    }


    /**
     * Recognizes the path against all current templates
     * 
     * @param path the path
     * @return the template object with the best score
     */
    public Template recognize(Deque<Point> path) {
        double bestScore = 0;
        double tempScore;
        double avgDistance;
        double avgDistance1;
        Deque<Point> template;
        Template bestTemplate = templates.get(0);

        path = resamplePoints(path, size);
        path = rotatePoints(path);
        path = squarePoints(path);

        for (Template i : templates) {
            template = i.getPath();
            avgDistance = distanceAtBestAngle(template, path);
            avgDistance1 = distanceAtBestAngle(i.getReversePath(), path); // checks the path going both directions to increase accuracy
            if (avgDistance > avgDistance1) {
                avgDistance = avgDistance1;
            }
            tempScore = 1 - avgDistance / (0.5 * Math.sqrt(2 * Math.pow(scale, 2)));
            i.setScore(tempScore);
            if (tempScore > bestScore) {
                bestScore = tempScore;
                bestTemplate = i;
            }
        }
        return bestTemplate;
    }


    /**
     * Scales the path into a square and moves the center to the origin
     * 
     * @param inputDeque resampled and rotated path
     * @return squared and translated path
     */
    public Deque<Point> squarePoints(Deque<Point> inputDeque) {
        outputDeque = new ArrayDeque<>(size);
        Point origin = new Point(0, 0);

        outputDeque = scaleTo(inputDeque, scale);
        outputDeque = translateTo(outputDeque, origin);
        return outputDeque;
    }

    /**
     * Squares and scales the path to a specified scale
     * 
     * @param inputDeque the path
     * @param scale scaling value
     * @return scaled and squared path
     */
    public Deque<Point> scaleTo(Deque<Point> inputDeque, int scale) {
        outputDeque = new ArrayDeque<>(size);
        this.scale = scale;
        Point tempPoint;
        boundingBox(inputDeque);
        dequeIterator = inputDeque.iterator();

        while (dequeIterator.hasNext()) {
            tempPoint = dequeIterator.next();
            outputDeque.addLast(new Point(tempPoint.getX() * scale / width, tempPoint.getY() * scale / height));
        }
        return outputDeque;
    }


    /**
     * Translates the path to a specified point
     * 
     * @param inputDeque the path
     * @param translationPoint the new center point
     * @return path centered around translator
     */
    public Deque<Point> translateTo(Deque<Point> inputDeque, Point translationPoint) {
        outputDeque = new ArrayDeque<>(size);
        Point tempPoint;
        Point centerPoint = getCentroid(inputDeque);
        dequeIterator = inputDeque.iterator();

        while (dequeIterator.hasNext()) {
            tempPoint = dequeIterator.next();
            outputDeque.addLast(new Point(tempPoint.getX() + translationPoint.getX() - centerPoint.getX(),
                tempPoint.getY() + translationPoint.getY() - centerPoint.getY()));
        }
        return outputDeque;
    }




    /**
     * Rotates path to the indicative angle around the centroid
     * 
     * @param inputDeque resampled path
     * @return rotated path
     */
    public Deque<Point> rotatePoints(Deque<Point> inputDeque) {
        outputDeque = new ArrayDeque<>(size);
        double angle = indicativeAngle(inputDeque);

        outputDeque = rotateBy(inputDeque, angle);
        return outputDeque;
    }


    /**
     * Rotates path by angle around the centroid
     * 
     * @param inputDeque resampled path
     * @param angle angle
     * @return rotated path
     */
    public Deque<Point> rotateBy(Deque<Point> inputDeque, double angle) {
        outputDeque = new ArrayDeque<>(size);
        Point tempPoint;
        Point centerPoint = getCentroid(inputDeque);
        dequeIterator = inputDeque.iterator();

        while (dequeIterator.hasNext()) {
            tempPoint = dequeIterator.next().rotate(angle, centerPoint);
            outputDeque.addLast(tempPoint);
        }
        return outputDeque;
    }


    /**
     * Resamples the number of points in path to size
     * 
     * @param inputDeque the path
     * @param size resample size
     * @return resampled path
     */
    public Deque<Point> resamplePoints(Deque<Point> inputDeque, int size) {
        this.size = size;
        outputDeque = new ArrayDeque<>(size);
        double length = pathLength(inputDeque);
        double distUntilNext = length / (size - 1);
        double alpha;
        double currentDistance;
        dequeIterator = inputDeque.iterator();
        Point firstTemp = dequeIterator.next();
        Point nextTemp = dequeIterator.next();

        outputDeque.addLast(firstTemp);
        while (dequeIterator.hasNext()) {
            currentDistance = firstTemp.distance(nextTemp);
            if (currentDistance >= distUntilNext) {
                alpha = distUntilNext / currentDistance;
                firstTemp = Point.interpolate(firstTemp, nextTemp, alpha);
                outputDeque.addLast(firstTemp);
                distUntilNext = length / (size - 1);
            } else {
                distUntilNext -= currentDistance;
                firstTemp = nextTemp;
                nextTemp = dequeIterator.next();
            }
            if (!dequeIterator.hasNext() && outputDeque.size() == size - 1) {
                outputDeque.addLast(nextTemp);
            }
        }
        return outputDeque;
    }


    /**
     * Returns the centroid of the path
     * 
     * @param path the path
     * @return centroid of the path
     */
    public Point getCentroid(Deque<Point> path) {
        double totalX = 0.0;
        double totalY = 0.0;
        size = path.size();
        Point tempPoint;
        dequeIterator = path.iterator();

        while (dequeIterator.hasNext()) {
            tempPoint = dequeIterator.next();
            totalX += tempPoint.getX();
            totalY += tempPoint.getY();
        }
        return new Point(totalX / size, totalY / size);
    }

    /**
     * sets instance variables width and height and returns center point of the box
     * 
     * @param path the path
     * @return canter point of the box
     */
    public Point boundingBox (Deque<Point> path) {
        double maxX;
        double maxY;
        double minX;
        double minY;
        Point tempPoint;
        dequeIterator = path.iterator();

        tempPoint = dequeIterator.next();
        maxX = tempPoint.getX();
        maxY = tempPoint.getY();
        minX = maxX;
        minY = maxY;
        while (dequeIterator.hasNext()) {
            tempPoint = dequeIterator.next();
            if (tempPoint.getX() > maxX)
                maxX = tempPoint.getX();
            if (tempPoint.getY() > maxY)
                maxY = tempPoint.getY();
            if (tempPoint.getX() < minX)
                minX = tempPoint.getX();
            if (tempPoint.getY() < minY)
                minY = tempPoint.getY();
        }
        width = maxX - minX;
        height = maxY - minY;
        return new Point((maxX + minX) / 2, (maxY + minY) / 2);
    }


    /**
     * Returns length of the path
     * 
     * @param path the path
     * @return length of path
     */
    public double pathLength(Deque<Point> path) {
        dequeIterator = path.iterator();
        double length = 0.0;
        Point firstTemp = dequeIterator.next();
        Point nextTemp;

        while (dequeIterator.hasNext()) {
            nextTemp = dequeIterator.next();
            length += firstTemp.distance(nextTemp);
            firstTemp = nextTemp;
        }
        return length;
    }


    /**
     * Returns the indicative angle of the path
     * 
     * @param path the path
     * @return the indicative angle
     */
    public double indicativeAngle(Deque<Point> path) {
        double angle;
        Point tempPoint;
        Point centerPoint = getCentroid(path);

        tempPoint = path.peekFirst();
        angle = -1 * Math.atan2(tempPoint.getY() - centerPoint.getY(), tempPoint.getX() - centerPoint.getX())
            + (Math.PI);
        return angle;
    }


    /**
     * Returns if the arrayList templates is emptyto prevent errors when templates is empty
     */
    public boolean templatesIsEmpty() {
        return (templates.size() == 0);
    }


    /**
     * returns ArrayList templates for testing purposes
     */
    public ArrayList<Template> getTemplates() {
        return templates;
    }

    
    /**
     * returns array with width and height for testing purposes
     */
    public double[] getDimensions() {
        double[] dimensions = {width, height};
        return dimensions;
    }


    /**
     * Uses a golden section search to calculate rotation that minimizes the distance between the
     * gesture and the template points.
     * 
     * @param points
     * @param templatePoints
     * @return best distance
     */
    private double distanceAtBestAngle(Deque<Point> points, Deque<Point> templatePoints) {
        double thetaA = -Math.toRadians(45);
        double thetaB = Math.toRadians(45);
        final double deltaTheta = Math.toRadians(2);
        double phi = 0.5 * (-1.0 + Math.sqrt(5.0));// golden ratio
        double x1 = phi * thetaA + (1 - phi) * thetaB;
        double f1 = distanceAtAngle(points, templatePoints, x1);
        double x2 = (1 - phi) * thetaA + phi * thetaB;
        double f2 = distanceAtAngle(points, templatePoints, x2);
        while (Math.abs(thetaB - thetaA) > deltaTheta) {
            if (f1 < f2) {
                thetaB = x2;
                x2 = x1;
                f2 = f1;
                x1 = phi * thetaA + (1 - phi) * thetaB;
                f1 = distanceAtAngle(points, templatePoints, x1);
            } else {
                thetaA = x1;
                x1 = x2;
                f1 = f2;
                x2 = (1 - phi) * thetaA + phi * thetaB;
                f2 = distanceAtAngle(points, templatePoints, x2);
            }
        }
        return Math.min(f1, f2);
    }


    private double distanceAtAngle(Deque<Point> points, Deque<Point> templatePoints, double theta) {
        Deque<Point> rotatedPoints = null;

        rotatedPoints = rotateBy(points, theta);
        if (rotatedPoints.size() != templatePoints.size()) { // to prevent edge-case errors
            rotatedPoints = resamplePoints(rotatedPoints, templatePoints.size());
        }
        return pathDistance(rotatedPoints, templatePoints);
    }

    
    public double pathDistance(Deque<Point> a, Deque<Point> b) {
        double total = 0;
        double length = a.size();
        Iterator<Point> aIterator = a.iterator();
        Iterator<Point> bIterator = b.iterator();

        while (aIterator.hasNext()) {
            total += aIterator.next().distance(bIterator.next());
        }
        total /= length;
        return total;
    }
}


// maow :3