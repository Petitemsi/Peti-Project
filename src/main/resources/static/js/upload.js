// Initialize toast notifications
const successToast = new bootstrap.Toast(document.getElementById('successToast'));
const errorToast = new bootstrap.Toast(document.getElementById('errorToast'));

// Global variable to store all items
let allItems = [];
let currentPage = 0;
let itemsPerPage = 10;
let totalItems = 0;

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

// Function to fetch categories from backend
async function fetchCategories() {
    const loadingSpinner = document.getElementById('loadingSpinner');
    const errorMessage = document.getElementById('errorMessage');
    const categorySelect = document.getElementById('itemCategory');
    const filterCategorySelect = document.getElementById('filterCategory');
    const editCategorySelect = document.getElementById('editItemCategory');

    try {
        loadingSpinner.style.display = 'block';
        errorMessage.style.display = 'none';

        const response = await fetch('/api/categories', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const categories = await response.json();

        // Clear existing options (except the first default option)
        categorySelect.innerHTML = '<option value="">Select category</option>';
        filterCategorySelect.innerHTML = '<option value="">All Categories</option>';
        editCategorySelect.innerHTML = '<option value="">Select category</option>';

        // Populate category dropdowns
        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.id || category.name;
            option.textContent = category.name;
            categorySelect.appendChild(option);

            // Also add to filter dropdown
            const filterOption = document.createElement('option');
            filterOption.value = category.name;
            filterOption.textContent = category.name;
            filterCategorySelect.appendChild(filterOption);

            // Add to edit modal dropdown
            const editOption = document.createElement('option');
            editOption.value = category.id || category.name;
            editOption.textContent = category.name;
            editCategorySelect.appendChild(editOption);
        });

        loadingSpinner.style.display = 'none';

    } catch (error) {
        console.error('Error fetching categories:', error);
        loadingSpinner.style.display = 'none';
        errorMessage.style.display = 'block';
        errorMessage.textContent = 'Failed to load categories. Please try again later.';

        // Fallback to default categories
        const defaultCategories = [
            'Shirts', 'Pants', 'Dresses', 'Shoes', 'Jackets', 'Accessories',
            'Underwear', 'Socks', 'Sweaters', 'Shorts', 'Skirts', 'Suits'
        ];

        categorySelect.innerHTML = '<option value="">Select category</option>';
        filterCategorySelect.innerHTML = '<option value="">All Categories</option>';
        editCategorySelect.innerHTML = '<option value="">Select category</option>';

        defaultCategories.forEach(category => {
            const option = document.createElement('option');
            option.value = category;
            option.textContent = category;
            categorySelect.appendChild(option);

            const filterOption = document.createElement('option');
            filterOption.value = category;
            filterOption.textContent = category;
            filterCategorySelect.appendChild(filterOption);

            const editOption = document.createElement('option');
            editOption.value = category;
            editOption.textContent = category;
            editCategorySelect.appendChild(editOption);
        });
    }
}

// Function to add clothing item
async function addClothingItem(formData) {
    const errorMessage = document.getElementById('errorMessage');
    const loadingSpinner = document.getElementById('loadingSpinner');
    const submitButton = document.getElementById('submitButton');
    const originalButtonText = submitButton.innerHTML;

    try {
        loadingSpinner.style.display = 'block';
        errorMessage.style.display = 'none';
        submitButton.disabled = true;
        submitButton.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Saving...';

        const response = await fetch('/api/wardrobe/add', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`
            },
            body: formData
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Failed to add item');
        }

        const result = await response.json();
        loadingSpinner.style.display = 'none';
        submitButton.innerHTML = originalButtonText;
        submitButton.disabled = false;

        return result;
    } catch (error) {
        console.error('Error adding clothing item:', error);
        loadingSpinner.style.display = 'none';
        errorMessage.style.display = 'block';
        errorMessage.textContent = error.message || 'Failed to add item. Please try again.';
        submitButton.innerHTML = originalButtonText;
        submitButton.disabled = false;
        throw error;
    }
}

// Function to fetch clothing items with pagination
async function fetchClothingItems(page = 0) {
    try {
        const response = await fetch(`/api/wardrobe/items?page=${page}&size=${itemsPerPage}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch clothing items');
        }

        const data = await response.json();
        allItems = data.content;
        totalItems = data.totalElements;
        currentPage = data.number;

        updatePaginationControls();
        return data.content;
    } catch (error) {
        console.error('Error fetching clothing items:', error);
        showErrorMessage('Failed to load clothing items. Please try again later.');
        throw error;
    }
}

