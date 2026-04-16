if (!window.__smartLoginPortalReady) {
  window.__smartLoginPortalReady = true;

  var apiBase = "/api";
  var state = { records: [], copies: [] };
  var session = { role: null, userId: "" };
  var toastTimer = null;

  var loginPage = document.getElementById("login-page");
  var portalPage = document.getElementById("portal-page");
  var adminPortal = document.getElementById("admin-portal");
  var userPortal = document.getElementById("user-portal");
  var adminRecordsPanel = document.getElementById("admin-records-panel");
  var adminCopiesPanel = document.getElementById("admin-copies-panel");
  var portalTitle = document.getElementById("portal-title");
  var loginBadge = document.getElementById("login-badge");
  var toast = document.getElementById("toast");
  var recordBody = document.querySelector("#record-table tbody");
  var copyBody = document.querySelector("#copy-table tbody");
  var userBookList = document.getElementById("user-book-list");
  var userInventoryList = document.getElementById("user-inventory-list");
  var adminPanels = Array.prototype.slice.call(document.querySelectorAll(".admin-panel"));
  var recordTable = document.getElementById("record-table");
  var copyTable = document.getElementById("copy-table");

  var selectedRecordIsbn = "";
  var selectedCopyId = "";

  function closeAdminPanels() {
    adminPanels.forEach(function (panel) { panel.classList.add("hidden"); });
  }

  function toggleAdminPanel(panelId) {
    var target = document.getElementById(panelId);
    if (!target) return;
    var willOpen = target.classList.contains("hidden");
    closeAdminPanels();
    if (willOpen) target.classList.remove("hidden");
  }

  function notify(message) {
    toast.textContent = message;
    toast.classList.add("show");
    if (toastTimer) clearTimeout(toastTimer);
    toastTimer = setTimeout(function () { toast.classList.remove("show"); }, 2200);
  }

  function escapeHtml(value) {
    return String(value == null ? "" : value)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function parseJson(response) {
    return response.text().then(function (text) {
      return text ? JSON.parse(text) : {};
    });
  }

  function handleResponse(response) {
    return parseJson(response).then(function (data) {
      if (!response.ok) {
        var requestError = new Error((data && data.error) || "Request failed");
        requestError.status = response.status;
        requestError.payload = data || {};
        throw requestError;
      }
      return data;
    });
  }

  function handleNetworkError(error) {
    var message = (error && error.message) ? error.message : String(error);
    if (/failed to fetch/i.test(message) || /networkerror/i.test(message)) {
      notify("Error: Cannot reach server. Please start the web server (run node server.js), then refresh.");
      return;
    }
    notify("Error: " + message);
  }

  function postJson(path, body) {
    return fetch(apiBase + path, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body || {})
    }).then(handleResponse);
  }

  function requestAndReload(path, body, successMessage, afterSuccess) {
    return postJson(path, body)
      .then(function () {
        if (typeof afterSuccess === "function") afterSuccess();
        notify(successMessage);
        return loadData();
      })
      .catch(handleNetworkError);
  }

  function getAvailableCopyForRecord(isbn) {
    var record = state.records.find(function (item) { return item.isbn === isbn; });
    if (record && record.status === "UNAVAILABLE") {
      return null;
    }
    for (var i = 0; i < state.copies.length; i += 1) {
      if (state.copies[i].isbn === isbn && state.copies[i].status === "AVAILABLE") {
        return state.copies[i];
      }
    }
    return null;
  }

  function getUserBorrowedCopies() {
    if (!session.userId) return [];
    return state.copies.filter(function (copy) {
      return copy.borrowerID === session.userId && copy.status === "BORROWED";
    });
  }

  function renderAdminTables() {
    recordBody.innerHTML = state.records.length
      ? state.records.map(function (record) {
          var selectedClass = selectedRecordIsbn && record.isbn === selectedRecordIsbn ? " class='is-selected'" : "";
          return "<tr data-isbn='" + escapeHtml(record.isbn) + "'" + selectedClass + "><td>" + escapeHtml(record.isbn) + "</td><td>" + escapeHtml(record.title) + "</td><td>" + escapeHtml(record.author) + "</td><td>" + escapeHtml(record.category || "-") + "</td><td>" + escapeHtml(record.publishingYear || "-") + "</td><td>" + escapeHtml((record.availableCopies || 0) + " / " + (record.totalCopies || 0)) + "</td></tr>";
        }).join("")
      : '<tr><td colspan="6" class="empty">No records.</td></tr>';

    copyBody.innerHTML = state.copies.length
      ? state.copies.map(function (copy) {
          var copySelectedClass = selectedCopyId && copy.copyID === selectedCopyId ? " class='is-selected'" : "";
          return "<tr data-copy-id='" + escapeHtml(copy.copyID) + "' data-isbn='" + escapeHtml(copy.isbn) + "'" + copySelectedClass + "><td>" + escapeHtml(copy.copyID) + "</td><td>" + escapeHtml(copy.isbn) + "</td><td>" + escapeHtml(copy.acquisitionDate || "-") + "</td><td><span class='chip chip-" + escapeHtml(copy.status) + "'>" + escapeHtml(copy.status) + "</span></td><td>" + escapeHtml(copy.borrowerID || "-") + "</td></tr>";
        }).join("")
      : '<tr><td colspan="5" class="empty">No copies.</td></tr>';
  }

  function renderUserCatalog() {
    if (!userBookList) return;

    userBookList.innerHTML = state.records.length
      ? state.records.map(function (record) {
          var availableCopy = getAvailableCopyForRecord(record.isbn);
          var isAvailable = Boolean(availableCopy);
          return [
            "<article class='book-card'>",
            "<div>",
            "<h4>" + escapeHtml(record.title) + "</h4>",
            "<div class='book-meta'>",
            "<span><strong>Author:</strong> " + escapeHtml(record.author) + "</span>",
            "<span><strong>ISBN:</strong> " + escapeHtml(record.isbn) + "</span>",
            "<span><strong>Category:</strong> " + escapeHtml(record.category || "-") + "</span>",
            "<span><strong>Year:</strong> " + escapeHtml(record.publishingYear || "-") + "</span>",
            "</div>",
            "</div>",
            "<span class='book-status " + (isAvailable ? "available" : "unavailable") + "'>" + (isAvailable ? "Available" : "Unavailable") + "</span>",
            "<button class='btn btn-primary user-borrow-action' type='button' data-isbn='" + escapeHtml(record.isbn) + "'" + (isAvailable ? "" : " disabled") + ">Borrow</button>",
            "</article>"
          ].join("");
        }).join("")
      : "<p class='empty'>No books match your search.</p>";
  }

  function renderUserInventory() {
    if (!userInventoryList) return;

    var borrowedCopies = getUserBorrowedCopies();
    userInventoryList.innerHTML = borrowedCopies.length
      ? borrowedCopies.map(function (copy) {
          var record = state.records.find(function (item) { return item.isbn === copy.isbn; }) || {};
          return [
            "<article class='inventory-card'>",
            "<div>",
            "<h4>" + escapeHtml(record.title || copy.isbn) + "</h4>",
            "<p><strong>Author:</strong> " + escapeHtml(record.author || "-") + "</p>",
            "<p><strong>Copy ID:</strong> " + escapeHtml(copy.copyID) + "</p>",
            "<p><strong>Borrowed On:</strong> " + escapeHtml(copy.lastBorrowingDate || "-") + "</p>",
            "</div>",
            "<div class='inventory-actions'>",
            "<button class='btn btn-secondary user-renew-action' type='button' data-copy-id='" + escapeHtml(copy.copyID) + "'>Renew</button>",
            "<button class='btn btn-primary user-return-action' type='button' data-copy-id='" + escapeHtml(copy.copyID) + "'>Return</button>",
            "</div>",
            "</article>"
          ].join("");
        }).join("")
      : "<p class='empty'>You have no borrowed books right now.</p>";
  }

  function renderViews() {
    renderAdminTables();
    renderUserCatalog();
    renderUserInventory();
  }

  function applyData(records, copies) {
    state.records = records || [];
    state.copies = copies || [];
    if (selectedRecordIsbn && !state.records.some(function (r) { return r.isbn === selectedRecordIsbn; })) {
      selectedRecordIsbn = "";
      var editIsbn = document.getElementById("edit-record-isbn");
      var removeIsbn = document.getElementById("remove-record-isbn");
      if (editIsbn) editIsbn.value = "";
      if (removeIsbn) removeIsbn.value = "";
    }
    if (selectedCopyId && !state.copies.some(function (c) { return c.copyID === selectedCopyId; })) {
      selectedCopyId = "";
      var removeCopyInput = document.getElementById("remove-copy-id");
      if (removeCopyInput) removeCopyInput.value = "";
    }
    renderViews();
  }

  function loadData() {
    var scopeQuery = session.role === "user" ? "?scope=published" : "";
    return Promise.all([
      fetch(apiBase + "/records" + scopeQuery).then(function (response) { return response.ok ? response.json() : []; }).catch(function () { return []; }),
      fetch(apiBase + "/copies" + scopeQuery).then(function (response) { return response.ok ? response.json() : []; }).catch(function () { return []; })
    ]).then(function (results) {
      applyData(results[0], results[1]);
    });
  }

  function searchCatalog(query) {
    var scopeSuffix = session.role === "user" ? "&scope=published" : "";
    return fetch(apiBase + "/search?q=" + encodeURIComponent(query || "") + scopeSuffix)
      .then(handleResponse)
      .then(function (data) {
        applyData(data.records, data.copies);
        return data;
      });
  }

  function showPortal(role) {
    loginPage.classList.add("hidden");
    portalPage.classList.remove("hidden");

    if (role === "admin") {
      portalTitle.textContent = "Admin Portal";
      loginBadge.textContent = "ADMIN";
      adminPortal.classList.remove("hidden");
      adminRecordsPanel.classList.remove("hidden");
      adminCopiesPanel.classList.remove("hidden");
      userPortal.classList.add("hidden");
      closeAdminPanels();
    } else {
      portalTitle.textContent = "Homepage";
      loginBadge.textContent = "USER";
      adminPortal.classList.add("hidden");
      adminRecordsPanel.classList.add("hidden");
      adminCopiesPanel.classList.add("hidden");
      userPortal.classList.remove("hidden");
    }

    loadData();
  }

  function showLogin() {
    portalPage.classList.add("hidden");
    loginPage.classList.remove("hidden");
    session = { role: null, userId: "" };
    document.getElementById("login-form").reset();
    document.getElementById("user-search-input").value = "";
    document.getElementById("search-input").value = "";
  }

  function handleUserBorrow(isbn) {
    var availableCopy = getAvailableCopyForRecord(isbn);
    if (!availableCopy) {
      notify("This book is currently unavailable.");
      return;
    }
    if (!confirm("Borrow this book now?")) {
      return;
    }
    requestAndReload("/borrow", { copyID: availableCopy.copyID, borrowerID: session.userId }, "Book borrowed successfully.");
  }

  function handleUserRenew(copyID) {
    if (!confirm("Renew this borrowed book?")) {
      return;
    }
    requestAndReload("/renew", { copyID: copyID, borrowerID: session.userId }, "Book renewed successfully.");
  }

  function handleUserReturn(copyID) {
    if (!confirm("Return this borrowed book?")) {
      return;
    }
    requestAndReload("/return", { copyID: copyID, borrowerID: session.userId }, "Book returned successfully.");
  }

  document.getElementById("login-form").addEventListener("submit", function (event) {
    event.preventDefault();
    var id = (document.getElementById("login-id").value || "").trim();
    var password = (document.getElementById("login-password").value || "").trim();

    if (!id || !password) {
      notify("Please enter ID and password.");
      return;
    }

    if (id === "admin" && password === "admin") {
      session = { role: "admin", userId: id };
      showPortal("admin");
      notify("Logged in as Admin.");
      return;
    }

    session = { role: "user", userId: id };
    showPortal("user");
    notify("Logged in as User.");
  });

  document.getElementById("logout-button").addEventListener("click", function () {
    showLogin();
    notify("Logged out.");
  });

  document.getElementById("record-form").addEventListener("submit", function (event) {
    event.preventDefault();
    var data = {
      isbn: document.getElementById("record-isbn").value.trim(),
      title: document.getElementById("record-title").value.trim(),
      author: document.getElementById("record-author").value.trim(),
      language: document.getElementById("record-language").value.trim(),
      category: document.getElementById("record-category").value.trim(),
      publishingYear: document.getElementById("record-year").value
    };

    fetch(apiBase + "/records", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data)
    }).then(handleResponse)
      .then(function (result) {
        var copyID = result && result.copy && result.copy.copyID;
        if (copyID) {
          notify("Submitted. Auto-created copy: " + copyID);
        } else {
          notify("Record added successfully");
        }
        loadData();
        event.target.reset();
      })
      .catch(function (error) {
        handleNetworkError(error);
      });
  });

  document.getElementById("copy-form").addEventListener("submit", function (event) {
    event.preventDefault();
    var data = {
      isbn: document.getElementById("add-copy-isbn").value.trim(),
      acquisitionDate: document.getElementById("add-copy-date").value
    };

    fetch(apiBase + "/copies", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data)
    }).then(handleResponse)
      .then(function (result) {
        notify("Copy added: " + (result.copyID || "created"));
        loadData();
        event.target.reset();
        document.getElementById("add-copy-date").value = new Date().toISOString().slice(0, 10);
      })
      .catch(handleNetworkError);
  });

  document.getElementById("edit-record-form").addEventListener("submit", function (event) {
    event.preventDefault();
    var isbn = (document.getElementById("edit-record-isbn").value || selectedRecordIsbn || "").trim();
    if (!isbn) {
      notify("Please enter or select an ISBN first.");
      return;
    }

    var updates = {};
    var title = document.getElementById("edit-record-title").value.trim();
    var author = document.getElementById("edit-record-author").value.trim();
    var language = document.getElementById("edit-record-language").value.trim();
    var category = document.getElementById("edit-record-category").value.trim();
    var year = document.getElementById("edit-record-year").value;
    var status = document.getElementById("edit-record-status").value;

    if (title) updates.title = title;
    if (author) updates.author = author;
    if (language) updates.language = language;
    if (category) updates.category = category;
    if (year) updates.publishingYear = year;
    if (status) updates.status = status;

    if (Object.keys(updates).length === 0) {
      notify("Please enter at least one field to update");
      return;
    }

    fetch(apiBase + "/records/" + encodeURIComponent(isbn), {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(updates)
    }).then(handleResponse)
      .then(function () {
        notify("Record updated successfully");
        loadData();
        event.target.reset();
        document.getElementById("edit-record-isbn").value = selectedRecordIsbn;
      })
      .catch(function (error) {
        handleNetworkError(error);
      });
  });

  document.getElementById("remove-record-form").addEventListener("submit", function (event) {
    event.preventDefault();
    var isbn = (document.getElementById("remove-record-isbn").value || selectedRecordIsbn || "").trim();
    if (!isbn) {
      notify("Please enter or select an ISBN first.");
      return;
    }
    if (!confirm("Are you sure you want to remove this record and all its copies?")) {
      return;
    }

    fetch(apiBase + "/records/" + encodeURIComponent(isbn), {
      method: "DELETE"
    }).then(handleResponse)
      .then(function () {
        notify("Record removed successfully");
        loadData();
        selectedRecordIsbn = "";
        document.getElementById("edit-record-isbn").value = "";
        document.getElementById("remove-record-isbn").value = "";
      })
      .catch(function (error) {
        if (error && error.payload && error.payload.requiresForce) {
          var borrowedCount = error.payload.borrowedCount || 0;
          if (confirm("There are " + borrowedCount + " borrowed copies for this book. Continue removing anyway?")) {
            fetch(apiBase + "/records/" + encodeURIComponent(isbn) + "?force=true", {
              method: "DELETE"
            }).then(handleResponse)
              .then(function () {
                notify("Record removed successfully.");
                loadData();
                selectedRecordIsbn = "";
                document.getElementById("edit-record-isbn").value = "";
                document.getElementById("remove-record-isbn").value = "";
              })
              .catch(handleNetworkError);
          }
          return;
        }
        handleNetworkError(error);
      });
  });

  document.getElementById("remove-copy-form").addEventListener("submit", function (event) {
    event.preventDefault();
    var copyID = (document.getElementById("remove-copy-id").value || selectedCopyId || "").trim();
    if (!copyID) {
      notify("Please enter or select a Copy ID first.");
      return;
    }
    if (!confirm("Are you sure you want to remove this copy?")) {
      return;
    }

    fetch(apiBase + "/copies/" + encodeURIComponent(copyID), {
      method: "DELETE"
    }).then(handleResponse)
      .then(function () {
        notify("Copy removed successfully");
        loadData();
        selectedCopyId = "";
        document.getElementById("remove-copy-id").value = "";
      })
      .catch(handleNetworkError);
  });

  document.addEventListener("click", function (event) {
    var opener = event.target.closest("[data-admin-panel-open]");
    if (!opener) return;
    toggleAdminPanel(opener.getAttribute("data-admin-panel-open"));
  });

  if (recordTable) {
    recordTable.addEventListener("click", function (event) {
      var row = event.target.closest("tbody tr[data-isbn]");
      if (!row) return;
      selectedRecordIsbn = row.getAttribute("data-isbn") || "";
      var editIsbn = document.getElementById("edit-record-isbn");
      var removeIsbn = document.getElementById("remove-record-isbn");
      var addCopyIsbn = document.getElementById("add-copy-isbn");
      if (editIsbn) editIsbn.value = selectedRecordIsbn;
      if (removeIsbn) removeIsbn.value = selectedRecordIsbn;
      if (addCopyIsbn) addCopyIsbn.value = selectedRecordIsbn;
      renderAdminTables();
    });
  }

  if (copyTable) {
    copyTable.addEventListener("click", function (event) {
      var row = event.target.closest("tbody tr[data-copy-id]");
      if (!row) return;
      selectedCopyId = row.getAttribute("data-copy-id") || "";
      var rowIsbn = row.getAttribute("data-isbn") || "";
      document.getElementById("remove-copy-id").value = selectedCopyId;
      if (rowIsbn) {
        selectedRecordIsbn = rowIsbn;
        document.getElementById("edit-record-isbn").value = rowIsbn;
        document.getElementById("remove-record-isbn").value = rowIsbn;
        document.getElementById("add-copy-isbn").value = rowIsbn;
      }
      renderAdminTables();
    });
  }

  document.getElementById("undo-button").addEventListener("click", function () {
    requestAndReload("/undo", {}, "Undo completed");
  });

  document.getElementById("redo-button").addEventListener("click", function () {
    requestAndReload("/redo", {}, "Redo completed");
  });

  var publishButton = document.getElementById("publish-button");
  if (publishButton) {
    publishButton.addEventListener("click", function () {
      postJson("/publish", {})
        .then(function () {
          notify("Database updated. User portal can now see latest changes.");
          return loadData();
        })
        .catch(function (error) {
          if (error && error.status === 404) {
            notify("Update endpoint not found. Please restart web server (node server.js) and try again.");
            return;
          }
          handleNetworkError(error);
        });
    });
  }

  document.getElementById("search-button").addEventListener("click", function () {
    var query = document.getElementById("search-input").value.trim();
    searchCatalog(query)
      .then(function () {
        notify(query ? "Search results updated." : "Showing all records and copies.");
      })
      .catch(handleNetworkError);
  });

  document.getElementById("refresh-button").addEventListener("click", function () {
    document.getElementById("search-input").value = "";
    loadData()
      .then(function () {
        notify("Data refreshed.");
      })
      .catch(handleNetworkError);
  });

  document.getElementById("export-button").addEventListener("click", function () {
    var snapshot = {
      exportedAt: new Date().toISOString(),
      records: state.records,
      copies: state.copies
    };
    var blob = new Blob([JSON.stringify(snapshot, null, 2)], { type: "application/json" });
    var link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = "library-snapshot.json";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(link.href);
    notify("Snapshot exported.");
  });

  document.getElementById("user-search-button").addEventListener("click", function () {
    var query = document.getElementById("user-search-input").value.trim();
    searchCatalog(query)
      .then(function () {
        notify(query ? "Catalog search completed." : "Showing full catalog.");
      })
      .catch(handleNetworkError);
  });

  document.getElementById("user-refresh-button").addEventListener("click", function () {
    document.getElementById("user-search-input").value = "";
    loadData()
      .then(function () {
        notify("Showing all books.");
      })
      .catch(handleNetworkError);
  });

  if (document.getElementById("add-copy-date")) {
    document.getElementById("add-copy-date").value = new Date().toISOString().slice(0, 10);
  }

  userBookList.addEventListener("click", function (event) {
    var button = event.target.closest(".user-borrow-action");
    if (!button) return;
    handleUserBorrow(button.getAttribute("data-isbn"));
  });

  userInventoryList.addEventListener("click", function (event) {
    var renewButton = event.target.closest(".user-renew-action");
    if (renewButton) {
      handleUserRenew(renewButton.getAttribute("data-copy-id"));
      return;
    }

    var returnButton = event.target.closest(".user-return-action");
    if (returnButton) {
      handleUserReturn(returnButton.getAttribute("data-copy-id"));
    }
  });
}
