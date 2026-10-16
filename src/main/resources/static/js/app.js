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
        currentUser: null,
        activeScreen: 'dashboard',
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
    const userProfileBadge = document.getElementById('userProfileBadge');
    const userAvatarText = document.getElementById('userAvatarText');
    const userDisplayName = document.getElementById('userDisplayName');
    const userRoleBadge = document.getElementById('userRoleBadge');
    const btnLogout = document.getElementById('btnLogout');
    const navTabUtilization = document.getElementById('navTabUtilizationDashboard');
    const navTabAllocations = document.getElementById('navTabAllocationApprovals');
    const headingAllocationApprovals = document.getElementById('headingAllocationApprovals');

    const navTabs = document.querySelectorAll('.nav-tab');
    const appScreens = document.querySelectorAll('.app-screen');
    const statusDot = document.getElementById('statusDot');
    const statusText = document.getElementById('statusText');
    const mobileStatusDot = document.getElementById('mobileStatusDot');
    const sidebar = document.getElementById('appSidebar');
    const sidebarToggleBtn = document.getElementById('sidebarToggleBtn');
    const sidebarCloseBtn = document.getElementById('sidebarCloseBtn');
    const sidebarBackdrop = document.getElementById('sidebarBackdrop');
    const btnRefreshDashboardSummary = document.getElementById('btnRefreshDashboardSummary');
    const tabBtnDashDemands = document.getElementById('tabBtnDashDemands');
    const tabBtnDashEmployees = document.getElementById('tabBtnDashEmployees');
    const dashDemandsPane = document.getElementById('dashDemandsPane');
    const dashEmployeesPane = document.getElementById('dashEmployeesPane');
    const btnDashViewAllAction = document.getElementById('btnDashViewAllAction');
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
    const openDemandsCountTab = document.getElementById('openDemandsCountTab');
    const btnRefreshDemands = document.getElementById('btnRefreshDemands');
    const btnRefreshDemandsList = document.getElementById('btnRefreshDemandsList');
    const tabBtnCreateDemand = document.getElementById('tabBtnCreateDemand');
    const tabBtnOpenDemands = document.getElementById('tabBtnOpenDemands');
    const paneCreateDemand = document.getElementById('paneCreateDemand');
    const paneOpenDemands = document.getElementById('paneOpenDemands');
    let requiredSkillsBuilderList = [];

    // Edit Demand Modal Elements
    const editDemandModal = document.getElementById('editDemandModal');
    const editDemandModalCloseBtn = document.getElementById('editDemandModalCloseBtn');
    const editDemandModalCancelBtn = document.getElementById('editDemandModalCancelBtn');
    const btnSaveEditedDemand = document.getElementById('btnSaveEditedDemand');
    const editDemandCodeBadge = document.getElementById('editDemandCodeBadge');
    const editDemandId = document.getElementById('editDemandId');
    const editProjectName = document.getElementById('editProjectName');
    const editDemandRole = document.getElementById('editDemandRole');
    const editDemandDepartment = document.getElementById('editDemandDepartment');
    const editDemandLocation = document.getElementById('editDemandLocation');
    const editDemandPriority = document.getElementById('editDemandPriority');
    const editDemandStatus = document.getElementById('editDemandStatus');
    const editDemandStartDate = document.getElementById('editDemandStartDate');
    const editDemandEndDate = document.getElementById('editDemandEndDate');
    const editDemandAllocationPercent = document.getElementById('editDemandAllocationPercent');
    const editDemandExperience = document.getElementById('editDemandExperience');
    const editSkillSelect = document.getElementById('editSkillSelect');
    const editSkillProficiency = document.getElementById('editSkillProficiency');
    const editSkillMandatory = document.getElementById('editSkillMandatory');
    const btnEditAddSkillReq = document.getElementById('btnEditAddSkillReq');
    const editRequiredSkillsChipsContainer = document.getElementById('editRequiredSkillsChipsContainer');
    const editEmptySkillsPrompt = document.getElementById('editEmptySkillsPrompt');
    const editDemandFormErrorBanner = document.getElementById('editDemandFormErrorBanner');
    const editDemandFormSuccessBanner = document.getElementById('editDemandFormSuccessBanner');
    let editRequiredSkillsList = [];

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
    const utilizationListContainer = document.getElementById('utilizationListContainer');
    const skillGapsListContainer = document.getElementById('skillGapsListContainer');
    const tabBtnEmpUtilization = document.getElementById('tabBtnEmpUtilization');
    const tabBtnSkillGaps = document.getElementById('tabBtnSkillGaps');
    const paneEmpUtilization = document.getElementById('paneEmpUtilization');
    const paneSkillGaps = document.getElementById('paneSkillGaps');
    const utilDateErrorBanner = document.getElementById('utilDateErrorBanner');

    // Screen 7: AI Assistant
    const chatMessages = document.getElementById('chatMessages');
    const chatInput = document.getElementById('chatInput');
    const sendBtn = document.getElementById('sendBtn');
    const chatForm = document.getElementById('chatForm');
    const quickPromptsBar = document.getElementById('quickPromptsBar');
    const aiContextDetail = document.getElementById('aiContextDetail');

    // ==========================================================
    // INITIALIZATION, AUTH & TAB SWITCHING
    // ==========================================================
    /**
     * Safely inspects response status, headers, and body.
     * Prevents "Unexpected end of JSON input" on empty bodies or HTML error pages.
     */
    async function safeParseResponse(res) {
        const contentType = res.headers.get('content-type') || '';
        const text = await res.text();

        let data = null;
        let isJson = false;
        if (text && text.trim().length > 0) {
            if (contentType.includes('application/json') || text.trim().startsWith('{') || text.trim().startsWith('[')) {
                try {
                    data = JSON.parse(text);
                    isJson = true;
                } catch (parseErr) {
                    console.warn('Response claimed to be JSON but failed to parse:', text.substring(0, 300), parseErr);
                }
            }
        }

        if (!res.ok) {
            let errMsg = '';
            if (isJson && data) {
                errMsg = data.message || data.error || (data.validationErrors ? Object.values(data.validationErrors).join(', ') : '');
            }
            if (!errMsg) {
                if (text && text.trim().length > 0 && !text.includes('<!DOCTYPE') && text.length < 200) {
                    errMsg = text.trim();
                } else if (res.status === 401) {
                    errMsg = 'Authentication required or session expired. Please sign in again.';
                } else if (res.status === 403) {
                    errMsg = 'Access denied: You do not have permission to perform this action.';
                } else if (res.status === 404) {
                    errMsg = 'The requested resource was not found (404).';
                } else if (res.status >= 500) {
                    errMsg = `Server error (${res.status}). Please try again or contact your administrator.`;
                } else {
                    errMsg = `Request failed with HTTP status ${res.status} (${res.statusText || 'Error'}).`;
                }
            }

            const error = new Error(errMsg);
            error.status = res.status;
            error.statusText = res.statusText;
            error.rawData = data;
            error.rawText = text;
            error.isAuthError = (res.status === 401);
            throw error;
        }

        if (!text || text.trim().length === 0) {
            return { success: true, data: null, empty: true };
        }

        if (!isJson) {
            throw new Error(`Expected JSON response but server returned ${contentType || 'non-JSON content'}: ${text.substring(0, 100)}`);
        }

        if (data && typeof data === 'object' && 'success' in data && data.success === false) {
            const error = new Error(data.message || 'Operation failed');
            error.status = res.status;
            error.rawData = data;
            throw error;
        }

        return data;
    }

    async function checkAuth() {
        try {
            const res = await fetch('/api/auth/me');
            if (res.ok) {
                const json = await res.json();
                state.currentUser = json.data;
                applyRoleBasedUI(state.currentUser);
                return true;
            } else {
                window.location.href = '/login';
                return false;
            }
        } catch (e) {
            window.location.href = '/login';
            return false;
        }
    }

    function applyRoleBasedUI(user) {
        if (!user) return;
        if (userDisplayName) userDisplayName.textContent = user.name || 'User';
        if (userRoleBadge) userRoleBadge.textContent = user.role;
        if (userAvatarText) {
            userAvatarText.textContent = user.role === 'PROJECT_MANAGER' ? 'PM' : 'RM';
        }

        if (user.role === 'PROJECT_MANAGER') {
            if (navTabUtilization) navTabUtilization.style.display = 'none';
            if (navTabAllocations) {
                const label = navTabAllocations.querySelector('.tab-label');
                if (label) label.textContent = 'My Allocation Requests';
            }
            if (headingAllocationApprovals) {
                headingAllocationApprovals.textContent = 'My Allocation Requests';
            }
        } else {
            if (navTabUtilization) navTabUtilization.style.display = 'flex';
            if (navTabAllocations) {
                const label = navTabAllocations.querySelector('.tab-label');
                if (label) label.textContent = 'Allocation Approvals';
            }
            if (headingAllocationApprovals) {
                headingAllocationApprovals.textContent = 'Resource Allocation Management & Approvals';
            }
        }
    }

    async function init() {
        const isAuthed = await checkAuth();
        if (!isAuthed) return;
        setupNavigation();
        setupEventListeners();
        checkOllamaStatus();
        await loadInitialData();
        loadNextDemandCodePreview();
        renderScreen(state.activeScreen || 'dashboard');
    }

    function openMobileSidebar() {
        if (sidebar) sidebar.classList.add('open');
        if (sidebarBackdrop) sidebarBackdrop.classList.add('active');
        document.body.style.overflow = 'hidden';
    }

    function closeMobileSidebar() {
        if (sidebar) sidebar.classList.remove('open');
        if (sidebarBackdrop) sidebarBackdrop.classList.remove('active');
        document.body.style.overflow = '';
    }

    function setupNavigation() {
        const allNavTabs = document.querySelectorAll('.nav-tab');
        allNavTabs.forEach(tab => {
            tab.addEventListener('click', () => {
                const targetScreen = tab.getAttribute('data-screen');
                switchScreen(targetScreen);
                closeMobileSidebar();
            });
        });

        if (sidebarToggleBtn) {
            sidebarToggleBtn.addEventListener('click', openMobileSidebar);
        }
        if (sidebarCloseBtn) {
            sidebarCloseBtn.addEventListener('click', closeMobileSidebar);
        }
        if (sidebarBackdrop) {
            sidebarBackdrop.addEventListener('click', closeMobileSidebar);
        }

        window.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && sidebar && sidebar.classList.contains('open')) {
                closeMobileSidebar();
            }
        });
    }

    function switchScreen(screenId) {
        if (screenId === 'utilization-dashboard' && state.currentUser && state.currentUser.role === 'PROJECT_MANAGER') {
            showToast('Access restricted: Project Managers cannot access enterprise utilization & skill gap analytics.', 'error');
            return;
        }

        state.activeScreen = screenId;

        const allNavTabs = document.querySelectorAll('.nav-tab');
        const allAppScreens = document.querySelectorAll('.app-screen');

        allNavTabs.forEach(tab => {
            if (tab.getAttribute('data-screen') === screenId) {
                tab.classList.add('active');
            } else {
                tab.classList.remove('active');
            }
        });

        allAppScreens.forEach(screen => {
            if (screen.id === `screen-${screenId}`) {
                screen.classList.add('active');
            } else {
                screen.classList.remove('active');
            }
        });

        renderScreen(screenId);
    }

    window.switchScreenTo = function(screenId) {
        switchScreen(screenId);
    };

    function renderScreen(screenId) {
        if (screenId === 'dashboard') {
            loadDashboardSummary();
        } else if (screenId === 'demand-intake') {
            loadOpenDemandsList();
            loadNextDemandCodePreview();
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

    async function loadDashboardSummary() {
        const dashEmployeesEl = document.getElementById('dashKpiEmployees');
        const dashEmployeesSubEl = document.getElementById('dashKpiEmployeesSub');
        const dashDemandsEl = document.getElementById('dashKpiDemands');
        const dashDemandsSubEl = document.getElementById('dashKpiDemandsSub');
        const dashAllocationsEl = document.getElementById('dashKpiAllocations');
        const dashAllocationsSubEl = document.getElementById('dashKpiAllocationsSub');
        const dashDemandsPreview = document.getElementById('dashDemandsPreview');
        const dashEmployeesPreview = document.getElementById('dashEmployeesPreview');

        // Refresh counts if data is loaded
        if (state.employees && state.employees.length > 0 && dashEmployeesEl) {
            const activeEmps = state.employees.filter(e => (e.employmentStatus || e.status || '').toUpperCase() === 'ACTIVE');
            dashEmployeesEl.textContent = activeEmps.length;
            if (dashEmployeesSubEl) {
                const depts = new Set(state.employees.map(e => e.department).filter(Boolean));
                dashEmployeesSubEl.textContent = `${state.employees.length} total employees · ${depts.size} departments`;
            }
        }
        if (state.demands && state.demands.length > 0 && dashDemandsEl) {
            const openDemandsCount = state.demands.filter(d => (d.status || '').toUpperCase() === 'OPEN').length;
            dashDemandsEl.textContent = openDemandsCount;
            if (dashDemandsSubEl) {
                dashDemandsSubEl.textContent = `${state.demands.length} total demands · ${openDemandsCount} pending allocation`;
            }
        }

        // Fetch allocations count if needed
        try {
            const allocRes = await fetch('/api/allocations');
            if (allocRes.ok) {
                const allocJson = await allocRes.json();
                state.allocations = allocJson.data || [];
                if (dashAllocationsEl) {
                    const confirmedCount = state.allocations.filter(a => a.status === 'CONFIRMED').length;
                    const pendingCount = state.allocations.filter(a => a.status === 'PENDING').length;
                    dashAllocationsEl.textContent = confirmedCount + pendingCount;
                    if (dashAllocationsSubEl) {
                        dashAllocationsSubEl.textContent = `${confirmedCount} confirmed, ${pendingCount} pending approval`;
                    }
                }
            }
        } catch (e) {
            console.warn('Dashboard allocations summary fetch error:', e);
        }

        // Populate full-width Project Demands table
        if (dashDemandsPreview) {
            if (!state.demands || state.demands.length === 0) {
                dashDemandsPreview.innerHTML = '<div class="empty-state"><p>No project demands found in the system.</p></div>';
            } else {
                const demandsList = state.demands;
                dashDemandsPreview.innerHTML = `
                    <table class="standard-table">
                        <thead>
                            <tr>
                                <th>Demand Code &amp; Project</th>
                                <th>Role &amp; Department</th>
                                <th>Evaluation Window</th>
                                <th>Allocation Req.</th>
                                <th>Priority</th>
                                <th>Status</th>
                                <th style="text-align: right;">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${demandsList.map(d => {
                                const allocPct = d.allocationPercent != null ? d.allocationPercent : (d.allocationPercentage != null ? d.allocationPercentage : 100);
                                const priorityClass = d.priority === 'CRITICAL' ? 'badge-danger' : d.priority === 'HIGH' ? 'badge-warning' : 'badge-tool';
                                const statusClass = (d.status || '').toUpperCase() === 'OPEN' ? 'badge-grounded' : 'badge-secondary';
                                return `
                                    <tr>
                                        <td>
                                            <strong style="color:var(--text-primary); font-size:0.9rem;">${escapeHtml(d.demandCode)}</strong>
                                            <div style="font-size:0.75rem; color:var(--text-muted); margin-top:0.15rem;">${escapeHtml(d.projectName || '')}</div>
                                        </td>
                                        <td>
                                            <span style="font-weight:600; color:var(--text-primary);">${escapeHtml(d.role || '')}</span>
                                            <div style="font-size:0.72rem; color:var(--text-secondary); margin-top:0.15rem;">${escapeHtml(d.department || '')} &bull; ${escapeHtml(d.location || '')}</div>
                                        </td>
                                        <td style="font-size:0.825rem; white-space:nowrap;">
                                            ${formatDate(d.startDate)} &ndash; ${formatDate(d.endDate)}
                                        </td>
                                        <td>
                                            <span class="badge badge-tool" style="font-weight:700;">${allocPct}%</span>
                                        </td>
                                        <td>
                                            <span class="badge ${priorityClass}">${escapeHtml(d.priority || 'MEDIUM')}</span>
                                        </td>
                                        <td>
                                            <span class="badge ${statusClass}">${escapeHtml(d.status || 'OPEN')}</span>
                                        </td>
                                        <td style="text-align: right; white-space: nowrap;">
                                            <button type="button" class="btn btn-primary btn-sm" onclick="window.marketplace.selectDemandForRanking(${d.id})" style="margin-right: 0.35rem;">
                                                Rank Candidates
                                            </button>
                                            <button type="button" class="btn btn-secondary btn-sm" onclick="window.marketplace.askAiAboutDemand(${d.id})">
                                                Ask AI
                                            </button>
                                        </td>
                                    </tr>
                                `;
                            }).join('')}
                        </tbody>
                    </table>
                `;
            }
        }

        // Populate full-width Employee Roster table
        if (dashEmployeesPreview) {
            if (!state.employees || state.employees.length === 0) {
                dashEmployeesPreview.innerHTML = '<div class="empty-state"><p>No talent records loaded in the system.</p></div>';
            } else {
                const emps = state.employees;
                dashEmployeesPreview.innerHTML = `
                    <table class="standard-table">
                        <thead>
                            <tr>
                                <th>Employee Name &amp; Code</th>
                                <th>Designation &amp; Department</th>
                                <th>Location</th>
                                <th>Max Capacity</th>
                                <th>Employment Status</th>
                                <th style="text-align: right;">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${emps.map(emp => {
                                const empName = emp.name || [emp.firstName, emp.lastName].filter(Boolean).join(' ') || emp.fullName || ('Employee #' + emp.id);
                                const maxCap = emp.maxCapacityPercent != null ? emp.maxCapacityPercent : (emp.maxCapacityPercentage != null ? emp.maxCapacityPercentage : 100);
                                const empStatus = (emp.employmentStatus || emp.status || 'ACTIVE').toUpperCase();
                                const statusBadge = empStatus === 'ACTIVE' ? '<span class="badge badge-grounded">ACTIVE</span>' :
                                                    empStatus === 'ON_LEAVE' ? '<span class="badge badge-warning">ON_LEAVE</span>' :
                                                    '<span class="badge badge-danger">' + escapeHtml(empStatus) + '</span>';
                                return `
                                    <tr>
                                        <td>
                                            <strong style="color:var(--text-primary); font-size:0.9rem;">${escapeHtml(empName)}</strong>
                                            <div style="font-size:0.72rem; color:var(--text-muted); margin-top:0.15rem;">${escapeHtml(emp.employeeCode || '')}</div>
                                        </td>
                                        <td>
                                            <span style="font-weight:600; color:var(--text-primary);">${escapeHtml(emp.designation || 'Engineer')}</span>
                                            <div style="font-size:0.72rem; color:var(--text-secondary); margin-top:0.15rem;">${escapeHtml(emp.department || '')}</div>
                                        </td>
                                        <td>${escapeHtml(emp.location || 'Remote')}</td>
                                        <td><span class="badge badge-tool" style="font-weight:700;">${maxCap}%</span></td>
                                        <td>${statusBadge}</td>
                                        <td style="text-align: right; white-space: nowrap;">
                                            <button type="button" class="btn btn-secondary btn-sm" onclick="window.marketplace.viewEmployeeAllocations(${emp.id}, '${escapeHtml(empName)}')" style="margin-right: 0.35rem;" title="View all historical and active allocations">
                                                Allocations
                                            </button>
                                            <button type="button" class="btn btn-secondary btn-sm" onclick="window.switchScreenTo('skills-matrix')" style="margin-right: 0.35rem;">
                                                View Skills
                                            </button>
                                            <button type="button" class="btn btn-secondary btn-sm" onclick="window.marketplace.askAiAboutEmployee('${escapeHtml(empName)}')">
                                                Availability
                                            </button>
                                        </td>
                                    </tr>
                                `;
                            }).join('')}
                        </tbody>
                    </table>
                `;
            }
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
        if (editSkillSelect) {
            editSkillSelect.innerHTML = '<option value="">-- Choose Skill --</option>';
        }
        state.skills.forEach(skill => {
            const opt = document.createElement('option');
            opt.value = skill.id;
            opt.textContent = `${skill.name} (${skill.category})`;
            skillSelect.appendChild(opt);

            if (editSkillSelect) {
                const editOpt = document.createElement('option');
                editOpt.value = skill.id;
                editOpt.textContent = `${skill.name} (${skill.category})`;
                editSkillSelect.appendChild(editOpt);
            }
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

    // Next Demand Code Preview (Shared Module Scope)
    async function loadNextDemandCodePreview() {
        if (!demandCodeInput) return;
        try {
            const res = await fetch('/api/demands/next-code');
            if (res.ok) {
                const json = await safeParseResponse(res);
                if (json && json.data && json.data.demandCode) {
                    demandCodeInput.value = json.data.demandCode;
                    demandCodeInput.placeholder = json.data.demandCode;
                    const hintEl = document.getElementById('demandCodeHint');
                    if (hintEl) {
                        hintEl.textContent = `Next sequence code: ${json.data.demandCode}. Final unique code assigned upon creation.`;
                    }
                }
            }
        } catch (err) {
            console.warn('Could not load next demand code preview:', err);
        }
    }
    window.loadNextDemandCodePreview = loadNextDemandCodePreview;

    function setupEventListeners() {
        // Sign Out Button
        if (btnLogout) {
            btnLogout.addEventListener('click', async () => {
                try {
                    await fetch('/api/auth/logout', { method: 'POST' });
                } catch (e) {
                    console.warn('Logout error:', e);
                } finally {
                    window.location.href = '/login';
                }
            });
        }

        // Refresh Dashboard Summary
        if (btnRefreshDashboardSummary) {
            btnRefreshDashboardSummary.addEventListener('click', async () => {
                await loadInitialData();
                loadDashboardSummary();
                showToast('Dashboard summary metrics refreshed', 'success');
            });
        }

        // Dashboard Workspace Overview Tabs
        if (tabBtnDashDemands && tabBtnDashEmployees) {
            tabBtnDashDemands.addEventListener('click', () => {
                tabBtnDashDemands.classList.add('active');
                tabBtnDashDemands.setAttribute('aria-selected', 'true');
                tabBtnDashEmployees.classList.remove('active');
                tabBtnDashEmployees.setAttribute('aria-selected', 'false');
                if (dashDemandsPane) dashDemandsPane.style.display = 'block';
                if (dashEmployeesPane) dashEmployeesPane.style.display = 'none';
                if (btnDashViewAllAction) {
                    btnDashViewAllAction.textContent = 'View Full Demands \u2192';
                    btnDashViewAllAction.setAttribute('onclick', "window.switchScreenTo('demand-intake')");
                }
            });

            tabBtnDashEmployees.addEventListener('click', () => {
                tabBtnDashEmployees.classList.add('active');
                tabBtnDashEmployees.setAttribute('aria-selected', 'true');
                tabBtnDashDemands.classList.remove('active');
                tabBtnDashDemands.setAttribute('aria-selected', 'false');
                if (dashEmployeesPane) dashEmployeesPane.style.display = 'block';
                if (dashDemandsPane) dashDemandsPane.style.display = 'none';
                if (btnDashViewAllAction) {
                    btnDashViewAllAction.textContent = 'View Skills Matrix \u2192';
                    btnDashViewAllAction.setAttribute('onclick', "window.switchScreenTo('skills-matrix')");
                }
            });
        }

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

        // Screen 1: Project Demands Horizontal Sub-Tabs
        function activateDemandSubTab(tabName) {
            if (tabName === 'open') {
                if (tabBtnOpenDemands) {
                    tabBtnOpenDemands.classList.add('active');
                    tabBtnOpenDemands.setAttribute('aria-selected', 'true');
                }
                if (tabBtnCreateDemand) {
                    tabBtnCreateDemand.classList.remove('active');
                    tabBtnCreateDemand.setAttribute('aria-selected', 'false');
                }
                if (paneOpenDemands) paneOpenDemands.style.display = 'block';
                if (paneCreateDemand) paneCreateDemand.style.display = 'none';
                loadOpenDemandsList();
            } else {
                if (tabBtnCreateDemand) {
                    tabBtnCreateDemand.classList.add('active');
                    tabBtnCreateDemand.setAttribute('aria-selected', 'true');
                }
                if (tabBtnOpenDemands) {
                    tabBtnOpenDemands.classList.remove('active');
                    tabBtnOpenDemands.setAttribute('aria-selected', 'false');
                }
                if (paneCreateDemand) paneCreateDemand.style.display = 'block';
                if (paneOpenDemands) paneOpenDemands.style.display = 'none';
                loadNextDemandCodePreview();
            }
        }

        if (tabBtnCreateDemand) {
            tabBtnCreateDemand.addEventListener('click', () => activateDemandSubTab('create'));
        }
        if (tabBtnOpenDemands) {
            tabBtnOpenDemands.addEventListener('click', () => activateDemandSubTab('open'));
        }
        if (btnRefreshDemandsList) {
            btnRefreshDemandsList.addEventListener('click', loadOpenDemandsList);
        }

        window.marketplace = window.marketplace || {};
        window.marketplace.showOpenDemandsTab = () => activateDemandSubTab('open');
        window.marketplace.showCreateDemandTab = () => activateDemandSubTab('create');

        // Submit Demand Form
        demandForm.addEventListener('submit', handleDemandFormSubmit);
        btnRefreshDemands.addEventListener('click', loadOpenDemandsList);

        // Reset Demand Form
        demandForm.addEventListener('reset', () => {
            requiredSkillsBuilderList = [];
            renderRequiredSkillChips();
            demandFormErrorBanner.style.display = 'none';
            demandFormSuccessBanner.style.display = 'none';
            setTimeout(loadNextDemandCodePreview, 0);
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

        // Screen 6 Utilization & Skill Gaps Sub-Tabs
        function activateUtilSubTab(tabName) {
            if (tabName === 'gaps') {
                if (tabBtnSkillGaps) {
                    tabBtnSkillGaps.classList.add('active');
                    tabBtnSkillGaps.setAttribute('aria-selected', 'true');
                }
                if (tabBtnEmpUtilization) {
                    tabBtnEmpUtilization.classList.remove('active');
                    tabBtnEmpUtilization.setAttribute('aria-selected', 'false');
                }
                if (paneSkillGaps) paneSkillGaps.style.display = 'block';
                if (paneEmpUtilization) paneEmpUtilization.style.display = 'none';
            } else {
                if (tabBtnEmpUtilization) {
                    tabBtnEmpUtilization.classList.add('active');
                    tabBtnEmpUtilization.setAttribute('aria-selected', 'true');
                }
                if (tabBtnSkillGaps) {
                    tabBtnSkillGaps.classList.remove('active');
                    tabBtnSkillGaps.setAttribute('aria-selected', 'false');
                }
                if (paneEmpUtilization) paneEmpUtilization.style.display = 'block';
                if (paneSkillGaps) paneSkillGaps.style.display = 'none';
            }
        }

        if (tabBtnEmpUtilization) {
            tabBtnEmpUtilization.addEventListener('click', () => activateUtilSubTab('util'));
        }
        if (tabBtnSkillGaps) {
            tabBtnSkillGaps.addEventListener('click', () => activateUtilSubTab('gaps'));
        }

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

        // Edit Demand Modal event listeners
        if (editDemandModalCloseBtn) {
            editDemandModalCloseBtn.addEventListener('click', closeEditDemandModal);
        }
        if (editDemandModalCancelBtn) {
            editDemandModalCancelBtn.addEventListener('click', closeEditDemandModal);
        }
        if (btnSaveEditedDemand) {
            btnSaveEditedDemand.addEventListener('click', handleSaveEditedDemand);
        }
        if (btnEditAddSkillReq) {
            btnEditAddSkillReq.addEventListener('click', () => {
                const skillId = editSkillSelect ? editSkillSelect.value : '';
                if (!skillId) {
                    showToast('Please select a skill to add', 'error');
                    return;
                }
                const skillObj = state.skills.find(s => s.id == skillId);
                if (!skillObj) return;

                if (editRequiredSkillsList.some(item => item.skillId == skillId)) {
                    showToast(`Skill '${skillObj.name}' is already added`, 'error');
                    return;
                }

                const minProf = editSkillProficiency ? editSkillProficiency.value : 'INTERMEDIATE';
                const isMandatory = editSkillMandatory ? editSkillMandatory.checked : true;

                editRequiredSkillsList.push({
                    skillId: Number(skillId),
                    skillName: skillObj.name,
                    minimumProficiency: minProf,
                    mandatory: isMandatory
                });

                renderEditRequiredSkillChips();
                if (editSkillSelect) editSkillSelect.value = '';
            });
        }
    }

    function renderEditRequiredSkillChips() {
        if (!editRequiredSkillsChipsContainer) return;
        if (editRequiredSkillsList.length === 0) {
            if (editEmptySkillsPrompt) editEmptySkillsPrompt.style.display = 'block';
            editRequiredSkillsChipsContainer.querySelectorAll('.skill-req-chip').forEach(c => c.remove());
            return;
        }

        if (editEmptySkillsPrompt) editEmptySkillsPrompt.style.display = 'none';
        editRequiredSkillsChipsContainer.querySelectorAll('.skill-req-chip').forEach(c => c.remove());

        editRequiredSkillsList.forEach((req, idx) => {
            const chip = document.createElement('div');
            chip.className = 'skill-req-chip';
            chip.innerHTML = `
                <span><strong>${escapeHtml(req.skillName)}</strong> (${req.minimumProficiency}${req.mandatory ? ' • Mandatory' : ''})</span>
                <span class="remove-chip" data-idx="${idx}" title="Remove skill">&times;</span>
            `;
            chip.querySelector('.remove-chip').addEventListener('click', (e) => {
                const removeIdx = Number(e.target.getAttribute('data-idx'));
                editRequiredSkillsList.splice(removeIdx, 1);
                renderEditRequiredSkillChips();
            });
            editRequiredSkillsChipsContainer.appendChild(chip);
        });
    }

    async function openEditDemandModal(demandId) {
        if (!editDemandModal) return;
        if (editDemandFormErrorBanner) editDemandFormErrorBanner.style.display = 'none';
        if (editDemandFormSuccessBanner) editDemandFormSuccessBanner.style.display = 'none';

        let demand = (state.demands || []).find(d => d.id == demandId);
        if (!demand) {
            try {
                const res = await fetch(`/api/demands/${demandId}`);
                const json = await safeParseResponse(res);
                demand = json ? json.data : null;
            } catch (err) {
                showToast('Failed to load demand: ' + err.message, 'error');
                return;
            }
        }
        if (!demand) {
            showToast('Demand not found', 'error');
            return;
        }

        editDemandId.value = demand.id;
        if (editDemandCodeBadge) editDemandCodeBadge.textContent = demand.demandCode || `#${demand.id}`;
        if (editProjectName) editProjectName.value = demand.projectName || '';
        if (editDemandRole) editDemandRole.value = demand.role || '';
        if (editDemandDepartment) editDemandDepartment.value = demand.department || 'Engineering';
        if (editDemandLocation) editDemandLocation.value = demand.location || '';
        if (editDemandPriority) editDemandPriority.value = demand.priority || 'HIGH';
        if (editDemandStatus) editDemandStatus.value = demand.status || 'OPEN';
        if (editDemandStartDate) editDemandStartDate.value = demand.startDate || '';
        if (editDemandEndDate) editDemandEndDate.value = demand.endDate || '';
        if (editDemandAllocationPercent) editDemandAllocationPercent.value = demand.allocationPercent || 100;
        if (editDemandExperience) editDemandExperience.value = demand.requiredExperienceYears || 0;

        editRequiredSkillsList = (demand.requiredSkills || []).map(s => ({
            skillId: s.skillId,
            skillName: s.skillName,
            minimumProficiency: s.minimumProficiency,
            mandatory: s.mandatory
        }));

        renderEditRequiredSkillChips();
        editDemandModal.style.display = 'flex';
    }

    function closeEditDemandModal() {
        if (editDemandModal) {
            editDemandModal.style.display = 'none';
        }
    }

    async function handleSaveEditedDemand() {
        const id = editDemandId.value;
        if (!id) return;

        if (editDemandFormErrorBanner) editDemandFormErrorBanner.style.display = 'none';
        if (editDemandFormSuccessBanner) editDemandFormSuccessBanner.style.display = 'none';

        const projectName = editProjectName.value.trim();
        const role = editDemandRole.value.trim();
        const department = editDemandDepartment ? editDemandDepartment.value : 'Engineering';
        const location = editDemandLocation.value.trim();
        const priority = editDemandPriority ? editDemandPriority.value : 'HIGH';
        const status = editDemandStatus ? editDemandStatus.value : 'OPEN';
        const startDate = editDemandStartDate.value;
        const endDate = editDemandEndDate.value;
        const allocationPercent = parseInt(editDemandAllocationPercent.value, 10);
        const requiredExperienceYears = parseFloat(editDemandExperience.value) || 0.0;

        if (!projectName || !role || !location || !startDate || !endDate) {
            if (editDemandFormErrorBanner) {
                editDemandFormErrorBanner.textContent = 'Please fill in all required fields.';
                editDemandFormErrorBanner.style.display = 'block';
            }
            return;
        }

        if (startDate > endDate) {
            if (editDemandFormErrorBanner) {
                editDemandFormErrorBanner.textContent = 'Start date cannot be after end date.';
                editDemandFormErrorBanner.style.display = 'block';
            }
            return;
        }

        if (isNaN(allocationPercent) || allocationPercent < 1 || allocationPercent > 100) {
            if (editDemandFormErrorBanner) {
                editDemandFormErrorBanner.textContent = 'Allocation percent must be between 1 and 100.';
                editDemandFormErrorBanner.style.display = 'block';
            }
            return;
        }

        const payload = {
            projectName,
            role,
            department,
            location,
            startDate,
            endDate,
            allocationPercent,
            priority,
            status,
            requiredExperienceYears,
            requiredSkills: editRequiredSkillsList.map(s => ({
                skillId: s.skillId,
                mandatory: s.mandatory,
                minimumProficiency: s.minimumProficiency
            }))
        };

        if (btnSaveEditedDemand) {
            btnSaveEditedDemand.disabled = true;
            btnSaveEditedDemand.textContent = 'Saving...';
        }

        try {
            const res = await fetch(`/api/demands/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const json = await safeParseResponse(res);
            if (!res.ok) {
                throw new Error((json && json.message) || 'Failed to update demand');
            }

            showToast('Demand updated successfully', 'success');
            closeEditDemandModal();
            await loadOpenDemandsList();
        } catch (err) {
            if (editDemandFormErrorBanner) {
                editDemandFormErrorBanner.textContent = err.message;
                editDemandFormErrorBanner.style.display = 'block';
            }
        } finally {
            if (btnSaveEditedDemand) {
                btnSaveEditedDemand.disabled = false;
                btnSaveEditedDemand.textContent = 'Save Changes';
            }
        }
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
            // demandCode is auto-generated by backend sequence to guarantee concurrency safety
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

            const json = await safeParseResponse(res);
            const created = (json && json.data) ? json.data : {};
            const assignedCode = created.demandCode || 'DEM-AUTO';

            demandFormSuccessBanner.innerHTML = `
                <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:0.5rem;">
                    <div>Demand <strong>${escapeHtml(assignedCode)}</strong> created successfully! It is now active for candidate matching.</div>
                    <button type="button" class="btn btn-secondary btn-sm" id="btnSwitchToOpenDemands" style="padding:0.25rem 0.65rem; font-size:0.75rem;">View in Open Demands &rarr;</button>
                </div>
            `;
            demandFormSuccessBanner.style.display = 'block';
            demandForm.reset();
            requiredSkillsBuilderList = [];
            renderRequiredSkillChips();
            await loadOpenDemandsList();
            showToast(`Demand ${assignedCode} created successfully`, 'success');
            loadNextDemandCodePreview();

            const btnSwitch = document.getElementById('btnSwitchToOpenDemands');
            if (btnSwitch) {
                btnSwitch.addEventListener('click', () => {
                    const tabBtn = document.getElementById('tabBtnOpenDemands');
                    if (tabBtn) tabBtn.click();
                });
            }
        } catch (err) {
            demandFormErrorBanner.textContent = err.message || 'Error creating demand';
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
            if (openDemandsCountTab) openDemandsCountTab.textContent = demands.length;

            if (demands.length === 0) {
                openDemandsListContainer.innerHTML = '<div class="empty-state"><p>No open demands found. Create one using the Create Demand tab.</p></div>';
                return;
            }

            openDemandsListContainer.innerHTML = demands.map(d => `
                <div class="demand-row-card">
                    <div class="demand-card-header">
                        <div class="demand-card-title-group">
                            <div class="demand-card-title-row">
                                <h4 class="demand-card-title">${escapeHtml(d.role)}</h4>
                                <span class="badge ${d.priority === 'CRITICAL' ? 'badge-danger' : d.priority === 'HIGH' ? 'badge-warning' : 'badge-grounded'}">${d.priority}</span>
                                <span class="badge badge-grounded">OPEN</span>
                            </div>
                            <div class="demand-card-meta">
                                <strong>Project:</strong> ${escapeHtml(d.projectName || 'Enterprise Initiative')} &bull;
                                <strong>Dept:</strong> ${escapeHtml(d.department || 'General')} &bull;
                                <strong>Location:</strong> ${escapeHtml(d.location)} &bull;
                                Code: <code>${escapeHtml(d.demandCode)}</code> (#${d.id})
                            </div>
                        </div>
                        <div class="demand-card-actions">
                            <button type="button" class="btn btn-secondary btn-sm" onclick="window.marketplace.openEditDemandModal(${d.id})">Edit Demand</button>
                            <button type="button" class="btn btn-primary btn-sm" onclick="window.marketplace.selectDemandForRanking(${d.id})">Rank Candidates &rarr;</button>
                            <button type="button" class="btn btn-secondary btn-sm" onclick="window.marketplace.askAiAboutDemand(${d.id})">Ask AI</button>
                        </div>
                    </div>

                    <div style="display:flex; align-items:center; gap:1.25rem; flex-wrap:wrap; font-size:0.8rem; color:var(--text-muted); background:rgba(15,23,42,0.4); padding:0.55rem 0.85rem; border-radius:4px; border:1px solid rgba(148,163,184,0.08);">
                        <div><strong style="color:var(--text-secondary);">Timeline:</strong> ${d.startDate} &rarr; ${d.endDate}</div>
                        <div><strong style="color:var(--text-secondary);">Allocation Req:</strong> <span style="color:#a5b4fc; font-weight:600;">${d.allocationPercent}% Capacity</span></div>
                        <div><strong style="color:var(--text-secondary);">Min Experience:</strong> ${d.requiredExperienceYears || 0} years</div>
                    </div>

                    <div style="display:flex; flex-direction:column; gap:0.35rem;">
                        <span style="font-size:0.75rem; font-weight:600; color:var(--text-secondary); text-transform:uppercase; letter-spacing:0.04em;">Required Skills:</span>
                        <div style="display:flex; flex-wrap:wrap; gap:0.4rem;">
                            ${(d.requiredSkills && d.requiredSkills.length > 0) ? d.requiredSkills.map(s => `
                                <span class="badge ${s.mandatory ? 'badge-tool' : 'badge-neutral'}" style="font-size:0.775rem; padding:0.25rem 0.6rem;">
                                    ${escapeHtml(s.skillName)} &bull; <strong>${s.minimumProficiency}</strong>${s.mandatory ? ' <span style="color:#f87171; font-weight:bold;">*Mandatory</span>' : ''}
                                </span>
                            `).join('') : '<span style="font-size:0.8rem; color:var(--text-muted);">No specific skills specified</span>'}
                        </div>
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
                        <th>Configured Capacity</th>
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
                                    <div style="font-weight:600; color:var(--text-primary);"><span class="clickable-emp-name" onclick="window.marketplace.viewEmployeeHistory(${emp.id})">${escapeHtml(emp.name)}</span></div>
                                    <div style="font-size:0.75rem; color:var(--text-secondary); font-family:monospace; cursor:pointer;" onclick="window.marketplace.viewEmployeeHistory(${emp.id})">${escapeHtml(emp.employeeCode)}</div>
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
                                    <div style="font-size:0.7rem; color:var(--text-muted);">${emp.maxCapacityPercent === 100 ? 'Full-Time (1.0 FTE)' : emp.maxCapacityPercent === 80 ? 'Part-Time (0.8 FTE)' : `Contracted (${emp.maxCapacityPercent}%)`}</div>
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
                                <td style="white-space:nowrap;">
                                    <button type="button" class="btn btn-primary btn-sm" onclick="window.marketplace.viewEmployeeHistory(${emp.id})" style="margin-right: 0.35rem;">History &amp; Availability</button>
                                    <button type="button" class="btn btn-secondary btn-sm" onclick="window.marketplace.askAiAboutEmployee('${escapeHtml(emp.name)}')">Inquire</button>
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

        // Set button loading state
        const origBtnHtml = btnRunRanking ? btnRunRanking.innerHTML : 'Rank Candidates';
        if (btnRunRanking) {
            btnRunRanking.disabled = true;
            btnRunRanking.innerHTML = '<span class="spinner" style="display:inline-block; width:14px; height:14px; border-width:2px; margin-right:6px; vertical-align:middle;"></span> Ranking...';
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
            const json = await safeParseResponse(res);
            const data = json ? json.data : null;
            const candidates = data ? data.candidates || [] : [];
            state.currentDemandCandidates = candidates;

            if (candidates.length === 0) {
                rankingResultsContainer.innerHTML = `
                    <div class="empty-state">
                        <span class="empty-icon"></span>
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
                                    ${c.existingAllocationStatus === 'PENDING' ?
                                        '<span class="badge badge-warning" style="background:#f59e0b; color:#fff; font-weight:700;">ALLOCATION PENDING</span>' :
                                      c.existingAllocationStatus === 'CONFIRMED' ?
                                        '<span class="badge badge-grounded" style="background:#10b981; color:#fff; font-weight:700;">ALREADY ALLOCATED</span>' :
                                        `<span class="badge ${c.eligible ? 'badge-tool' : 'badge-danger'}">
                                            ${c.eligible ? 'ELIGIBLE' : 'INELIGIBLE'}
                                        </span>`
                                    }
                                    <div class="score-number">${c.totalScore.toFixed(1)}<span style="font-size:0.8rem; color:var(--text-muted);">/100</span></div>
                                </div>
                            </div>

                            <!-- Score breakdown tags -->
                            <div class="score-breakdown-tags">
                                <span class="score-tag" title="Skill Match Score">Skills: <strong>${c.skillScore.toFixed(1)}/70</strong></span>
                                <span class="score-tag" title="Availability Score">Availability: <strong>${c.availabilityScore.toFixed(1)}/20</strong></span>
                                <span class="score-tag" title="Location Alignment Score">Location: <strong>${c.locationScore.toFixed(1)}/10</strong></span>
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
                                        <strong>Exclusion Reasons:</strong> ${c.exclusionReasons.map(r => escapeHtml(r)).join('; ')}
                                    </div>
                                ` : ''}
                            </div>

                            <!-- Deterministic Match Explanation -->
                            <div class="candidate-explanation">
                                ${escapeHtml(c.matchExplanation)}
                            </div>

                            <!-- Card Action Footer -->
                            <div class="candidate-card-actions">
                                <label class="checkbox-container">
                                    <input type="checkbox" class="candidate-compare-chk" data-empid="${c.employeeId}" ${isSelected ? 'checked' : ''}>
                                    <span class="checkbox-label">Select for Comparison</span>
                                </label>
                                <div style="display:flex; gap:0.5rem; align-items:center;">
                                    ${c.existingAllocationStatus === 'PENDING' ? `
                                        <button type="button" class="btn btn-secondary btn-sm" disabled title="An active allocation request already exists for this demand" style="opacity:0.85; cursor:not-allowed;"><span style="color:#f59e0b; margin-right:4px;">●</span> Allocation Pending</button>
                                        <button type="button" class="btn btn-secondary btn-sm" onclick="window.marketplace.viewEmployeeAllocations(${c.employeeId}, '${escapeHtml(c.employeeName)}')">View Request</button>
                                    ` : c.existingAllocationStatus === 'CONFIRMED' ? `
                                        <button type="button" class="btn btn-secondary btn-sm" disabled title="Employee is already allocated to this demand" style="opacity:0.85; cursor:not-allowed;"><span style="color:#10b981; margin-right:4px;">✓</span> Already Allocated</button>
                                        <button type="button" class="btn btn-secondary btn-sm" onclick="window.marketplace.viewEmployeeAllocations(${c.employeeId}, '${escapeHtml(c.employeeName)}')">Allocation History</button>
                                    ` : c.eligible ? `
                                        <button type="button" class="btn btn-primary btn-sm" onclick="window.marketplace.openCreateAllocationForCandidate(${demandId}, ${c.employeeId}, '${escapeHtml(c.employeeName)}')">Allocate Candidate</button>
                                    ` : `
                                        <button type="button" class="btn btn-secondary btn-sm" disabled title="${escapeHtml((c.exclusionReasons || []).join('; ') || 'Candidate is not eligible based on capacity/skills')}">Ineligible for Allocation</button>
                                    `}
                                    <button type="button" class="btn btn-secondary btn-sm" onclick="window.marketplace.askAiAboutCandidateDemand(${demandId}, '${escapeHtml(c.employeeName)}')">Inquire</button>
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
            console.error('Candidate ranking failed:', e);
            let displayMsg = escapeHtml(e.message || 'An unexpected error occurred while ranking candidates');
            let actionHtml = '';
            if (e.isAuthError || e.status === 401) {
                actionHtml = `<div style="margin-top:0.75rem;"><a href="/login" class="btn btn-primary btn-sm">Sign In Again</a></div>`;
            }
            rankingResultsContainer.innerHTML = `
                <div class="error-banner" style="display:flex; flex-direction:column; gap:0.5rem;">
                    <div><strong>Failed to rank candidates:</strong> ${displayMsg}</div>
                    ${actionHtml}
                </div>
            `;
            showToast(`Ranking failed: ${e.message}`, 'error');
        } finally {
            if (btnRunRanking) {
                btnRunRanking.disabled = false;
                btnRunRanking.innerHTML = origBtnHtml;
            }
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
            const json = await safeParseResponse(res);
            const candidates = json && json.data ? json.data.candidates || [] : [];
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
                    <span class="empty-icon"></span>
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
                                    <div>Skills: <strong>${c.skillScore.toFixed(1)}</strong> / 70</div>
                                    <div>Availability: <strong>${c.availabilityScore.toFixed(1)}</strong> / 20</div>
                                    <div>Location: <strong>${c.locationScore.toFixed(1)}</strong> / 10</div>
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
                                            <span class="badge badge-tool">${matched.candidateProficiency}</span>
                                            <div style="font-size:0.7rem; color:var(--text-muted); margin-top:0.2rem;">${matched.yearsOfExperience || 0} years experience</div>
                                        </td>
                                    `;
                                } else if (isMissingMandatory) {
                                    return `
                                        <td>
                                            <span class="badge badge-danger">Missing (Mandatory)</span>
                                        </td>
                                    `;
                                } else {
                                    return `
                                        <td>
                                            <span class="badge badge-neutral">Not Possessed</span>
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
                                ${c.existingAllocationStatus === 'PENDING' ? `
                                    <button type="button" class="btn btn-secondary btn-sm" disabled title="An active allocation request already exists for this demand" style="opacity:0.85; cursor:not-allowed;"><span style="color:#f59e0b; margin-right:4px;">●</span> Allocation Pending</button>
                                ` : c.existingAllocationStatus === 'CONFIRMED' ? `
                                    <button type="button" class="btn btn-secondary btn-sm" disabled title="Employee is already allocated to this demand" style="opacity:0.85; cursor:not-allowed;"><span style="color:#10b981; margin-right:4px;">✓</span> Already Allocated</button>
                                ` : c.eligible ? `
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
                        <span class="empty-icon"></span>
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
                                        <div style="display:flex; gap:0.4rem; flex-wrap:wrap; align-items:center;">
                                            ${a.status === 'PENDING' ? (
                                                (state.currentUser && state.currentUser.role === 'PROJECT_MANAGER') ?
                                                    '<span class="badge badge-warning">Pending Review</span>' :
                                                    `<button type="button" class="btn btn-primary btn-sm" onclick="window.marketplace.confirmAllocationDialog(${a.id}, '${escapeHtml(a.employeeName)}', ${a.allocationPercent})">Confirm</button>
                                                     <button type="button" class="btn btn-danger btn-sm" onclick="window.marketplace.rejectAllocationDialog(${a.id}, '${escapeHtml(a.employeeName)}')">Reject</button>`
                                            ) : ''}
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
                    <strong>Capacity Safety:</strong> This transaction executes serialized pessimistic write locking on the employee row to ensure 100% capacity safety.
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
                        await refreshMarketplaceData();
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
                        await refreshMarketplaceData();
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
                        await refreshMarketplaceData();
                    } else {
                        showToast(`Cancellation failed: ${json.message || res.statusText}`, 'error');
                    }
                } catch (err) {
                    showToast(`Error cancelling allocation: ${err.message}`, 'error');
                }
            }
        });
    }

    async function refreshMarketplaceData() {
        await loadInitialData();
        if (state.activeScreen === 'allocation-approvals') {
            await loadAllocations();
        }
        if (state.selectedDemandId && rankingDemandSelect) {
            rankingDemandSelect.value = state.selectedDemandId;
            await runCandidateRanking();
        }
        if (state.activeScreen === 'candidate-comparison' && state.selectedDemandId) {
            renderCandidateComparison();
        }
        if (state.activeScreen === 'dashboard') {
            loadDashboardSummary();
        }
    }

    // ==========================================================
    // EMPLOYEE PROJECT HISTORY & AVAILABILITY MODAL
    // ==========================================================
    const employeeHistoryModal = document.getElementById('employeeHistoryModal');
    const employeeHistoryModalCloseBtn = document.getElementById('employeeHistoryModalCloseBtn');
    const employeeHistoryModalDismissBtn = document.getElementById('employeeHistoryModalDismissBtn');

    if (employeeHistoryModalCloseBtn) {
        employeeHistoryModalCloseBtn.addEventListener('click', closeEmployeeHistoryModal);
    }
    if (employeeHistoryModalDismissBtn) {
        employeeHistoryModalDismissBtn.addEventListener('click', closeEmployeeHistoryModal);
    }
    if (employeeHistoryModal) {
        employeeHistoryModal.addEventListener('click', (e) => {
            if (e.target === employeeHistoryModal) closeEmployeeHistoryModal();
        });
    }

    let currentHistoryEmpId = null;
    let currentHistoryRequestId = 0;

    function closeEmployeeHistoryModal() {
        currentHistoryEmpId = null;
        currentHistoryRequestId++;
        if (employeeHistoryModal) {
            employeeHistoryModal.style.display = 'none';
        }
    }

    // Modal Sub-Tab Switching
    document.querySelectorAll('.modal-tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const targetTab = btn.getAttribute('data-tab');
            document.querySelectorAll('.modal-tab-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            document.querySelectorAll('.modal-tab-pane').forEach(pane => {
                pane.style.display = pane.id === `pane-${targetTab}` ? 'block' : 'none';
            });
        });
    });

    async function viewEmployeeHistory(employeeId) {
        currentHistoryEmpId = employeeId;
        const requestId = ++currentHistoryRequestId;
        if (!employeeHistoryModal) return;

        employeeHistoryModal.style.display = 'flex';

        // Reset to first tab
        document.querySelectorAll('.modal-tab-btn').forEach(b => b.classList.remove('active'));
        const btnTab1 = document.getElementById('btnTabProjectHistory');
        if (btnTab1) btnTab1.classList.add('active');
        document.querySelectorAll('.modal-tab-pane').forEach(pane => {
            pane.style.display = pane.id === 'pane-tab-project-history' ? 'block' : 'none';
        });

        // Initialize Employee Header in loading state
        const nameEl = document.getElementById('historyEmpName');
        const codeEl = document.getElementById('historyEmpCode');
        const statusEl = document.getElementById('historyEmpStatus');
        const subEl = document.getElementById('historyEmpSubtitle');

        if (nameEl) nameEl.textContent = 'Loading employee details...';
        if (codeEl) codeEl.textContent = `EMP-${employeeId}`;
        if (statusEl) {
            statusEl.textContent = 'LOADING';
            statusEl.className = 'badge badge-tool';
        }
        if (subEl) subEl.textContent = 'Fetching profile, assignments, leaves, and capacity...';

        // Initialize KPI Stat Cards in loading state
        const elMaxCap = document.getElementById('statConfiguredCapacity');
        const elCommitted = document.getElementById('statCommittedActive');
        const elRemaining = document.getElementById('statRemainingCapacity');
        const elPending = document.getElementById('statPendingCount');

        if (elMaxCap) elMaxCap.textContent = '...';
        if (elCommitted) elCommitted.textContent = '...';
        if (elRemaining) elRemaining.textContent = '...';
        if (elPending) elPending.textContent = '...';

        // Initialize Tab counts
        const countAllocEl = document.getElementById('historyAllocCount');
        const countLeavesEl = document.getElementById('historyLeavesCount');
        const countSkillsEl = document.getElementById('historySkillsCount');
        if (countAllocEl) countAllocEl.textContent = 'Loading...';
        if (countLeavesEl) countLeavesEl.textContent = 'Loading...';
        if (countSkillsEl) countSkillsEl.textContent = 'Loading...';

        // Show loading state in tab containers
        const allocContainer = document.getElementById('historyAllocationsTableContainer');
        const leavesContainer = document.getElementById('historyLeavesTableContainer');
        const skillsContainer = document.getElementById('historySkillsContainer');
        const checkResultContainer = document.getElementById('histCheckResultContainer');

        if (allocContainer) allocContainer.innerHTML = '<div class="loading-state"><div class="spinner"></div><p>Loading project allocation history...</p></div>';
        if (leavesContainer) leavesContainer.innerHTML = '<div class="loading-state"><div class="spinner"></div><p>Loading scheduled unavailabilities...</p></div>';
        if (skillsContainer) skillsContainer.innerHTML = '<div class="loading-state"><div class="spinner"></div><p>Loading verified skills...</p></div>';
        if (checkResultContainer) checkResultContainer.innerHTML = '<div class="loading-state"><div class="spinner"></div><p>Evaluating availability & capacity overlap...</p></div>';

        // Default calculator date range: today to 30 days ahead
        const today = new Date().toISOString().split('T')[0];
        const thirtyDays = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
        const startInput = document.getElementById('histCheckStartDate');
        const endInput = document.getElementById('histCheckEndDate');
        const percentInput = document.getElementById('histCheckPercent');
        if (startInput) startInput.value = today;
        if (endInput) endInput.value = thirtyDays;
        if (percentInput) percentInput.value = 100;

        let loadedEmp = null;
        let loadedAvail = null;

        function refreshKpis() {
            if (requestId !== currentHistoryRequestId) return;
            const maxCap = (loadedEmp && loadedEmp.maxCapacityPercent != null) ? loadedEmp.maxCapacityPercent : 100;
            const committed = (loadedAvail && loadedAvail.peakAllocatedPercent != null) ? loadedAvail.peakAllocatedPercent : 0;
            const remaining = (loadedAvail && loadedAvail.remainingCapacityPercent != null) ? loadedAvail.remainingCapacityPercent : Math.max(0, maxCap - committed);
            const pendingCount = (loadedAvail && loadedAvail.pendingAllocationsCount != null) ? loadedAvail.pendingAllocationsCount : 0;

            if (elMaxCap) elMaxCap.textContent = `${maxCap}%`;
            if (elCommitted) elCommitted.textContent = `${committed}%`;
            if (elRemaining) elRemaining.textContent = `${remaining}%`;
            if (elPending) elPending.textContent = `${pendingCount}`;
        }

        // Section 1: Employee Profile & Header
        const profileTask = (async () => {
            try {
                const res = await fetch(`/api/employees/${employeeId}`);
                const json = await safeParseResponse(res);
                if (requestId !== currentHistoryRequestId) return;
                if (!res.ok) throw new Error(json?.message || `Employee profile request failed (${res.status})`);
                const emp = json?.data || {};
                loadedEmp = emp;

                if (nameEl) nameEl.textContent = emp.name || 'Employee';
                if (codeEl) codeEl.textContent = emp.employeeCode || `ID-${emp.id}`;
                if (statusEl) {
                    statusEl.textContent = emp.employmentStatus || 'ACTIVE';
                    statusEl.className = `badge ${emp.employmentStatus === 'ACTIVE' ? 'badge-tool' : emp.employmentStatus === 'ON_LEAVE' ? 'badge-warning' : 'badge-danger'}`;
                }
                if (subEl) {
                    subEl.textContent = `${emp.department || 'N/A'} • ${emp.designation || 'Staff'} • ${emp.location || 'Remote'}`;
                }
                refreshKpis();
            } catch (err) {
                if (requestId !== currentHistoryRequestId) return;
                console.error(`[EmployeeDetails] Failed to load profile for employee ${employeeId}:`, err);
                if (nameEl) nameEl.textContent = `Employee #${employeeId}`;
                if (subEl) subEl.textContent = `Profile unavailable: ${escapeHtml(err.message)}`;
                showToast(`Failed to load employee profile: ${err.message}`, 'error');
            }
        })();

        // Section 2: Project Allocation History
        const allocationsTask = (async () => {
            try {
                const res = await fetch(`/api/allocations/employee/${employeeId}`);
                const json = await safeParseResponse(res);
                if (requestId !== currentHistoryRequestId) return;
                if (!res.ok) throw new Error(json?.message || `Allocation history request failed (${res.status})`);
                const allocs = Array.isArray(json?.data) ? json.data : [];
                renderHistoryAllocations(allocs, employeeId);
            } catch (err) {
                if (requestId !== currentHistoryRequestId) return;
                console.error(`[EmployeeDetails] Failed to load allocations for employee ${employeeId}:`, err);
                if (countAllocEl) countAllocEl.textContent = 'Error';
                if (allocContainer) {
                    allocContainer.innerHTML = `
                        <div class="error-banner" style="margin: 1rem 0; padding: 1rem; background: rgba(239, 68, 68, 0.12); border: 1px solid rgba(239, 68, 68, 0.3); border-radius: var(--radius-sm); color: #f87171; font-size: 0.85rem;">
                            <strong>Failed to load project allocation history:</strong> ${escapeHtml(err.message)}
                        </div>
                    `;
                }
            }
        })();

        // Section 3: Scheduled Leaves & Unavailabilities
        const leavesTask = (async () => {
            try {
                // Fetch canonical /unavailability endpoint (backend supports both singular and plural)
                const res = await fetch(`/api/employees/${employeeId}/unavailability`);
                const json = await safeParseResponse(res);
                if (requestId !== currentHistoryRequestId) return;
                if (!res.ok) throw new Error(json?.message || `Leaves request failed (${res.status})`);
                const unavails = Array.isArray(json?.data) ? json.data : [];
                renderHistoryLeaves(unavails);
            } catch (err) {
                if (requestId !== currentHistoryRequestId) return;
                console.error(`[EmployeeDetails] Failed to load unavailabilities for employee ${employeeId}:`, err);
                if (countLeavesEl) countLeavesEl.textContent = 'Error';
                if (leavesContainer) {
                    leavesContainer.innerHTML = `
                        <div class="error-banner" style="margin: 1rem 0; padding: 1rem; background: rgba(239, 68, 68, 0.12); border: 1px solid rgba(239, 68, 68, 0.3); border-radius: var(--radius-sm); color: #f87171; font-size: 0.85rem;">
                            <strong>Failed to load scheduled unavailabilities:</strong> ${escapeHtml(err.message)}
                        </div>
                    `;
                }
            }
        })();

        // Section 4: Verified Skills
        const skillsTask = (async () => {
            try {
                const res = await fetch(`/api/employees/${employeeId}/skills`);
                const json = await safeParseResponse(res);
                if (requestId !== currentHistoryRequestId) return;
                if (!res.ok) throw new Error(json?.message || `Skills request failed (${res.status})`);
                const skills = Array.isArray(json?.data) ? json.data : [];
                renderHistorySkills(skills);
            } catch (err) {
                if (requestId !== currentHistoryRequestId) return;
                console.error(`[EmployeeDetails] Failed to load skills for employee ${employeeId}:`, err);
                if (countSkillsEl) countSkillsEl.textContent = 'Error';
                if (skillsContainer) {
                    skillsContainer.innerHTML = `
                        <div class="error-banner" style="grid-column: 1 / -1; margin: 1rem 0; padding: 1rem; background: rgba(239, 68, 68, 0.12); border: 1px solid rgba(239, 68, 68, 0.3); border-radius: var(--radius-sm); color: #f87171; font-size: 0.85rem;">
                            <strong>Failed to load verified skills:</strong> ${escapeHtml(err.message)}
                        </div>
                    `;
                }
            }
        })();

        // Section 5: Availability & Capacity Assessment
        const availabilityTask = (async () => {
            try {
                const res = await fetch(`/api/employees/${employeeId}/availability?startDate=${today}&endDate=${thirtyDays}&allocationPercent=100`);
                const json = await safeParseResponse(res);
                if (requestId !== currentHistoryRequestId) return;
                if (!res.ok) throw new Error(json?.message || `Availability assessment failed (${res.status})`);
                const avail = json?.data || {};
                loadedAvail = avail;
                renderHistoryAvailabilityResult(avail, today, thirtyDays, 100);
                refreshKpis();
            } catch (err) {
                if (requestId !== currentHistoryRequestId) return;
                console.error(`[EmployeeDetails] Failed to calculate availability for employee ${employeeId}:`, err);
                if (checkResultContainer) {
                    checkResultContainer.innerHTML = `
                        <div class="error-banner" style="margin: 1rem 0; padding: 1rem; background: rgba(239, 68, 68, 0.12); border: 1px solid rgba(239, 68, 68, 0.3); border-radius: var(--radius-sm); color: #f87171; font-size: 0.85rem;">
                            <strong>Failed to calculate availability assessment:</strong> ${escapeHtml(err.message)}
                        </div>
                    `;
                }
            }
        })();

        // Concurrently await all tasks with Promise.allSettled so every spinner is guaranteed to dismiss
        await Promise.allSettled([profileTask, allocationsTask, leavesTask, skillsTask, availabilityTask]);
    }

    function renderHistoryAllocations(allocs, employeeId) {
        const countEl = document.getElementById('historyAllocCount');
        const container = document.getElementById('historyAllocationsTableContainer');
        if (!container) return;

        const sorted = (allocs || []).slice().sort((a, b) => new Date(b.startDate || 0) - new Date(a.startDate || 0));
        if (countEl) countEl.textContent = `${sorted.length} record(s)`;

        if (sorted.length === 0) {
            container.innerHTML = `
                <div class="empty-state" style="padding:1.5rem 0;">
                    <p style="color:var(--text-secondary);">No project allocation records found for this employee.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = `
            <table class="standard-table" style="font-size:0.8rem;">
                <thead>
                    <tr>
                        <th>Project / Demand</th>
                        <th>Role</th>
                        <th>Dates</th>
                        <th>Alloc %</th>
                        <th>Status</th>
                        <th>Audit Details</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    ${sorted.map(a => {
                        const isCurrentDemand = state.selectedDemandId && a.demandId == state.selectedDemandId;
                        let statusBadge;
                        switch (a.status) {
                            case 'CONFIRMED':
                                statusBadge = '<span class="badge" style="background:#10b981; color:#fff;">CONFIRMED</span>';
                                break;
                            case 'COMPLETED':
                                statusBadge = '<span class="badge" style="background:#8b5cf6; color:#fff;">COMPLETED</span>';
                                break;
                            case 'PENDING':
                                statusBadge = '<span class="badge" style="background:#f59e0b; color:#fff;">PENDING</span>';
                                break;
                            case 'CANCELLED':
                                statusBadge = '<span class="badge" style="background:#64748b; color:#fff;">CANCELLED</span>';
                                break;
                            case 'REJECTED':
                                statusBadge = '<span class="badge" style="background:#ef4444; color:#fff;">REJECTED</span>';
                                break;
                            default:
                                statusBadge = `<span class="badge badge-neutral">${a.status}</span>`;
                        }

                        let actionButtons = '';
                        if (a.status === 'CONFIRMED') {
                            actionButtons = `
                                <button type="button" class="btn btn-secondary btn-xs" onclick="window.marketplace.completeAllocationAction(${a.id}, ${employeeId})" style="margin-right:4px;">Mark Completed</button>
                                <button type="button" class="btn btn-danger btn-xs" onclick="window.marketplace.cancelAllocationAction(${a.id}, ${employeeId})">Cancel</button>
                            `;
                        } else if (a.status === 'PENDING') {
                            actionButtons = `
                                <button type="button" class="btn btn-danger btn-xs" onclick="window.marketplace.cancelAllocationAction(${a.id}, ${employeeId})">Cancel Request</button>
                            `;
                        } else {
                            actionButtons = `<span style="font-size:0.7rem; color:var(--text-muted);">Archived</span>`;
                        }

                        return `
                            <tr>
                                <td>
                                    <div style="font-weight:600; color:var(--text-primary);">${escapeHtml(a.projectName || 'Project')}</div>
                                    <div style="font-size:0.7rem; color:var(--text-secondary); font-family:monospace;">Demand: ${escapeHtml(a.demandCode || a.demandId)} ${isCurrentDemand ? '<span style="color:var(--accent-primary); font-weight:700;">(Current)</span>' : ''}</div>
                                </td>
                                <td>${escapeHtml(a.projectRole || 'N/A')}</td>
                                <td style="white-space:nowrap;">${a.startDate} to ${a.endDate}</td>
                                <td><strong>${a.allocationPercent}%</strong></td>
                                <td>${statusBadge}</td>
                                <td style="font-size:0.72rem; color:var(--text-muted);">
                                    <div>Allocated by: ${escapeHtml(a.allocatedBy || 'System')}</div>
                                    ${a.confirmedBy ? `<div>Confirmed: ${escapeHtml(a.confirmedBy)} (${formatDate(a.confirmedAt)})</div>` : ''}
                                    ${a.rejectionReason ? `<div style="color:#f87171;">Reason: ${escapeHtml(a.rejectionReason)}</div>` : ''}
                                </td>
                                <td style="white-space:nowrap;">${actionButtons}</td>
                            </tr>
                        `;
                    }).join('')}
                </tbody>
            </table>
        `;
    }

    function renderHistoryLeaves(unavails) {
        const countEl = document.getElementById('historyLeavesCount');
        const container = document.getElementById('historyLeavesTableContainer');
        if (!container) return;

        if (countEl) countEl.textContent = `${unavails.length} record(s)`;
        if (!unavails || unavails.length === 0) {
            container.innerHTML = `
                <div class="empty-state" style="padding:1.5rem 0;">
                    <p style="color:var(--text-secondary);">No scheduled leaves or unavailabilities on record.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = `
            <table class="standard-table" style="font-size:0.8rem;">
                <thead>
                    <tr>
                        <th>Type</th>
                        <th>Date Range</th>
                        <th>Description / Reason</th>
                    </tr>
                </thead>
                <tbody>
                    ${unavails.map(u => `
                        <tr>
                            <td><span class="badge badge-warning">${escapeHtml(u.type || 'LEAVE')}</span></td>
                            <td style="white-space:nowrap;">${u.startDate} to ${u.endDate}</td>
                            <td>${escapeHtml(u.reason || u.description || 'Scheduled leave / absence')}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    }

    function renderHistorySkills(skills) {
        const countEl = document.getElementById('historySkillsCount');
        const container = document.getElementById('historySkillsContainer');
        if (!container) return;

        if (countEl) countEl.textContent = `${skills.length} verified skill(s)`;
        if (!skills || skills.length === 0) {
            container.innerHTML = `
                <div class="empty-state" style="padding:1.5rem 0; grid-column: 1 / -1;">
                    <p style="color:var(--text-secondary);">No skills assigned to this employee.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = skills.map(s => {
            const profBadgeClass = s.proficiencyLevel === 'ADVANCED' ? 'badge-grounded' :
                                   s.proficiencyLevel === 'INTERMEDIATE' ? 'badge-tool' : 'badge-neutral';
            return `
                <div style="background: rgba(30, 41, 59, 0.5); border: 1px solid var(--border-color); border-radius: var(--radius-sm); padding: 0.75rem;">
                    <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:0.35rem;">
                        <span style="font-weight:600; color:var(--text-primary); font-size:0.85rem;">${escapeHtml(s.skillName)}</span>
                        <span class="badge ${profBadgeClass}" style="font-size:0.7rem;">${s.proficiencyLevel}</span>
                    </div>
                    <div style="font-size:0.72rem; color:var(--text-secondary); margin-bottom:0.25rem;">
                        Category: <strong>${escapeHtml(s.category || 'GENERAL')}</strong> • Experience: <strong>${s.yearsOfExperience || 0} years</strong>
                    </div>
                    ${s.evidenceNotes ? `<div style="font-size:0.7rem; color:var(--text-muted); font-style:italic; margin-top:0.35rem; border-top:1px solid rgba(255,255,255,0.05); padding-top:0.25rem;">${escapeHtml(s.evidenceNotes)}</div>` : ''}
                </div>
            `;
        }).join('');
    }

    function renderHistoryAvailabilityResult(data, startDate, endDate, reqPercent) {
        const container = document.getElementById('histCheckResultContainer');
        if (!container || !data) return;

        const isAvail = data.available;
        const bannerClass = isAvail ? 'background: rgba(16, 185, 129, 0.12); border: 1px solid rgba(16, 185, 129, 0.3);' :
                                      'background: rgba(239, 68, 68, 0.12); border: 1px solid rgba(239, 68, 68, 0.3);';
        const titleColor = isAvail ? '#34d399' : '#f87171';
        const titleText = isAvail ? 'AVAILABLE FOR STAFFING' : 'CAPACITY OR CALENDAR CONFLICT';

        container.innerHTML = `
            <div style="${bannerClass} border-radius: var(--radius-sm); padding: 1rem;">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.5rem;">
                    <span style="font-weight:700; font-size:0.85rem; color:${titleColor}; letter-spacing:0.04em;">${titleText}</span>
                    <span style="font-size:0.75rem; color:var(--text-secondary);">${startDate} to ${endDate} (${reqPercent}% requested)</span>
                </div>
                <div style="font-size:0.8rem; color:var(--text-primary); margin-bottom:0.75rem;">
                    ${escapeHtml(data.reason || 'Assessment completed successfully.')}
                </div>
                <div style="display:grid; grid-template-columns: repeat(4, 1fr); gap: 0.5rem; font-size:0.75rem; border-top:1px solid rgba(255,255,255,0.08); padding-top:0.5rem;">
                    <div><span style="color:var(--text-muted);">Configured Max:</span> <strong>${data.maxCapacityPercent}%</strong></div>
                    <div><span style="color:var(--text-muted);">Committed Active:</span> <strong>${data.peakAllocatedPercent != null ? data.peakAllocatedPercent : 0}%</strong></div>
                    <div><span style="color:var(--text-muted);">Remaining Capacity:</span> <strong style="color:#34d399;">${data.remainingCapacityPercent}%</strong></div>
                    <div><span style="color:var(--text-muted);">Pending Pipeline:</span> <strong style="color:#fbbf24;">${data.pendingAllocationsCount || 0} req (${data.pendingAllocationPercent || 0}%)</strong></div>
                </div>
                ${data.overlappingUnavailabilities && data.overlappingUnavailabilities.length > 0 ? `
                    <div style="margin-top:0.6rem; padding-top:0.4rem; border-top:1px dashed rgba(239,68,68,0.3); font-size:0.75rem; color:#f87171;">
                        <strong>Conflicting Scheduled Unavailability:</strong>
                        ${data.overlappingUnavailabilities.map(u => `${u.type} (${u.startDate} to ${u.endDate})`).join(', ')}
                    </div>
                ` : ''}
            </div>
        `;
    }

    // Interactive Availability Check Button Binding
    const btnRunHistoryCheck = document.getElementById('btnRunHistoryCheck');
    if (btnRunHistoryCheck) {
        btnRunHistoryCheck.addEventListener('click', async () => {
            if (!currentHistoryEmpId) return;
            const s = document.getElementById('histCheckStartDate').value;
            const e = document.getElementById('histCheckEndDate').value;
            const p = document.getElementById('histCheckPercent').value || 100;
            if (!s || !e) {
                showToast('Please select start date and end date', 'warning');
                return;
            }
            const checkContainer = document.getElementById('histCheckResultContainer');
            if (checkContainer) checkContainer.innerHTML = '<div class="loading-state"><div class="spinner"></div><p>Calculating capacity and overlap...</p></div>';

            try {
                const res = await fetch(`/api/employees/${currentHistoryEmpId}/availability?startDate=${s}&endDate=${e}&allocationPercent=${p}`);
                const json = await safeParseResponse(res);
                if (!res.ok) throw new Error(json?.message || 'Failed to assess availability');
                renderHistoryAvailabilityResult(json.data, s, e, p);
            } catch (err) {
                if (checkContainer) checkContainer.innerHTML = `<div class="error-banner">${escapeHtml(err.message)}</div>`;
            }
        });
    }

    async function completeAllocationAction(allocationId, employeeId) {
        if (!confirm('Mark this allocation as COMPLETED? Project allocation history will be preserved.')) return;
        try {
            const res = await fetch(`/api/allocations/${allocationId}/complete`, { method: 'POST' });
            const json = await safeParseResponse(res);
            if (!res.ok) throw new Error(json?.message || 'Failed to complete allocation');
            showToast('Allocation marked as COMPLETED. History preserved.', 'success');
            viewEmployeeHistory(employeeId);
            loadAllocationsData();
            loadDashboardSummary();
        } catch (err) {
            showToast(err.message, 'error');
        }
    }

    async function cancelAllocationAction(allocationId, employeeId) {
        const reason = prompt('Please enter reason for cancellation:', 'Project schedule adjusted');
        if (reason === null) return;
        try {
            const res = await fetch(`/api/allocations/${allocationId}/cancel`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ reason: reason || 'Allocation cancelled' })
            });
            const json = await safeParseResponse(res);
            if (!res.ok) throw new Error(json?.message || 'Failed to cancel allocation');
            showToast('Allocation cancelled. Record preserved in history.', 'success');
            viewEmployeeHistory(employeeId);
            loadAllocationsData();
            loadDashboardSummary();
        } catch (err) {
            showToast(err.message, 'error');
        }
    }

    async function viewEmployeeAllocations(employeeId, employeeName) {
        viewEmployeeHistory(employeeId);
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

                // Requirement 4 guard: check if active allocation already exists for this demand and employee
                const existing = (state.allocations || []).find(a =>
                    a.demandId == demandId && a.employeeId == employeeId &&
                    (a.status === 'PENDING' || a.status === 'CONFIRMED')
                );
                if (existing) {
                    const msg = existing.status === 'PENDING' ?
                        'An active allocation request already exists for this demand' :
                        'Employee is already allocated to this demand';
                    showToast(msg, 'warning');
                    return;
                }

                const confirmBtn = document.getElementById('modalConfirmBtn');
                if (confirmBtn) {
                    confirmBtn.disabled = true;
                    confirmBtn.textContent = 'Submitting...';
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
                        await refreshMarketplaceData();
                    } else {
                        showToast(`Creation failed: ${json.message || res.statusText}`, 'error');
                        if (confirmBtn) {
                            confirmBtn.disabled = false;
                            confirmBtn.textContent = 'Create Allocation Request';
                        }
                    }
                } catch (err) {
                    showToast(`Error creating allocation: ${err.message}`, 'error');
                    if (confirmBtn) {
                        confirmBtn.disabled = false;
                        confirmBtn.textContent = 'Create Allocation Request';
                    }
                }
            }
        });
    }

    function openCreateAllocationForCandidate(demandId, employeeId, employeeName) {
        // Requirement 4 guard: Check if an active allocation already exists for this demand
        const cand = (state.currentDemandCandidates || []).find(c => c.employeeId == employeeId);
        if (cand) {
            if (cand.existingAllocationStatus === 'PENDING') {
                showToast('An active allocation request already exists for this demand', 'warning');
                return;
            }
            if (cand.existingAllocationStatus === 'CONFIRMED') {
                showToast('Employee is already allocated to this demand', 'warning');
                return;
            }
        }

        const existingAlloc = (state.allocations || []).find(a =>
            a.demandId == demandId && a.employeeId == employeeId &&
            (a.status === 'PENDING' || a.status === 'CONFIRMED')
        );
        if (existingAlloc) {
            const msg = existingAlloc.status === 'PENDING' ?
                'An active allocation request already exists for this demand' :
                'Employee is already allocated to this demand';
            showToast(msg, 'warning');
            return;
        }

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

                const confirmBtn = document.getElementById('modalConfirmBtn');
                if (confirmBtn) {
                    confirmBtn.disabled = true;
                    confirmBtn.textContent = 'Submitting...';
                }

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
                        await refreshMarketplaceData();
                        switchScreen('allocation-approvals');
                    } else {
                        showToast(`Failed: ${json.message || res.statusText}`, 'error');
                        if (confirmBtn) {
                            confirmBtn.disabled = false;
                            confirmBtn.textContent = 'Submit Allocation';
                        }
                    }
                } catch (err) {
                    showToast(`Error: ${err.message}`, 'error');
                    if (confirmBtn) {
                        confirmBtn.disabled = false;
                        confirmBtn.textContent = 'Submit Allocation';
                    }
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

        if (utilDateErrorBanner) {
            utilDateErrorBanner.style.display = 'none';
        }

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

        // Validate Date Range
        if (startDate && endDate && startDate > endDate) {
            const errorMsg = 'Validation error: Window start date cannot be after end date.';
            if (utilDateErrorBanner) {
                utilDateErrorBanner.textContent = errorMsg;
                utilDateErrorBanner.style.display = 'block';
            }
            showToast(errorMsg, 'error');
            utilizationListContainer.innerHTML = '<div class="empty-state"><p style="color:#fca5a5;">Start date cannot be after end date. Please adjust the window filters above.</p></div>';
            skillGapsListContainer.innerHTML = '<div class="empty-state"><p style="color:#fca5a5;">Start date cannot be after end date. Please adjust the window filters above.</p></div>';
            return;
        }

        try {
            // Fetch analytics reports in parallel
            const utilQuery = `/api/analytics/reports/utilization-by-employee-department?startDate=${startDate}&endDate=${endDate}${selectedDept ? `&department=${encodeURIComponent(selectedDept)}` : ''}`;
            const [utilRes, gapsRes, empRes] = await Promise.all([
                fetch(utilQuery),
                fetch('/api/analytics/reports/skill-gaps-by-category'),
                fetch('/api/employees')
            ]);

            const utilJson = await utilRes.json();
            const gapsJson = await gapsRes.json();
            const empJson = await empRes.json();

            if (!utilJson.success) {
                throw new Error(utilJson.message || 'Failed to calculate utilization');
            }

            const utilData = utilJson.data || {};
            const gapsData = gapsJson.data || {};
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

            // 1. Render Full-Width Employee Capacity Utilization Table
            const employeeDetails = utilData.employeeDetails || [];
            if (employeeDetails.length === 0) {
                utilizationListContainer.innerHTML = '<div class="empty-state"><p>No active employees found matching the filter criteria.</p></div>';
            } else {
                utilizationListContainer.innerHTML = `
                    <table class="standard-table">
                        <thead>
                            <tr>
                                <th>Employee &amp; Code</th>
                                <th>Department</th>
                                <th>Peak Commitment</th>
                                <th>Time-Weighted Avg</th>
                                <th>Remaining Capacity</th>
                                <th>Allocations in Window</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${employeeDetails.map(emp => {
                                const peak = emp.peakSimultaneousAllocationPercent || 0;
                                const peakUtil = emp.peakUtilizationPercent || 0;
                                const timeWeighted = emp.timeWeightedAverageUtilizationPercent || 0;
                                const maxCap = emp.maxCapacityPercent || 100;
                                const allocCount = emp.aggregateConfirmedAllocationsCount || 0;
                                const remainingCap = Math.max(0, maxCap - peak);

                                const fillClass = peakUtil > 100 ? 'danger' : peakUtil >= 80 ? 'committed' : 'normal';
                                const badgeClass = peakUtil > 100 ? 'badge-danger' : peakUtil >= 80 ? 'badge-warning' : 'badge-tool';

                                return `
                                    <tr>
                                        <td>
                                            <strong style="color:var(--text-primary); font-size:0.9rem;">${escapeHtml(emp.employeeName)}</strong>
                                            <div style="font-size:0.75rem; color:var(--text-muted); font-family:monospace; margin-top:0.15rem;">${escapeHtml(emp.employeeCode)}</div>
                                        </td>
                                        <td>
                                            <span class="badge badge-secondary">${escapeHtml(emp.department || 'General')}</span>
                                        </td>
                                        <td style="min-width: 180px;">
                                            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.35rem;">
                                                <span class="badge ${badgeClass}" title="Peak simultaneous allocation on any single day in window">${peak}% Peak</span>
                                                <span style="font-size:0.75rem; color:var(--text-secondary);">${peakUtil}% of max</span>
                                            </div>
                                            <div class="progress-bar-container" style="height:6px; background:rgba(255,255,255,0.08); border-radius:3px; overflow:hidden;">
                                                <div class="progress-bar-fill ${fillClass}" style="width: ${Math.min(100, peakUtil)}%; height:100%;"></div>
                                            </div>
                                        </td>
                                        <td>
                                            <span style="font-weight:600; color:var(--text-primary);">${timeWeighted}%</span>
                                            <div style="font-size:0.7rem; color:var(--text-muted);">Window weighted</div>
                                        </td>
                                        <td>
                                            <strong style="color:${remainingCap > 0 ? '#10b981' : '#f87171'}; font-size:0.9rem;">${remainingCap}%</strong>
                                            <div style="font-size:0.7rem; color:var(--text-muted);">Max: ${maxCap}%</div>
                                        </td>
                                        <td>
                                            ${emp.hasConfirmedAllocations ?
                                                `<span class="badge badge-grounded">${allocCount} Confirmed</span>` :
                                                `<span class="text-xs text-muted">0 Allocations</span>`
                                            }
                                        </td>
                                    </tr>
                                `;
                            }).join('')}
                        </tbody>
                    </table>
                `;
            }

            // 2. Render Full-Width Enterprise Skill Shortage Analysis Table
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
                    <div style="margin-bottom:1rem; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:0.5rem; background:rgba(15,23,42,0.4); padding:0.75rem 1rem; border-radius:var(--radius-sm); border:1px solid var(--border-color); font-size:0.825rem; color:var(--text-secondary);">
                        <div>Categories with Deficit: <strong style="color:var(--text-primary);">${gapsData.categoriesWithGapsCount || 0}</strong> / ${gapsData.totalCategories || 0}</div>
                        <div>Total Unmet Demand Slots: <strong style="color:#f87171;">${gapsData.totalUnmetSkillDemandSlots || 0} slots</strong></div>
                    </div>
                    <table class="standard-table">
                        <thead>
                            <tr>
                                <th>Required Skill</th>
                                <th>Skill Category</th>
                                <th>Open Demands</th>
                                <th>Qualified Employee Supply</th>
                                <th>Deficit / Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${allSkillRows.map(row => {
                                const isCritical = row.deficit > 0 && row.mandatoryDemandCount > 0;
                                const isModerate = row.deficit > 0 && !isCritical;
                                const statusBadge = isCritical ? `<span class="badge badge-danger">DEFICIT: -${row.deficit}</span>` :
                                                    isModerate ? `<span class="badge badge-warning">DEFICIT: -${row.deficit}</span>` :
                                                    '<span class="badge badge-grounded">ADEQUATE</span>';
                                return `
                                    <tr>
                                        <td>
                                            <strong style="color:var(--text-primary); font-size:0.9rem;">${escapeHtml(row.skillName)}</strong>
                                            <div style="font-size:0.75rem; color:var(--text-muted); margin-top:0.15rem;">Min Proficiency: <strong>${row.minimumProficiency}</strong>${row.mandatoryDemandCount > 0 ? ` &bull; ${row.mandatoryDemandCount} mandatory` : ''}</div>
                                        </td>
                                        <td><span class="badge badge-secondary">${escapeHtml(row.category)}</span></td>
                                        <td><strong>${row.demandCount}</strong> demand${row.demandCount === 1 ? '' : 's'}</td>
                                        <td><strong>${row.qualifiedResourceCount}</strong> qualified employee${row.qualifiedResourceCount === 1 ? '' : 's'}</td>
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
                if (data && data.ollamaReachable) {
                    if (statusDot) statusDot.className = 'pulse-dot';
                    if (mobileStatusDot) mobileStatusDot.className = 'pulse-dot';
                    if (statusText) statusText.textContent = `Local Ollama: ${data.model} (Active)`;
                } else {
                    if (statusDot) statusDot.className = 'pulse-dot offline';
                    if (mobileStatusDot) mobileStatusDot.className = 'pulse-dot offline';
                    if (statusText) statusText.textContent = `Local Ollama: ${data ? data.model : 'Model'} (Offline)`;
                }
            } else {
                if (statusDot) statusDot.className = 'pulse-dot offline';
                if (mobileStatusDot) mobileStatusDot.className = 'pulse-dot offline';
                if (statusText) statusText.textContent = 'Local Ollama: Offline';
            }
        } catch {
            if (statusDot) statusDot.className = 'pulse-dot offline';
            if (mobileStatusDot) mobileStatusDot.className = 'pulse-dot offline';
            if (statusText) statusText.textContent = 'Local Ollama: Disconnected';
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
        avatar.innerHTML = role === 'user' ? 'User' : 'AI';

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
        avatar.innerHTML = 'AI';

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
                toolBadge.textContent = `${tool.toolName} (${tool.executionDurationMs}ms)`;
                metaDiv.appendChild(toolBadge);
            });

            if (data.grounded) {
                const groundedBadge = document.createElement('span');
                groundedBadge.className = 'badge badge-grounded';
                groundedBadge.textContent = 'Database Grounded';
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
        avatar.innerHTML = '!';

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
        avatar.innerHTML = 'AI';

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
        cancelAllocationDialog,
        viewEmployeeAllocations,
        viewEmployeeHistory,
        completeAllocationAction,
        cancelAllocationAction,
        closeEmployeeHistoryModal,
        openEditDemandModal,
        closeEditDemandModal,
        refreshMarketplaceData
    };

    // Run Initialization
    init();
});
