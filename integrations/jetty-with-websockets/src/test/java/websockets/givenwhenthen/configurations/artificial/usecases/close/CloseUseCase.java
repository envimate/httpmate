package websockets.givenwhenthen.configurations.artificial.usecases.close;

import websockets.givenwhenthen.configurations.artificial.ArtificialConfiguration;

import static websockets.givenwhenthen.configurations.artificial.usecases.close.CloseEvent.closeEvent;

public final class CloseUseCase {

    public void closeAllWebSockets() {
        ArtificialConfiguration.MESSAGE_BUS.send("CloseEvent", closeEvent());
    }
}
