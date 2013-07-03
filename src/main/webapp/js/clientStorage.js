/**
 * Contains client storage implementation plugin based on local storage with fallback to cookies storage.
 * @author David Flanagan @ "Javascript: The Definitive Guide"
 */

(function($){
    $.getStorage = function(){
        var DEFAULT_COOKIE_EXPIRATION_TIME = 60 * 60 * 24 * 365;
        return window.localStorage || new CookieStorage(DEFAULT_COOKIE_EXPIRATION_TIME, "/");

        //An implementation of local storage-like API based on cookies
        function CookieStorage(maxage, path){

            //An object that holds all cookies
            var cookies = (function() {
                var cookies = {};
                var cookiesString = document.cookie;
                if (cookiesString === ""){
                    return cookies;
                }
                var list = cookiesString.split("; "); //Splitting cookie string into individual name=value pairs
                for(var i = 0; i < list.length; i++) {
                    var cookie = list[i];
                    var p = cookie.indexOf("=");
                    var name = cookie.substring(0, p);
                    var value = decodeURIComponent(cookie.substring(p + 1));
                    cookies[name] = value;
                }
                return cookies;
            }());

            //Keys array stores a list of existing cookie names
            var keys = [];
            for(var key in cookies){
                keys.push(key);
            }

            //Now define the public properties and methods of the Storage API
            this.length = keys.length; // The number of stored cookies

            //Returns the name of the nth cookie, or null if n is out of range
            this.key = function(n) {
                if ( (n < 0) || (n >= keys.length) ){
                    return null;
                }
                return keys[n];
            };

            //Returns the value of the named cookie, or null.
            this.getItem = function(name) {
                return cookies[name] || null;
            };

            //Stores a value
            this.setItem = function(key, value) {
                if (!(key in cookies)) {
                    keys.push(key);
                    this.length++;
                }
                cookies[key] = value;

                var cookie = key + "=" + encodeURIComponent(value);
                if (!!maxage){
                    cookie += "; max-age=" + maxage;
                }
                if (!!path){
                    cookie += "; path=" + path;
                }

                document.cookie = cookie;
            };

            //Removes the specified cookie
            this.removeItem = function(key) {
                if (!(key in cookies)){
                    return;
                }

                delete cookies[key];

                var keyIndex = keys.indexOf(key); //This is ECMAScript 5 method
                keys.splice(keyIndex,1);

                this.length--;
                document.cookie = key + "=; max-age=0";
            }

            //Removes all cookies
            this.clear = function() {
                for(var i = 0; i < keys.length; i++){
                    document.cookie = keys[i] + "=; max-age=0";
                }

                cookies = {};
                keys = [];
                this.length = 0;
            };
        }
    };
})(jQuery);