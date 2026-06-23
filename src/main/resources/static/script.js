let stompClient = null;
let jwtToken = null;
let currentUser = null;

// 1. Authenticate and get the VIP Pass (JWT)
async function login() {
    currentUser = document.getElementById('username').value;
    const pass = document.getElementById('password').value;

    // Hit our AuthController
    const response = await fetch(`/api/auth/login?username=${currentUser}&password=${pass}`, { method: 'POST' });
    const text = await response.text();

    if (text.includes("SUCCESS")) {
        // Extract the token from the Java response string
        jwtToken = text.split('\n')[1].trim(); 
        
        // Swap the UI
        document.getElementById('login-screen').style.display = 'none';
        document.getElementById('chat-screen').style.display = 'block';
        
        // Connect to the real-time stream!
        connectWebSocket();
    } else {
        document.getElementById('error-msg').style.display = 'block';
    }
}

// 2. Connect to the STOMP WebSocket
function connectWebSocket() {
    const socket = new SockJS('/ws'); // Matches our WebSocketConfig endpoint
    stompClient = Stomp.over(socket);
    
    // Turn off massive debug logs in the browser console
    stompClient.debug = null; 

    stompClient.connect({}, function (frame) {
        console.log('Connected to WebSockets!');
        
        // Subscribe to the RabbitMQ broadcast channel
        stompClient.subscribe('/topic/messages', function (message) {
            const parsedMessage = JSON.parse(message.body);
            displayMessage(parsedMessage.sender, parsedMessage.content);
        });
    });
}

// 3. Send a message to the REST API (which forwards to RabbitMQ)
async function sendMessage() {
    const inputField = document.getElementById('message-input');
    const content = inputField.value.trim();

    if (content && jwtToken) {
        const messageObj = { sender: currentUser, content: content };

        await fetch('/api/messages', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + jwtToken // Show our VIP pass!
            },
            body: JSON.stringify(messageObj)
        });

        inputField.value = ''; // Clear the input box
    }
}

// 4. Paint the new messages onto the screen
function displayMessage(sender, content) {
    const chatBox = document.getElementById('chat-box');
    const msgDiv = document.createElement('div');
    msgDiv.className = 'message';
    msgDiv.innerHTML = `<span class="sender">${sender}:</span> ${content}`;
    
    chatBox.appendChild(msgDiv);
    chatBox.scrollTop = chatBox.scrollHeight; // Auto-scroll to bottom
}

// Listen for the "Enter" key to send messages easily
document.getElementById("message-input").addEventListener("keypress", function(event) {
    if (event.key === "Enter") {
        event.preventDefault();
        sendMessage();
    }
});