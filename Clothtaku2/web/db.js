var mysql = require('mysql');

var pool;

exports.connect = function(done) {
    pool = mysql.createPool({
        connectionLimit: 100,
        host     : 'localhost',
        user     : 'root',
        password : 'clothtaku',
        database : 'clothtaku'
    });
}

exports.get = function() {
  return pool;
}