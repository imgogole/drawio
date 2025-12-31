module fr.polytech.wid.s7projectskribbl
{
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    exports fr.polytech.wid.s7projectskribbl.client;
    opens fr.polytech.wid.s7projectskribbl.client to javafx.fxml;
    exports fr.polytech.wid.s7projectskribbl.client.network;
    opens fr.polytech.wid.s7projectskribbl.client.network to javafx.fxml;
    exports fr.polytech.wid.s7projectskribbl.client.controller;
    opens fr.polytech.wid.s7projectskribbl.client.controller to javafx.fxml;
}