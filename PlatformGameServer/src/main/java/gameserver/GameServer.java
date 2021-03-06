package gameserver;

import PlatformGameShared.Enums.GameState;
import PlatformGameShared.Enums.InputType;
import PlatformGameShared.Enums.LoginState;
import PlatformGameShared.Enums.RegisterState;
import PlatformGameShared.Interfaces.IPlatformGameClient;
import PlatformGameShared.Interfaces.IPlatformGameServer;
import PlatformGameShared.PlatformLogger;
import PlatformGameShared.Points.GameLevel;
import PlatformGameShared.Points.GameStateEvent;
import PlatformGameShared.Points.SpriteUpdate;
import PlatformGameShared.PropertiesLoader;
import com.google.gson.Gson;
import loginclient.IPlatformLoginClient;
import loginclient.PlatformLoginClientMock;
import loginclient.PlatformLoginClientREST;
import models.classes.GameTimerTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;

/**
 * The Game container, will be controlled by an instance of IPlatformGameClient (later via websockets and
 * send it to an instance of this Interface (which will later contain websockets)
 */
public class GameServer implements IPlatformGameServer {
    GameTimerTask gameTimerTask;
    List<IPlatformGameClient> joinedClients;//clients which are logged in
    IPlatformGameClient lobbyLeader;//The first client to join, unless they leave, then it's whatever remaining client joined first
    IPlatformLoginClient loginClient; //REST communicator
    Gson gson = new Gson();
    int minAmountOfPlayers = Integer.parseInt(PropertiesLoader.getPropValues("gameServer.minPlayers","application.properties"));
    Timer timer = new Timer();
    String[] maps;
    String currentMap;

    boolean gameStarted = false;

    public GameServer() {
        if (PropertiesLoader.getPropValues("RESTClient.useMock", "application.properties").equals("true")) {
            loginClient = new PlatformLoginClientMock();
        } else {
            loginClient = new PlatformLoginClientREST();
        }
        joinedClients = new ArrayList<>();
        String json = loginClient.getLevelNames();
        maps = gson.fromJson(json, String[].class);
        currentMap = maps[0];
    }

    @Override
    public void registerPlayer(String name, String password, IPlatformGameClient client) {
        if (joinedClients.contains(client)) return;
        PlatformLogger.Log(Level.INFO, "Registering " + name);
        RegisterState registerState = loginClient.attemptRegistration(name, password);
        if (registerState != RegisterState.ERROR) {//We already print a rather verbose error when the RegisterState == ERROR, so there's not need to print this
            PlatformLogger.Log(Level.INFO, "Attempt to register " + name + ". state: " + registerState);
        }
        client.receiveRegisterState(name, registerState);
    }

    @Override
    public void loginPlayer(String name, String password, IPlatformGameClient client) {
        if (joinedClients.contains(client)) return;
        PlatformLogger.Log(Level.INFO, name + " attempted to log in " + this.toString());
        LoginState loginState = loginClient.attemptLogin(name, password);
        if (loginState != LoginState.ERROR) { //We already print a rather verbose error when the LoginState == ERROR, so there's not need to print this
            Level loginStateLoggingLevel = Level.FINE;
            if (loginState == LoginState.SUCCESS) loginStateLoggingLevel = Level.INFO;
            PlatformLogger.Log(loginStateLoggingLevel, "Login status for " + name + ": " + loginState);
        }
        client.receiveLoginState(name, loginState);
        if (loginState == LoginState.SUCCESS) {
            client.setName(name);
            joinedClients.add(client);
            if (joinedClients.size() == 1) lobbyLeader = client;
            notifyClientsNames();
            client.lobbyMapNamesNotify(maps);
            client.lobbyNotifyNewMapChoice(currentMap);
        }
    }

    @Override
    public void attemptStartGame(IPlatformGameClient platformGameClient) {
        if (!gameStarted) {
            if (joinedClients.contains(platformGameClient)) {
                if (joinedClients.size() >= minAmountOfPlayers) {
                    int size = joinedClients.size();
                    String[] names = new String[size];
                    int[] playerNrs = new int[size];
                    for (int i = 0; i < size; i++) {
                        names[i] = joinedClients.get(i).getName();
                        playerNrs[i] = joinedClients.get(i).getPlayerNr();
                    }
                    GameLevel gameLevel = gson.fromJson(loginClient.getLevelContent(currentMap), GameLevel.class);
                    gameTimerTask = new GameTimerTask(this, playerNrs, names, gameLevel);
                    for (IPlatformGameClient joinedClient : joinedClients) joinedClient.gameStartNotification();
                    //TODO send stuff to the GameTimerTask, like the map perhaps
                    timer.schedule(gameTimerTask, 1000, GameTimerTask.tickRate);
                    gameStarted = true;
                } else {
                    PlatformLogger.Log(Level.FINE, "A request to start the game was denied because of too few players");
                }
            }
        } else {
            PlatformLogger.Log(Level.WARNING, "A game start attempt was made, but the game has already begun.", platformGameClient);
        }
    }

