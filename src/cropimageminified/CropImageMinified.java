package cropimageminified;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.image.*;
import javafx.scene.control.Button;

public class CropImageMinified extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        
        CropImage ci = new CropImage(600, 200, "https://filedn.com/ltOdFv1aqz1YIFhf4gTY8D7/ingus-info/BLOGS/Photography-stocks3/stock-photography-slider.jpg");
        
        
        ImageView img = new ImageView();
        img.setFitHeight(200);
        img.setFitWidth(600);
        AnchorPane.setTopAnchor(img, 240.0);
        
        Button btn = new Button("Crop");
        AnchorPane.setTopAnchor(btn, 460.0);
        btn.setOnAction(e -> {
            img.setImage(ci.crop());
        });
        
        
        AnchorPane root = new AnchorPane();
        root.getChildren().addAll(ci, img, btn);
        Scene scene = new Scene(root, 800, 800);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}