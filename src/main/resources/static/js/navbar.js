// Check user role and adjust navigation
document.addEventListener('DOMContentLoaded', function () {
    const userRole = getUserRole();


    // Get all admin links
    const adminLinks = document.querySelectorAll('.admin-link');

    if (userRole === 'USER') {
        // Hide all admin links
        adminLinks.forEach(link => {
            link.style.display = 'none';
        });
    } else {
        // Show all admin links
        adminLinks.forEach(link => {
            link.style.display = 'block';
        });
    }

    // Display username in navbar
    try {
        const user = getUserInfo();
        if (user && user.username) {
            document.getElementById('usernameDisplay').textContent = user.username;
        }
    } catch (e) {
        console.error('Error loading user info:', e);
    }
});