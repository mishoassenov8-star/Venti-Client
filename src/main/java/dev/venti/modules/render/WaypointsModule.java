package dev.venti.modules.render;

import dev.venti.config.BooleanSetting;
import dev.venti.config.ColorSetting;
import dev.venti.config.NumberSetting;
import dev.venti.config.StringListSetting;
import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import dev.venti.module.Modules;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Saved places drawn as on-screen markers with their distance.
 *
 * <p>Entries are {@code "Name x y z"}, optionally with a trailing {@code nether}, {@code end} or
 * {@code overworld} to say which dimension they belong to; the y may be omitted. Markers are
 * projected from world space with the camera's own yaw, pitch and FOV, so they sit over the place
 * they name and are only drawn while that place is in front of you — there is no through-wall
 * outline and nothing is sent anywhere. Nether and overworld entries are scaled by 8 when shown
 * from the other side, like the coordinates themselves.</p>
 */
public final class WaypointsModule extends Module {
    private static final Pattern SPLIT = Pattern.compile("[\\s,]+");
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

    private final StringListSetting points = this.add("points", new StringListSetting("Waypoints"));
    private final BooleanSetting otherDimension = this.add("other-dimension", new BooleanSetting("Show In Other Dimension", true));
    private final BooleanSetting showDistance = this.add("show-distance", new BooleanSetting("Show Distance", true));
    private final NumberSetting maxDistance = this.add("max-distance", new NumberSetting("Max Distance", 0.0f, 0.0f, 30000.0f).step(100.0f));
    private final NumberSetting hideWithin = this.add("hide-within", new NumberSetting("Hide Within", 3.0f, 0.0f, 32.0f).step(1.0f));
    private final NumberSetting scale = this.add("scale", new NumberSetting("Scale", 1.0f, 0.5f, 2.0f).step(0.1f));
    private final ColorSetting color = this.add("color", new ColorSetting("Color", 0xFF4FC3F7));
    private final BooleanSetting deaths = this.add("deaths", new BooleanSetting("Death Waypoints", true));
    private final NumberSetting deathMemory = this.add("death-memory", new NumberSetting("Death Memory", 3.0f, 1.0f, 10.0f).step(1.0f));

    private List<Point> parsed;
    private boolean wasDead;

    public WaypointsModule() {
        super("waypoints", "Waypoints", "Saved places shown on screen with distance. Entries are \"Name x y z\", optionally ending in nether or end.", ModuleCategory.RENDER);
        this.points.onChange(() -> this.parsed = null);
        this.deathMemory.visibleWhen(() -> this.deaths.get());
    }

    public static WaypointsModule active() {
        WaypointsModule module = Modules.get(WaypointsModule.class);
        return module != null && module.isEnabled() ? module : null;
    }

    @Override
    protected void onEnable() {
        this.parsed = null;
        this.wasDead = false;
    }

