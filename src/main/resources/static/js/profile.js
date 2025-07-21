document.addEventListener('DOMContentLoaded', function() {
    // Load user data
    const user = getUserInfo();

    // Set profile information
    document.getElementById('userName').textContent = `${user.firstName} ${user.lastName}`;
    document.getElementById('userEmail').textContent = user.email;
    document.getElementById('userRole').textContent = user.role;

    // Set form values
    document.getElementById('firstName').value = user.firstName || '';
    document.getElementById('lastName').value = user.lastName || '';
    document.getElementById('email').value = user.email || '';

    // Set profile picture
    const profilePicture = document.getElementById('profilePicture');
    const defaultPictureUrl = `https://ui-avatars.com/api/?name=${user.firstName}+${user.lastName}&background=6c5ce7&color=fff&size=150`;
    profilePicture.src = user.pictureUrl || defaultPictureUrl;

    // Set modal profile picture
    document.getElementById('currentProfilePicture').src = user.pictureUrl || defaultPictureUrl.replace('size=150', 'size=200');

    // Initialize Bootstrap elements
    const profileTabs = new bootstrap.Tab(document.getElementById('info-tab'));
    const changePictureModal = new bootstrap.Modal(document.getElementById('changePictureModal'));
    const successToast = new bootstrap.Toast(document.getElementById('successToast'));
    const errorToast = new bootstrap.Toast(document.getElementById('errorToast'));

    // Event listeners
    document.getElementById('changePictureBtn').addEventListener('click', function() {
        changePictureModal.show();
    });

    document.getElementById('profileImageUpload').addEventListener('change', function(e) {
        if (e.target.files && e.target.files[0]) {
            const reader = new FileReader();
            reader.onload = function(event) {
                document.getElementById('currentProfilePicture').src = event.target.result;
            };
            reader.readAsDataURL(e.target.files[0]);
        }
    });

    document.getElementById('saveProfilePicture').addEventListener('click', async function() {
        const fileInput = document.getElementById('profileImageUpload');
        if (!fileInput.files || !fileInput.files[0]) {
            document.getElementById('errorMessage').textContent = 'Please select an image to upload.';
            errorToast.show();
            return;
        }

        const formData = new FormData();
        formData.append('profilePicture', fileInput.files[0]);

        try {
            const response = await fetch('/api/users/profilePicture', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`
                },
                body: formData
            });

            // Handle both JSON and plain text responses
            let result;
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                result = await response.json();
            } else {
                const text = await response.text();
                result = { profilePictureUrl: text };
            }

            if (!response.ok) {
                throw new Error('Failed to upload profile picture');
            }

            profilePicture.src = result.profilePictureUrl || document.getElementById('currentProfilePicture').src;

            document.getElementById('successMessage').textContent = 'Profile picture updated successfully! Now you are logging out';
            successToast.show();
            setTimeout(() => {
                window.location.href = '/logout';
            }, 2000);
            changePictureModal.hide();
        } catch (error) {
            document.getElementById('errorMessage').textContent = 'Error uploading profile picture. Please try again.';
            errorToast.show();
            console.error('Error:', error);
        }
    });

    // Profile form submission handler
    document.getElementById('profileForm').addEventListener('submit', async function(e) {
        e.preventDefault();

        // Show loading state
        const saveBtn = document.getElementById('saveProfileChanges');
        const originalBtnText = saveBtn.innerHTML;
        saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Saving...';
        saveBtn.disabled = true;

        const updatedUser = {
            firstName: document.getElementById('firstName').value.trim(),
            lastName: document.getElementById('lastName').value.trim(),
            email: document.getElementById('email').value.trim()
        };

        // Basic client-side validation
        if (!updatedUser.firstName || !updatedUser.lastName || !updatedUser.email) {
            document.getElementById('errorMessage').textContent = 'All fields are required';
            errorToast.show();
            saveBtn.innerHTML = originalBtnText;
            saveBtn.disabled = false;
            return;
        }

        try {
            const response = await fetch('/api/users/changeInfo', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`
                },
                body: JSON.stringify(updatedUser)
            });

            // Handle both JSON and plain text responses
            let data;
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            } else {
                const text = await response.text();
                data = { message: text };
            }

            if (!response.ok) {
                throw new Error(data.message || 'Failed to update profile information');
            }

            // Update displayed info
            document.getElementById('userName').textContent = `${updatedUser.firstName} ${updatedUser.lastName}`;
            document.getElementById('userEmail').textContent = updatedUser.email;

            // Update default avatar if using generated one
            if (profilePicture.src.includes('ui-avatars.com')) {
                const newDefaultUrl = `https://ui-avatars.com/api/?name=${updatedUser.firstName}+${updatedUser.lastName}&background=6c5ce7&color=fff&size=150`;
                profilePicture.src = newDefaultUrl;
            }

            // Show success message
            document.getElementById('successMessage').textContent = data.message || 'Profile updated successfully! you are logging out';
            successToast.show();
            setTimeout(() => {
                window.location.href = '/logout';
            }, 2000);

            // Update local user info if needed
            if (typeof updateUserInfo === 'function') {
                updateUserInfo(updatedUser);
            }

        } catch (error) {
            console.error('Error:', error);
            document.getElementById('errorMessage').textContent = error.message || 'Error updating profile. Please try again.';
            errorToast.show();

            // Reset form to original values
            document.getElementById('firstName').value = user.firstName || '';
            document.getElementById('lastName').value = user.lastName || '';
            document.getElementById('email').value = user.email || '';
        } finally {
            saveBtn.innerHTML = originalBtnText;
            saveBtn.disabled = false;
        }
    });

    document.getElementById('cancelProfileChanges').addEventListener('click', function() {
        // Reset form to original values
        document.getElementById('firstName').value = user.firstName || '';
        document.getElementById('lastName').value = user.lastName || '';
        document.getElementById('email').value = user.email || '';
    });

    document.getElementById('passwordForm').addEventListener('submit', async function(e) {
        e.preventDefault();

        const currentPassword = document.getElementById('currentPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        // Simple validation
        if (newPassword !== confirmPassword) {
            document.getElementById('errorMessage').textContent = 'New passwords and confirm password do not match!';
            errorToast.show();
            return;
        }

        if (newPassword.length < 8) {
            document.getElementById('errorMessage').textContent = 'Password must be at least 8 characters long!';
            errorToast.show();
            return;
        }

        // Show loading state
        const saveBtn = document.querySelector('#passwordForm button[type="submit"]');
        const originalBtnText = saveBtn.innerHTML;
        saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Saving...';
        saveBtn.disabled = true;

        try {
            const response = await fetch('/api/users/changePassword', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`
                },
                body: JSON.stringify({
                    currentPassword,
                    newPassword
                })
            });

            // Handle both JSON and plain text responses
            let data;
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            } else {
                const text = await response.text();
                data = { message: text };
            }

            if (!response.ok) {
                throw new Error(data.message || 'Failed to change password');
            }

            document.getElementById('successMessage').textContent = data.message || 'Password changed successfully!';
            successToast.show();

            // Reset form
            this.reset();
        } catch (error) {
            document.getElementById('errorMessage').textContent = error.message || 'Error changing password. Please try again.';
            errorToast.show();
            console.error('Error:', error);
        } finally {
            saveBtn.innerHTML = originalBtnText;
            saveBtn.disabled = false;
        }
    });

    document.getElementById('logoutBtn').addEventListener('click', function() {
        localStorage.removeItem('jwtToken');
        window.location.href = '/login.html';
    });
});