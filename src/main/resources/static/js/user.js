function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

function getUserInfo() {
    const userJson = decodeURIComponent(getCookie('userInfo'));
    return JSON.parse(userJson);
}
function getUserRole() {
    const user = getUserInfo();
    return user ? user.role : null;
}


const userJson = decodeURIComponent(getCookie('userInfo'));
const user = JSON.parse(userJson);
// console.log(user);