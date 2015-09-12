
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("comment", function(request, response) {
  var postId = request.params.postId;
  var content = request.params.content;
  var userId = request.params.userId;
  if (userId == undefined) {
    userId = "OxTnR4Nt5W";  // id of fitter
  }

  var Post = Parse.Object.extend("Post"); //class
  var post = new Post();
  post.id = postId;

  var user = new Parse.User();
  user.id = userId;

  var COMMENT = Parse.Object.extend("Comment");
  var comment = new COMMENT();
  comment.set("post", post);
  comment.set("user", user);
  comment.set("content", content);
  comment.save(null, {
    success:function(results) {
      response.success("postId:" + postId + ", userId:" + userId + ", content:" + content + ", results:" + results);
    },
    error:function(error) {
      response.success(error);
    }
  });
});

var NOTIFICATION_TYPE = {
  "FOLLOWED_ME":0x0001,
  "LIKE_ON_MY_POST":0x0002,
  "COMMENT_ON_MY_POST":0x0003,
  "COMMENT_ON_MY_COMMENT":0x0004,
  "MENTIONED_ON_A_POST":0x0005,
  "MENTIONED_ON_A_COMMENT":0x0006,
};

Parse.Cloud.afterSave("Relation", function(request) {
  var objectId = request.object.id;
  var relationType = request.object.get("type");
  if (relationType != "follow") {
    console.error("[ERROR] relationType is not follow: " + relationType);
    return;
  }

  var toUser = request.object.get("to");
  var fromUser = request.user;

  if (toUser == null || fromUser == null) {
    console.error("toUser is null. " + request.toString());
    return;
  }

  if (fromUser == null) {
    console.error("fromUser is null. " + request.toString());
    return;
  }

  var Notification = Parse.Object.extend("Notification");
  var notification = new Notification();
  notification.set("type", NOTIFICATION_TYPE.FOLLOWED_ME);

  var payload = {"userId":fromUser.id, };
  notification.set("payload", payload);
  var content = fromUser.getUsername() + "님이 following 하고 있습니다.";
  notification.set("content", content);
  notification.set("read", false);
  notification.setACL(new Parse.ACL(toUser));

  notification.save(null, {
    success: function(notification) {
      console.log("new notification, type:" + NOTIFICATION_TYPE.FOLLOWED_ME + ", content:" + content);
      var query = new Parse.Query(Parse.Installation);
      query.equalTo('user', toUser);

      Parse.Push.send({
        where: query,
        data: {
          id: notification.id,
          type: NOTIFICATION_TYPE.FOLLOWED_ME,
          alert: content,
          followerId:fromUser.id
        }
      }, {
        success: function() {
          // Push was successful
          console.log("push success");
        },
        error: function(error) {
          console.log("[ERROR] push fail, error:" + error.code + ":" + error.message);
        }
      });
    },
    error: function(notification, error) {
      console.error('Failed to create new object, with error code: ' + error.message);
    }
  });
});

Parse.Cloud.afterSave("Comment", function(request) {
  var postId = request.object.get("post").id;
  var commentWriter = request.user;

  var Post = Parse.Object.extend("Post"); //class
  var query = new Parse.Query(Post);
  query.equalTo("objectId", postId);
  query.find({
    success: function(results) {
      if (results.length < 1) {
        console.error("post is not existed. postId:" + postId);
        return;
      }

      var post = results[0];
      var comment = request.object;
      if (!comment.existed()) {
          post.increment("commentCount", 1);
      }
      post.set("lastComment", comment);
      post.save(null, {
        success:function() {

        },
        error:function() {
          console.error("fail to save post, " + postId);
        }
      });

      var user = post.get("user");
      if (user.id == commentWriter.id) {
        return;
      }

      sendMentionPushIfMentioned(commentWriter, NOTIFICATION_TYPE.MENTIONED_ON_A_COMMENT, postId, request.object.get("content"), user);

      var Notification = Parse.Object.extend("Notification");
      var notification = new Notification();
      notification.set("type", NOTIFICATION_TYPE.COMMENT_ON_MY_POST);

      var payload = {"userId":commentWriter.id, "postId":postId, "commentId":request.object.id};
      notification.set("payload", payload);
      var commentContent = request.object.get("content");
      var content = commentWriter.getUsername() + "님이 댓글을 남겼습니다. \"" + getShortString(commentContent, 40) + "\"";
      notification.set("content", content);
      notification.set("read", false);
      notification.setACL(new Parse.ACL(user));

      notification.save(null, {
        success: function(notification) {
          console.log("new notification, type:" + NOTIFICATION_TYPE.COMMENT_ON_MY_POST + ", postId:"+ postId + ", content:" + content);
          var query = new Parse.Query(Parse.Installation);
          query.equalTo('user', user);

          Parse.Push.send({
            where: query,
            data: {
              id: notification.id,
              type: NOTIFICATION_TYPE.COMMENT_ON_MY_POST,
              alert: content,
              postId: postId,
              userId: user.id
            }
          }, {
            success: function() {
              // Push was successful
              console.log("push success");
            },
            error: function(error) {
              console.log("[ERROR] push fail, error:" + error.code + ":" + error.message);
            }
          });

        },
        error: function(notification, error) {
          console.error('Failed to create new object, with error code: ' + error.message);
        }
      });
    },
    error: function(error) {
      console.error("Error: " + error.code + " " + error.message);
    }
  });
});