function updatePaginationControls() {
    const paginationContainer = document.getElementById('paginationControls');
    if (!paginationContainer) return;

    const totalPages = Math.ceil(totalItems / itemsPerPage);

    let paginationHTML = `
        <div class="pagination-info">
            Showing ${(currentPage * itemsPerPage) + 1} to 
            ${Math.min((currentPage + 1) * itemsPerPage, totalItems)} of ${totalItems} items
        </div>
        <ul class="pagination">
    `;

    // Previous button
    paginationHTML += `
        <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${currentPage - 1})" aria-label="Previous">
                <span aria-hidden="true">&laquo;</span>
            </a>
        </li>
    `;

    // Page numbers
    const maxVisiblePages = 5;
    let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);

    if (endPage - startPage + 1 < maxVisiblePages) {
        startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }

    if (startPage > 0) {
        paginationHTML += `<li class="page-item"><a class="page-link" href="#" onclick="changePage(0)">1</a></li>`;
        if (startPage > 1) {
            paginationHTML += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        paginationHTML += `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#" onclick="changePage(${i})">${i + 1}</a>
            </li>
        `;
    }

    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) {
            paginationHTML += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
        }
        paginationHTML += `<li class="page-item"><a class="page-link" href="#" onclick="changePage(${totalPages - 1})">${totalPages}</a></li>`;
    }

    // Next button
    paginationHTML += `
        <li class="page-item ${currentPage >= totalPages - 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${currentPage + 1})" aria-label="Next">
                <span aria-hidden="true">&raquo;</span>
            </a>
        </li>
    `;

    paginationHTML += `</ul>`;
    paginationContainer.innerHTML = paginationHTML;
}

function changePage(newPage) {
    if (newPage < 0 || newPage >= Math.ceil(totalItems / itemsPerPage)) return;
    currentPage = newPage;
    loadClothingItems();
}

// Function to apply filters to items
function applyFilters() {
    const searchTerm = document.querySelector('input[placeholder="Search clothing items..."]').value.toLowerCase();
    const categoryFilter = document.getElementById('filterCategory').value;
    const seasonFilter = document.getElementById('filterSeason').value;
    const occasionFilter = document.getElementById('filterOccasion').value;

    // Filter items based on criteria
    const filteredItems = allItems.filter(item => {
        // Search term matching (name and description)
        const matchesSearch = !searchTerm ||
            item.name.toLowerCase().includes(searchTerm) ||
            (item.description && item.description.toLowerCase().includes(searchTerm));

        // Category filter
        const matchesCategory = !categoryFilter ||
            (item.category && item.category.name === categoryFilter);

        // Season filter
        const matchesSeason = !seasonFilter ||
            item.season === seasonFilter;

        // Occasion filter
        const matchesOccasion = !occasionFilter ||
            item.occasion === occasionFilter;

        return matchesSearch && matchesCategory && matchesSeason && matchesOccasion;
    });

    // Display filtered items
    updateTable(filteredItems);
}

