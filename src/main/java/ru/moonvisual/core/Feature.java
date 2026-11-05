package ru.moonvisual.core;

public class Feature {
    public enum Type { TOGGLE, SLIDER, COLOR, SELECT }

    public final String id, nameEn, nameRu, category;
    public final Type type;
    public final String[] options;
    public final float max;
    public final boolean implemented;
    public float value;
    public boolean enabled;

    public Feature(String id, String nameEn, String nameRu, String category, Type type,
                   float value, float max, String[] options, boolean implemented) {
        this.id = id;
        this.nameEn = nameEn;
        this.nameRu = nameRu;
        this.category = category;
        this.type = type;
        this.max = max;
        this.options = options;
        this.implemented = implemented;
        this.value = value;
        this.enabled = type == Type.TOGGLE && value >= 0.5f;
    }

    public boolean active() {
        return type == Type.TOGGLE ? enabled : true;
    }

    public String displayName() {
        return ru.moonvisual.lang.Lang.t("feature." + id + ".name");
    }

    public String optionName(int i) {
        if (options == null || i < 0 || i >= options.length) return "?";
        return options[i];
    }
}
