const USER_NAME_ITEM_KEY = "UserName";
const USER_JSON_DATA_KEY = "UserInfo";
var messages = [];
var lastId = -100;

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
    setInterval(requireOnlineUsers, 5000);
    showMessage();
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

function requireOnlineUsers() {
        var request = new XMLHttpRequest();
        var url = "/users";
        request.open("GET", url, true);
        var onlineUsers = [];
        request.setRequestHeader("Authorization", "Token " + JSON.parse(window.sessionStorage.getItem(USER_JSON_DATA_KEY))["token"]);

        request.onreadystatechange = function () {
            if (request.status == 200) {
                if(request.responseText.localeCompare("") == 0){
                    return;
                }
                var parsed = JSON.parse(request.responseText);
                for (var i = 0; i < parsed["users"].length; i++) {
                    var username = parsed["users"][i]["username"];
                    if (onlineUsers.indexOf(username) != -1) {
                        continue;
                    }
                    console.log("Adding username " + username);
                    onlineUsers.push(username);
                }
                refreshUsers(onlineUsers);
            }
            else {
                alert("Error on server, while requiring online users " + request.status);
            }
        };
        request.send(null);
}

function refreshUsers(onlineUsers) {
    var usersListDiv = document.getElementById("usersH2");
    while(usersListDiv.firstChild){
        console.log(usersListDiv.firstChild);
        usersListDiv.removeChild(usersListDiv.firstChild);
    }
    usersListDiv.textContent=" Users online : ";

    for (var i = 0; i < onlineUsers.length; i++) {
        var userNameP = document.createElement("p");
        var userNameString = document.createTextNode(onlineUsers[i]);
        userNameP.appendChild(userNameString);

        usersListDiv.appendChild(userNameP);

    }
}

function sendMessage() {
    var xhr = new XMLHttpRequest();
    var url = "/messages";
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Authorization", "Token " + JSON.parse(window.sessionStorage.getItem(USER_JSON_DATA_KEY))["token"]);
    var data = JSON.stringify ( {
         "message" : document.getElementById("area").value
     } );

    xhr.onreadystatechange = function() {
        if ( xhr.status == 200) {
            document.getElementById("area").value = "";
        }
        else {
            alert("err send");
        }
    };

    xhr.send(data);
}

function showMessage() {
    var flag = true;
    var offset = 0;
    do{
        var xhr = new XMLHttpRequest();
        var url = "/messages";
        xhr.open("GET", url, false);
        xhr.setRequestHeader("Authorization", "Token " + JSON.parse(window.sessionStorage.getItem(USER_JSON_DATA_KEY))["token"]);
        xhr.setRequestHeader("offset", offset.toString());
        xhr.setRequestHeader("count", "100");

        xhr.onreadystatechange = function() {
            if ( xhr.status == 200) {
                var parsed = JSON.parse(xhr.responseText);
                var len = parsed["messages"].length;
                for (var i = 0; i < len; i++) {
                    var message = parsed["messages"][i]["message"];
                    var user = parsed["messages"][i]["author"];
                    var id = parsed["messages"][i]["id"];
                    console.log("last: "+ lastId + " " + "id: " + id);

                    if(lastId != id){
                        refreshMessages(message, user);
                        lastId = id;
                    }else {
                        return;
                    }




                }
                offset += parsed["messages"].length;
                console.log("offset: " + offset);

                if(len < 100){
                    console.log("flag");
                    flag = false;
                }

            }
            else {
                alert("err request msg");
            }
        };
        xhr.send(null);
    } while (flag);






    setInterval(function () {
        var xhr = new XMLHttpRequest();
        var url = "/messages";
        xhr.open("GET", url, false);
        xhr.setRequestHeader("Authorization", "Token " + JSON.parse(window.sessionStorage.getItem(USER_JSON_DATA_KEY))["token"]);
        xhr.setRequestHeader("offset", offset.toString());
        xhr.setRequestHeader("count", "10");

        xhr.onreadystatechange = function() {
            if ( xhr.status == 200) {

                if(xhr.responseText.localeCompare("") == 0){
                    return;
                }
                var parsed = JSON.parse(xhr.responseText);
                for (var i = 0; i < parsed["messages"].length; i++) {
                    var message = parsed["messages"][i]["message"];
                    var user = parsed["messages"][i]["author"];
                    var id = parsed["messages"][i]["id"];
                    console.log("last: "+ lastId + " " + "id: " + id);
                    if(lastId != id){
                        console.log("write");
                        refreshMessages(message, user);
                        lastId = id;
                    } else {
                        return;
                    }

                }
                offset += parsed["messages"].length;
                console.log("offset: " + offset);

            }
            else {
                alert("err request msg");
            }
        };

        xhr.send(null);
    }, 1000);

}

function refreshMessages(message, user) {
    var messageP = document.createElement("p");
    messageP.setAttribute("align", "left");
    var userMessageString = document.createTextNode(user + ": " + message);
    messageP.appendChild(userMessageString);
    var chatBox = document.getElementById("messageBox");
    chatBox.appendChild(messageP);
}