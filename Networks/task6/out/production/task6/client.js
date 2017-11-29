const USER_NAME_ITEM_KEY = "UserName";
const USER_JSON_DATA_KEY = "UserJSON";

var messages = [];
var onlineUsers = [];

function loginEntered() {
    var xhr = new XMLHttpRequest();
    var url = "/login";
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    window.sessionStorage.setItem(USER_NAME_ITEM_KEY, document.getElementById("inputLogin").value ) ;

    var data = JSON.stringify ( {
        "username" : sessionStorage.getItem(USER_NAME_ITEM_KEY)
    } );

    xhr.onreadystatechange = function() {
        if ( xhr.status == 200) {
            window.sessionStorage.setItem(USER_JSON_DATA_KEY, xhr.responseText);
            document.location = "/main";
        }
        else {
            alert("Login is invalid");
        }
    };


    xhr.send(data);

}

function displayUserName() {
    var newH1 = document.createElement("h1");
    var userNameP = document.createTextNode(sessionStorage.getItem(USER_NAME_ITEM_KEY));
    newH1.appendChild(userNameP);
    document.getElementById("userLogin").appendChild(newH1);
    requireOnlineUsers();
}

function logoutEntered() {
    var xhr = new XMLHttpRequest();
    var url = "/logout";
    xhr.open("GET", url, true);

    xhr.setRequestHeader("Authorization", "Token " + JSON.parse(window.sessionStorage.getItem(USER_JSON_DATA_KEY))["token"]);

    xhr.onreadystatechange = function() {
        if ( xhr.status == 200 ) {
            document.location = "/";
        }
        else {
            alert("Error on server, while logging out");
        }
    };

    xhr.send(null);
}

function refreshUsers() {
    for (var i = 0; i < onlineUsers.length; i++) {
        var userNameP = document.createElement("p");
        var userNameString = document.createTextNode(onlineUsers[i]);
        userNameP.appendChild(userNameString);
        userNameP.className = "activeUser";
        var usersListDiv = document.getElementById("chatUsers");
        usersListDiv.appendChild(userNameP);
    }
}

function refreshMessages() {
    for (var i = 0; i < messages.length; i++) {
        var messageP = document.createElement("p");
        messageP.className = "message";
        messageP.setAttribute("align", "left");
        var userMessageString = document.createTextNode(messages[i]);
        messageP.appendChild(userMessageString);
        var chatBox = document.getElementById("messageBox");
        chatBox.appendChild(messageP);

    }
}

function requireOnlineUsers() {
    var request = new XMLHttpRequest();
    var url = "/users";
    request.open("GET",url,true);

    request.setRequestHeader("Authorization", "Token " + JSON.parse(window.sessionStorage.getItem(USER_JSON_DATA_KEY))["token"]);

    request.onreadystatechange = function() {
        if ( request.status == 200 ) {
            var parsed =  JSON.parse(request.responseText);
            for (var i = 0; i < parsed["users"].length; i++) {
                var username = parsed["users"][i]["username"];
                if (onlineUsers.includes(username)) {
                    return;
                }
                console.log("Adding username " + username);
                onlineUsers.push(username);
            }
            refreshUsers();
        }
        else {
            alert("Error on server, while requiring online users " + request.status);
        }
    };
    request.send(null);
}

