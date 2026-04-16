const express = require('express');
const path = require('path');
const app = express();
const port = process.env.PORT || 3000;

app.use(express.json());
app.use((req, res, next) => {
  // Disable browser caching during development so UI changes appear immediately.
  res.setHeader('Cache-Control', 'no-store, no-cache, must-revalidate, proxy-revalidate');
  res.setHeader('Pragma', 'no-cache');
  res.setHeader('Expires', '0');
  next();
});
app.use(express.static(path.join(__dirname), { etag: false, lastModified: false }));

const library = {
  libraryID: 'LIB0001',
  name: 'Central University Library',
  address: '123 Campus Drive, City',
  phone: '+1 (800) 123-4567',
  email: 'info@universitylib.edu',
};

const bookRecords = [];
const bookCopies = [];
const publishedRecords = [];
const publishedCopies = [];

// Undo/Redo functionality
const undoList = [];
const redoList = [];

function addUndoCommand(command) {
  undoList.push(command);
  redoList.length = 0; // Clear redo list when new command is executed
}

function undoOneCommand() {
  if (undoList.length === 0) {
    return { error: 'Nothing to undo' };
  }
  const command = undoList.pop();
  const result = command.undo();
  redoList.push(command);
  return result;
}

function redoOneCommand() {
  if (redoList.length === 0) {
    return { error: 'Nothing to redo' };
  }
  const command = redoList.pop();
  const result = command.redo();
  undoList.push(command);
  return result;
}

function createBookRecord({ isbn, title, author, language, category, publishingYear, status }) {
  return {
    owner: library.name,
    isbn,
    title,
    author,
    language,
    category,
    publishingYear,
    status: status || 'AVAILABLE',
    totalCopies: 0,
    availableCopies: 0,
    borrowCount: 0,
  };
}

function createBookCopy({ isbn, acquisitionDate }) {
  const sameISBN = bookCopies.filter(copy => copy.isbn === isbn).length + 1;
  const copyID = `${isbn}-${String(sameISBN).padStart(3, '0')}`;
  return {
    owner: library.name,
    isbn,
    copyID,
    acquisitionDate,
    acquisitionPrice: 0,
    status: 'AVAILABLE',
    borrowerID: '',
    lastBorrowingDate: '',
  };
}

function cloneData(data) {
  return JSON.parse(JSON.stringify(data));
}

function publishSnapshot() {
  publishedRecords.length = 0;
  publishedCopies.length = 0;
  cloneData(bookRecords).forEach(record => publishedRecords.push(record));
  cloneData(bookCopies).forEach(copy => publishedCopies.push(copy));
}

function initializeDemoData() {
  bookRecords.push(
    createBookRecord({
      isbn: '9780140449136',
      title: 'Meditations',
      author: 'Marcus Aurelius',
      language: 'English',
      category: 'Philosophy',
      publishingYear: '2006',
    }),
    createBookRecord({
      isbn: '9780261103573',
      title: 'The Fellowship of the Ring',
      author: 'J.R.R. Tolkien',
      language: 'English',
      category: 'Fantasy',
      publishingYear: '2001',
    }),
    createBookRecord({
      isbn: 'TEST-0001',
      title: 'test1',
      author: 'demo',
      language: 'English',
      category: 'Demo',
      publishingYear: '2026',
    }),
    createBookRecord({
      isbn: 'TEST-0002',
      title: 'test2',
      author: 'demo',
      language: 'English',
      category: 'Demo',
      publishingYear: '2026',
    })
  );

  const first = createBookCopy({ isbn: '9780140449136', acquisitionDate: '2024-01-15' });
  bookCopies.push(first);
  const second = createBookCopy({ isbn: '9780140449136', acquisitionDate: '2024-02-20' });
  bookCopies.push(second);
  const third = createBookCopy({ isbn: '9780261103573', acquisitionDate: '2024-03-08' });
  bookCopies.push(third);

  const demo1 = createBookCopy({ isbn: 'TEST-0001', acquisitionDate: '2026-04-16' });
  bookCopies.push(demo1);
  const demo2 = createBookCopy({ isbn: 'TEST-0002', acquisitionDate: '2026-04-16' });
  bookCopies.push(demo2);

  bookRecords[0].totalCopies = 2;
  bookRecords[0].availableCopies = 2;
  bookRecords[1].totalCopies = 1;
  bookRecords[1].availableCopies = 1;
  bookRecords[2].totalCopies = 1;
  bookRecords[2].availableCopies = 1;
  bookRecords[3].totalCopies = 1;
  bookRecords[3].availableCopies = 1;
}

