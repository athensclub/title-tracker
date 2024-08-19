package athensclub.android.titletracker;

import android.transition.Scene;
import android.transition.TransitionManager;

public class SceneManager {

    private Scene current;

    public void setScene(Scene scene) {
        current = scene;
        TransitionManager.go(scene);
    }

    public Scene getCurrentScene() {
        return current;
    }
}
