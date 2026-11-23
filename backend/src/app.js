const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const routes = require('./routes');
const { notFound, errorHandler } = require('./middleware/errorHandler');

const app = express();

app.use(cors());
app.use(express.json());
app.use(morgan('dev')); // prints every request: "POST /api/auth/login 200 35 ms"

app.use('/api', routes);

app.use(notFound);
app.use(errorHandler);

module.exports = app;