function filterQuery(value, query) {
  if (!query) return true;
  return value.toLowerCase().includes(query.toLowerCase());
}

app.get('/api/library', (req, res) => {
  res.json(library);
});

app.get('/api/records', (req, res) => {
  const usePublished = String(req.query.scope || '').toLowerCase() === 'published';
  res.json(usePublished ? publishedRecords : bookRecords);
});

app.get('/api/copies', (req, res) => {
  const usePublished = String(req.query.scope || '').toLowerCase() === 'published';
  res.json(usePublished ? publishedCopies : bookCopies);
});

app.get('/api/search', (req, res) => {
  const usePublished = String(req.query.scope || '').toLowerCase() === 'published';
  const recordsSource = usePublished ? publishedRecords : bookRecords;
  const copiesSource = usePublished ? publishedCopies : bookCopies;
  const query = (req.query.q || '').trim().toLowerCase();
  const recordResults = recordsSource.filter(record => (
    filterQuery(record.isbn, query) ||
    filterQuery(record.title, query) ||
    filterQuery(record.author, query)
  ));

  // Important for UI availability:
  // When a record matches, include ALL copies for that ISBN so the frontend can
  // correctly compute Available/Unavailable even after searching.
  const matchingIsbns = new Set(recordResults.map(r => r.isbn));

  const copyResults = copiesSource.filter(copy => (
    filterQuery(copy.copyID, query) ||
    filterQuery(copy.isbn, query) ||
    matchingIsbns.has(copy.isbn)
  ));

  res.json({ records: recordResults, copies: copyResults });
});

app.post('/api/records', (req, res) => {
  const { isbn, title, author, language, category, publishingYear } = req.body;
  if (!isbn || !title || !author || !language || !category || !publishingYear) {
    return res.status(400).json({ error: 'Missing required record fields.' });
  }
  const today = new Date().toISOString().slice(0, 10);
  const existingRecord = bookRecords.find(record => record.isbn === isbn);

  if (existingRecord) {
    const newCopy = createBookCopy({ isbn, acquisitionDate: today });
    if (existingRecord.status === 'UNAVAILABLE') {
      newCopy.status = 'UNAVAILABLE';
    }
    bookCopies.push(newCopy);
    existingRecord.totalCopies += 1;
    if (newCopy.status === 'AVAILABLE') {
      existingRecord.availableCopies += 1;
    }

    addUndoCommand({
      type: 'add_copy_via_add_data',
      data: { record: existingRecord, copy: newCopy },
      undo: () => {
        const copyIndex = bookCopies.findIndex(c => c.copyID === newCopy.copyID);
        if (copyIndex !== -1) {
          bookCopies.splice(copyIndex, 1);
          existingRecord.totalCopies -= 1;
          if (newCopy.status === 'AVAILABLE') {
            existingRecord.availableCopies -= 1;
          }
        }
      },
      redo: () => {
        bookCopies.push(newCopy);
        existingRecord.totalCopies += 1;
        if (newCopy.status === 'AVAILABLE') {
          existingRecord.availableCopies += 1;
        }
      }
    });

    return res.status(201).json({
      message: 'Record exists. Added a new copy automatically.',
      record: existingRecord,
      copy: newCopy
    });
  }

  const record = createBookRecord({ isbn, title, author, language, category, publishingYear, status: 'AVAILABLE' });
  const firstCopy = createBookCopy({ isbn, acquisitionDate: today });
  bookRecords.push(record);
  bookCopies.push(firstCopy);
  record.totalCopies = 1;
  record.availableCopies = 1;

  addUndoCommand({
    type: 'add_record_with_copy',
    data: { record, copy: firstCopy },
    undo: () => {
      const recordIndex = bookRecords.findIndex(r => r.isbn === record.isbn);
      if (recordIndex !== -1) {
        bookRecords.splice(recordIndex, 1);
      }
      const copyIndex = bookCopies.findIndex(c => c.copyID === firstCopy.copyID);
      if (copyIndex !== -1) {
        bookCopies.splice(copyIndex, 1);
      }
    },
    redo: () => {
      bookRecords.push(record);
      bookCopies.push(firstCopy);
    }
  });

  res.status(201).json({
    message: 'Record and first copy created successfully.',
    record,
    copy: firstCopy
  });
});

