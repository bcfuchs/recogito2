define(['common/helpers/annotationUtils'], function(AnnotationUtils) {

  var TEXT = 3; // HTML DOM node type for text nodes

  var Highlighter = function(rootNode) {

    var walkTextNodes = function(node, nodeArray) {
          var nodes = (nodeArray) ? nodeArray : [];

          if (node.nodeType === TEXT)
            nodes.push(node);

          node = node.firstChild;

          while(node) {
            walkTextNodes(node, nodes);
            node = node.nextSibling;
          }

          return nodes;
        },

        /** Basically a hand-coded dropWhile + takeWhile **/
        textNodesBetween = function(startNode, endNode, rootNode) {
          var allTextNodes = walkTextNodes(rootNode),
              nodesBetween = [],
              len = allTextNodes.length,
              take = false,
              n, i;

          for (i=0; i<len; i++) {
            n = allTextNodes[i];

            if (n === endNode)
              take = false;

            if (take)
              nodesBetween.push(n);

            if (n === startNode)
              take = true;
          }

          return nodesBetween;
        },

        /** Shorthand **/
        surround = function(range, css) {
          var wrapper = document.createElement('SPAN');
          wrapper.className = css;
          range.surroundContents(wrapper);
          return wrapper;
        },

        wrapRange = function(range, cssClass) {
          if (range.startContainer === range.endContainer) {
            return [ surround(range, cssClass) ];
          } else {
            // The tricky part - we need to break the range apart and create
            // sub-ranges for each segment
            var nodesBetween =
              textNodesBetween(range.startContainer, range.endContainer, rootNode);

            // Start with start and end nodes
            var startRange = rangy.createRange();
            startRange.selectNodeContents(range.startContainer);
            startRange.setStart(range.startContainer, range.startOffset);
            var startWrapper = surround(startRange, cssClass);

            var endRange = rangy.createRange();
            endRange.selectNode(range.endContainer);
            endRange.setEnd(range.endContainer, range.endOffset);
            var endWrapper = surround(endRange, cssClass);

            // And wrap nodes in between, if any
            var centerWrappers = jQuery.map(nodesBetween, function(node) {
              var r = rangy.createRange();
              r.selectNodeContents(node);
              return surround(r, cssClass);
            });

            return [ startWrapper ].concat(centerWrappers,  [ endWrapper ]);
          }
        },

        determineCSSClass = function(annotation) {
          var entityType = AnnotationUtils.getEntityType(annotation),
              cssClass = (entityType) ? 'annotation ' + entityType.toLowerCase() : 'annotation';

          return cssClass;
        },

        renderAnnotation = function(annotation) {
          var anchor = annotation.anchor.substr(12),
              quote = AnnotationUtils.getQuote(annotation),
              range = rangy.createRange(),
              spans;

          range.selectCharacters(rootNode.childNodes[0], parseInt(anchor), parseInt(anchor) + quote.length);
          spans = wrapRange(range, determineCSSClass(annotation));

          // Attach annotation data as payload to the SPANs and set id, if any
          jQuery.each(spans, function(idx, span) {
            AnnotationUtils.attachAnnotation(span, annotation);
          });

          return spans;
        },

        removeAnnotation = function(annotation) {
          var spans = jQuery('[data-id="' + annotation.annotation_id + '"]');
          jQuery.each(spans, function(idx, span) {
            var el = jQuery(span);
            el.replaceWith(el.contents());
          });
          rootNode.normalize();
        },

        refreshAnnotation = function(annotation) {
          var spans = jQuery('[data-id=' + annotation.annotation_id + ']');
          spans.removeClass();
          spans.addClass(determineCSSClass(annotation));
          return spans;
        },

        /**
         * Returns all annotations this DOM element is enclosed in.
         *
         * Results are sorted by length, shortest first, so that the 'smallest' annotation
         * is the first in the list.
         */
        getAnnotationsAt = function(element, annotation) {
          // Helper to get all annotations in case of multipe nested annotation spans
          var getAnnotationsRecursive = function(element, a) {
                var annotations = (a) ? a : [ ],
                    parent = element.container;

                annotations.push(element.annotation);

                if (jQuery(parent).hasClass('annotation'))
                  return getAnnotationsRecursive(parent, annotations);
                else
                  return annotations;
              },

              sortByQuoteLength = function(annotations) {
                return annotations.sort(function(a, b) {
                  return AnnotationUtils.getQuote(a).length - AnnotationUtils.getQuote(b).length;
                });
              };

          return sortByQuoteLength(getAnnotationsRecursive(element));
        };

    this.wrapRange = wrapRange;
    this.renderAnnotation = renderAnnotation;
    this.removeAnnotation = removeAnnotation;
    this.refreshAnnotation = refreshAnnotation;
    this.getAnnotationsAt = getAnnotationsAt;
  };

  return Highlighter;

});