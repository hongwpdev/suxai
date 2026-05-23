package dev.hongwp.suxai.model;

public class NewsItem {

    private String level;
    private String text;

    public NewsItem() {}

    public NewsItem(String level, String text) {
        this.level = level;
        this.text = text;
    }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
