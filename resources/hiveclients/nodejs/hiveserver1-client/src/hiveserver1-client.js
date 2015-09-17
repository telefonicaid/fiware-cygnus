/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
 *
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

// dependencies
var hive = require('thrift-hive');
process.stdin.resume();
process.stdin.setEncoding('utf8');
var util = require('util');

// get the given arguments
if (process.argv.size < 7) {
    console.log('Usage: npm start hive_host hive_port db_name hadoop_user hadoop_password');
    process.exit();
} // if

var hiveHost = process.argv[2];
var hivePort = process.argv[3];
var dbName = process.argv[4];
var hadoopUser = process.argv[5];
var hadoopPassword = process.argv[6];

// get a connection
var client = hive.createClient({
    version: '0.7.1-cdh3u2',
    server: hiveHost,
    port: hivePort,
    timeout: 1000
});

// loop prompting for HiveQL sentences
function doLoop() {
    process.stdout.clearLine();
    process.stdout.cursorTo(0);
    process.stdout.write('remotehive> ');

    process.stdin.on('data', function (query) {
        if (query === 'exit\n') {
            client.end();
            process.exit();
        } // if

        // use the given database
        client.execute('use ' + dbName, function (err) {
            if (err) {
                console.log(err);
                return;
            } // if

            // do the query
            client.query(query)
                .on('row', function (database) {
                    console.log(database);
                })
                .on('error', function (err) {
                    console.log(err.message);
                    doLoop();
                })
                .on('end', function () {
                    doLoop();
                });
        });
    });
} // doLoop

// do the loop
doLoop();