    @Override
    protected void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) {
            this.wasDead = false;
            return;
        }
        boolean dead = mc.player.isDead();
        if (dead && !this.wasDead && this.deaths.get()) {
            this.forgetOldDeaths(this.deathMemory.get().intValue() - 1);
            this.save("Death " + LocalTime.now().format(CLOCK),
                    mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ(), mc.world.getRegistryKey());
        }
        this.wasDead = dead;
    }

    /** Called from the HUD element registered in {@code VentiMod}. */
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden || mc.gameRenderer == null) {
            return;
        }
        Camera camera = mc.gameRenderer.getCamera();
        if (camera == null || !camera.isReady()) {
            return;
        }
        Vec3d eye = camera.getCameraPos();
        RegistryKey<World> here = mc.world.getRegistryKey();
        double limit = this.maxDistance.get();

        List<Shown> visible = new ArrayList<>();
        for (Point point : this.parsed()) {
            Shown shown = this.place(point, here, eye);
            if (shown == null || (limit > 0.0 && shown.distance() > limit)) {
                continue;
            }
            visible.add(shown);
        }
        visible.sort(Comparator.comparingDouble(Shown::distance).reversed());

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        double fovDegrees = Math.max(30.0, Math.min(160.0, mc.options.getFov().getValue().doubleValue()));
        float fov = (float) Math.toRadians(fovDegrees);
        double focal = (height / 2.0) / Math.tan(fov / 2.0);

        for (Shown shown : visible) {
            this.draw(context, mc, camera, eye, shown, focal, width, height);
        }
    }

    private void draw(DrawContext context, MinecraftClient mc, Camera camera, Vec3d eye, Shown shown,
                      double focal, int width, int height) {
        float fade = Math.min(1.0f, Math.max(0.0f, (float) (shown.distance() - this.hideWithin.get()) / 4.0f));
        if (fade <= 0.02f) {
            return;
        }
        double[] screen = project(shown.at(), eye, camera, focal, width, height);
        if (screen == null) {
            return;
        }
        float size = this.scale.get();
        int x = (int) Math.round(screen[0]);
        int y = (int) Math.round(screen[1]);
        if (x < -300 || x > width + 300 || y < -300 || y > height + 300) {
            return;
        }

        int accent = withAlpha(this.color.argb(), fade);
        String title = shown.point().name();
        String detail = this.showDistance.get() ? distance(shown.distance()) : null;
        if (shown.converted()) {
            detail = detail == null ? "(" + dimensionName(shown.point().dimension()) + ")"
                    : detail + " (" + dimensionName(shown.point().dimension()) + ")";
        }

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(size, size);

        context.fill(-2, -2, 3, 3, withAlpha(0x101014, fade * 0.8f));
        context.fill(-1, -1, 2, 2, accent);

        String label = detail == null ? title : title + "  " + detail;
        int textWidth = mc.textRenderer.getWidth(label);
        int labelY = -13;
        context.fill(-textWidth / 2 - 2, labelY - 1, textWidth / 2 + 2, labelY + 9, withAlpha(0x101014, fade * 0.55f));
        context.drawText(mc.textRenderer, label, -textWidth / 2, labelY, accent, true);

        context.getMatrices().popMatrix();
    }

    /** Projects a world position to screen space, or null when it is behind the camera. */
    private static double[] project(Vec3d at, Vec3d eye, Camera camera, double focal, int width, int height) {
        double dx = at.x - eye.x;
        double dy = at.y - eye.y;
        double dz = at.z - eye.z;

        double yaw = Math.toRadians(camera.getYaw());
        double pitch = Math.toRadians(camera.getPitch());
        double sinYaw = Math.sin(yaw);
        double cosYaw = Math.cos(yaw);
        double sinPitch = Math.sin(pitch);
        double cosPitch = Math.cos(pitch);

        double forwardX = -sinYaw * cosPitch;
        double forwardY = -sinPitch;
        double forwardZ = cosYaw * cosPitch;
        double rightX = -cosYaw;
        double rightZ = -sinYaw;
        double upX = -rightZ * forwardY;
        double upY = rightZ * forwardX - rightX * forwardZ;
        double upZ = rightX * forwardY;

        double depth = dx * forwardX + dy * forwardY + dz * forwardZ;
        if (depth <= 0.1) {
            return null;
        }
        double camX = dx * rightX + dz * rightZ;
        double camY = dx * upX + dy * upY + dz * upZ;
        return new double[] {
                width / 2.0 + camX / depth * focal,
                height / 2.0 - camY / depth * focal
        };
    }

    private Shown place(Point point, RegistryKey<World> here, Vec3d eye) {
        double x = point.x() + 0.5;
        double z = point.z() + 0.5;
        boolean converted = false;
        if (!point.dimension().equals(here)) {
            if (!this.otherDimension.get()) {
                return null;
            }
            if (point.dimension() == World.NETHER && here == World.OVERWORLD) {
                x /= 8.0;
                z /= 8.0;
            } else if (point.dimension() == World.OVERWORLD && here == World.NETHER) {
                x *= 8.0;
                z *= 8.0;
            } else {
                return null;
            }
            converted = true;
        }
        double y = point.y() == null || converted ? eye.y : point.y() + 0.5;
        Vec3d at = new Vec3d(x, y, z);
        return new Shown(point, at, at.distanceTo(eye), converted);
    }

    private void save(String name, int x, int y, int z, RegistryKey<World> dimension) {
        this.points.get().add(name + " " + x + " " + y + " " + z + suffix(dimension));
        this.points.edited();
        this.parsed = null;
        this.settings().save();
        this.chat(net.minecraft.text.Text.literal("saved " + name + " at " + x + ", " + y + ", " + z));
    }

    private void forgetOldDeaths(int keep) {
        List<String> entries = this.points.get();
        List<Integer> deaths = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            Point point = parse(entries.get(i));
            if (point != null && point.death()) {
                deaths.add(i);
            }
        }
        for (int i = deaths.size() - keep - 1; i >= 0; i--) {
            entries.remove((int) deaths.get(i));
        }
        this.points.edited();
        this.parsed = null;
    }

    private List<Point> parsed() {
        if (this.parsed == null) {
            List<Point> out = new ArrayList<>();
            for (String entry : this.points.get()) {
                Point point = parse(entry);
                if (point != null) {
                    out.add(point);
                }
            }
            this.parsed = List.copyOf(out);
        }
        return this.parsed;
    }

    private static Point parse(String entry) {
        List<String> tokens = new ArrayList<>(List.of(SPLIT.split(entry.trim())));
        tokens.removeIf(String::isEmpty);
        if (tokens.isEmpty()) {
            return null;
        }
        RegistryKey<World> dimension = World.OVERWORLD;
        RegistryKey<World> named = dimension(tokens.get(tokens.size() - 1));
        if (named != null) {
            dimension = named;
            tokens.remove(tokens.size() - 1);
        }
        List<Integer> numbers = new ArrayList<>();
        while (!tokens.isEmpty() && numbers.size() < 3) {
            Integer value = number(tokens.get(tokens.size() - 1));
            if (value == null) {
                break;
            }
            numbers.add(0, value);
            tokens.remove(tokens.size() - 1);
        }
        if (numbers.size() < 2) {
            return null;
        }
        String name = String.join(" ", tokens).trim();
        if (name.isEmpty()) {
            name = "Waypoint";
        }
        boolean death = name.startsWith("Death ");
        return numbers.size() == 3
                ? new Point(name, numbers.get(0), numbers.get(1), numbers.get(2), dimension, death)
                : new Point(name, numbers.get(0), null, numbers.get(1), dimension, death);
    }

    private static Integer number(String token) {
        try {
            return (int) Math.floor(Double.parseDouble(token));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static RegistryKey<World> dimension(String token) {
        return switch (token.toLowerCase(Locale.ROOT)) {
            case "nether" -> World.NETHER;
            case "end" -> World.END;
            case "overworld" -> World.OVERWORLD;
            default -> null;
        };
    }

    private static String suffix(RegistryKey<World> dimension) {
        if (dimension == World.NETHER) {
            return " nether";
        }
        if (dimension == World.END) {
            return " end";
        }
        return "";
    }

    private static String dimensionName(RegistryKey<World> dimension) {
        if (dimension == World.NETHER) {
            return "nether";
        }
        if (dimension == World.END) {
            return "end";
        }
        return "overworld";
    }

    private static String distance(double blocks) {
        if (blocks >= 1000.0) {
            return String.format(Locale.ROOT, "%.1fkm", blocks / 1000.0);
        }
        return String.format(Locale.ROOT, "%dm", (int) Math.round(blocks));
    }

    private static int withAlpha(int argb, float factor) {
        int alpha = Math.max(0, Math.min(255, (int) (((argb >>> 24) & 0xFF) * factor)));
        return (alpha << 24) | (argb & 0x00FFFFFF);
    }

    private record Point(String name, int x, Integer y, int z, RegistryKey<World> dimension, boolean death) {
    }

    private record Shown(Point point, Vec3d at, double distance, boolean converted) {
    }
}
