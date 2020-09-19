package cropimageminified;

import java.awt.image.BufferedImage;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class CropImage extends StackPane{

    private ImageView imageView;
    
    public CropImage(double width, double height, String imagePath) {
        Image img = new Image(imagePath);
        imageView = new ImageView(img);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        
        double viewportWidth = this.getViewportWidth(img, width, height);
        double viewportHeight = this.getViewportHeight(img, width, height);
        double centerX = viewportWidth/2;
        double centerY = img.getHeight()/2-viewportHeight/2;
        imageView.setViewport(new Rectangle2D(centerX, centerY, viewportWidth, viewportHeight));
        
        ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();
        imageView.setOnMousePressed(e -> {
            Point2D mousePress = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
            mouseDown.set(mousePress);
        });
        imageView.setOnMouseDragged(e -> {
            Point2D dragPoint = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
            shift(imageView, dragPoint.subtract(mouseDown.get()));
            mouseDown.set(imageViewToImage(imageView, new Point2D(e.getX(), e.getY())));
        });
        final int MIN_PIXELS = 100;
        imageView.setOnScroll(e -> {
            double delta = e.getDeltaY();
            Rectangle2D viewport = imageView.getViewport();
            
            double scale = clamp(Math.pow(1.01, delta), Math.min(MIN_PIXELS/viewport.getWidth(), MIN_PIXELS/viewport.getHeight()), Math.max(viewportWidth/viewport.getWidth(), viewportHeight/viewport.getHeight()));
            double newWidth = viewport.getWidth() * scale;
            double newHeight = viewport.getHeight() * scale;
            
            Point2D mouse = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
            double newMinX = clamp(mouse.getX() - (mouse.getX() - viewport.getMinX()) * scale,
                    0, img.getWidth() - newWidth);
            double newMinY = clamp(mouse.getY() - (mouse.getY() - viewport.getMinY()) * scale,
                    0, img.getHeight() - newHeight);

            imageView.setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
        });
        
        imageView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                imageView.setViewport(new Rectangle2D(centerX, centerY, viewportWidth, viewportHeight));
            }
        });
        
        //imageFrame
        super.setAlignment(Pos.CENTER);
        super.setPrefSize(width, height);
        super.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
        super.getChildren().add(imageView);
    }
    
    private double getViewportWidth(Image img, double frameWidth, double frameHeight){
        //make sure return width to equal width of frame
        if(frameWidth > frameHeight){
            return this.getShortestImageDimension(img);
        }
        else{
            double substractedPercentage = (frameWidth*100/frameHeight);
            double substractedValue = (substractedPercentage/100)*this.getShortestImageDimension(img);
            return substractedValue;
        }
    }
    private double getViewportHeight(Image img, double frameWidth, double frameHeight){
        if(frameWidth > frameHeight){
            double substractedPercentage = (frameHeight*100/frameWidth);
            double substractedValue = (substractedPercentage/100)*this.getShortestImageDimension(img);
            return substractedValue;
        }
        else{
            return this.getShortestImageDimension(img);
        }
    }
    private double getShortestImageDimension(Image image){
        return image.getWidth()>image.getHeight()?image.getHeight():image.getWidth();
    }
    
    // shift the viewport of the imageView by the specified delta, clamping so
    // the viewport does not move off the actual image:
    private void shift(ImageView imageView, Point2D delta) {
        Rectangle2D viewport = imageView.getViewport();

        double width = imageView.getImage().getWidth() ;
        double height = imageView.getImage().getHeight() ;

        double maxX = width - viewport.getWidth();
        double maxY = height - viewport.getHeight();

        double minX = clamp(viewport.getMinX() - delta.getX(), 0, maxX);
        double minY = clamp(viewport.getMinY() - delta.getY(), 0, maxY);

        imageView.setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
    }
    private double clamp(double value, double min, double max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }
    
    // convert mouse coordinates in the imageView to coordinates in the actual image:
    private Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates) {
        double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
        double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();

        Rectangle2D viewport = imageView.getViewport();
        return new Point2D(viewport.getMinX() + xProportion * viewport.getWidth(), viewport.getMinY() + yProportion * viewport.getHeight());
    }
    
    //crop image
    public Image crop() {
        Bounds selectionBounds = imageView.getBoundsInParent();

        // show bounds info
        System.out.println( "Selected area: " + selectionBounds);

        int width = (int) selectionBounds.getWidth();
        int height = (int) selectionBounds.getHeight();

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D( selectionBounds.getMinX(), selectionBounds.getMinY(), width, height));

        WritableImage wi = new WritableImage( width, height);
        imageView.snapshot(parameters, wi);

        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(wi, null);
        Image image = SwingFXUtils.toFXImage(bufImageARGB, null);

        return image;
    }
}