app.post('/api/copies', (req, res) => {
  const { isbn, acquisitionDate } = req.body;
  if (!isbn || !acquisitionDate) {
    return res.status(400).json({ error: 'Missing required copy fields.' });
  }
  const record = bookRecords.find(item => item.isbn === isbn);
  if (!record) {
    return res.status(404).json({ error: 'No book record exists for that ISBN.' });
  }
  const copy = createBookCopy({ isbn, acquisitionDate });
  bookCopies.push(copy);
  record.totalCopies += 1;
  record.availableCopies += 1;
  
  // Add undo command
  addUndoCommand({
    type: 'add_copy',
    data: { copy, record },
    undo: () => {
      const copyIndex = bookCopies.findIndex(c => c.copyID === copy.copyID);
      if (copyIndex !== -1) {
        bookCopies.splice(copyIndex, 1);
        record.totalCopies -= 1;
        record.availableCopies -= 1;
      }
    },
    redo: () => {
      bookCopies.push(copy);
      record.totalCopies += 1;
      record.availableCopies += 1;
    }
  });
  
  res.status(201).json(copy);
});

app.post('/api/borrow', (req, res) => {
  const { copyID, borrowerID } = req.body;
  if (!copyID || !borrowerID) {
    return res.status(400).json({ error: 'copyID and borrowerID are required.' });
  }
  const copy = bookCopies.find(item => item.copyID === copyID);
  if (!copy) {
    return res.status(404).json({ error: 'Copy not found.' });
  }
  if (copy.status !== 'AVAILABLE') {
    return res.status(409).json({ error: 'Copy is not available to borrow.' });
  }
  const record = bookRecords.find(item => item.isbn === copy.isbn);
  if (record && record.status === 'UNAVAILABLE') {
    return res.status(409).json({ error: 'This book is currently unavailable for borrowing.' });
  }

  // Prevent borrowing the same book (same ISBN) twice for a single user.
  const alreadyBorrowedSameIsbn = bookCopies.some(c =>
    c.isbn === copy.isbn &&
    c.status === 'BORROWED' &&
    c.borrowerID === borrowerID
  );
  if (alreadyBorrowedSameIsbn) {
    return res.status(409).json({ error: 'You already borrowed this book.' });
  }

  const oldStatus = copy.status;
  const oldBorrowerID = copy.borrowerID;
  const oldBorrowingDate = copy.lastBorrowingDate;
  copy.status = 'BORROWED';
  copy.borrowerID = borrowerID;
  copy.lastBorrowingDate = new Date().toISOString().slice(0, 10);
  let oldAvailableCopies = 0;
  let oldBorrowCount = 0;
  if (record) {
    oldAvailableCopies = record.availableCopies;
    oldBorrowCount = record.borrowCount;
    record.availableCopies = Math.max(0, record.availableCopies - 1);
    record.borrowCount += 1;
  }
  
  // Add undo command
  addUndoCommand({
    type: 'borrow',
    data: { copy, record, oldStatus, oldBorrowerID, oldBorrowingDate, oldAvailableCopies, oldBorrowCount },
    undo: () => {
      copy.status = oldStatus;
      copy.borrowerID = oldBorrowerID;
      copy.lastBorrowingDate = oldBorrowingDate;
      if (record) {
        record.availableCopies = oldAvailableCopies;
        record.borrowCount = oldBorrowCount;
      }
    },
    redo: () => {
      copy.status = 'BORROWED';
      copy.borrowerID = borrowerID;
      copy.lastBorrowingDate = new Date().toISOString().slice(0, 10);
      if (record) {
        record.availableCopies = Math.max(0, record.availableCopies - 1);
        record.borrowCount += 1;
      }
    }
  });
  publishSnapshot();
  
  res.json(copy);
});

app.post('/api/renew', (req, res) => {
  const { copyID, borrowerID } = req.body;
  if (!copyID || !borrowerID) {
    return res.status(400).json({ error: 'copyID and borrowerID are required.' });
  }

  const copy = bookCopies.find(item => item.copyID === copyID);
  if (!copy) {
    return res.status(404).json({ error: 'Copy not found.' });
  }
  if (copy.status !== 'BORROWED') {
    return res.status(409).json({ error: 'Only borrowed copies can be renewed.' });
  }
  if (copy.borrowerID !== borrowerID) {
    return res.status(409).json({ error: 'This copy is borrowed by a different user.' });
  }

  const oldBorrowingDate = copy.lastBorrowingDate;
  copy.lastBorrowingDate = new Date().toISOString().slice(0, 10);

  addUndoCommand({
    type: 'renew',
    data: { copy, oldBorrowingDate },
    undo: () => {
      copy.lastBorrowingDate = oldBorrowingDate;
    },
    redo: () => {
      copy.lastBorrowingDate = new Date().toISOString().slice(0, 10);
    }
  });
  publishSnapshot();

  res.json(copy);
});

