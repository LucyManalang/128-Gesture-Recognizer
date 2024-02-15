package comp128.gestureRecognizer;

import edu.macalester.graphics.Point;

import java.util.Deque;
import java.util.Iterator;
import java.util.ArrayDeque;

public class Template {
    private double score;
    private String name;
    private Deque<Point> path;


    public Template(String name, Deque<Point> path) {
        this.name = name;
        this.path = path;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public Deque<Point> getPath() {
        return path;
    }

    public Deque<Point> getReversePath() {
        Deque<Point> outputDeque = new ArrayDeque<>();
        Iterator<Point> dequeIterator = path.iterator();

        while (dequeIterator.hasNext()) {
            outputDeque.addFirst(dequeIterator.next());
        }
        return outputDeque;
    }

    public String toString() {
        return name;
    }
}