Parse.Cloud.afterSave("Post", function(request) {
  var post = request.object;
  if (post.has("commentCount")) {
    return;
  }

  var postId = post.id;
  var user = request.user;
  var content = post.get("content");

  sendMentionPushIfMentioned(user, NOTIFICATION_TYPE.MENTIONED_ON_A_POST, postId, content);

  var slack = require('cloud/slack.js');
  var fitLogSummary = post.get("fitLogSummary");
  if (fitLogSummary == undefined) {
    fitLogSummary = "";
  }

  var text = "[" + postId + "] " + content + fitLogSummary;
  slack.sendMessage(user.getUsername(), text);
});

function sendMentionPushIfMentioned(user, type, postId, content, except) {
  var mentionedUsers = findMentionedUsers(content);
  var notificationContent = user.getUsername() + "님이 회원님을 언급하였습니다." + "\"" + getShortString(content, 40) + "\"";

  for (var i in mentionedUsers) {
    var mentionedUserId = mentionedUsers[i];
    if (mentionedUserId == user.getUsername()) {
      continue;
    }

    var query = new Parse.Query(Parse.User);
    query.equalTo("username", mentionedUserId);
    query.find({
      success: function(results) {
        if (results.count != 0) {
          var toUser = results[0];
          if (except != undefined && toUser.id == except.id) {
            return;
          }
          sendMentionPush(user, toUser, type, postId, notificationContent);
        }
      },
      error: function(error) {
        console.error("cannot find username: " + mentionedUserId);
      }
    });
  }

  return mentionedUsers;
}

function getShortString(string, maxLength) {
  if(string.length <= maxLength) {
    return string;
  }

  return string.substring(0, maxLength) + "...";
}

function sendMentionPush (fromUser, toUser, type, postId, content) {
  var Notification = Parse.Object.extend("Notification");
  var notification = new Notification();
  notification.set("type", type);

  var payload = {"postId":postId, "userId":fromUser.id};
  notification.set("payload", payload);
  notification.set("content", content);
  notification.set("read", false);
  notification.setACL(new Parse.ACL(toUser));

  notification.save(null, {
    success: function(notification) {
      console.log("new notification, type:" + type + ", postId:"+ postId + ", content:" + content);
      var query = new Parse.Query(Parse.Installation);
      query.equalTo('user', toUser);

      Parse.Push.send({
        where: query,
        data: {
          id: notification.id,
          type: type,
          alert: content,
          postId: postId,
          userId: fromUser.id
        }
      }, {
        success: function() {
          // Push was successful
          console.log("push success");
        },
        error: function(error) {
          console.log("[ERROR] push fail, error:" + error.code + ":" + error.message);
        }
      });

    },
    error: function(notification, error) {
      console.error('Failed to create new object, with error code: ' + error.message);
    }
  });
}

function findMentionedUsers (content) {
  var mentionedUsers = content.match(/\"<@.*?>\"/g);
  console.log("mentionedUsers: " + mentionedUsers);

  var results = new Array();
  for (var i in mentionedUsers) {
    var string = mentionedUsers[i];
    var length = string.length;
    var userId = string.substring(3, length-2);
    results.push(userId);
  }

  console.log("results: " + results);
  return results;
}
