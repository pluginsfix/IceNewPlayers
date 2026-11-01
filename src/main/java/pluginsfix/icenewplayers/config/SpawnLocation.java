package pluginsfix.icenewplayers.config;

public record SpawnLocation(
        String world,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {
    public static SpawnLocation defaultLocation() {
        return new SpawnLocation("world", 0.5, 64.0, 0.5, 0.0f, 0.0f);
    }
}
