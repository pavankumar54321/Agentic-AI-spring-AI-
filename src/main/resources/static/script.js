const messagesContainer = document.getElementById('messages');
const userInput = document.getElementById('user-input');
const sendBtn = document.getElementById('send-btn');
const typingIndicator = document.getElementById('typing-indicator');
const languageSelect = document.getElementById('language-select');

let attachedFileContent = null;
let attachedFileName = null;
const attachmentPreview = document.getElementById('attachment-preview');
const attachmentFilenameSpan = document.getElementById('attachment-filename');
const fileInput = document.getElementById('file-input');

function handleFileSelect(event) {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = function(e) {
        attachedFileContent = e.target.result;
        attachedFileName = file.name;
        attachmentFilenameSpan.textContent = file.name;
        attachmentPreview.style.display = 'flex';
    };
    reader.readAsText(file);
}

function removeAttachment() {
    attachedFileContent = null;
    attachedFileName = null;
    fileInput.value = '';
    attachmentPreview.style.display = 'none';
}

// Auto-resize textarea
function autoResize(textarea) {
    textarea.style.height = 'auto';
    textarea.style.height = (textarea.scrollHeight < 150 ? textarea.scrollHeight : 150) + 'px';
}

function handleEnter(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendMessage(e);
    }
}

function clearChat() {
    // Keep only the first welcome message
    while (messagesContainer.children.length > 1) {
        messagesContainer.removeChild(messagesContainer.lastChild);
    }
}

function scrollToBottom() {
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function formatAIResponse(text) {
    // If it's a critical emergency, wrap the top warning in a special div
    if (text.includes('⚠️ MEDICAL EMERGENCY DETECTED')) {
        text = text.replace('⚠️ MEDICAL EMERGENCY DETECTED', '<div class="emergency-alert">⚠️ CRITICAL MEDICAL EMERGENCY DETECTED</div>');
    }

    // Parse markdown to HTML using DOMPurify and Marked.js
    let rawHtml = marked.parse(text);
    return DOMPurify.sanitize(rawHtml);
}

function appendMessage(content, isUser) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${isUser ? 'user-message' : 'ai-message'}`;
    
    const avatarDiv = document.createElement('div');
    avatarDiv.className = `avatar ${isUser ? 'user-avatar' : 'ai-avatar'}`;
    
    const contentDiv = document.createElement('div');
    contentDiv.className = 'message-content';
    
    if (isUser) {
        contentDiv.textContent = content; // Safe raw text for user
    } else {
        contentDiv.innerHTML = formatAIResponse(content); // Parsed HTML for AI
    }
    
    messageDiv.appendChild(avatarDiv);
    messageDiv.appendChild(contentDiv);
    
    messagesContainer.appendChild(messageDiv);
    scrollToBottom();
}

async function sendMessage(e) {
    e.preventDefault();
    
    let text = userInput.value.trim();
    if (!text && !attachedFileContent) return;
    
    let displayMessage = text;
    let queryPayload = text;

    if (attachedFileContent) {
        if (!text) {
             displayMessage = `📎 Attached Medical Report: ${attachedFileName}`;
             queryPayload = `Analyze this medical report:\n\n${attachedFileContent}`;
        } else {
             displayMessage = text + `\n\n📎 Attached Medical Report: ${attachedFileName}`;
             queryPayload = text + `\n\nMedical Report Content:\n${attachedFileContent}`;
        }
    }
    
    // Append user message
    appendMessage(displayMessage, true);
    
    // Check if translation is requested
    const lang = languageSelect.value;
    if (lang !== 'English') {
        queryPayload = queryPayload + ` (Please reply in ${lang})`;
    }

    // Reset input and attachments
    userInput.value = '';
    userInput.style.height = 'auto';
    removeAttachment();
    
    // Disable input and show typing indicator
    userInput.disabled = true;
    sendBtn.disabled = true;
    typingIndicator.style.display = 'flex';
    scrollToBottom();
    
    try {
        // Call the Spring Boot backend
        const baseUrl = window.location.protocol === 'file:' || window.location.hostname === '127.0.0.1' || window.location.hostname === 'localhost' && window.location.port !== '8081'
            ? 'http://localhost:8081' 
            : '';
        const response = await fetch(`${baseUrl}/api/medical/query`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ query: queryPayload })
        });
        
        const data = await response.json();
        
        // Hide typing and enable input BEFORE appending message (in case of error in appending)
        typingIndicator.style.display = 'none';
        userInput.disabled = false;
        sendBtn.disabled = false;
        userInput.focus();
        
        if (!response.ok) {
            appendMessage("Server Error: " + (data.error || "An unknown error occurred on the server."), false);
            return;
        }
        
        // Append AI response
        if (data.response) {
            appendMessage(data.response, false);
        } else if (data.error) {
            appendMessage("Error: " + data.error, false);
        } else {
            appendMessage("Received an unknown response format from the server.", false);
        }
        
    } catch (error) {
        typingIndicator.style.display = 'none';
        userInput.disabled = false;
        sendBtn.disabled = false;
        appendMessage("Connection error: Unable to reach the Medical AI server. Please ensure the backend is running.", false);
        console.error('Error:', error);
    }
}
