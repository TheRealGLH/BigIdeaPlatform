package models.classes.objects;

import PlatformGameShared.Enums.InputType;
import PlatformGameShared.Enums.SpriteType;
import PlatformGameShared.Enums.SpriteUpdateType;
import PlatformGameShared.PlatformLogger;
import PlatformGameShared.Points.SpriteUpdate;
import PlatformGameShared.Points.Vector2;
import PlatformGameShared.PropertiesLoader;
import models.classes.GameObject;
import models.enums.WeaponType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Player extends MovableObject {


    private WeaponType currentWeapon = WeaponType.ROCKET;
    private static final int maxLives = Integer.parseInt(PropertiesLoader.getPropValues("game.playerMaxLives", "game.properties"));
    ;
    private int currentLives = maxLives;
    private boolean hasInputMove = false;
    private boolean willJump = false;
    private boolean willShoot = false;
    private boolean shotLastUpdate = false;
    private InputType lastMove;
    private float startX, startY;
    private String name = "-";
    private List<IPlayerEventListener> shootEventListenerList = new ArrayList<>();
    private static float baseSize = 40;
    private float standingHeight = baseSize;
    private float width;
    private boolean ducked = false;

    private int invulnerableTimer = 0;
    private static final int maxInvulnerableTimer = 30;


    private float walkAcceleration = 1;
    private float maxHorizontalAcceleration = 5;

    public Player(float xPos, float yPos) {
        super(xPos, yPos, baseSize, baseSize);
        width = baseSize;
        this.startX = xPos;
        this.startY = yPos;
    }

    /**
     * Gives the Player new inputs to use for the next game tick
     *
     * @param inputType The input we want the player to use
     */
    public void handleInput(InputType inputType) {
        PlatformLogger.Log(Level.FINEST, this + "received input " + inputType);
        switch (inputType) {
            case MOVELEFT:
            case MOVERIGHT:
            case DUCK:
                lastMove = inputType;
                hasInputMove = true;
                break;
            case JUMP:
                willJump = true;
                break;
            case SHOOT:
                willShoot = true;
                break;
        }
    }

    /**
     * Makes the player use their weapon this tick
     */
    public void useWeapon() {
        if (!shotLastUpdate) {
            shotLastUpdate = true;
            for (IPlayerEventListener iPlayerEventListener : shootEventListenerList) {
                iPlayerEventListener.onShootEvent(this);
            }
        }
    }

    /**
     * Kills the player and removes one life.
     */
    public void Kill(boolean forceKill, GameObject origin) {
        if (forceKill | invulnerableTimer <= 0) {
            String deathMessage;

            Level loglevel;
            if (origin == null) {
                deathMessage = this.name + " died in an unusual way. There was no origin.";
                loglevel = Level.SEVERE;
            } else if (origin.equals(this)) {
                loglevel = Level.INFO;
                deathMessage = this.name + "suicided";
            } else if (origin instanceof Player) {
                loglevel = Level.INFO;
                Player other = (Player) origin;
                deathMessage = other.name + " killed " + name + " with " + other.getCurrentWeapon();
            } else {
                loglevel = Level.INFO;
                deathMessage = this.name + " was killed by " + origin.getClass().getName();
            }
            PlatformLogger.Log(loglevel, deathMessage);
            setAcceleration(0, 0);
            setVelocity(0, 0);
            setPosition(startX, startY);
            this.currentLives--;
            this.invulnerableTimer = maxInvulnerableTimer;
            PlatformLogger.Log(Level.FINE, name + " has " + currentLives + " lives left.");
            for (IPlayerEventListener iPlayerEventListener : shootEventListenerList) {
                iPlayerEventListener.onDeathEvent(this);
            }
        }
    }

    public void Kill() {
        Kill(false, this);
    }

    /**
     * Makes the player jump somewhat upwards
     */
    public void jump() {
        if (isGrounded()) {
            addAcceleration(getAcceleration(false), 2.5f);
        }
    }

    public void duck() {
        setSize(width, standingHeight / 1);
        ducked = true;
    }

    public void unDuck() {
        if (ducked) {
            setSize(width, standingHeight);
            ducked = false;
        }
    }

    public void setCurrentWeapon(WeaponType weaponType) {
        this.currentWeapon = weaponType;
    }

    public WeaponType getCurrentWeapon() {
        return this.currentWeapon;
    }

    /**
     * @return The amount of acceleration we have when we're walking
     */
    public float getWalkAcceleration() {
        return walkAcceleration;
    }

    /**
     * Sets the amount of acceleration when walking.
     *
     * @param walkAcceleration The amount to set.
     */
    public void setWalkAcceleration(float walkAcceleration) {
        this.walkAcceleration = walkAcceleration;
    }

    @Override
    public void update() {

        if (invulnerableTimer > 0) {
            invulnerableTimer--;
            if (invulnerableTimer < 1) {
                setChanged();
                notifyObservers(new SpriteUpdate(getObjectNr(), getPosition(), getSize(), SpriteUpdateType.MOVE, getSpriteType(), isFacingLeft(), getLabel()));
            }
        }

        if (hasInputMove) {
            float acc = walkAcceleration;
            if (!isGrounded()) acc = walkAcceleration / 2;
            switch (lastMove) {
                case MOVELEFT:
                    //setAcceleration(-acc,getAcceleration().getY());
                    addAcceleration(-acc, 0);
                    setFacingLeft(true);
                    break;
                case MOVERIGHT:
                    //setAcceleration(acc,getAcceleration().getY());
                    addAcceleration(acc, 0);
                    setFacingLeft(false);
                    break;
                case DUCK:
                    //TODO ducking
                    break;
            }
        } else {
            //if we're not walking, we don't want to have any more X acceleration.
            setAcceleration(0, getAcceleration().getY());
        }
        if (willJump) jump();
        if (willShoot) useWeapon();
        else shotLastUpdate = false;
        hasInputMove = false;
        willJump = false;
        willShoot = false;
        Vector2 acc = getAcceleration();
        //Cap acceleration
        if (acc.getX() > maxHorizontalAcceleration) setAcceleration(maxHorizontalAcceleration, acc.getY());
        if (acc.getX() < -maxHorizontalAcceleration) setAcceleration(-maxHorizontalAcceleration, acc.getY());
        super.update();
    }

    @Override
    public void onCollide(GameObject other, Vector2 collidePoint) {
        //Nobody here but us chickens
    }

    @Override
    public void onOutOfBounds() {
        PlatformLogger.Log(Level.FINE, name + "fell out of the world!");
        Kill(true, this);
    }

    @Override
    public SpriteType getSpriteType() {
        return isInvulnerable() ? SpriteType.PLAYERINVULN : SpriteType.PLAYER;
    }

    @Override
    public String toString() {
        return "Player@" + hashCode() + " Pos: " + getPosition() + " Name: " + name;
    }


    @Override
    public String getLabel() {
        return name + " " + currentWeapon + " \n" +
                "Lives: " + currentLives + " P: " + getPosition();
    }


    /*
    @Override
    public String getLabel() {
        return name + " " + currentWeapon + " P ; " + getPosition() +
                "\n V " + getVelocity() +
                "\n A " + getAcceleration() +
                "\n G " + isGrounded();
    }
     */

    public int getCurrentLives() {
        return currentLives;
    }

    public boolean isInvulnerable() {
        return invulnerableTimer > 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addShootEventListener(IPlayerEventListener shootEventListener) {
        shootEventListenerList.add(shootEventListener);
    }
}
