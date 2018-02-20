package adk.string;

/**
 * Crafted by Adk on 12/26/2017.
 * Every time a FluidicElement's position is changed, the onPositionUpdate is invoked. Useful for
 * UI animations based on the position of the FludicElements.
 */
public interface FludicPositionChangeListener {
    public void onPositionChanged(float newX, float newY);
}
