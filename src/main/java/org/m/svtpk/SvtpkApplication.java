package org.m.svtpk;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.m.svtpk.entity.AudioReferencesEntity;
import org.m.svtpk.entity.EpisodeEntity;
import org.m.svtpk.entity.SubtitleReferencesEntity;
import org.m.svtpk.entity.VideoReferencesEntity;
import org.m.svtpk.services.EpisodeService;
import org.m.svtpk.utils.Arrow;
import org.m.svtpk.utils.RunnableCopier;
import org.m.svtpk.utils.Settings;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class SvtpkApplication extends Application {
    EpisodeEntity currentEpisode = new EpisodeEntity();
    EpisodeService episodeService = new EpisodeService();
    Text infoText;
    Button dlBtn;
    ImageView episodeImageView;
    ImageView statusIcon;
    TextField addressTextField;
    Stage window;
    Settings settings;
    HBox mainContentBox;
    ChoiceBox<String> resolutionChoiceBox;
    ChoiceBox<String> languageChoiceBox;
    ChoiceBox<String> subsChoiceBox;

    static SimpleDoubleProperty loadingCounter;
    static Text loaded;
    VBox progress;


    @Override
    public void start(Stage stage) {
        window = stage;
        window.getIcons().add(new Image("file:src/main/resources/images/arrow_green.png"));
        window.setTitle("SVTpk");
        window.setScene(homeScene());
        window.show();
    }

    public Scene homeScene() {
        System.out.println("System property: " + System.getProperty("user.dir"));
        System.out.println("Operating System: " + System.getProperty("os.name"));
        System.out.println("Java runtime version: " + System.getProperty("java.runtime.version" ));

        settings = Settings.load();
        GridPane grid = basicGrid();
        Label addressFieldLabel = new Label("Ange adress");
        addressFieldLabel.setAlignment(Pos.CENTER);

        addressTextField = addressTextField == null ? new TextField() : addressTextField;
        addressTextField.setPrefWidth(400);


        episodeImageView = currentEpisode.getImageURL() == null ? new ImageView() : new ImageView(new Image(currentEpisode.getImageURL()));
        episodeImageView.setPreserveRatio(true);
        episodeImageView.setFitWidth(200);

        infoText = new Text(currentEpisode.hasID(currentEpisode) ? currentEpisode.toString() : "");
        infoText.prefHeight(160);
        infoText.setFill(Color.DARKGREEN);
        VBox vBoxInfoText = new VBox(addressFieldLabel, addressTextField, episodeImageView, infoText);

        //lägg till alternativen till den här,
        VBox vBoxSettings = new VBox();
        vBoxSettings.prefWidth(200);

        Text resText = new Text("Upplösning");
        ArrayList<String> resolutionsList = new ArrayList<>();
        resolutionChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(resolutionsList));
        resolutionChoiceBox.valueProperty().addListener((observableValue, s, newValue) -> {
            if (newValue != null) {
                settings.setResolution(newValue);
                settings.save();
            }
        });
        HBox res = new HBox(resText, resolutionChoiceBox);
        res.setSpacing(30);
        res.setAlignment(Pos.BASELINE_LEFT);
        res.setPrefWidth(200);

        Text subsText = new Text("Undertexter");
        ArrayList<String> subs = new ArrayList<>();
        subsChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(subs));
        subsChoiceBox.valueProperty().addListener((observableValue, s, newValue) -> {
            if (newValue != null && !newValue.trim().equals("")) {
                settings.setSubs(newValue);
                settings.save();
            }
        });
        HBox sub = new HBox(subsText, subsChoiceBox);
        sub.setSpacing(29);

        Text languageText = new Text("Språk");
        HashMap<String, AudioReferencesEntity> availableAudio = currentEpisode.getAvailableAudio();
        ArrayList<String> audioList = new ArrayList<>();
        availableAudio.forEach((k, v) -> {
            audioList.add(v.getLabel());
        });
        languageChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(audioList));
        languageChoiceBox.setItems(FXCollections.observableArrayList(audioList));
        languageChoiceBox.valueProperty().addListener((observableValue, s, newValue) -> {
            if (newValue != null) {
                settings.setAudio(newValue);
                settings.save();
            }
        });
        HBox lang = new HBox(languageText, languageChoiceBox);
        lang.setSpacing(62);

        Text dirText = new Text("Spara till...");
        DirectoryChooser d = new DirectoryChooser();
        Button dirBtn = new Button("Välj");

        HBox hBoxCopy = new HBox(dirText, dirBtn);
        Text currentSavePath = settings.getPath() == null ? new Text() : new Text(settings.getPath());//settings.getPath()==null ? "" : settings.getPath());
        currentSavePath.textProperty().addListener((observableValue, s, newValue) -> {
            if (newValue != null) {
                settings.setPath(newValue);
                settings.save();
            }
        });
        hBoxCopy.setSpacing(38);

        dirBtn.setOnAction(e -> {
                    String path = String.valueOf(d.showDialog(window));
                    System.out.println("path:" + path);
                    if (!path.equals("null")) {
                        settings.setPath(path);
                    } else {
                        settings.setPath();
                    }
                    settings.save();
                    currentSavePath.setText(settings.getPath());
                }
        );
        VBox copy = new VBox(hBoxCopy, currentSavePath);
       /*
        path.valueProperty().addListener((observableValue, s, newValue) -> {
            settings.setResolution(newValue);
            settings.save();
        });
        */
        vBoxSettings.getChildren().add(res);
        vBoxSettings.getChildren().add(lang);
        vBoxSettings.getChildren().add(sub);
        //  vBoxSettings.getChildren().add(copy);


        TitledPane settingsPane = new TitledPane("Inställningar", vBoxSettings);
        settingsPane.setLayoutX(1);
        settingsPane.setLayoutY(1);
        settingsPane.prefWidth(200);

        Accordion accordion = new Accordion();
        accordion.getPanes().add(settingsPane);
        VBox settingsBox = new VBox(accordion);

        mainContentBox = mainContentBox != null ? mainContentBox : new HBox(vBoxInfoText, settingsBox);
        mainContentBox.setVisible(currentEpisode.hasID(currentEpisode));

        statusIcon = currentEpisode.hasID(currentEpisode) ? Arrow.getImgViewArrowDown("green") : Arrow.getImgViewArrowDown("grey");
        HBox statusIndicator = new HBox(statusIcon);
        statusIndicator.prefHeight(100);
        statusIndicator.setAlignment(Pos.BOTTOM_CENTER);
        statusIndicator.setDisable(!currentEpisode.hasID(currentEpisode));

        loadingCounter = loadingCounter != null ? loadingCounter : new SimpleDoubleProperty();
        loadingCounter.addListener(((observableValue, number, newValue) -> {
            System.out.println("obsValue:" + observableValue + "\tnumber:" + number + "\tnewValue:" + newValue);
            loadingCounter.set((Double) newValue);
        }));

        progress = progress != null ? progress : new VBox();
        progress.prefWidth(200);
        loaded = loaded != null ? loaded : new Text();
        loaded.setText("");
        progress.getChildren().add(loaded);
        progress.setAlignment(Pos.BOTTOM_CENTER);

        dlBtn = new Button("Kopiera");
        dlBtn.setDisable(!currentEpisode.hasID(currentEpisode));
        HBox hboxDlBtn = new HBox(10);
        hboxDlBtn.setAlignment(Pos.BOTTOM_CENTER);
        hboxDlBtn.getChildren().add(dlBtn);


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
        dlBtn.setOnAction(e -> {
            //ändra statusIndicator
            statusIcon.setImage(Arrow.getImgArrowDown("grey"));
            String status = episodeService.copyEpisodeToDisk(currentEpisode).toString();
            if ("200 OK".equals(status)) {
                System.out.println(status + " it goooood!");
                dlBtn.setText("Kopierat!");
                dlBtn.setDisable(true);
            } else {
                System.out.println(status);
                statusIcon.setImage(Arrow.getImgArrowDown("red"));
                infoText.setText(infoText.getText() + "\nNågot gick snett!");
            }
        });
        Button debugBtn = new Button("DEBUG");
        debugBtn.setAlignment(Pos.BOTTOM_CENTER);
        debugBtn.setOnAction(e -> {
            RunnableCopier.test();
            //window.setScene(optionsScene2(currentEpisode));
        });


        grid.add(addressFieldLabel, 0, 1);
        grid.add(search, 0, 2);
        grid.add(mainContentBox, 0, 3);
        grid.add(statusIndicator, 0, 4);
        grid.add(progress, 0, 5);
        grid.add(hboxDlBtn, 0, 6);

        grid.add(debugBtn, 0, 7);

        return new Scene(grid, 640, 480);
    }

    public static void updateLoadingBar(double loaderCounter) {
        System.out.println("loaderCounter säger: " + loaderCounter);
        loaded.setText(loaderCounter + "%");
        loadingCounter.set(loaderCounter);
    }


    private void updateUI() {
        mainContentBox.setVisible(currentEpisode.hasID(currentEpisode));
        System.out.println("currentEpisode is live? "+currentEpisode.isLive());
        if (currentEpisode.isLive()){
            infoText.setVisible(true);
            infoText.setFill(Color.FIREBRICK);
            infoText.setText("\n\nDu kan tyvärr inte kopiera en live-sändning.");
            currentEpisode = new EpisodeEntity();
        }
        else if (!currentEpisode.getSvtId().equals("")) {
            setVideoRes();
            setAudioLanguage();
            setSubs();
            loaded.setText("");
            infoText.setVisible(true);
            infoText.setFill(Color.DARKGREEN);
            infoText.setText(currentEpisode.toString());
            if(currentEpisode.getImageURL()!=null)
            episodeImageView.setImage(new Image(currentEpisode.getImageURL()));
            statusIcon.setImage(Arrow.getImgArrowDown("green"));
            statusIcon.setDisable(false);
            dlBtn.setText("Kopiera");
            dlBtn.setDisable(false);
            System.out.println("hej ui");
        } else if (addressTextField.getText().length() > 0) {
            loaded.setText("");
            currentEpisode = new EpisodeEntity();
            infoText.setVisible(true);
            infoText.setFill(Color.FIREBRICK);
            infoText.setText("Tyvärr, hittar inte det avsnittet.");
            episodeImageView.setImage(null);
            statusIcon.setImage(Arrow.getImgArrowDown("grey"));
            statusIcon.setDisable(true);
            dlBtn.setDisable(true);
        } else {
            loaded.setText("");
            currentEpisode = new EpisodeEntity();
            infoText.setVisible(false);
            episodeImageView.setImage(null);
            statusIcon.setImage(Arrow.getImgArrowDown("grey"));
            statusIcon.setDisable(true);
            dlBtn.setDisable(true);
        }
    }

    private String setVideoRes() {
        String selectedRes = "";
        ArrayList<String> res = new ArrayList<>();
        for (Map.Entry<String, VideoReferencesEntity> entry : currentEpisode.getAvailableResolutions().entrySet()) {
            res.add(entry.getKey());
            System.out.println("Entry: "+entry.getKey());
            if (settings.getResolution().equals(entry.getKey())) {
                selectedRes = entry.getKey();
                resolutionChoiceBox.setValue(selectedRes);
            }
        }
        if (resolutionChoiceBox.getValue() == null) {
            selectedRes = res.get(0);
            System.out.println("ingen matchande res, sätter till: " + res.get(0));
        }
        resolutionChoiceBox.setItems(FXCollections.observableArrayList(res));
        resolutionChoiceBox.setValue(selectedRes);
        return selectedRes;
    }

    private void setAudioLanguage() {
        String selectedAudio = "";
        ArrayList<String> lang = new ArrayList<>();
        for (Map.Entry<String, AudioReferencesEntity> entry : currentEpisode.getAvailableAudio().entrySet()) {
            lang.add(entry.getKey());
            if (settings.getAudio().equals(entry.getKey())) {
                selectedAudio = entry.getKey();
                languageChoiceBox.setValue(selectedAudio);
            }
        }
        if (languageChoiceBox.getValue() == null) {
            selectedAudio = lang.get(0);
            System.out.println("ingen matchande Auidio, sätter till " + lang.get(0));
        }
        languageChoiceBox.setItems(FXCollections.observableArrayList(lang));
        languageChoiceBox.setValue(selectedAudio);
    }

    private void setSubs() {
        String selectedSubs = "Inga undertexter";
        ArrayList<String> subs = new ArrayList<>();
        subs.add(selectedSubs);
        for (Map.Entry<String, SubtitleReferencesEntity> entry : currentEpisode.getAvailableSubs().entrySet()) {
            subs.add(entry.getKey());
            if (settings.getSubs().equals(entry.getKey())) {
                selectedSubs = entry.getKey();
                subsChoiceBox.setValue(selectedSubs);
            }
        }
        if (subsChoiceBox.getValue() == null) {
            selectedSubs = subs.get(0);
            System.out.println("inga matchande subs, sätter till " + subs.get(0));
        }
        subsChoiceBox.setItems(FXCollections.observableArrayList(subs));
        subsChoiceBox.setValue(selectedSubs);
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
