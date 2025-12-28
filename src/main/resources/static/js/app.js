let currentChatId = null;
let selectedFile = null;

document.addEventListener('DOMContentLoaded', () => {
    loadChats();
});

function loadChats() {
    fetch('/api/chat')
        .then(response => response.json())
        .then(chats => {
            const list = document.getElementById('chat-list');
            list.innerHTML = '';
            chats.forEach(chat => {
                const div = document.createElement('div');
                div.className = `chat-list-item ${currentChatId === chat.id ? 'active' : ''}`;
                div.onclick = () => loadChat(chat.id, chat.title);
                div.innerHTML = `
                    <div class="chat-list-item-title">${chat.title}</div>
                    <div class="chat-list-item-date">${new Date(chat.createdAt).toLocaleDateString(window.appConfig.locale)}</div>
                `;
                list.appendChild(div);
            });

            if (!currentChatId && chats.length > 0) {
                loadChat(chats[0].id, chats[0].title);
            }
        });
}

function createNewChat() {
    openNewChatModal();
}

function openNewChatModal() {
    const modal = document.getElementById('newChatModal');
    const input = document.getElementById('newChatTopic');
    modal.style.display = 'flex';
    input.value = '';
    setTimeout(() => input.focus(), 100);
}

function closeNewChatModal() {
    document.getElementById('newChatModal').style.display = 'none';
}

function handleModalKeyPress(event) {
    if (event.key === 'Enter') {
        submitNewChat();
    }
}

function submitNewChat() {
    const title = document.getElementById('newChatTopic').value.trim();
    if (title) {
        fetch(`/api/chat/new?title=${encodeURIComponent(title)}`, { method: 'POST' })
            .then(response => response.json())
            .then(chat => {
                closeNewChatModal();
                loadChats();
                loadChat(chat.id, chat.title);
            });
    } else {
        // Optional: Shake animation or border red
        document.getElementById('newChatTopic').style.borderColor = 'red';
    }
}

function loadChat(chatId, title) {
    currentChatId = chatId;
    document.getElementById('chat-title').innerText = title;

    document.querySelectorAll('.chat-list-item').forEach(el => el.classList.remove('active'));

    fetch(`/api/chat/${chatId}`)
        .then(response => response.json())
        .then(chat => {
            const area = document.getElementById('messages-area');
            area.innerHTML = '';
            chat.messages.forEach(msg => {
                appendMessage(msg.content, msg.sender, msg.imageUrl);
            });
            scrollToBottom();
        });
}

function previewImage(input) {
    if (input.files && input.files[0]) {
        selectedFile = input.files[0];
        const reader = new FileReader();
        reader.onload = function (e) {
            document.getElementById('image-preview').src = e.target.result;
            document.getElementById('image-preview-container').style.display = 'block';
        }
        reader.readAsDataURL(input.files[0]);
    }
}

function clearImage() {
    selectedFile = null;
    document.getElementById('file-input').value = "";
    document.getElementById('image-preview-container').style.display = 'none';
}

function sendMessage() {
    const input = document.getElementById('message-input');
    const text = input.value.trim();

    if ((!text && !selectedFile) || !currentChatId) return;

    // Optimistic UI update
    let imageUrl = null;
    if (selectedFile) {
        imageUrl = URL.createObjectURL(selectedFile);
    }
    appendMessage(text, 'USER', imageUrl);

    // Prepare Data
    const formData = new FormData();
    formData.append('content', text);
    if (selectedFile) {
        formData.append('file', selectedFile);
    }

    input.value = '';
    clearImage();
    scrollToBottom();

    fetch(`/api/chat/${currentChatId}/message`, {
        method: 'POST',
        body: formData // Fetch automatically sets Content-Type to multipart/form-data
    })
        .then(async response => {
            if (!response.ok) {
                let errorMsg = "Unknown error";
                try {
                    const errData = await response.json();
                    errorMsg = errData.error || errData.details || response.statusText;
                } catch (e) {
                    errorMsg = response.statusText;
                }
                throw new Error(errorMsg);
            }
            return response.json();
        })
        .then(msg => {
            appendMessage(msg.content, 'AI');
            scrollToBottom();
        })
        .catch(err => {
            console.error("Chat Error:", err);
            appendMessage(window.appConfig.translations.connectionError, 'AI');
        });
}

function appendMessage(text, sender, imageUrl) {
    const area = document.getElementById('messages-area');
    const div = document.createElement('div');
    div.className = `message ${sender.toLowerCase()}`;

    // Parse Markdown for AI responses
    let contentHtml = text;
    if (sender === 'AI' && typeof marked !== 'undefined') {
        contentHtml = marked.parse(text);
    } else {
        // Escape HTML for user messages to prevent XSS (simple replacement)
        contentHtml = text.replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
        // Preserve newlines for user messages
        contentHtml = contentHtml.replace(/\n/g, '<br>');
    }

    let html = `<div class="message-content">${contentHtml}</div>`;
    if (imageUrl) {
        // If it comes from server, it's relative path like uploads/..., need to prepend / if not starts with http or blob
        if (!imageUrl.startsWith('http') && !imageUrl.startsWith('blob')) {
            imageUrl = '/' + imageUrl;
        }
        html += `<img src="${imageUrl}" alt="Uploaded Image">`;
    }

    div.innerHTML = html;
    area.appendChild(div);
}

function handleKeyPress(event) {
    if (event.key === 'Enter') {
        sendMessage();
    }
}

function scrollToBottom() {
    const area = document.getElementById('messages-area');
    area.scrollTop = area.scrollHeight;
}

function shareChat() {
    if (!currentChatId) return;

    fetch(`/api/chat/${currentChatId}`)
        .then(r => r.json())
        .then(chat => {
            const url = window.location.origin + '/chat/share/' + chat.shareToken;
            navigator.clipboard.writeText(url).then(() => {
                alert(window.appConfig.translations.clipboardCopied + '\n' + url);
            });
        });
}
