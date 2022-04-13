package org.m.svtpk;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.m.svtpk.entity.EpisodeEntity;
import org.m.svtpk.services.EpisodeService;
import org.m.svtpk.utils.Arrow;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.stream.Stream;

@SpringBootApplication
public class SvtpkApplication extends Application {
    EpisodeEntity currentEpisode = new EpisodeEntity();
    EpisodeService episodeService = new EpisodeService();
    Text infoText;
    Button dlBtn;
    ImageView episodeImageView;
    TextField addressTextField;
    Stage window;
    SimpleIntegerProperty loadingCounter;

    @Override
    public void start(Stage stage) {
        window = stage;
        window.getIcons().add(new Image("file:src/main/resources/images/arrow.png"));
        window.setTitle("SVTpk");
        window.setScene(homeScene());
        window.show();
    }

    private Scene optionsScene() {
        GridPane grid = basicGrid();
        //HBox navbar = Navbar.getNavbar(window);
        HBox navbar = new HBox(10);
        navbar.prefHeight(160);
        navbar.setSpacing(100);

        // left part

        ImageView imgArrowLeft = Arrow.getImgArrowLeft();


        HBox goBackButtonBox = new HBox(10);
        Text goBackText = new Text("Gå Tillbaka");
        goBackText.setTextAlignment(TextAlignment.LEFT);
        goBackButtonBox.getChildren().add(imgArrowLeft);
        goBackButtonBox.getChildren().add(goBackText);
        goBackButtonBox.setOnMouseClicked(e -> {
            window.setScene(homeScene());
        });


        navbar.getChildren().add(goBackButtonBox);


        //right part
        Label resolutionLabel = new Label("Inställningar");
        resolutionLabel.setAlignment(Pos.CENTER_LEFT);
        navbar.getChildren().add(resolutionLabel);


        grid.add(navbar, 0, 8);
        return new Scene(grid, 640, 480);
    }

    private Scene optionsScene2(EpisodeEntity currentEpisode){

        BorderPane borderPane = new BorderPane();
        MenuBar menuBar = new MenuBar();
        Menu filesMenu = new Menu("Fil");
        filesMenu.getItems().addAll(new MenuItem("Klickity Klick"), new SeparatorMenuItem(), new MenuItem("Exit"));
        Menu helpMenu = new Menu("Hjälp");
        helpMenu.getItems().addAll(new MenuItem("ffmpeg"), new MenuItem("SvtPlay"));
        menuBar.getMenus().addAll(filesMenu,helpMenu);
        ImageView imgArrowLeft = new ImageView(new Image("file:src/main/resources/images/arrow.png"));

        HBox navbar = new HBox(10);
        navbar.prefHeight(160);
        navbar.setSpacing(100);

        // left part

        imgArrowLeft.rotateProperty().setValue(90);
        imgArrowLeft.setPreserveRatio(true);
        imgArrowLeft.setFitWidth(20);

        HBox goBackButtonBox = new HBox(10);
        Text goBackText = new Text("Gå Tillbaka");
        goBackText.setTextAlignment(TextAlignment.LEFT);
        goBackButtonBox.getChildren().add(imgArrowLeft);
        goBackButtonBox.getChildren().add(goBackText);
        goBackButtonBox.setOnMouseClicked(e -> {
            window.setScene(homeScene());
        });


        navbar.getChildren().add(goBackButtonBox);
        VBox vbox = new VBox();
        vbox.getChildren().add(menuBar);
        vbox.getChildren().add(navbar);
        borderPane.setTop(vbox);
        borderPane.setCenter(new Text("CENTERN"));
        borderPane.setRight(new Text("Höger"));


    return new Scene(borderPane,640,480);

    }
    public Scene homeScene() {
        GridPane grid = basicGrid();
        Label addressFieldLabel = new Label("Ange adress");
        addressFieldLabel.setAlignment(Pos.CENTER);
        grid.add(addressFieldLabel, 0, 1);

        addressTextField = addressTextField == null ? new TextField() : addressTextField;
        addressTextField.setPrefWidth(400);


        episodeImageView = currentEpisode.getImageURL() == null ? new ImageView() : new ImageView( new Image(currentEpisode.getImageURL()));
        episodeImageView.setPreserveRatio(true);
        episodeImageView.setFitWidth(200);
        grid.add(episodeImageView, 0, 2);

        infoText = new Text(currentEpisode.hasID(currentEpisode) ? currentEpisode.toString() : "");
        HBox hBoxInfoText = new HBox(10);
        infoText.prefHeight(160);
        infoText.setFill(Color.DARKGREEN);
        hBoxInfoText.getChildren().add(infoText);

        grid.add(hBoxInfoText, 0, 3);

        dlBtn = new Button("Kopiera");
        dlBtn.setDisable(!currentEpisode.hasID(currentEpisode));
        HBox hboxDlBtn = new HBox(10);
        hboxDlBtn.setAlignment(Pos.BOTTOM_CENTER);
        hboxDlBtn.getChildren().add(dlBtn);
        grid.add(hboxDlBtn, 0, 4);


        addressTextField.textProperty().addListener((observable, oldValue, newValue) -> {
           /* search every time user types a letter in search box. not good now, maybe good later.
             currentEpisode = episodeService.findEpisode(addressTextField.getText());
           */
            if (addressTextField.getText().length() < 1) {
                currentEpisode = new EpisodeEntity();
                updateUI();
            }
        });


        addressTextField.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                currentEpisode = episodeService.findEpisode(addressTextField.getText());
                updateUI();
            }
        });

        Button findEpisodeBtn = new Button("Hitta");
        findEpisodeBtn.setOnAction(e -> {
            currentEpisode = episodeService.findEpisode(addressTextField.getText());
            updateUI();
        });

        HBox search = new HBox(10);
        search.getChildren().add(addressTextField);
        search.getChildren().add(findEpisodeBtn);
        grid.add(search, 0, 3);

        dlBtn.setOnAction(e -> {
            infoText.setText(episodeService.copyEpisodeToDisk(currentEpisode).toString());
        });
        Button debugBtn = new Button("DEBUG");
        debugBtn.setOnAction(e -> {
            window.setScene(optionsScene2(currentEpisode));
        });
        grid.add(debugBtn, 0, 6);

        return new Scene(grid, 640, 480);
    }

    private void updateUI() {
        if (!currentEpisode.getSvtId().equals("")) {
            infoText.setVisible(true);
            infoText.setFill(Color.DARKGREEN);
            infoText.setText(currentEpisode.toString());
            episodeImageView.setImage(new Image(currentEpisode.getImageURL()));
            System.out.println("hej ui");

            dlBtn.setDisable(false);
        } else if (addressTextField.getText().length() > 0) {
            currentEpisode = new EpisodeEntity();
            infoText.setVisible(true);
            infoText.setFill(Color.FIREBRICK);
            infoText.setText("Tyvärr, hittar inte det avsnittet.");
            episodeImageView.setImage(null);
            dlBtn.setDisable(true);
        } else {
            currentEpisode = new EpisodeEntity();
            infoText.setVisible(false);
            episodeImageView.setImage(null);
            dlBtn.setDisable(true);
        }
    }

    private GridPane basicGrid() {

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        grid.setPrefSize(500, 400);
        grid.setMinSize(500, 400);
        grid.setPrefWidth(600);
        //grid.setGridLinesVisible(true);
        Text title = new Text("SVTpk");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        title.setTextAlignment(TextAlignment.LEFT);
        grid.add(title, 0, 0, 2, 1);
        return grid;
    }

    public static void main(String[] args) {
        launch();
    }

}
