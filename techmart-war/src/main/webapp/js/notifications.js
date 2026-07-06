
function showToast(message) {
    let container = document.getElementById('toast-container');
    if (!container) return;
    let toast = document.createElement('div');
    toast.className = 'toast show align-items-center mb-2 bg-white border border-primary';
    toast.innerHTML = `
        <div class="d-flex p-2">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close" onclick="this.parentElement.parentElement.remove()"></button>
        </div>`;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 5000);
}

function loadNotifications() {
    if (typeof isLoggedIn !== 'undefined' && !isLoggedIn) return;

    fetch('get-notifications')
        .then(res => res.json())
        .then(notifications => {
            notifications.filter(n => n.isRead === 0).forEach(n => {
                showToast("🔔 " + n.message);
                fetch('mark-read?id=' + n.id);
            });
        })
        .catch(err => console.error("Notification fetch error:", err));
}

setInterval(loadNotifications, 10000);