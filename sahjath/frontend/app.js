const GATEWAY_URL = "http://localhost:8080";
const TOKEN_URL = "http://localhost:8180/realms/bookstore/protocol/openid-connect/token";
const CLIENT_ID = "bookstore-frontend";

let accessToken = "";

const loginPanel = document.getElementById("loginPanel");
const workspace = document.getElementById("workspace");
const authStatus = document.getElementById("authStatus");
const logoutButton = document.getElementById("logoutButton");
const output = document.getElementById("output");
const toast = document.getElementById("toast");

document.getElementById("loginForm").addEventListener("submit", login);
document.getElementById("orderForm").addEventListener("submit", createOrder);
document.getElementById("paymentForm").addEventListener("submit", createPayment);
document.getElementById("completePaymentButton").addEventListener("click", completePayment);
document.getElementById("loadOrdersButton").addEventListener("click", loadOrders);
document.getElementById("loadPaymentsButton").addEventListener("click", loadPayments);
document.getElementById("clearButton").addEventListener("click", () => output.textContent = "Ready.");
logoutButton.addEventListener("click", logout);

async function login(event) {
    event.preventDefault();
    const button = event.submitter;
    setBusy(button, true, "Signing in...");

    const body = new URLSearchParams({
        grant_type: "password",
        client_id: CLIENT_ID,
        username: document.getElementById("username").value,
        password: document.getElementById("password").value
    });

    try {
        const response = await fetch(TOKEN_URL, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.error_description || "Login failed");

        accessToken = data.access_token;
        loginPanel.classList.add("hidden");
        workspace.classList.remove("hidden");
        logoutButton.classList.remove("hidden");
        authStatus.textContent = "JWT authenticated";
        authStatus.className = "status status-on";
        showToast("Signed in successfully");
        await loadOrders();
    } catch (error) {
        showToast(error.message + ". Keycloak may still be starting.", true);
    } finally {
        setBusy(button, false, "Sign in");
    }
}

function logout() {
    accessToken = "";
    workspace.classList.add("hidden");
    loginPanel.classList.remove("hidden");
    logoutButton.classList.add("hidden");
    authStatus.textContent = "Not signed in";
    authStatus.className = "status status-off";
    output.textContent = "Ready. Create an order to begin.";
}

async function createOrder(event) {
    event.preventDefault();
    const button = event.submitter;
    setBusy(button, true, "Creating...");
    const request = {
        userId: Number(document.getElementById("userId").value),
        items: [{
            bookId: Number(document.getElementById("bookId").value),
            bookTitle: document.getElementById("bookTitle").value,
            quantity: Number(document.getElementById("quantity").value),
            unitPrice: Number(document.getElementById("unitPrice").value)
        }]
    };

    try {
        const order = await api("/api/orders", { method: "POST", body: JSON.stringify(request) });
        showResult("Order created", order);
        document.getElementById("paymentOrderId").value = order.id;
        document.getElementById("paymentAmount").value = order.totalAmount;
        showToast(`Order #${order.id} created`);
    } catch (error) {
        showError(error);
    } finally {
        setBusy(button, false, "Create order");
    }
}

async function loadOrders() {
    try {
        const orders = await api("/api/orders");
        showResult("Orders", orders);
    } catch (error) {
        showError(error);
    }
}

async function createPayment(event) {
    event.preventDefault();
    const button = event.submitter;
    setBusy(button, true, "Creating...");
    const request = {
        orderId: Number(document.getElementById("paymentOrderId").value),
        amount: Number(document.getElementById("paymentAmount").value),
        paymentMethod: document.getElementById("paymentMethod").value
    };

    try {
        const payment = await api("/api/payments", { method: "POST", body: JSON.stringify(request) });
        showResult("Pending payment created", payment);
        document.getElementById("paymentId").value = payment.id;
        showToast(`Payment #${payment.id} is pending`);
    } catch (error) {
        showError(error);
    } finally {
        setBusy(button, false, "Create pending payment");
    }
}

async function completePayment() {
    const button = document.getElementById("completePaymentButton");
    const paymentId = document.getElementById("paymentId").value;
    if (!paymentId) {
        showToast("Enter or create a payment ID first", true);
        return;
    }
    setBusy(button, true, "Processing...");
    try {
        const payment = await api(`/api/payments/${paymentId}/status`, {
            method: "PATCH",
            body: JSON.stringify({ status: "SUCCESS" })
        });
        showResult("Payment completed; order marked PAID", payment);
        showToast(`Payment #${payment.id} completed`);
    } catch (error) {
        showError(error);
    } finally {
        setBusy(button, false, "Mark SUCCESS");
    }
}

async function loadPayments() {
    try {
        const payments = await api("/api/payments");
        showResult("Payments", payments);
    } catch (error) {
        showError(error);
    }
}

async function api(path, options = {}) {
    if (!accessToken) throw new Error("Please sign in first");
    const response = await fetch(GATEWAY_URL + path, {
        ...options,
        headers: {
            "Authorization": `Bearer ${accessToken}`,
            "Content-Type": "application/json",
            ...(options.headers || {})
        }
    });

    const contentType = response.headers.get("content-type") || "";
    const data = contentType.includes("application/json") ? await response.json() : await response.text();
    if (!response.ok) {
        const message = typeof data === "object" ? data.message || JSON.stringify(data) : data;
        throw new Error(`${response.status}: ${message || response.statusText}`);
    }
    return data;
}

function showResult(title, data) {
    output.textContent = `${title}\n\n${JSON.stringify(data, null, 2)}`;
}

function showError(error) {
    output.textContent = `Request failed\n\n${error.message}`;
    showToast(error.message, true);
}

function setBusy(button, busy, text) {
    if (!button) return;
    button.disabled = busy;
    button.textContent = text;
}

function showToast(message, isError = false) {
    toast.textContent = message;
    toast.className = `toast${isError ? " error" : ""}`;
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => toast.classList.add("hidden"), 3500);
}

