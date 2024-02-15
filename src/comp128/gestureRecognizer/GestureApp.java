package comp128.gestureRecognizer;

import edu.macalester.graphics.*;
import edu.macalester.graphics.ui.Button;
import edu.macalester.graphics.ui.TextField;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * The window and user interface for drawing gestures and automatically recognizing them
 * Created by bjackson on 10/29/2016.
 */
public class GestureApp {

    private CanvasWindow canvas;
    private Recognizer recognizer;
    private IOManager ioManager;
    private GraphicsGroup uiGroup;
    private Button addTemplateButton;
    private TextField templateNameField;
    private GraphicsText matchLabel;
    private Deque<Point> path;
    private Template result;
    private String name;
    private double score;


    public GestureApp(){
        canvas = new CanvasWindow("Gesture Recognizer", 600, 600);
        recognizer = new Recognizer();
        path = new ArrayDeque<>();
        ioManager = new IOManager();
        setupUI();
    }

    /**
     * Create the user interface
     */
    private void setupUI(){
        matchLabel = new GraphicsText("Match: ");
        matchLabel.setFont(FontStyle.PLAIN, 24);
        canvas.add(matchLabel, 10, 30);

        uiGroup = new GraphicsGroup();

        templateNameField = new TextField();

        addTemplateButton = new Button("Add Template");
        addTemplateButton.onClick( () -> addTemplate() );

        Point center = canvas.getCenter();
        double fieldWidthWithMargin = templateNameField.getSize().getX() + 5;
        double totalWidth = fieldWidthWithMargin + addTemplateButton.getSize().getX();


        uiGroup.add(templateNameField, center.getX() - totalWidth/2.0, 0);
        uiGroup.add(addTemplateButton, templateNameField.getPosition().getX() + fieldWidthWithMargin, 0);
        canvas.add(uiGroup, 0, canvas.getHeight() - uiGroup.getHeight());

        Consumer<Character> handleKeyCommand = ch -> keyTyped(ch);
        canvas.onCharacterTyped(handleKeyCommand);

        canvas.onMouseDown(d -> {
            path.clear();
            removeAllNonUIGraphicsObjects();
            path.add(d.getPosition());
        });
        
        canvas.onDrag(d -> {
            Point position = d.getPosition();
            Line drawnLine = new Line(path.peekLast(), position);
            canvas.add(drawnLine);
            path.addLast(position);
        });

        canvas.onMouseUp(d -> {
            if (!recognizer.templatesIsEmpty()){
                result = recognizer.recognize(path);
                score = result.getScore();
                name = result.toString();
                matchLabel.setText("Match: " + name + " Confidence: " + score);
            }      
        });
    }

    /**
     * Clears the canvas, but preserves all the UI objects
     */
    private void removeAllNonUIGraphicsObjects() {
        canvas.removeAll();
        canvas.add(matchLabel);
        matchLabel.setText("Match: ");
        canvas.add(uiGroup);
    }

    /**
     * Handle what happens when the add template button is pressed. This method adds the points stored in path as a template
     * with the name from the templateNameField textbox. If no text has been entered then the template is named with "no name gesture"
     */
    private void addTemplate() {
        String name = templateNameField.getText();
        if (name.isEmpty()){
            name = "no name gesture";
        }
        recognizer.addTemplate(name, path); // Add the points stored in the path as a template

    }

    /**
     * Handles keyboard commands used to save and load gestures for debugging and to write tests.
     * Note, once you type in the templateNameField, you need to call canvas.requestFocus() in order to get
     * keyboard events. This is best done in the mouseDown callback on the canvas.
     */
    public void keyTyped(Character ch) {
        if (ch.equals('L')){
            String name = templateNameField.getText();
            if (name.isEmpty()){
                name = "gesture";
            }
            Deque<Point> points = ioManager.loadGesture(name+".xml");
            if (points != null){
                recognizer.addTemplate(name, points);
                System.out.println("Loaded "+name);
            }
        }
        else if (ch.equals('s')){
            String name = templateNameField.getText();
            if (name.isEmpty()){
                name = "gesture";
            }
            ioManager.saveGesture(path, name, name+".xml");
            System.out.println("Saved "+name);
        }
    }


    /**
     * drawPoint is used to visualize and points apart from the visualizer, mostly to test edge cases and for fun
     * not used in final program
     * @param positions the path
     * @param fillColor color of the dots
     */
    public void drawPoint(Deque<Point> positions, Color fillColor) {
        Point position;
        Iterator<Point> dequeIterator = positions.iterator(); 
        while(dequeIterator.hasNext()){
            position = dequeIterator.next();
            Rectangle rectangle = new Rectangle(position.getX(), position.getY(), 2, 2);
            rectangle.setFilled(true);
            rectangle.setStrokeColor(new Color(0, 0, 0));
            rectangle.setFillColor(fillColor);
            canvas.add(rectangle);
        }
    }

    public static void main(String[] args){
        GestureApp window = new GestureApp();

    }
}
