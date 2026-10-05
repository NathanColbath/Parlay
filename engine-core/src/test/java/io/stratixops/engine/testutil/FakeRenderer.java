package io.stratixops.engine.testutil;

import io.stratixops.engine.Camera2D;
import io.stratixops.engine.Renderer;
import io.stratixops.engine.Sprite;

/** No-op {@link Renderer} that counts calls for headless scene tests. */
public final class FakeRenderer implements Renderer {

    public int clears;
    public int draws;
    public int rects;
    public int texts;
    public int pushes;
    public int pops;

    @Override
    public void clear(int rgba) {
        clears++;
    }

    @Override
    public void draw(Sprite sprite, float x, float y) {
        draws++;
    }

    @Override
    public void fillRect(float x, float y, float w, float h, int rgba) {
        rects++;
    }

    @Override
    public void drawText(String text, float x, float y) {
        texts++;
    }

    @Override
    public void pushCamera(Camera2D camera) {
        pushes++;
    }

    @Override
    public void popCamera() {
        pops++;
    }
}
