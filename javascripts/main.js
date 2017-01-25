---
---

$(document).ready(function() {
	// Setup baguetteBox
	{% for type in site.data.screens %}
		if($("#{{ type.id }}").length) {
			baguetteBox.run("#{{ type.id }}", {});
		}
	{% endfor %}

	// Get and show download counts
	$.getJSON("https://api.github.com/repos/brunonova/drmips/releases/tags/{{ site.cur_ver }}").done(function (release) {
		var asset;
		for(var i = 0; i < release.assets.length; i++) {
			asset = release.assets[i];
			switch(asset.name) {
				case "DrMIPS_{{ site.cur_ver }}.zip":
					$("#download_pc").prop("title", "Downloaded " + asset.download_count + " times");
					break;
				case "DrMIPS_{{ site.cur_ver }}_setup.exe":
					$("#download_pc_setup").prop("title", "Downloaded " + asset.download_count + " times");
					break;
				case "DrMIPS_{{ site.cur_ver }}.apk":
					$("#download_android").prop("title", "Downloaded " + asset.download_count + " times");
					break;
				case "DrMIPS_{{ site.cur_ver }}.tar.xz":
					$("#download_source").prop("title", "Downloaded " + asset.download_count + " times");
					break;
			}
		}
	})
});
