package org.m.svtpk.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;

public class Arrow {

    public static ImageView getImgViewArrowLeft() {
        ImageView imgArrowLeft = new ImageView(new Image(getArrowDownLocation()+getArrowDownFiletype()));
        imgArrowLeft.rotateProperty().setValue(90);
        imgArrowLeft.setPreserveRatio(true);
        imgArrowLeft.setFitWidth(20);
        return imgArrowLeft;
    }

    public static ImageView getImgViewArrowDown(String colour, int width) {
        ImageView imgArrowDown = getImgViewArrowDown(colour);
        imgArrowDown.setFitWidth(width);
        return imgArrowDown;
    }
    public static ImageView getImgViewArrowDown(String colour) {
        ImageView imgArrowDown = new ImageView(getImgArrowDown(colour));
        imgArrowDown.setPreserveRatio(true);
        imgArrowDown.setFitWidth(40);
        return imgArrowDown;
    }

    public static Image getImgArrowDown(String colour) {
        return new Image(getArrowDownLocation()+"_" + colour.toLowerCase() + getArrowDownFiletype());
    }
    public static String getArrowDownLocation() {
        return "file:src/main/resources/images/arrow";
    }
    public static String getArrowDownFiletype(){
        return ".png";
    }
}
