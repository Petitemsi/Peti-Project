// Function to fetch and display item count
async function getItemCount() {
    try {
        const response = await fetch('/api/wardrobe/items/count');
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const itemCount = await response.json();
        document.getElementById('totalItemsCount').textContent = itemCount;
    } catch (error) {
        console.error("Error fetching item count:", error);
        document.getElementById('totalItemsCount').textContent = 'Error';
    }
}

// Function to fetch and display user count
async function getUserCount() {
    try {
        const response = await fetch('/api/users/count');
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const userCount = await response.json();
        document.getElementById('totalUsersCount').textContent = userCount;
    } catch (error) {
        console.error("Error fetching user count:", error);
        document.getElementById('totalUsersCount').textContent = 'Error';
    }
}

// Function to fetch and display most used categories
async function getMostUsedCategories() {
    try {
        const response = await fetch('/api/categories/top-used');
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const categories = await response.json();
        const list = document.getElementById('mostUsedCategoriesList');
        list.innerHTML = '';
        categories.forEach(category => {
            const li = document.createElement('li');
            li.className = 'list-group-item d-flex justify-content-between align-items-center';
            li.innerHTML = `<span>${category.categoryName}</span><span class="badge bg-primary rounded-pill">${category.usageCount}</span>`;
            list.appendChild(li);
        });

        // Return the categories data to be used by other functions
        return categories;
    } catch (error) {
        console.error("Error fetching most used categories:", error);
        document.getElementById('mostUsedCategoriesList').innerHTML =
            '<li class="list-group-item">Error loading data</li>';
        return [];
    }
}

// Function to fetch and display least used categories
async function getLeastUsedCategories() {
    try {
        const response = await fetch('/api/categories/least-used');
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const categories = await response.json();
        const list = document.getElementById('leastUsedCategoriesList');
        list.innerHTML = '';
        categories.forEach(category => {
            const li = document.createElement('li');
            li.className = 'list-group-item d-flex justify-content-between align-items-center';
            li.innerHTML = `<span>${category.categoryName}</span><span class="badge bg-danger rounded-pill">${category.usageCount}</span>`;
            list.appendChild(li);
        });
    } catch (error) {
        console.error("Error fetching least used categories:", error);
        document.getElementById('leastUsedCategoriesList').innerHTML =
            '<li class="list-group-item">Error loading data</li>';
    }
}

// Function to update category distribution chart with most used categories data
async function updateCategoryDistributionChart() {
    try {
        // Get the most used categories data
        const categories = await getMostUsedCategories();

        // Update the category distribution chart
        if (window.categoryChart && categories.length > 0) {
            const labels = categories.map(category => category.categoryName);
            const data = categories.map(category => category.usageCount);

            // Generate colors dynamically based on number of categories
            const backgroundColors = generateChartColors(categories.length);
            const borderColors = backgroundColors.map(color => color.replace('0.8', '1'));

            window.categoryChart.data.labels = labels;
            window.categoryChart.data.datasets[0].data = data;
            window.categoryChart.data.datasets[0].backgroundColor = backgroundColors;
            window.categoryChart.data.datasets[0].borderColor = borderColors;
            window.categoryChart.update();
        }
    } catch (error) {
        console.error("Error updating category distribution chart:", error);
    }
}

// Helper function to generate chart colors
function generateChartColors(count) {
    const colors = [
        'rgba(108, 92, 231, 0.8)',
        'rgba(253, 121, 168, 0.8)',
        'rgba(0, 184, 148, 0.8)',
        'rgba(253, 203, 110, 0.8)',
        'rgba(225, 112, 85, 0.8)',
        'rgba(85, 185, 225, 0.8)',
        'rgba(153, 102, 255, 0.8)',
        'rgba(255, 159, 64, 0.8)'
    ];

    // If we have more categories than colors, repeat the colors
    return Array(count).fill().map((_, i) => colors[i % colors.length]);
}

// Function to fetch and update seasonal chart data
async function getSeasonalCategoryUsage() {
    try {
        const response = await fetch('/api/categories/seasonal-usage');
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const seasonalData = await response.json();

        // Update the seasonal chart
        if (window.seasonalChart) {
            const labels = Object.keys(seasonalData);
            const data = labels.map(season => {
                return seasonalData[season].reduce((sum, category) => sum + category.usageCount, 0);
            });

            window.seasonalChart.data.labels = labels;
            window.seasonalChart.data.datasets[0].data = data;
            window.seasonalChart.update();
        }
    } catch (error) {
        console.error("Error fetching seasonal category usage:", error);
    }
}

async function getPeakUsagePeriods() {
    try {
        const response = await fetch('/api/wardrobe/peak-usage');
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const usageData = await response.json();

        // Update the peak usage chart
        if (window.peakUsageChart) {
            const labels = usageData.map(item => item.date);
            const data = usageData.map(item => item.usageCount);

            window.peakUsageChart.data.labels = labels;
            window.peakUsageChart.data.datasets[0].data = data;
            window.peakUsageChart.update();
        }
    } catch (error) {
        console.error("Error fetching peak usage periods:", error);
        document.getElementById('peakUsageChart').innerHTML = '<p>Error loading data</p>';
    }
}

async function getOutfitUsageCount() {
    try {
        const response = await fetch('/api/outfits/usage-count-last-7-days');
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const outfitCount = await response.json();
        document.getElementById('outfitsThisWeekCount').textContent = outfitCount;
    } catch (error) {
        console.error("Error fetching outfit usage count:", error);
        document.getElementById('outfitsThisWeekCount').textContent = 'Error';
    }
}

async function getItemUsageTrends() {
    try {
        const response = await fetch('/api/wardrobe/item-usage-trends');
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const trendData = await response.json();

        // Update the usage trends chart
        if (window.itemUsageChart) {
            const labels = trendData.usageTrends.map(item => item.date);
            const data = trendData.usageTrends.map(item => item.usageCount);

            window.itemUsageChart.data.labels = labels;
            window.itemUsageChart.data.datasets[0].data = data;
            window.itemUsageChart.update();
        }

        // Update newly added items list
        const newItemsList = document.getElementById('newlyAddedItemsList');
        if (newItemsList) {
            newItemsList.innerHTML = '';
            trendData.newlyAddedItems.forEach(item => {
                const li = document.createElement('li');
                li.className = 'list-group-item d-flex justify-content-between align-items-center';
                li.innerHTML = `<span>${item.name}</span><span class="badge bg-success rounded-pill">Added: ${item.createdAt}</span>`;
                newItemsList.appendChild(li);
            });
        }
    } catch (error) {
        console.error("Error fetching item usage trends:", error);
        document.getElementById('itemUsageChart').innerHTML = '<p>Error loading data</p>';
        const newItemsList = document.getElementById('newlyAddedItemsList');
        if (newItemsList) {
            newItemsList.innerHTML = '<li class="list-group-item">Error loading data</li>';
        }
    }
}

// Function to initialize all dashboard data
async function initDashboard() {
    await Promise.all([
        getItemCount(),
        getUserCount(),
        updateCategoryDistributionChart(), // This now handles both the list and chart
        getLeastUsedCategories(),
        getSeasonalCategoryUsage(),
        getPeakUsagePeriods(),
        getOutfitUsageCount(),
        getItemUsageTrends()
    ]);
}

// Call the initialization function when the page loads
document.addEventListener('DOMContentLoaded', function() {
    initDashboard();
});