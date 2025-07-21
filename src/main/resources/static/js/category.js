const apiUrl = '/api/categories';
const successToast = new bootstrap.Toast(document.getElementById('successToast'));
const errorToast = new bootstrap.Toast(document.getElementById('errorToast'));
let categoryToDeleteId = null;

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

function openDialog(category = null) {
    document.getElementById('dialogBackdrop').classList.add('show');
    document.getElementById('categoryDialog').classList.add('show');
    document.getElementById('dialogTitle').textContent = category ? 'Edit Category' : 'Add Category';
    document.getElementById('categoryId').value = category ? category.id : '';
    document.getElementById('categoryName').value = category ? category.name : '';
    document.getElementById('categoryDescription').value = category ? category.description : '';
}

function closeDialog() {
    document.getElementById('dialogBackdrop').classList.remove('show');
    document.getElementById('categoryDialog').classList.remove('show');
    document.getElementById('categoryForm').reset();
}

async function fetchCategories() {
    try {
        const response = await fetch(`${apiUrl}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch categories');
        }

        const categories = await response.json();
        const tableBody = document.getElementById('categoryTableBody');

        if (categories.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="4" class="text-center py-4">
                        <i class="fas fa-box-open fa-2x mb-2" style="color: var(--primary-light);"></i>
                        <p class="mb-0">No categories found</p>
                    </td>
                </tr>
            `;
            return;
        }

        tableBody.innerHTML = '';
        categories.forEach(category => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${category.id}</td>
                <td>
                    <span class="badge badge-category">${category.name}</span>
                </td>
                <td>
                    <div class="text-truncate" style="max-width: 300px;">
                        ${category.description || 'No description'}
                    </div>
                </td>
                <td>
                    <div class="action-buttons">
                        <button class="btn btn-warning btn-sm" onclick='openDialog(${JSON.stringify(category)})' title="Edit">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-danger btn-sm" onclick="showDeleteConfirmation(${category.id})" title="Delete">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </td>
            `;
            tableBody.appendChild(row);
        });
    } catch (error) {
        console.error('Error fetching categories:', error);
        showErrorMessage('Failed to load categories. Please try again later.');
    }
}

async function saveCategory(event) {
    event.preventDefault();
    const id = document.getElementById('categoryId').value;
    const name = document.getElementById('categoryName').value.trim();
    const description = document.getElementById('categoryDescription').value.trim();

    if (!name) {
        showErrorMessage('Category name is required');
        return;
    }

    const category = {
        name,
        description
    };

    try {
        const method = id ? 'PUT' : 'POST';
        const url = id ? `${apiUrl}/${id}` : apiUrl;

        const response = await fetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`
            },
            body: JSON.stringify(category)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Failed to save category');
        }

        closeDialog();
        showSuccessMessage(`Category ${id ? 'updated' : 'added'} successfully!`);
        fetchCategories();
    } catch (error) {
        console.error('Error saving category:', error);
        showErrorMessage(error.message || 'Failed to save category. Please try again.');
    }
}

function showDeleteConfirmation(id) {
    categoryToDeleteId = id;
    const deleteModal = new bootstrap.Modal(document.getElementById('deleteConfirmationModal'));
    deleteModal.show();
}

async function deleteCategory() {
    if (!categoryToDeleteId) return;

    try {
        const response = await fetch(`${apiUrl}/${categoryToDeleteId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Failed to delete category');
        }

        showSuccessMessage('Category deleted successfully');
        fetchCategories();
    } catch (error) {
        console.error('Error deleting category:', error);
        showErrorMessage(error.message || 'Failed to delete category. Please try again.');
    } finally {
        const deleteModal = bootstrap.Modal.getInstance(document.getElementById('deleteConfirmationModal'));
        deleteModal.hide();
        categoryToDeleteId = null;
    }
}

document.addEventListener('DOMContentLoaded', function() {

    // Initialize the page
    fetchCategories();

    document.getElementById('categoryForm').addEventListener('submit', saveCategory);
    document.getElementById('confirmDeleteButton').addEventListener('click', deleteCategory);
});