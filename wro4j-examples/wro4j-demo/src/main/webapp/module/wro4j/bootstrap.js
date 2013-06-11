(function() {
    var wro4j = window.wro4j || {
        groups: [],
        cssGroups: [],
        jsGroups: [],
    };
    var config = wro4j.config || {       
        path: "/wro/"
    };    
    var api = {
        path: "wroAPI/groupNameMapping"
    };
    var groupNameMapping = {
        "1": "1-123",
        "2": "2-234",
        "100": "100-123",
        "200": "200-234",
    }
    //TODO invoke ajax call to api endpoint
    //get("/undefined", function(result){console.log("result is:" + result)});
 
    var groups = wro4j.groups;
    var cssGroups = wro4j.cssGroups;
    var jsGroups = wro4j.jsGroups;
  
    for (var i = 0; i < groups.length; i++) {
        var url = computeUrl(groups[i]);
        loadCss(url + ".css");
        loadCss(url + ".js");
    }
    for (var i = 0; i < cssGroups.length; i++) {
        var url = computeUrl(cssGroups[i]);
        loadCss(url + ".css");    
    }
    for (var i = 0; i < jsGroups.length; i++) {
        var url = computeUrl(jsGroups[i]);
        loadJs(url + ".js");    
    }
    function get(url, callback) {
        var xmlHttp = null;
        xmlHttp = new XMLHttpRequest();
        xmlHttp.open( "GET", url, false );
        xmlHttp.send( null );
        callback(xmlHttp.responseText);
    }
    function computeUrl(groupName) {      
        var hashedValue = groupNameMapping[groupName];        
        var changedGroupName = hashedValue != null ? hashedValue : groupName;
        return config.path + changedGroupName;
    }
    function loadJs(url) {
        var script = document.createElement('script');
        script.type = 'text/javascript';
        script.async = true;
        script.src = url;
        console.log('loadJs:'+url);
        appendChild(script);      
    }    
    function loadCss(url) {
        var link = document.createElement('link');
        link.type = 'text/css';
        link.rel = 'stylesheet';
        link.href = url;
        console.log('load css:'+url);
        appendChild( link );
    }
    function appendChild(child) {
        console.log("child loaded")
        if (false) {
            (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(child);
        }
    }    
})();
