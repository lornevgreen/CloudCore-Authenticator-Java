package com.cloudcore.authenticator.core;

public class Response {


    public String fullRequest;
    public String fullResponse;
    public boolean success;
    public String outcome;
    public int milliseconds;

    public Response(boolean success, String outcome, int milliseconds, String fullRequest, String fullResponse) {
        this.success = success;
        this.outcome = outcome;
        this.milliseconds = milliseconds;
        this.fullRequest = fullRequest;
        this.fullResponse = fullResponse;
    }

    public Response() {
        this.success = false;
        this.outcome = "not used";
        this.milliseconds = 0;
        this.fullRequest = "No request";
        this.fullResponse = "No response";
    }
}
