package com.cloudcore.authenticator.raida;

import com.cloudcore.authenticator.core.Config;
import com.cloudcore.authenticator.utils.Utils;
import com.google.gson.Gson;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.asynchttpclient.*;

import static org.asynchttpclient.Dsl.asyncHttpClient;

/*
 * This Class Contains the properties of a RAIDA node.
 */
public class Node {

    private AsyncHttpClient client;
    private Gson gson;

    public int nodeNumber;
    public String fullUrl;
    public MultiDetectResponse multiResponse = new MultiDetectResponse();

    //Constructor
    public Node(int NodeNumber) {
        this.nodeNumber = NodeNumber;
        fullUrl = GetFullURL();
        System.out.println(fullUrl);

        client = asyncHttpClient();
        gson = Utils.createGson();
    }

    public Node(int NodeNumber, RAIDANode node) {
        this.nodeNumber = NodeNumber;
        fullUrl = "https://" + node.urls[0].url + "/service/";

        client = asyncHttpClient();
        gson = Utils.createGson();
    }

    public String GetFullURL() {
        return "https://raida" + (nodeNumber - 1) + ".cloudcoin.global/service/";
    }

    public class MultiDetectResponse {
        public com.cloudcore.authenticator.raida.Response[] responses;
    }

    //int[] nn, int[] sn, String[] an, String[] pan, int[] d, int timeout
    public CompletableFuture<MultiDetectResponse> MultiDetect() {
        /*PREPARE REQUEST*/
        RAIDA raida = RAIDA.activeRAIDA;
        int[] nn = raida.multiRequest.nn;
        int[] sn = raida.multiRequest.sn;
        String[] an = raida.multiRequest.an[nodeNumber - 1];
        String[] pan = raida.multiRequest.pan[nodeNumber - 1];
        int[] d = raida.multiRequest.d;
        int timeout = raida.multiRequest.timeout;

        return MultiDetect(nn, sn, an, pan, d, timeout);
    }

    public CompletableFuture<MultiDetectResponse> MultiDetect(int[] nn, int[] sn, String[] an, String[] pan, int[] d, int timeout) {
        com.cloudcore.authenticator.raida.Response[] response = new com.cloudcore.authenticator.raida.Response[nn.length];
        for (int i = 0; i < nn.length; i++) {
            response[i] = new com.cloudcore.authenticator.raida.Response();
        }

        ArrayList<Param> formParams = new ArrayList<>();
        for (int i = 0; i < nn.length; i++) {
            formParams.add(new Param("nns[]", Integer.toString(nn[i])));
            formParams.add(new Param("sns[]", Integer.toString(sn[i])));
            formParams.add(new Param("ans[]", an[i]));
            if (Config.DEBUG_MODE)
                formParams.add(new Param("pans[]", an[i]));
            else
                formParams.add(new Param("pans[]", pan[i]));
            formParams.add(new Param("denomination[]", Integer.toString(d[i])));
            //System.out.println("url is " + this.fullUrl + "detect?nns[]=" + nn[i] + "&sns[]=" + sn[i] + "&ans[]=" + an[i] + "&pans[]=" + pan[i] + "&denomination[]=" + d[i]);
            response[i].fullRequest = this.fullUrl + "detect?nns[]=" + nn[i] + "&sns[]=" + sn[i] + "&ans[]=" + an[i] + "&pans[]=" + pan[i] + "&denomination[]=" + d[i]; // Record what was sent
        }

        /* MAKE REQUEST */
        final long before = System.currentTimeMillis();

        return client.preparePost(fullUrl + "multi_detect")
                .setFormParams(formParams)
                .setRequestTimeout(timeout)
                .execute(new AsyncHandler() {
                    private final org.asynchttpclient.Response.ResponseBuilder builder = new org.asynchttpclient.Response.ResponseBuilder();

                    @Override
                    public State onStatusReceived(HttpResponseStatus responseStatus) {
                        builder.accumulate(responseStatus);
                        return State.CONTINUE;
                    }

                    @Override
                    public State onHeadersReceived(HttpResponseHeaders headers) {
                        builder.accumulate(headers);
                        return State.CONTINUE;
                    }

                    @Override
                    public State onBodyPartReceived(HttpResponseBodyPart bodyPart) {
                        builder.accumulate(bodyPart);
                        return State.CONTINUE;
                    }

                    @Override
                    public MultiDetectResponse onCompleted() {
                        /* MAKE REQUEST */
                        long after, ts;

                        org.asynchttpclient.Response httpResponse = builder.build();
                        String totalResponse = httpResponse.getResponseBody();
                        try {
                            if (200 == builder.build().getStatusCode()) {
                                /* PROCESS REQUEST*/
                                after = System.currentTimeMillis();
                                ts = after - before;

                                try {
                                    System.out.println("Response: " + totalResponse);
                                    DetectResponse[] responses = gson.fromJson(totalResponse, DetectResponse[].class);

                                    for (int i = 0; i < nn.length; i++) {
                                        response[i].fullResponse = totalResponse;
                                        response[i].success = "pass".equals(responses[i].status);
                                        response[i].outcome = responses[i].status;
                                    }
                                } catch (Exception e) {
                                    System.out.println("/4: " + e.getLocalizedMessage() + httpResponse.getUri().toUrl());
                                    for (int i = 0; i < nn.length; i++) {
                                        response[i].fullResponse = totalResponse;
                                        response[i].outcome = "e";
                                    }
                                }

                                multiResponse.responses = response;
                                return multiResponse;
                            } else { // 404 not found or 500 error.
                                System.out.println("RAIDA " + nodeNumber + " had an error: " + httpResponse.getStatusCode());
                                after = System.currentTimeMillis();
                                ts = after - before;

                                for (int i = 0; i < nn.length; i++) {
                                    response[i].outcome = "error";
                                    response[i].fullResponse = Integer.toString(httpResponse.getStatusCode());
                                }
                                multiResponse.responses = response;
                                return multiResponse;
                            }
                        } catch (Exception e) {
                            System.out.println("Exception: " + e.getLocalizedMessage());
                            e.printStackTrace();
                        }
                        return multiResponse;
                    }

                    @Override
                    public void onThrowable(Throwable e) {
                        long after = System.currentTimeMillis();
                        long ts = after - before;

                        switch (e.getClass().getCanonicalName()) {
                            case "TimeoutException":
                                for (int i = 0; i < nn.length; i++) {
                                    response[i].outcome = "noresponse";
                                    response[i].fullResponse = e.getLocalizedMessage();
                                }
                                multiResponse.responses = response;
                                return;
                            default:
                                System.out.println("Node#MD" + e.getLocalizedMessage());
                                for (int i = 0; i < nn.length; i++) {
                                    response[i].outcome = "error";
                                    response[i].fullResponse = e.getLocalizedMessage();
                                }
                                multiResponse.responses = response;
                                return;
                        }
                    }
                }).toCompletableFuture();
    }
}
