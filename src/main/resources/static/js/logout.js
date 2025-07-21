function logout() {
    // Clear stored data
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('username');
    localStorage.removeItem('currentWeatherData');

    // Redirect to login
    window.location.href = '/logout';
}