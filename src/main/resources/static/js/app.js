/**
 * Talent and Resource Allocation Marketplace - Frontend Application
 * Handles all 7 screens:
 * 1. Demand Intake
 * 2. Employee Skills Matrix
 * 3. Resource Search and Ranking
 * 4. Candidate Comparison
 * 5. Allocation Approval Workflow
 * 6. Utilization and Skill-Gap Dashboard
 * 7. AI Assistant (Ollama llama3.2:3b)
 */

document.addEventListener('DOMContentLoaded', () => {
    // --- Application State ---
    const state = {
        activeScreen: 'demand-intake',
        skills: [],
        demands: [],
        employees: [],
        employeeSkills: [],
        allocations: [],
        selectedDemandId: null,
        selectedCandidateIdsForComparison: new Set(),
        currentDemandCandidates: [],
        currentAllocationStatusFilter: 'ALL',
        isAiSending: false
    };

    // --- DOM Elements ---
    const navTabs = document.querySelectorAll('.nav-tab');
    const appScreens = document.querySelectorAll('.app-screen');
    const statusDot = document.getElementById('statusDot');
    const statusText = document.getElementById('statusText');
    const toast = document.getElementById('toastNotification');
    const actionModal = document.getElementById('actionModal');
    const modalTitle = document.getElementById('modalTitle');
    const modalBody = document.getElementById('modalBody');
    const modalConfirmBtn = document.getElementById('modalConfirmBtn');
    const modalCancelBtn = document.getElementById('modalCancelBtn');
    const modalCloseBtn = document.getElementById('modalCloseBtn');

    // Screen 1: Demand Intake
    const demandForm = document.getElementById('demandForm');
    const demandCodeInput = document.getElementById('demandCode');
    const btnGenDemandCode = document.getElementById('btnGenDemandCode');
    const skillSelect = document.getElementById('skillSelect');
    const skillProficiency = document.getElementById('skillProficiency');
    const skillMandatory = document.getElementById('skillMandatory');
    const btnAddSkillReq = document.getElementById('btnAddSkillReq');
    const requiredSkillsContainer = document.getElementById('requiredSkillsChipsContainer');
    const emptySkillsPrompt = document.getElementById('emptySkillsPrompt');
    const demandFormErrorBanner = document.getElementById('demandFormErrorBanner');
    const demandFormSuccessBanner = document.getElementById('demandFormSuccessBanner');
    const openDemandsListContainer = document.getElementById('openDemandsListContainer');
    const openDemandsCountBadge = document.getElementById('openDemandsCountBadge');
    const btnRefreshDemands = document.getElementById('btnRefreshDemands');
    let requiredSkillsBuilderList = [];

    // Screen 2: Skills Matrix
    const filterMatrixSearch = document.getElementById('filterMatrixSearch');
    const filterMatrixDepartment = document.getElementById('filterMatrixDepartment');
    const filterMatrixSkill = document.getElementById('filterMatrixSkill');
    const filterMatrixProficiency = document.getElementById('filterMatrixProficiency');
    const filterMatrixStatus = document.getElementById('filterMatrixStatus');
    const matrixTableContainer = document.getElementById('matrixTableContainer');
    const matrixCountDisplay = document.getElementById('matrixCountDisplay');
    const btnRefreshMatrix = document.getElementById('btnRefreshMatrix');

    // Screen 3: Resource Ranking
    const rankingDemandSelect = document.getElementById('rankingDemandSelect');
    const rankingMinScore = document.getElementById('rankingMinScore');
    const rankingEligibleOnly = document.getElementById('rankingEligibleOnly');
    const btnRunRanking = document.getElementById('btnRunRanking');
    const rankingDemandSummary = document.getElementById('rankingDemandSummary');
    const rankingResultsContainer = document.getElementById('rankingResultsContainer');
    const rankingActionToolbar = document.getElementById('rankingActionToolbar');
    const selectedCandidatesCountText = document.getElementById('selectedCandidatesCountText');
    const btnCompareSelectedFromRanking = document.getElementById('btnCompareSelectedFromRanking');
    const btnClearSelectedCandidates = document.getElementById('btnClearSelectedCandidates');

    // Screen 4: Candidate Comparison
    const compareDemandSelect = document.getElementById('compareDemandSelect');
    const btnQuickCompareTop2 = document.getElementById('btnQuickCompareTop2');
    const btnQuickCompareTop3 = document.getElementById('btnQuickCompareTop3');
    const comparisonCandidateCheckboxes = document.getElementById('comparisonCandidateCheckboxes');
    const comparisonTableWrapper = document.getElementById('comparisonTableWrapper');

    // Screen 5: Allocation Approvals
    const statusPillButtons = document.querySelectorAll('.pill-btn');
    const allocationsTableContainer = document.getElementById('allocationsTableContainer');
    const allocationsCountDisplay = document.getElementById('allocationsCountDisplay');
    const btnRefreshAllocations = document.getElementById('btnRefreshAllocations');
    const btnOpenNewAllocationModal = document.getElementById('btnOpenNewAllocationModal');

    // Screen 6: Utilization Dashboard
    const btnRefreshDashboard = document.getElementById('btnRefreshDashboard');
    const kpiActiveEmployees = document.getElementById('kpiActiveEmployees');
    const kpiTotalEmployees = document.getElementById('kpiTotalEmployees');
    const kpiOpenDemands = document.getElementById('kpiOpenDemands');
    const kpiDemandedCapacity = document.getElementById('kpiDemandedCapacity');
    const kpiConfirmedAllocations = document.getElementById('kpiConfirmedAllocations');
    const kpiPendingAllocations = document.getElementById('kpiPendingAllocations');
    const kpiAvgUtilization = document.getElementById('kpiAvgUtilization');
    const utilizationListContainer = document.getElementById('utilizationListContainer');
    const skillGapsListContainer = document.getElementById('skillGapsListContainer');

    // Screen 7: AI Assistant
    const chatMessages = document.getElementById('chatMessages');
    const chatInput = document.getElementById('chatInput');
    const sendBtn = document.getElementById('sendBtn');
    const chatForm = document.getElementById('chatForm');
    const quickPromptsBar = document.getElementById('quickPromptsBar');
    const aiContextDetail = document.getElementById('aiContextDetail');

    // ==========================================================
    // INITIALIZATION & TAB SWITCHING
    // ==========================================================
    async function init() {
        setupNavigation();
        setupEventListeners();
        checkOllamaStatus();
        await loadInitialData();
        renderScreen('demand-intake');
    }

    function setupNavigation() {
        navTabs.forEach(tab => {
            tab.addEventListener('click', () => {
                const targetScreen = tab.getAttribute('data-screen');
                switchScreen(targetScreen);
            });
        });
    }

    function switchScreen(screenId) {
        state.activeScreen = screenId;

        navTabs.forEach(tab => {
            if (tab.getAttribute('data-screen') === screenId) {
                tab.classList.add('active');
            } else {
                tab.classList.remove('active');
            }
        });

        appScreens.forEach(screen => {
            if (screen.id === `screen-${screenId}`) {
                screen.classList.add('active');
            } else {
                screen.classList.remove('active');
            }
        });

        renderScreen(screenId);
    }

    function renderScreen(screenId) {
        if (screenId === 'demand-intake') {
            loadOpenDemandsList();
        } else if (screenId === 'skills-matrix') {
            loadSkillsMatrix();
        } else if (screenId === 'resource-ranking') {
            populateRankingDemandDropdown();
        } else if (screenId === 'candidate-comparison') {
            populateComparisonDemandDropdown();
        } else if (screenId === 'allocation-approvals') {
            loadAllocations();
        } else if (screenId === 'utilization-dashboard') {
            loadUtilizationDashboard();
        } else if (screenId === 'ai-assistant') {
            updateAiContextPrompts();
        }
    }

    // ==========================================================
    // CORE DATA LOADING
    // ==========================================================
    async function loadInitialData() {
        try {
            const [skillsRes, demandsRes, employeesRes, empSkillsRes] = await Promise.all([
                fetch('/api/skills'),
                fetch('/api/demands'),
                fetch('/api/employees'),
                fetch('/api/employee-skills')
            ]);

            if (skillsRes.ok) {
                const json = await skillsRes.json();
                state.skills = json.data || [];
                populateSkillSelectOptions();
            }

            if (demandsRes.ok) {
                const json = await demandsRes.json();
                state.demands = json.data || [];
            }

            if (employeesRes.ok) {
                const json = await employeesRes.json();
                state.employees = json.data || [];
            }

            if (empSkillsRes.ok) {
                const json = await empSkillsRes.json();
                state.employeeSkills = json.data || [];
            }
        } catch (e) {
            console.error('Failed to load initial data:', e);
            showToast('Failed to load initial marketplace data: ' + e.message, 'error');
        }
    }

    // ==========================================================
    // SCREEN 1: DEMAND INTAKE
    // ==========================================================
    function populateSkillSelectOptions() {
        skillSelect.innerHTML = '<option value="">-- Choose Skill --</option>';
        state.skills.forEach(skill => {
            const opt = document.createElement('option');
            opt.value = skill.id;
            opt.textContent = `${skill.name} (${skill.category})`;
            skillSelect.appendChild(opt);
        });

        // Also populate matrix filter
        if (filterMatrixSkill) {
            filterMatrixSkill.innerHTML = '<option value="">All Skills</option>';
            state.skills.forEach(skill => {
                const opt = document.createElement('option');
                opt.value = skill.name;
                opt.textContent = skill.name;
                filterMatrixSkill.appendChild(opt);
            });
        }
    }

    function setupEventListeners() {
        // Gen Demand Code
        btnGenDemandCode.addEventListener('click', () => {
            const rand = Math.floor(100 + Math.random() * 900);
            demandCodeInput.value = `DEM-2026-${rand}`;
        });

        // Add Skill Req to Builder
        btnAddSkillReq.addEventListener('click', () => {
            const skillId = skillSelect.value;
            if (!skillId) {
                showToast('Please select a skill to add', 'error');
                return;
            }

            const skillObj = state.skills.find(s => s.id == skillId);
            if (!skillObj) return;

            // Check if already in builder
            if (requiredSkillsBuilderList.some(item => item.skillId == skillId)) {
                showToast(`Skill '${skillObj.name}' is already added`, 'error');
                return;
            }

            const minProf = skillProficiency.value;
            const isMandatory = skillMandatory.checked;

            requiredSkillsBuilderList.push({
                skillId: Number(skillId),
                skillName: skillObj.name,
                minimumProficiency: minProf,
                mandatory: isMandatory
            });

            renderRequiredSkillChips();
            skillSelect.value = '';
        });

        // Submit Demand Form
        demandForm.addEventListener('submit', handleDemandFormSubmit);
        btnRefreshDemands.addEventListener('click', loadOpenDemandsList);

        // Reset Demand Form
        demandForm.addEventListener('reset', () => {
            requiredSkillsBuilderList = [];
            renderRequiredSkillChips();
            demandFormErrorBanner.style.display = 'none';
            demandFormSuccessBanner.style.display = 'none';
        });

        // Screen 2 Matrix Filters
        [filterMatrixSearch, filterMatrixDepartment, filterMatrixSkill, filterMatrixProficiency, filterMatrixStatus]
            .forEach(input => input && input.addEventListener('input', filterAndRenderMatrixTable));
        btnRefreshMatrix.addEventListener('click', async () => {
            await loadInitialData();
            loadSkillsMatrix();
        });

        // Screen 3 Ranking
        btnRunRanking.addEventListener('click', runCandidateRanking);
        btnCompareSelectedFromRanking.addEventListener('click', () => {
            if (state.selectedCandidateIdsForComparison.size < 2) {
                showToast('Please select at least 2 candidates to compare', 'error');
                return;
            }
            switchScreen('candidate-comparison');
        });
        btnClearSelectedCandidates.addEventListener('click', () => {
            state.selectedCandidateIdsForComparison.clear();
            updateRankingSelectedToolbar();
            document.querySelectorAll('.candidate-compare-chk').forEach(c => c.checked = false);
        });

        // Screen 4 Comparison
        compareDemandSelect.addEventListener('change', () => {
            const demandId = compareDemandSelect.value;
            if (demandId) {
                loadCandidatesForComparisonScreen(demandId);
            }
        });
        btnQuickCompareTop2.addEventListener('click', () => selectTopNCandidatesForComparison(2));
        btnQuickCompareTop3.addEventListener('click', () => selectTopNCandidatesForComparison(3));

        // Screen 5 Allocations
        statusPillButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                statusPillButtons.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                state.currentAllocationStatusFilter = btn.getAttribute('data-status');
                loadAllocations();
            });
        });
        btnRefreshAllocations.addEventListener('click', loadAllocations);
        btnOpenNewAllocationModal.addEventListener('click', openNewAllocationModal);

        // Screen 6 Dashboard
        btnRefreshDashboard.addEventListener('click', loadUtilizationDashboard);
        const btnApplyUtilFilter = document.getElementById('btnApplyUtilFilter');
        if (btnApplyUtilFilter) {
            btnApplyUtilFilter.addEventListener('click', loadUtilizationDashboard);
        }

        // Screen 7 AI Assistant
        sendBtn.addEventListener('click', handleAiSend);
        chatInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleAiSend();
            }
        });

        // Modal close
        modalCloseBtn.addEventListener('click', closeModal);
        modalCancelBtn.addEventListener('click', closeModal);
    }

    function renderRequiredSkillChips() {
        if (requiredSkillsBuilderList.length === 0) {
            emptySkillsPrompt.style.display = 'block';
            requiredSkillsContainer.querySelectorAll('.skill-req-chip').forEach(c => c.remove());
            return;
        }

        emptySkillsPrompt.style.display = 'none';
        requiredSkillsContainer.querySelectorAll('.skill-req-chip').forEach(c => c.remove());

        requiredSkillsBuilderList.forEach((req, idx) => {
            const chip = document.createElement('div');
            chip.className = 'skill-req-chip';
            chip.innerHTML = `
                <span><strong>${escapeHtml(req.skillName)}</strong> (${req.minimumProficiency}${req.mandatory ? ' • Mandatory' : ''})</span>
                <span class="remove-chip" data-idx="${idx}" title="Remove skill">&times;</span>
            `;
            chip.querySelector('.remove-chip').addEventListener('click', (e) => {
                const removeIdx = Number(e.target.getAttribute('data-idx'));
                requiredSkillsBuilderList.splice(removeIdx, 1);
                renderRequiredSkillChips();
            });
            requiredSkillsContainer.appendChild(chip);
        });
    }

    async function handleDemandFormSubmit(e) {
        e.preventDefault();
        demandFormErrorBanner.style.display = 'none';
        demandFormSuccessBanner.style.display = 'none';

        const code = demandCodeInput.value.trim();
        const project = document.getElementById('projectName').value.trim();
        const role = document.getElementById('demandRole').value.trim();
        const dept = document.getElementById('demandDepartment').value;
        const loc = document.getElementById('demandLocation').value.trim();
        const start = document.getElementById('demandStartDate').value;
        const end = document.getElementById('demandEndDate').value;
        const alloc = parseInt(document.getElementById('demandAllocationPercent').value, 10);
        const prio = document.getElementById('demandPriority').value;
        const exp = parseFloat(document.getElementById('demandExperience').value) || 0.0;

        // Front-end date sanity
        if (start && end && start > end) {
            demandFormErrorBanner.textContent = 'Validation error: Demand start date cannot be after end date.';
            demandFormErrorBanner.style.display = 'block';
            return;
        }

        const payload = {
            demandCode: code,
            projectName: project,
            role: role,
            department: dept,
            location: loc,
            startDate: start,
            endDate: end,
            allocationPercent: alloc,
            priority: prio,
            requiredExperienceYears: exp,
            requiredSkills: requiredSkillsBuilderList.map(s => ({
                skillId: s.skillId,
                mandatory: s.mandatory,
                minimumProficiency: s.minimumProficiency
            }))
        };

        const submitBtn = document.getElementById('btnSubmitDemand');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Submitting...';

        try {
            const res = await fetch('/api/demands', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            const json = await res.json();
            if (res.ok) {
                demandFormSuccessBanner.innerHTML = `✅ Demand <strong>${escapeHtml(code)}</strong> created successfully! It is now active for candidate matching.`;
                demandFormSuccessBanner.style.display = 'block';
                demandForm.reset();
                requiredSkillsBuilderList = [];
                renderRequiredSkillChips();
                await loadOpenDemandsList();
                showToast(`Demand ${code} created successfully`, 'success');
            } else {
                let msg = json.message || 'Validation error while creating demand.';
                if (json.errors) {
                    msg += ' ' + Object.entries(json.errors).map(([k, v]) => `${k}: ${v}`).join(', ');
                }
                demandFormErrorBanner.textContent = `❌ ${msg}`;
                demandFormErrorBanner.style.display = 'block';
            }
        } catch (err) {
            demandFormErrorBanner.textContent = `Network error: ${err.message}`;
            demandFormErrorBanner.style.display = 'block';
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Create Demand';
        }
    }

    async function loadOpenDemandsList() {
        openDemandsListContainer.innerHTML = '<div class="loading-state"><div class="spinner"></div><p>Loading open demands...</p></div>';
        try {
            const res = await fetch('/api/demands?status=OPEN');
            const json = await res.json();
            const demands = json.data || [];
            state.demands = demands;
            openDemandsCountBadge.textContent = `${demands.length} Demands`;

            if (demands.length === 0) {
                openDemandsListContainer.innerHTML = '<div class="empty-state"><p>No open demands found. Create one using the form.</p></div>';
                return;
            }

            openDemandsListContainer.innerHTML = demands.map(d => `
                <div class="card" style="margin-bottom:0.75rem; padding:0.85rem; border:1px solid var(--border-color); background:rgba(30,41,59,0.5);">
                    <div style="display:flex; justify-content:space-between; align-items:flex-start;">
                        <div>
                            <strong style="color:var(--text-primary); font-size:0.95rem;">${escapeHtml(d.role)}</strong>
                            <div style="font-size:0.75rem; color:var(--text-secondary); margin-top:0.2rem;">
                                🏢 ${escapeHtml(d.department || 'General')} • 📍 ${escapeHtml(d.location)} • Code: <code>${escapeHtml(d.demandCode)}</code> (#${d.id})
                            </div>
                        </div>
                        <span class="badge ${d.priority === 'CRITICAL' ? 'badge-danger' : d.priority === 'HIGH' ? 'badge-warning' : 'badge-grounded'}">${d.priority}</span>
                    </div>

                    <div style="font-size:0.75rem; color:var(--text-muted); margin:0.4rem 0;">
                        📅 ${d.startDate} to ${d.endDate} • ${d.allocationPercent}% Capacity
                    </div>

                    <div style="display:flex; flex-wrap:wrap; gap:0.35rem; margin-top:0.35rem;">
                        ${(d.requiredSkills || []).map(s => `
                            <span class="badge ${s.mandatory ? 'badge-tool' : 'badge-neutral'}">
                                ${escapeHtml(s.skillName)} (${s.minimumProficiency}${s.mandatory ? '*' : ''})
                            </span>
                        `).join('')}
                    </div>

                    <div style="display:flex; gap:0.5rem; margin-top:0.75rem; padding-top:0.5rem; border-top:1px solid var(--border-color);">
                        <button type="button" class="btn btn-secondary btn-sm" onclick="window.marketplace.selectDemandForRanking(${d.id})">🔍 Rank Candidates</button>
                        <button type="button" class="btn btn-secondary btn-sm" onclick="window.marketplace.askAiAboutDemand(${d.id})">🦙 Ask AI</button>
                    </div>
                </div>
            `).join('');
        } catch (e) {
            openDemandsListContainer.innerHTML = `<div class="error-banner">Failed to load demands: ${escapeHtml(e.message)}</div>`;
        }
    }

    // ==========================================================
    // SCREEN 2: EMPLOYEE SKILLS MATRIX
    // ==========================================================
    async function loadSkillsMatrix() {
        matrixTableContainer.innerHTML = '<div class="loading-state"><div class="spinner"></div><p>Loading employee skills matrix...</p></div>';
        try {
            const [empRes, empSkillsRes] = await Promise.all([
                fetch('/api/employees'),
                fetch('/api/employee-skills')
            ]);

            const empJson = await empRes.json();
            const empSkillsJson = await empSkillsRes.json();

            state.employees = empJson.data || [];
            state.employeeSkills = empSkillsJson.data || [];

            // Populate departments in filter
            const depts = Array.from(new Set(state.employees.map(e => e.department).filter(Boolean)));
            filterMatrixDepartment.innerHTML = '<option value="">All Departments</option>' +
                depts.map(d => `<option value="${escapeHtml(d)}">${escapeHtml(d)}</option>`).join('');

            filterAndRenderMatrixTable();
        } catch (e) {
            matrixTableContainer.innerHTML = `<div class="error-banner">Failed to load skills matrix: ${escapeHtml(e.message)}</div>`;
        }
    }

    function filterAndRenderMatrixTable() {
        const search = (filterMatrixSearch.value || '').trim().toLowerCase();
        const dept = filterMatrixDepartment.value;
        const skillFilter = filterMatrixSkill.value;
        const profFilter = filterMatrixProficiency.value;
        const statusFilter = filterMatrixStatus.value;

        // Group employee skills by employeeId
        const skillsByEmp = {};
        state.employeeSkills.forEach(es => {
            if (!skillsByEmp[es.employeeId]) skillsByEmp[es.employeeId] = [];
            skillsByEmp[es.employeeId].push(es);
        });

        const filtered = state.employees.filter(emp => {
            // Text search
            if (search) {
                const matchesCode = (emp.employeeCode || '').toLowerCase().includes(search);
                const matchesName = (emp.name || '').toLowerCase().includes(search);
                if (!matchesCode && !matchesName) return false;
            }

            // Department filter
            if (dept && emp.department !== dept) return false;

            // Status filter
            if (statusFilter && emp.employmentStatus !== statusFilter) return false;

            // Skill & Proficiency filter
            const empSkills = skillsByEmp[emp.id] || [];
            if (skillFilter) {
                const hasSkill = empSkills.some(s => s.skillName === skillFilter);
                if (!hasSkill) return false;
            }

            if (profFilter) {
                const weightMap = { 'BEGINNER': 1, 'INTERMEDIATE': 2, 'ADVANCED': 3 };
                const requiredWeight = weightMap[profFilter] || 0;
                const meetsProf = empSkills.some(s => {
                    if (skillFilter && s.skillName !== skillFilter) return false;
                    return (weightMap[s.proficiencyLevel] || 0) >= requiredWeight;
                });
                if (!meetsProf) return false;
            }

            return true;
        });

        matrixCountDisplay.textContent = `Showing ${filtered.length} of ${state.employees.length} employees`;

        if (filtered.length === 0) {
            matrixTableContainer.innerHTML = '<div class="empty-state"><p>No employees match the specified filters.</p></div>';
            return;
        }

        matrixTableContainer.innerHTML = `
            <table class="standard-table">
                <thead>
                    <tr>
                        <th>Employee</th>
                        <th>Department & Role</th>
                        <th>Status</th>
                        <th>Capacity</th>
                        <th>Skills & Verified Proficiencies</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    ${filtered.map(emp => {
                        const empSkills = skillsByEmp[emp.id] || [];
                        const statusBadgeClass = emp.employmentStatus === 'ACTIVE' ? 'badge-tool' :
                                                 emp.employmentStatus === 'ON_LEAVE' ? 'badge-warning' : 'badge-danger';
                        return `
                            <tr>
                                <td>
                                    <div style="font-weight:600; color:var(--text-primary);">${escapeHtml(emp.name)}</div>
                                    <div style="font-size:0.75rem; color:var(--text-secondary); font-family:monospace;">${escapeHtml(emp.employeeCode)}</div>
                                </td>
                                <td>
                                    <div>${escapeHtml(emp.designation || 'Staff')}</div>
                                    <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(emp.department || 'N/A')} • ${escapeHtml(emp.location || 'Remote')}</div>
                                </td>
                                <td>
                                    <span class="badge ${statusBadgeClass}">${emp.employmentStatus}</span>
                                </td>
                                <td>
                                    <strong>${emp.maxCapacityPercent}%</strong>
                                </td>
                                <td>
                                    <div style="display:flex; flex-wrap:wrap; gap:0.3rem;">
                                        ${empSkills.length === 0 ? '<span class="text-muted text-xs">No skills assigned</span>' :
                                          empSkills.map(s => `
                                            <span class="badge ${s.proficiencyLevel === 'ADVANCED' ? 'badge-grounded' : s.proficiencyLevel === 'INTERMEDIATE' ? 'badge-tool' : 'badge-neutral'}" title="${s.yearsOfExperience || 0} years experience">
                                                ${escapeHtml(s.skillName)}: ${s.proficiencyLevel} (${s.yearsOfExperience || 0}y)
                                            </span>
                                          `).join('')}
                                    </div>
                                </td>
                                <td>
                                    <button type="button" class="btn btn-secondary btn-sm" onclick="window.marketplace.askAiAboutEmployee('${escapeHtml(emp.name)}')">🦙 Inquire</button>
                                </td>
                            </tr>
                        `;
                    }).join('')}
                </tbody>
            </table>
        `;
    }

    // ==========================================================
    // SCREEN 3: RESOURCE SEARCH AND RANKING
    // ==========================================================
    function populateRankingDemandDropdown() {
        rankingDemandSelect.innerHTML = '<option value="">-- Choose Demand to Evaluate --</option>';
        state.demands.forEach(d => {
            const opt = document.createElement('option');
            opt.value = d.id;
            opt.textContent = `${d.role} (${d.demandCode}) - ${d.department}`;
            if (state.selectedDemandId && d.id == state.selectedDemandId) {
                opt.selected = true;
            }
            rankingDemandSelect.appendChild(opt);
        });

        if (state.selectedDemandId) {
            runCandidateRanking();
        }
    }

    async function runCandidateRanking() {
        const demandId = rankingDemandSelect.value;
        if (!demandId) {
            showToast('Please select a project demand to evaluate', 'error');
            return;
        }

        state.selectedDemandId = demandId;
        const demand = state.demands.find(d => d.id == demandId);

        // Render Demand Summary Ribbon
        if (demand) {
            rankingDemandSummary.style.display = 'flex';
            rankingDemandSummary.innerHTML = `
                <div><strong>Role:</strong> ${escapeHtml(demand.role)}</div>
                <div><strong>Code:</strong> <code>${escapeHtml(demand.demandCode)}</code></div>
                <div><strong>Timeline:</strong> ${demand.startDate} to ${demand.endDate} (${demand.allocationPercent}%)</div>
                <div><strong>Location:</strong> ${escapeHtml(demand.location)}</div>
                <div><strong>Skills:</strong> ${(demand.requiredSkills || []).map(s => `${escapeHtml(s.skillName)}${s.mandatory ? '*' : ''}`).join(', ')}</div>
            `;
        }

        rankingResultsContainer.innerHTML = '<div class="loading-state"><div class="spinner"></div><p>Evaluating candidates with deterministic matching engine...</p></div>';

        const eligibleOnly = rankingEligibleOnly.checked;
        const minScore = parseFloat(rankingMinScore.value) || null;

        let url = `/api/demands/${demandId}/candidates?eligibleOnly=${eligibleOnly}`;
        if (minScore !== null && minScore > 0) {
            url += `&minScore=${minScore}`;
        }

        try {
            const res = await fetch(url);
            const json = await res.json();
            const data = json.data;
            const candidates = data ? data.candidates || [] : [];
            state.currentDemandCandidates = candidates;

            if (candidates.length === 0) {
                rankingResultsContainer.innerHTML = `
                    <div class="empty-state">
                        <span class="empty-icon">⚠️</span>
                        <h3>No Matching Candidates</h3>
                        <p>No candidates satisfied the current criteria for demand #${demandId} (Eligible Only: ${eligibleOnly}). Try unchecking 'Eligible Only'.</p>
                    </div>
                `;
                return;
            }

            rankingResultsContainer.innerHTML = `
                <div style="margin-bottom:1rem; display:flex; justify-content:space-between; align-items:center;">
                    <span class="text-sm text-secondary">
                        Evaluated <strong>${data.totalCandidatesEvaluated}</strong> candidates • <strong>${data.eligibleCandidatesCount}</strong> eligible
                    </span>
                </div>
                ${candidates.map((c, index) => {
                    const isSelected = state.selectedCandidateIdsForComparison.has(c.employeeId);
                    return `
                        <div class="candidate-card ${c.eligible ? 'eligible-card' : 'ineligible-card'}">
                            <div class="candidate-card-top">
                                <div class="candidate-info">
                                    <div class="rank-badge">#${index + 1}</div>
                                    <div>
                                        <div class="candidate-name">${escapeHtml(c.employeeName)}</div>
                                        <div class="candidate-sub">
                                            ${escapeHtml(c.designation || 'Specialist')} • ${escapeHtml(c.department)} • ${escapeHtml(c.location)} • Code: <code>${escapeHtml(c.employeeCode)}</code>
                                        </div>
                                    </div>
                                </div>
                                <div class="candidate-score-block">
                                    <span class="badge ${c.eligible ? 'badge-tool' : 'badge-danger'}">
                                        ${c.eligible ? 'ELIGIBLE' : 'INELIGIBLE'}
                                    </span>
                                    <div class="score-number">${c.totalScore.toFixed(1)}<span style="font-size:0.8rem; color:var(--text-muted);">/100</span></div>
                                </div>
                            </div>

                            <!-- Score breakdown tags -->
                            <div class="score-breakdown-tags">
                                <span class="score-tag" title="Skill Match Score">🎯 Skills: <strong>${c.skillScore.toFixed(1)}/70</strong></span>
                                <span class="score-tag" title="Availability Score">📅 Availability: <strong>${c.availabilityScore.toFixed(1)}/20</strong></span>
                                <span class="score-tag" title="Location Alignment Score">📍 Location: <strong>${c.locationScore.toFixed(1)}/10</strong></span>
                            </div>

                            <!-- Matched vs Missing Skills -->
                            <div style="font-size:0.775rem; margin:0.4rem 0;">
                                <div style="margin-bottom:0.3rem;">
                                    <strong>Matched Skills:</strong>
                                    ${(c.matchedSkills && c.matchedSkills.length > 0) ?
                                        c.matchedSkills.map(m => `<span class="badge badge-tool" style="margin:0 0.2rem 0.2rem 0;">${escapeHtml(m.skillName)} (${m.candidateProficiency})</span>`).join('') :
                                        '<span class="text-muted">None</span>'}
                                </div>

                                ${c.missingMandatorySkills && c.missingMandatorySkills.length > 0 ? `
                                    <div style="margin-bottom:0.3rem;">
                                        <strong style="color:var(--accent-danger);">Missing Mandatory:</strong>
                                        ${c.missingMandatorySkills.map(m => `<span class="badge badge-danger" style="margin:0 0.2rem 0.2rem 0;">${escapeHtml(m.skillName)} (Req: ${m.requiredProficiency})</span>`).join('')}
                                    </div>
                                ` : ''}

                                ${c.exclusionReasons && c.exclusionReasons.length > 0 ? `
                                    <div style="margin-top:0.4rem; color:#fca5a5; font-size:0.75rem;">
                                        ⛔ <strong>Exclusion Reasons:</strong> ${c.exclusionReasons.map(r => escapeHtml(r)).join('; ')}
                                    </div>
                                ` : ''}
                            </div>

                            <!-- Deterministic Match Explanation -->
                            <div class="candidate-explanation">
                                💡 ${escapeHtml(c.matchExplanation)}
                            </div>

                            <!-- Card Action Footer -->
                            <div class="candidate-card-actions">
                                <label class="checkbox-container">
                                    <input type="checkbox" class="candidate-compare-chk" data-empid="${c.employeeId}" ${isSelected ? 'checked' : ''}>
                                    <span class="checkbox-label">Select for Comparison</span>
                                </label>
                                <div style="display:flex; gap:0.5rem;">
                                    ${c.eligible ? `
                                        <button type="button" class="btn btn-primary btn-sm" onclick="window.marketplace.openCreateAllocationForCandidate(${demandId}, ${c.employeeId}, '${escapeHtml(c.employeeName)}')">⚡ Allocate Candidate</button>
                                    ` : `
                                        <button type="button" class="btn btn-secondary btn-sm" disabled title="Candidate is not eligible based on capacity/skills">Ineligible for Allocation</button>
                                    `}
                                    <button type="button" class="btn btn-secondary btn-sm" onclick="window.marketplace.askAiAboutCandidateDemand(${demandId}, '${escapeHtml(c.employeeName)}')">🦙 Inquire</button>
                                </div>
                            </div>
                        </div>
                    `;
                }).join('')}
            `;

            // Bind checkbox listeners for comparison selection
            document.querySelectorAll('.candidate-compare-chk').forEach(chk => {
                chk.addEventListener('change', (e) => {
                    const empId = Number(e.target.getAttribute('data-empid'));
                    if (e.target.checked) {
                        state.selectedCandidateIdsForComparison.add(empId);
                    } else {
                        state.selectedCandidateIdsForComparison.delete(empId);
                    }
                    updateRankingSelectedToolbar();
                });
            });

            updateRankingSelectedToolbar();
        } catch (e) {
            rankingResultsContainer.innerHTML = `<div class="error-banner">Failed to rank candidates: ${escapeHtml(e.message)}</div>`;
        }
    }

    function updateRankingSelectedToolbar() {
        const count = state.selectedCandidateIdsForComparison.size;
        if (count > 0) {
            rankingActionToolbar.style.display = 'flex';
            selectedCandidatesCountText.textContent = `${count} candidate${count > 1 ? 's' : ''} selected for comparison`;
        } else {
            rankingActionToolbar.style.display = 'none';
        }
    }

    // ==========================================================
    // SCREEN 4: CANDIDATE COMPARISON
    // ==========================================================
    function populateComparisonDemandDropdown() {
        compareDemandSelect.innerHTML = '<option value="">-- Choose Demand --</option>';
        state.demands.forEach(d => {
            const opt = document.createElement('option');
            opt.value = d.id;
            opt.textContent = `${d.role} (${d.demandCode})`;
            if (state.selectedDemandId && d.id == state.selectedDemandId) {
                opt.selected = true;
            }
            compareDemandSelect.appendChild(opt);
        });

        if (state.selectedDemandId) {
            loadCandidatesForComparisonScreen(state.selectedDemandId);
        }
    }

    async function loadCandidatesForComparisonScreen(demandId) {
        comparisonCandidateCheckboxes.innerHTML = '<div class="loading-state"><div class="spinner"></div><p>Loading candidates for comparison...</p></div>';
        comparisonCandidateCheckboxes.style.display = 'block';

        try {
            const res = await fetch(`/api/demands/${demandId}/candidates?eligibleOnly=false`);
            const json = await res.json();
            const candidates = json.data ? json.data.candidates || [] : [];
            state.currentDemandCandidates = candidates;

            if (candidates.length === 0) {
                comparisonCandidateCheckboxes.innerHTML = '<p class="text-sm text-muted">No candidates found for this demand.</p>';
                comparisonTableWrapper.innerHTML = '<div class="empty-state"><p>No candidates available for comparison.</p></div>';
                return;
            }

            comparisonCandidateCheckboxes.innerHTML = `
                <div style="width:100%; margin-bottom:0.5rem; font-size:0.8rem; font-weight:600; color:var(--text-secondary);">
                    Select at least 2 candidates to compare side-by-side:
                </div>
                ${candidates.map(c => `
                    <label class="checkbox-container" style="background:rgba(30,41,59,0.6); padding:0.4rem 0.65rem; border-radius:var(--radius-sm); border:1px solid var(--border-color);">
                        <input type="checkbox" class="comp-candidate-selector" data-empid="${c.employeeId}" ${state.selectedCandidateIdsForComparison.has(c.employeeId) ? 'checked' : ''}>
                        <span>${escapeHtml(c.employeeName)} (${c.totalScore.toFixed(0)} pts • ${c.eligible ? 'Eligible' : 'Ineligible'})</span>
                    </label>
                `).join('')}
            `;

            // If fewer than 2 candidates selected, auto-select top 2
            if (state.selectedCandidateIdsForComparison.size < 2 && candidates.length >= 2) {
                state.selectedCandidateIdsForComparison.clear();
                state.selectedCandidateIdsForComparison.add(candidates[0].employeeId);
                state.selectedCandidateIdsForComparison.add(candidates[1].employeeId);
                document.querySelectorAll('.comp-candidate-selector').forEach(chk => {
                    const id = Number(chk.getAttribute('data-empid'));
                    if (state.selectedCandidateIdsForComparison.has(id)) chk.checked = true;
                });
            }

            // Checkbox listener
            document.querySelectorAll('.comp-candidate-selector').forEach(chk => {
                chk.addEventListener('change', (e) => {
                    const id = Number(e.target.getAttribute('data-empid'));
                    if (e.target.checked) {
                        state.selectedCandidateIdsForComparison.add(id);
                    } else {
                        state.selectedCandidateIdsForComparison.delete(id);
                    }
                    renderComparisonMatrix();
                });
            });

            renderComparisonMatrix();
        } catch (e) {
            comparisonCandidateCheckboxes.innerHTML = `<div class="error-banner">Failed to load candidates: ${escapeHtml(e.message)}</div>`;
        }
    }

    function selectTopNCandidatesForComparison(n) {
        if (!state.currentDemandCandidates || state.currentDemandCandidates.length === 0) return;
        state.selectedCandidateIdsForComparison.clear();
        state.currentDemandCandidates.slice(0, n).forEach(c => {
            state.selectedCandidateIdsForComparison.add(c.employeeId);
        });
        document.querySelectorAll('.comp-candidate-selector').forEach(chk => {
            const id = Number(chk.getAttribute('data-empid'));
            chk.checked = state.selectedCandidateIdsForComparison.has(id);
        });
        renderComparisonMatrix();
    }

    function renderComparisonMatrix() {
        const selectedIds = Array.from(state.selectedCandidateIdsForComparison);
        const candidates = state.currentDemandCandidates.filter(c => selectedIds.includes(c.employeeId));

        if (candidates.length < 2) {
            comparisonTableWrapper.innerHTML = `
                <div class="empty-state">
                    <span class="empty-icon">⚖️</span>
                    <h3>Select at least 2 candidates</h3>
                    <p>Currently, ${candidates.length} candidate is selected. Check at least 2 candidates above to generate the side-by-side comparison matrix.</p>
                </div>
            `;
            return;
        }

        const demand = state.demands.find(d => d.id == (compareDemandSelect.value || state.selectedDemandId));
        const requiredSkills = demand ? demand.requiredSkills || [] : [];

        comparisonTableWrapper.innerHTML = `
            <table class="comparison-table">
                <thead>
                    <tr>
                        <th class="col-feature">Candidate Profile</th>
                        ${candidates.map(c => `
                            <th class="col-candidate">
                                <div style="font-size:1.05rem; font-weight:700; color:var(--text-primary);">${escapeHtml(c.employeeName)}</div>
                                <div style="font-size:0.75rem; color:var(--text-secondary); font-family:monospace;">${escapeHtml(c.employeeCode)}</div>
                                <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(c.designation || 'Staff')} • ${escapeHtml(c.department)}</div>
                            </th>
                        `).join('')}
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td class="col-feature">Eligibility Verdict</td>
                        ${candidates.map(c => `
                            <td style="text-align:center;">
                                <span class="badge ${c.eligible ? 'badge-tool' : 'badge-danger'}" style="font-size:0.85rem; padding:0.35rem 0.8rem;">
                                    ${c.eligible ? 'ELIGIBLE' : 'INELIGIBLE'}
                                </span>
                            </td>
                        `).join('')}
                    </tr>

                    <tr>
                        <td class="col-feature">Overall Match Score</td>
                        ${candidates.map(c => `
                            <td>
                                <div style="font-size:1.35rem; font-weight:700; text-align:center; color:var(--text-primary);">
                                    ${c.totalScore.toFixed(1)} / 100
                                </div>
                                <div class="progress-bar-container">
                                    <div class="progress-bar-fill ${c.eligible ? 'normal' : 'danger'}" style="width: ${Math.min(100, c.totalScore)}%;"></div>
                                </div>
                            </td>
                        `).join('')}
                    </tr>

                    <tr>
                        <td class="col-feature">Score Breakdown</td>
                        ${candidates.map(c => `
                            <td>
                                <div style="display:flex; flex-direction:column; gap:0.25rem;">
                                    <div>🎯 Skills: <strong>${c.skillScore.toFixed(1)}</strong> / 70</div>
                                    <div>📅 Availability: <strong>${c.availabilityScore.toFixed(1)}</strong> / 20</div>
                                    <div>📍 Location: <strong>${c.locationScore.toFixed(1)}</strong> / 10</div>
                                </div>
                            </td>
                        `).join('')}
                    </tr>

                    <tr>
                        <td class="col-feature">Availability Assessment</td>
                        ${candidates.map(c => {
                            const avail = c.availabilityAssessment;
                            return `
                                <td>
                                    <div>Status: <span class="badge ${avail && avail.available ? 'badge-tool' : 'badge-danger'}">${avail && avail.available ? 'AVAILABLE' : 'UNAVAILABLE'}</span></div>
                                    <div style="font-size:0.75rem; color:var(--text-secondary); margin-top:0.3rem;">
                                        Remaining Capacity: <strong>${avail ? avail.remainingCapacityPercent : 'N/A'}%</strong>
                                    </div>
                                    ${avail && avail.reason ? `<div style="font-size:0.75rem; color:var(--text-muted); margin-top:0.2rem;">${escapeHtml(avail.reason)}</div>` : ''}
                                </td>
                            `;
                        }).join('')}
                    </tr>

                    ${requiredSkills.map(reqSkill => `
                        <tr>
                            <td class="col-feature">
                                <strong>${escapeHtml(reqSkill.skillName)}</strong>
                                <div style="font-size:0.7rem; color:var(--text-muted);">${reqSkill.mandatory ? 'Mandatory' : 'Optional'} (Req: ${reqSkill.minimumProficiency})</div>
                            </td>
                            ${candidates.map(c => {
                                const matched = (c.matchedSkills || []).find(m => m.skillId === reqSkill.skillId || m.skillName === reqSkill.skillName);
                                const isMissingMandatory = (c.missingMandatorySkills || []).some(m => m.skillId === reqSkill.skillId || m.skillName === reqSkill.skillName);

                                if (matched) {
                                    return `
                                        <td>
                                            <span class="badge badge-tool">✅ ${matched.candidateProficiency}</span>
                                            <div style="font-size:0.7rem; color:var(--text-muted); margin-top:0.2rem;">${matched.yearsOfExperience || 0} years experience</div>
                                        </td>
                                    `;
                                } else if (isMissingMandatory) {
                                    return `
                                        <td>
                                            <span class="badge badge-danger">❌ Missing (Mandatory)</span>
                                        </td>
                                    `;
                                } else {
                                    return `
                                        <td>
                                            <span class="badge badge-neutral">⚠️ Not Possessed</span>
                                        </td>
                                    `;
                                }
                            }).join('')}
                        </tr>
                    `).join('')}

                    <tr>
                        <td class="col-feature">Missing Mandatory Skills</td>
                        ${candidates.map(c => `
                            <td>
                                ${(c.missingMandatorySkills && c.missingMandatorySkills.length > 0) ?
                                    c.missingMandatorySkills.map(m => `<span class="badge badge-danger" style="margin:0.2rem;">${escapeHtml(m.skillName)}</span>`).join('') :
                                    '<span class="badge badge-tool">None - All Met</span>'}
                            </td>
                        `).join('')}
                    </tr>

                    <tr>
                        <td class="col-feature">Exclusion Reasons</td>
                        ${candidates.map(c => `
                            <td>
                                ${(c.exclusionReasons && c.exclusionReasons.length > 0) ?
                                    c.exclusionReasons.map(r => `<div style="color:#fca5a5; font-size:0.75rem; margin-bottom:0.2rem;">• ${escapeHtml(r)}</div>`).join('') :
                                    '<span class="text-sm text-secondary">None (Eligible)</span>'}
                            </td>
                        `).join('')}
                    </tr>

                    <tr>
                        <td class="col-feature">Match Explanation</td>
                        ${candidates.map(c => `
                            <td style="font-size:0.775rem; color:var(--text-secondary); line-height:1.4;">
                                ${escapeHtml(c.matchExplanation)}
                            </td>
                        `).join('')}
                    </tr>

                    <tr>
                        <td class="col-feature">Action</td>
                        ${candidates.map(c => `
                            <td style="text-align:center;">
                                ${c.eligible ? `
                                    <button type="button" class="btn btn-primary btn-sm" onclick="window.marketplace.openCreateAllocationForCandidate(${demand ? demand.id : 'null'}, ${c.employeeId}, '${escapeHtml(c.employeeName)}')">Allocate</button>
                                ` : `
                                    <button type="button" class="btn btn-secondary btn-sm" disabled>Ineligible</button>
                                `}
                            </td>
                        `).join('')}
                    </tr>
                </tbody>
            </table>
        `;
    }

    // ==========================================================
    // SCREEN 5: ALLOCATION APPROVAL WORKFLOW
    // ==========================================================
    async function loadAllocations() {
        allocationsTableContainer.innerHTML = '<div class="loading-state"><div class="spinner"></div><p>Loading resource allocations...</p></div>';

        let url = '/api/allocations';
        if (state.currentAllocationStatusFilter && state.currentAllocationStatusFilter !== 'ALL') {
            url += `?status=${state.currentAllocationStatusFilter}`;
        }

        try {
            const res = await fetch(url);
            const json = await res.json();
            const allocations = json.data || [];
            state.allocations = allocations;

            allocationsCountDisplay.textContent = `${allocations.length} allocation${allocations.length === 1 ? '' : 's'}`;

            if (allocations.length === 0) {
                allocationsTableContainer.innerHTML = `
                    <div class="empty-state">
                        <span class="empty-icon">🛡️</span>
                        <h3>No Allocations Found</h3>
                        <p>There are no resource allocation records matching status filter '<strong>${state.currentAllocationStatusFilter}</strong>'.</p>
                    </div>
                `;
                return;
            }

            allocationsTableContainer.innerHTML = `
                <table class="standard-table">
                    <thead>
                        <tr>
                            <th>ID & Status</th>
                            <th>Resource / Employee</th>
                            <th>Project Demand</th>
                            <th>Allocated %</th>
                            <th>Date Range</th>
                            <th>Audit / Reason</th>
                            <th>Lifecycle Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${allocations.map(a => {
                            const statusClass = a.status === 'CONFIRMED' ? 'badge-tool' :
                                                a.status === 'PENDING' ? 'badge-warning' :
                                                a.status === 'REJECTED' ? 'badge-danger' : 'badge-neutral';
                            return `
                                <tr>
                                    <td>
                                        <div style="font-weight:700;">#${a.id}</div>
                                        <span class="badge ${statusClass}">${a.status}</span>
                                    </td>
                                    <td>
                                        <div style="font-weight:600; color:var(--text-primary);">${escapeHtml(a.employeeName)}</div>
                                        <div style="font-size:0.75rem; color:var(--text-secondary); font-family:monospace;">${escapeHtml(a.employeeCode)}</div>
                                    </td>
                                    <td>
                                        <div style="font-weight:600;">${escapeHtml(a.projectRole || 'Project')}</div>
                                        <div style="font-size:0.75rem; color:var(--text-secondary);">Demand: <code>${escapeHtml(a.demandCode)}</code></div>
                                    </td>
                                    <td>
                                        <strong style="font-size:0.95rem;">${a.allocationPercent}%</strong>
                                    </td>
                                    <td>
                                        <div style="font-size:0.8rem;">${a.startDate} to ${a.endDate}</div>
                                    </td>
                                    <td>
                                        <div style="font-size:0.75rem; color:var(--text-secondary);">
                                            ${a.confirmedBy ? `<div>Confirmed by: <strong>${escapeHtml(a.confirmedBy)}</strong></div>` : ''}
                                            ${a.rejectionReason ? `<div style="color:#fca5a5;">Reason: ${escapeHtml(a.rejectionReason)}</div>` : ''}
                                            <div style="color:var(--text-muted); font-size:0.7rem;">Created: ${formatDate(a.createdAt)}</div>
                                        </div>
                                    </td>
                                    <td>
                                        <div style="display:flex; gap:0.4rem; flex-wrap:wrap;">
                                            ${a.status === 'PENDING' ? `
                                                <button type="button" class="btn btn-primary btn-sm" onclick="window.marketplace.confirmAllocationDialog(${a.id}, '${escapeHtml(a.employeeName)}', ${a.allocationPercent})">Confirm</button>
                                                <button type="button" class="btn btn-danger btn-sm" onclick="window.marketplace.rejectAllocationDialog(${a.id}, '${escapeHtml(a.employeeName)}')">Reject</button>
                                            ` : ''}
                                            ${a.status === 'CONFIRMED' ? `
                                                <button type="button" class="btn btn-danger btn-sm" onclick="window.marketplace.cancelAllocationDialog(${a.id}, '${escapeHtml(a.employeeName)}')">Cancel</button>
                                            ` : ''}
                                            ${a.status === 'REJECTED' || a.status === 'CANCELLED' ? `
                                                <span class="text-xs text-muted">Closed</span>
                                            ` : ''}
                                        </div>
                                    </td>
                                </tr>
                            `;
                        }).join('')}
                    </tbody>
                </table>
            `;
        } catch (e) {
            allocationsTableContainer.innerHTML = `<div class="error-banner">Failed to load allocations: ${escapeHtml(e.message)}</div>`;
        }
    }

    // Allocation Lifecycle Modals
    function confirmAllocationDialog(allocationId, employeeName, percent) {
        showModal({
            title: `Confirm Allocation #${allocationId}`,
            bodyHtml: `
                <p>Are you sure you want to confirm the <strong>${percent}%</strong> allocation for <strong>${escapeHtml(employeeName)}</strong>?</p>
                <div style="margin-top:1rem;" class="form-group">
                    <label for="modalConfirmedByInput">Confirmed By (Approver Name)</label>
                    <input type="text" id="modalConfirmedByInput" value="Resource Manager" required>
                </div>
                <div class="text-sm text-secondary" style="margin-top:0.75rem; background:rgba(99,102,241,0.1); padding:0.6rem; border-radius:4px;">
                    🔒 <strong>Capacity Safety:</strong> This transaction executes serialized pessimistic write locking on the employee row to ensure 100% capacity safety.
                </div>
            `,
            confirmText: 'Confirm Allocation',
            confirmClass: 'btn-primary',
            onConfirm: async () => {
                const confirmedBy = (document.getElementById('modalConfirmedByInput').value || 'Resource Manager').trim();
                try {
                    const res = await fetch(`/api/allocations/${allocationId}/confirm`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ confirmedBy })
                    });
                    const json = await res.json();
                    if (res.ok) {
                        showToast(`Allocation #${allocationId} confirmed successfully!`, 'success');
                        closeModal();
                        loadAllocations();
                    } else {
                        showToast(`Confirmation failed: ${json.message || res.statusText}`, 'error');
                    }
                } catch (err) {
                    showToast(`Error confirming allocation: ${err.message}`, 'error');
                }
            }
        });
    }

    function rejectAllocationDialog(allocationId, employeeName) {
        showModal({
            title: `Reject Allocation #${allocationId}`,
            bodyHtml: `
                <p>Please enter the business rejection reason for candidate <strong>${escapeHtml(employeeName)}</strong>:</p>
                <div style="margin-top:1rem;" class="form-group">
                    <label for="modalRejectionReasonInput">Rejection Reason <span class="required">*</span></label>
                    <textarea id="modalRejectionReasonInput" rows="2" placeholder="e.g. Candidate required for higher priority client engagement" required></textarea>
                </div>
                <div style="margin-top:0.75rem;" class="form-group">
                    <label for="modalRejectedByInput">Rejected By</label>
                    <input type="text" id="modalRejectedByInput" value="Staffing Director">
                </div>
            `,
            confirmText: 'Reject Allocation',
            confirmClass: 'btn-danger',
            onConfirm: async () => {
                const reason = (document.getElementById('modalRejectionReasonInput').value || '').trim();
                const rejectedBy = (document.getElementById('modalRejectedByInput').value || 'Staffing Director').trim();

                if (!reason) {
                    showToast('Rejection reason is required', 'error');
                    return;
                }

                try {
                    const res = await fetch(`/api/allocations/${allocationId}/reject`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ reason, rejectedBy })
                    });
                    const json = await res.json();
                    if (res.ok) {
                        showToast(`Allocation #${allocationId} rejected.`, 'success');
                        closeModal();
                        loadAllocations();
                    } else {
                        showToast(`Rejection failed: ${json.message || res.statusText}`, 'error');
                    }
                } catch (err) {
                    showToast(`Error rejecting allocation: ${err.message}`, 'error');
                }
            }
        });
    }

    function cancelAllocationDialog(allocationId, employeeName) {
        showModal({
            title: `Cancel Confirmed Allocation #${allocationId}`,
            bodyHtml: `
                <p>Are you sure you want to cancel the confirmed allocation for <strong>${escapeHtml(employeeName)}</strong>? This will release employee capacity back to the available pool.</p>
                <div style="margin-top:1rem;" class="form-group">
                    <label for="modalCancelReasonInput">Cancellation Reason</label>
                    <textarea id="modalCancelReasonInput" rows="2" placeholder="e.g. Project scope reduced or delayed"></textarea>
                </div>
            `,
            confirmText: 'Cancel Allocation',
            confirmClass: 'btn-danger',
            onConfirm: async () => {
                const reason = (document.getElementById('modalCancelReasonInput').value || 'Cancelled by manager').trim();
                try {
                    const res = await fetch(`/api/allocations/${allocationId}/cancel`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ reason })
                    });
                    const json = await res.json();
                    if (res.ok) {
                        showToast(`Allocation #${allocationId} cancelled and capacity restored.`, 'success');
                        closeModal();
                        loadAllocations();
                    } else {
                        showToast(`Cancellation failed: ${json.message || res.statusText}`, 'error');
                    }
                } catch (err) {
                    showToast(`Error cancelling allocation: ${err.message}`, 'error');
                }
            }
        });
    }

    function openNewAllocationModal() {
        showModal({
            title: 'Create New Resource Allocation',
            bodyHtml: `
                <div class="form-group" style="margin-bottom:0.75rem;">
                    <label for="modalNewDemandId">Target Demand <span class="required">*</span></label>
                    <select id="modalNewDemandId">
                        <option value="">-- Choose Demand --</option>
                        ${state.demands.map(d => `<option value="${d.id}">${escapeHtml(d.role)} (${d.demandCode})</option>`).join('')}
                    </select>
                </div>
                <div class="form-group" style="margin-bottom:0.75rem;">
                    <label for="modalNewEmployeeId">Employee <span class="required">*</span></label>
                    <select id="modalNewEmployeeId">
                        <option value="">-- Choose Employee --</option>
                        ${state.employees.map(e => `<option value="${e.id}">${escapeHtml(e.name)} (${e.employeeCode}) - ${e.department}</option>`).join('')}
                    </select>
                </div>
                <div class="form-row form-row-2">
                    <div class="form-group">
                        <label for="modalNewStartDate">Start Date <span class="required">*</span></label>
                        <input type="date" id="modalNewStartDate" value="2026-12-01">
                    </div>
                    <div class="form-group">
                        <label for="modalNewEndDate">End Date <span class="required">*</span></label>
                        <input type="date" id="modalNewEndDate" value="2026-12-31">
                    </div>
                </div>
                <div class="form-row form-row-2">
                    <div class="form-group">
                        <label for="modalNewPercent">Allocation % <span class="required">*</span></label>
                        <input type="number" id="modalNewPercent" min="1" max="100" value="60">
                    </div>
                    <div class="form-group">
                        <label for="modalNewRequestedBy">Requested By</label>
                        <input type="text" id="modalNewRequestedBy" value="Staffing Lead">
                    </div>
                </div>
            `,
            confirmText: 'Create Allocation Request',
            confirmClass: 'btn-primary',
            onConfirm: async () => {
                const demandId = document.getElementById('modalNewDemandId').value;
                const employeeId = document.getElementById('modalNewEmployeeId').value;
                const startDate = document.getElementById('modalNewStartDate').value;
                const endDate = document.getElementById('modalNewEndDate').value;
                const allocationPercent = parseInt(document.getElementById('modalNewPercent').value, 10);
                const requestedBy = (document.getElementById('modalNewRequestedBy').value || 'Staffing Lead').trim();

                if (!demandId || !employeeId || !startDate || !endDate || !allocationPercent) {
                    showToast('Please fill all required fields', 'error');
                    return;
                }

                try {
                    const res = await fetch('/api/allocations', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            demandId: Number(demandId),
                            employeeId: Number(employeeId),
                            startDate,
                            endDate,
                            allocationPercent,
                            requestedBy
                        })
                    });
                    const json = await res.json();
                    if (res.ok) {
                        showToast('Allocation request created successfully!', 'success');
                        closeModal();
                        loadAllocations();
                    } else {
                        showToast(`Creation failed: ${json.message || res.statusText}`, 'error');
                    }
                } catch (err) {
                    showToast(`Error creating allocation: ${err.message}`, 'error');
                }
            }
        });
    }

    function openCreateAllocationForCandidate(demandId, employeeId, employeeName) {
        const demand = state.demands.find(d => d.id == demandId);
        showModal({
            title: `Allocate Candidate: ${escapeHtml(employeeName)}`,
            bodyHtml: `
                <p>Create a pending allocation request for <strong>${escapeHtml(employeeName)}</strong> onto demand <strong>${escapeHtml(demand ? demand.role : demandId)}</strong>.</p>
                <div class="form-row form-row-2" style="margin-top:1rem;">
                    <div class="form-group">
                        <label>Start Date</label>
                        <input type="date" id="modalCandAllocStart" value="${demand ? demand.startDate : ''}">
                    </div>
                    <div class="form-group">
                        <label>End Date</label>
                        <input type="date" id="modalCandAllocEnd" value="${demand ? demand.endDate : ''}">
                    </div>
                </div>
                <div class="form-row form-row-2">
                    <div class="form-group">
                        <label>Allocation %</label>
                        <input type="number" id="modalCandAllocPercent" value="${demand ? demand.allocationPercent : 60}" min="1" max="100">
                    </div>
                    <div class="form-group">
                        <label>Requested By</label>
                        <input type="text" id="modalCandAllocReqBy" value="Staffing Lead">
                    </div>
                </div>
            `,
            confirmText: 'Submit Allocation',
            confirmClass: 'btn-primary',
            onConfirm: async () => {
                const startDate = document.getElementById('modalCandAllocStart').value;
                const endDate = document.getElementById('modalCandAllocEnd').value;
                const allocationPercent = parseInt(document.getElementById('modalCandAllocPercent').value, 10);
                const requestedBy = document.getElementById('modalCandAllocReqBy').value;

                try {
                    const res = await fetch('/api/allocations', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            demandId,
                            employeeId,
                            startDate,
                            endDate,
                            allocationPercent,
                            requestedBy
                        })
                    });
                    const json = await res.json();
                    if (res.ok) {
                        showToast(`Allocation created for ${employeeName}!`, 'success');
                        closeModal();
                        switchScreen('allocation-approvals');
                    } else {
                        showToast(`Failed: ${json.message || res.statusText}`, 'error');
                    }
                } catch (err) {
                    showToast(`Error: ${err.message}`, 'error');
                }
            }
        });
    }

    // ==========================================================
    // SCREEN 6: UTILIZATION AND SKILL-GAP DASHBOARD
    // ==========================================================
    async function loadUtilizationDashboard() {
        utilizationListContainer.innerHTML = '<div class="loading-state"><div class="spinner"></div><p>Calculating date-bounded capacity utilization via analytics report...</p></div>';
        skillGapsListContainer.innerHTML = '<div class="loading-state"><div class="spinner"></div><p>Analyzing skill shortages via analytics report...</p></div>';

        // 1. Initialize Date Range Filters if empty
        const utilStartDateInput = document.getElementById('utilFilterStartDate');
        const utilEndDateInput = document.getElementById('utilFilterEndDate');
        const utilDeptInput = document.getElementById('utilFilterDept');
        const utilWindowLabel = document.getElementById('utilWindowLabel');

        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');

        if (utilStartDateInput && !utilStartDateInput.value) {
            utilStartDateInput.value = `${year}-${month}-01`;
        }
        if (utilEndDateInput && !utilEndDateInput.value) {
            const lastDay = new Date(year, now.getMonth() + 1, 0).getDate();
            utilEndDateInput.value = `${year}-${month}-${String(lastDay).padStart(2, '0')}`;
        }

        const startDate = utilStartDateInput ? utilStartDateInput.value : `${year}-${month}-01`;
        const endDate = utilEndDateInput ? utilEndDateInput.value : `${year}-${month}-28`;
        const selectedDept = utilDeptInput ? utilDeptInput.value : '';

        try {
            // Fetch analytics reports in parallel
            const utilQuery = `/api/analytics/reports/utilization-by-employee-department?startDate=${startDate}&endDate=${endDate}${selectedDept ? `&department=${encodeURIComponent(selectedDept)}` : ''}`;
            const [utilRes, openDemRes, gapsRes, allocRes, empRes] = await Promise.all([
                fetch(utilQuery),
                fetch('/api/analytics/reports/open-demands-by-role-priority'),
                fetch('/api/analytics/reports/skill-gaps-by-category'),
                fetch('/api/allocations'),
                fetch('/api/employees')
            ]);

            const utilJson = await utilRes.json();
            const openDemJson = await openDemRes.json();
            const gapsJson = await gapsRes.json();
            const allocJson = await allocRes.json();
            const empJson = await empRes.json();

            if (!utilJson.success) {
                throw new Error(utilJson.message || 'Failed to calculate utilization');
            }

            const utilData = utilJson.data || {};
            const openDemData = openDemJson.data || {};
            const gapsData = gapsJson.data || {};
            const allAllocations = allocJson.data || [];
            const allEmployees = empJson.data || [];

            // Populate departments dropdown if not populated
            if (utilDeptInput && utilDeptInput.options.length <= 1) {
                const depts = [...new Set(allEmployees.map(e => e.department).filter(Boolean))].sort();
                depts.forEach(d => {
                    const opt = document.createElement('option');
                    opt.value = d;
                    opt.textContent = d;
                    utilDeptInput.appendChild(opt);
                });
            }

            // Update evaluation window label
            if (utilWindowLabel && utilData.windowDays) {
                utilWindowLabel.textContent = `Evaluation Window: ${utilData.windowDays} days (${startDate} to ${endDate})`;
            }

            // 1. Update KPI Metric Cards
            const confirmedAllocsInWindow = allAllocations.filter(a => a.status === 'CONFIRMED' && a.startDate <= endDate && a.endDate >= startDate);
            const pendingAllocs = allAllocations.filter(a => a.status === 'PENDING');

            kpiActiveEmployees.textContent = utilData.totalEmployees || 0;
            kpiTotalEmployees.textContent = `${allEmployees.length} total headcount (${utilData.totalEmployees || 0} active)`;

            kpiOpenDemands.textContent = openDemData.totalOpenDemands || 0;
            kpiDemandedCapacity.textContent = `${openDemData.totalRequestedCapacityPercent || 0}% total requested capacity`;

            kpiConfirmedAllocations.textContent = confirmedAllocsInWindow.length;
            kpiPendingAllocations.textContent = `${pendingAllocs.length} pending review`;

            kpiAvgUtilization.textContent = `${(utilData.averagePeakUtilizationPercent || 0).toFixed(1)}% Peak`;

            // 2. Render Employee Capacity Utilization List
            const employeeDetails = utilData.employeeDetails || [];
            if (employeeDetails.length === 0) {
                utilizationListContainer.innerHTML = '<div class="empty-state"><p>No active employees found matching the criteria.</p></div>';
            } else {
                utilizationListContainer.innerHTML = employeeDetails.map(emp => {
                    const peak = emp.peakSimultaneousAllocationPercent || 0;
                    const peakUtil = emp.peakUtilizationPercent || 0;
                    const timeWeighted = emp.timeWeightedAverageUtilizationPercent || 0;
                    const maxCap = emp.maxCapacityPercent || 100;
                    const allocCount = emp.aggregateConfirmedAllocationsCount || 0;

                    const fillClass = peakUtil > 100 ? 'danger' : peakUtil >= 80 ? 'committed' : 'normal';

                    return `
                        <div style="background:rgba(30,41,59,0.5); border:1px solid var(--border-color); border-radius:var(--radius-sm); padding:0.85rem; margin-bottom:0.75rem;">
                            <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:0.5rem;">
                                <div>
                                    <strong style="color:var(--text-primary); font-size:0.9rem;">${escapeHtml(emp.employeeName)}</strong>
                                    <span style="font-size:0.75rem; color:var(--text-secondary); margin-left:0.4rem;">(${escapeHtml(emp.employeeCode)} • ${escapeHtml(emp.department)})</span>
                                </div>
                                <div style="display:flex; gap:0.4rem; align-items:center; flex-wrap:wrap;">
                                    <span class="badge ${fillClass === 'danger' ? 'badge-danger' : fillClass === 'committed' ? 'badge-warning' : 'badge-tool'}" title="Maximum simultaneous commitment on any single day within window">
                                        🎯 ${peak}% Peak (${peakUtil}% of cap)
                                    </span>
                                    <span class="badge badge-secondary" title="Day-weighted average allocation across entire window">
                                        ⏱️ ${timeWeighted}% Avg
                                    </span>
                                    <span class="badge badge-grounded">
                                        ${allocCount} confirmed
                                    </span>
                                </div>
                            </div>
                            <div class="progress-bar-container" style="margin-top:0.6rem;">
                                <div class="progress-bar-fill ${fillClass}" style="width: ${Math.min(100, peakUtil)}%;"></div>
                            </div>
                            <div style="display:flex; justify-content:space-between; font-size:0.75rem; color:var(--text-muted); margin-top:0.3rem;">
                                <span>Configured Max: ${maxCap}%</span>
                                <span>${emp.hasConfirmedAllocations ? `${allocCount} active allocation(s) in window` : '0 allocations in window'}</span>
                            </div>
                        </div>
                    `;
                }).join('');
            }

            // 3. Render Enterprise Skill Shortage Analysis
            const categoryGaps = gapsData.categoryGaps || [];
            const allSkillRows = [];
            categoryGaps.forEach(cg => {
                (cg.skillGaps || []).forEach(sg => {
                    allSkillRows.push({
                        ...sg,
                        category: cg.category
                    });
                });
            });

            allSkillRows.sort((a, b) => b.deficit - a.deficit || b.demandCount - a.demandCount);

            if (allSkillRows.length === 0) {
                skillGapsListContainer.innerHTML = '<div class="empty-state"><p>No skill shortages detected across current demands.</p></div>';
            } else {
                skillGapsListContainer.innerHTML = `
                    <div style="margin-bottom:0.75rem; display:flex; justify-content:space-between; align-items:center; font-size:0.8rem; color:var(--text-secondary);">
                        <span>Categories with Deficit: <strong>${gapsData.categoriesWithGapsCount || 0}</strong> / ${gapsData.totalCategories || 0}</span>
                        <span>Total Unmet Demand Slots: <strong>${gapsData.totalUnmetSkillDemandSlots || 0}</strong></span>
                    </div>
                    <table class="standard-table">
                        <thead>
                            <tr>
                                <th>Demanded Skill</th>
                                <th>Category</th>
                                <th>Demands</th>
                                <th>Qualified Supply</th>
                                <th>Deficit Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${allSkillRows.map(row => {
                                const isCritical = row.deficit > 0 && row.mandatoryDemandCount > 0;
                                const isModerate = row.deficit > 0 && !isCritical;
                                const statusBadge = isCritical ? `<span class="badge badge-danger">🚨 DEFICIT: -${row.deficit}</span>` :
                                                    isModerate ? `<span class="badge badge-warning">⚠️ DEFICIT: -${row.deficit}</span>` :
                                                    '<span class="badge badge-tool">✅ ADEQUATE</span>';
                                return `
                                    <tr>
                                        <td>
                                            <strong>${escapeHtml(row.skillName)}</strong>
                                            <div style="font-size:0.7rem; color:var(--text-muted);">Min: ${row.minimumProficiency} • ${row.mandatoryDemandCount} mandatory</div>
                                        </td>
                                        <td><span class="badge badge-secondary">${escapeHtml(row.category)}</span></td>
                                        <td>${row.demandCount} demand${row.demandCount === 1 ? '' : 's'}</td>
                                        <td><strong>${row.qualifiedResourceCount}</strong> qualified</td>
                                        <td>${statusBadge}</td>
                                    </tr>
                                `;
                            }).join('')}
                        </tbody>
                    </table>
                `;
            }
        } catch (e) {
            utilizationListContainer.innerHTML = `<div class="error-banner">Error calculating utilization: ${escapeHtml(e.message)}</div>`;
            skillGapsListContainer.innerHTML = `<div class="error-banner">Error analyzing skill gaps: ${escapeHtml(e.message)}</div>`;
        }
    }

    // ==========================================================
    // SCREEN 7: AI ASSISTANT (OLLAMA LLAMA3.2:3B)
    // ==========================================================
    async function checkOllamaStatus() {
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

    function updateAiContextPrompts() {
        const demand = state.demands.find(d => d.id == state.selectedDemandId);
        const demandCode = demand ? demand.demandCode : (state.demands.length > 0 ? state.demands[0].demandCode : 'DEM-001');
        const demandId = demand ? demand.id : (state.demands.length > 0 ? state.demands[0].id : '50');

        quickPromptsBar.innerHTML = `
            <button type="button" class="prompt-chip" data-prompt="Find eligible candidates for demand ${demandId}">Find candidates for ${demandCode}</button>
            <button type="button" class="prompt-chip" data-prompt="Compare the top three candidates for demand ${demandId}">Compare top candidates</button>
            <button type="button" class="prompt-chip" data-prompt="Which mandatory skills are missing for demand ${demandId}?">Missing mandatory skills</button>
            <button type="button" class="prompt-chip" data-prompt="Show the current allocation status for demand ${demandId}">Allocation status</button>
            <button type="button" class="prompt-chip" data-prompt="Check whether Devon Vance is available during November 2026">Check Devon Vance availability</button>
            <button type="button" class="prompt-chip" data-prompt="Why is Marcus Brody not eligible for demand ${demandId}?">Explain Marcus Brody</button>
        `;

        quickPromptsBar.querySelectorAll('.prompt-chip').forEach(btn => {
            btn.addEventListener('click', () => {
                chatInput.value = btn.getAttribute('data-prompt');
                handleAiSend();
            });
        });

        if (demand) {
            aiContextDetail.innerHTML = `
                <div style="font-size:0.85rem; color:var(--text-primary); font-weight:600;">Bound Demand: ${escapeHtml(demand.role)}</div>
                <div style="font-size:0.75rem; color:var(--text-secondary); margin-top:0.25rem;">
                    Code: <code>${escapeHtml(demand.demandCode)}</code> • Dept: ${escapeHtml(demand.department)} • Dates: ${demand.startDate} to ${demand.endDate}
                </div>
            `;
        }
    }

    async function handleAiSend() {
        const message = chatInput.value.trim();
        if (!message || state.isAiSending) return;

        appendChatMessage('user', message);
        chatInput.value = '';
        chatInput.style.height = 'auto';

        state.isAiSending = true;
        sendBtn.disabled = true;
        const loadingId = 'loading-' + Date.now();
        appendChatLoading(loadingId);

        try {
            const res = await fetch('/api/assistant/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message })
            });

            removeChatLoading(loadingId);

            if (res.ok) {
                const json = await res.json();
                appendChatAssistantMessage(json.data);
            } else {
                const errJson = await res.json().catch(() => ({}));
                const errMsg = errJson.message || `HTTP ${res.status}: ${res.statusText}`;
                appendChatErrorMessage(errMsg);
            }
        } catch (e) {
            removeChatLoading(loadingId);
            appendChatErrorMessage(`Network error: ${e.message}. Ensure backend and local Ollama are running.`);
        } finally {
            state.isAiSending = false;
            sendBtn.disabled = false;
            chatInput.focus();
        }
    }

    function appendChatMessage(role, text) {
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

    function appendChatAssistantMessage(data) {
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

    function appendChatErrorMessage(errorText) {
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

    function appendChatLoading(id) {
        const bubble = document.createElement('div');
        bubble.id = id;
        bubble.className = 'message-bubble assistant';

        const avatar = document.createElement('div');
        avatar.className = 'avatar ai-avatar';
        avatar.innerHTML = '🦙';

        const content = document.createElement('div');
        content.className = 'message-content';
        content.innerHTML = '<div style="display:flex; align-items:center; gap:0.5rem;"><div class="spinner" style="width:18px;height:18px;margin:0;"></div><span>Reasoning & executing tools with llama3.2:3b...</span></div>';

        bubble.appendChild(avatar);
        bubble.appendChild(content);
        chatMessages.appendChild(bubble);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function removeChatLoading(id) {
        const el = document.getElementById(id);
        if (el) el.remove();
    }

    // ==========================================================
    // MODAL & TOAST UTILITIES
    // ==========================================================
    let modalConfirmHandler = null;

    function showModal({ title, bodyHtml, confirmText, confirmClass, onConfirm }) {
        modalTitle.textContent = title;
        modalBody.innerHTML = bodyHtml;
        modalConfirmBtn.textContent = confirmText || 'Proceed';
        modalConfirmBtn.className = `btn ${confirmClass || 'btn-primary'}`;

        if (modalConfirmHandler) {
            modalConfirmBtn.removeEventListener('click', modalConfirmHandler);
        }
        modalConfirmHandler = onConfirm;
        modalConfirmBtn.addEventListener('click', modalConfirmHandler);

        actionModal.style.display = 'flex';
    }

    function closeModal() {
        actionModal.style.display = 'none';
        if (modalConfirmHandler) {
            modalConfirmBtn.removeEventListener('click', modalConfirmHandler);
            modalConfirmHandler = null;
        }
    }

    function showToast(message, type = 'success') {
        toast.className = `toast-notification ${type}`;
        toast.textContent = message;
        toast.style.display = 'block';

        setTimeout(() => {
            toast.style.display = 'none';
        }, 4000);
    }

    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function formatDate(dateStr) {
        if (!dateStr) return '';
        try {
            const d = new Date(dateStr);
            return d.toLocaleDateString();
        } catch {
            return dateStr;
        }
    }

    // ==========================================================
    // EXPOSE GLOBAL API BINDINGS FOR BUTTON ACTIONS
    // ==========================================================
    window.marketplace = {
        selectDemandForRanking: (demandId) => {
            state.selectedDemandId = demandId;
            switchScreen('resource-ranking');
        },
        askAiAboutDemand: (demandId) => {
            state.selectedDemandId = demandId;
            switchScreen('ai-assistant');
            chatInput.value = `Find eligible candidates for demand ${demandId}`;
            handleAiSend();
        },
        askAiAboutEmployee: (empName) => {
            switchScreen('ai-assistant');
            chatInput.value = `Check whether ${empName} is available during November 2026`;
            handleAiSend();
        },
        askAiAboutCandidateDemand: (demandId, candName) => {
            state.selectedDemandId = demandId;
            switchScreen('ai-assistant');
            chatInput.value = `Why is ${candName} eligible or ineligible for demand ${demandId}?`;
            handleAiSend();
        },
        openCreateAllocationForCandidate,
        confirmAllocationDialog,
        rejectAllocationDialog,
        cancelAllocationDialog
    };

    // Run Initialization
    init();
});
