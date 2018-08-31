package test;

import device.DialogFlowWebHook;
import org.junit.Test;


public class TestDialogFlow {

    @Test
    public void test01() {
        DialogFlowWebHook webHook = new DialogFlowWebHook();

        webHook.addOnAction("LightsON", (param) -> {return "Luci accese";});
        webHook.addOnAction("LightsOFF", (param) -> {return "Luci spente";});

        webHook.startServer();
    }
}
