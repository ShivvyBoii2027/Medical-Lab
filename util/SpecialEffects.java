/*
 * MediLab Pro — SpecialEffects Utility
 * High-High Impact animations designed to impress and excite.
 * Emerald Glows for Success / Haptic Shakes for Faults.
 */
package util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class SpecialEffects {

    /**
     * Emerald Pulse & Scale: For successful lab approvals or high-quality results.
     */
    public static void playClinicalSuccess(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(300), node);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.05);
        st.setToY(1.05);
        st.setCycleCount(2);
        st.setAutoReverse(true);

        DropShadow ds = new DropShadow();
        ds.setColor(Color.web(Theme.COL_SUCCESS));
        ds.setRadius(0);
        node.setEffect(ds);

        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(ds.radiusProperty(), 0)),
                new KeyFrame(Duration.millis(400), new KeyValue(ds.radiusProperty(), 40)));
        tl.setCycleCount(2);
        tl.setAutoReverse(true);
        tl.setOnFinished(e -> node.setEffect(null));

        new ParallelTransition(st, tl).play();
    }

    /**
     * Haptic Shake & Red Pulse: For rejections or critical diagnostic alerts.
     */
    public static void playClinicalFault(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(6);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);

        ColorAdjust ca = new ColorAdjust();
        node.setEffect(ca);

        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(ca.hueProperty(), 0),
                        new KeyValue(ca.saturationProperty(), 0)),
                new KeyFrame(Duration.millis(200), new KeyValue(ca.hueProperty(), -0.7),
                        new KeyValue(ca.saturationProperty(), 0.8)));
        tl.setCycleCount(2);
        tl.setAutoReverse(true);
        tl.setOnFinished(e -> node.setEffect(null));

        new ParallelTransition(tt, tl).play();
    }

    /**
     * Holographic Scanline Pulse: For general system confirmations.
     */
    public static void playHoloPulse(Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(400), node);
        ft.setFromValue(1.0);
        ft.setToValue(0.6);
        ft.setCycleCount(4);
        ft.setAutoReverse(true);
        ft.play();
    }
}
