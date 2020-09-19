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
    private static final int MIN_PIXELS = 10;
    private double width, height;
    
    public CropImage(String imagePath, int frameWidth, int frameHeight) {
        Image image = new Image(imagePath);
     
        width = image.getWidth();
        height = image.getHeight();

        imageView = new ImageView(image);
        imageView.setPreserveRatio(true);//keep the aspect ratio so you can set only width or height
        imageView.setFitWidth(frameWidth);//set image width 300px
        imageView.setFitHeight(frameHeight);//set image height 300px
        reset();

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

        imageView.setOnScroll(e -> {
            double delta = e.getDeltaY();
            Rectangle2D viewport = imageView.getViewport();

            double Max_Pixel = this.getLargestImageDimension(width, height);//not allow to zoom out of image longest dimension

            //calculate substract value
//            double substractedPercentage = 100-(frameWidth*100/frameHeight);
//            double substractedValue = (substractedPercentage/100)*Max_Pixel;
//            System.out.println("Max Pixel "+Max_Pixel);
//            System.out.println("substracted value "+substractedValue);
            
            
            double substractValue = Max_Pixel/6;//size / 4 ex : 400/4 = 100, then height - 100 = width
            double scale = clamp(Math.pow(1.01, delta),
                // don't scale so we're zoomed in to fewer than MIN_PIXELS in any direction:
                Math.min(MIN_PIXELS / viewport.getWidth(), MIN_PIXELS / viewport.getHeight()),
                // don't scale so that we're bigger than image dimensions:
                //Math.max(frameWidth / viewport.getWidth(), frameHeight / viewport.getHeight())
                Math.max((Max_Pixel-substractValue) / viewport.getWidth(), Max_Pixel / viewport.getHeight())
            );

            Point2D mouse = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));

            double newWidth = viewport.getWidth() * scale;
            double newHeight = viewport.getHeight() * scale;
            
            System.out.println("new Width "+newWidth);
            System.out.println("new Height "+newHeight);

            double newMinX = clamp(mouse.getX() - (mouse.getX() - viewport.getMinX()) * scale,
                    0, width - newWidth);
            double newMinY = clamp(mouse.getY() - (mouse.getY() - viewport.getMinY()) * scale,
                    0, height - newHeight);

            imageView.setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
        });

        imageView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                reset();
            }
        });

        //image container
        super.getChildren().add(imageView);
        super.setAlignment(Pos.CENTER);
        super.setPrefSize(frameWidth, frameHeight);
        super.setMaxSize(frameWidth, frameHeight);
        super.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
    }
    private double getLargestImageDimension(double imageWidth, double imageHeight){
        return imageWidth>imageHeight?imageHeight:imageWidth;
    }
    
    public Image cropClick(){
        Bounds selectionBounds = imageView.getBoundsInParent();
        // show bounds info
        System.out.println( "Selected area: " + selectionBounds);
        // crop the image
        return crop(imageView, selectionBounds);
    }
    public void resetClick(){
        reset();
    }

    //crop image
    private Image crop(ImageView imageView, Bounds bounds) {
        int newWidth = (int) bounds.getWidth();
        int newHeight = (int) bounds.getHeight();
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D( bounds.getMinX(), bounds.getMinY(), newWidth, newHeight));

        WritableImage wi = new WritableImage( newWidth, newHeight);
        imageView.snapshot(parameters, wi);

        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(wi, null);
        Image image = SwingFXUtils.toFXImage(bufImageARGB, null);
        return image;
    }

    // reset to center
    private void reset() {
        double size = this.getLargestImageDimension(width, height);

        final double substractValue = size/4;//size / 4 ex : 400/4 = 100, then height - 100 = width (because height larger than width 1/4)

        double centerX = (width/2)-((size-substractValue)/2);
        double centerY = (height/2)-(size/2);
        imageView.setViewport(new Rectangle2D(centerX, centerY, size-substractValue, size));
    }

    // shift the viewport of the imageView by the specified delta, clamping so
    // the viewport does not move off the actual image:
    private void shift(ImageView imageView, Point2D delta) {
        Rectangle2D viewport = imageView.getViewport();

        double newWidth = imageView.getImage().getWidth() ;
        double newHeight = imageView.getImage().getHeight() ;

        double maxX = newWidth - viewport.getWidth();
        double maxY = newHeight - viewport.getHeight();

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
        return new Point2D(
                viewport.getMinX() + xProportion * viewport.getWidth(),
                viewport.getMinY() + yProportion * viewport.getHeight());
    }
}
