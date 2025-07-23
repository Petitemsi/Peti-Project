    // Initialize toast notifications
    const successToast = new bootstrap.Toast(document.getElementById('successToast'));
    const errorToast = new bootstrap.Toast(document.getElementById('errorToast'));

    // Function to show success message
    function showSuccessMessage(message) {
    document.getElementById('successMessage').textContent = message;
    successToast.show();
}

    // Function to show error message
    function showErrorMessage(message) {
    document.getElementById('errorMessageToast').textContent = message;
    errorToast.show();
}

    // Function to fetch users based on tab
    async function fetchUsers(tab) {
    console.log(`Fetching users for tab: ${tab}`);
    let url;
    switch (tab) {
    case 'active':
    url = '/api/users/active';
    break;
    case 'locked':
    url = '/api/users/locked';
    break;
    case 'notApproved':
    url = '/api/users/not-approved';
    break;
    default:
    return;
}

    try {
    const response = await fetch(url, {
    method: 'GET',
    headers: {
    'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
    'Content-Type': 'application/json'
}
});

    if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
}

    const users = await response.json();
    renderUsers(tab, users);
} catch (error) {
    console.error('Error fetching users:', error);
    showErrorMessage('Failed to load users. Please try again later.');
}
}

    // Function to render users in the table
    function renderUsers(tab, users) {
    const tableBody = document.getElementById(`${tab}UsersTable`);
    tableBody.innerHTML = '';

    if (users.length === 0) {
    const tr = document.createElement('tr');
    tr.innerHTML = `
                <td colspan="${tab === 'active' ? 6 : 5}" class="text-center py-4">
                    <i class="fas fa-user-slash fa-2x mb-2" style="color: var(--primary-light);"></i>
                    <p class="mb-0">No ${tab} users found</p>
                </td>
            `;
    tableBody.appendChild(tr);
    return;
}

    users.forEach(user => {
    const row = document.createElement('tr');
    row.dataset.userId = user.id;

    if (tab === 'active') {
    // Active tab with Status column and delete icon
    const statusButton = user.active
    ? `<button class="btn btn-sm btn-warning" onclick="toggleActive(${user.id}, false)">
                           <i class="fas fa-pause-circle me-1"></i> Deactivate
                       </button>`
    : `<button class="btn btn-sm btn-success" onclick="toggleActive(${user.id}, true)">
                           <i class="fas fa-play-circle me-1"></i> Activate
                       </button>`;

    row.innerHTML = `
                    <td>${user.id}</td>
                    <td>
                        <div class="fw-bold">${user.username}</div>
                        <small class="text-muted">Registered ${formatDate(user.createdAt)}</small>
                    </td>
                    <td>${user.email}</td>
                    <td><span class="badge ${user.role === 'ADMIN' ? 'badge-category' : 'badge-season'}">${user.role}</span></td>
                    <td>${statusButton}</td>
                    <td>
                        <div class="action-buttons">
                            <button class="btn btn-danger btn-sm" onclick="deleteUser(${user.id})" title="Delete User">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </td>
                `;
} else {
    // Other tabs without Status column
    row.innerHTML = `
                    <td>${user.id}</td>
                    <td>
                        <div class="fw-bold">${user.username}</div>
                        <small class="text-muted">Registered ${formatDate(user.createdAt)}</small>
                    </td>
                    <td>${user.email}</td>
                    <td><span class="badge ${user.role === 'ADMIN' ? 'badge-category' : 'badge-season'}">${user.role}</span></td>
                    <td>
                        <div class="action-buttons">
                            ${tab === 'locked' ? `
                            <button class="btn btn-success btn-sm" onclick="unlockAccount(${user.id})">
                                <i class="fas fa-unlock me-1"></i> Unlock
                            </button>
                            ` : ''}
                            ${tab === 'notApproved' ? `
                            <button class="btn btn-primary btn-sm" onclick="toggleApproval(${user.id}, true)">
                                <i class="fas fa-check-circle me-1"></i> Approve
                            </button>
                            ` : ''}
                        </div>
                    </td>
                `;
}

    tableBody.appendChild(row);
});
}

    // Function to format date
    function formatDate(dateString) {
    if (!dateString) return 'recently';

    const date = new Date(dateString);
    const now = new Date();
    const diffInDays = Math.floor((now - date) / (1000 * 60 * 60 * 24));

    if (diffInDays === 0) return 'today';
    if (diffInDays === 1) return 'yesterday';
    if (diffInDays < 7) return `${diffInDays} days ago`;
    if (diffInDays < 30) return `${Math.floor(diffInDays / 7)} weeks ago`;
    return date.toLocaleDateString();
}

    // Functions to handle actions
    async function toggleActive(userId, isActive) {
    try {
    const response = await fetch(`/api/users/${userId}/toggle-active`, {
    method: 'POST',
    headers: {
    'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
    'Content-Type': 'application/json'
},
    body: JSON.stringify(isActive)
});

    if (!response.ok) {
    throw new Error('Failed to toggle active status');
}

    fetchUsers('active'); // Refresh active tab
    showSuccessMessage(`User ${isActive ? 'activated' : 'deactivated'} successfully`);
} catch (error) {
    console.error('Error toggling active status:', error);
    showErrorMessage(error.message || 'Failed to toggle active status');
}
}

    async function toggleApproval(userId, isApproved) {
    try {
    const response = await fetch(`/api/users/${userId}/toggle-approval`, {
    method: 'POST',
    headers: {
    'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
    'Content-Type': 'application/json'
},
    body: JSON.stringify(isApproved)
});

    if (!response.ok) {
    throw new Error('Failed to toggle approval status');
}

    fetchUsers('notApproved'); // Refresh not-approved tab
    showSuccessMessage(`User ${isApproved ? 'approved' : 'unapproved'} successfully`);
} catch (error) {
    console.error('Error toggling approval status:', error);
    showErrorMessage(error.message || 'Failed to toggle approval status');
}
}

    async function unlockAccount(userId) {
    try {
    const response = await fetch(`/api/users/${userId}/unlock`, {
    method: 'POST',
    headers: {
    'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
    'Content-Type': 'application/json'
}
});

    if (!response.ok) {
    throw new Error('Failed to unlock account');
}

    fetchUsers('locked'); // Refresh locked tab
    showSuccessMessage('User unlocked successfully');
} catch (error) {
    console.error('Error unlocking account:', error);
    showErrorMessage(error.message || 'Failed to unlock account');
}
}

    // New function to handle user deletion
    async function deleteUser(userId) {
    // Show confirmation dialog
    if (confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
    try {
    const response = await fetch(`/api/users/${userId}`, {
    method: 'DELETE',
    headers: {
    'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
    'Content-Type': 'application/json'
}
});

    if (!response.ok) {
    throw new Error('Failed to delete user');
}

    fetchUsers('active'); // Refresh active tab
    showSuccessMessage('User deleted successfully');
} catch (error) {
    console.error('Error deleting user:', error);
    showErrorMessage(error.message || 'Failed to delete user');
}
}
}

    // Event listener for tab changes
    document.getElementById('userTabs').addEventListener('shown.bs.tab', function (event) {
    const tab = event.target.id.split('-')[0]; // Get tab name (active, locked, not-approved)
    fetchUsers(tab);
});

    // Fetch active users on page load
    document.addEventListener('DOMContentLoaded', function() {
    fetchUsers('active');
});