// Function to update the clothing table
function updateTable(items) {
    const tbody = document.getElementById('clothingTableBody');
    tbody.innerHTML = '';

    if (items.length === 0) {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td colspan="8" class="text-center py-4">
                <i class="fas fa-box-open fa-2x mb-2" style="color: var(--primary-light);"></i>
                <p class="mb-0">No clothing items found matching your criteria</p>
            </td>
        `;
        tbody.appendChild(tr);
        return;
    }

    items.forEach(item => {
        const tr = document.createElement('tr');
        tr.dataset.itemId = item.id;
        tr.innerHTML = `
            <td>
                <img src="${item.imageUrl || 'https://via.placeholder.com/60x60?text=No+Image'}"
                     alt="${item.name}" class="item-image">
            </td>
            <td>
                <div class="fw-bold">${item.name}</div>
                <small class="text-muted">Added ${formatDate(item.createdAt)}</small>
            </td>
            <td><span class="badge badge-category">${item.category.name}</span></td>
            <td><span class="badge badge-occasion">${item.occasion || 'N/A'}</span></td>
            <td><span class="badge badge-season">${item.season || 'N/A'}</span></td>
            <td><span class="badge badge-color">${item.color || 'N/A'}</span></td>
            <td>
                <div class="text-truncate" style="max-width: 200px;">
                    ${item.description || 'No description'}
                </div>
            </td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-warning btn-sm edit-btn" title="Edit">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-danger btn-sm delete-btn" title="Delete">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
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

// Function to load clothing items
async function loadClothingItems() {
    try {
        await fetchClothingItems(currentPage);
        applyFilters();
    } catch (error) {
        console.error('Error loading clothing items:', error);
    }
}

// Function to delete an item
let itemToDelete = null;

async function deleteItem(itemId) {
    itemToDelete = itemId;
    const deleteModal = new bootstrap.Modal(document.getElementById('deleteConfirmationModal'));
    deleteModal.show();
}

// Function to edit an item
async function editItem(itemId) {
    try {
        // Fetch item details
        const response = await fetch(`/api/wardrobe/items/${itemId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch item details');
        }

        const item = await response.json();

        // Populate edit form
        document.getElementById('editItemId').value = item.id;
        document.getElementById('editItemName').value = item.name;
        document.getElementById('editItemCategory').value = item.category.name;
        document.getElementById('editItemColor').value = item.color || '';
        document.getElementById('editItemOccasion').value = item.occasion || '';
        document.getElementById('editItemSeason').value = item.season || '';
        document.getElementById('editItemDescription').value = item.description || '';

        // Set current image preview
        const currentImagePreview = document.getElementById('currentImagePreview');
        if (item.imageUrl) {
            currentImagePreview.src = item.imageUrl;
            document.getElementById('currentImageContainer').style.display = 'block';
        } else {
            document.getElementById('currentImageContainer').style.display = 'none';
        }

        // Show edit modal
        const editModal = new bootstrap.Modal(document.getElementById('editItemModal'));
        editModal.show();

    } catch (error) {
        console.error('Error fetching item for edit:', error);
        showErrorMessage('Failed to load item for editing: ' + error.message);
    }
}

// Function to save edited item
async function saveEditedItem(formData) {
    const errorMessage = document.getElementById('editErrorMessage');
    const loadingSpinner = document.getElementById('editLoadingSpinner');
    const submitButton = document.getElementById('editSubmitButton');
    const originalButtonText = submitButton.innerHTML;

    try {
        loadingSpinner.style.display = 'block';
        errorMessage.style.display = 'none';
        submitButton.disabled = true;
        submitButton.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Saving...';

        const itemId = document.getElementById('editItemId').value;
        const response = await fetch(`/api/wardrobe/items/${itemId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`
            },
            body: formData
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Failed to update item');
        }

        const result = await response.json();
        loadingSpinner.style.display = 'none';
        submitButton.innerHTML = originalButtonText;
        submitButton.disabled = false;

        // Update the allItems array with the edited item
        const index = allItems.findIndex(item => item.id === itemId);
        if (index !== -1) {
            allItems[index] = result;
        }

        return result;
    } catch (error) {
        console.error('Error updating clothing item:', error);
        loadingSpinner.style.display = 'none';
        errorMessage.style.display = 'block';
        errorMessage.textContent = error.message || 'Failed to update item. Please try again.';
        submitButton.innerHTML = originalButtonText;
        submitButton.disabled = false;
        throw error;
    }
}

// Event listeners
document.addEventListener('DOMContentLoaded', function() {
    // Load initial data
    fetchCategories();
    loadClothingItems();

    // Add form submission handler
    document.getElementById('addItemForm').addEventListener('submit', async function(e) {
        e.preventDefault();

        const form = e.target;
        const formData = new FormData(form);

        try {
            const result = await addClothingItem(formData);

            // Close modal and show success message
            const modal = bootstrap.Modal.getInstance(document.getElementById('addItemModal'));
            modal.hide();
            form.reset();

            // Reset to first page when adding new item
            currentPage = 0;
            await loadClothingItems();

            showSuccessMessage('Item added successfully!');
        } catch (error) {
            console.error('Form submission error:', error);
        }
    });

    // Edit form submission handler
    document.getElementById('editItemForm').addEventListener('submit', async function(e) {
        e.preventDefault();

        const form = e.target;
        const formData = new FormData(form);

        try {
            await saveEditedItem(formData);

            // Close modal and show success message
            const modal = bootstrap.Modal.getInstance(document.getElementById('editItemModal'));
            modal.hide();

            showSuccessMessage('Item updated successfully!');
            await loadClothingItems();
        } catch (error) {
            console.error('Edit form submission error:', error);
        }
    });

    // Confirm delete button handler
    document.getElementById('confirmDeleteButton').addEventListener('click', async function() {
        if (!itemToDelete) return;

        try {
            const response = await fetch(`/api/wardrobe/items/${itemToDelete}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error('Failed to delete item');
            }

            // Close the modal
            const deleteModal = bootstrap.Modal.getInstance(document.getElementById('deleteConfirmationModal'));
            deleteModal.hide();

            showSuccessMessage('Item deleted successfully');

            // Reset to first page if we deleted the last item on the current page
            if (allItems.length === 1 && currentPage > 0) {
                currentPage = 0;
            }

            await loadClothingItems();
        } catch (error) {
            console.error('Error deleting item:', error);
            showErrorMessage('Failed to delete item: ' + error.message);
        } finally {
            itemToDelete = null;
        }
    });

    // Search input handler - using debounce for better performance
    let searchTimeout;
    document.querySelector('input[placeholder="Search clothing items..."]').addEventListener('input', function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            applyFilters();
        }, 300);
    });

    // Filter change handlers
    document.getElementById('filterCategory').addEventListener('change', applyFilters);
    document.getElementById('filterSeason').addEventListener('change', applyFilters);
    document.getElementById('filterOccasion').addEventListener('change', applyFilters);

    // Delegate event listeners for dynamic content
    document.getElementById('clothingTableBody').addEventListener('click', function(e) {
        // Delete button handler
        if (e.target.closest('.delete-btn')) {
            e.preventDefault();
            const itemId = e.target.closest('tr').dataset.itemId;
            deleteItem(itemId);
        }

        // Edit button handler
        if (e.target.closest('.edit-btn')) {
            e.preventDefault();
            const itemId = e.target.closest('tr').dataset.itemId;
            editItem(itemId);
        }
    });

    // Items per page change handler
    document.getElementById('itemsPerPage').addEventListener('change', function() {
        itemsPerPage = parseInt(this.value);
        currentPage = 0; // Reset to first page when changing page size
        loadClothingItems();
    });
});

// Load categories when modal is opened
document.getElementById('addItemModal').addEventListener('show.bs.modal', function() {
    fetchCategories();
});