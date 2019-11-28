package models.classes.objects;

import SharedClasses.Vector2;
import models.classes.GameObject;

public abstract class MovableObject extends GameObject {
    private Vector2 acceleration;
    private Vector2 velocity;
    private float maxHorizontalVelocity = 20, maxVerticalVelocity = 20;
    private Vector2 maxVelocity = new Vector2(maxHorizontalVelocity, maxVerticalVelocity);
    private boolean useGravity;

    public MovableObject(float xPosition, float yPosition, float width, float height) {
        super(xPosition, yPosition, width, height);
        useGravity = true;
    }

    public abstract void onCollide(GameObject other, Vector2 collidePoint);

    public boolean isGrounded() {
        //TODO proper implementation
        return this.getPosition().getY() <= 0;
    }

    public void update() {
        throw new UnsupportedOperationException("MovableObject update() not yet implemented");
    }

    public void addAcceleration(float x, float y) {
        acceleration = new Vector2(acceleration.getX() + x, acceleration.getY() + y);
    }

    public void Kill(){
        //Send delete spriteupdate and event notification
        throw new UnsupportedOperationException("Method Kill() has not yet been implemented");
    }

    public void setAcceleration(float x, float y) {
        acceleration = new Vector2(x, y);
    }

    public Vector2 getAcceleration() {
        return acceleration;
    }

    public float getAcceleration(boolean isUpward) {
        return (isUpward) ? acceleration.getY() : acceleration.getX();
    }

    public void setVelocity(float x, float y) {
        velocity = new Vector2(x, y);
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public float getVelocity(boolean isUpward) {
        return (isUpward) ? velocity.getY() : velocity.getX();
    }

    public boolean isUseGravity() {
        return useGravity;
    }

    public void setUseGravity(boolean useGravity) {
        this.useGravity = useGravity;
    }
}
