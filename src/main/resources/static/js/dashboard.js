// Global variables
let currentWeatherData = null;
let allItems = [];
let currentPage = 0;
let itemsPerPage = 12; // Default to 12 items per page
let totalItems = 0;

// Check authentication and load data on page load
document.addEventListener('DOMContentLoaded', function () {
    const token = localStorage.getItem('jwtToken');
    const username = localStorage.getItem('username');

    if (!token) {
        window.location.href = '/page/login';
        return;
    }

    if (username) {
        const usernameElement = document.getElementById('username');
        if (usernameElement) {
            usernameElement.textContent = username;
        }
    }

    // Load wardrobe items
    loadAllItems();

    // Load weather data automatically
    loadWeatherData();

    // Set up items per page dropdown
    document.getElementById('itemsPerPage').addEventListener('change', function() {
        itemsPerPage = parseInt(this.value);
        currentPage = 0; // Reset to first page when changing page size
        const searchTerm = document.getElementById('searchInput').value;
        if (searchTerm.trim()) {
            searchItems();
        } else {
            loadAllItems();
        }
    });
});

// Weather functions
function refreshWeather() {
    document.getElementById('weatherInfo').style.display = 'block';
    document.getElementById('weatherContainer').style.display = 'none';
    document.getElementById('error').style.display = 'none';
    loadWeatherData();
}

async function loadWeatherData() {
    try {
        document.getElementById('loading').style.display = 'block';
        document.getElementById('error').style.display = 'none';

        const weatherData = await getLocationAndWeather();
        currentWeatherData = weatherData;
        localStorage.setItem("currentWeatherData", JSON.stringify(weatherData));
        document.getElementById('loading').style.display = 'none';
        document.getElementById('weatherInfo').style.display = 'block';
        document.getElementById('weatherContainer').style.display = 'flex';
    } catch (error) {
        document.getElementById('loading').style.display = 'none';
        // document.getElementById('error').textContent = error.message;
        // document.getElementById('error').style.display = 'block';
    }
}

async function getLocationAndWeather() {
    return new Promise((resolve, reject) => {
        if (navigator.geolocation) {
            const options = {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 300000
            };

            navigator.geolocation.getCurrentPosition(
                async (position) => {
                    try {
                        const lat = position.coords.latitude;
                        const lon = position.coords.longitude;
                        const accuracy = position.coords.accuracy;

                        // Try to get approximate address
                        try {
                            const address = await getApproximateAddress(lat, lon);
                            document.getElementById('address').textContent = address || "Address not available";
                        } catch (error) {
                            document.getElementById('address').textContent = "Address lookup failed";
                        }

                        // Get weather data
                        const weatherData = await fetchWeather(lat, lon);
                        resolve(weatherData.current_weather);
                    } catch (error) {
                        reject(error);
                    }
                },
                (error) => {
                    let errorMessage = 'Error getting location: ';
                    switch (error.code) {
                        case error.PERMISSION_DENIED:
                            errorMessage += "User denied the request for Geolocation.";
                            break;
                        case error.POSITION_UNAVAILABLE:
                            errorMessage += "Location information is unavailable.";
                            break;
                        case error.TIMEOUT:
                            errorMessage += "The request to get user location timed out.";
                            break;
                        default:
                            errorMessage += "An unknown error occurred.";
                            break;
                    }
                    reject(new Error(errorMessage));
                },
                options
            );
        } else {
            reject(new Error("Geolocation is not supported by this browser."));
        }
    });
}

async function getApproximateAddress(lat, lon) {
    const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}&zoom=10`);
    const data = await response.json();
    if (data.address) {
        const addressParts = [];
        if (data.address.district) addressParts.push(data.address.district);
        if (data.address.town) addressParts.push(data.address.town);
        if (data.address.village) addressParts.push(data.address.village);
        if (data.address.county) addressParts.push(data.address.county);
        if (data.address.state) addressParts.push(data.address.state);
        if (data.address.country) addressParts.push(data.address.country);
        return addressParts.join(', ');
    }
    return null;
}

async function fetchWeather(lat, lon) {
    try {
        const apiUrl = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current_weather=true`;
        const response = await fetch(apiUrl);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        displayWeather(data);
        return data;
    } catch (error) {
        throw error;
    }
}

function displayWeather(weatherData) {
    if (weatherData && weatherData.current_weather) {
        const current = weatherData.current_weather;
        const weatherIcon = document.getElementById('weatherIcon');

        // Set appropriate icon based on weather condition
        const weatherCode = current.weathercode;
        if (weatherCode === 0) {
            weatherIcon.className = 'fas fa-sun fa-3x me-3 text-warning';
        } else if (weatherCode === 1 || weatherCode === 2) {
            weatherIcon.className = 'fas fa-cloud-sun fa-3x me-3 text-secondary';
        } else if (weatherCode === 3) {
            weatherIcon.className = 'fas fa-cloud fa-3x me-3 text-secondary';
        } else if (weatherCode >= 45 && weatherCode <= 48) {
            weatherIcon.className = 'fas fa-smog fa-3x me-3 text-secondary';
        } else if (weatherCode >= 51 && weatherCode <= 67) {
            weatherIcon.className = 'fas fa-cloud-rain fa-3x me-3 text-primary';
        } else if (weatherCode >= 71 && weatherCode <= 86) {
            weatherIcon.className = 'fas fa-snowflake fa-3x me-3 text-info';
        } else if (weatherCode >= 95 && weatherCode <= 99) {
            weatherIcon.className = 'fas fa-bolt fa-3x me-3 text-warning';
        }

        document.getElementById('temperature').textContent = `${current.temperature}°C`;
        document.getElementById('windSpeed').textContent = `${current.windspeed} km/h`;
        document.getElementById('windDirection').textContent = `${current.winddirection}°`;

        const weatherCondition = getWeatherCondition(current.weathercode);
        document.getElementById('weatherCondition').textContent = weatherCondition;

        document.getElementById('weatherContainer').style.display = 'flex';
    } else {
        throw new Error("No weather data available.");
    }
}