app.post('/api/return', (req, res) => {
  const { copyID, borrowerID } = req.body;
  if (!copyID) {
    return res.status(400).json({ error: 'copyID is required.' });
  }
  const copy = bookCopies.find(item => item.copyID === copyID);
  if (!copy) {
    return res.status(404).json({ error: 'Copy not found.' });
  }
  if (copy.status !== 'BORROWED') {
    return res.status(409).json({ error: 'Only borrowed copies can be returned.' });
  }
  if (borrowerID && copy.borrowerID !== borrowerID) {
    return res.status(409).json({ error: 'This copy is borrowed by a different user.' });
  }
  const oldStatus = copy.status;
  const oldBorrowerID = copy.borrowerID;
  const oldBorrowingDate = copy.lastBorrowingDate;
  copy.status = 'AVAILABLE';
  copy.borrowerID = '';
  copy.lastBorrowingDate = '';
  const record = bookRecords.find(item => item.isbn === copy.isbn);
  let oldAvailableCopies = 0;
  if (record) {
    oldAvailableCopies = record.availableCopies;
    record.availableCopies += 1;
  }
  
  // Add undo command
  addUndoCommand({
    type: 'return',
    data: { copy, oldStatus, oldBorrowerID, oldBorrowingDate, record, oldAvailableCopies },
    undo: () => {
      copy.status = oldStatus;
      copy.borrowerID = oldBorrowerID;
      copy.lastBorrowingDate = oldBorrowingDate;
      if (record) {
        record.availableCopies = oldAvailableCopies;
      }
    },
    redo: () => {
      copy.status = 'AVAILABLE';
      copy.borrowerID = '';
      copy.lastBorrowingDate = '';
      if (record) {
        record.availableCopies = oldAvailableCopies + 1;
      }
    }
  });
  publishSnapshot();
  
  res.json(copy);
});

app.put('/api/records/:isbn', (req, res) => {
  const { isbn } = req.params;
  const updates = req.body;
  
  const record = bookRecords.find(r => r.isbn === isbn);
  if (!record) {
    return res.status(404).json({ error: 'Record not found' });
  }
  
  // Store old values for undo
  const oldValues = {};
  Object.keys(updates).forEach(key => {
    if (updates[key] !== undefined && updates[key] !== '') {
      oldValues[key] = record[key];
      record[key] = updates[key];
    }
  });

  const affectedCopies = [];
  if (updates.status === 'UNAVAILABLE' || updates.status === 'AVAILABLE') {
    bookCopies.forEach(copy => {
      if (copy.isbn === isbn && (copy.status === 'AVAILABLE' || copy.status === 'UNAVAILABLE')) {
        affectedCopies.push({ copy, oldStatus: copy.status });
        copy.status = updates.status;
      }
    });
    record.availableCopies = bookCopies.filter(c => c.isbn === isbn && c.status === 'AVAILABLE').length;
  }
  
  // Add undo command
  addUndoCommand({
    type: 'edit_record',
    data: { record, oldValues, newValues: { ...updates }, affectedCopies },
    undo: () => {
      Object.keys(oldValues).forEach(key => {
        record[key] = oldValues[key];
      });
      affectedCopies.forEach(({ copy, oldStatus }) => {
        copy.status = oldStatus;
      });
      record.availableCopies = bookCopies.filter(c => c.isbn === isbn && c.status === 'AVAILABLE').length;
    },
    redo: () => {
      Object.keys(updates).forEach(key => {
        if (updates[key] !== undefined && updates[key] !== '') {
          record[key] = updates[key];
        }
      });
      affectedCopies.forEach(({ copy }) => {
        copy.status = updates.status;
      });
      record.availableCopies = bookCopies.filter(c => c.isbn === isbn && c.status === 'AVAILABLE').length;
    }
  });
  
  res.json(record);
});

app.put('/api/copies/:copyID', (req, res) => {
  const { copyID } = req.params;
  const updates = req.body;
  
  const copy = bookCopies.find(c => c.copyID === copyID);
  if (!copy) {
    return res.status(404).json({ error: 'Copy not found' });
  }
  
  // Store old values for undo
  const oldValues = {};
  Object.keys(updates).forEach(key => {
    if (updates[key] !== undefined && updates[key] !== '') {
      oldValues[key] = copy[key];
      copy[key] = updates[key];
    }
  });
  
  // Add undo command
  addUndoCommand({
    type: 'edit_copy',
    data: { copy, oldValues, newValues: { ...updates } },
    undo: () => {
      Object.keys(oldValues).forEach(key => {
        copy[key] = oldValues[key];
      });
    },
    redo: () => {
      Object.keys(updates).forEach(key => {
        if (updates[key] !== undefined && updates[key] !== '') {
          copy[key] = updates[key];
        }
      });
    }
  });
  
  res.json(copy);
});

