/**
 * Created by hyungchulkim on 8/20/15.
 */
module.exports = {
    sendMessage: function(username, text) {
        var json = {"channel": "#fitter", "username": username, "text":text, "icon_emoji": ":ghost:"};
        Parse.Cloud.httpRequest({
            method: "POST",
            headers: {"Content-Type": "application/json; charset=UTF-8"},
            url: "https://hooks.slack.com/services/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",
            body: json
        }).then(function(httpResponse) {

        }, function(httpResponse) {
            console.error("error1: " + httpResponse.text);
        });
    }
}
