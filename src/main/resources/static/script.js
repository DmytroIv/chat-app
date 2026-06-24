let stompClient = null;
let jwtToken = null;
let currentUser = null;
let currentRoom = 'general'; // Default room
let currentSubscription = null;

async function login() {
    currentUser = document.getElementById('username').value;
    const pass = document.getElementById('password').value;

    const response = await fetch(`/api/auth/login?username=${currentUser}&password=${pass}`, { method: 'POST' });
    const text = await response.text();

    if (text.includes("SUCCESS")) {
        jwtToken = text.split('\n')[1].trim(); 
        
        document.getElementById('login-screen').style.display = 'none';
        document.getElementById('app-screen').style.display = 'flex'; // Use flex for our split layout
        
        connectWebSocket();
    } else {
        document.getElementById('error-msg').style.display = 'block';
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
        // Ask the API for messages specifically from the 'currentRoom'
        const response = await fetch(`/api/messages/${currentRoom}`, {
            method: 'GET',
            headers: { 'Authorization': 'Bearer ' + jwtToken }
        });

        if (response.ok) {
            const messages = await response.json();
            messages.forEach(msg => { displayMessage(msg.sender, msg.content); });
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
    // Subscribe to the dynamic STOMP topic created by our Java MessageConsumer
    currentSubscription = stompClient.subscribe(`/topic/${currentRoom}`, function (message) {
        const parsedMessage = JSON.parse(message.body);
        displayMessage(parsedMessage.sender, parsedMessage.content);
    });
}

async function sendMessage() {
    const inputField = document.getElementById('message-input');
    const content = inputField.value.trim();

    if (content && jwtToken) {
        // Package the currentRoom into the JSON payload!
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