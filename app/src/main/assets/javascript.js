window.map;

function initMap() {

    var currrentLat = getParameterByName('lat');
    var currrentLng = getParameterByName('lng');
    var description = getParameterByName('description');

    var point = new google.maps.LatLng(currrentLat, currrentLng);

    // Initialize the Google Maps API v3
    map = new google.maps.Map(document.getElementById('map'),{
         zoom: 17,
         center: point,
        disableDefaultUI: true
    });

    ponerPosiciones(point, description);
}

function ponerPosiciones(posicionActual, descripcion) {
  var infowindow = new google.maps.InfoWindow();

  var marker, i;

  marker = new google.maps.Marker({
      position: posicionActual,
      map: map
  });

  google.maps.event.addListener(marker, 'click', (function(marker, i) {
    return function() {
      if (descripcion === "actual") {
        infowindow.setContent("Posición Actual");
      } else {
        infowindow.setContent(descripcion);
      }
      infowindow.open(map, marker);
    }
  })(marker, 0));
}

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
    results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}