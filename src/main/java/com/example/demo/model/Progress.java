package com.example.demo.model;

public enum Progress {
    BACKLOG(0),
    TODO(1),
    IN_PROGRESS(2),
    QA(3),
    DONE(4);

    private final int value;
    Progress(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
