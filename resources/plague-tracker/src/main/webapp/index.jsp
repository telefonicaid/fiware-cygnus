<%-- 
    Document   : index
    Created on : 25-sep-2013, 8:07:14
    Author     : Francisco Romero Bueno frb@tid.es
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
        <style type="text/css">
            html { height: 100% }
            body { height: 100%; margin: 0; padding: 0 }
            #map-canvas { height: 75%; width: 100%; position: absolute; top: 0 }
            #temp-chart-canvas { height: 25%; width: 33.3%; position: absolute; bottom: 0; left: 0 }
            #rainfall-chart-canvas { height: 25%; width: 33.3%; position: absolute; bottom: 0; left: 33.3% }
            #humidity-chart-canvas { height: 25%; width: 33.3%; position: absolute; bottom: 0; right: 0 }
            #charts-control { height: 5%; width: 15%; position: fixed; bottom: 20%; right: 0 }
        </style>
        <script type="text/javascript"
            src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDnWKZc1fJ6SCf8ls9KGdLkFy3KttUQsB8&sensor=false">
        </script>
        <script type="text/javascript"
            src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js">
        </script>
        <script type="text/javascript"
            src="https://www.google.com/jsapi">
        </script>
        <script type="text/javascript" src="scripts/spin.js"></script>
        <script type="text/javascript" src="scripts/plagueTranslator.js"></script>
        <script type="text/javascript">
            google.load("visualization", "1", {packages:["corechart"]});
            
            function printSpinner(elementId) {
                var opts = {
                    lines: 13, // The number of lines to draw
                    length: 20, // The length of each line
                    width: 10, // The line thickness
                    radius: 30, // The radius of the inner circle
                    corners: 1, // Corner roundness (0..1)
                    rotate: 0, // The rotation offset
                    direction: 1, // 1: clockwise, -1: counterclockwise
                    color: '#000', // #rgb or #rrggbb or array of colors
                    speed: 0.7, // Rounds per second
                    trail: 60, // Afterglow percentage
                    shadow: false, // Whether to render a shadow
                    hwaccel: false, // Whether to use hardware acceleration
                    className: 'spinner', // The CSS class to assign to the spinner
                    zIndex: 2e9, // The z-index (defaults to 2000000000)
                    top: 'auto', // Top position relative to parent in px
                    left: 'auto' // Left position relative to parent in px
                };
                
                var target = document.getElementById(elementId);
                var spinner = new Spinner(opts).spin(target);
                
                if (elementId == 'temp-chart-canvas')
                    window.tempSpinner = spinner;
                else if (elementId == 'rainfall-chart-canvas')
                    window.rainfallSpinner = spinner;
                else if (elementId == 'humidity-chart-canvas')
                    window.huiditySpinner = spinner;
                else if (elementId == 'map-canvas')
                    window.mapSpinner = spinner;
            } // printSpinner
            
            function hideSpinner(elementId) {
                if (elementId == 'temp-chart-canvas')
                    window.tempSpinner.stop();
                else if (elementId == 'rainfall-chart-canvas')
                    window.rainfallSpinner.stop();
                else if (elementId == 'humidity-chart-canvas')
                    window.huiditySpinner.stop();
                else if (elementId == 'map-canvas')
                    window.mapSpinner.stop();
            } // hideSpinner
            
            function printCurrentFocuses(type) {
                printSpinner('map-canvas');
                
                $.getJSON('CommandsMgr?cmd=getCurrentFocuses&type=' + type, function(data) {
                    hideSpinner('map-canvas');
                    
                    for(focus in data.focuses) {
                        var area = data.focuses[focus].area;
                        var coords = [];

                        for(coord in area) {
                            coords.push(new google.maps.LatLng(area[coord].lat, area[coord].lng));
                        } // for

                        var areaPolygon = new google.maps.Polygon({
                            paths: coords,
                            strokeColor: "#888888",
                            strokeOpacity: 0.8,
                            strokeWeight: 2,
                            fillColor: "#B8B8B8",
                            fillOpacity: 0
                        });

                        areaPolygon.setMap(window.map); 
                        window.usedPolygons.push(areaPolygon);
                    } // for
                });
            } // printCurrentFocuses

            function getColor(count, max) {
                if (count < (max / 7))
                    return "#99FF00";
                else if (count < (2 * max / 7))
                    return "#FFFF00";
                else if (count < (3 * max / 7))
                    return "#FFCC00";
                else if (count < (4 * max / 7))
                    return "#FF9900";
                else if (count < (5 * max / 7))
                    return "#FF6600";
                else if (count < (6 * max / 7))
                    return "#FF3300";
                else if (count <= max)
                    return "#FF0000";
            } // getColor

            function printInfectionLevels(type) {
                printSpinner('map-canvas');
                
                $.getJSON('CommandsMgr?cmd=getInfectionLevels&type=' + type, function(data) {
                    hideSpinner('map-canvas');
                    
                    // get the max count
                    var max = 0;

                    for(infection_level in data.infection_levels) {
                        var count = parseInt(data.infection_levels[infection_level].count);

                        if (count > max)
                            max = count;
                    } // for

                    // print the neighbourhood in a colour ranging from green to red depending on its hits 
                    for(infection_level in data.infection_levels) {
                        var count = parseInt(data.infection_levels[infection_level].count);
                        var color = getColor(count, max);
                        var area = data.infection_levels[infection_level].area;
                        var coords = [];

                        for(coord in area) {
                            coords.push(new google.maps.LatLng(area[coord].lat, area[coord].lng));
                        } // for

                        var areaPolygon = new google.maps.Polygon({
                            paths: coords,
                            strokeColor: "#888888",
                            strokeOpacity: 0.8,
                            strokeWeight: 2,
                            fillColor: color,
                            fillOpacity: 0.35
                        });

                        areaPolygon.setMap(window.map); 
                        window.usedPolygons.push(areaPolygon);
                    } // for
                });
            } // printInfectionLevels
            
            function printForecast(type) {
                $.getJSON('CommandsMgr?cmd=getForecasts&type=' + type, function(data) {
                    // print the neighbourhood in a colour ranging from green to red depending on its hits 
                    for(forecast in data.forecast) {
                        var color = getColor(data.forecasts[forecast].severity, 10);
                        var area = data.forecasts[forecast].area;
                        var coords = [];

                        for(coord in area) {
                            coords.push(new google.maps.LatLng(area[coord].lat, area[coord].lng));
                        } // for

                        var areaPolygon = new google.maps.Polygon({
                            paths: coords,
                            strokeColor: "#888888",
                            strokeOpacity: 0.8,
                            strokeWeight: 2,
                            fillColor: color,
                            fillOpacity: 0.35
                        });

                        areaPolygon.setMap(window.map);
                        window.usedPolygons.push(areaPolygon);
                    } // for
                });
            } // printForecast

            function submitMapForm() {
                // delete all the currently used polygons
                for(var i = 0 ; i < window.usedPolygons.length; i++) {
                    window.usedPolygons[i].setMap(null);
                } // for
                
                window.usedPolygons = [];
            
                // get the current selection
                var viewSelect = document.getElementById("view_select");
                var view = viewSelect.options[viewSelect.selectedIndex].value;
                var typeSelect = document.getElementById("map_type_select");
                var type = typeSelect.options[typeSelect.selectedIndex].value;
                
                if (type == 'rats')
                    type = 'Ratas';
                else if (type == 'mice')
                    type = 'Ratones';
                else if (type == 'pigeons')
                    type = 'Paloma';
                else if (type == 'cockroaches')
                    type = 'Cucarachas';
                else if (type == 'bees')
                    type = 'Abejas';
                else if (type == 'wasps')
                    type = 'Avispas';
                else if (type == 'ticks')
                    type = 'Garrapatas';
                else if (type == 'fleas')
                    type = 'Pulgas';
                else {
                    alert("Please, select a plague type");
                    return;
                } // else

                if (view == "focuses")
                    printCurrentFocuses(type);
                else if (view == "infection_levels")
                    printInfectionLevels(type);
                else
                    alert("Please, select a view");
            } // submitMapForm
            
            function submitChartForm() {
                var typeSelect = document.getElementById("chart_type_select");
                var type = typeSelect.options[typeSelect.selectedIndex].value;
                
                if (type == 'rats')
                    type = 'Ratas';
                else if (type == 'mice')
                    type = 'Ratones';
                else if (type == 'pigeons')
                    type = 'Paloma';
                else if (type == 'cockroaches')
                    type = 'Cucarachas';
                else if (type == 'bees')
                    type = 'Abejas';
                else if (type == 'wasps')
                    type = 'Avispas';
                else if (type == 'ticks')
                    type = 'Garrapatas';
                else if (type == 'fleas')
                    type = 'Pulgas';
                
                initializeCharts(type);
            } // submitChartForm

            function PlaguesControl(controlDiv) {
                // set CSS styles for the DIV containing the control

                // setting padding to 5 px will offset the control from the edge of the map
                controlDiv.style.padding = '5px';

                // set CSS for the control border
                var controlUI = document.createElement('div');
                controlUI.style.backgroundColor = 'white';
                controlUI.style.borderStyle = 'solid';
                controlUI.style.borderWidth = '0px';
                controlUI.style.cursor = 'pointer';
                controlUI.style.textAlign = 'center';
                controlUI.title = 'Click to select a view';
                controlDiv.appendChild(controlUI);

                // set CSS for the control interior
                var controlSelect = document.createElement('div');
                controlSelect.style.fontFamily = 'Arial,sans-serif';
                controlSelect.style.fontSize = '12px';
                controlSelect.style.paddingLeft = '4px';
                controlSelect.style.paddingRight = '4px';
                controlSelect.innerHTML =
                    '<form>' +
                        '<table style="border: 0px">' +
                            '<tr><td><select id="view_select" style="border: 0px; width: 100%" onchange="submitMapForm()">' +
                                '<option value="">Select a view</option>' +
                                '<option value="focuses">Current focuses</option>' +
                                '<option value="infection_levels">Infection forecast</option>' +
                            '</select></td></tr>' +
                            '<tr><td><select id="map_type_select" style="border: 0px; width: 100%" onchange="submitMapForm()">' +
                                '<option value="">Select a plague type</option>' +
                                '<option value="rats">Rats</option>' +
                                '<option value="mice">Mice</option>' +
                                '<option value="pigeons">Pigeons</option>' +
                                '<option value="cockroaches">Cockroaches</option>' +
                                '<option value="bees">Bees</option>' +
                                '<option value="wasps">Wasps</option>' +
                                '<option value="ticks">Ticks</option>' +
                                '<option value="fleas">Fleas</option>' +
                            '</select></td></tr>' +
/*                                
                            '<tr><td><input id="rats_radio" name="plague_type" type="radio" style="float: left" onclick="SubmitForm()">Rats</input><br></tr></td>' +
                            '<tr><td><input id="mice_radio" name="plague_type" type="radio" style="float: left" onclick="SubmitForm()">Mice</input><br></tr></td>' +
                            '<tr><td><input id="pigeons_radio" name="plague_type" type="radio" style="float: left" onclick="SubmitForm()">Pigeons</input><br></tr></td>' +
                            '<tr><td><input id="cockroaches_radio" name="plague_type" type="radio" style="float: left" onclick="SubmitForm()">Cockroaches</input></tr></td>' +
                            */
                        '</table>' +
                    '</form>';
                controlUI.appendChild(controlSelect);
            } // PlaguesControl

            function initializeMap() {
                // define options for the map
                var mapOptions = {
                    center: new google.maps.LatLng(36.722453, -4.423971),
                    zoom: 15,
                    mapTypeId: google.maps.MapTypeId.ROADMAP
                };

                // create the map as global variable (making it a property of the windows object; do not use "var")
                window.map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
                
                // create an array for storing drawn polygons
                window.usedPolygons = [];

                // create the plagues control
                var plaguesControlDiv = document.createElement('div');
                var plaguesControl = new PlaguesControl(plaguesControlDiv);

                // position the plagues control
                plaguesControlDiv.index = 1;
                map.controls[google.maps.ControlPosition.TOP_RIGHT].push(plaguesControlDiv);
                
                // print the current focuses as the default view
    //            PrintCurrentFocuses();
            } // initializeMap
            
            function initializeCharts(type) {
                // google charts does not allow to print a scatter plot chart without data, this, the jquery must be
                // done in advance... this leads to a white frame drawn before the jquery finishes and the chart is
                // fully loaded
                
                // print an animated spinner while the jquery finishes
                printSpinner('temp-chart-canvas');
                printSpinner('rainfall-chart-canvas');
                printSpinner('humidity-chart-canvas');
                
                $.getJSON('CommandsMgr?cmd=getCorrelations&type=' + type, function(data) {
                    // hide the spinner, the data is already available
                    hideSpinner('temp-chart-canvas');
                    hideSpinner('rainfall-chart-canvas');
                    hideSpinner('humidity-chart-canvas');
                    
                    // calculate monthly means for all the data, this is necessary in order to calculate the
                    // correlation indexes
                    var incidentsMean = 0;
                    var tempMean = 0;
                    var rainfallMean = 0;
                    var humidityMean = 0;
                    
                    for(corr in data.correlations) {
                        incidentsMean += parseInt(data.correlations[corr].num_incidents);
                        tempMean += (parseInt(data.correlations[corr].avg_temp) / 30);
                        rainfallMean += (parseInt(data.correlations[corr].avg_rainfall) / 30);
                        humidityMean += (parseInt(data.correlations[corr].avg_humidity) / 30);
                    } // for
                    
                    incidentsMean /= data.correlations.length;
                    tempMean /= data.correlations.length;
                    rainfallMean /= data.correlations.length;
                    humidityMean /= data.correlations.length;
                    
                    // arrays where to store the points for the scatter plots
                    var tempChartData = [];
                    var tempHeader = ['Rat incidents', 'Average temperature'];
                    tempChartData.push(tempHeader);
                    var rainChartData = [];
                    var rainHeader = ['Rat incidents', 'Average rainfall'];
                    rainChartData.push(rainHeader);                    
                    var humChartData = [];
                    var humHeader = ['Rat incidents', 'Average humidity'];
                    humChartData.push(humHeader);
                    var i = 1;
                    
                    // variables where to accumulate data for the correlation indexes
                    var tempA = 0;
                    var tempB = 0;
                    var tempC = 0;
                    var rainfallA = 0;
                    var rainfallB = 0;
                    var rainfallC = 0;
                    var humidityA = 0;
                    var humidityB = 0;
                    var humidityC = 0;
                    
                    // process the json data
                    for(corr in data.correlations) {
                        var numIncidents = parseInt(data.correlations[corr].num_incidents);
                        var avgTemp = parseInt(data.correlations[corr].avg_temp) / 30;
                        var avgRainfall = parseInt(data.correlations[corr].avg_rainfall) / 30;
                        var avgHumidity = parseInt(data.correlations[corr].avg_humidity) / 30;
                        
                        // compute data fot the correlation index
                        tempA += ((incidentsMean - numIncidents) * (tempMean - avgTemp));
                        tempB += ((incidentsMean - numIncidents) * (incidentsMean - numIncidents));
                        tempC += ((tempMean - avgTemp) * (tempMean - avgTemp));
                        rainfallA += ((incidentsMean - numIncidents) * (rainfallMean - avgRainfall));
                        rainfallB += ((incidentsMean - numIncidents) * (incidentsMean - numIncidents));
                        rainfallC += ((rainfallMean - avgRainfall) * (rainfallMean - avgRainfall));
                        humidityA += ((incidentsMean - numIncidents) * (humidityMean - avgHumidity));
                        humidityB += ((incidentsMean - numIncidents) * (incidentsMean - numIncidents));
                        humidityC += ((humidityMean - avgHumidity) * (humidityMean - avgHumidity));

                        // store points for the scatter plots
                        var tempCorrInfo = [];
                        tempCorrInfo[0] = numIncidents;
                        tempCorrInfo[1] = avgTemp;
                        tempChartData[i] = tempCorrInfo;
                        var rainCorrInfo = [];
                        rainCorrInfo[0] = numIncidents;
                        rainCorrInfo[1] = avgRainfall;
                        rainChartData[i] = rainCorrInfo;
                        var humCorrInfo = [];
                        humCorrInfo[0] = numIncidents;
                        humCorrInfo[1] = avgHumidity;
                        humChartData[i] = humCorrInfo;
                        i++;
                    } // for
                    
                    // last correlation calculus
                    var corrIndexTemp = tempA / Math.sqrt(tempB * tempC);
                    var corrIndexRainfall = rainfallA / Math.sqrt(rainfallB * rainfallC);
                    var corrIndexHumidity = humidityA / Math.sqrt(humidityB * humidityC);
                    
                    // options for the different charts to be drawn
                    var tempOptions = {
                        title: 'Temperature vs. ' + fromSpaToEng(type, false, true) + ' incidents (corr. index '
                            + Math.floor(corrIndexTemp * 100) / 100 + ')',
                        hAxis: {title: 'Incidents'},
                        vAxis: {title: 'Temperature'},
                        legend: 'none'
                    };
                    
                    var rainOptions = {
                        title: 'Rainfall vs. ' + fromSpaToEng(type, false, true) + ' incidents (corr. index '
                            + Math.floor(corrIndexRainfall * 100) / 100 + ')',
                        hAxis: {title: 'Incidents'},
                        vAxis: {title: 'Rainfall'},
                        legend: 'none'
                    };
                    
                    var humOptions = {
                        title: 'Humidity vs. ' + fromSpaToEng(type, false, true) + ' incidents (corr. index '
                            + Math.floor(corrIndexHumidity * 100) / 100 + ')',
                        hAxis: {title: 'Incidents'},
                        vAxis: {title: 'Humidity'},
                        legend: 'none'
                    };

                    // draw the charts
                    var tempChart = new google.visualization.ScatterChart(document.getElementById('temp-chart-canvas'));
                    tempChart.draw(google.visualization.arrayToDataTable(tempChartData), tempOptions);                    
                    var rainChart = new google.visualization.ScatterChart(document.getElementById('rainfall-chart-canvas'));
                    rainChart.draw(google.visualization.arrayToDataTable(rainChartData), rainOptions);
                    var humChart = new google.visualization.ScatterChart(document.getElementById('humidity-chart-canvas'));
                    humChart.draw(google.visualization.arrayToDataTable(humChartData), humOptions);   
                });
            } // initializeCharts

            function initialize() {
                initializeMap();
                initializeCharts('Ratas');
            } // initialize
        </script>
        </head>
    <body onload="initialize()">
        <div id="map-canvas"></div>
        <div id="temp-chart-canvas"></div>
        <div id="rainfall-chart-canvas"></div>
        <div id="humidity-chart-canvas"></div>
        <div id="charts-control">
            <select id="chart_type_select" style="border: 0px; float: right" onchange="submitChartForm()">
                <option value="">Select a plague type</option>
                <option value="rats">Rats</option>
                <option value="mice">Mice</option>
                <option value="pigeons">Pigeons</option>
                <option value="cockroaches">Cockroaches</option>
                <option value="bees">Bees</option>
                <option value="wasps">Wasps</option>
                <option value="ticks">Ticks</option>
                <option value="fleas">Fleas</option>
            </select></td></tr>
        </div>
    </body>
</html>
