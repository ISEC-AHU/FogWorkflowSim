package org.workflowsim.utils;

public class TSPDecisionResult {
    private final double time;
    private final int[] action;

    public TSPDecisionResult(double time, int[] action) {
        this.time = time;
        this.action = action;
    }

    public double getTime() {
        return time;
    }

    public int[] getAction() {
        return action;
    }
}