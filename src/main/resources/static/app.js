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
        console.log('Connected: ' + frame);
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function subscribe() {
        stompClient.subscribe($( "#name" ), function (greeting) {
        console.log('Subscribed: ' + frame);
        });
}

function showMessages(message) {
    $("#messages").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#subscribe" ).click(function() { subscribe(); });
});

