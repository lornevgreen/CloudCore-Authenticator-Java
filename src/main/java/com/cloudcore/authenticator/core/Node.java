package com.cloudcore.authenticator.core;

import com.google.gson.Gson;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.asynchttpclient.*;
import org.json.JSONException;
import org.json.JSONObject;

import static org.asynchttpclient.Dsl.asyncHttpClient;

/*
 * This Class Contains the properties of a RAIDA node.
 */
public class Node {

    public enum NodeStatus {
        Ready,
        NotReady,
    }

    public enum TicketHistory {}

    private AsyncHttpClient client;
    private Gson gson;

    public int NodeNumber;
    public String FullUrl;
    public boolean FailsDetect = false;
    public boolean FailsFix = false;
    public boolean FailsEcho = false;
    public boolean HasTicket = false;
    public MultiDetectResponse MultiResponse = new MultiDetectResponse();
    public String Ticket = "";

    //Constructor
    public Node(int NodeNumber) {
        this.NodeNumber = NodeNumber;
        FullUrl = GetFullURL();
        System.out.println(FullUrl);

        client = asyncHttpClient();
        gson = Utils.createGson();
    }

    public Node(int NodeNumber, RAIDANode node) {
        this.NodeNumber = NodeNumber;
        FullUrl = "https://" + node.urls[0].url + "/service/";

        client = asyncHttpClient();
        gson = Utils.createGson();
    }

    public String GetFullURL() {
        return "https://raida" + (NodeNumber - 1) + ".cloudcoin.global/service/";
    }

    /**
     * Method DETECT
     * Sends a Detection request to a RAIDA server
     *
     * @return Response Object.
     */
    public CompletableFuture<Response> Detect(CloudCoin coin, int nodeNumber) {
        return CompletableFuture.supplyAsync(() -> {
            Response detectResponse = new Response();
            detectResponse.fullRequest = this.FullUrl + "detect?nn=" + coin.nn + "&sn=" + coin.getSn() + "&an=" + coin.an.get(nodeNumber) + "&pan=" + coin.pan[nodeNumber] + "&denomination=" + coin.denomination + "&b=t";
            long before = System.currentTimeMillis();
            coin.SetAnsToPans();
            try {
                detectResponse.fullResponse = Utils.GetHtmlFromURL(detectResponse.fullRequest);

                long after = System.currentTimeMillis();
                long ts = after - before;
                coin.response[nodeNumber] = detectResponse;

                if (detectResponse.fullResponse.contains("pass")) {
                    detectResponse.outcome = "pass";
                    detectResponse.success = true;
                    FailsDetect = true; // TODO: THIS LINE WAS NOT PRESENT ON ZERO VARIABLE METHOD
                } else if (detectResponse.fullResponse.contains("fail") && detectResponse.fullResponse.length() < 200)//less than 200 incase their is a fail message inside errored page
                {
                    detectResponse.outcome = "fail";
                    detectResponse.success = false;
                    FailsDetect = true;
                    //RAIDA_Status.failsDetect[RAIDANumber] = true;
                } else {
                    detectResponse.outcome = "error";
                    detectResponse.success = false;
                    FailsDetect = true;
                    //RAIDA_Status.failsDetect[RAIDANumber] = true;
                }

            } catch (Exception e){
                System.out.println("/3" + e.getLocalizedMessage());
                detectResponse.outcome = "error";
                detectResponse.fullResponse = e.getLocalizedMessage();
                detectResponse.success = false;
            }
            return detectResponse;
        });
    }//end detect

    public class MultiDetectResponse {
        public Response[] responses;
    }

    //int[] nn, int[] sn, String[] an, String[] pan, int[] d, int timeout
    public CompletableFuture<MultiDetectResponse> MultiDetect() {
        /*PREPARE REQUEST*/
        RAIDA raida = RAIDA.ActiveRAIDA;
        int[] nn = raida.multiRequest.nn;
        int[] sn = raida.multiRequest.sn;
        String[] an = raida.multiRequest.an[NodeNumber - 1];
        String[] pan = raida.multiRequest.pan[NodeNumber - 1];
        int[] d = raida.multiRequest.d;
        int timeout = raida.multiRequest.timeout;

        return MultiDetect(nn, sn, an, pan, d, timeout);
    }

