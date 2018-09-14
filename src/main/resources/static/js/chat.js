'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var blackArea = document.querySelector('#blackArea')
var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var username = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function connect(event) {
    username = document.querySelector('#name').value.trim();

    if (username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}


function onConnected() {
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onMessageReceived);
    stompClient.subscribe('/topic/black', onBlackReceived);

    // Tell your username to the server
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({userName: username, type: 'JOIN'})
    )
    var httpRequest = new XMLHttpRequest();

    httpRequest.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            onBlackReceived({"body": this.responseText});
        }
    };
    ;
    httpRequest.open('GET', '/blackWordList');
    httpRequest.send();
    connectingElement.classList.add('hidden');
}


function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}


function sendMessage(event) {
    var messageContent = messageInput.value.trim();

    if (messageContent && stompClient) {
        var chatMessage = {
            userName: username,
            message: messageInput.value,
            type: 'CHAT'
        };

        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}

function onBlackReceived(payload) {
    var message = JSON.parse(payload.body);
    if (message.type == "ADD_BLACK") {
        message.message.split(",").forEach(function (world) {
            var messageElement = document.createElement('li');
            var textElement = document.createElement('p');
            var messageText = document.createTextNode(world);
            textElement.appendChild(messageText);
            messageElement.appendChild(textElement);
            blackArea.appendChild(messageElement);
            blackArea.scrollTop = blackArea.scrollHeight;
        });

    } else if (message.type == "REMOVE_BLACK") {
        var item = blackArea.getElementsByTagName("li");
        var removeTarget = [];
        message.message.split(",").forEach(function (world) {
            for (var i = 0; i < item.length; i++) {
                if (world == item[i].innerText.trim()) {
                    removeTarget.push(item[i]);
                    break;
                }
            }
        });
        removeTarget.forEach(function(target){
            blackArea.removeChild(target);
        });
    }
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

    var messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.message = message.userName + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.message = message.userName + ' left!';
    } else {
        messageElement.classList.add('chat-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.userName[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.userName);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.userName);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    var textElement = document.createElement('p');

    // var messageText = document.createTextNode(message.message);

    var messageText = (message.message);
    var elem = document.createElement('div'); //색상을 넣기 위하여 사용
    elem.innerHTML = messageText;
    textElement.appendChild(elem);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}


function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }

    var index = Math.abs(hash % colors.length);
    return colors[index];
}

usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', sendMessage, true)
