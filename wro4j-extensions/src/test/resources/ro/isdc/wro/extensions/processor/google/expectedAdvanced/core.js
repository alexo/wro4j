var j=true,k=null,l=false;
(function(){function E(a,c){return c.M()}function b(a,c){return new b.c.h(a,c,u)}function v(){if(b.w)return;try{document.D.U("left")}catch(a){setTimeout(v,1);return}b.d()}var F=window.H,G=window.A,u,I=/^(?:[^<]*(<[\w\W]+>)[^>]*$|#([\w\-]*)$)/,w=/\S/,x=/^\s+/,y=/\s+$/,J=/\d/,K=/^<(\w+)\s*\/?>(?:<\/\1>)?$/,L=/^[\],:{}\s]*$/,M=/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g,N=/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,O=/(?:^|:|,)(?:\s*\[)+/g,P=/(webkit)[ \/]([\w.]+)/,Q=/(opera)(?:.*version)?[ \/]([\w.]+)/,
R=/(msie) ([\w.]+)/,S=/(mozilla)(?:.*? rv:([\w.]+))?/,T=/-([a-z])/ig,n=navigator.Xa,o,m,U=Object.prototype.fa,q=Object.prototype.za,r=Array.prototype.J,p=Array.prototype.ba,z=String.prototype.N,C=Array.prototype.W,D={};b.c=b.prototype={j:b,h:function(a,c,e){var d;if(!a)return this;if(a.Z){this.g=this[0]=a;this.a=1;return this}if(a==="body"&&!c&&document.C){this.g=document;this[0]=document.C;this.e=a;this.a=1;return this}if(typeof a==="string"){if(a.R(0)==="<"&&a.R(a.a-1)===">"&&a.a>=3)d=[k,a,k];else d=
I.k(a);if(d&&(d[1]||!c))if(d[1]){e=(c=c instanceof b?c[0]:c)?c.Ka||c:document;if(a=K.k(a))if(b.v(c)){a=[document.T(a[1])];b.c.la.b(a,c,j)}else a=[e.T(a[1])];else{a=b.ma([d[1]],[e]);a=(a.na?b.qa(a.V):a.V).pa}return b.x(this,a)}else{if((c=document.va(d[2]))&&c.La){if(c.Ba!==d[2])return e.F(a);this.a=1;this[0]=c}this.g=document;this.e=a;return this}else if(!c||c.X)return(c||e).F(a);else return this.j(c).F(a)}else if(b.n(a))return e.d(a);if(a.e!==undefined){this.e=a.e;this.g=a.g}return b.Y(a,this)},e:"",
X:"@VERSION",a:0,K:function(a,c,e){var d=this.j();if(b.m(a))r.i(d,a);else b.x(d,a);d.Pa=this;d.g=this.g;if(c==="find")d.e=this.e+(this.e?" ":"")+e;else if(c)d.e=this.e+"."+c+"("+e+")";return d},u:function(a,c){return b.u(this,a,c)},d:function(a){b.Q();o.sa(a);return this},ba:function(){return this.K(p.i(this,arguments),"slice",p.b(arguments).Fa(","))},I:function(a){return this.K(b.I(this,function(c,b){return a.b(c,b,c)}))},J:r,ca:[].ca,da:[].da};b.c.h.prototype=b.c;b.p=b.c.p=function(){var a,c,e,
d,g,f=arguments[0]||{},h=1,i=arguments.a,m=l;if(typeof f==="boolean"){m=f;f=arguments[1]||{};h=2}if(typeof f!=="object"&&!b.n(f))f={};if(i===h){f=this;--h}for(;h<i;h++)if((a=arguments[h])!=k)for(c in a){e=f[c];d=a[c];if(f===d)continue;if(m&&d&&(b.v(d)||(g=b.m(d)))){if(g){g=l;e=e&&b.m(e)?e:[]}else e=e&&b.v(e)?e:{};f[c]=b.p(m,e,d)}else if(d!==undefined)f[c]=d}return f};b.p({Ha:function(a){if(window.A===b)window.A=G;if(a&&window.H===b)window.H=F;return b},w:l,y:1,Aa:function(a){if(a)b.y++;else b.d(j)},
d:function(a){if(a===j&&!--b.y||a!==j&&!b.w){if(!document.C)return setTimeout(b.d,1);b.w=j;if(a!==j&&--b.y>0)return;o.Sa(document,[b]);if(b.c.ga)b(document).ga("ready").Wa("ready")}},Q:function(){if(o)return;o=b.ja();if(document.aa==="complete")return setTimeout(b.d,1);if(document.s){document.s("DOMContentLoaded",m,l);window.s("load",b.d,l)}else if(document.t){document.t("onreadystatechange",m);window.t("onload",b.d);var a=l;try{a=window.ua==k}catch(c){}if(document.D.U&&a)v()}},n:function(a){return b.r(a)===
"function"},m:Array.m||function(a){return b.r(a)==="array"},G:function(a){return a&&typeof a==="object"&&"setInterval"in a},Ea:function(a){return a==k||!J.z(a)||isNaN(a)},r:function(a){return a==k?String(a):D[U.b(a)]||"object"},v:function(a){if(!a||b.r(a)!=="object"||a.Z||b.G(a))return l;if(a.j&&!q.b(a,"constructor")&&!q.b(a.j.prototype,"isPrototypeOf"))return l;for(var c in a);return c===undefined||q.b(a,c)},Da:function(a){for(var c in a)return l;return j},E:function(a){throw a;},Na:function(a){if(typeof a!==
"string"||!a)return k;a=b.N(a);if(window.B&&window.B.$)return window.B.$(a);if(L.z(a.o(M,"@").o(N,"]").o(O,"")))return(new Function("return "+a))();b.E("Invalid JSON: "+a)},Oa:function(a,c,e){if(window.ia){e=new DOMParser;c=e.Ma(a,"text/xml")}else{c=new ActiveXObject("Microsoft.XMLDOM");c.ka="false";c.Ga(a)}e=c.D;if(!e||!e.q||e.q==="parsererror")b.E("Invalid XML: "+a);return c},Ia:function(){},xa:function(a){if(a&&w.z(a))(window.ta||function(a){window.eval.b(window,a)})(a)},oa:function(a){return a.o(T,
E)},q:function(a,c){return a.q&&a.q.M()===c.M()},u:function(a,c,e){var d,g=0,f=a.a,h=f===undefined||b.n(a);if(e)if(h)for(d in a){if(c.i(a[d],e)===l)break}else for(;g<f;){if(c.i(a[g++],e)===l)break}else if(h)for(d in a){if(c.b(a[d],d,a[d])===l)break}else for(;g<f;)if(c.b(a[g],g,a[g++])===l)break;return a},N:z?function(a){return a==k?"":z.b(a)}:function(a){return a==k?"":a.fa().o(x,"").o(y,"")},Y:function(a,c){var e=c||[];if(a!=k){var d=b.r(a);if(a.a==k||d==="string"||d==="function"||d==="regexp"||
b.G(a))r.b(e,a);else b.x(e,a)}return e},Ca:function(a,c){if(C)return C.b(c,a);for(var b=0,d=c.a;b<d;b++)if(c[b]===a)return b;return-1},x:function(a,c){var b=a.a,d=0;if(typeof c.a==="number")for(var g=c.a;d<g;d++)a[b++]=c[d];else for(;c[d]!==undefined;)a[b++]=c[d++];a.a=b;return a},ya:function(a,c,b){for(var d=[],g,b=!!b,f=0,h=a.a;f<h;f++){g=!!c(a[f],f);if(b!==g)d.J(a[f])}return d},I:function(a,c,e){var d,g,f=[],h=0,i=a.a;if(a instanceof b||i!==undefined&&typeof i==="number"&&(i>0&&a[0]&&a[i-1]||i===
0||b.m(a)))for(;h<i;h++){d=c(a[h],h,e);if(d!=k)f[f.a]=d}else for(g in a){d=c(a[g],g,e);if(d!=k)f[f.a]=d}return f.S.i([],f)},l:1,Qa:function(a,c){function e(){return a.i(c,g.S(p.b(arguments)))}if(typeof c==="string")var d=a[c],c=a,a=d;if(!b.n(a))return undefined;var g=p.b(arguments,2);e.l=a.l=a.l||e.l||b.l++;return e},P:function(a,c,e,d,g,f){var h=a.a;if(typeof c==="object"){for(var i in c)b.P(a,i,c[i],d,g,e);return a}if(e!==undefined){d=!f&&d&&b.n(e);for(i=0;i<h;i++)g(a[i],c,d?e.b(a[i],i,g(a[i],c)):
e,f);return a}return h?g(a[0],c):undefined},Ja:function(){return(new Date).wa()},ha:function(a){a=a.ea();a=P.k(a)||Q.k(a)||R.k(a)||a.W("compatible")<0&&S.k(a)||[];return{f:a[1]||"",O:a[2]||"0"}},L:function(){function a(b,c){return new a.c.h(b,c)}b.p(j,a,this);a.Va=this;a.c=a.prototype=this();a.c.j=a;a.L=this.L;a.c.h=function(e,d){if(d&&d instanceof b&&!(d instanceof a))d=a(d);return b.c.h.b(this,e,d,c)};a.c.h.prototype=a.c;var c=a(document);return a},f:{}});b.u("Boolean Number String Function Array Date RegExp Object".Ua(" "),
function(a,b){D["[object "+b+"]"]=b.ea()});n=b.ha(n);if(n.f){b.f[n.f]=j;b.f.O=n.O}if(b.f.Ya)b.f.Ta=j;if(w.z("\u00a0")){x=/^[\s\xA0]+/;y=/[\s\xA0]+$/}u=b(document);if(document.s)m=function(){document.Ra("DOMContentLoaded",m,l);b.d()};else if(document.t)m=function(){if(document.aa==="complete"){document.ra("onreadystatechange",m);b.d()}};return b})();