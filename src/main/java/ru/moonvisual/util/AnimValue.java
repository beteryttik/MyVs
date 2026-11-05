package ru.moonvisual.util;

public class AnimValue {
    public float value;

    public void approach(float target, float speed) {
        value += (target - value) * speed;
    }
}
