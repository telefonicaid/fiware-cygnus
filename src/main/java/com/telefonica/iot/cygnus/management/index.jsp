<html>
    <head>
        <meta http-equiv="refresh" content="30">
        <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
        <script type="text/javascript">
            /*var stats = httpGet('http://localhost:8081/v1/stats');*/
            var time = sessionStorage.getItem("time");
            var rows = sessionStorage.getItem("rows");

            if (time === null) {
                time = "0";
                rows = "[[0, 0]]";
            } else {
                time = JSON.stringify(JSON.parse(time) + 1);
                rows = JSON.stringify(JSON.parse(rows).push([JSON.parse(time), JSON.parse(time)]));
            } // if else
            
            sessionStorage.setItem("time", time);
            sessionStorage.setItem("rows", rows);
                
            google.charts.load('current', {'packages':['corechart']});
            google.charts.setOnLoadCallback(drawChannelsChart);

            function httpGet(url) {
                var xmlHttp = new XMLHttpRequest();
                xmlHttp.open("GET", url, false);
                xmlHttp.send(null);
                return xmlHttp.responseText;
            } // httpGet

            function drawChannelsChart() {
                var data = new google.visualization.DataTable();
                data.addColumn('number', 'time');
                data.addColumn('number', 'sth-channel');
                data.addRows(JSON.parse(rows));

                var options = {
                    hAxis: {
                        title: 'Time'
                    },
                    vAxis: {
                        title: 'Events'
                    }
                };

                var chart = new google.visualization.LineChart(document.getElementById('channels'));

                chart.draw(data, options);
            } // drawChannelsChart
        </script>
    </head>
    <body>
        <h1>Cygnus Control Panel (beta)</h1>
        <div id="channels" style="width: 900px; height: 500px"></div>
    </body>
</html>
