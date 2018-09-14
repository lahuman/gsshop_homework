'use strict';

var chatPage = document.querySelector('#chat-page');
var blackwordForm = document.querySelector('#blackwordForm');
var wordInput = document.querySelector('#word');
var wordArea = document.querySelector('#wordArea');
var removeBtn = document.querySelector("#removeBtn")
var connectingElement = document.querySelector('.connecting');

var stompClient = null;

function connect() {
    chatPage.classList.remove('hidden');

    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);
}


function onConnected() {
    stompClient.subscribe('/topic/black', onBlackReceived);

    var httpRequest = new XMLHttpRequest();

    httpRequest.onreadystatechange =  function() {
        if (this.readyState == 4 && this.status == 200) {
            onBlackReceived({"body":this.responseText});
        }
    };;
    httpRequest.open('GET', '/blackWordList');
    httpRequest.send();
    connectingElement.classList.add('hidden');
}


function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}


function addBlackword(event) {
    var word = wordInput.value.trim();

    if(word && stompClient) {
        var chatMessage = {
            message: wordInput.value,
            type: 'ADD_BLACK'
        };

        stompClient.send("/app/black.add", {}, JSON.stringify(chatMessage));
        wordInput.value = '';
    }
    event.preventDefault();
}

function removeBlackword(event){
    var words = [];
    var removeTarget = [];
    var wordList = wordArea.getElementsByTagName("li");
    for(var i=0; i<wordList.length; i++){
        var input = wordList[i].getElementsByTagName("input")[0];
        if(input.checked){
            words.push(input.value);
            removeTarget.push(wordList[i]);
        }
    }
    if(words.length > 0 && stompClient) {
        var chatMessage = {
            message: words.join(),
            type: 'REMOVE_BLACK'
        };

        stompClient.send("/app/black.remove", {}, JSON.stringify(chatMessage));
    }
    event.preventDefault();
}

function onBlackReceived(payload){
    var message = JSON.parse(payload.body);
    if(message.type == "ADD_BLACK"){
        message.message.split(",").forEach(function (world) {
            var messageElement = document.createElement('li');
            var checkboxElement = document.createElement('input');
            checkboxElement.setAttribute("type", "checkbox");
            checkboxElement.setAttribute("name", "blackword");
            checkboxElement.setAttribute("id", world);
            checkboxElement.setAttribute("value", world);
            var labelElement = document.createElement("label");
            labelElement.setAttribute("for", world);
            var messageText = document.createTextNode(world);
            labelElement.appendChild(messageText);
            messageElement.appendChild(checkboxElement);
            messageElement.appendChild(labelElement);
            wordArea.appendChild(messageElement);
            wordArea.scrollTop = wordArea.scrollHeight;
        });

    }else  if(message.type == "REMOVE_BLACK"){
        var item = wordArea.getElementsByTagName("li");
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
            wordArea.removeChild(target);
        })
    }
}

blackwordForm.addEventListener('submit', addBlackword, true);
removeBtn.addEventListener('click', removeBlackword, true);
connect();
