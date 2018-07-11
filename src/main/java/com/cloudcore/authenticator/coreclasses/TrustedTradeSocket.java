package com.cloudcore.authenticator.coreclasses;

import java.util.Dictionary;
import java.util.HashMap;

import static com.cloudcore.authenticator.coreclasses.TrustedTradeSocket.PacketType.*;

class TrustedTradeSocket
{
    public enum Status {NONE, STATUS_CONNECTED, STATUS_ERROR, STATUS_DISCONNECTED, STATUS_SENDING, STATUS_DONE, STATUS_REQUEST_RECIPIENT, STATUS_WAITING_RECIPIENT }

    public enum PacketType {NONE, PACKET_TYPE_INIT, PACKET_TYPE_WORD, PACKET_TYPE_COINS, PACKET_TYPE_PROGRESS, PACKET_TYPE_DONE, PACKET_TYPE_REQUEST_RECIPIENT, PACKET_TYPE_OK, PACKET_TYPE_HASH, PACKET_TYPE_RECIPIENT_REPLY }

    Status status = Status.STATUS_DISCONNECTED;
    String errorMsg = "";

    ClientWebSocket ws;
    CancellationTokenSource cts;
    String _url;
    int _timeout;
    Func<String, bool> _onWord;
    Func<bool> _onStatusChange;
    Func<String, bool> _onReceive;
    Func<String, bool> _onProgress;
    public String secretWord = "";

    public String Url { get => _url; set => _url = value; }
    public int Timeout { get => _timeout; set => _timeout = value; }
    public Func<String, bool> OnWord { get => _onWord; set => _onWord = value; }
    public Func<bool> OnStatusChange { get => _onStatusChange; set => _onStatusChange = value; }
    public Func<String, bool> OnReceive { get => _onReceive; set => _onReceive = value; }
    public Func<String, bool> OnProgress { get => _onProgress; set => _onProgress = value; }



    public TrustedTradeSocket(String url, int timeout = 10, Func<String, bool> onWord = null, Func<bool> onStatusChange = null, Func<String, bool> onReceive = null, Func<String, bool> onProgress = null)
    {
        Url = url;
        Timeout = timeout;
        OnWord = onWord;
        OnStatusChange = onStatusChange;
        OnReceive = onReceive;
        OnProgress = onProgress;

    }

    public async Task Connect()
{

    try
    {
        ws = new ClientWebSocket();
        cts = new CancellationTokenSource();
        await ws.ConnectAsync(new Uri(Url), cts.Token);

    }
    catch (WebSocketException ex)
    {
        await Connect();
        return;
    }
    catch (Exception ex)
    {
        System.out.println("Exception: {0}", ex);
    }

    await Send("{\"type\":1}");

    await Task.Factory.StartNew(
        async () =>
        {
                var rcvBytes = new byte[128];
    var rcvBuffer = new ArraySegment<byte>(rcvBytes);
    while (true)
    {
        WebSocketReceiveResult rcvResult =
                await ws.ReceiveAsync(rcvBuffer, cts.Token);
        byte[] msgBytes = rcvBuffer.Skip(rcvBuffer.Offset).Take(rcvResult.size()).toArray();
        String rcvMsg = Encoding.UTF8.GetString(msgBytes);
        OnMessage(rcvMsg);
        //System.out.println("Received: {0}", rcvMsg);
    }
            }, cts.Token, TaskCreationOptions.LongRunning, TaskScheduler.Default);
}

    void OnMessage(String message)
    {
        System.out.println("");
        var data = JsonConvert.DeserializeObject<Dictionary<String, String>>(message);
        if(data["result"] != "success")
        {
            SetError(data["message"]);
            return;
        }
        PacketType packet = (PacketType)int.Parse(data["type"]);
        switch (packet)
        {
            case PACKET_TYPE_WORD:
                OnWord?.Invoke(data["data"]);
                break;
            case PACKET_TYPE_PROGRESS:
                OnProgress?.Invoke(data["data"]);
                break;
            case PACKET_TYPE_DONE:
                SetStatus(Status.STATUS_DONE);
                break;
            case PACKET_TYPE_OK:
                if(status == Status.STATUS_REQUEST_RECIPIENT)
                    SetStatus(Status.STATUS_WAITING_RECIPIENT);
                break;
            case PACKET_TYPE_RECIPIENT_REPLY:
                if(status != Status.STATUS_WAITING_RECIPIENT)
                {
                    SetError("Protocol Error");
                    return;
                }
                System.out.println("recipient replied:" + data["data"]);
                break;
            case PACKET_TYPE_HASH:
                System.out.println("Received CloudCoins");
                System.out.println("h=" + data["data"]);
                OnReceive?.Invoke(data["data"]);
                break;
            default:
                SetError("Invalid packet " + data["type"]);
                break;
        }

    }

    public String GetError()
    {
        return errorMsg;
    }

    public String GetStatus() {
        HashMap<Status, String> r = new HashMap<Status, String>() {
            {
                put(Status.STATUS_DISCONNECTED, "Disconnected");
                put(Status.STATUS_ERROR, "Error");
                put(Status.STATUS_CONNECTED, "Connected");
                put(Status.STATUS_SENDING, "Sending Coins");
                put(Status.STATUS_DONE, "Coins sent");
                put(Status.STATUS_REQUEST_RECIPIENT, "Waiting for recipient");
                put(Status.STATUS_WAITING_RECIPIENT, "Waiting for recipient");
            }
        };

        return r.get(status);
    }

    public void SetStatus(Status newStatus)
    {
        status = newStatus;
        OnStatusChange?.Invoke();
    }

    public void SetError(String msg)
    {
        errorMsg = msg;
        SetStatus(Status.STATUS_ERROR);
    }

    public /*async*/ Task SendCoins(String sh, String stack)
{
    Dictionary<String, String> json = new Dictionary<String, String> { ["type"] = "3", ["word"] = sh, ["stack"] = stack};
    String message = JsonConvert.SerializeObject(json);
    await Send(message);
    SetStatus(Status.STATUS_REQUEST_RECIPIENT);//Status.STATUS_SENDING
}

    public /*async*/ Task Send(String message)
{
    byte[] sendBytes = Encoding.UTF8.GetBytes(message);
    var sendBuffer = new ArraySegment<byte>(sendBytes);
    await ws.SendAsync(sendBuffer, WebSocketMessageType.Text, true, cts.Token);
}
}
