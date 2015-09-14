---
---

$(document).ready(function() {
	{% for type in site.data.screens %}
		if($("#{{ type.id }}").length) {
			baguetteBox.run("#{{ type.id }}", {});
		}
	{% endfor %}
});
