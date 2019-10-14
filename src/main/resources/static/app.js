var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#results").show();
    }
    else {
        $("#results").hide();
    }
    $("#messages").html("");
}

function connect() {
    const wsUrl = "ws://"+window.location.host+"/subscribe"
    const socket = new WebSocket(wsUrl);
    stompClient = Stomp.over(socket);
    stompClient.connect({'Authorization':'jgusdflskdfhlskdf'}, function (frame) {
        setConnected(true);
        addSysmsg("connected: "+frame)
        console.log('Connected: ' + frame);
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    addSysmsg("disconnected")
    console.log("Disconnected");
}

function subscribe() {
        var topic = document.getElementById('name').value
        stompClient.subscribe(topic, function (greeting) {
        addSysmsg("subscribed to topic "+topic+" : "+frame)
        console.log('Subscribed: ' + frame);
        });
}

function addMessage(message) {
    $("#messages").append("<tr><td>" + message + "</td></tr>");
}

function addSysmsg(sysmsg) {
    $("#sysmsg").append("<tr><td>" + sysmsg + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#subscribe" ).click(function() { subscribe(); });
});

