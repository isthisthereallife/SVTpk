package org.m.svtpk.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Arrow {

    public static ImageView getImgArrowLeft() {
        ImageView imgArrowLeft = new ImageView(new Image("file:src/main/resources/images/arrow.png"));
        imgArrowLeft.rotateProperty().setValue(90);
        imgArrowLeft.setPreserveRatio(true);
        imgArrowLeft.setFitWidth(20);
        return imgArrowLeft;
    }
}
