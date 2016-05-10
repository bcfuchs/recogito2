define(['../../../../common/config',
        '../../../../common/formatting',
        '../../../../common/placeUtils'], function(Config, Formatting, PlaceUtils) {

  var SLIDE_DURATION = 200;

  var PlaceSection = function(parent, placeBody, quote) {
    var self = this,

        element = (function() {
          var el = jQuery(
            '<div class="section place">' +
              '<div class="map"></div>' +
              '<div class="panel-container">' +
                '<div class="panel">' +
                  '<h3></h3>' +
                  '<p class="gazetteer"></p>' +
                  '<p class="description"></p>' +
                  '<p class="names"></p>' +
                  '<p class="date"></p>' +
                  '<div class="created">' +
                    '<a class="by"></a>' +
                    '<span class="at"></span>' +
                  '</div>' +
                  '<button class="change btn tiny">Change</button>' +
                  '<div class="warning-unverified">' +
                    '<span class="warning"><span class="icon">&#xf071;</span> Automatic Match</span>' +
                    '<button class="unverified-change">Change</button>' +
                    '<button class="unverified-confirm">Confirm</button>' +
                  '</div>' +
                '</div>' +
                '<div class="warning-unlocated">' +
                  '<span class="icon">&#xf29c;</span>' +
                  '<span class="caption">NO LOCATION</span>' +
                '</div>' +
              '</div>' +
            '</div>');

          el.find('.warning-unverified, .created, .change').hide();
          parent.append(el);
          return el;
        })(),

        noMatchTemplate = jQuery(
          '<div class="no-match">' +
            '<h3>No automatic match found</h3>' +
            '<button>Search</button>' +
          '</div>'),

        panelContainer = element.find('.panel-container'),
        panel = element.find('.panel'),

        title = element.find('h3'),
        gazetteerId = element.find('.gazetteer'),
        description = element.find('.description'),
        names = element.find('.names'),
        date = element.find('.date'),

        createdSection = element.find('.created, .change'),
        createdBy = createdSection.find('.by'),
        createdAt = createdSection.find('.at'),

        warningUnverified = element.find('.warning-unverified'),
        warningUnlocated = element.find('.warning-unlocated'),

        btnChange = element.find('.unverified-change, .change'),
        btnConfirm = element.find('.unverified-confirm'),

        currentGazetteerRecord = false,

        awmc = L.tileLayer('http://a.tiles.mapbox.com/v3/isawnyu.map-knmctlkh/{z}/{x}/{y}.png', {
          attribution: 'Tiles &copy; <a href="http://mapbox.com/" target="_blank">MapBox</a> | ' +
                       'Data &copy; <a href="http://www.openstreetmap.org/" target="_blank">OpenStreetMap</a> and contributors, CC-BY-SA | '+
                       'Tiles and Data &copy; 2013 <a href="http://www.awmc.unc.edu" target="_blank">AWMC</a> ' +
                       '<a href="http://creativecommons.org/licenses/by-nc/3.0/deed.en_US" target="_blank">CC-BY-NC 3.0</a>'
        }),

        map = L.map(element.find('.map')[0], {
          center: new L.LatLng(41.893588, 12.488022),
          zoom: 4,
          zoomControl: false,
          layers: [ awmc ]
        }),

        setCenter = function(latLon) {
          var centerOnLayer = map.latLngToContainerPoint(latLon);
          centerOnLayer = centerOnLayer.subtract([145, 10]);
          map.setView(map.layerPointToLatLng(centerOnLayer), 4, { animate: false });

          // TODO figure out why we need this!? (bad timing?)
          map.invalidateSize();
        },

        /** Renders the standard place card with gazetteer record **/
        renderStandardCard = function(gazetteerRecord, coord) {
          var latLon = (coord) ? [coord[1], coord[0]] : false;

          title.html(gazetteerRecord.title);
          gazetteerId.html(Formatting.formatGazetteerURI(gazetteerRecord.uri));

          if (gazetteerRecord.descriptions) {
            description.html(gazetteerRecord.descriptions[0].description);
            description.show();
          } else {
            description.empty();
            description.hide();
          }

          // names.htmlcreatedBy(labels.slice(1).join(', '));

          if (gazetteerRecord.temporal_bounds) {
            date.html(Formatting.yyyyMMddToYear(gazetteerRecord.temporal_bounds.from) + ' - ' +
                      Formatting.yyyyMMddToYear(gazetteerRecord.temporal_bounds.to));
            date.show();
          } else {
            date.empty();
            date.hide();
          }

          if (placeBody.last_modified_by) {
            createdBy.html(placeBody.last_modified_by);
            createdBy.attr('href', '/' + placeBody.last_modified_by);
          }

          if (placeBody.last_modified_at)
            createdAt.html(Formatting.timeSince(placeBody.last_modified_at));

          if (placeBody.status.value === 'UNVERIFIED') {
            createdSection.hide();
            warningUnverified.show();
          } else {
            createdSection.show();
            warningUnverified.hide();
          }

          // Map
          if (latLon) {
            panelContainer.removeClass('unlocated');
            L.marker(latLon).addTo(map);
            setCenter(latLon);
          } else {
            setCenter([37.98, 23.73]);
            panelContainer.addClass('unlocated');
          }
        },

        /** Renders a 'no match' place card, due to yellow status or failed match **/
        renderNoMatchCard = function() {
          panel.html(noMatchTemplate);
        },

        /** Renders the error edge cases where the place body has a URI that can't be resolved **/
        renderResolveErrorCard = function() {
          gazetteerId.html(Formatting.formatGazetteerURI(placeBody.uri));

          if (placeBody.last_modified_by) {
            createdBy.html(placeBody.last_modified_by);
            createdBy.attr('href', '/' + placeBody.last_modified_by);
          }

          if (placeBody.last_modified_at)
            createdAt.html(Formatting.timeSince(placeBody.last_modified_at));

          createdSection.show();
          warningUnverified.hide();
        },

        /** Fills the template by delegating to the appropriate place card renderer **/
        fillTemplate = function(gazetteerRecord, coord) {
          if (gazetteerRecord) {
            // PlaceBody with URI, record for it - we're all set!
            // (Coord might still be undefined - but the render code will handle that)
            renderStandardCard(gazetteerRecord, coord);
          } else if (placeBody.uri) {
            // No gazetteer record, but a URI!? - edge case
            renderResolveErrorCard();
          } else {
            // No record, no URI, no coord - render the 'no match' card
            renderNoMatchCard();
          }
        },

        /** Fills the place card based on the URI contained in the place body **/
        fillFromURI = function() {
          jQuery.getJSON('/api/places/' + encodeURIComponent(placeBody.uri), function(place) {
            var record = PlaceUtils.getRecord(place, placeBody.uri),
                coord = place.representative_point;

            currentGazetteerRecord = record;
            fillTemplate(record, coord);
          }).fail(function(error) {
            fillTemplate(false, false);
          });
        },

        /** Fills the place card based on a search on the provided quote string **/
        fillFromQuote = function() {
          PlaceUtils.findMatches(quote).done(function(response) {
            if (response.total > 0) {
              var topPlace = response.items[0],
                  bestRecord = PlaceUtils.getBestMatchingRecord(topPlace),
                  coord = topPlace.representative_point;

              placeBody.uri = bestRecord.uri;
              currentGazetteerRecord = bestRecord;
              fillTemplate(bestRecord, coord);
            } else {
              fillTemplate(false, false);
            }
          });
        },

        onConfirm = function() {
          placeBody.status.value = 'VERIFIED';
          createdBy.html(Config.me);
          createdAt.html(Formatting.timeSince(new Date()));
          createdSection.fadeIn(SLIDE_DURATION);
          warningUnverified.slideUp(SLIDE_DURATION);
        },

        commit = function() {
          // TODO implement
        },

        destroy = function() {
          element.remove();
        };

    btnConfirm.click(onConfirm);

    if (placeBody.uri)
      fillFromURI();
    else
      fillFromQuote();

    this.commit = commit;
    this.destroy = destroy;
  };

  return PlaceSection;

});