app.delete('/api/records/:isbn', (req, res) => {
  const { isbn } = req.params;
  const forceDelete = String(req.query.force || '').toLowerCase() === 'true';
  const recordIndex = bookRecords.findIndex(r => r.isbn === isbn);
  if (recordIndex === -1) {
    return res.status(404).json({ error: 'Record not found' });
  }
  
  const record = bookRecords[recordIndex];
  const relatedCopies = bookCopies.filter(c => c.isbn === isbn);
  const borrowedCopies = relatedCopies.filter(copy => copy.status === 'BORROWED');
  if (borrowedCopies.length > 0 && !forceDelete) {
    return res.status(409).json({
      error: `There are ${borrowedCopies.length} borrowed copy/copies for this book.`,
      borrowedCount: borrowedCopies.length,
      requiresForce: true
    });
  }
  
  // Remove record and related copies
  bookRecords.splice(recordIndex, 1);
  relatedCopies.forEach(copy => {
    const copyIndex = bookCopies.findIndex(c => c.copyID === copy.copyID);
    if (copyIndex !== -1) {
      bookCopies.splice(copyIndex, 1);
    }
  });
  
  // Add undo command
  addUndoCommand({
    type: 'remove_record',
    data: { record, relatedCopies },
    undo: () => {
      bookRecords.push(record);
      relatedCopies.forEach(copy => {
        bookCopies.push(copy);
      });
    },
    redo: () => {
      const idx = bookRecords.findIndex(r => r.isbn === record.isbn);
      if (idx !== -1) bookRecords.splice(idx, 1);
      relatedCopies.forEach(copy => {
        const copyIdx = bookCopies.findIndex(c => c.copyID === copy.copyID);
        if (copyIdx !== -1) bookCopies.splice(copyIdx, 1);
      });
    }
  });
  
  res.json({ success: true, message: 'Record and related copies removed' });
});

app.delete('/api/copies/:copyID', (req, res) => {
  const { copyID } = req.params;
  const copyIndex = bookCopies.findIndex(c => c.copyID === copyID);
  if (copyIndex === -1) {
    return res.status(404).json({ error: 'Copy not found' });
  }
  
  const copy = bookCopies[copyIndex];
  const record = bookRecords.find(r => r.isbn === copy.isbn);
  
  bookCopies.splice(copyIndex, 1);
  if (record) {
    record.totalCopies -= 1;
    record.availableCopies = Math.max(0, record.availableCopies - (copy.status === 'AVAILABLE' ? 1 : 0));
  }
  
  // Add undo command
  addUndoCommand({
    type: 'remove_copy',
    data: { copy, record },
    undo: () => {
      bookCopies.push(copy);
      if (record) {
        record.totalCopies += 1;
        record.availableCopies += (copy.status === 'AVAILABLE' ? 1 : 0);
      }
    },
    redo: () => {
      const idx = bookCopies.findIndex(c => c.copyID === copy.copyID);
      if (idx !== -1) bookCopies.splice(idx, 1);
      if (record) {
        record.totalCopies -= 1;
        record.availableCopies = Math.max(0, record.availableCopies - (copy.status === 'AVAILABLE' ? 1 : 0));
      }
    }
  });
  
  res.json({ success: true, message: 'Copy removed' });
});

app.post('/api/undo', (req, res) => {
  const result = undoOneCommand();
  if (result && result.error) {
    return res.status(400).json(result);
  }
  res.json({ success: true, message: 'Undo completed' });
});

app.post('/api/redo', (req, res) => {
  const result = redoOneCommand();
  if (result && result.error) {
    return res.status(400).json(result);
  }
  res.json({ success: true, message: 'Redo completed' });
});

app.post('/api/publish', (req, res) => {
  publishSnapshot();
  res.json({ success: true, message: 'Database updated for user portal.' });
});

app.listen(port, () => {
  console.log(`Library web server running at http://localhost:${port}`);
  console.log(`Open this URL in browser: http://localhost:${port}/`);
  console.log('Live Preview should be stopped to avoid serving old content.');
});

initializeDemoData();
publishSnapshot();
