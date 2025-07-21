document.getElementById('getWeatherBtn').addEventListener('click', () => {
    getLocationAndWeather();
});

function getLocationAndWeather() {
    // Show loading indicator
    document.getElementById('loading').style.display = 'block';
    document.getElementById('locationContainer').style.display = 'none';
    document.getElementById('weatherContainer').style.display = 'none';
    document.getElementById('error').textContent = '';

    if (navigator.geolocation) {
        // Add options for better reliability
        const options = {
            enableHighAccuracy: true,
            timeout: 10000, // 10 seconds timeout
            maximumAge: 300000 // 5 minutes cache
        };

        navigator.geolocation.getCurrentPosition(
            async (position) => {
                // Clear any previous error messages when we get a successful location
                document.getElementById('error').textContent = '';

                const lat = position.coords.latitude;
                const lon = position.coords.longitude;
                const accuracy = position.coords.accuracy;

                // Display location details
                document.getElementById('latitude').textContent = lat.toFixed(4);
                document.getElementById('longitude').textContent = lon.toFixed(4);
                document.getElementById('accuracy').textContent = `${accuracy} meters`;

                // Try to get approximate address
                try {
                    const address = await getApproximateAddress(lat, lon);
                    document.getElementById('address').textContent = address || "Address not available";
                } catch (error) {
                    document.getElementById('address').textContent = "Address lookup failed";
                }

                document.getElementById('locationContainer').style.display = 'block';

                // Now get weather data
                fetchWeather(lat, lon);
            },
            (error) => {
                document.getElementById('loading').style.display = 'none';
                // Only show error if we don't already have location data displayed
                if (document.getElementById('locationContainer').style.display === 'none') {
                    document.getElementById('error').textContent = `Error getting location: ${error.message}`;
                }
            },
            options // Add the options object
        );
    } else {
        document.getElementById('loading').style.display = 'none';
        document.getElementById('error').textContent = "Geolocation is not supported by this browser.";
    }
}

async function getApproximateAddress(lat, lon) {
    // Using Nominatim (OpenStreetMap's geocoding service)
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
    } catch (error) {
        document.getElementById('loading').style.display = 'none';
        document.getElementById('error').textContent = `Error fetching weather data: ${error.message}`;
    }
}

function displayWeather(weatherData) {
    document.getElementById('loading').style.display = 'none';

    if (weatherData && weatherData.current_weather) {
        const current = weatherData.current_weather;

        document.getElementById('temperature').textContent = `${current.temperature}°C`;
        document.getElementById('windSpeed').textContent = `${current.windspeed} km/h`;
        document.getElementById('windDirection').textContent = `${current.winddirection}°`;

        // Convert weather code to human-readable condition
        const weatherCondition = getWeatherCondition(current.weathercode);
        document.getElementById('weatherCondition').textContent = weatherCondition;

        document.getElementById('weatherContainer').style.display = 'block';
    } else {
        document.getElementById('error').textContent = "No weather data available.";
    }
}

// Helper function to convert weather code to text
function getWeatherCondition(code) {
    // WMO Weather interpretation codes (https://open-meteo.com/en/docs)
    const weatherCodes = {
        0: 'Clear sky',
        1: 'Mainly clear',
        2: 'Partly cloudy',
        3: 'Overcast',
        45: 'Fog',
        48: 'Depositing rime fog',
        51: 'Light drizzle',
        53: 'Moderate drizzle',
        55: 'Dense drizzle',
        56: 'Light freezing drizzle',
        57: 'Dense freezing drizzle',
        61: 'Slight rain',
        63: 'Moderate rain',
        65: 'Heavy rain',
        66: 'Light freezing rain',
        67: 'Heavy freezing rain',
        71: 'Slight snow fall',
        73: 'Moderate snow fall',
        75: 'Heavy snow fall',
        77: 'Snow grains',
        80: 'Slight rain showers',
        81: 'Moderate rain showers',
        82: 'Violent rain showers',
        85: 'Slight snow showers',
        86: 'Heavy snow showers',
        95: 'Thunderstorm',
        96: 'Thunderstorm with slight hail',
        99: 'Thunderstorm with heavy hail'
    };

    return weatherCodes[code] || 'Unknown weather condition';
}