    public CompletableFuture<MultiDetectResponse> MultiDetect(int[] nn, int[] sn, String[] an, String[] pan, int[] d, int timeout) {
        Response[] response = new Response[nn.length];
        for (int i = 0; i < nn.length; i++) {
            response[i] = new Response();
        }

        ArrayList<Param> formParams = new ArrayList<>();
        for (int i = 0; i < nn.length; i++) {
            formParams.add(new Param("nns[]", Integer.toString(nn[i])));
            formParams.add(new Param("sns[]", Integer.toString(sn[i])));
            formParams.add(new Param("ans[]", an[i]));
            formParams.add(new Param("pans[]", pan[i]));
            formParams.add(new Param("denomination[]", Integer.toString(d[i])));
            // System.out.println("url is " + this.fullUrl + "detect?nns[]=" + nn[i] + "&sns[]=" + sn[i] + "&ans[]=" + an[i] + "&pans[]=" + pan[i] + "&denomination[]=" + d[i]);
            response[i].fullRequest = this.FullUrl + "detect?nns[]=" + nn[i] + "&sns[]=" + sn[i] + "&ans[]=" + an[i] + "&pans[]=" + pan[i] + "&denomination[]=" + d[i]; // Record what was sent
        }

        /* MAKE REQUEST */
        final long before = System.currentTimeMillis();

        return client.preparePost(FullUrl + "multi_detect")
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
                            JSONObject json = new JSONObject(httpResponse);

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

                                MultiResponse.responses = response;
                                return MultiResponse;
                            } else { // 404 not found or 500 error.
                                System.out.println("RAIDA " + NodeNumber + " had an error: " + httpResponse.getStatusCode());
                                after = System.currentTimeMillis();
                                ts = after - before;

                                for (int i = 0; i < nn.length; i++) {
                                    response[i].outcome = "error";
                                    response[i].fullResponse = Integer.toString(httpResponse.getStatusCode());
                                }
                                MultiResponse.responses = response;
                                return MultiResponse;
                            }
                        } catch (JSONException e) {
                            System.out.println("Exception: " + e.getLocalizedMessage());
                            e.printStackTrace();
                        }
                        return MultiResponse;
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
                                MultiResponse.responses = response;
                                return;
                            default:
                                System.out.println("Node#MD" + e.getLocalizedMessage());
                                for (int i = 0; i < nn.length; i++) {
                                    response[i].outcome = "error";
                                    response[i].fullResponse = e.getLocalizedMessage();
                                }
                                MultiResponse.responses = response;
                                return;
                        }
                    }
                }).toCompletableFuture();
    }//End multi detect

    /**
     * Method FIX
     * Repairs a fracked RAIDA
     *
     * @param triad three ints trusted server RAIDA numbers
     * @param m1    String ticket from the first trusted server
     * @param m2    String ticket from the second trusted server
     * @param m3    String ticket from the third trusted server
     * @param pan   String proposed authenticity number (to replace the wrong AN the RAIDA has)
     * @return String status sent back from the server: sucess, fail or error.
     */
    public Response Fix(int[] triad, String m1, String m2, String m3, String pan) {
        Response fixResponse = new Response();
        long before = System.currentTimeMillis();
        fixResponse.fullRequest = FullUrl + "fix?fromserver1=" + triad[0] + "&message1=" + m1 + "&fromserver2=" + triad[1] + "&message2=" + m2 + "&fromserver3=" + triad[2] + "&message3=" + m3 + "&pan=" + pan;
        long after = System.currentTimeMillis();
        long ts = after - before;

        try {
            fixResponse.fullResponse = Utils.GetHtmlFromURL(fixResponse.fullRequest);
            if (fixResponse.fullResponse.contains("success")) {
                fixResponse.outcome = "success";
                fixResponse.success = true;
            } else {
                fixResponse.outcome = "fail";
                fixResponse.success = false;
            }
        } catch (Exception e){//quit
            System.out.println("/5" + e.getLocalizedMessage());
            fixResponse.outcome = "error";
            fixResponse.fullResponse = e.getLocalizedMessage();
            fixResponse.success = false;
        }
        return fixResponse;
    }//end fixit


    /**
     * Method GET TICKET
     * Returns an ticket from a trusted server
     *
     * @param nn  int that is the coin's Network Number
     * @param sn  int that is the coin's Serial Number
     * @param an  String that is the coin's Authenticity Number (GUID)
     * @param d   int that is the Denomination of the Coin
     * @return Response Object.
     */
    public CompletableFuture<Response> GetTicket(int nn, int sn, String an, int d) {
        return CompletableFuture.supplyAsync(() -> {
            RAIDA raida = RAIDA.GetInstance();
            Response get_ticketResponse = new Response();
            get_ticketResponse.fullRequest = FullUrl + "get_ticket?nn=" + nn + "&sn=" + sn + "&an=" + an + "&pan=" + an + "&denomination=" + d;
            long before = System.currentTimeMillis();

            try {
                get_ticketResponse.fullResponse = Utils.GetHtmlFromURL(get_ticketResponse.fullRequest);
                long after = System.currentTimeMillis();
                long ts = after - before;

                if (get_ticketResponse.fullResponse.contains("ticket")) {
                    String[] KeyPairs = get_ticketResponse.fullResponse.split(",");
                    String message = KeyPairs[3];
                    int startTicket = Utils.ordinalIndexOf(message, "\"", 3) + 2;
                    int endTicket = Utils.ordinalIndexOf(message, "\"", 4) - startTicket;
                    get_ticketResponse.outcome = message.substring(startTicket - 1, endTicket + 1); //This is the ticket or message

                } else {
                }//end if

            } catch (Exception e) {
                System.out.println("/6" + e.getLocalizedMessage());
                e.printStackTrace();
                get_ticketResponse.outcome = "error";
                get_ticketResponse.fullResponse = e.getLocalizedMessage();
            }//end try catch
            return get_ticketResponse;
        });
    }//end get ticket

}
