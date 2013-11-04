package bits.hidpunk.movementmouse;

public interface MovementMouseListener {
    public void mouseMoved(long timeMicros, int dx, int dy);
    public void mouseButtonPressed(long timeMicros, int button);
    public void mouseButtonReleased(long timeMicros, int button);
}
