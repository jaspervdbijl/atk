package com.acutus.atk.entity;

public interface ChangeListener<T> {
    void changed(T oldValue, T newValue);
}
