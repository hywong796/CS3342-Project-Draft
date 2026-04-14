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

function createBookRecord({ isbn, title, author, language, category, publishingYear }) {
  return {
    owner: library.name,
    isbn,
    title,
    author,
    language,
    category,
    publishingYear,
    totalCopies: 0,
    availableCopies: 0,
    borrowCount: 0,
  };
}

function createBookCopy({ isbn, acquisitionDate, acquisitionPrice }) {
  const sameISBN = bookCopies.filter(copy => copy.isbn === isbn).length + 1;
  const copyID = `${isbn}-${String(sameISBN).padStart(3, '0')}`;
  return {
    owner: library.name,
    isbn,
    copyID,
    acquisitionDate,
    acquisitionPrice,
    status: 'AVAILABLE',
    borrowerID: '',
    lastBorrowingDate: '',
  };
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
    })
  );

  const first = createBookCopy({ isbn: '9780140449136', acquisitionDate: '2024-01-15', acquisitionPrice: 12.5 });
  const second = createBookCopy({ isbn: '9780140449136', acquisitionDate: '2024-02-20', acquisitionPrice: 14.0 });
  const third = createBookCopy({ isbn: '9780261103573', acquisitionDate: '2024-03-08', acquisitionPrice: 18.0 });

  bookCopies.push(first, second, third);
  bookRecords[0].totalCopies = 2;
  bookRecords[0].availableCopies = 2;
  bookRecords[1].totalCopies = 1;
  bookRecords[1].availableCopies = 1;
}

function filterQuery(value, query) {
  if (!query) return true;
  return value.toLowerCase().includes(query.toLowerCase());
}

app.get('/api/library', (req, res) => {
  res.json(library);
});

app.get('/api/records', (req, res) => {
  res.json(bookRecords);
});

app.get('/api/copies', (req, res) => {
  res.json(bookCopies);
});

app.get('/api/search', (req, res) => {
  const query = (req.query.q || '').trim().toLowerCase();
  const recordResults = bookRecords.filter(record => {
    return (
      filterQuery(record.isbn, query) ||
      filterQuery(record.title, query) ||
      filterQuery(record.author, query)
    );
  });
  const copyResults = bookCopies.filter(copy => {
    return (
      filterQuery(copy.copyID, query) ||
      filterQuery(copy.isbn, query)
    );
  });
  res.json({ records: recordResults, copies: copyResults });
});

app.post('/api/records', (req, res) => {
  const { isbn, title, author, language, category, publishingYear } = req.body;
  if (!isbn || !title || !author || !language || !category || !publishingYear) {
    return res.status(400).json({ error: 'Missing required record fields.' });
  }
  if (bookRecords.some(record => record.isbn === isbn)) {
    return res.status(409).json({ error: 'A record with this ISBN already exists.' });
  }
  const record = createBookRecord({ isbn, title, author, language, category, publishingYear });
  bookRecords.push(record);
  res.status(201).json(record);
});

app.post('/api/copies', (req, res) => {
  const { isbn, acquisitionDate, acquisitionPrice } = req.body;
  if (!isbn || !acquisitionDate || acquisitionPrice == null) {
    return res.status(400).json({ error: 'Missing required copy fields.' });
  }
  const record = bookRecords.find(item => item.isbn === isbn);
  if (!record) {
    return res.status(404).json({ error: 'No book record exists for that ISBN.' });
  }
  const copy = createBookCopy({ isbn, acquisitionDate, acquisitionPrice });
  bookCopies.push(copy);
  record.totalCopies += 1;
  record.availableCopies += 1;
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
  copy.status = 'BORROWED';
  copy.borrowerID = borrowerID;
  copy.lastBorrowingDate = new Date().toISOString().slice(0, 10);
  const record = bookRecords.find(item => item.isbn === copy.isbn);
  if (record) {
    record.availableCopies = Math.max(0, record.availableCopies - 1);
    record.borrowCount += 1;
  }
  res.json(copy);
});

app.post('/api/return', (req, res) => {
  const { copyID } = req.body;
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
  copy.status = 'PROCESSING';
  copy.borrowerID = '';
  res.json(copy);
});

app.listen(port, () => {
  console.log(`Library web server running at http://localhost:${port}`);
  console.log(`Open this URL in browser: http://localhost:${port}/`);
  console.log('Live Preview should be stopped to avoid serving old content.');
});

initializeDemoData();
