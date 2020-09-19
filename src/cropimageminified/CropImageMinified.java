package cropimageminified;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.image.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

public class CropImageMinified extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        AnchorPane root = new AnchorPane();
        root.getChildren().addAll(this.cropImage(600, 700, "https://filedn.com/ltOdFv1aqz1YIFhf4gTY8D7/ingus-info/BLOGS/Photography-stocks3/stock-photography-slider.jpg"));
        Scene scene = new Scene(root, 800, 800);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private StackPane cropImage(int width, int height, String imagePath){
        Image img = new Image(imagePath);
        ImageView imageView = new ImageView(img);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-color: blue");
        
        double viewportWidth = this.getViewportWidth(img, width, height);
        double viewportHeight = this.getViewportHeight(img, width, height);
        double centerX = viewportWidth/2;
        double centerY = img.getHeight()/2-viewportHeight/2;
        imageView.setViewport(new Rectangle2D(centerX, centerY, viewportWidth, viewportHeight));

        //imageFrame
        StackPane frame = new StackPane();
        frame.setAlignment(Pos.CENTER);
        frame.setPrefSize(width, height);
        frame.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
        frame.getChildren().add(imageView);
        return frame;
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
}