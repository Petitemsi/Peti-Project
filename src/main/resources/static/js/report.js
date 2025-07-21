
document.addEventListener('DOMContentLoaded', function() {
    const saveReportBtn = document.getElementById('saveReportBtn');
    const reportForm = document.getElementById('reportForm');
    const addReportModal = new bootstrap.Modal(document.getElementById('addReportModal'));
    const successToast = new bootstrap.Toast(document.getElementById('successToast'));
    const errorToast = new bootstrap.Toast(document.getElementById('errorToast'));
    const deleteConfirmationModal = new bootstrap.Modal(document.getElementById('deleteConfirmationModal'));
    const confirmDeleteButton = document.getElementById('confirmDeleteButton');

    let reportToDeleteId = null;
    let isUser = false;

    // Initialize the page
    async function init() {
        try {
            const role = await getUserRole();
            isUser = role === 'USER';
            updateUIForUserRole();
            fetchReports();
        } catch (error) {
            console.error('Error initializing page:', error);
            showErrorMessage('Error loading user information');
        }
    }

    // Update UI based on user role
    function updateUIForUserRole() {
        // Show/hide admin controls
        document.querySelectorAll('.admin-only').forEach(el => {
            el.style.display = isUser ? '' : 'none';
        });
    }

    // Function to show success message
    function showSuccessMessage(message) {
        document.getElementById('successMessage').textContent = message;
        successToast.show();
    }

    // Function to show error message
    function showErrorMessage(message) {
        document.getElementById('errorMessage').textContent = message;
        errorToast.show();
    }

    // Save report handler
    saveReportBtn.addEventListener('click', function() {
        if (!isUser) {
            showErrorMessage('Only admins can create reports');
            return;
        }

        const title = document.getElementById('title').value;
        const details = document.getElementById('details').value;

        if (!title || !details) {
            showErrorMessage('Please fill in all required fields');
            return;
        }

        const reportData = {
            title: title,
            details: details
        };

        fetch('/api/reports', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(reportData)
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => { throw new Error(err.message || 'Network response was not ok'); });
                }
                return response.json();
            })
            .then(data => {
                addReportModal.hide();
                reportForm.reset();
                showSuccessMessage('Report saved successfully!');
                fetchReports();
            })
            .catch(error => {
                console.error('Error:', error);
                showErrorMessage(error.message || 'Error saving report');
            });
    });

    // Function to fetch and display reports
    function fetchReports() {
        fetch('/api/reports', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(reports => {
                const reportsList = document.getElementById('reportsList');

                if (reports.length === 0) {
                    reportsList.innerHTML = `
                    <div class="empty-state">
                        <i class="fas fa-flag fa-4x"></i>
                        <h4>No reports found</h4>
                        ${isUser ? '<p>Click the "Add Report" button to create your first report</p>' : ''}
                    </div>
                `;
                    return;
                }

                let html = '';
                reports.forEach(report => {
                    const date = new Date(report.createdAt).toLocaleString();
                    html += `
                    <div class="card report-card mb-3">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <h5 class="card-title">${report.title}</h5>
                                    <p class="card-text">${report.details}</p>
                                    <div class="d-flex align-items-center">
                                        <small class="text-muted me-3">
                                            <i class="fas fa-user me-1"></i> ${report.user.username}
                                        </small>
                                        <small class="text-muted">
                                            <i class="fas fa-clock me-1"></i> ${date}
                                        </small>
                                    </div>
                                </div>
                                ${isUser ? `
                                <button class="btn btn-sm btn-danger delete-btn" data-id="${report.id}">
                                    <i class="fas fa-trash"></i>
                                </button>
                                ` : ''}
                            </div>
                        </div>
                    </div>
                `;
                });

                reportsList.innerHTML = html;

                if (isUser) {
                    document.querySelectorAll('.delete-btn').forEach(btn => {
                        btn.addEventListener('click', function() {
                            reportToDeleteId = this.getAttribute('data-id');
                            deleteConfirmationModal.show();
                        });
                    });
                }
            })
            .catch(error => {
                console.error('Error:', error);
                document.getElementById('reportsList').innerHTML = `
                <div class="alert alert-danger">
                    <i class="fas fa-exclamation-triangle me-2"></i>
                    Error loading reports: ${error.message}
                </div>
            `;
            });
    }

    // Confirm delete button handler
    confirmDeleteButton.addEventListener('click', function() {
        if (!reportToDeleteId || !isUser) return;

        fetch(`/api/reports/${reportToDeleteId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => { throw new Error(err.message || 'Network response was not ok'); });
                }
                fetchReports();
                showSuccessMessage('Report deleted successfully');
                deleteConfirmationModal.hide();
            })
            .catch(error => {
                console.error('Error:', error);
                showErrorMessage(error.message || 'Error deleting report');
            })
            .finally(() => {
                reportToDeleteId = null;
            });
    });

    // Initialize the page
    init();
});