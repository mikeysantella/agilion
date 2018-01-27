package com.agilion.services.dataengine.interfaceV2;

import dataengine.api.State;

/**
 * Created by Alex_Lappy_486 on 1/26/18.
 */
public class DataOperationStatus
{
    private State state;

    public DataOperationStatus(State state)
    {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
