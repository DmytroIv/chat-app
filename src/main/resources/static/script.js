let stompClient = null;
let jwtToken = null;
let currentUser = null;
let currentRoom = "general"; // Default room
let currentSubscription = null;

function showAuthMessage(msg, isError) {
  const errorMsg = document.getElementById("error-msg");
  const successMsg = document.getElementById("success-msg");

  if (isError) {
    errorMsg.innerText = msg;
    errorMsg.style.display = "block";
    successMsg.style.display = "none";
  } else {
    successMsg.innerText = msg;
    successMsg.style.display = "block";
    errorMsg.style.display = "none";
  }
}

async function register() {
  const user = document.getElementById("username").value;
  const pass = document.getElementById("password").value;

  if (!user || !pass) {
    showAuthMessage("Username and password required!", true);
    return;
  }

  try {
    const response = await fetch(
      `/api/auth/register?username=${user}&password=${pass}`,
      { method: "POST" },
    );
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

async function login() {
  currentUser = document.getElementById("username").value;
  const pass = document.getElementById("password").value;

  if (!currentUser || !pass) {
    showAuthMessage("Username and password required!", true);
    return;
  }

  try {
    const response = await fetch(
      `/api/auth/login?username=${currentUser}&password=${pass}`,
      { method: "POST" },
    );
    const text = await response.text();

    if (text.includes("SUCCESS")) {
      jwtToken = text.split("\n")[1].trim();

      localStorage.setItem("jwtToken", jwtToken);
      localStorage.setItem("currentUser", currentUser);

      document.getElementById("login-screen").style.display = "none";
      document.getElementById("app-screen").style.display = "flex"; // Use flex for our split layout

      connectWebSocket();
    } else {
      showAuthMessage("Invalid credentials", true);
    }
  } catch (error) {
    showAuthMessage("Login failed to reach the server.", true);
  }
}

function logout() {
  localStorage.removeItem("jwtToken");
  localStorage.removeItem("currentUser");

  if (stompClient) {
    stompClient.disconnect();
  }

  location.reload();
}

async function switchRoom(roomName, dmUser = null) {
  currentRoom = roomName;

  // Update the UI Header
  if (dmUser) {
    document.getElementById("current-room-title").innerText = `@ ${dmUser}`;
  } else {
    document.getElementById("current-room-title").innerText = `# ${roomName}`;
  }

  const listItems = document.querySelectorAll("#channel-list li");
  listItems.forEach((li) => li.classList.remove("active"));

  if (event && event.target && event.target.tagName === "LI" && !dmUser) {
    event.target.classList.add("active");
  }

  document.getElementById("chat-box").innerHTML = "";

  await fetchChatHistory();

  if (currentSubscription) {
    currentSubscription.unsubscribe();
  }
  subscribeToCurrentRoom();
}

async function fetchChatHistory() {
  try {
    const response = await fetch(`/api/messages/${currentRoom}`, {
      method: "GET",
      headers: { Authorization: "Bearer " + jwtToken },
    });

    if (response.ok) {
      const messages = await response.json();
      messages.forEach((msg) => {
        displayMessage(msg.sender, msg.content, msg.timestamp);
      });
    }
  } catch (error) {
    console.error("Failed to load chat history:", error);
  }
}

function connectWebSocket() {
  const socket = new SockJS("/ws");
  stompClient = Stomp.over(socket);
  stompClient.debug = null;

  stompClient.connect({ username: currentUser }, async function (frame) {
    console.log("Connected to WebSockets!");

    stompClient.subscribe("/topic/presence", function (message) {
      const activeUsers = JSON.parse(message.body);
      updatePresenceList(activeUsers);
    });

    stompClient.subscribe("/topic/channels", function (message) {
      fetchChannels();
    });

    await fetchActiveUsers();
    await fetchChannels();
    await fetchChatHistory();
    subscribeToCurrentRoom();
  });
}

function updatePresenceList(users) {
  const list = document.getElementById("online-users-list");
  list.innerHTML = "";

  users.forEach((user) => {
    const li = document.createElement("li");

    li.innerHTML = `<span style="color: #10b981; margin-right: 8px;">●</span> ${user}`;

    if (user !== currentUser) {
      li.style.cursor = "pointer";
      li.onclick = () => startDirectMessage(user);
    } else {
      li.innerHTML +=
        ' <span style="color: #64748b; font-size: 12px;">(You)</span>';
      li.style.cursor = "default";
    }

    list.appendChild(li);
  });
}

async function fetchActiveUsers() {
  try {
    const response = await fetch("/api/presence", {
      method: "GET",
      headers: { Authorization: "Bearer " + jwtToken },
    });

    if (response.ok) {
      const users = await response.json();
      updatePresenceList(users);
    }
  } catch (error) {
    console.error("Failed to load active users:", error);
  }
}

function startDirectMessage(targetUser) {
  const sortedNames = [currentUser, targetUser].sort();
  const dmRoomName = `dm_${sortedNames[0]}_${sortedNames[1]}`;

  switchRoom(dmRoomName, targetUser);
}

function subscribeToCurrentRoom() {
  currentSubscription = stompClient.subscribe(
    `/topic/${currentRoom}`,
    function (message) {
      const parsedMessage = JSON.parse(message.body);
      displayMessage(
        parsedMessage.sender,
        parsedMessage.content,
        parsedMessage.timestamp,
      );
    },
  );
}

async function sendMessage() {
  const inputField = document.getElementById("message-input");
  const content = inputField.value.trim();

  if (content && jwtToken) {
    // Package the currentRoom into the JSON payload
    const messageObj = {
      room: currentRoom,
      sender: currentUser,
      content: content,
    };

    await fetch("/api/messages", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + jwtToken,
      },
      body: JSON.stringify(messageObj),
    });

    inputField.value = "";
  }
}