function getWeatherCondition(code) {
    const weatherCodes = {
        0: 'Clear sky', 1: 'Mainly clear', 2: 'Partly cloudy', 3: 'Overcast',
        45: 'Fog', 48: 'Depositing rime fog', 51: 'Light drizzle', 53: 'Moderate drizzle',
        55: 'Dense drizzle', 56: 'Light freezing drizzle', 57: 'Dense freezing drizzle',
        61: 'Slight rain', 63: 'Moderate rain', 65: 'Heavy rain',
        66: 'Light freezing rain', 67: 'Heavy freezing rain', 71: 'Slight snow fall',
        73: 'Moderate snow fall', 75: 'Heavy snow fall', 77: 'Snow grains',
        80: 'Slight rain showers', 81: 'Moderate rain showers', 82: 'Violent rain showers',
        85: 'Slight snow showers', 86: 'Heavy snow showers', 95: 'Thunderstorm',
        96: 'Thunderstorm with slight hail', 99: 'Thunderstorm with heavy hail'
    };
    return weatherCodes[code] || 'Unknown weather condition';
}

// Wardrobe functions
async function loadAllItems() {
    await loadItems(`/api/wardrobe/items?page=${currentPage}&size=${itemsPerPage}`);
}

async function searchItems() {
    const searchTerm = document.getElementById('searchInput').value;
    if (searchTerm.trim()) {
        await loadItems(`/api/wardrobe/items/search?q=${encodeURIComponent(searchTerm)}&page=${currentPage}&size=${itemsPerPage}`);
    } else {
        await loadAllItems();
    }
}

async function loadItems(url) {
    const token = localStorage.getItem('jwtToken');
    const loadingDiv = document.getElementById('loadingDiv');
    const emptyDiv = document.getElementById('emptyDiv');
    const itemsContainer = document.getElementById('itemsContainer');

    loadingDiv.style.display = 'block';
    emptyDiv.style.display = 'none';
    itemsContainer.style.display = 'none';

    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            if (response.status === 401) {
                logout();
                return;
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        loadingDiv.style.display = 'none';

        // Handle both paginated and non-paginated responses
        if (data.content) {
            // Paginated response
            allItems = data.content;
            totalItems = data.totalElements;
            currentPage = data.number;
            updatePaginationControls();
        } else {
            // Non-paginated response (for search)
            allItems = Array.isArray(data) ? data : [data];
            totalItems = allItems.length;
            updatePaginationControls();
        }

        if (!allItems || allItems.length === 0) {
            emptyDiv.style.display = 'block';
        } else {
            displayItems(allItems);
        }
    } catch (error) {
        console.error('Error loading items:', error);
        loadingDiv.style.display = 'none';
        emptyDiv.style.display = 'block';
    }
}

function displayItems(items) {
    const itemsContainer = document.getElementById('itemsContainer');
    itemsContainer.innerHTML = '';

    items.forEach(item => {
        const itemHtml = `
            <div class="col-md-4 col-lg-3 mb-4">
                <div class="card h-100 item-card">
                    <div class="position-relative">
                        ${item.imageUrl ?
            `<img src="${item.imageUrl}" class="card-img-top item-image-dashboard" alt="Clothing item">` :
            `<div class="item-image-placeholder">
                                <i class="fas fa-tshirt"></i>
                            </div>`}
                        <span class="badge bg-primary category-badge">${item.category.name}</span>
                    </div>
                    <div class="card-body">
                        <h5 class="card-title">${item.name}</h5>
                        <p class="card-text text-muted">${item.description || ''}</p>
                        <div class="d-flex justify-content-between align-items-center">
                            <small class="text-muted">
                                <i class="fas fa-calendar me-1"></i>
                                ${item.lastUsedDate ? new Date(item.lastUsedDate).toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric'
        }) : 'Never used'}
                            </small>
                            <small class="text-muted">
                                <i class="fas fa-times me-1"></i>
                                ${item.usageCount || 0} uses
                            </small>
                        </div>
                    </div>
                    <div class="card-footer bg-transparent card-footer-buttons">
                        <button class="btn btn-outline-success btn-sm" onclick="markAsUsed(${item.id})">
                            <i class="fas fa-check me-1"></i>Used
                        </button>
                    </div>
                </div>
            </div>
        `;
        itemsContainer.innerHTML += itemHtml;
    });

    itemsContainer.style.display = 'flex';
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
    const searchTerm = document.getElementById('searchInput').value;
    if (searchTerm.trim()) {
        searchItems();
    } else {
        loadAllItems();
    }
}

async function markAsUsed(itemId) {
    if (confirm('Mark this item as used today?')) {
        const token = localStorage.getItem('jwtToken');
        try {
            const response = await fetch(`/api/wardrobe/items/${itemId}/use`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                loadAllItems();
            } else if (response.status === 401) {
                logout();
            }
        } catch (error) {
            console.error('Error marking item as used:', error);
        }
    }
}