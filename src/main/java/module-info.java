module org.m.svtpk {
    requires javafx.controls;
    requires jdk.crypto.ec;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    opens org.m.svtpk;
    exports org.m.svtpk;
    exports org.m.svtpk.entity;
}