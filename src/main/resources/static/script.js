let stompClient = null;
let jwtToken = null;
let currentUser = null;
let currentRoom = 'general'; // Default room
let currentSubscription = null;

// Utility function to display messages on the login screen
function showAuthMessage(msg, isError) {
    const errorMsg = document.getElementById('error-msg');
    const successMsg = document.getElementById('success-msg');
    
    if (isError) {
        errorMsg.innerText = msg;
        errorMsg.style.display = 'block';
        successMsg.style.display = 'none';
    } else {
        successMsg.innerText = msg;
        successMsg.style.display = 'block';
        errorMsg.style.display = 'none';
    }
}

// Register a new user
async function register() {
    const user = document.getElementById('username').value;
    const pass = document.getElementById('password').value;

    if (!user || !pass) {
        showAuthMessage("Username and password required!", true);
        return;
    }

    try {
        const response = await fetch(`/api/auth/register?username=${user}&password=${pass}`, { method: 'POST' });
        const text = await response.text();

        if (text.includes("SUCCESS")) {
            showAuthMessage(text, false);
        } else {
            showAuthMessage(text, true);
        }
    } catch (error) {
        showAuthMessage("Registration failed to reach the server.", true);
    }
}

// Refactored login to use our new message logic
async function login() {
    currentUser = document.getElementById('username').value;
    const pass = document.getElementById('password').value;

    if (!currentUser || !pass) {
        showAuthMessage("Username and password required!", true);
        return;
    }

    try {
        const response = await fetch(`/api/auth/login?username=${currentUser}&password=${pass}`, { method: 'POST' });
        const text = await response.text();

        if (text.includes("SUCCESS")) {
            jwtToken = text.split('\n')[1].trim(); 
            
            document.getElementById('login-screen').style.display = 'none';
            document.getElementById('app-screen').style.display = 'flex'; // Use flex for our split layout
            
            connectWebSocket();
        } else {
            showAuthMessage("Invalid credentials", true);
        }
    } catch (error) {
        showAuthMessage("Login failed to reach the server.", true);
    }
}

// Handles clicking a channel in the sidebar
async function switchRoom(roomName) {
    currentRoom = roomName;
    
    // Update the UI Header
    document.getElementById('current-room-title').innerText = `# ${roomName}`;
    
    // Update the Sidebar active styling
    const listItems = document.querySelectorAll('#channel-list li');
    listItems.forEach(li => li.classList.remove('active'));
    event.target.classList.add('active');

    // Clear the chat box
    document.getElementById('chat-box').innerHTML = '';

    // Fetch the specific history for this new room
    await fetchChatHistory();

    // Disconnect from the old room's live stream, and connect to the new one
    if (currentSubscription) {
        currentSubscription.unsubscribe();
    }
    subscribeToCurrentRoom();
}

async function fetchChatHistory() {
    try {
        const response = await fetch(`/api/messages/${currentRoom}`, {
            method: 'GET',
            headers: { 'Authorization': 'Bearer ' + jwtToken }
        });

        if (response.ok) {
            const messages = await response.json();
            messages.forEach(msg => { displayMessage(msg.sender, msg.content, msg.timestamp); });
        }
    } catch (error) {
        console.error("Failed to load chat history:", error);
    }
}

function connectWebSocket() {
    const socket = new SockJS('/ws'); 
    stompClient = Stomp.over(socket);
    stompClient.debug = null; 

    stompClient.connect({}, async function (frame) {
        console.log('Connected to WebSockets!');
        
        // Load history and subscribe to the default room immediately after connecting
        await fetchChatHistory();
        subscribeToCurrentRoom();
    });
}

function subscribeToCurrentRoom() {
    currentSubscription = stompClient.subscribe(`/topic/${currentRoom}`, function (message) {
        const parsedMessage = JSON.parse(message.body);
        displayMessage(parsedMessage.sender, parsedMessage.content, parsedMessage.timestamp);
    });
}

async function sendMessage() {
    const inputField = document.getElementById('message-input');
    const content = inputField.value.trim();

    if (content && jwtToken) {
        // Package the currentRoom into the JSON payload
        const messageObj = { room: currentRoom, sender: currentUser, content: content };

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

function displayMessage(sender, content, timestamp) {
    const chatBox = document.getElementById('chat-box');
    const msgDiv = document.createElement('div');
    msgDiv.className = 'message';
    
    const timeHtml = timestamp ? `<span class="timestamp">[${timestamp}]</span>` : '';
    
    msgDiv.innerHTML = `${timeHtml} <span class="sender">${sender}:</span> ${content}`;
    
    chatBox.appendChild(msgDiv);
    chatBox.scrollTop = chatBox.scrollHeight; 
}

document.getElementById("message-input").addEventListener("keypress", function(event) {
    if (event.key === "Enter") {
        event.preventDefault();
        sendMessage();
    }
});