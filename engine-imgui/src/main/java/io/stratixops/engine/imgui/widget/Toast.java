package io.stratixops.engine.imgui.widget;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImGuiIO;
import io.stratixops.engine.imgui.theme.Theme;
import java.util.List;

/**
 * Toast stack (design system section 3.6): bottom-right, max four visible,
 * 3 px variant border, elevation shadow. Newest sits closest to the corner.
 * Rendered on the foreground draw list so toasts never disturb panel layout.
 */
public final class Toast {

    public static final float WIDTH = 320f;
    public static final float HEIGHT = 64f;
    public static final int MAX_VISIBLE = 4;

    public enum Variant {
        INFO, SUCCESS, WARNING, ERROR, PROGRESS
    }

    /** One toast. {@code body} may be null. */
    public record Message(String title, String body, Variant variant) {
    }

    private Toast() {
    }

    public static void render(Theme t, List<Message> messages) {
        if (messages.isEmpty()) {
            return;
        }
        ImDrawList draw = ImGui.getForegroundDrawList();
        ImGuiIO io = ImGui.getIO();
        float x0 = io.getDisplaySizeX() - WIDTH - t.space.SPACE_4;
        float y = io.getDisplaySizeY() - t.size.SIZE_STATUS_BAR_H - t.space.SPACE_4;

        int visible = Math.min(messages.size(), MAX_VISIBLE);
        for (int i = 0; i < visible; i++) {
            Message message = messages.get(messages.size() - 1 - i);
            y -= HEIGHT + t.space.SPACE_2;
            float y0 = y + t.space.SPACE_2;
            draw.addRectFilled(x0, y0, x0 + WIDTH, y0 + HEIGHT, Ui.u32(t.color.BG_ELEVATED),
                    t.radius.RADIUS_MD);
            draw.addRect(x0, y0, x0 + WIDTH, y0 + HEIGHT, Ui.u32(t.color.BORDER_DEFAULT),
                    t.radius.RADIUS_MD, 1f);
            draw.addRectFilled(x0, y0, x0 + 3f, y0 + HEIGHT, Ui.u32(variantColor(t, message.variant())),
                    t.radius.RADIUS_XS);
            draw.addText(x0 + t.space.SPACE_3, y0 + t.space.SPACE_3,
                    Ui.u32(t.color.TEXT_PRIMARY), message.title());
            if (message.body() != null) {
                draw.addText(x0 + t.space.SPACE_3, y0 + t.space.SPACE_3 + 16f,
                        Ui.u32(t.color.TEXT_MUTED),
                        Ui.ellipsize(message.body(), WIDTH - t.space.SPACE_3 * 2));
            }
        }
    }

    private static int variantColor(Theme t, Variant variant) {
        return switch (variant) {
            case SUCCESS -> t.color.STATE_SUCCESS;
            case WARNING -> t.color.STATE_WARN;
            case ERROR -> t.color.STATE_ERROR;
            case PROGRESS -> t.color.ACCENT_DEFAULT;
            case INFO -> t.color.STATE_INFO;
        };
    }
}
