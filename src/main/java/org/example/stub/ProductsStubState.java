package org.example.stub;

import org.springframework.stereotype.Component;

@Component
public class ProductsStubState {

    public enum Mode { OK, SLOW, ERROR500, NOT_FOUND }

    private volatile Mode mode = Mode.OK;
    private volatile int delayMs = 0;

    public Mode getMode() { return mode; }
    public void setMode(Mode mode) { this.mode = mode; }

    public int getDelayMs() { return delayMs; }
    public void setDelayMs(int delayMs) { this.delayMs = delayMs; }
}
