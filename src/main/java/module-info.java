module fr.polytech.wid.s7projectskribbl
{
    requires javafx.controls;
    requires javafx.fxml;

    exports fr.polytech.wid.s7projectskribbl.client;
    opens fr.polytech.wid.s7projectskribbl.client to javafx.fxml;
}