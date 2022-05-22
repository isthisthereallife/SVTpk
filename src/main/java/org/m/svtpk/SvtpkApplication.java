package org.m.svtpk;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.m.svtpk.entity.*;
import org.m.svtpk.services.EpisodeService;
import org.m.svtpk.utils.Arrow;
import org.m.svtpk.utils.EpisodeCopier;
import org.m.svtpk.utils.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    HBox episodeHBox;
    HBox mainContentBox;
    VBox settingsBox;
    VBox queueVBox;
    ChoiceBox<String> resolutionChoiceBox;
    ChoiceBox<String> languageChoiceBox;
    ChoiceBox<String> subsChoiceBox;

    static SimpleDoubleProperty loadingCounter;
    static Text loaded;
    static ProgressBar progressBar;
    VBox progress;
    ArrayList<EpisodeEntity> entityArrayList;
    ObservableList<EpisodeEntity> queue;

    @Override
    public void start(Stage stage) {
        window = stage;
        window.getIcons().add(Arrow.getImgArrowDown("green"));
        window.setTitle("SVTpk");
        window.setScene(homeScene());
        window.show();
    }

    public Scene homeScene() {

        settings = Settings.load();
        if (settings.isAdvancedUser()) {
            System.out.println("System property: " + System.getProperty("user.dir"));
            System.out.println("Operating System: " + System.getProperty("os.name"));
            System.out.println("Java runtime version: " + System.getProperty("java.runtime.version"));
        }
        GridPane grid = basicGrid();

        Label addressFieldLabel = new Label("Ange adress till avsnitt");
        addressFieldLabel.setAlignment(Pos.CENTER);

        //paste from clipboard if clipboard text exists and seems relevant
        Clipboard clip = Clipboard.getSystemClipboard();
        addressTextField = addressTextField == null ?
                clip == null ?
                        new TextField()
                        : clip.getString().contains("svt") ?
                        new TextField(Clipboard.getSystemClipboard().getString())
                        :
                        new TextField()
                : addressTextField;
        addressTextField.setPrefWidth(400);


        // QUEUE

        ListView<EpisodeEntity> queueListView = new ListView<>();
        entityArrayList = new ArrayList<>();
        queue = FXCollections.observableArrayList(entityArrayList);

        queueListView.setItems(queue);
        queueListView.setPrefWidth(200);
        queueListView.setPrefHeight(600);
        queueListView.setContextMenu(getContextMenu(currentEpisode));

        queueVBox = new VBox(queueListView);

        episodeImageView = currentEpisode.getImageURL() == null ? new ImageView() : new ImageView(new Image(String.valueOf(currentEpisode.getImageURL())));
        episodeImageView.setPreserveRatio(true);
        episodeImageView.setFitWidth(200);

        infoText = new Text(currentEpisode.hasID(currentEpisode) ? currentEpisode.toString() : "");
        infoText.prefHeight(160);
        infoText.setFill(Color.DARKGREEN);
        VBox vBoxInfoText = new VBox(addressFieldLabel, addressTextField, episodeImageView, infoText);

        //lägg till alternativen till den här,
        VBox vBoxSettings = new VBox();
        vBoxSettings.prefWidth(100);

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
        vBoxSettings.getChildren().add(copy);


        TitledPane settingsPane = new TitledPane("Inställningar", vBoxSettings);
        settingsPane.setLayoutX(1);
        settingsPane.setLayoutY(1);
        //settingsPane.prefWidth(200);

        Accordion accordion = new Accordion();
        accordion.getPanes().add(settingsPane);
        settingsBox = new VBox(accordion);

        episodeHBox = new HBox(vBoxInfoText, settingsBox);
        mainContentBox = mainContentBox != null ? mainContentBox : new HBox(episodeHBox);
        //mainContentBox = mainContentBox != null ? mainContentBox : episodeHBox;

        //byt den här till en separat låda
        episodeHBox.setVisible(currentEpisode.hasID(currentEpisode));

        statusIcon = currentEpisode.hasID(currentEpisode) ? Arrow.getImgViewArrowDown("green") : Arrow.getImgViewArrowDown("grey");
        HBox statusIndicator = new HBox(statusIcon);
        statusIndicator.prefHeight(100);
        statusIndicator.setAlignment(Pos.BOTTOM_CENTER);
        statusIndicator.setDisable(!currentEpisode.hasID(currentEpisode));
        dlBtn = new Button("Kopiera");

        loadingCounter = loadingCounter != null ? loadingCounter : new SimpleDoubleProperty();
        loadingCounter.addListener(((observableValue, number, newValue) -> {
            loadingCounter.set((Double) newValue);
            dlBtn.setDisable(true);
            if (loadingCounter.getValue() >= 0) {
                loaded.setFill(Color.DARKGREY);
                dlBtn.setText("Kopierar...");
            }
            if (loadingCounter.getValue() == 100) {
                loaded.setFill(Color.DARKGREEN);
                dlBtn.setText("Kopierat!");
            }
        }));

        progress = progress != null ? progress : new VBox();
        //progress.prefWidth(200);
        loaded = loaded != null ? loaded : new Text();
        loaded.setText("");
        progressBar = new ProgressBar();
        progressBar.setProgress(0);
        progressBar.setVisible(false);
        progress.getChildren().add(progressBar);
        progress.getChildren().add(loaded);
        progress.setAlignment(Pos.BOTTOM_CENTER);


        dlBtn.setDisable(!currentEpisode.hasID(currentEpisode));
        HBox hboxDlBtn = new HBox(10);
        hboxDlBtn.setAlignment(Pos.BOTTOM_CENTER);
        hboxDlBtn.getChildren().add(dlBtn);


        addressTextField.textProperty().addListener((observable, oldValue, newValue) -> {
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
            dlBtn.setText("Kopierar...");
            dlBtn.setDisable(true);
            //QueueHandler(currentEpisode)

            EpisodeCopier t = new EpisodeCopier(currentEpisode);
            Thread th = new Thread(t);
            th.start();

            currentEpisode.setProgressState(ProgressStates.QUEUED);
            queue.add(currentEpisode);
            System.out.println("\nadded " + currentEpisode.getEpisodeTitle() + " to queue");
            //add to queue

        });
        Button debugBtn = new Button("DEBUG");
        debugBtn.setAlignment(Pos.BOTTOM_CENTER);
        debugBtn.setOnAction(e -> {
        });

        HBox episodeTree = new HBox();

        //grid.setGridLinesVisible(true);
        ColumnConstraints cC = new ColumnConstraints();
        cC.setPercentWidth(100);
        grid.getColumnConstraints().add(cC);
        grid.add(addressFieldLabel, 0, 1);
        grid.add(search, 0, 2, 6, 1);
        queueVBox.setVisible(true);

        mainContentBox.getChildren().add(queueVBox);

        grid.add(mainContentBox, 0, 4);
        //grid.add(queueVBox, 1, 4, 3, 3);
        grid.add(statusIndicator, 0, 4);
        grid.add(progress, 0, 6);
        grid.add(episodeTree, 0, 5, 2, 6);
        grid.add(hboxDlBtn, 0, 7);

        //grid.add(debugBtn, 0, 7);

        return new Scene(grid, 640, 480);
    }

    public static void updateLoadingBar(double loaderCounter) {
        progressBar.setVisible(true);
        progressBar.setProgress(loaderCounter);
        loaded.setText(Math.round(loaderCounter * 100) + "%");

        loadingCounter.set(loaderCounter * 100);
    }

    private ContextMenu getContextMenu(EpisodeEntity currentEpisode) {
        // Open a menu
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem miPlay = new MenuItem("Start");
        //MenuItem miPause = new MenuItem("Pause");
        MenuItem miAbort = new MenuItem("Stop");
        MenuItem miRemove = new MenuItem("Ta Bort från listan");
        MenuItem miRemoveDone = new MenuItem("Ta Bort Färdiga");
        MenuItem miRemoveAll = new MenuItem("Rensa Listan");

        contextMenu.getItems().addAll(miPlay, /*miPause,*/ miAbort, miRemove, miRemoveAll);
        miPlay.setOnAction((mi) -> {
            // pausa dom andra/den aktiva.
            // börja denna
        });
        /*
        miPause.setOnAction((mi) -> {
            // pausa denna
        });
        */
        miAbort.setOnAction((mi) -> {
            // avsluta denna
        });
        miRemove.setOnAction((mi) -> {
            //ta bort denna från queue
        });
        miRemoveDone.setOnAction((mi) ->{
            //Ta bort dom i queue som är ProgressState.DONE
            queue.removeIf((u)->{
                return u.getProgressState().equals(ProgressStates.DONE);
            });
        });
        miRemoveAll.setOnAction((mi) -> {
            //ta bort alla från queue
        });

        //contextMenu.show(m, e.getScreenX(), e.getScreenY());


        System.out.println("\nJAPP!");

        return contextMenu;
    }

    private void updateUI() {
        episodeHBox.setVisible(currentEpisode.hasID(currentEpisode));
        queueVBox.setVisible(true);
        progressBar.setVisible(false);
        if (currentEpisode.isLive()) {
            // if requested episode is a live-stream
            loaded.setVisible(true);
            loaded.setFill(Color.FIREBRICK);
            loaded.setText("\n\nDu kan tyvärr inte kopiera en live-sändning.");
            episodeImageView.setVisible(false);
            settingsBox.setVisible(false);
            currentEpisode = new EpisodeEntity();
            dlBtn.setText("Kopiera");
            dlBtn.setDisable(true);
        } else if (!currentEpisode.getSvtId().equals("")) {
            //if there is a SvtId
            setVideoRes();
            setAudioLanguage();
            setSubs();
            loaded.setText("");
            infoText.setVisible(true);
            infoText.setFill(Color.DARKGREEN);
            infoText.setText(currentEpisode.toString());
            settingsBox.setVisible(true);
            if (currentEpisode.getImageURL() != null) {
                // if there is an image URL
                episodeImageView.setImage(new Image(String.valueOf(currentEpisode.getImageURL())));
                episodeImageView.setVisible(true);
            }
            statusIcon.setImage(Arrow.getImgArrowDown("green"));
            statusIcon.setDisable(false);
            dlBtn.setText("Kopiera");
            dlBtn.setDisable(false);
        } else if (addressTextField.getText().length() > 0) {
            //if text supplied but no episode found
            currentEpisode = new EpisodeEntity();
            loaded.setVisible(true);
            loaded.setFill(Color.FIREBRICK);
            loaded.setText("Tyvärr, hittar inte det avsnittet.");
            episodeImageView.setImage(null);
            statusIcon.setImage(Arrow.getImgArrowDown("grey"));
            statusIcon.setDisable(true);
            dlBtn.setText("Kopiera");
            dlBtn.setDisable(true);
        } else {
            // no text in text field, clear UI
            loaded.setText("");
            currentEpisode = new EpisodeEntity();
            infoText.setVisible(false);

            queueVBox.setVisible(true);
            episodeImageView.setImage(null);
            statusIcon.setImage(Arrow.getImgArrowDown("grey"));
            statusIcon.setDisable(true);
            dlBtn.setText("Kopiera");
            dlBtn.setDisable(true);
        }
    }

    private void setVideoRes() {
        String selectedRes = "";
        ArrayList<String> res = new ArrayList<>();
        for (Map.Entry<String, VideoReferencesEntity> entry : currentEpisode.getAvailableResolutions().entrySet()) {
            res.add(entry.getKey());
            if (settings.getResolution().equals(entry.getKey())) {
                selectedRes = entry.getKey();
                resolutionChoiceBox.setValue(selectedRes);
            }
        }
        if (resolutionChoiceBox.getValue() == null) {
            selectedRes = res.get(0);
        }
        resolutionChoiceBox.setItems(FXCollections.observableArrayList(res));
        resolutionChoiceBox.setValue(selectedRes);
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
        //grid.setPrefSize(500, 400);
        //grid.setMinSize(500, 400);
        //grid.setPrefWidth(600);
        //grid.setGridLinesVisible(true);
        Text title = new Text("SVTpk");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        title.setTextAlignment(TextAlignment.CENTER);
        grid.add(title, 0, 0, 2, 1);
        return grid;
    }

    public static void main(String[] args) {
        launch();
    }

}
