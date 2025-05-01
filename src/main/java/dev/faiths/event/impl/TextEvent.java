package dev.faiths.event.impl;


import dev.faiths.event.Event;

public class TextEvent extends Event {

   public String string;

    public TextEvent(String string) {
        this.string = string;
    }

    public String getText() {
        return string;
    }

    public void setText(String pass) {
        this.string = pass;
    }
}
