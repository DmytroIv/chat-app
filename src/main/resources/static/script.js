let stompClient = null;
let jwtToken = null;
let currentUser = null;

// 1. Authenticate and get the VIP Pass (JWT)
async function login() {
    currentUser = document.getElementById('username').value;
    const pass = document.getElementById('password').value;

    const response = await fetch(`/api/auth/login?username=${currentUser}&password=${pass}`, { method: 'POST' });
    const text = await response.text();

    if (text.includes("SUCCESS")) {
        jwtToken = text.split('\n')[1].trim(); 
        
        document.getElementById('login-screen').style.display = 'none';
        document.getElementById('chat-screen').style.display = 'block';
        
        // Fetch the historical messages before turning on the live stream!
        await fetchChatHistory();

        connectWebSocket();
    } else {
        document.getElementById('error-msg').style.display = 'block';
    }
}

// Grab the history from PostgreSQL via the REST API
async function fetchChatHistory() {
    try {
        const response = await fetch('/api/messages', {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + jwtToken // Show our VIP pass!
            }
        });

        if (response.ok) {
            const messages = await response.json();
            // Loop through the database records and paint them on the screen
            messages.forEach(msg => {
                displayMessage(msg.sender, msg.content);
            });
        }
    } catch (error) {
        console.error("Failed to load chat history:", error);
    }
}

// 2. Connect to the STOMP WebSocket
function connectWebSocket() {
    const socket = new SockJS('/ws'); 
    stompClient = Stomp.over(socket);
    stompClient.debug = null; 

    stompClient.connect({}, function (frame) {
        console.log('Connected to WebSockets!');
        
        stompClient.subscribe('/topic/messages', function (message) {
            const parsedMessage = JSON.parse(message.body);
            displayMessage(parsedMessage.sender, parsedMessage.content);
        });
    });
}

// 3. Send a message to the REST API
async function sendMessage() {
    const inputField = document.getElementById('message-input');
    const content = inputField.value.trim();

    if (content && jwtToken) {
        const messageObj = { sender: currentUser, content: content };

        await fetch('/api/messages', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + jwtToken 
            },
            body: JSON.stringify(messageObj)
        });

        inputField.value = ''; 
    }
}

// 4. Paint the new messages onto the screen
function displayMessage(sender, content) {
    const chatBox = document.getElementById('chat-box');
    const msgDiv = document.createElement('div');
    msgDiv.className = 'message';
    msgDiv.innerHTML = `<span class="sender">${sender}:</span> ${content}`;
    
    chatBox.appendChild(msgDiv);
    chatBox.scrollTop = chatBox.scrollHeight; 
}

document.getElementById("message-input").addEventListener("keypress", function(event) {
    if (event.key === "Enter") {
        event.preventDefault();
        sendMessage();
    }
});