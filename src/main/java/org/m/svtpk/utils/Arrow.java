package org.m.svtpk.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Arrow {

    public static ImageView getImgViewArrowLeft() {
        ImageView imgArrowLeft = new ImageView(new Image("file:src/main/resources/images/arrow.png"));
        imgArrowLeft.rotateProperty().setValue(90);
        imgArrowLeft.setPreserveRatio(true);
        imgArrowLeft.setFitWidth(20);
        return imgArrowLeft;
    }

    public static ImageView getImgViewArrowDown(String colour) {
        ImageView imgArrowDown = new ImageView(new Image("file:src/main/resources/images/arrow_" + colour + ".png"));
        imgArrowDown.setPreserveRatio(true);
        imgArrowDown.setFitWidth(40);
        return imgArrowDown;
    }

    public static Image getImgArrowDown(String colour) {
        return new Image("file:src/main/resources/images/arrow_" + colour + ".png");
    }
}
