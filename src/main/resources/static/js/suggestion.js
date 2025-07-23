
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

    // Global variables
    let outfitToDelete = null;
    let clothingItems = [];

    // Function to get weather condition from weather code
    function getWeatherCondition(weatherCode) {
    const weatherMap = {
    0: 'CLEAR',
    1: 'CLEAR',
    2: 'PARTLY_CLOUDY',
    3: 'CLOUDY',
    45: 'FOG',
    48: 'FOG',
    51: 'DRIZZLE',
    53: 'DRIZZLE',
    55: 'DRIZZLE',
    56: 'FREEZING_DRIZZLE',
    57: 'FREEZING_DRIZZLE',
    61: 'RAIN',
    63: 'RAIN',
    65: 'RAIN',
    66: 'FREEZING_RAIN',
    67: 'FREEZING_RAIN',
    71: 'SNOW',
    73: 'SNOW',
    75: 'SNOW',
    77: 'SNOW',
    80: 'RAIN_SHOWERS',
    81: 'RAIN_SHOWERS',
    82: 'RAIN_SHOWERS',
    85: 'SNOW_SHOWERS',
    86: 'SNOW_SHOWERS',
    95: 'THUNDERSTORM',
    96: 'THUNDERSTORM',
    99: 'THUNDERSTORM'
};
    return weatherMap[weatherCode] || 'UNKNOWN';
}

    // Function to generate outfit suggestion
    async function generateOutfit() {
    const occasion = document.getElementById('occasion').value;
    const season = document.getElementById('season').value;
    const considerWeather = document.getElementById('weather').checked;

    if (!occasion || !season) {
    showErrorMessage('Please select both occasion and season');
    return;
}

    try {
    // Create outfit data object
    const outfitData = {
    occasion: occasion,
    season: season,
    considerWeather: considerWeather
};

    if (considerWeather) {
    const currentWeatherData = JSON.parse(localStorage.getItem('currentWeatherData'));
    if (!currentWeatherData) {
    showErrorMessage('No weather data available. Please refresh weather data first.');
    return;
}
    outfitData.weatherDetails = {
    temperature: currentWeatherData.temperature,
    weatherCondition: getWeatherCondition(currentWeatherData.weathercode)
};
}

    // Show loading state
    const generateBtn = document.getElementById('generateOutfitBtn');
    generateBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i> Generating...';
    generateBtn.disabled = true;

    // Call API to generate suggestion
    const response = await fetch('/api/wardrobe/suggest-outfit', {
    method: 'POST',
    headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`
},
    body: JSON.stringify(outfitData)
});

    if (!response.ok) {
    // Try to parse the error response as JSON
    const errorResponse = await response.json();
    if (errorResponse && errorResponse.message) {
    throw new Error(errorResponse.message);
} else {
    throw new Error('Failed to generate outfit suggestion');
}
}

    const items = await response.json();

    // Update the table with the generated items
    updateTable(items);

    // Close the modal
    const modal = bootstrap.Modal.getInstance(document.getElementById('outfitModal'));
    modal.hide();

    // Show success message
    showSuccessMessage('Clothing items suggestions generated successfully!');

} catch (error) {
    console.error('Error generating outfit:', error);
    showErrorMessage(error.message); // This will now show the custom message
} finally {
    // Reset button state
    const generateBtn = document.getElementById('generateOutfitBtn');
    generateBtn.innerHTML = '<i class="fas fa-magic me-2"></i>Generate';
    generateBtn.disabled = false;
}
}

    // Function to update the items table
    function updateTable(items) {
    const tbody = document.getElementById('itemsTableBody');
    tbody.innerHTML = '';

    if (!items || items.length === 0) {
    const tr = document.createElement('tr');
    tr.innerHTML = `
                <td colspan="8" class="text-center py-4">
                    <i class="fas fa-box-open fa-2x mb-2" style="color: var(--primary-light);"></i>
                    <p class="mb-2">No items found</p>
                    <button type="button" class="btn btn-primary btn-sm" data-bs-toggle="modal" data-bs-target="#outfitModal">
                        <i class="fas fa-magic me-2"></i>Generate Your First Suggestion
                    </button>
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
                    <img src="${item.imageUrl || 'https://via.placeholder.com/50?text=No+Image'}"
                         alt="${item.name}"
                         class="img-thumbnail"
                         style="width: 50px; height: 50px; object-fit: cover;"
                         onerror="this.src='https://via.placeholder.com/50?text=No+Image'">
                </td>
                <td>${item.name}</td>
                <td>${item.categoryName || 'N/A'}</td>
                <td>${item.color || 'N/A'}</td>
                <td><span class="badge badge-season">${item.season || 'N/A'}</span></td>
                <td><span class="badge badge-occasion">${item.occasion || 'N/A'}</span></td>
                <td>${item.usageCount || 0}</td>
                <td>
                    <button class="btn btn-success btn-sm mark-used-btn" title="Mark as Used" data-item-id="${item.id}">
                        <i class="fas fa-check-circle me-1"></i> Mark as Used
                    </button>
                </td>
            `;
    tbody.appendChild(tr);
});

    // Add event listeners for mark as used buttons
    document.querySelectorAll('.mark-used-btn').forEach(button => {
    button.addEventListener('click', function() {
    const itemId = this.getAttribute('data-item-id');
    markItemAsUsed(itemId);
});
});
}

    // Function to mark item as used
    async function markItemAsUsed(itemId) {
    try {
    const response = await fetch(`/api/wardrobe/items/${itemId}/use`, {
    method: 'POST',
    headers: {
    'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`
}
});

    if (!response.ok) {
    throw new Error('Failed to mark item as used');
}

    // Update the usage count in the UI
    const row = document.querySelector(`tr[data-item-id="${itemId}"]`);
    if (row) {
    const usageCountCell = row.cells[6];
    const currentCount = parseInt(usageCountCell.textContent) || 0;
    usageCountCell.textContent = currentCount + 1;
}

    showSuccessMessage('Item marked as used successfully!');

} catch (error) {
    console.error('Error marking item as used:', error);
    showErrorMessage('Failed to mark item as used: ' + error.message);
}
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

    // Event listeners
    document.addEventListener('DOMContentLoaded', function() {
    // Load navbar
    fetch('/page/navbar')
        .then(response => response.text())
        .then(data => {
            document.getElementById('navbar').innerHTML = data;
        })
        .catch(error => console.error('Error loading navbar:', error));

    // Generate outfit button handler
    document.getElementById('generateOutfitBtn').addEventListener('click', generateOutfit);

    // Initialize with empty table
    updateTable([]);
});