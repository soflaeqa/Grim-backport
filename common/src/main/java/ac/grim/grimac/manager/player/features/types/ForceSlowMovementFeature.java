package ac.grim.grimac.manager.player.features.types;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.feature.FeatureState;
import ac.grim.grimac.player.GrimPlayer;

public class ForceSlowMovementFeature implements GrimFeature {

    @Override
    public String getName() {
        return "ForceSlowMovement";
    }

    @Override
    public void setState(GrimPlayer player, ConfigManager config, FeatureState state) {
        switch (state) {
            case ENABLED:
                player.setForceSlowMovement(true);
                break;
            case DISABLED:
                player.setForceSlowMovement(false);
                break;
            default:
                player.setForceSlowMovement(isEnabledInConfig(player, config));
                break;
        }
    }

    @Override
    public boolean isEnabled(GrimPlayer player) {
        return player.isForceSlowMovement();
    }

    @Override
    public boolean isEnabledInConfig(GrimPlayer player, ConfigManager config) {
        return config.getBooleanElse("force-slow-movement", true);
    }

}