function displayMessage(sender, content, timestamp) {
  const chatBox = document.getElementById("chat-box");
  const msgDiv = document.createElement("div");
  msgDiv.className = "message";

  const timeHtml = timestamp
    ? `<span class="timestamp">[${timestamp}]</span>`
    : "";

  msgDiv.innerHTML = `${timeHtml} <span class="sender">${sender}:</span> ${content}`;

  chatBox.appendChild(msgDiv);
  chatBox.scrollTop = chatBox.scrollHeight;
}

async function fetchChannels() {
  try {
    const response = await fetch("/api/channels", {
      headers: { Authorization: "Bearer " + jwtToken },
    });

    if (response.ok) {
      const channels = await response.json();
      const list = document.getElementById("channel-list");
      list.innerHTML = "";

      channels.forEach((channel) => {
        const li = document.createElement("li");
        li.innerText = `# ${channel}`;
        li.onclick = () => switchRoom(channel);

        if (channel === currentRoom) {
          li.classList.add("active");
        }

        list.appendChild(li);
      });
    }
  } catch (error) {
    console.error("Failed to load channels:", error);
  }
}

async function createNewChannel() {
  let channelName = prompt("Enter new channel name (no spaces):");
  if (!channelName) return;

  channelName = channelName
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9_]/g, "");

  if (channelName) {
    await fetch(`/api/channels/${channelName}`, {
      method: "POST",
      headers: { Authorization: "Bearer " + jwtToken },
    });

    await fetchChannels();
  }
}

window.onload = function () {
  const savedToken = localStorage.getItem("jwtToken");
  const savedUser = localStorage.getItem("currentUser");

  if (savedToken && savedUser) {
    jwtToken = savedToken;
    currentUser = savedUser;

    document.getElementById("login-screen").style.display = "none";
    document.getElementById("app-screen").style.display = "flex";

    connectWebSocket();
  }
};

document
  .getElementById("message-input")
  .addEventListener("keypress", function (event) {
    if (event.key === "Enter") {
      event.preventDefault();
      sendMessage();
    }
  });
