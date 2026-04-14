if (!window.__smartLoginPortalReady) {
  window.__smartLoginPortalReady = true;

  var apiBase = "/api";
  var state = { records: [], copies: [] };
  var toastTimer = null;

  var loginPage = document.getElementById("login-page");
  var portalPage = document.getElementById("portal-page");
  var adminPortal = document.getElementById("admin-portal");
  var userPortal = document.getElementById("user-portal");
  var portalTitle = document.getElementById("portal-title");
  var loginBadge = document.getElementById("login-badge");
  var toast = document.getElementById("toast");

  function notify(message) {
    toast.textContent = message;
    toast.classList.add("show");
    if (toastTimer) clearTimeout(toastTimer);
    toastTimer = setTimeout(function () { toast.classList.remove("show"); }, 2200);
  }

  function renderTables() {
    var recordBody = document.querySelector("#record-table tbody");
    var copyBody = document.querySelector("#copy-table tbody");

    recordBody.innerHTML = state.records.length
      ? state.records.map(function (r) {
          return "<tr><td>" + r.isbn + "</td><td>" + r.title + "</td><td>" + r.author + "</td><td>" + (r.category || "-") + "</td><td>" + (r.publishingYear || "-") + "</td><td>" + (r.availableCopies || 0) + " / " + (r.totalCopies || 0) + "</td></tr>";
        }).join("")
      : '<tr><td colspan="6" class="empty">No records.</td></tr>';

    copyBody.innerHTML = state.copies.length
      ? state.copies.map(function (c) {
          return "<tr><td>" + c.copyID + "</td><td>" + c.isbn + "</td><td>" + (c.acquisitionDate || "-") + "</td><td>$" + Number(c.acquisitionPrice || 0).toFixed(2) + "</td><td><span class='chip chip-" + c.status + "'>" + c.status + "</span></td><td>" + (c.borrowerID || "-") + "</td></tr>";
        }).join("")
      : '<tr><td colspan="6" class="empty">No copies.</td></tr>';
  }

  function loadData() {
    return Promise.all([
      fetch(apiBase + "/records").then(function (r) { return r.ok ? r.json() : []; }).catch(function () { return []; }),
      fetch(apiBase + "/copies").then(function (r) { return r.ok ? r.json() : []; }).catch(function () { return []; })
    ]).then(function (res) {
      state.records = res[0] || [];
      state.copies = res[1] || [];
      renderTables();
    });
  }

  function showPortal(role) {
    loginPage.classList.add("hidden");
    portalPage.classList.remove("hidden");

    if (role === "admin") {
      portalTitle.textContent = "Admin Portal";
      loginBadge.textContent = "ADMIN";
      adminPortal.classList.remove("hidden");
      userPortal.classList.add("hidden");
    } else {
      portalTitle.textContent = "User Portal";
      loginBadge.textContent = "USER";
      adminPortal.classList.add("hidden");
      userPortal.classList.remove("hidden");
    }

    loadData();
  }

  function showLogin() {
    portalPage.classList.add("hidden");
    loginPage.classList.remove("hidden");
    document.getElementById("login-form").reset();
  }

  document.getElementById("login-form").addEventListener("submit", function (e) {
    e.preventDefault();
    var id = (document.getElementById("login-id").value || "").trim();
    var pw = (document.getElementById("login-password").value || "").trim();

    if (!id || !pw) {
      notify("Please enter ID and password.");
      return;
    }

    if (id === "admin" && pw === "admin") {
      showPortal("admin");
      notify("Logged in as Admin.");
    } else {
      showPortal("user");
      notify("Logged in as User.");
    }
  });

  document.getElementById("logout-button").addEventListener("click", function () {
    showLogin();
    notify("Logged out.");
  });

  function bindPlaceholder(id, msg) {
    var el = document.getElementById(id);
    if (!el) return;
    el.addEventListener("click", function () { notify(msg); });
  }
  function bindFormPlaceholder(id, msg) {
    var el = document.getElementById(id);
    if (!el) return;
    el.addEventListener("submit", function (e) { e.preventDefault(); notify(msg); });
  }

  bindFormPlaceholder("record-form", "Create record connected.");
  bindFormPlaceholder("copy-form", "Add copy connected.");
  bindPlaceholder("borrow-button", "Borrow action connected.");
  bindPlaceholder("return-button", "Return action connected.");
  bindPlaceholder("search-button", "Search action connected.");
  bindPlaceholder("refresh-button", "Refresh action connected.");
  bindPlaceholder("export-button", "Export action connected.");
  bindPlaceholder("user-search-button", "User search action connected.");
  bindPlaceholder("user-borrow-button", "Borrow request submitted (placeholder).");
  bindPlaceholder("user-renew-button", "Renew request submitted (placeholder).");
}
