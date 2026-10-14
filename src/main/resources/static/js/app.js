document.addEventListener('DOMContentLoaded', () => {
    const chatMessages = document.getElementById('chatMessages');
    const chatInput = document.getElementById('chatInput');
    const sendBtn = document.getElementById('sendBtn');
    const ollamaStatusPill = document.getElementById('ollamaStatusPill');
    const statusText = document.getElementById('statusText');
    const statusDot = document.getElementById('statusDot');
    const promptChips = document.querySelectorAll('.prompt-chip');
    const tabButtons = document.querySelectorAll('.tab-btn');
    const dashboardContent = document.getElementById('dashboardContent');

    let isSending = false;

    // Check Ollama status on load
    checkAssistantStatus();

    // Event Listeners
    sendBtn.addEventListener('click', handleSend);
    chatInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSend();
        }
    });

    promptChips.forEach(chip => {
        chip.addEventListener('click', () => {
            const prompt = chip.getAttribute('data-prompt');
            chatInput.value = prompt;
            handleSend();
        });
    });

    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            tabButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            const tab = btn.getAttribute('data-tab');
            loadDashboardTab(tab);
        });
    });

    // Load initial tab
    loadDashboardTab('demands');

    async function checkAssistantStatus() {
        try {
            const res = await fetch('/api/assistant/status');
            if (res.ok) {
                const json = await res.json();
                const data = json.data;
                if (data.ollamaReachable) {
                    statusDot.className = 'pulse-dot';
                    statusText.textContent = `Local Ollama: ${data.model} (Active)`;
                } else {
                    statusDot.className = 'pulse-dot offline';
                    statusText.textContent = `Local Ollama: ${data.model} (Offline)`;
                }
            } else {
                statusDot.className = 'pulse-dot offline';
                statusText.textContent = 'Local Ollama: Offline';
            }
        } catch (e) {
            statusDot.className = 'pulse-dot offline';
            statusText.textContent = 'Local Ollama: Disconnected';
        }
    }

    async function handleSend() {
        const message = chatInput.value.trim();
        if (!message || isSending) return;

        // Append user bubble
        appendMessage('user', message);
        chatInput.value = '';
        chatInput.style.height = 'auto';

        // Add loading bubble
        isSending = true;
        sendBtn.disabled = true;
        const loadingId = 'loading-' + Date.now();
        appendLoading(loadingId);

        try {
            const res = await fetch('/api/assistant/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message })
            });

            removeLoading(loadingId);

            if (res.ok) {
                const json = await res.json();
                const data = json.data;
                appendAssistantMessage(data);
            } else {
                const errJson = await res.json().catch(() => ({}));
                const errMsg = errJson.message || `HTTP ${res.status}: ${res.statusText}`;
                appendErrorMessage(errMsg);
            }
        } catch (e) {
            removeLoading(loadingId);
            appendErrorMessage(`Network error: ${e.message}. Verify that the backend and Ollama are reachable.`);
        } finally {
            isSending = false;
            sendBtn.disabled = false;
            chatInput.focus();
        }
    }

    function appendMessage(role, text) {
        const bubble = document.createElement('div');
        bubble.className = `message-bubble ${role}`;

        const avatar = document.createElement('div');
        avatar.className = `avatar ${role === 'user' ? 'user-avatar' : 'ai-avatar'}`;
        avatar.innerHTML = role === 'user' ? '👤' : '🦙';

        const content = document.createElement('div');
        content.className = 'message-content';
        content.textContent = text;

        bubble.appendChild(avatar);
        bubble.appendChild(content);
        chatMessages.appendChild(bubble);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function appendAssistantMessage(data) {
        const bubble = document.createElement('div');
        bubble.className = 'message-bubble assistant';

        const avatar = document.createElement('div');
        avatar.className = 'avatar ai-avatar';
        avatar.innerHTML = '🦙';

        const content = document.createElement('div');
        content.className = 'message-content';

        const textDiv = document.createElement('div');
        textDiv.style.whiteSpace = 'pre-wrap';
        textDiv.textContent = data.answer;
        content.appendChild(textDiv);

        // Tool metadata
        if (data.toolsInvoked && data.toolsInvoked.length > 0) {
            const metaDiv = document.createElement('div');
            metaDiv.className = 'tool-meta';

            data.toolsInvoked.forEach(tool => {
                const toolBadge = document.createElement('span');
                toolBadge.className = 'badge badge-tool';
                toolBadge.textContent = `🛠️ ${tool.toolName} (${tool.executionDurationMs}ms)`;
                metaDiv.appendChild(toolBadge);
            });

            if (data.grounded) {
                const groundedBadge = document.createElement('span');
                groundedBadge.className = 'badge badge-grounded';
                groundedBadge.textContent = '🔒 Database Grounded';
                metaDiv.appendChild(groundedBadge);
            }

            content.appendChild(metaDiv);
        }

        bubble.appendChild(avatar);
        bubble.appendChild(content);
        chatMessages.appendChild(bubble);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function appendErrorMessage(errorText) {
        const bubble = document.createElement('div');
        bubble.className = 'message-bubble assistant';

        const avatar = document.createElement('div');
        avatar.className = 'avatar ai-avatar';
        avatar.innerHTML = '⚠️';

        const content = document.createElement('div');
        content.className = 'message-content';
        content.style.borderColor = 'rgba(239, 68, 68, 0.5)';
        content.style.backgroundColor = 'rgba(239, 68, 68, 0.1)';
        content.textContent = errorText;

        bubble.appendChild(avatar);
        bubble.appendChild(content);
        chatMessages.appendChild(bubble);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function appendLoading(id) {
        const bubble = document.createElement('div');
        bubble.id = id;
        bubble.className = 'message-bubble assistant';

        const avatar = document.createElement('div');
        avatar.className = 'avatar ai-avatar';
        avatar.innerHTML = '🦙';

        const content = document.createElement('div');
        content.className = 'message-content';
        content.innerHTML = '<div style="display:flex; align-items:center; gap:0.5rem;"><div class="spinner"></div><span>Reasoning & executing tools with llama3.2:3b...</span></div>';

        bubble.appendChild(avatar);
        bubble.appendChild(content);
        chatMessages.appendChild(bubble);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function removeLoading(id) {
        const el = document.getElementById(id);
        if (el) el.remove();
    }

    // Dashboard tabs loader
    async function loadDashboardTab(tab) {
        dashboardContent.innerHTML = '<div style="display:flex; justify-content:center; padding:2rem;"><div class="spinner"></div></div>';

        try {
            if (tab === 'demands') {
                const res = await fetch('/api/demands');
                const json = await res.json();
                renderDemands(json.data || []);
            } else if (tab === 'allocations') {
                const res = await fetch('/api/allocations');
                const json = await res.json();
                renderAllocations(json.data || []);
            } else if (tab === 'candidates') {
                const res = await fetch('/api/demands/50/candidates');
                const json = await res.json();
                renderCandidates(json.data);
            }
        } catch (e) {
            dashboardContent.innerHTML = `<div style="color:var(--accent-danger); padding:1rem;">Failed to load data: ${e.message}</div>`;
        }
    }

    function renderDemands(demands) {
        if (!demands || demands.length === 0) {
            dashboardContent.innerHTML = '<p style="color:var(--text-muted);">No open demands found.</p>';
            return;
        }

        dashboardContent.innerHTML = demands.map(d => `
            <div class="card-item">
                <div class="card-top">
                    <span class="card-title">${d.role} (#${d.id})</span>
                    <span class="badge badge-grounded">${d.status}</span>
                </div>
                <div style="font-size:0.8rem; color:var(--text-secondary); margin-bottom:0.4rem;">
                    🏢 ${d.department} • 📍 ${d.location} • 📅 ${d.startDate} to ${d.endDate} (${d.allocationPercent}%)
                </div>
                <div class="card-tags">
                    ${(d.requiredSkills || []).map(s => `<span class="skill-tag">${s.skillName} (${s.minimumProficiency}${s.mandatory ? '*' : ''})</span>`).join('')}
                </div>
                <div style="margin-top:0.6rem; display:flex; gap:0.5rem;">
                    <button class="action-link-btn" onclick="askAiAboutDemand(${d.id})">Ask AI for Candidates</button>
                    <button class="action-link-btn" onclick="askAiAboutSkills(${d.id})">Analyze Skill Gaps</button>
                </div>
            </div>
        `).join('');
    }

    function renderAllocations(allocations) {
        if (!allocations || allocations.length === 0) {
            dashboardContent.innerHTML = '<p style="color:var(--text-muted);">No resource allocations found.</p>';
            return;
        }

        dashboardContent.innerHTML = allocations.map(a => `
            <div class="card-item">
                <div class="card-top">
                    <span class="card-title">${a.employeeName} (${a.employeeCode})</span>
                    <span class="badge ${a.status === 'CONFIRMED' ? 'badge-tool' : 'badge-grounded'}">${a.status}</span>
                </div>
                <div style="font-size:0.8rem; color:var(--text-secondary);">
                    📌 Demand: ${a.demandCode} • Role: ${a.projectRole} • ${a.allocationPercent}%
                </div>
                <div style="font-size:0.75rem; color:var(--text-muted); margin-top:0.3rem;">
                    📅 ${a.startDate} to ${a.endDate} ${a.confirmedBy ? '• Confirmed by: ' + a.confirmedBy : ''}
                </div>
            </div>
        `).join('');
    }

    function renderCandidates(rec) {
        if (!rec || !rec.candidates) {
            dashboardContent.innerHTML = '<p style="color:var(--text-muted);">No candidate recommendations found.</p>';
            return;
        }

        dashboardContent.innerHTML = `
            <div style="margin-bottom:1rem; padding:0.75rem; background:rgba(99,102,241,0.1); border-radius:8px;">
                <strong>${rec.role} (Demand #${rec.demandId})</strong>
                <div style="font-size:0.8rem; color:var(--text-secondary);">
                    Evaluated: ${rec.totalCandidatesEvaluated} • Eligible: ${rec.eligibleCandidatesCount}
                </div>
            </div>
            ${rec.candidates.map(c => `
                <div class="card-item">
                    <div class="card-top">
                        <span class="card-title">${c.employeeName} (${c.employeeCode})</span>
                        <span class="badge ${c.eligible ? 'badge-tool' : 'badge-grounded'}">
                            ${c.eligible ? 'ELIGIBLE' : 'INELIGIBLE'} • Score: ${c.totalScore}
                        </span>
                    </div>
                    <div style="font-size:0.8rem; color:var(--text-secondary); margin-top:0.3rem;">
                        ${c.matchExplanation}
                    </div>
                </div>
            `).join('')}
        `;
    }

    // Global helper bindings for cards
    window.askAiAboutDemand = (id) => {
        chatInput.value = `Find eligible candidates for demand ${id}`;
        handleSend();
    };

    window.askAiAboutSkills = (id) => {
        chatInput.value = `Which mandatory skills are missing for demand ${id}?`;
        handleSend();
    };
});
