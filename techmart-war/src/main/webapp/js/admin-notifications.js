
function showToast(message) {
    let container = document.getElementById('admin-toast-container');
    if (!container) return;
    let toast = document.createElement('div');
    toast.className = 'toast show align-items-center mb-2 bg-white border border-warning';
    toast.innerHTML = `
        <div class="d-flex p-2">
            <div class="toast-body"><i class="bi bi-bell-fill text-warning me-2"></i> ${message}</div>
            <button type="button" class="btn-close" onclick="this.parentElement.parentElement.remove()"></button>
        </div>`;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 8000);
}

function loadAdminNotifications() {
    fetch('/techmart-war/admin-notifications')
        .then(res => res.json())
        .then(data => {
            data.filter(n => n.isRead === 0).forEach(n => {
                showToast(n.message);

                fetch('/techmart-war/mark-read?id=' + n.id)
                    .then(() => console.log("Notification " + n.id + " marked as read."));
            });
        })
        .catch(err => console.error("Admin notification fetch error:", err));
}

setInterval(loadAdminNotifications, 10000);