    @Override
    public synchronized void receiveInput(InputType type, IPlatformGameClient client) {
        if (joinedClients.contains(client)) {
            gameTimerTask.sendInput(client.getPlayerNr(), type);
        }
    }

    @Override
    public void sendSpriteUpdates(List<SpriteUpdate> spriteUpdateList) {
        if (spriteUpdateList.size() > 0) {
            PlatformLogger.Log(Level.FINER, "Sending updates: " + spriteUpdateList);
            for (IPlatformGameClient platformGameClient : joinedClients) {
                platformGameClient.updateScreen(spriteUpdateList);
            }
        }

    }

    @Override
    public void removePlayer(IPlatformGameClient client) {
        if (joinedClients.contains(client)) {
            PlatformLogger.Log(Level.INFO, "Removing player: " + client.getName());
            joinedClients.remove(client);
            //TODO notify our GameTimerTask
            if (lobbyLeader.equals(client)) {
                lobbyLeader = joinedClients.get(0);
                PlatformLogger.Log(Level.FINE, lobbyLeader + " is now the lobby leader.");
            }
            if (joinedClients.size() > 0) {
                notifyClientsNames();
            } else {
                PlatformLogger.Log(Level.INFO, "The game was cancelled, because there are no players left.");
                gameTimerTask.cancel();
            }
        }
    }

    @Override
    public void selectLobbyMap(IPlatformGameClient client, String mapName) {
        if (client.equals(lobbyLeader)) {
            //we need to actually test if the map is in our list, don't want clients to send bogus names and mess us up
            boolean mapExists = false;
            for (String map : maps) {
                if (mapName.equals(map)) mapExists = true;
            }
            if (mapExists) {
                PlatformLogger.Log(Level.INFO, client.getName() + " selected the map: " + mapName);
                currentMap = mapName;
                notifyClientsMapSelected();
            } else {
                PlatformLogger.Log(Level.WARNING, "Client: " + client.getName() + " tried to select invalid map: " + mapName);
            }
        } else {
            PlatformLogger.Log(Level.WARNING, "Client: " + client.getAddress() + " tried to select a map, but they are not the lobby leader!");
        }
    }

    @Override
    public void sendGameStateEvent(GameStateEvent gameState) {
        for (IPlatformGameClient platformGameClient : joinedClients) {
            platformGameClient.receiveGameState(gameState);
        }
        if (gameState.getState() == GameState.GAMEOVER) {
            gameTimerTask.cancel();
            gameStarted = false;
            notifyClientsNames();
            notifyClientsMapList();
            notifyClientsMapSelected();
            loginClient.sendGameEnd(currentMap, gameState.getName(), getJoinedClientNamesAsArray());
        }
    }

    @Override
    public void sendInputRequest() {
        for (IPlatformGameClient platformGameClient : joinedClients) {
            platformGameClient.receiveAllowInput();
        }
    }

    void notifyClientsNames() {
        String names[] = new String[joinedClients.size()];
        for (int i = 0; i < joinedClients.size(); i++) {
            IPlatformGameClient currClient = joinedClients.get(i);
            if (currClient.equals(lobbyLeader)) {
                names[i] = currClient.getName() + " (Leader)";
            } else {
                names[i] = joinedClients.get(i).getName();
            }
        }
        PlatformLogger.Log(Level.INFO, "There are currently " + joinedClients.size() + " players left.");
        for (IPlatformGameClient platformGameClient : joinedClients) {
            platformGameClient.lobbyJoinedNotify(names);
        }
    }

    void notifyClientsMapList() {
        for (IPlatformGameClient client : joinedClients) {
            client.lobbyMapNamesNotify(maps);
        }
    }

    void notifyClientsMapSelected() {
        for (IPlatformGameClient joinedClient : joinedClients) {
            joinedClient.lobbyNotifyNewMapChoice(currentMap);
        }
    }

    String[] getJoinedClientNamesAsArray() {
        String names[] = new String[joinedClients.size()];
        for (int i = 0; i < joinedClients.size(); i++) {
            IPlatformGameClient currClient = joinedClients.get(i);
            names[i] = joinedClients.get(i).getName();

        }
        return names;
    }
}
