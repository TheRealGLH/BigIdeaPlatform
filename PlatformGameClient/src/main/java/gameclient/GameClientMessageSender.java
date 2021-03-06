package gameclient;

import PlatformGameShared.Enums.InputType;
import PlatformGameShared.Interfaces.IPlatformGameClient;
import PlatformGameShared.Interfaces.IPlatformGameServer;
import PlatformGameShared.Messages.Client.*;
import PlatformGameShared.Points.GameStateEvent;
import PlatformGameShared.Points.SpriteUpdate;

import java.util.List;

public class GameClientMessageSender implements IPlatformGameServer {

    private ICommunicator communicator = CommunicatorClientWebSocketEndpoint.getInstance();

    public GameClientMessageSender(){
        communicator.start();
    }

    @Override
    public void registerPlayer(String name, String password, IPlatformGameClient client) {
        communicator.setGameClient(client);
        PlatformGameMessageRegister messageRegister = new PlatformGameMessageRegister(name, password);
        communicator.sendMessage(messageRegister);
    }

    @Override
    public void loginPlayer(String name, String password, IPlatformGameClient client) {
        communicator.setGameClient(client);
        PlatformGameMessageLogin messageLogin = new PlatformGameMessageLogin(name, password);
        communicator.sendMessage(messageLogin);
    }

    @Override
    public void attemptStartGame(IPlatformGameClient client) {
        PlatformGameMessageStart messageStart = new PlatformGameMessageStart();
        communicator.sendMessage(messageStart);
    }

    @Override
    public void receiveInput(InputType type, IPlatformGameClient client) {
        PlatformGameMessageInput messageInput = new PlatformGameMessageInput(type);
        communicator.sendMessage(messageInput);
    }

    @Override
    public void sendSpriteUpdates(List<SpriteUpdate> spriteUpdateList) {
        throw new UnsupportedOperationException("This method is not supposed to be called on the client");
    }

    @Override
    public void removePlayer(IPlatformGameClient client) {
        throw new UnsupportedOperationException("This method is not supposed to be called on the client");
    }

    @Override
    public void selectLobbyMap(IPlatformGameClient client, String mapName) {
        PlatformGameMessageMapChange messageMapChange = new PlatformGameMessageMapChange(mapName);
        communicator.sendMessage(messageMapChange);
    }

    @Override
    public void sendGameStateEvent(GameStateEvent gameState) {
        throw new UnsupportedOperationException("This method is not supposed to be called on the client");
    }

    @Override
    public void sendInputRequest() {
        throw new UnsupportedOperationException("This method is not supposed to be called on the client");
    }
}
