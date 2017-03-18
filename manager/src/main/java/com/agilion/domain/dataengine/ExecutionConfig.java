package com.agilion.domain.dataengine;

/**
 * Created by Alex_Lappy_486 on 3/5/17.
 */
public class ExecutionConfig
{
    public ExecutionConfig()
    {

    }

    public ExecutionConfig(ExecutionOrder order, int step)
    {
        this.order = order;
        this.step = step;
    }

    private ExecutionOrder order;
    private int step;

    public ExecutionOrder getOrder() {
        return order;
    }

    public void setOrder(ExecutionOrder order) {
        this.order = order;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }
}
