package com.soen342.util;

public interface Observable {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyall();
}
